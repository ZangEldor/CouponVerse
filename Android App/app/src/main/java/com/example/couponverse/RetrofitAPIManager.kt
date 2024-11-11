package com.example.couponverse

import android.content.Context
import com.example.couponverse.data.CouponAI
import com.example.couponverse.data.User
import com.example.couponverse.data.CouponAPI
import com.example.couponverse.data.Group
import com.example.couponverse.data.Message
import com.example.couponverse.data.Product
import com.example.couponverse.data.UserUpdateData
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback

/**
 * Singleton class for managing Retrofit API calls.
 *
 * This class provides methods for making network requests to the server using Retrofit.
 * It handles all API interactions, including user management, coupon operations, group management, and more.
 * Each method corresponds to a specific API endpoint and uses Retrofit's `Call` and `Callback` mechanisms.
 *
 * @property context The application context used to initialize Retrofit and manage preferences.
 * @property service The Retrofit service interface used for API calls.
 */
class RetrofitAPIManager private constructor(context: Context) {

    private val preferencesHelper : PreferencesManager =
        PreferencesManager(context)
    private val baseUrlAPI : String = preferencesHelper.getServerUrl()
    private val service : RESTfulAPI = Retrofit.Builder()
        .baseUrl(baseUrlAPI)
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(RESTfulAPI::class.java)

    companion object {
        @Volatile
        private var APIManager : RetrofitAPIManager? = null

        /**
         * Returns the singleton instance of `RetrofitAPIManager`.
         *
         * @param context The application context.
         * @return The singleton instance of `RetrofitAPIManager`.
         */
        fun getService(context: Context) : RetrofitAPIManager {
            return APIManager ?: synchronized(this) {
                APIManager ?: RetrofitAPIManager(context).also { APIManager = it }
            }
        }
    }

    /**
     * Checks if a user exists by their username.
     *
     * @param username The username to check.
     * @param callback The callback to handle the response or error.
     */
    fun checkUserExists(username: String, callback: Callback<Void>) {
        val checkCall: Call<Void> = service.checkUserExists(username)
        checkCall.enqueue(callback)
    }

    /**
     * Registers a new user (without an ID).
     *
     * @param userInfo The user information to register.
     * @param callback The callback to handle the response or error.
     */
    fun postUserNoId(userInfo: User, callback: Callback<Void>) {
        val postCall: Call<Void> = service.registerUser(userInfo)
        postCall.enqueue(callback)
    }

    /**
     * Logs in a user with their username and password.
     *
     * @param user The user with username and password.
     * @param callback The callback to handle the response or error.
     */
    fun loginUser(user: User, callback: Callback<User>) {
        val userCall: Call<User> = service.loginUser(user.userName!!, user.password)
        userCall.enqueue(callback)
    }

    /**
     * Deletes a user by their username.
     *
     * Currently not in use.
     *
     * @param name The username of the user to delete.
     * @param callback The callback to handle the response or error.
     */
    fun deleteUserByName(name: String, callback: Callback<Void>) {
        val deleteUserCall: Call<Void> = service.deleteUserByUsername(name)
        deleteUserCall.enqueue(callback)
    }

    /**
     * Updates user information by their username.
     *
     * @param name The username of the user to update.
     * @param userInfo The new user information.
     * @param callback The callback to handle the response or error.
     */
    fun updateUserByName(name: String, userInfo: UserUpdateData, callback: Callback<Void>) {
        val updateUserCall: Call<Void> = service.updateUserByName(name, userInfo)
        updateUserCall.enqueue(callback)
    }


    /**
     * Updates a user's password.
     *
     * Currently not in use.
     *
     * @param name The username of the user.
     * @param password The new password.
     * @param callback The callback to handle the response or error.
     */
    fun updateUserPassword(name: String, password: String, callback: Callback<Void>) {
        val updateUserCall: Call<Void> = service.updateUserPassword(name, password)
        updateUserCall.enqueue(callback)
    }

    /**
     * Adds a new coupon for a user.
     *
     * @param user The user to add the coupon for.
     * @param coupon The coupon data to be sent to the server API.
     * @param callback The callback to handle the response or error.
     */
    fun addNewCouponUser(user: User, coupon: CouponAPI, callback: Callback<Void>) {
        val addNewCouponCall: Call<Void> = service.postNewCouponUser(user.userName!!, coupon)
        addNewCouponCall.enqueue(callback)
    }

    /**
     * Updates a coupon for a user by its index.
     *
     * @param username The username of the user.
     * @param index The index of the coupon.
     * @param coupon The updated coupon data to be sent to the server API.
     * @param callback The callback to handle the response or error.
     */
    fun updateCouponUserIndex(username: String, index: Int, coupon: CouponAPI, callback: Callback<Void>) {
        val updateCouponCall: Call<Void> = service.updateCouponByUserIndex(username, index, coupon)
        updateCouponCall.enqueue(callback)
    }

    /**
     * Updates a user's average embedding based on a new coupon.
     *
     * @param username The username of the user.
     * @param couponDesc The description of the coupon.
     * @param callback The callback to handle the response or error.
     */
    fun updateUserAverageEmbedding(username: String, couponDesc: String, callback: Callback<Void>) {
        val updateUserAddAvgEmbeddingCall: Call<Void> =
            service.updateUserAverageEmbedding(username, couponDesc)
        updateUserAddAvgEmbeddingCall.enqueue(callback)
    }

    /**
     * Updates a user's average embedding based on an edited coupon.
     *
     * @param username The username of the user.
     * @param couponDesc The new coupon description.
     * @param oldCouponDesc The old coupon description.
     * @param callback The callback to handle the response or error.
     */
    fun updateUserAverageEmbeddingOnEdit(username: String, couponDesc: String,
                                         oldCouponDesc: String, callback: Callback<Void>) {
        val updateUserEditAvgEmbeddingCall: Call<Void> =
            service.updateUserAverageEmbeddingOnEdit(username, couponDesc, oldCouponDesc)
        updateUserEditAvgEmbeddingCall.enqueue(callback)
    }

    /**
     * Deletes a coupon by its index for a user.
     *
     * @param username The username of the user.
     * @param index The index of the coupon.
     * @param callback The callback to handle the response or error.
     */
    fun deleteCoupon(username: String, index: Int, callback: Callback<Void>) {
        val deleteCouponCall: Call<Void> = service.deleteCouponByUserIndex(username, index)
        deleteCouponCall.enqueue(callback)
    }

    /**
     * Extracts coupon information from text using an AI model.
     *
     * @param desc The text description of the coupon.
     * @param callback The callback to handle the AI analysis result or error.
     */
    fun extractNewCoupon(desc: String, callback: Callback<CouponAI>) {
        val extractNewCouponCall: Call<CouponAI> = service.mlNER(desc)
        extractNewCouponCall.enqueue(callback)
    }

    /**
     * Retrieves a user's list of coupons.
     *
     * @param username The username of the user.
     * @param callback The callback to handle the response or error.
     */
    fun getUserCouponsList(username: String, callback: Callback<List<CouponAPI>>) {
        val getUserCouponsListCall: Call<List<CouponAPI>> = service.getUserCoupons(username)
        getUserCouponsListCall.enqueue(callback)
    }

    /**
     * Creates a new group.
     *
     * @param group The group data.
     * @param callback The callback to handle the response or error.
     */
    fun postNewGroup(group: Group, callback: Callback<Group>) {
        val postNewGroupCall: Call<Group> = service.postGroup(group)
        postNewGroupCall.enqueue(callback)
    }

    /**
     * Gets a list from the server of all groups where the specified user is a member.
     *
     * @param username The username of the user.
     * @param user The user.
     * @param callback The callback to handle the response or error.
     */
    fun getUserGroups(username: String, callback: Callback<List<Group>>) {
        val getUserGroupsCall: Call<List<Group>> = service.getUserGroups(username)
        getUserGroupsCall.enqueue(callback)
    }

    /**
     * Removes a user from an existing group.
     *
     * @param username The username of the user.
     * @param group The group to remove the user from.
     * @param callback The callback to handle the response or error.
     */
    fun removeUserFromGroup(username: String, group: String, callback: Callback<Void>) {
        val removeUserGroupCall: Call<Void> = service.removeUserFromGroup(username, group)
        removeUserGroupCall.enqueue(callback)
    }

    /**
     * Gets a group (community) from the server based on community's ID.
     *
     * @param id The ID of the group.
     * @param callback The callback to handle the response or error.
     */
    fun getGroupByID(id: String, callback: Callback<Group>) {
        val getGroupIDCall: Call<Group> = service.getGroupByID(id)
        getGroupIDCall.enqueue(callback)
    }

    /**
     * Updates a group's information based on its id.
     *
     * @param id The id of the group.
     * @param groupData The group's updated data.
     * @param callback The callback to handle the response or error.
     */
    fun updateGroupByID(id: String, groupData: Group, callback: Callback<Void>) {
        val updateGroupCall: Call<Void> = service.putGroupByID(id, groupData)
        updateGroupCall.enqueue(callback)
    }

    /**
     * Retrieves a list of groups all with names having some substring.
     *
     * @param substr The substring to search by.
     * @param callback The callback to handle the response or error.
     */
    fun getGroupsBySubstring(substr: String, callback: Callback<List<Group>>) {
        val getGroupsCall: Call<List<Group>> = service.getGroupsByName(substr)
        getGroupsCall.enqueue(callback)
    }

    /**
     * Adds a user to a group by the group's ID and user's username.
     *
     * @param id The group's ID.
     * @param username The user's username.
     * @param callback The callback to handle the response or error.
     */
    fun addUserToGroup(id: String, username: String, callback: Callback<Void>) {
        val addUserToGroupCall: Call<Void> = service.addUserToGroup(id, username)
        addUserToGroupCall.enqueue(callback)
    }

    /**
     * Executes a Python script on the server.
     *
     * Currently not in use.
     *
     * @param script The Python script's name to execute.
     * @param callback The callback to handle the response or error.
     */
    fun executePythonScript(script: String, callback: Callback<Void>) {
        val executePythonScriptCall: Call<Void> = service.runPyScript(script)
        executePythonScriptCall.enqueue(callback)
    }

    /**
     * Retrieves user recommendations based on a query.
     *
     * @param query The search query for user recommendations.
     * @param callback The callback to handle the response or error.
     */
    fun getUsersRecommendations(query: String, callback: Callback<List<User>>) {
        val getUserQueryRecommendations: Call<List<User>> = service.getUserRecsName(query)
        getUserQueryRecommendations.enqueue(callback)
    }

    /**
     * Retrieves a list of users from a list of usernames.
     *
     * @param usernames The list of usernames.
     * @param callback The callback to handle the response or error.
     */
    fun getUsersFromList(usernames: List<String>, callback: Callback<List<User>>) {
        val getUserListCall: Call<List<User>> = service.getUsersList(usernames)
        getUserListCall.enqueue(callback)
    }

    /**
     * Retrieves ML-based product recommendations.
     *
     * @param embeddings The embeddings used for recommendations.
     * @param callback The callback to handle the response or error.
     */
    fun getMLProducts(embeddings: List<Double>, callback: Callback<List<Product>>) {
        val getUserQueryRecommendations: Call<List<Product>> = service.mlRecModel(embeddings)
        getUserQueryRecommendations.enqueue(callback)
    }

    /**
     * Sends a new message to the server
     *
     * @param id The group id
     * @param message The new message
     */
    fun sendmessage(id: String?, message: Message, callback: Callback<Void>) {
        val getUserQueryRecommendations: Call<Void> = service.addMessageToGroup(id!!,message)
        getUserQueryRecommendations.enqueue(callback)
    }
}