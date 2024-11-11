package com.example.couponverse

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

/**
 * Activity for managing community's name, photo and its members.
 *
 * Provides functionality to:
 * - Select and update the community's name and display picture.
 * - Update on server and alert caller activity of changes.
 */
class GroupSettingsActivity : AppCompatActivity() {

    private val serverAPIManager = RetrofitAPIManager.getService(this)
    private lateinit var currentUser: User
    private lateinit var currentGroup: Group
    private lateinit var groupNameEdit: EditText
    private lateinit var groupPictureView: ImageView
    private lateinit var membersContainer: LinearLayout
    private val MAX_IMAGE_WIDTH_HEIGHT = 360

    /**
     * Initializes the activity, setting up UI elements and loading current settings.
     *
     * @param savedInstanceState The saved instance state for restoring the activity's state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_group_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentUser = intent.extras!!.getSerializable("userObject") as User
        currentGroup = intent.extras!!.getSerializable("groupObject") as Group

        groupNameEdit = findViewById<TextInputEditText>(R.id.community_name_edit)
        groupPictureView = findViewById<ImageView>(R.id.picture_view)
        membersContainer = findViewById<LinearLayout>(R.id.membersContainer)

        loadGroupInfo()
    }

    /**
     * Loads the current group's relevant information into the UI components.
     */
    private fun loadGroupInfo() {
        findViewById<EditText>(R.id.community_name_edit).setText(currentGroup.name)
        if (!currentGroup.picture.isNullOrEmpty()) {
            try {
                val decodedImage = Base64.decode(currentUser.picture, Base64.DEFAULT)
                val imageBitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                groupPictureView.setImageBitmap(imageBitmap)
            } catch (e: IllegalArgumentException) { // decoding fails
                groupPictureView.setImageDrawable(ContextCompat.getDrawable(groupPictureView.context, R.drawable.communities_icon))
            }
        }
        else {
            groupPictureView.setImageDrawable(ContextCompat.getDrawable(groupPictureView.context, R.drawable.communities_icon))
        }
        // get list of admins ordered alphabetically first and then other users
        val sortedAdminsList: List<String> = currentGroup.admins.sortedBy { it.lowercase() }
        val usersListNoAdmins = currentGroup.users.filter { it !in sortedAdminsList }
        val sortedUsersListNoAdmins: List<String> = usersListNoAdmins.sortedBy { it.lowercase() }

        displayMembers(sortedAdminsList, sortedUsersListNoAdmins)
    }

    /**
     * Displays all group members in the following order - first admins in alphabetical order
     * (excludes current user) and then the rest of the users in alphabetical order.
     *
     * @param sortedAdminsList The sorted list of admins.
     * @param sortedUsersListNoAdmins The sorted list of non-admin users.
     */
    private fun displayMembers(sortedAdminsList: List<String>, sortedUsersListNoAdmins: List<String>) {
        membersContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)
        var lastMemberDisplayed: View? = null
        for (admin in sortedAdminsList) {
            // skip current user (necessarily admin) so no self-changes will happen
            if (admin == currentUser.userName)
                continue

            val memberView: View = inflater.inflate(R.layout.community_member_item, membersContainer, false)
            // set margin between members
            (memberView.layoutParams as LinearLayout.LayoutParams).setMargins(0, 0, 0 ,6)

            memberView.id = sortedAdminsList.indexOf(admin)
            memberView.findViewById<TextView>(R.id.userName).text = admin
            memberView.findViewById<CheckBox>(R.id.is_admin).isChecked = true
            memberView.findViewById<CheckBox>(R.id.to_remove).isChecked = false

            membersContainer.addView(memberView)
            lastMemberDisplayed = memberView
        }
        for (user in sortedUsersListNoAdmins) {
            val memberView: View = inflater.inflate(R.layout.community_member_item, membersContainer, false)
            // set margin between members
            (memberView.layoutParams as LinearLayout.LayoutParams).setMargins(0, 0, 0 ,6)

            memberView.id = sortedAdminsList.indexOf(user)
            memberView.findViewById<TextView>(R.id.userName).text = user
            memberView.findViewById<CheckBox>(R.id.is_admin).isChecked = false
            memberView.findViewById<CheckBox>(R.id.to_remove).isChecked = false

            membersContainer.addView(memberView)
            lastMemberDisplayed = memberView
        }
        if (lastMemberDisplayed == null)
            return
        (lastMemberDisplayed.layoutParams as LinearLayout.LayoutParams).setMargins(0)
    }

    /**
     * Opens the image picker for selecting a new community picture.
     *
     * @param view The view that triggered this method.
     */
    fun selectImage(view: View) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 3)
    }

    /**
     * Handles the result of the image picker, updating the community picture if a new image was selected.
     *
     * @param requestCode The request code passed in `startActivityForResult`.
     * @param resultCode The result code returned by the child activity.
     * @param data The data returned by the child activity.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null && data.data != null) {
            val selectedImage: Uri? = data.data
            groupPictureView.setImageURI(selectedImage)
            resizeImage()
        }
    }

    /**
     * Resizes the selected profile picture to fit within a predefined dimension square while
     * maintaining aspect ratio.
     */
    private fun resizeImage() {
        val drawable = groupPictureView.drawable ?: return
        val bitmap = (drawable as BitmapDrawable).bitmap

        val width = bitmap.width
        val height = bitmap.height
        val aspectRatio = width.toFloat() / height.toFloat()

        var newWidth = MAX_IMAGE_WIDTH_HEIGHT
        var newHeight = MAX_IMAGE_WIDTH_HEIGHT
        if (width > height) {
            newHeight = (newWidth / aspectRatio).toInt()
        } else {
            newWidth = (newHeight * aspectRatio).toInt()
        }

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        groupPictureView.setImageBitmap(resizedBitmap)
    }

    /**
     * Converts the community's profile picture to a Base64-encoded string.
     *
     * @return The Base64-encoded string representation of the image.
     */
    private fun getGroupImageBase64() : String? {
        val drawable = groupPictureView.drawable ?: return null
        val bitmap = (drawable as BitmapDrawable).bitmap
        // convert the Bitmap to a Base64-encoded string
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * Sends a request to update the group's information on the server.
     *
     * @param view The view that triggered this method.
     */
    fun saveChangesToServer(view: View) {
        val newMembersList = kotlin.collections.mutableListOf<String>()
        val newAdminsList = kotlin.collections.mutableListOf<String>()
        val memberCount = membersContainer.childCount

        for (i in 0 until memberCount) {
            val memberView = membersContainer.getChildAt(i)

            // if member is to be removed dont add to any list
            val toRemoveCheckBox = memberView.findViewById<CheckBox>(R.id.to_remove)
            if (toRemoveCheckBox.isChecked)
                continue

            // add member to new members list
            val userNameTextView = memberView.findViewById<TextView>(R.id.userName)
            val userName = userNameTextView.text.toString()
            newMembersList.add(userName)

            // if member is admin add to new admins list
            val isAdminCheckBox = memberView.findViewById<CheckBox>(R.id.is_admin)
            if (isAdminCheckBox.isChecked)
                newAdminsList.add(userName)
        }

        // keep the current user in the group
        newAdminsList.add(currentUser.userName)
        newMembersList.add(currentUser.userName)

        val updatedGroup = Group(
            id = currentGroup.id,
            name = groupNameEdit.text.toString(),
            admins = newAdminsList.toList(),
            users = newMembersList.toList(),
            messages = currentGroup.messages,
            picture = getGroupImageBase64()
        )

        val TAG = "CHECK_RESPONSE_GROUPUPDATE"
        serverAPIManager.updateGroupByID(updatedGroup.id!!, updatedGroup, object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // successful response
                if  (response.isSuccessful) { // update locally and go to main menu
                    currentGroup = updatedGroup
                    finish()
                    //alertCommunityRefresh()
                }
                else { // update failed
                    Log.i(TAG, "onResponse: unsuccessful - Code ${response.code()}}")
                    val updateFailed = "Failed updating group. Please try again."
                    Toast.makeText(this@GroupSettingsActivity, updateFailed, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.i(TAG, "onFailure: ${t.message}")
                val updateFailed = "Failed updating group. Please try again."
                Toast.makeText(this@GroupSettingsActivity, updateFailed, Toast.LENGTH_SHORT).show()
            }
        })
    }


    /**
     * Triggers a refresh of the coupon list in the browsing activity.
     */
    private fun alertCommunityRefresh() {
        val resultIntent = Intent()
        resultIntent.putExtra("coupon_updated", true)
        setResult(android.app.Activity.RESULT_OK, resultIntent)
    }
}