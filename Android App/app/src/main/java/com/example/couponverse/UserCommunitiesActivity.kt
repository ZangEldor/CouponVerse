package com.example.couponverse

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setMargins
import com.example.couponverse.data.Group
import com.example.couponverse.data.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity for browsing the groups the current user is a part of.
 *
 * Provides functionality to:
 * -
 */
class UserCommunitiesActivity : AppCompatActivity() {

    private val serverAPIManager = RetrofitAPIManager.getService(this)
    private lateinit var groupsContainer: LinearLayout
    private var currentUser: User? = null
    private var groupsList: List<Group>? = null

    /**
     * Initializes the activity, setting up UI elements and loading the user's groups.
     *
     * @param savedInstanceState The saved instance state for restoring the activity's state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_communities)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        groupsContainer = findViewById<LinearLayout>(R.id.groupsContainer)
        currentUser = intent.extras!!.getSerializable("userObject") as User

        val safeCastUser: User = currentUser as User
        val usrImgView: ImageView = findViewById<ImageView>(R.id.user_pfp)
        if (safeCastUser.picture != null) {
            try {
                val decodedImage = Base64.decode(safeCastUser.picture, Base64.DEFAULT)
                val imageBitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                usrImgView.setImageBitmap(imageBitmap)
            } catch (e: IllegalArgumentException) { // decoding fails
                usrImgView.setImageDrawable(ContextCompat.getDrawable(usrImgView.context, R.drawable.user_icon))
            }
        }
        else {
            usrImgView.setImageDrawable(ContextCompat.getDrawable(usrImgView.context, R.drawable.user_icon))
        }

        // get groupsList from the server
        val user: User = currentUser as User
        serverAPIManager.getUserGroups(user.userName, object :
            Callback<List<Group>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<Group>>, response: Response<List<Group>>) {
                if (response.isSuccessful && response.body().isNullOrEmpty()) {
                    groupsList = response.body()
                    manageDisplayGroupsList() // begin groups display upon receiving of the list
                } else {
                    Toast.makeText(this@UserCommunitiesActivity, "Failed to load user's groups.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Group>>, t: Throwable) {
                Toast.makeText(this@UserCommunitiesActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                groupsList = null
            }
        })
    }

    /**
     * Manages the display of groups by sorting and setting up the UI.
     */
    private fun manageDisplayGroupsList() {
        // if the list of groups is not empty or null begin sorting them by Name
        var safeCastList: List<Group> = listOf()
        if (!groupsList.isNullOrEmpty()) {
            safeCastList = groupsList as List<Group>
            groupsList = safeCastList.sortedBy { it.name.lowercase() }
        }

        displayGroups() // call function to display the groups in the container
    }

    /**
     * Displays the list of user's groups in the UI.
     */
    private fun displayGroups() {
        // select the corresponding list to the sorting option selected by the user
        if (groupsList.isNullOrEmpty()) {
            return
        }
        // remove all current views inside the container and begin display of coupons
        groupsContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)
        val safeCastList: List<Group> = groupsList as List<Group>
        var lastGroupViewDisplayed : View? = null
        for (group in safeCastList) {
            // create new group view
            val groupView: View = inflater.inflate(R.layout.community_item, groupsContainer, false)
            // set margin between groups
            (groupView.layoutParams as LinearLayout.LayoutParams).setMargins(0, 0, 0 ,20)

            groupView.id = safeCastList.indexOf(group)
            groupView.findViewById<TextView>(R.id.communityName).text = group.name

            if (group.messages.isNotEmpty()) {
                groupView.findViewById<TextView>(R.id.communityRecentMessage).text =
                    group.messages.last().data
            }

            // set image
            val grpImgView: ImageView = groupView.findViewById<ImageView>(R.id.communityImage)
            if (group.picture != null) {
                try {
                    val decodedImage = Base64.decode(group.picture, Base64.DEFAULT)
                    val imageBitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                    grpImgView.setImageBitmap(imageBitmap)
                } catch (e: IllegalArgumentException) { // decoding fails
                    grpImgView.setImageDrawable(ContextCompat.getDrawable(grpImgView.context, R.drawable.communities_icon))
                }
            }
            else {
                grpImgView.setImageDrawable(ContextCompat.getDrawable(grpImgView.context, R.drawable.communities_icon))
            }

            groupView.setOnClickListener { view ->
                gotoGroup(view)
            }

            groupView.setOnLongClickListener { view ->
                showPopupMenu(view)
                true
            }

            groupsContainer.addView(groupView)
            lastGroupViewDisplayed = groupView
        }
        // remove margins from last view
        if (lastGroupViewDisplayed == null)
                return
        (lastGroupViewDisplayed.layoutParams as LinearLayout.LayoutParams).setMargins(0)
    }

    /**
     * Displays the community chosen by the user.
     *
     * @param view The view that triggered this method.
     */
    private fun gotoGroup(view: View) {
        val intent = Intent(applicationContext, CommunityActivity::class.java)
        intent.putExtra("userObject", currentUser)
        intent.putExtra("groupObject", groupsList?.get(view.id))
        startActivity(intent)
    }

    /**
     * Shows a popup menu with options to edit or leave a group.
     *
     * @param view The view that triggered this method.
     */
    private fun showPopupMenu(view: View) {
        val group = groupsList?.get(view.id)!!
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.community_popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_group -> {
                    editGroup(group)
                    true
                }
                R.id.leave_group -> {
                    leaveGroup(group)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    /**
     * Checks if user can edit the group, if so then starts the activity to edit the group.
     *
     * @param group The group to be edited.
     */
    private fun editGroup(group: Group) {
        if (!group.admins.contains(currentUser!!.userName)) {
            Toast.makeText(this@UserCommunitiesActivity, "Not allowed - only admins can edit groups.", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(applicationContext, GroupSettingsActivity::class.java)
        intent.putExtra("userObject", currentUser)
        intent.putExtra("groupObject", group)

        // start the activity while knowing to expect a result
        val editGroupLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val isUpdated = result.data?.getBooleanExtra("group_updated", false) ?: false
                if (isUpdated)
                    refreshGroupListFromServer()
            }
        }
        editGroupLauncher.launch(intent)
    }

    /**
     * Refreshes the list of user's groups from the server after a group has been edited.
     */
    private fun refreshGroupListFromServer() {
        // get groupsList from the server
        val user: User = currentUser as User
        serverAPIManager.getUserGroups(user.userName, object :
            Callback<List<Group>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<Group>>, response: Response<List<Group>>) {
                if (response.isSuccessful && response.body().isNullOrEmpty()) {
                    groupsList = response.body()
                    manageDisplayGroupsList() // begin groups display upon receiving of the list
                } else {
                    Toast.makeText(this@UserCommunitiesActivity, "Failed to load user's groups.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Group>>, t: Throwable) {
                Toast.makeText(this@UserCommunitiesActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                groupsList = null
            }
        })
    }

    /**
     * Attempts to leave the group via the server API.
     *
     * @param group The group the user wishes to leave.
     */
    private fun leaveGroup(group: Group) {
        serverAPIManager.removeUserFromGroup(currentUser!!.userName, group.name, object : Callback<Void>{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    refreshCouponList(group)
                } else {
                    Toast.makeText(this@UserCommunitiesActivity, "Failed to leave group.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@UserCommunitiesActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Refreshes the groups list after the user left a group.
     *
     * @param group the group the user left.
     */
    private fun refreshCouponList(group: Group) {
        groupsList = groupsList?.filter { it != group } // remove old group from list
        displayGroups()
    }
}