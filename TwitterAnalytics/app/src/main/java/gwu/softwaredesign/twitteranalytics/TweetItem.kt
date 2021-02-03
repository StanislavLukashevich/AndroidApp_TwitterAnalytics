package gwu.softwaredesign.twitteranalytics

import java.io.Serializable

data class TweetItem(
    val like_count:Int,
    val retweet_count:Int,
    val reply_count:Int,
    val quote_count:Int
) : Serializable