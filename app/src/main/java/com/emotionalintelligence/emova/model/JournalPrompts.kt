package com.emotionalintelligence.emova.model

import com.emotionalintelligence.emova.R

val journalPromptResIds: Map<EmotionFamily, List<Int>> = mapOf(
    EmotionFamily.POSITIVE  to listOf(R.string.prompt_pos_1, R.string.prompt_pos_2, R.string.prompt_pos_3),
    EmotionFamily.DIFFICULT to listOf(R.string.prompt_dif_1, R.string.prompt_dif_2, R.string.prompt_dif_3),
    EmotionFamily.ACTIVATED to listOf(R.string.prompt_act_1, R.string.prompt_act_2, R.string.prompt_act_3)
)

val journalDefaultPromptResIds = listOf(
    R.string.prompt_default_1,
    R.string.prompt_default_2,
    R.string.prompt_default_3
)

fun getPromptResIdsForFamily(family: EmotionFamily): List<Int> =
    journalPromptResIds[family] ?: journalDefaultPromptResIds