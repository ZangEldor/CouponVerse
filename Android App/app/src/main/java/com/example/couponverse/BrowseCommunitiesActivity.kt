package com.example.couponverse

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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity for browsing the communities in the app.
 *
 * Provides functionality to:
 * - TODO(Gets recommendation from server based on user's embedding and chosen ML model)
 *
 * - Gets groups list by query.
 * - Display and scroll through communities.
 * - Handle user interactions with groups (on click tries to add self to group and open it).
 * - Sort the recommended groups by name.
 */
class BrowseCommunitiesActivity : AppCompatActivity() {

    private val serverAPIManager = RetrofitAPIManager.getService(this)
    private lateinit var searchGroupsField: EditText
    private lateinit var groupsContainer: LinearLayout
    private lateinit var userProfilePic: ImageView
    private lateinit var recType: TextView
    private var currentUser: User? = null
    private var recommendedGroups: List<Group>? = null
    private var sortedGroups: List<Group>? = null
    private val MAX_GROUPS_TO_DISPLAY = 20

    /**
     * Initializes the activity, setting up UI elements and loading the user's recommended groups.
     *
     * @param savedInstanceState The saved instance state for restoring the activity's state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_browse_communities)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentUser = intent.extras!!.getSerializable("userObject") as User
        searchGroupsField = findViewById<EditText>(R.id.searchCommunitiesField)
        groupsContainer = findViewById<LinearLayout>(R.id.groupsContainer)
        userProfilePic = findViewById<ImageView>(R.id.user_pfp)
        recType = findViewById<TextView>(R.id.recommendationTypeText)

        val safeCastUser: User = currentUser as User
        if (safeCastUser.picture != null) {
            try {
                val decodedImage = Base64.decode(safeCastUser.picture, Base64.DEFAULT)
                val imageBitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                userProfilePic.setImageBitmap(imageBitmap)
            } catch (e: IllegalArgumentException) { // decoding fails
                userProfilePic.setImageDrawable(ContextCompat.getDrawable(userProfilePic.context, R.drawable.user_icon))
            }
        }
        else {
            userProfilePic.setImageDrawable(ContextCompat.getDrawable(userProfilePic.context, R.drawable.user_icon))
        }

        searchGroupsField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()

                if (query.isNotEmpty())
                    lookupGroupsByName(query)
            }
        })
        
        // TODO - Add option to select recommendation type and get recommendations into scrollview
    }

    /**
     * Looks up groups by name through a server API call.
     *
     * @param query The search query for looking up the groups.
     */
    private fun lookupGroupsByName(query: String) {
        serverAPIManager.getGroupsBySubstring(query, object : Callback<List<Group>> {
            override fun onResponse(call: Call<List<Group>>, response: Response<List<Group>>) {
                if (response.isSuccessful) {
                    recommendedGroups = response.body()
                    manageGroupsDisplay()
                } else {
                    Toast.makeText(this@BrowseCommunitiesActivity, "Failed to load group recommendations.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Group>>, t: Throwable) {
                Toast.makeText(this@BrowseCommunitiesActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Manages the display of user recommendations by sorting and updating the UI.
     */
    private fun manageGroupsDisplay() {
        var safeCastList: List<Group> = listOf()
        if (!recommendedGroups.isNullOrEmpty()) {
            safeCastList = recommendedGroups as List<Group>
            sortedGroups = safeCastList.sortedBy { it.name.lowercase() }
        }
        displayGroupRecommendations()
    }

    /**
     * Displays the first 15 of the list of recommended groups in the UI.
     */
    private fun displayGroupRecommendations() {
        if (recommendedGroups.isNullOrEmpty())
            return
        groupsContainer.removeAllViews()
        val safeCastList: List<Group> = (sortedGroups as List<Group>).take(MAX_GROUPS_TO_DISPLAY)
        val inflater = LayoutInflater.from(this)
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
        val groupName = view.findViewById<TextView>(R.id.communityName).text.toString()
        val selectedGroup = recommendedGroups?.find { it.name == groupName }
        // user is already in the group, no need to attempt joining to group
        if (selectedGroup!!.users.contains(currentUser!!.userName)) {
            val intent = Intent(applicationContext, CommunityActivity::class.java)
            intent.putExtra("userObject", currentUser)
            intent.putExtra("groupObject", selectedGroup)
            startActivity(intent)
            return
        }
        // user is not in the group - first try to join and if successful then display group
        serverAPIManager.addUserToGroup(selectedGroup.id!!, currentUser!!.userName, object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    selectedGroup.users += currentUser!!.userName // add self to user's members list
                    val intent = Intent(applicationContext, CommunityActivity::class.java)
                    intent.putExtra("userObject", currentUser)
                    intent.putExtra("groupObject", selectedGroup)
                    startActivity(intent)
                } else {
                    val joinFailed = "Joining the group failed. Please try again."
                    Toast.makeText(this@BrowseCommunitiesActivity, joinFailed, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@BrowseCommunitiesActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }
}