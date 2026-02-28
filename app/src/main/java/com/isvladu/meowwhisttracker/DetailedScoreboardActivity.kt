package com.isvladu.meowwhisttracker

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.isvladu.meowwhisttracker.databinding.ActivityDetailedScoreboardBinding
import com.isvladu.meowwhisttracker.model.GameState
import org.json.JSONObject

class DetailedScoreboardActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GAME_JSON = "extra_game_json"
    }

    private lateinit var binding: ActivityDetailedScoreboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailedScoreboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val repo = GameRepository(this)
        val gameJson = intent.getStringExtra(EXTRA_GAME_JSON)
        val state: GameState? = if (gameJson != null) {
            try { repo.deserializeState(JSONObject(gameJson)) } catch (e: Exception) { null }
        } else {
            repo.loadGame()
        }
        if (state != null) buildTable(state)
    }

    private fun buildTable(state: GameState) {
        val table = binding.tableScoreboard
        val players = state.players
        val orange = getColor(R.color.cat_orange)
        val white = getColor(R.color.white)
        val brown = getColor(R.color.cat_brown)
        val successBg = getColor(R.color.success_bg)
        val errorBg = getColor(R.color.error_bg)
        val creamBg = getColor(R.color.cat_cream)
        val greyBg = Color.parseColor("#E8E0D8")

        val colRoundDp = 64
        val colPlayerDp = 90

        // Header row
        val headerRow = TableRow(this)
        headerRow.addView(makeCell("Rd\nCărți", colRoundDp, orange, white, true))
        for (p in players) {
            headerRow.addView(makeCell(p.name, colPlayerDp, orange, white, true))
        }
        table.addView(headerRow)

        // Round rows
        for (i in 0..state.currentRoundIndex) {
            val round = state.rounds[i]
            val rowBg = if (i % 2 == 0) white else creamBg

            val row = TableRow(this)
            row.addView(makeCell("R${i + 1}\n${round.cardCount}", colRoundDp, greyBg, brown, false))

            for (p in players) {
                val pr = round.playerRounds.first { it.playerId == p.id }
                val (cellText, cellBg) = when {
                    pr.taken >= 0 -> {
                        val scoreStr = if (pr.score > 0) "+${pr.score}" else "${pr.score}"
                        val hit = pr.bid == pr.taken
                        "${pr.bid}/${pr.taken}\n$scoreStr" to if (hit) successBg else errorBg
                    }
                    pr.bid >= 0 -> "${pr.bid}/?" to rowBg
                    else -> "?" to rowBg
                }
                row.addView(makeCell(cellText, colPlayerDp, cellBg, brown, false))
            }
            table.addView(row)
        }

        // Total row
        val totalRow = TableRow(this)
        totalRow.addView(makeCell("TOTAL", colRoundDp, orange, white, true))
        for (p in players) {
            val scoreStr = if (p.totalScore > 0) "+${p.totalScore}" else "${p.totalScore}"
            totalRow.addView(makeCell(scoreStr, colPlayerDp, orange, white, true))
        }
        table.addView(totalRow)
    }

    private fun makeCell(
        text: String,
        widthDp: Int,
        bgColor: Int,
        textColor: Int,
        bold: Boolean
    ): TextView {
        val density = resources.displayMetrics.density
        return TextView(this).apply {
            this.text = text
            setBackgroundColor(bgColor)
            setTextColor(textColor)
            gravity = Gravity.CENTER
            setPadding(8, 12, 8, 12)
            textSize = 12f
            if (bold) setTypeface(null, Typeface.BOLD)
            layoutParams = TableRow.LayoutParams((widthDp * density).toInt(), TableRow.LayoutParams.WRAP_CONTENT).also {
                it.setMargins(1, 1, 1, 1)
            }
        }
    }
}
