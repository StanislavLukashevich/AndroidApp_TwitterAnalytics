package gwu.softwaredesign.twitteranalytics

import android.util.Base64
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.net.URLEncoder

class TwitterAPIManager {

    val okHttpClient: OkHttpClient

    // An init block allows us to perform extra logic during instance creation (similar to having
    // extra logic in a constructor)
    init {
        val builder = OkHttpClient.Builder()
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(loggingInterceptor)

        okHttpClient = builder.build()
    }

    /**
     * Twitter requires us to encodedour API Key and API Secret in a special way for the request.
     *
     * Step 1 for application-only OAuth from:
     * https://developer.twitter.com/en/docs/basics/authentication/oauth-2-0/application-only
     */
    private fun encodeSecrets(
        apiKey: String,
        apiSecret: String
    ) : String {
        // Encoding for a URL -- converts things like spaces into %20
        val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
        val encodedSecret = URLEncoder.encode(apiSecret, "UTF-8")

        // Concatenate the two together, with a colon in-between
        val combinedEncoded = "$encodedKey:$encodedSecret"

        // Base-64 encode the combined string - server expects to have the credentials
        // in the agreed-upon format (generally used for transmitting binary data)
        // https://en.wikipedia.org/wiki/Base64
        return Base64.encodeToString(combinedEncoded.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * All of Twitter's APIs are also protected by OAuth.
     */
    fun retrieveOAuthToken(
        apiKey: String,
        apiSecret: String
    ): String {
        // Twitter requires us to encode our API Key and API Secret in a special way for the request.
        val base64Combined = encodeSecrets(apiKey, apiSecret)

        // Step 2 for application-only OAuth from:
        // https://developer.twitter.com/en/docs/authentication/oauth-2-0/application-only
        //
        // OAuth is defined to be a POST call, which has a specific body / payload to let the server
        // know we are doing "application-only" OAuth (e.g. we will only access public information)
        val requestBody = "grant_type=client_credentials".toRequestBody(
            contentType = "application/x-www-form-urlencoded".toMediaType()
        )

        // The encoded secrets become a header on the request
        val request = Request.Builder()
            .url("https://api.twitter.com/oauth2/token")
            .header("Authorization", "Basic $base64Combined")
            .post(requestBody)
            .build()

        // "Execute" the request (.execute will block the current thread until the server replies with a response)
        val response = okHttpClient.newCall(request).execute()

        // Get the JSON body from the response (if it exists)
        val responseString = response.body?.string()

        // If the response was successful (e.g. status code was a 200) AND the server sent us back
        // some JSON (which will contain the OAuth token), then we can go ahead and parse the JSON body.
        return if (response.isSuccessful && !responseString.isNullOrBlank()) {
            // Set up for parsing the JSON response from the root element
            val json = JSONObject(responseString)

            // Pull out the OAuth token
            json.getString("access_token")
        } else {
            ""
        }
    }






    fun retrieveMetricByUser(oAuthToken: String, user: String?, numberOfTweets: Int) : MutableList<TweetItem> {

        val tweets: MutableList<TweetItem> = mutableListOf<TweetItem>()
        //val oAuthToken2 = "AAAAAAAAAAAAAAAAAAAAAHNbJwEAAAAAL68fSqejEAojI4xqYtavX01N7VY%3D5HfjiVbvBEUrJL30qPP7bWpuaGvLjw8J1qd7PxyBc1QIg1K1ch"

        val request: Request = Request.Builder()
            .url("https://api.twitter.com/2/tweets/search/recent?query=from:$user&tweet.fields=public_metrics&max_results=$numberOfTweets")
            .header("Authorization", "Bearer $oAuthToken")
            .get()
            .build()

        // "Execute" the request (.execute will block the current thread until the server replies with a response)
        val response: Response = okHttpClient.newCall(request).execute()

        // Get the JSON body from the response (if it exists)
        val responseString = response.body?.string()

        // If the response was successful (e.g. status code was a 200) AND the server sent us back
        // some JSON (which will contain the Tweets), then we can go ahead and parse the JSON body.
        if (response.isSuccessful && !responseString.isNullOrBlank()) {
            // Set up for parsing the JSON response from the root element
            val json = JSONObject(responseString)

            // The list of Tweets will be within the statuses array
            val data = json.getJSONArray("data")

            // Loop thru the statuses array and parse each individual list, adding it to our `tweets`
            // list which we will return at the end.
            for (i in 0 until data.length()) {
                val objectNoName = data.getJSONObject(i)
                val public_metrics = objectNoName.getJSONObject("public_metrics")
                val like_count = public_metrics.getString("like_count")
                val retweet_count = public_metrics.getString("retweet_count")
                val reply_count = public_metrics.getString("reply_count")
                val quote_count = public_metrics.getString("quote_count")

                val tweet = TweetItem(
                    like_count = like_count.toInt(),
                    retweet_count = retweet_count.toInt(),
                    reply_count = reply_count.toInt(),
                    quote_count = quote_count.toInt()
                )
                println("###### like_count: $like_count, retweet_count: $retweet_count, reply_count: $reply_count, quote_count: $quote_count #####")
                tweets.add(tweet)
            }
        } else {
            // Response failed (maybe the server is down)
            // We could throw an Exception here for the Activity, or update the function to return an error-type
            println("TwitterAPIManager: Response Failed")
        }

        return tweets
    }

}