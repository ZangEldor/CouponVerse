package com.example.couponverse

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setMargins
import com.example.couponverse.data.User
import com.example.couponverse.data.Group
import com.example.couponverse.data.Message
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity for displaying and managing group members.
 *
 * Provides functionality to:
 * - Display the group's image and list of members.
 * - Handle user interactions through a popup menu.
 * - Start a new chat with a selected member.
 */
class GroupMembersActivity : AppCompatActivity() {

    private val serverAPIManager = RetrofitAPIManager.getService(this)
    private lateinit var group: Group
    private lateinit var currentUser: User
    private lateinit var usersContainer: LinearLayout
    private lateinit var groupImage: ImageView
    private var usersList: List<User>? = null

    /**
     * Initializes the activity, setting up UI elements and loading group and user data.
     *
     * @param savedInstanceState The saved instance state for restoring the activity's state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_group_members)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        group = intent.extras!!.getSerializable("groupObject") as Group
        currentUser = intent.extras!!.getSerializable("userObject") as User

        usersContainer = findViewById<LinearLayout>(R.id.usersContainer)
        groupImage = findViewById<ImageView>(R.id.groupImage)

        // display group image
        if (group.picture != null) {
            try {
                val decodedImage = Base64.decode(group.picture, Base64.DEFAULT)
                val imageBitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                groupImage.setImageBitmap(imageBitmap)
            } catch (e: IllegalArgumentException) { // decoding fails
                groupImage.setImageDrawable(ContextCompat.getDrawable(groupImage.context, R.drawable.user_icon))
            }
        }
        else {
            groupImage.setImageDrawable(ContextCompat.getDrawable(groupImage.context, R.drawable.user_icon))
        }

        // get detailed users info from the server (needed to be able to display images)
        serverAPIManager.getUsersFromList(group.users, object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    usersList = response.body()
                    displayGroupMembers()
                } else {
                    Toast.makeText(this@GroupMembersActivity, "Failed to load users.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@GroupMembersActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Displays the list of group members, including the current user as the first.
     */
    private fun displayGroupMembers() {
        val safeCastUsers = usersList as List<User>
        if (safeCastUsers.isEmpty())
            return
        val sortedGroupUsers = safeCastUsers.sortedBy { it.userName }

        // display users
        val inflater = LayoutInflater.from(this)
        // display current user first
        displayMember(inflater, currentUser)
        for (user in sortedGroupUsers) { // display rest of users
            if (user.id != currentUser.id)
                displayMember(inflater, user)
        }

        // remove margin from last view in container
        if (usersContainer.childCount == 0)
            return
        val lastUser: View = usersContainer.getChildAt(usersContainer.childCount - 1)
        (lastUser.layoutParams as LinearLayout.LayoutParams).setMargins(0)
    }

    /**
     * Inflates and displays a single user's view in the container.
     *
     * @param inflater The LayoutInflater used to inflate the user view.
     * @param user The user to be displayed.
     */
    private fun displayMember(inflater : LayoutInflater, user: User) {
        // create new user view
        val userView: View = inflater.inflate(R.layout.user_item, usersContainer, false)
        // set margin between users
        (userView.layoutParams as LinearLayout.LayoutParams).setMargins(0, 0, 0 ,12)

        // set image
        val usrImgView: ImageView = userView.findViewById<ImageView>(R.id.userProfile)
        if (user.picture != null) {
            try {
                val decodedImage = Base64.decode(user.picture, Base64.DEFAULT)
                val imageBitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                usrImgView.setImageBitmap(imageBitmap)
            } catch (e: IllegalArgumentException) { // decoding fails
                usrImgView.setImageDrawable(ContextCompat.getDrawable(usrImgView.context, R.drawable.user_icon))
            }
        }
        else {
            usrImgView.setImageDrawable(ContextCompat.getDrawable(usrImgView.context, R.drawable.user_icon))
        }

        userView.findViewById<TextView>(R.id.userName).text = user.userName

        userView.setOnClickListener { view ->
            showPopupMenu(view)
        }

        // add the view to the container
        usersContainer.addView(userView)
    }

    /**
     * Shows a popup menu with options for the user.
     *
     * @param view The view to anchor the popup menu.
     */
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.user_popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.copy_text -> { // copies the user's username to the clipboard
                    val clipboard = view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val username = view.findViewById<TextView>(R.id.userName).text.toString()
                    val clip = ClipData.newPlainText("Copied Username", username)
                    clipboard.setPrimaryClip(clip)
                    true
                }
                /*
                R.id.start_group -> {
                    beginChatWithUser(view)
                    true
                }
                 */
                else -> false
            }
        }
        popupMenu.show()
    }

    /**
     * Initiates a chat (group with 2 members) with the selected user by creating a new group.
     *
     * @param view The view that triggered this method.
     */
    private fun beginChatWithUser(view: View) {
        // create the new group with the current user and chosen user
        val usersList: List<String> = listOf(currentUser.userName!!,
            view.findViewById<TextView>(R.id.userName).text.toString())
        val adminsList: List<String> = usersList
        val messages: List<Message> = listOf()

        val newGroup = Group(
            name = "",
            admins = adminsList,
            users = usersList,
            messages = messages)

        // call the server to create the group
        serverAPIManager.postNewGroup(newGroup ,object : Callback<Group> {
            override fun onResponse(call: Call<Group>, response: Response<Group>) {
                if (response.isSuccessful) { // if group creation was successful
                    val finalGroup = response.body() as Group
                    // move into new activity to display the group
                    val intent = Intent(applicationContext, CommunityActivity::class.java)
                    intent.putExtra("groupObject", finalGroup)
                    intent.putExtra("userObject", currentUser)
                    startActivity(intent)
                } else { // group creation didn't succeed
                    val failedGroupCreation = "Failed to create chat with user. Please try again"
                    Toast.makeText(this@GroupMembersActivity, failedGroupCreation, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Group>, t: Throwable) { // call failed
                Toast.makeText(this@GroupMembersActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}