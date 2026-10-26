package com.example.militaryid.data

import android.content.Context
import android.content.SharedPreferences

enum class LearningStatus {
    UNSEEN,
    LEARNING,   // ✕
    REVIEWING,  // △
    MASTERED    // ◯
}

class ProgressManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("military_id_prefs", Context.MODE_PRIVATE)

    fun getStatus(weaponId: String): LearningStatus {
        val s = prefs.getString("status_$weaponId", null) ?: return LearningStatus.UNSEEN
        return try {
            LearningStatus.valueOf(s)
        } catch (e: Exception) {
            LearningStatus.UNSEEN
        }
    }

    fun setStatus(weaponId: String, status: LearningStatus) {
        prefs.edit().putString("status_$weaponId", status.name).apply()
    }

    fun getCounts(): Map<LearningStatus, Int> {
        val all = WeaponsRepository.weapons
        val counts = mutableMapOf(
            LearningStatus.UNSEEN to 0,
            LearningStatus.LEARNING to 0,
            LearningStatus.REVIEWING to 0,
            LearningStatus.MASTERED to 0
        )
        for (w in all) {
            val s = getStatus(w.id)
            counts[s] = (counts[s] ?: 0) + 1
        }
        return counts
    }

    fun resetAll() {
        prefs.edit().clear().apply()
    }
}
