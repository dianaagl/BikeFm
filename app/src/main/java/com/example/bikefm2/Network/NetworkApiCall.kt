package com.example.bikefm2.Network

import AddFriendMutation
import LoginQuery
import RegistrationMutation
import SetLocationMutation
import UsersSearchQuery
import VerifyQuery
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.coroutines.toDeferred
import com.example.bikefm2.data.model.Friend
import com.example.bikefm2.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import com.example.bikefm2.data.Result
import com.example.bikefm2.data.Location

class NetworkApiCall @Inject constructor() {

    suspend fun loginUser(username: String, password: String): Result<User> = withContext(Dispatchers.IO){
        val client = NetworkService.getInstance().getApolloClient()
        val response = client.query(LoginQuery(username, password)).await()

        return@withContext if(response.data != null){
            val user = response.data?.login()?.user()
            val token = response.data?.login()?.token()
            val error = response.data?.login()?.error()

            if (error == null && user != null && token != null)
                Result.Success(
                    User(displayName = user.name(),
                        userId = user._id(),
                        token = token,
                        friends = user.friends()?.map { it ->
                            Friend(
                                userId = it._id(),
                                displayName = it.name(),
                                lastname = it.name(),
                                location = it.location()?.let {
                                    Location(it.latitude(), it.longitude())
                                },
                                type = "friend"
                            )
                        },
                        friendsRequests = user.friendRequests()?.map { it ->
                            Friend(
                                userId = it._id(),
                                displayName = it.name(),
                                lastname = it.name(),
                                location = null,
                                type = "friend_req"
                            )
                        },
                        sentFriendsRequests = user.sentFriendRequests()?.map { it ->
                            Friend(
                                userId = it._id(),
                                displayName = it.name(),
                                lastname = it.name(),
                                location = null,
                                type = "sent_friend_req"
                            )
                        }
                    ))
            else Result.Error(java.lang.Exception(error.toString()))

        } else Result.Error(java.lang.Exception(response.errors.toString()))
    }

    suspend fun registerUser(username: String, password: String): Result<User> = withContext(Dispatchers.IO){

        val client = NetworkService.getInstance().getApolloClient()
        val response = client.mutate(RegistrationMutation(username, password)).await()
        if(response.data != null){
            val user = response.data?.registration()?.user()
            val token = response.data?.registration()?.token()
            val error = response.data?.registration()?.error()

            if (error == null && user != null && token != null)
                Result.Success(data = User(
                    displayName = user.name(),
                    userId = user._id(),
                    token = token,
                    friends = null,
                    friendsRequests = null,
                    sentFriendsRequests = null
                ))

            else Result.Error(exception = Exception(response.data?.registration()?.error().toString()))
        }
        Result.Error(exception = java.lang.Exception(response.errors.toString()))
    }
    suspend fun setUserLocation(token: String, loc: Location): Boolean = withContext(Dispatchers.IO){
        val client = NetworkService.getInstance().getApolloClientWithTokenIntercetor(token)

        val response = client.mutate(SetLocationMutation(loc.latitude, loc.longitude)).await()
        if(response.errors != null){
            return@withContext false
        }
        return@withContext true
    }
    suspend fun verifyUser(token: String): Result<User> = withContext(Dispatchers.IO){
        val client = NetworkService.getInstance().getApolloClient()

        val response = client.query(VerifyQuery(token)).await()
        return@withContext response.data?.let {
            val user = response.data?.user()
            user?.let {
                Result.Success(data = User(
                    displayName = user.name(),
                    userId = user._id(),
                    token = token,
                    friends = user.friends()?.map { friend ->  Friend(
                        userId = friend._id(),
                        displayName = friend.name(),
                        lastname = friend.name(),
                        location = friend.location()?.let { loc ->
                            Location(loc.latitude(), loc.longitude())
                        },
                        type = "friend"
                    )},
                    friendsRequests = user.friendRequests()?.map { friend -> Friend(
                        userId = friend._id(),
                        displayName = friend.name(),
                        lastname = friend.name(),
                        location = null,
                        type = "friend_req"
                    )},
                    sentFriendsRequests = user.sentFriendRequests()?.map { friend -> Friend(
                        userId = friend._id(),
                        displayName = friend.name(),
                        lastname = friend.name(),
                        location = null,
                        type = "sent_friend_req"
                    )}

                ))
            } ?:  Result.Error(exception = java.lang.Exception(response.errors.toString()))
        } ?: Result.Error(exception = java.lang.Exception(response.errors.toString()))
    }

    suspend fun findUsers(query: String, token: String): List<Friend>? = withContext(Dispatchers.IO){
        val client = NetworkService.getInstance().getApolloClientWithTokenIntercetor(token)
        val response = client.query(UsersSearchQuery(query)).await()
        return@withContext response.data?.let {
            val users = response.data?.usersSearch()
            users?.let {
                it.map { user ->
                    Friend(
                        userId = user._id(),
                        displayName = user.name(),
                        lastname = user.name(),
                        location = null,
                        type = ""
                    )
                }
            }
        }
    }

    suspend fun addFriend(friendId: String, token: String): Result<Boolean> = withContext(Dispatchers.IO){
        val client = NetworkService.getInstance().getApolloClientWithTokenIntercetor(token)

        val response = client.mutate(AddFriendMutation(friendId)).await()
        response.data?.let { Result.Success(data = true) }
            ?: Result.Error(exception = java.lang.Exception(response.errors.toString()))
    }
}