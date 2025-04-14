package com.example.sentimentanalysis

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import com.example.sentimentanalysis.R
import android.widget.LinearLayout

class MainActivity : AppCompatActivity() {

    private lateinit var inputText: EditText
    private lateinit var btnAnalyze: Button
    private lateinit var resultLabel: TextView
    private lateinit var emojiLabel: TextView
    private lateinit var rootLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootLayout = findViewById(R.id.rootLayout)
        inputText = findViewById(R.id.etInput)
        btnAnalyze = findViewById(R.id.btnSubmit)
        resultLabel = findViewById(R.id.resultLabel)
        emojiLabel = findViewById(R.id.tvEmoji)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.2:5000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(SentimentApi::class.java)

        btnAnalyze.setOnClickListener {
            val text = inputText.text.toString()

            if (text.isBlank()) {
                Toast.makeText(this, "Vui lòng nhập văn bản", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Clear previous result
            resultLabel.text = ""
            emojiLabel.text = ""

            Toast.makeText(this, "Đang phân tích...", Toast.LENGTH_SHORT).show()

            api.analyzeSentiment(RequestBody(text)).enqueue(object : Callback<SentimentResponse> {
                override fun onResponse(
                    call: Call<SentimentResponse>,
                    response: Response<SentimentResponse>
                ) {
                    if (response.isSuccessful) {
                        val sentiment = response.body()
                        resultLabel.text = "Kết quả: ${sentiment?.label} (${sentiment?.confidence})"
                        updateUIBasedOnSentiment(sentiment?.label)
                    } else {
                        resultLabel.text = "Lỗi: Không thể phân tích"
                        emojiLabel.text = "🤔"
                        rootLayout.setBackgroundColor(Color.GRAY)
                    }
                }

                override fun onFailure(call: Call<SentimentResponse>, t: Throwable) {
                    resultLabel.text = "Lỗi kết nối: ${t.message}"
                    emojiLabel.text = "😞"
                    rootLayout.setBackgroundColor(Color.GRAY)
                }
            })
        }
    }

    private fun updateUIBasedOnSentiment(label: String?) {
        when (label) {
            "positive" -> {
                emojiLabel.text = "😊"
                rootLayout.setBackgroundColor(Color.parseColor("#00C853"))
            }
            "neutral" -> {
                emojiLabel.text = "😐"
                rootLayout.setBackgroundColor(Color.parseColor("#808080"))
            }
            "negative" -> {
                emojiLabel.text = "😞"
                rootLayout.setBackgroundColor(Color.parseColor("#8B0000"))
            }
            else -> {
                emojiLabel.text = "🤔"
                rootLayout.setBackgroundColor(Color.GRAY)
            }
        }
    }
}
