package com.doodly.app.data.local

enum class Mood(val label: String, val colorHex: String) {
    HAPPY("행복", "#FFD54A"),
    EXCITED("설렘", "#FF8FB1"),
    CALM("평온", "#7ED9A6"),
    TIRED("피곤", "#A0A8B3"),
    SAD("슬픔", "#6FA8FF"),
    ANGRY("화남", "#FF7A6B");

    companion object {
        fun fromName(value: String): Mood = entries.find { it.name == value } ?: CALM
    }
}
