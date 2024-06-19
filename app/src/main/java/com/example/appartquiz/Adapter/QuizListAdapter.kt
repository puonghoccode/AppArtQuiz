package com.example.appartquiz.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appartquiz.Model.QuizModel
import com.example.appartquiz.Activity.QuizActivity
import com.example.appartquiz.databinding.QuizItemRecyclerRowBinding

class QuizListAdapter(private val quizModelList: List<QuizModel>) :
    RecyclerView.Adapter<QuizListAdapter.MyViewHolder>() {

    class MyViewHolder(private val binding: QuizItemRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: QuizModel) {
            binding.quizTitleText.text = model.title
            Glide.with(binding.catImg).load(model.image).into(binding.catImg)
            binding.quizTimeText.text = "${model.time} min"
            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, QuizActivity::class.java).apply {
                    QuizActivity.questionModelList = model.questionList
                    QuizActivity.time = model.time
                    QuizActivity.quizId = model.id
                    QuizActivity.quizTitle = model.title  // Pass the quizTitle
                }
                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = QuizItemRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return quizModelList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(quizModelList[position])
    }
}
