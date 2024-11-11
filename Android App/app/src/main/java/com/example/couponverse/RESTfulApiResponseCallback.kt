package com.example.couponverse

/**
 * A callback interface for handling responses from RESTful API calls.
 *
 * This interface is designed to be used with asynchronous API calls, providing methods
 * to handle both successful responses and failures. Implementations of this interface
 * should define how to process the result of the API call or handle any errors that occur.
 *
 * @param T The type of the result expected from the API call.
 */
interface RESTfulApiResponseCallback<T> {
    fun onSuccess(result: T?)
    fun onFailure(error: String)
}