package com.example.appartquiz.Model

data class QuizModel(
    val id : String,
    val title : String,
    val subtitle : String,
    val time : String,
    val questionList : List<QuestionModel>,
    val image: String
){
    constructor() : this("","","","", emptyList(),"")
}

data class QuestionModel(
    val question : String,
    val options : List<String>,
    val correct : String,
){
    constructor() : this ("", emptyList(),"")
}