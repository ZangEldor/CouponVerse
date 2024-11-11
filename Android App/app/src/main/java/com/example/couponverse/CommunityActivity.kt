package com.example.couponverse

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.couponverse.data.Group
import com.example.couponverse.data.User
import com.example.couponverse.data.Message
import com.google.gson.Gson
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter


/**
 * Activity for displaying community details and managing interactions with the community.
 *
 * Provides functionality to:
 * - Display the community name with underline formatting.
 * - View the members of the community.
 * - Open additional options for the community.
 */
class CommunityActivity : AppCompatActivity() {

    private val serverAPIManager = RetrofitAPIManager.getService(this)
    private lateinit var preferencesHelper: PreferencesManager

    private lateinit var group: Group
    private lateinit var currentUser: User
    private var groupUsers: List<User>? = null
    private lateinit var chatContainer: LinearLayout
    private lateinit var chatScrollView: ScrollView
    private lateinit var hubConnection: HubConnection
    /**
     * Initializes the activity, setting up UI elements and loading community details.
     *
     * @param savedInstanceState The saved instance state for restoring the activity's state.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        preferencesHelper = PreferencesManager(this)
        group = intent.extras!!.getSerializable("groupObject") as Group
        currentUser = intent.extras!!.getSerializable("userObject") as User

        // add underline to community name
        val communityNameView: TextView = findViewById(R.id.community_name)
        var name = SpannableString(group.name)
        //name.setSpan(UnderlineSpan(), 10, 19, 0)
        communityNameView.text = getCommunityName()

        // set chat area
        chatContainer = findViewById(R.id.chatContainer)
        chatScrollView = findViewById(R.id.scrollChatSelf)
        // display messages
        displayMessages(group.messages)

        // set message sending area
        val messageInput = findViewById<EditText>(R.id.messageInput)
        val sendMessageButton = findViewById<Button>(R.id.sendMessageButton)

        sendMessageButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()

            if (messageText.isNotEmpty()) {
                // Send message to server
                sendMessageToServer(messageText)

                // Clear the input field
                messageInput.text.clear()
            }
        }

        // Initialize SignalR connection
        setupSignalRConnection()

        // get detailed users info from the server (needed for images)
        serverAPIManager.getUsersFromList(group.users, object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    groupUsers = response.body()
                    displayGroupImage()
                }
                displayGroupImage()
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@CommunityActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Send the entered message to the server
     *
     */
     @RequiresApi(Build.VERSION_CODES.O)
     private fun sendMessageToServer(messageText: String)  {
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val message = Message(
            sender = currentUser.userName,
            data = messageText,
            timestamp_string = currentDate
        )
        serverAPIManager.sendmessage(group.id,message, object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    addMessage(message)
                } else {
                    Toast.makeText(
                        this@CommunityActivity,
                        "Failed to send message",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@CommunityActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }




    /**
     * Refreshes activity when resumed (to include group updates)
     *
     */
    override fun onResume() {
        super.onResume()
        refreshGroupFromServer()
    }

    /**
     * Determines the appropriate community name to display based on group users.
     *
     * @return The community name to be displayed.
     */
    private fun getCommunityName() : String {
        // if group name is not blank or more than 2 users (not a private chat)
        if (group.name.isNotEmpty() || group.users.count() > 2)
            return group.name
        if (group.users.count() < 2) // if group has only one user
            return group.users[0]
        if (group.users[0] == currentUser.userName) // find the user that doesn't match the current
            return group.users[1]
        return group.users[0]
    }

    /**
     * Gets the image corresponding to the community, and displays it
     *
     * @param currentUser The current user.
     * @return The URL or identifier for the community image, or null if not applicable.
     */
    private fun displayGroupImage() {
        val communityImg: String? = getCommunityImage()

        // set image
        val communityImgView: ImageView = findViewById<ImageView>(R.id.community_icon)
        if (communityImg != null) {
            try {
                val decodedImage = Base64.decode(communityImg, Base64.DEFAULT)
                val imageBitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                communityImgView.setImageBitmap(imageBitmap)
            } catch (e: IllegalArgumentException) { // decoding fails
                communityImgView.setImageDrawable(ContextCompat.getDrawable(communityImgView.context, R.drawable.communities_icon))
            }
        }
        else {
            communityImgView.setImageDrawable(ContextCompat.getDrawable(communityImgView.context, R.drawable.communities_icon))
        }
    }

    /**
     * Determines the community image based on the current user and group users.
     *
     * If the community is a chat of 2 people, get the image based on the other user.
     *
     * @param currentUser The current user.
     * @return The URL or identifier for the community image, or null if not applicable.
     */
    private fun getCommunityImage() : String? {
        val groupSize = group.users.count()
        if (groupSize < 2)
            return null
        if (groupSize != 2)
            return group.picture

        // retreive image of one of the users
        if (groupUsers.isNullOrEmpty())
            return null
        if (groupUsers!![0].id == currentUser.id)
            return groupUsers!![1].picture
        return groupUsers!![0].picture
    }

    /**
     * Opens the GroupMembersActivity to display members of the current group.
     *
     * @param view The view that triggered this method.
     */
    fun viewMembers(view: View) {
        val intent = Intent(this, GroupMembersActivity::class.java)
        intent.putExtra("userObject", currentUser)
        intent.putExtra("groupObject", group)
        startActivity(intent)
    }

    /**
     * Opens the GroupSettingsActivity if current user is a group admin to edit the community's info.
     *
     * @param view The view that triggered this method.
     */
    fun openSettings(view: View) {
        if (!group.admins.contains(currentUser!!.userName)) {
            Toast.makeText(this@CommunityActivity, "Not allowed - only admins can edit groups.", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, GroupSettingsActivity::class.java)
        intent.putExtra("userObject", currentUser)
        intent.putExtra("groupObject", group)
        startActivity(intent)
    }

    /**
     * Refreshes the community from the server after an edit.
     */
    private fun refreshGroupFromServer() {
        serverAPIManager.getGroupByID(group.id!!, object : Callback<Group> {
            override fun onResponse(call: Call<Group>, response: Response<Group>) {
                if (response.isSuccessful) {
                    group = response.body()!!
                    manageDisplayOnRefresh()
                } else {
                    Toast.makeText(this@CommunityActivity, "Couldn't refresh group.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Group>, t: Throwable) {
                Toast.makeText(this@CommunityActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Displays the community when refreshed after an edit.
     */
    private fun manageDisplayOnRefresh() {
        // add underline to community name
        val communityNameView: TextView = findViewById(R.id.community_name)
        val name = SpannableString(group.name)
        //name.setSpan(UnderlineSpan(), 10, 19, 0)
        communityNameView.text = getCommunityName()

        // get detailed users info from the server (needed for images)
        serverAPIManager.getUsersFromList(group.users, object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    groupUsers = response.body()
                    displayGroupImage()
                }
                displayGroupImage()
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@CommunityActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupSignalRConnection() {
        var hubUrl = preferencesHelper.getServerUrl() + "/groupshub"

        // Create a connection to the SignalR hub
        hubConnection = HubConnectionBuilder.create(hubUrl).build()


        // Listen for updates to the group object from the server
        hubConnection.on("ReceiveGroupUpdate", { messageData: String,idData: String ->
            runOnUiThread {
                // Handle the group data update (e.g., update the UI)
                updateGroupData(messageData,idData)
            }
        }, String::class.java, String::class.java)

        hubConnection.start()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateGroupData(messageData: String,idData:String) {
        Log.d("SignalR", "Received group update: $messageData $idData")

        // edge case in which the new message is irrelevant
        if (idData != group.id){
            return
        }
        // Example: Deserialize the group data and update the UI
        val newMessage = Gson().fromJson(messageData, Message::class.java)
        addMessage(newMessage)
        chatScrollView.post {
            chatScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the SignalR connection when the activity is destroyed
        hubConnection.stop()
    }
    /**
     * Displays a single message
     *
     * @param message a single message*
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun addMessage(message: Message) {
        val inflater = LayoutInflater.from(this)
        val messageView = inflater.inflate(R.layout.chat_message_item, chatContainer, false)
        val senderTextView: TextView = messageView.findViewById(R.id.messageSender)
        val messageTextView: TextView = messageView.findViewById(R.id.messageText)

        senderTextView.text = "${message.sender} - ${message.timestamp_string}"
        messageTextView.text = message.data

        // Check if the message is from the current user
        if (message.sender == currentUser.userName) {
            // Right-align and set background color to green
            val layoutParams = messageView.layoutParams as LinearLayout.LayoutParams
            layoutParams.gravity = Gravity.END
            messageView.layoutParams = layoutParams

            // Set background color to green
            messageTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.holo_green_dark))
        } else {
            // Left-align for other users
            val layoutParams = messageView.layoutParams as LinearLayout.LayoutParams
            layoutParams.gravity = Gravity.START
            messageView.layoutParams = layoutParams
        }

        chatContainer.addView(messageView)
        chatScrollView.post {
            chatScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }
    /**
     * Displays all messages
     *
     * @param messages A list of messages
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayMessages(messages: List<Message>) {
        for (message in messages) {
            addMessage(message)
        }
    }
}