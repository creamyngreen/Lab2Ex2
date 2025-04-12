package com.tuyetmai.maisentimentanalysis

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import com.tuyetmai.maisentimentanalysis.R
import android.graphics.Color
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

        // Chỉnh sửa baseUrl nếu cần
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.18:5000/") // Địa chỉ của Flask server
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(SentimentApi::class.java)

        btnAnalyze.setOnClickListener {
            val text = inputText.text.toString()
            if (text.isBlank()) {
                Toast.makeText(this, "Vui lòng nhập văn bản", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gửi dữ liệu dưới dạng JSON tới API
            api.analyzeSentiment(RequestBody(text)).enqueue(object : Callback<SentimentResponse> {
                override fun onResponse(
                    call: Call<SentimentResponse>,
                    response: Response<SentimentResponse>
                ) {
                    if (response.isSuccessful) {
                        val sentiment = response.body()
                        resultLabel.text = "Kết quả: ${sentiment?.label} (${sentiment?.confidence})"

                        when (sentiment?.label) {
                            "positive" -> {
                                emojiLabel.text = "😊"
                                rootLayout.setBackgroundColor(Color.parseColor("#00C853")) // xa lánh iu đời
                            }
                            "neutral" -> {
                                emojiLabel.text = "😐"
                                rootLayout.setBackgroundColor(Color.parseColor("#F5B7B1")) // hồng pát teo
                            }
                            "negative" -> {
                                emojiLabel.text = "😞"
                                rootLayout.setBackgroundColor(Color.parseColor("#8B0000")) // đỏ rực lửa
                            }
                            else -> {
                                emojiLabel.text = "🤔"
                                rootLayout.setBackgroundColor(Color.GRAY)
                            }
                        }


                    } else {
                        resultLabel.text = "Lỗi: Không thể phân tích"
                        emojiLabel.text = "🤔"
                        resultLabel.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
                    }

                }

                override fun onFailure(call: Call<SentimentResponse>, t: Throwable) {
                    resultLabel.text = "Lỗi kết nối: ${t.message}"
                    emojiLabel.text = "😞"  // Mặt buồn nếu gặp lỗi kết nối
                }
            })
        }
    }
}
