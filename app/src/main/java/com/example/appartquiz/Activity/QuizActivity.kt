package com.example.appartquiz.Activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.appartquiz.Model.QuestionModel
import com.example.appartquiz.Model.QuizModel
import com.example.appartquiz.R
import com.example.appartquiz.databinding.ActivityQuizBinding
import com.example.appartquiz.databinding.ScoreDialogBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson

class QuizActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    lateinit var binding: ActivityQuizBinding
    private var questionModelList: MutableList<QuestionModel> = mutableListOf()
    private var currentQuestionIndex = 0
    private var selectedAnswer = ""
    private var score = 0
    private var quizTime = 0

    companion object {
        lateinit var questionModelList: List<QuestionModel>
        var time: String? = null
        var quizId: String? = null
        var quizTitle: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)
        binding.apply {
            btn0.setOnClickListener(this@QuizActivity)
            btn1.setOnClickListener(this@QuizActivity)
            btn2.setOnClickListener(this@QuizActivity)
            btn3.setOnClickListener(this@QuizActivity)
            nextBtn.setOnClickListener(this@QuizActivity)
        }

        getDataFromFirebase()
    }

    private fun startTimer() {
        val totalTimeInMillis = quizTime * 60 * 1000L
        object : CountDownTimer(totalTimeInMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                binding.timerIndicatorTextview.text = String.format("%02d:%02d", minutes, remainingSeconds)
            }

            override fun onFinish() {
                finishQuiz()
            }
        }.start()
    }

    private fun loadQuestions() {
        selectedAnswer = ""
        if (currentQuestionIndex == questionModelList.size) {
            finishQuiz()
            return
        }

        binding.apply {
            val questionModel = questionModelList[currentQuestionIndex]
            questionIndicatorTextview.text = "Question ${currentQuestionIndex + 1}/${questionModelList.size}"
            questionProgressIndicator.progress = ((currentQuestionIndex + 1).toFloat() / questionModelList.size * 100).toInt()
            questionTextview.text = questionModel.question
            btn0.text = questionModel.options[0]
            btn1.text = questionModel.options[1]
            btn2.text = questionModel.options[2]
            btn3.text = questionModel.options[3]

            resetButtonColors()
        }
    }

    override fun onClick(view: View?) {
        val clickedBtn = view as Button
        if (clickedBtn.id == R.id.next_btn) {
            if (selectedAnswer.isEmpty()) {
                Toast.makeText(applicationContext, "Please select an answer to continue", Toast.LENGTH_SHORT).show()
                return
            }
            if (selectedAnswer == questionModelList[currentQuestionIndex].correct) {
                score++
            }
            currentQuestionIndex++
            loadQuestions()
        } else {
            selectedAnswer = clickedBtn.text.toString()
            resetButtonColors()
            clickedBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.orange))
        }
    }

    private fun resetButtonColors() {
        binding.apply {
            btn0.setBackgroundColor(ContextCompat.getColor(this@QuizActivity, R.color.buttermilk))
            btn1.setBackgroundColor(ContextCompat.getColor(this@QuizActivity, R.color.buttermilk))
            btn2.setBackgroundColor(ContextCompat.getColor(this@QuizActivity, R.color.buttermilk))
            btn3.setBackgroundColor(ContextCompat.getColor(this@QuizActivity, R.color.buttermilk))
        }
    }

    private fun finishQuiz() {
        val totalQuestions = questionModelList.size
        val percentage = ((score.toFloat() / totalQuestions.toFloat()) * 100).toInt()

        val dialogBinding = ScoreDialogBinding.inflate(layoutInflater)
        dialogBinding.apply {
            scoreProgressIndicator.progress = percentage
            scoreProgressIndicator.setIndicatorColor(ContextCompat.getColor(this@QuizActivity, R.color.yellow))
            scoreProgressText.text = "$percentage%"
            if (percentage > 60) {
                scoreTitle.text = "Congrats! You have passed"
                scoreTitle.setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.white))
            } else {
                scoreTitle.text = "Oops! You have failed"
                scoreTitle.setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.white))
            }
            scoreSubtitle.text = "$score out of $totalQuestions are correct"
            scoreSubtitle.setTextColor(ContextCompat.getColor(this@QuizActivity, R.color.white))
            finishBtn.setOnClickListener {
                finish()
            }
        }

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .show()
    }

    private fun getDataFromFirebase() {
        FirebaseDatabase.getInstance().reference
            .child("0")
            .child("quizzes")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            val jsonData = snapshot.value.toString()
                            val quizModel = Gson().fromJson(jsonData, QuizModel::class.java)
                            questionModelList.addAll(quizModel.questionList)
                            quizTime = quizModel.time.toInt()
                        }
                        questionModelList.shuffle()
                        loadQuestions()
                        startTimer()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle possible errors.
                    Toast.makeText(this@QuizActivity, "Failed to load data: ${databaseError.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_favorites -> {
                // Already in quiz activity
                return true
            }
            R.id.navigation_discover -> {
                val intent = Intent(this, DiscoveryActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.navigation_settings -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return false
    }
}
