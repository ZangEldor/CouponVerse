package com.example.couponverse

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
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
import com.example.couponverse.data.Group
import com.example.couponverse.data.Message
import com.example.couponverse.data.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity for browsing users and managing user interactions.
 *
 * Provides functionality to:
 * - Search for users by name.
 * - Display a list of recommended users.
 * - Show user details with a popup menu for further actions.
 * - Start a chat with a selected user.
 */
class BrowseUsersActivity : AppCompatActivity() {

    private val serverAPIManager = RetrofitAPIManager.getService(this)
    private lateinit var searchUsersField: EditText
    private lateinit var usersContainer: LinearLayout
    private lateinit var userProfilePic: ImageView
    private var recommendedUsers: List<User>? = null
    private var sortedUsers: List<User>? = null
    private lateinit var currentUser: User

    /**
     * Initializes the activity, setting up UI elements and listeners.
     *
     * @param savedInstanceState The saved instance state for restoring the activity's state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_browse_users)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentUser = intent.extras!!.getSerializable("userObject") as User

        searchUsersField = findViewById<EditText>(R.id.searchUsersField)
        usersContainer = findViewById<LinearLayout>(R.id.usersContainer)
        userProfilePic = findViewById<ImageView>(R.id.userProfile)

        if (currentUser.picture != null) {
            try {
                val decodedImage = Base64.decode(currentUser.picture, Base64.DEFAULT)
                val imageBitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                userProfilePic.setImageBitmap(imageBitmap)
            } catch (e: IllegalArgumentException) { // decoding fails
                userProfilePic.setImageDrawable(ContextCompat.getDrawable(userProfilePic.context, R.drawable.user_icon))
            }
        }
        else {
            userProfilePic.setImageDrawable(ContextCompat.getDrawable(userProfilePic.context, R.drawable.user_icon))
        }

        searchUsersField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()

                if (query.isNotEmpty())
                    lookupUsersByName(query)
            }
        })
    }

    /**
     * Looks up users by name through a server API call.
     *
     * @param query The search query for looking up the users.
     */
    private fun lookupUsersByName(query: String) {
        serverAPIManager.getUsersRecommendations(query, object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    recommendedUsers = response.body()
                    manageUserDisplay()
                } else {
                    Toast.makeText(this@BrowseUsersActivity, "Failed to load user recommendations.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@BrowseUsersActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Manages the display of user recommendations by sorting and updating the UI.
     */
    private fun manageUserDisplay() {
        var safeCastList: List<User> = listOf()
        if (!recommendedUsers.isNullOrEmpty()) {
            safeCastList = recommendedUsers as List<User>
            sortedUsers = safeCastList.sortedBy { it.userName!!.lowercase() }
        }
        displayUserRecommendations()
    }

    /**
     * Displays the list of recommended users in the UI.
     */
    private fun displayUserRecommendations() {
        if (recommendedUsers.isNullOrEmpty())
            return
        usersContainer.removeAllViews()
        val safeCastUsers: List<User> = sortedUsers as List<User>
        val inflater = LayoutInflater.from(this)
        var lastUserDisplayed: View? = null
        for (user in safeCastUsers) {
            if (user.id == currentUser.id)
                continue
            // create new user view
            val userView: View = inflater.inflate(R.layout.user_item, usersContainer, false)
            // set margin between users
            (userView.layoutParams as LinearLayout.LayoutParams).setMargins(0, 0, 0 ,15)

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
            lastUserDisplayed = userView
        }
        if (lastUserDisplayed == null)
                return
        (lastUserDisplayed.layoutParams as LinearLayout.LayoutParams).setMargins(0)
    }

    /**
     * Shows a popup menu with options for the selected user.
     *
     * @param view The view that triggered this method.
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
            messages = messages
        )

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
                    Toast.makeText(this@BrowseUsersActivity, failedGroupCreation, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Group>, t: Throwable) { // call failed
                Toast.makeText(this@BrowseUsersActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}