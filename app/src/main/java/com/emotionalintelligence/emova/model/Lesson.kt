package com.emotionalintelligence.emova.model

import android.content.Context
import com.emotionalintelligence.emova.R

data class Lesson(
    val id: String,
    val title: String,
    val category: LessonCategory,
    val duration: String,
    val description: String,
    val content: LessonContent,
    val titleResId: Int = 0,
    val descResId: Int = 0
)

enum class LessonCategory(val label: String, val emoji: String, val labelResId: Int) {
    SELF_AWARENESS("Self-Awareness", "🪞", R.string.lesson_cat_awareness),
    EMPATHY        ("Empathy",        "🤝", R.string.lesson_cat_empathy),
    REGULATION     ("Regulation",     "🌿", R.string.lesson_cat_regulation),
    SOCIAL         ("Social Skills",  "💬", R.string.lesson_cat_social),
    MOTIVATION     ("Motivation",     "🎯", R.string.lesson_cat_motivation)
}

sealed class LessonContent {
    data class Reflection(val prompt: String, val hints: List<String>) : LessonContent()
    data class Scenario(val situation: String, val options: List<ScenarioOption>) : LessonContent()
    data class Breathing(val steps: List<BreathingStep>) : LessonContent()
    data class Gratitude(val prompts: List<String>) : LessonContent()
}

data class ScenarioOption(val text: String, val feedback: String, val isIdeal: Boolean)
data class BreathingStep(val instruction: String, val durationSeconds: Int, val emoji: String)

// ─── English baseline (used for DB storage, IDs must never change) ────────────

val lessonLibrary = listOf(
    Lesson("sa_01", "Name It to Tame It", LessonCategory.SELF_AWARENESS, "3 min",
        "Labeling emotions reduces their intensity. Practice precision.",
        LessonContent.Reflection(
            prompt = "Think of a moment this week when you felt uncomfortable. Instead of 'bad' or 'stressed', what is the most precise word for what you felt?",
            hints = listOf("Was it closer to anxious, overwhelmed, or dread?", "Was it frustration, resentment, or disappointment?", "Was it loneliness, exclusion, or rejection?")
        ), R.string.lesson_sa01_title, R.string.lesson_sa01_desc),

    Lesson("sa_02", "Body Scan Check-In", LessonCategory.SELF_AWARENESS, "4 min",
        "Emotions live in the body. Learn to read your physical signals.",
        LessonContent.Reflection(
            prompt = "Close your eyes and scan your body from head to toe. Where do you feel tension, tightness, or ease? What might that sensation be telling you emotionally?",
            hints = listOf("Tight chest or shoulders often signal stress or anxiety", "Heaviness in the stomach can mean dread or guilt", "Warmth in the chest often signals joy or love")
        ), R.string.lesson_sa02_title, R.string.lesson_sa02_desc),

    Lesson("sa_03", "Your Trigger Map", LessonCategory.SELF_AWARENESS, "5 min",
        "Discover what consistently sets off strong reactions in you.",
        LessonContent.Reflection(
            prompt = "Think of 3 situations that reliably make you feel angry, anxious, or hurt. What do they have in common? What value or need is being threatened?",
            hints = listOf("Common triggers: feeling ignored, losing control, being criticized", "Look for patterns across different people or places", "Behind every trigger is usually an unmet need")
        ), R.string.lesson_sa03_title, R.string.lesson_sa03_desc),

    Lesson("em_01", "The Other Chair", LessonCategory.EMPATHY, "4 min",
        "Step into someone else's perspective — even someone difficult.",
        LessonContent.Reflection(
            prompt = "Think of someone you recently had a conflict or tension with. Try to describe the situation entirely from their point of view — their fears, pressures, and intentions. What might they have been feeling?",
            hints = listOf("Assume positive or neutral intent first", "What challenges might they be dealing with that you don't see?", "How would you feel if you were in their exact situation?")
        ), R.string.lesson_em01_title, R.string.lesson_em01_desc),

    Lesson("em_02", "Empathy vs Sympathy", LessonCategory.EMPATHY, "3 min",
        "Learn the crucial difference and practice responding with empathy.",
        LessonContent.Scenario(
            situation = "Your friend says: 'I've been so anxious about work lately, I can barely sleep.' How do you respond?",
            options = listOf(
                ScenarioOption("\"At least you still have a job! Things could be worse.\"", "This minimizes their feeling. Comparing to worse situations rarely helps — it signals that their pain isn't valid.", false),
                ScenarioOption("\"Oh no, that sounds really hard. What's been going on?\"", "This is empathy — you acknowledge the feeling and invite them to share more without judgment.", true),
                ScenarioOption("\"You should try meditation, it really helped me.\"", "Jumping to solutions skips over the emotional need to feel heard first. Save advice for later.", false)
            )
        ), R.string.lesson_em02_title, R.string.lesson_em02_desc),

    Lesson("em_03", "Compassionate Listening", LessonCategory.EMPATHY, "3 min",
        "Most people listen to reply. Practice listening to understand.",
        LessonContent.Reflection(
            prompt = "In your next conversation today, try to listen without planning your response. Just absorb. Afterward, reflect: What did you notice about the other person that you might have missed before?",
            hints = listOf("Notice their tone, not just their words", "What emotions are underneath what they're saying?", "What do they seem to need from this conversation?")
        ), R.string.lesson_em03_title, R.string.lesson_em03_desc),

    Lesson("re_01", "The Body Scan", LessonCategory.REGULATION, "4 min",
        "A mindfulness technique to release tension held in your body.",
        LessonContent.Reflection(
            prompt = "Find a comfortable position. Close your eyes and slowly scan from head to toe — noticing tension without judgment, letting each area soften as you breathe out.",
            hints = listOf("Sit or lie down comfortably", "Close your eyes and take 3 slow breaths", "Bring attention to the top of your head", "Slowly scan down — notice tension without judging", "At each area, breathe out and let it soften", "Continue all the way down to your feet", "Open your eyes and notice how you feel")
        ), R.string.lesson_re01_title, R.string.lesson_re01_desc),

    Lesson("re_02", "The 10-Second Pause", LessonCategory.REGULATION, "3 min",
        "One habit that can change how you respond under stress.",
        LessonContent.Scenario(
            situation = "You get a passive-aggressive email from a colleague in front of your team. Your first instinct is to fire back immediately. What do you do?",
            options = listOf(
                ScenarioOption("Reply immediately and set the record straight", "Reacting immediately when emotional often escalates things. Your amygdala is driving — not your best self.", false),
                ScenarioOption("Take 10 seconds, breathe, then decide if a reply is even needed now", "The pause interrupts the stress response. Most situations benefit from a short delay — your reply will be clearer and calmer.", true),
                ScenarioOption("Ignore it completely and say nothing", "Avoidance can work short-term but unresolved tensions tend to grow. A thoughtful response later is usually better.", false)
            )
        ), R.string.lesson_re02_title, R.string.lesson_re02_desc),

    Lesson("ss_01", "Giving Feedback with Care", LessonCategory.SOCIAL, "4 min",
        "Feedback is a gift — when it's delivered well.",
        LessonContent.Scenario(
            situation = "A teammate keeps missing deadlines. You need to address it. How do you start the conversation?",
            options = listOf(
                ScenarioOption("\"You're always late with everything. It's affecting the whole team.\"", "Using 'always' and leading with blame triggers defensiveness. It's about them as a person, not the behavior.", false),
                ScenarioOption("\"I wanted to check in — I've noticed the last two deadlines were missed. Is everything okay?\"", "This opens with curiosity and specific facts. It assumes something might be wrong rather than leading with blame — much more effective.", true),
                ScenarioOption("\"Just a heads up, the team is getting frustrated.\"", "Indirect feedback rarely leads to change and can feel cowardly. Be specific and direct but kind.", false)
            )
        ), R.string.lesson_ss01_title, R.string.lesson_ss01_desc),

    Lesson("mo_01", "Gratitude Depth Dive", LessonCategory.MOTIVATION, "5 min",
        "Go beyond listing — explore why you're grateful.",
        LessonContent.Gratitude(
            prompts = listOf(
                "Name one person who made a positive difference in your life recently. What specifically did they do?",
                "What is one small thing today that you might normally overlook?",
                "What challenge are you grateful for, even if it was hard?"
            )
        ), R.string.lesson_mo01_title, R.string.lesson_mo01_desc)
)

// ─── Localized lesson library — call from LessonViewModel ────────────────────
// Replace `lessonLibrary` with `localizedLessonLibrary(getApplication())` in your ViewModel

fun localizedLessonLibrary(context: Context): List<Lesson> {
    fun str(id: Int) = context.getString(id)
    return listOf(
        lessonLibrary[0].copy(title = str(R.string.lesson_sa01_title), description = str(R.string.lesson_sa01_desc),
            content = LessonContent.Reflection(str(R.string.lesson_sa01_prompt),
                listOf(str(R.string.lesson_sa01_hint1), str(R.string.lesson_sa01_hint2), str(R.string.lesson_sa01_hint3)))),

        lessonLibrary[1].copy(title = str(R.string.lesson_sa02_title), description = str(R.string.lesson_sa02_desc),
            content = LessonContent.Reflection(str(R.string.lesson_sa02_prompt),
                listOf(str(R.string.lesson_sa02_hint1), str(R.string.lesson_sa02_hint2), str(R.string.lesson_sa02_hint3)))),

        lessonLibrary[2].copy(title = str(R.string.lesson_sa03_title), description = str(R.string.lesson_sa03_desc),
            content = LessonContent.Reflection(str(R.string.lesson_sa03_prompt),
                listOf(str(R.string.lesson_sa03_hint1), str(R.string.lesson_sa03_hint2), str(R.string.lesson_sa03_hint3)))),

        lessonLibrary[3].copy(title = str(R.string.lesson_em01_title), description = str(R.string.lesson_em01_desc),
            content = LessonContent.Reflection(str(R.string.lesson_em01_prompt),
                listOf(str(R.string.lesson_em01_hint1), str(R.string.lesson_em01_hint2), str(R.string.lesson_em01_hint3)))),

        lessonLibrary[4].copy(title = str(R.string.lesson_em02_title), description = str(R.string.lesson_em02_desc),
            content = LessonContent.Scenario(str(R.string.lesson_em02_situation), listOf(
                ScenarioOption(str(R.string.lesson_em02_opt1_text), str(R.string.lesson_em02_opt1_fb), false),
                ScenarioOption(str(R.string.lesson_em02_opt2_text), str(R.string.lesson_em02_opt2_fb), true),
                ScenarioOption(str(R.string.lesson_em02_opt3_text), str(R.string.lesson_em02_opt3_fb), false)))),

        lessonLibrary[5].copy(title = str(R.string.lesson_em03_title), description = str(R.string.lesson_em03_desc),
            content = LessonContent.Reflection(str(R.string.lesson_em03_prompt),
                listOf(str(R.string.lesson_em03_hint1), str(R.string.lesson_em03_hint2), str(R.string.lesson_em03_hint3)))),

        lessonLibrary[6].copy(title = str(R.string.lesson_re01_title), description = str(R.string.lesson_re01_desc),
            content = LessonContent.Reflection(str(R.string.lesson_re01_prompt),
                listOf(str(R.string.lesson_re01_hint1), str(R.string.lesson_re01_hint2), str(R.string.lesson_re01_hint3),
                    str(R.string.lesson_re01_hint4), str(R.string.lesson_re01_hint5), str(R.string.lesson_re01_hint6), str(R.string.lesson_re01_hint7)))),

        lessonLibrary[7].copy(title = str(R.string.lesson_re02_title), description = str(R.string.lesson_re02_desc),
            content = LessonContent.Scenario(str(R.string.lesson_re02_situation), listOf(
                ScenarioOption(str(R.string.lesson_re02_opt1_text), str(R.string.lesson_re02_opt1_fb), false),
                ScenarioOption(str(R.string.lesson_re02_opt2_text), str(R.string.lesson_re02_opt2_fb), true),
                ScenarioOption(str(R.string.lesson_re02_opt3_text), str(R.string.lesson_re02_opt3_fb), false)))),

        lessonLibrary[8].copy(title = str(R.string.lesson_ss01_title), description = str(R.string.lesson_ss01_desc),
            content = LessonContent.Scenario(str(R.string.lesson_ss01_situation), listOf(
                ScenarioOption(str(R.string.lesson_ss01_opt1_text), str(R.string.lesson_ss01_opt1_fb), false),
                ScenarioOption(str(R.string.lesson_ss01_opt2_text), str(R.string.lesson_ss01_opt2_fb), true),
                ScenarioOption(str(R.string.lesson_ss01_opt3_text), str(R.string.lesson_ss01_opt3_fb), false)))),

        lessonLibrary[9].copy(title = str(R.string.lesson_mo01_title), description = str(R.string.lesson_mo01_desc),
            content = LessonContent.Gratitude(listOf(
                str(R.string.lesson_mo01_prompt1), str(R.string.lesson_mo01_prompt2), str(R.string.lesson_mo01_prompt3))))
    )
}