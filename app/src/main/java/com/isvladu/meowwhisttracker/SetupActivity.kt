package com.isvladu.meowwhisttracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.isvladu.meowwhisttracker.adapter.PlayerSetupAdapter
import com.isvladu.meowwhisttracker.databinding.ActivitySetupBinding

class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding
    private lateinit var viewModel: GameViewModel
    private lateinit var playerAdapter: PlayerSetupAdapter
    private var selectedPlayerCount = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[GameViewModel::class.java]

        playerAdapter = PlayerSetupAdapter(selectedPlayerCount)
        binding.rvPlayerNames.apply {
            layoutManager = LinearLayoutManager(this@SetupActivity)
            adapter = playerAdapter
        }

        setupChips()

        binding.btnStart.setOnClickListener {
            val names = playerAdapter.getNames()
            val premiuEnabled = binding.switchPremiu.isChecked
            viewModel.clearGame()
            viewModel.startGame(names, premiuEnabled)
            startActivity(Intent(this, GameActivity::class.java))
        }

        binding.btnResume.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, GameHistoryActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val hasSaved = viewModel.restoreGame()
        binding.btnResume.visibility = if (hasSaved) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun setupChips() {
        binding.chipGroupPlayers.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            selectedPlayerCount = when (checkedIds[0]) {
                R.id.chip2 -> 2
                R.id.chip3 -> 3
                R.id.chip4 -> 4
                R.id.chip5 -> 5
                R.id.chip6 -> 6
                else -> 4
            }
            playerAdapter.setCount(selectedPlayerCount)
        }
        binding.chip4.isChecked = true
    }
}
