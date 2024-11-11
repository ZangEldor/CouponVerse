package com.example.couponverse

import com.example.couponverse.data.Coupon
import com.example.couponverse.data.CouponAI
import com.example.couponverse.data.CouponAPI
import com.example.couponverse.data.User
import com.example.couponverse.data.Group
import com.example.couponverse.data.Message
import com.example.couponverse.data.Product
import com.example.couponverse.data.UserUpdateData
import com.example.couponverse.data.UsernameUpdateData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Query

/**
 * Interface for defining RESTful API endpoints.
 *
 * This interface outlines all the endpoints available in the RESTful API. Each method corresponds to a specific
 * API call, using Retrofit annotations to define HTTP methods and endpoint paths. The methods include user management,
 * coupon operations, group management, machine learning model interactions, and more.
 *
 * @see <a href="https://www.codeproject.com/Articles/5308542/A-Complete-Tutorial-to-Connect-Android-with-ASP-NE">CodeProject Tutorial</a>
 */
interface RESTfulAPI {

    // USERS
    /**
     * Logs in a user with the provided username and password.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return A [Call] that returns a [User] object.
     */
    @GET("api/Users/login")
    fun loginUser(@Query("username") username: String, @Query("password") password: String) : Call<User>

    /**
     * Checks if a user exists by their username.
     *
     * @param username The username to check.
     * @return A [Call] that returns Void.
     */
    @GET("api/Users/checkUserExists")
    fun checkUserExists(@Query("username") username: String) : Call<Void>

    /**
     * Updates the average embedding for a user based on a new coupon.
     *
     * @param username The username of the user.
     * @param coupon The coupon description.
     * @return A [Call] that returns Void.
     */
    @POST("api/Users/updateAverageEmbedding")
    fun updateUserAverageEmbedding(@Query("userName") username: String, @Query("coupon") coupon: String) : Call<Void>

    /**
     * Updates the average embedding for a user based on an edited coupon.
     *
     * @param userName The username of the user.
     * @param couponDesc The new coupon description.
     * @param oldCouponDesc The old coupon description.
     * @return A [Call] that returns Void.
     */
    @POST("api/Users/updateAverageEmbeddingOnEdit")
    fun updateUserAverageEmbeddingOnEdit(@Query("userName") userName: String,
                                     @Query("coupon") couponDesc: String,
                                     @Query("oldCoupon") oldCouponDesc: String) : Call<Void>

    /**
     * Retrieves user recommendations based on a query.
     *
     * @param query The search query for recommendations.
     * @return A [Call] that returns a list of [User] objects.
     */
    @GET("api/Users/userRecs")
    fun getUserRecsName(@Query("query") query: String): Call<List<User>>

    /**
     * Retrieves a list of users from a list of usernames.
     *
     * @param usernames The list of usernames.
     * @return A [Call] that returns a list of [User] objects.
     */
    @POST("api/Users/UsersFromList")
    fun getUsersList(@Body usernames: List<String>) : Call<List<User>>

    /**
     * Registers a new user with the provided user data.
     *
     * @param user The [User] object containing user details.
     * @return A [Call] that returns Void.
     */
    @POST("api/Users/register")
    fun registerUser(@Body user: User) : Call<Void>

    /**
     * Updates user information based on the username.
     *
     * @param username The username of the user to update.
     * @param userData The new user data.
     * @return A [Call] that returns Void.
     */
    @PUT("api/Users/{userName}")
    fun updateUserByName(@Path("userName") username: String, @Body userData: UserUpdateData) : Call<Void>

    /**
     * Deletes a user by their username.
     *
     * @param username The username of the user to delete.
     * @return A [Call] that returns Void.
     */
    @DELETE("api/Users/{userName}")
    fun deleteUserByUsername(@Path("userName") username: String) : Call<Void>

    /**
     * Updates a user's password.
     *
     * @param username The username of the user.
     * @param password The new password.
     * @return A [Call] that returns Void.
     */
    @PUT("api/Users/password")
    fun updateUserPassword(@Query("userName") username: String, @Query("password") password: String) : Call<Void>

    // COUPONS
    /**
     * Retrieves a list of coupons for a user.
     *
     * @param username The username of the user.
     * @return A [Call] that returns a list of [CouponAPI] objects.
     */
    @GET("api/Coupon/{userName}")
    fun getUserCoupons(@Path("userName") username: String) : Call<List<CouponAPI>>

    /**
     * Updates a coupon for a user by its index.
     *
     * @param username The username of the user.
     * @param index The index of the coupon to update.
     * @param coupon The updated coupon data.
     * @return A [Call] that returns Void.
     */
    @PUT("api/Coupon/{userName}/{index}")
    fun updateCouponByUserIndex(@Path("userName") username: String,
                                @Path("index") index: Int,
                                @Body coupon: CouponAPI) : Call<Void>

    /**
     * Deletes a coupon by its index for a user.
     *
     * @param username The username of the user.
     * @param index The index of the coupon to delete.
     * @return A [Call] that returns Void.
     */
    @DELETE("api/Coupon/{userName}/{index}")
    fun deleteCouponByUserIndex(@Path("userName") username: String,
                                @Path("index") index: Int) : Call<Void>

    /**
     * Adds a new coupon for a user.
     *
     * @param username The username of the user.
     * @param coupon The coupon data to add.
     * @return A [Call] that returns Void.
     */
    @POST("api/Coupon/addNewCoupon")
    fun postNewCouponUser(@Query("username") username: String, @Body coupon: CouponAPI) : Call<Void>

    // GROUPS
    /**
     * Retrieves a list of all groups.
     *
     * @return A [Call] that returns a list of [Group] objects.
     */
    @GET("api/Groups")
    fun getGroups() : Call<List<Group>>

    /**
     * Creates a new group.
     *
     * @param group The [Group] object containing group details.
     * @return A [Call] that returns the created [Group] object.
     */
    @POST("api/Groups")
    fun postGroup(@Body group: Group) : Call<Group>

    /**
     * Retrieves a group by its ID.
     *
     * @param id The ID of the group.
     * @return A [Call] that returns the [Group] object.
     */
    @GET("api/Groups/id/{id}")
    fun getGroupByID(@Path("id") id: String) : Call<Group>

    /**
     * Retrieves a list of all groups the user is a part of.
     *
     * @param username
     * @return A [Call] that returns a list of [Group] objects.
     */
    @GET("api/Groups/{username}/getGroups")
    fun getUserGroups(@Path("username") username: String) : Call<List<Group>>

    /**
     * Updates a group by its ID.
     *
     * @param id The ID of the group to update.
     * @param group The new group data.
     * @return A [Call] that returns Void.
     */
    @PUT("api/Groups/{id}")
    fun putGroupByID(@Path("id") id: String, @Body group: Group) : Call<Void>

    /**
     * Deletes a group by its ID.
     *
     * @param id The ID of the group to delete.
     * @return A [Call] that returns Void.
     */
    @DELETE("api/Groups/{id}")
    fun deleteGroup(@Path("id") id: String) : Call<Void>

    /**
     * Retrieves a list of groups by a name substring.
     *
     * @param name The name of the group.
     * @return A [Call] that returns a list of [Group] object.
     */
    @GET("api/Groups/name/{groupName}")
    fun getGroupsByName(@Path("groupName") substr: String) : Call<List<Group>>

    /**
     * Adds an admin to a group.
     *
     * @param id The ID of the group.
     * @param admin The username of the admin to add.
     * @return A [Call] that returns Void.
     */
    @POST("api/Groups/{id}/addAdmin")
    fun addAdminToGroup(@Path("id") id: String, @Body admin: String) : Call<Void>

    /**
     * Removes an admin from a group.
     *
     * @param id The ID of the group.
     * @param admin The username of the admin to remove.
     * @return A [Call] that returns Void.
     */
    @POST("api/Groups/{id}/removeAdmin")
    fun removeAdminFromGroup(@Path("id") id: String, @Body admin: String) : Call<Void>

    /**
     * Adds a user to a group.
     *
     * @param id The ID of the group.
     * @param user The username of the user to add.
     * @return A [Call] that returns Void.
     */
    @POST("api/Groups/{id}/addUser")
    fun addUserToGroup(@Path("id") id: String, @Body user: String) : Call<Void>

    /**
     * Removes a user from a group.
     *
     * @param id The ID of the group.
     * @param user The username of the user to remove.
     * @return A [Call] that returns Void.
     */
    @POST("api/Groups/{id}/removeUser")
    fun removeUserFromGroup(@Path("id") id: String, @Body user: String) : Call<Void>

    /**
     * Updates a group's picture.
     *
     * @param id The ID of the group.
     * @param picture The base64-encoded picture string.
     * @return A [Call] that returns Void.
     */
    @POST("api/Groups/{id}/updatePicture")
    fun updateGroupPicture(@Path("id") id: String, @Query("picture") picture: String) : Call<Void>

    /**
     * Adds a message to a group.
     *
     * @param id The ID of the group.
     * @param message The [Message] object containing the message details.
     * @return A [Call] that returns Void.
     */
    @POST("api/Groups/{id}/addMessage")
    fun addMessageToGroup(@Path("id") id: String, @Body message: Message) : Call<Void>

    /**
     * Updates a user's information in a group.
     *
     * @param usernameData The [UsernameUpdateData] object containing the username and new data.
     * @return A [Call] that returns Void.
     */
    @POST("api/Groups/updateUsername")
    fun updateUserInGroup(@Body usernameData: UsernameUpdateData) : Call<Void>

    //MLModels
    /**
     * Runs NER (Named Entity Recognition) on the provided input for extracting coupon details.
     *
     * @param input The input text for NER.
     * @return A [Call] that returns a [CouponAI] object with the NER results.
     */
    @GET("api/MLModels/NER")
    fun mlNER(@Query("input") input: String) : Call<CouponAI>

    /**
     * Updates the embedding model with the provided input.
     *
     * @param input The input data for embedding.
     * @return A [Call] that returns Void.
     */
    @GET("api/MLModels/embedding")
    fun mlEmbedding(@Query("input") input: String) : Call<Void>

    /**
     * Retrieves recommendations from the ML model based on the provided input.
     *
     * @param input The input data for the recommendation model.
     * @return A [Call] that returns a list of [Product] objects.
     */
    @POST("api/MLModels/MLRecommendationsModel")
    fun mlRecModel(@Body input: List<Double>) : Call<List<Product>>

    // Python
    /**
     * Executes a Python script specified by the script name.
     *
     * @param scriptName The name of the Python script to execute.
     * @return A [Call] that returns Void.
     */
    @GET("api/run-script/{scriptName}")
    fun runPyScript(@Path("scriptName") scriptName: String) : Call<Void>

}