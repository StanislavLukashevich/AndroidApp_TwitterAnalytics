package gwu.softwaredesign.twitteranalytics

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*

class MainActivity : AppCompatActivity() {

    private lateinit var usernameSearch : EditText
    private lateinit var analyzeButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences : SharedPreferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

        usernameSearch = findViewById(R.id.usernameSearch)
        analyzeButton = findViewById(R.id.buttonAnalyze)
        analyzeButton.isEnabled = false

        val textWatcher : TextWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val inputtedSearch : String = usernameSearch.text.toString()
                val enableButton = inputtedSearch.isNotEmpty()
                analyzeButton.isEnabled = enableButton
            }
        }
        usernameSearch.addTextChangedListener(textWatcher)

        val spinner: Spinner = findViewById<Spinner>(R.id.spinnerNumberTweets)
        val numberTweetsOptions = resources.getStringArray(R.array.number_tweets_array)
        val adapterArray = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item , numberTweetsOptions)
        spinner.adapter = adapterArray
        var currentSelectedNumberTweets: Int = 30 //I hope this works... (also general is default)
        println("after declaration: NUM OF TWEETS IS NOW $currentSelectedNumberTweets")

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                currentSelectedNumberTweets = parent.getItemAtPosition(position).toString().toInt()
                println("onItemSelected: NUM OF TWEETS IS NOW $currentSelectedNumberTweets")

            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        spinner.isActivated =  true


        currentSelectedNumberTweets.toInt()

        analyzeButton.setOnClickListener{ /*view ->*/
            Log.d("MainActivity", "analyzeButton called")
            sharedPreferences
                .edit()
                .putString("SAVED_SEARCH", usernameSearch.text.toString())
                .apply()
            val intentAnalyze = Intent(this, AnalysisActivity::class.java)
            var sentUsername = usernameSearch.text.toString()
            println( "sentUsername is $sentUsername")
            println("MainActivity ################ Sent user is: $sentUsername")
            println("MainActivity ################ Sent number of tweets is: $currentSelectedNumberTweets")
            intentAnalyze.putExtra("SEARCH_USERNAME", sentUsername)
            intentAnalyze.putExtra("NUMBER_OF_TWEETS", currentSelectedNumberTweets)
            startActivity(intentAnalyze)
        }
        val savedSearch = sharedPreferences.getString("SAVED_SEARCH", "")
        usernameSearch.setText(savedSearch)

    }

}