package com.isvladu.meowwhisttracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isvladu.meowwhisttracker.adapter.ScoreboardAdapter
import com.isvladu.meowwhisttracker.databinding.ActivityResultsBinding

class ResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultsBinding
    private lateinit var viewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[GameViewModel::class.java]

        if (viewModel.gameState.value == null) {
            viewModel.restoreGame()
        }

        val state = viewModel.gameState.value
        if (state == null) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }

        val entries = ScoreboardAdapter.buildEntries(state.players, null)
        val adapter = ScoreboardAdapter(entries)
        binding.rvFinalScores.apply {
            layoutManager = LinearLayoutManager(this@ResultsActivity)
            this.adapter = adapter
        }

        binding.btnPlayAgain.setOnClickListener {
            val playerNames = state.players.map { it.name }
            viewModel.clearGame()
            viewModel.startGame(playerNames, state.premiuEnabled)
            startActivity(Intent(this, GameActivity::class.java))
            finish()
        }

        binding.btnNewGame.setOnClickListener {
            viewModel.clearGame()
            val intent = Intent(this, SetupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
