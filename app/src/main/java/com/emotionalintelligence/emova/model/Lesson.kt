package com.emotionalintelligence.emova.model

import com.emotionalintelligence.emova.R

data class Lesson(
    val id: String,
    val titleResId: Int, // Cambiado a Int (ResId)
    val category: LessonCategory,
    val duration: String,
    val descriptionResId: Int, // Cambiado a Int (ResId)
    val content: LessonContent
)

enum class LessonCategory(val label: String, val emoji: String, val labelResId: Int) {
    SELF_AWARENESS("Self-Awareness", "🪞", R.string.lesson_cat_awareness),
    EMPATHY        ("Empathy",        "🤝", R.string.lesson_cat_empathy),
    REGULATION     ("Regulation",     "🌿", R.string.lesson_cat_regulation),
    SOCIAL         ("Social Skills",  "💬", R.string.lesson_cat_social),
    MOTIVATION     ("Motivation",     "🎯", R.string.lesson_cat_motivation),
    DEEP_DIVE      ("Deep Dive",      "🤿", R.string.lesson_cat_deepdive) // 🎯 NUEVA CATEGORÍA
}

sealed class LessonContent {
    data class Reflection(val promptResId: Int, val hintResIds: List<Int>) : LessonContent()
    data class Scenario(val situationResId: Int, val options: List<ScenarioOption>) : LessonContent()
    data class Breathing(val steps: List<BreathingStep>) : LessonContent()

    data class GuidedJournal(
        val promptResIds: List<Int>,
        val finishEmoji: String,
        val finishTitleResId: Int,
        val finishBodyResId: Int
    ) : LessonContent()
}

data class ScenarioOption(val textResId: Int, val feedbackResId: Int, val isIdeal: Boolean)
data class BreathingStep(val instructionResId: Int, val durationSeconds: Int, val emoji: String)

// ─── Base de Datos Reactiva (Usando solo IDs de recursos) ────────────

val lessonLibrary = listOf(
    Lesson(
        id = "sa_01",
        titleResId = R.string.lesson_sa01_title,
        category = LessonCategory.SELF_AWARENESS,
        duration = "3 min",
        descriptionResId = R.string.lesson_sa01_desc,
        content = LessonContent.Reflection(
            promptResId = R.string.lesson_sa01_prompt,
            hintResIds = listOf(R.string.lesson_sa01_hint1, R.string.lesson_sa01_hint2, R.string.lesson_sa01_hint3)
        )
    ),

    Lesson(
        id = "sa_02",
        titleResId = R.string.lesson_sa02_title,
        category = LessonCategory.SELF_AWARENESS,
        duration = "4 min",
        descriptionResId = R.string.lesson_sa02_desc,
        content = LessonContent.Reflection(
            promptResId = R.string.lesson_sa02_prompt,
            hintResIds = listOf(R.string.lesson_sa02_hint1, R.string.lesson_sa02_hint2, R.string.lesson_sa02_hint3)
        )
    ),

    Lesson(
        id = "sa_03",
        titleResId = R.string.lesson_sa03_title,
        category = LessonCategory.SELF_AWARENESS,
        duration = "5 min",
        descriptionResId = R.string.lesson_sa03_desc,
        content = LessonContent.Reflection(
            promptResId = R.string.lesson_sa03_prompt,
            hintResIds = listOf(R.string.lesson_sa03_hint1, R.string.lesson_sa03_hint2, R.string.lesson_sa03_hint3)
        )
    ),

    Lesson(
        id = "em_01",
        titleResId = R.string.lesson_em01_title,
        category = LessonCategory.EMPATHY,
        duration = "4 min",
        descriptionResId = R.string.lesson_em01_desc,
        content = LessonContent.Reflection(
            promptResId = R.string.lesson_em01_prompt,
            hintResIds = listOf(R.string.lesson_em01_hint1, R.string.lesson_em01_hint2, R.string.lesson_em01_hint3)
        )
    ),

    Lesson(
        id = "em_02",
        titleResId = R.string.lesson_em02_title,
        category = LessonCategory.EMPATHY,
        duration = "3 min",
        descriptionResId = R.string.lesson_em02_desc,
        content = LessonContent.Scenario(
            situationResId = R.string.lesson_em02_situation,
            options = listOf(
                ScenarioOption(R.string.lesson_em02_opt1_text, R.string.lesson_em02_opt1_fb, false),
                ScenarioOption(R.string.lesson_em02_opt2_text, R.string.lesson_em02_opt2_fb, true),
                ScenarioOption(R.string.lesson_em02_opt3_text, R.string.lesson_em02_opt3_fb, false)
            )
        )
    ),

    Lesson(
        id = "em_03",
        titleResId = R.string.lesson_em03_title,
        category = LessonCategory.EMPATHY,
        duration = "3 min",
        descriptionResId = R.string.lesson_em03_desc,
        content = LessonContent.Reflection(
            promptResId = R.string.lesson_em03_prompt,
            hintResIds = listOf(R.string.lesson_em03_hint1, R.string.lesson_em03_hint2, R.string.lesson_em03_hint3)
        )
    ),

    Lesson(
        id = "re_01",
        titleResId = R.string.lesson_re01_title,
        category = LessonCategory.REGULATION,
        duration = "4 min",
        descriptionResId = R.string.lesson_re01_desc,
        content = LessonContent.Reflection(
            promptResId = R.string.lesson_re01_prompt,
            hintResIds = listOf(
                R.string.lesson_re01_hint1, R.string.lesson_re01_hint2, R.string.lesson_re01_hint3,
                R.string.lesson_re01_hint4, R.string.lesson_re01_hint5, R.string.lesson_re01_hint6, R.string.lesson_re01_hint7
            )
        )
    ),

    Lesson(
        id = "re_02",
        titleResId = R.string.lesson_re02_title,
        category = LessonCategory.REGULATION,
        duration = "3 min",
        descriptionResId = R.string.lesson_re02_desc,
        content = LessonContent.Scenario(
            situationResId = R.string.lesson_re02_situation,
            options = listOf(
                ScenarioOption(R.string.lesson_re02_opt1_text, R.string.lesson_re02_opt1_fb, false),
                ScenarioOption(R.string.lesson_re02_opt2_text, R.string.lesson_re02_opt2_fb, true),
                ScenarioOption(R.string.lesson_re02_opt3_text, R.string.lesson_re02_opt3_fb, false)
            )
        )
    ),

    Lesson(
        id = "ss_01",
        titleResId = R.string.lesson_ss01_title,
        category = LessonCategory.SOCIAL,
        duration = "4 min",
        descriptionResId = R.string.lesson_ss01_desc,
        content = LessonContent.Scenario(
            situationResId = R.string.lesson_ss01_situation,
            options = listOf(
                ScenarioOption(R.string.lesson_ss01_opt1_text, R.string.lesson_ss01_opt1_fb, false),
                ScenarioOption(R.string.lesson_ss01_opt2_text, R.string.lesson_ss01_opt2_fb, true),
                ScenarioOption(R.string.lesson_ss01_opt3_text, R.string.lesson_ss01_opt3_fb, false)
            )
        )
    ),

    Lesson(
        id = "dd_01",
        titleResId = R.string.lesson_dd01_title,
        category = LessonCategory.DEEP_DIVE,
        duration = "5 min",
        descriptionResId = R.string.lesson_dd01_desc,
        content = LessonContent.GuidedJournal(
            promptResIds = listOf(
                R.string.lesson_dd01_q1, // ¿Qué lo causó?
                R.string.lesson_dd01_q2, // ¿Cómo se siente en tu cuerpo?
                R.string.lesson_dd01_q3  // ¿Qué intentaste hacer?
            ),
            finishEmoji = "🧠",
            finishTitleResId = R.string.lesson_dd_done_title,
            finishBodyResId = R.string.lesson_dd_done_body
        )
    ),

    // 🎯 3. NUEVO: Deep Dive 2 (Diario de Resiliencia)
    Lesson(
        id = "dd_02",
        titleResId = R.string.lesson_dd02_title,
        category = LessonCategory.DEEP_DIVE,
        duration = "5 min",
        descriptionResId = R.string.lesson_dd02_desc,
        content = LessonContent.GuidedJournal(
            promptResIds = listOf(
                R.string.lesson_dd02_q1, // ¿Qué aprendiste?
                R.string.lesson_dd02_q2, // ¿Qué harás diferente?
                R.string.lesson_dd02_q3  // ¿Qué necesitas ahora?
            ),
            finishEmoji = "🌱",
            finishTitleResId = R.string.lesson_dd_done_title,
            finishBodyResId = R.string.lesson_dd_done_body
        )
    ),

    Lesson(
        id = "mo_01",
        titleResId = R.string.lesson_mt01_title,
        category = LessonCategory.MOTIVATION,
        duration = "4 min",
        descriptionResId = R.string.lesson_mt01_desc,
        content = LessonContent.Reflection(
            promptResId = R.string.lesson_mt01_prompt,
            hintResIds = listOf(R.string.lesson_mt01_hint1, R.string.lesson_mt01_hint2, R.string.lesson_mt01_hint3)
        )
    ),

    Lesson(
        id = "mo_02",
        titleResId = R.string.lesson_mt02_title,
        category = LessonCategory.MOTIVATION,
        duration = "3 min",
        descriptionResId = R.string.lesson_mt02_desc,
        content = LessonContent.Scenario(
            situationResId = R.string.lesson_mt02_situation,
            options = listOf(
                ScenarioOption(R.string.lesson_mt02_opt1_text, R.string.lesson_mt02_opt1_fb, false),
                ScenarioOption(R.string.lesson_mt02_opt2_text, R.string.lesson_mt02_opt2_fb, true),
                ScenarioOption(R.string.lesson_mt02_opt3_text, R.string.lesson_mt02_opt3_fb, false)
            )
        )
    ),

    Lesson(
        id = "mo_03",
        titleResId = R.string.lesson_mt03_title,
        category = LessonCategory.MOTIVATION,
        duration = "4 min",
        descriptionResId = R.string.lesson_mt03_desc,
        content = LessonContent.GuidedJournal(
            promptResIds = listOf(R.string.lesson_mt03_q1, R.string.lesson_mt03_q2, R.string.lesson_mt03_q3),
            finishEmoji = "🏆",
            finishTitleResId = R.string.lesson_mt03_done_title,
            finishBodyResId = R.string.lesson_mt03_done_body
        )
    ),

    Lesson(
        id = "ss_02",
        titleResId = R.string.lesson_ss02_title,
        category = LessonCategory.SOCIAL,
        duration = "3 min",
        descriptionResId = R.string.lesson_ss02_desc,
        content = LessonContent.Scenario(
            situationResId = R.string.lesson_ss02_situation,
            options = listOf(
                ScenarioOption(R.string.lesson_ss02_opt1_text, R.string.lesson_ss02_opt1_fb, false),
                ScenarioOption(R.string.lesson_ss02_opt2_text, R.string.lesson_ss02_opt2_fb, true),
                ScenarioOption(R.string.lesson_ss02_opt3_text, R.string.lesson_ss02_opt3_fb, false)
            )
        )
    ),

    Lesson(
        id = "ss_03",
        titleResId = R.string.lesson_ss03_title,
        category = LessonCategory.SOCIAL,
        duration = "4 min",
        descriptionResId = R.string.lesson_ss03_desc,
        content = LessonContent.Reflection(
            promptResId = R.string.lesson_ss03_prompt,
            hintResIds = listOf(R.string.lesson_ss03_hint1, R.string.lesson_ss03_hint2, R.string.lesson_ss03_hint3)
        )
    ),

    Lesson(
        id = "re_03",
        titleResId = R.string.lesson_re03_title,
        category = LessonCategory.REGULATION,
        duration = "3 min",
        descriptionResId = R.string.lesson_re03_desc,
        content = LessonContent.Reflection(
            promptResId = R.string.lesson_re03_prompt,
            hintResIds = listOf(R.string.lesson_re03_hint1, R.string.lesson_re03_hint2, R.string.lesson_re03_hint3)
        )
    ),

    Lesson(
        id = "re_04",
        titleResId = R.string.lesson_re04_title,
        category = LessonCategory.REGULATION,
        duration = "3 min",
        descriptionResId = R.string.lesson_re04_desc,
        content = LessonContent.Scenario(
            situationResId = R.string.lesson_re04_situation,
            options = listOf(
                ScenarioOption(R.string.lesson_re04_opt1_text, R.string.lesson_re04_opt1_fb, false),
                ScenarioOption(R.string.lesson_re04_opt2_text, R.string.lesson_re04_opt2_fb, true),
                ScenarioOption(R.string.lesson_re04_opt3_text, R.string.lesson_re04_opt3_fb, false)
            )
        )
    )
)

/**
 * The order lessons are offered as "today's lesson": categories are interleaved so the
 * daily lesson feels varied. Lessons missing from this list are appended at the end.
 */
private val lessonPathOrder = listOf(
    "sa_01", "re_01", "em_01", "mo_01", "ss_01",
    "sa_02", "re_02", "em_02", "mo_02", "ss_02",
    "dd_01", "sa_03", "re_03", "em_03", "mo_03",
    "ss_03", "re_04", "dd_02"
)

val lessonPath: List<Lesson> = run {
    val byId = lessonLibrary.associateBy { it.id }
    val ordered = lessonPathOrder.mapNotNull { byId[it] }
    ordered + lessonLibrary.filter { it !in ordered }
}
