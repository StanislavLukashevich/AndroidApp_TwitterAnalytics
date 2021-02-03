package gwu.softwaredesign.twitteranalytics

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils.indexOf
import android.util.Log
import android.widget.Toast
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import org.jetbrains.anko.doAsync

class AnalysisActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        var user : String? = "realDonaldTrump"    //default
        var numberOfTweets : Int? = 15            //default

        user = intent.getStringExtra("SEARCH_USERNAME")
        numberOfTweets = intent.getIntExtra("NUMBER_OF_TWEETS", 0)

        println("AnalysisActivity ################ Received user is: $user")
        println("AnalysisActivity ################ Received number of tweets is: $numberOfTweets")

        doAsync{
            val twitterManager = TwitterAPIManager()
            try{
                val apiKey = getString(R.string.twitter_api_key)
                val apiSecret = getString(R.string.twitter_api_secret)

                val oAuthToken = twitterManager.retrieveOAuthToken(
                    apiKey = apiKey,
                    apiSecret = apiSecret
                )
                //val oAuthToken = "AAAAAAAAAAAAAAAAAAAAAHNbJwEAAAAAL68fSqejEAojI4xqYtavX01N7VY%3D5HfjiVbvBEUrJL30qPP7bWpuaGvLjw8J1qd7PxyBc1QIg1K1ch"

                val tweets:MutableList<TweetItem> = twitterManager.retrieveMetricByUser(
                    oAuthToken = oAuthToken,
                    user = user,
                    numberOfTweets = numberOfTweets
                    )


                //TODO: Graph Functionality Here...?

                runOnUiThread {

                    val likesArray : Array<Any> = Array<Any>(numberOfTweets) {Any()}

                    for(tweet in tweets){
                        likesArray[tweets.indexOf(tweet)] = tweet.like_count
                    }

                    // Implemented this library for data visualization:
                    //    https://github.com/AAChartModel/AAChartCore-Kotlin

                    val aaChartView = findViewById<AAChartView>(R.id.aa_chart_view)
                    val aaChartModel : AAChartModel = AAChartModel()
                        .chartType(AAChartType.Line)
                        .title("Twitter Likes for $user")
                        .subtitle("Last $numberOfTweets Tweets")
                        .xAxisLabelsEnabled(true)
                        .axesTextColor("#FFFFFF")
                        .backgroundColor("#9B80FF")
                        .dataLabelsEnabled(true)
                        .series(arrayOf(
                            AASeriesElement()
                                .name("Likes")
                                .data(likesArray)
                        )
                        )
                    //The chart view object calls the instance object of AAChartModel and draws the final graphic
                    aaChartView.aa_drawChartWithChartModel(aaChartModel)
                }

            } catch (exception: Exception){
                Log.e("AnalysisActivity", "Retrieving Tweets failed", exception)
                runOnUiThread {
                    Toast.makeText(this@AnalysisActivity, "Failed to retrieve Tweets!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}