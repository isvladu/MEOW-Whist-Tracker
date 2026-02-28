package com.isvladu.meowwhisttracker

import android.content.Context
import com.isvladu.meowwhisttracker.model.*
import org.json.JSONArray
import org.json.JSONObject

data class HistoryEntry(val timestamp: Long, val state: GameState, val stateJson: String)

class GameRepository(context: Context) {

    private val prefs = context.getSharedPreferences("meow_whist_prefs", Context.MODE_PRIVATE)

    fun saveGame(state: GameState) {
        val json = serializeState(state)
        prefs.edit().putString("game_state", json.toString()).apply()
    }

    fun loadGame(): GameState? {
        val str = prefs.getString("game_state", null) ?: return null
        return try {
            deserializeState(JSONObject(str))
        } catch (e: Exception) {
            null
        }
    }

    fun clearGame() {
        prefs.edit().remove("game_state").apply()
    }

    fun saveToHistory(state: GameState) {
        val historyStr = prefs.getString("game_history", "[]")
        val arr = try { JSONArray(historyStr) } catch (e: Exception) { JSONArray() }
        val entry = JSONObject()
        entry.put("timestamp", System.currentTimeMillis())
        entry.put("state", serializeState(state))
        arr.put(entry)
        prefs.edit().putString("game_history", arr.toString()).apply()
    }

    fun loadHistory(): List<HistoryEntry> {
        val historyStr = prefs.getString("game_history", "[]")
        val arr = try { JSONArray(historyStr) } catch (e: Exception) { return emptyList() }
        return (0 until arr.length()).mapNotNull { i ->
            try {
                val obj = arr.getJSONObject(i)
                val stateObj = obj.getJSONObject("state")
                HistoryEntry(
                    timestamp = obj.getLong("timestamp"),
                    state = deserializeState(stateObj),
                    stateJson = stateObj.toString()
                )
            } catch (e: Exception) {
                null
            }
        }.reversed()
    }

    private fun serializeState(state: GameState): JSONObject {
        val obj = JSONObject()
        obj.put("currentRoundIndex", state.currentRoundIndex)
        obj.put("phase", state.phase.name)
        obj.put("currentBidderPosition", state.currentBidderPosition)
        obj.put("premiuEnabled", state.premiuEnabled)

        val playersArr = JSONArray()
        for (p in state.players) {
            val pObj = JSONObject()
            pObj.put("id", p.id)
            pObj.put("name", p.name)
            pObj.put("totalScore", p.totalScore)
            pObj.put("consecutiveHits", p.consecutiveHits)
            playersArr.put(pObj)
        }
        obj.put("players", playersArr)

        val roundsArr = JSONArray()
        for (r in state.rounds) {
            val rObj = JSONObject()
            rObj.put("index", r.index)
            rObj.put("cardCount", r.cardCount)
            rObj.put("trump", r.trump.name)
            rObj.put("dealerIndex", r.dealerIndex)
            val prArr = JSONArray()
            for (pr in r.playerRounds) {
                val prObj = JSONObject()
                prObj.put("playerId", pr.playerId)
                prObj.put("bid", pr.bid)
                prObj.put("taken", pr.taken)
                prObj.put("score", pr.score)
                prArr.put(prObj)
            }
            rObj.put("playerRounds", prArr)
            roundsArr.put(rObj)
        }
        obj.put("rounds", roundsArr)
        return obj
    }

    fun deserializeState(obj: JSONObject): GameState {
        val playersArr = obj.getJSONArray("players")
        val players = (0 until playersArr.length()).map { i ->
            val p = playersArr.getJSONObject(i)
            Player(
                id = p.getInt("id"),
                name = p.getString("name"),
                totalScore = p.getInt("totalScore"),
                consecutiveHits = p.optInt("consecutiveHits", 0)
            )
        }

        val roundsArr = obj.getJSONArray("rounds")
        val rounds = (0 until roundsArr.length()).map { i ->
            val r = roundsArr.getJSONObject(i)
            val prArr = r.getJSONArray("playerRounds")
            val playerRounds = (0 until prArr.length()).map { j ->
                val pr = prArr.getJSONObject(j)
                PlayerRound(
                    playerId = pr.getInt("playerId"),
                    bid = pr.getInt("bid"),
                    taken = pr.getInt("taken"),
                    score = pr.getInt("score")
                )
            }
            Round(
                index = r.getInt("index"),
                cardCount = r.getInt("cardCount"),
                trump = Suit.valueOf(r.getString("trump")),
                dealerIndex = r.getInt("dealerIndex"),
                playerRounds = playerRounds
            )
        }

        return GameState(
            players = players,
            rounds = rounds,
            currentRoundIndex = obj.getInt("currentRoundIndex"),
            phase = GamePhase.valueOf(obj.getString("phase")),
            currentBidderPosition = obj.optInt("currentBidderPosition", 0),
            premiuEnabled = obj.optBoolean("premiuEnabled", false)
        )
    }
}
