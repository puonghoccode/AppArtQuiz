package com.example.appartquiz.Model

data class QuizModel(
    val id: String = "",
    val image: String = "",
    val questionList: List<QuestionModel> = emptyList(),
    val time: String = "",
    val title: String = ""
)

data class QuestionModel(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correct: String = ""
)
