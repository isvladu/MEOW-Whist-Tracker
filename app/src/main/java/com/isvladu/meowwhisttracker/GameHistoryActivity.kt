package com.isvladu.meowwhisttracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.isvladu.meowwhisttracker.adapter.HistoryAdapter
import com.isvladu.meowwhisttracker.databinding.ActivityGameHistoryBinding

class GameHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val history = GameRepository(this).loadHistory()
        if (history.isEmpty()) {
            binding.tvNoHistory.visibility = View.VISIBLE
            binding.rvHistory.visibility = View.GONE
        } else {
            binding.tvNoHistory.visibility = View.GONE
            binding.rvHistory.apply {
                layoutManager = LinearLayoutManager(this@GameHistoryActivity)
                adapter = HistoryAdapter(history) { entry ->
                    val intent = Intent(this@GameHistoryActivity, DetailedScoreboardActivity::class.java)
                    intent.putExtra(DetailedScoreboardActivity.EXTRA_GAME_JSON, entry.stateJson)
                    startActivity(intent)
                }
            }
        }
    }
}
