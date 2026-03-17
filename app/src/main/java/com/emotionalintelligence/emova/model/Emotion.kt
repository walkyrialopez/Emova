package com.emotionalintelligence.emova.model

import androidx.compose.ui.graphics.Color
import com.emotionalintelligence.emova.R

data class Emotion(
    val id: String,
    val name: String,           // English name — used for DB storage and navigation
    val emoji: String,
    val family: EmotionFamily,
    val description: String,    // English description — fallback
    val color: Color,
    val nameResId: Int,         // Localized display name
    val descResId: Int          // Localized description
)

val emotionList = listOf(

    // ── POSITIVE ──────────────────────────────────────────────────────────────
    Emotion("pos_1",  "Happy",       "😄", EmotionFamily.POSITIVE, "Feeling pleased and content.",                EmotionFamily.POSITIVE.color, R.string.emotion_pos_1_name,  R.string.emotion_pos_1_desc),
    Emotion("pos_2",  "Excited",     "🤩", EmotionFamily.POSITIVE, "Eagerly anticipating something.",             EmotionFamily.POSITIVE.color, R.string.emotion_pos_2_name,  R.string.emotion_pos_2_desc),
    Emotion("pos_3",  "Proud",       "🥲", EmotionFamily.POSITIVE, "Feeling deep pleasure or satisfaction.",      EmotionFamily.POSITIVE.color, R.string.emotion_pos_3_name,  R.string.emotion_pos_3_desc),
    Emotion("pos_4",  "Loving",      "🥰", EmotionFamily.POSITIVE, "Feeling warmth and affection.",               EmotionFamily.POSITIVE.color, R.string.emotion_pos_4_name,  R.string.emotion_pos_4_desc),
    Emotion("pos_5",  "Grateful",    "🙏", EmotionFamily.POSITIVE, "Feeling or showing thankfulness.",            EmotionFamily.POSITIVE.color, R.string.emotion_pos_5_name,  R.string.emotion_pos_5_desc),
    Emotion("pos_6",  "Secure",      "🤗", EmotionFamily.POSITIVE, "Feeling safe and supported.",                 EmotionFamily.POSITIVE.color, R.string.emotion_pos_6_name,  R.string.emotion_pos_6_desc),
    Emotion("pos_7",  "Hopeful",     "🌟", EmotionFamily.POSITIVE, "Feeling optimistic about the future.",        EmotionFamily.POSITIVE.color, R.string.emotion_pos_7_name,  R.string.emotion_pos_7_desc),
    Emotion("pos_8",  "Curious",     "🤔", EmotionFamily.POSITIVE, "Eager to know or learn something.",           EmotionFamily.POSITIVE.color, R.string.emotion_pos_8_name,  R.string.emotion_pos_8_desc),
    Emotion("pos_9",  "Eager",       "😁", EmotionFamily.POSITIVE, "Enthusiastically ready and looking forward.", EmotionFamily.POSITIVE.color, R.string.emotion_pos_9_name,  R.string.emotion_pos_9_desc),
    Emotion("pos_10", "Peaceful",    "😌", EmotionFamily.POSITIVE, "Free from disturbance; calm and at ease.",    EmotionFamily.POSITIVE.color, R.string.emotion_pos_10_name, R.string.emotion_pos_10_desc),
    Emotion("pos_11", "Inspired",    "✨", EmotionFamily.POSITIVE, "Feeling mentally stimulated and creative.",   EmotionFamily.POSITIVE.color, R.string.emotion_pos_11_name, R.string.emotion_pos_11_desc),
    Emotion("pos_12", "Playful",     "😜", EmotionFamily.POSITIVE, "Light-hearted and full of fun.",              EmotionFamily.POSITIVE.color, R.string.emotion_pos_12_name, R.string.emotion_pos_12_desc),
    Emotion("pos_13", "Confident",   "😎", EmotionFamily.POSITIVE, "Feeling assured in your own abilities.",      EmotionFamily.POSITIVE.color, R.string.emotion_pos_13_name, R.string.emotion_pos_13_desc),
    Emotion("pos_14", "Connected",   "🫂", EmotionFamily.POSITIVE, "Feeling close and in harmony with others.",   EmotionFamily.POSITIVE.color, R.string.emotion_pos_14_name, R.string.emotion_pos_14_desc),
    Emotion("pos_15", "Satisfied",   "😊", EmotionFamily.POSITIVE, "Content with how things have turned out.",    EmotionFamily.POSITIVE.color, R.string.emotion_pos_15_name, R.string.emotion_pos_15_desc),

    // ── DIFFICULT ─────────────────────────────────────────────────────────────
    Emotion("dif_1",  "Sad",          "😢", EmotionFamily.DIFFICULT, "Feeling or showing sorrow.",                EmotionFamily.DIFFICULT.color, R.string.emotion_dif_1_name,  R.string.emotion_dif_1_desc),
    Emotion("dif_2",  "Disappointed", "😞", EmotionFamily.DIFFICULT, "Feeling let down by someone or something.", EmotionFamily.DIFFICULT.color, R.string.emotion_dif_2_name,  R.string.emotion_dif_2_desc),
    Emotion("dif_3",  "Lonely",       "😔", EmotionFamily.DIFFICULT, "Sad from being disconnected.",              EmotionFamily.DIFFICULT.color, R.string.emotion_dif_3_name,  R.string.emotion_dif_3_desc),
    Emotion("dif_4",  "Scared",       "😨", EmotionFamily.DIFFICULT, "Afraid; filled with fear.",                 EmotionFamily.DIFFICULT.color, R.string.emotion_dif_4_name,  R.string.emotion_dif_4_desc),
    Emotion("dif_5",  "Anxious",      "😬", EmotionFamily.DIFFICULT, "Experiencing worry or unease.",             EmotionFamily.DIFFICULT.color, R.string.emotion_dif_5_name,  R.string.emotion_dif_5_desc),
    Emotion("dif_6",  "Insecure",     "😥", EmotionFamily.DIFFICULT, "Not confident or assured.",                 EmotionFamily.DIFFICULT.color, R.string.emotion_dif_6_name,  R.string.emotion_dif_6_desc),
    Emotion("dif_7",  "Overwhelmed",  "🫠", EmotionFamily.DIFFICULT, "Buried under too much at once.",            EmotionFamily.DIFFICULT.color, R.string.emotion_dif_7_name,  R.string.emotion_dif_7_desc),
    Emotion("dif_8",  "Hopeless",     "😶", EmotionFamily.DIFFICULT, "Feeling no hope or possibility ahead.",     EmotionFamily.DIFFICULT.color, R.string.emotion_dif_8_name,  R.string.emotion_dif_8_desc),
    Emotion("dif_9",  "Grief",        "💔", EmotionFamily.DIFFICULT, "Deep sorrow from loss.",                    EmotionFamily.DIFFICULT.color, R.string.emotion_dif_9_name,  R.string.emotion_dif_9_desc),
    Emotion("dif_10", "Ashamed",      "😳", EmotionFamily.DIFFICULT, "Feeling guilt or embarrassment about yourself.", EmotionFamily.DIFFICULT.color, R.string.emotion_dif_10_name, R.string.emotion_dif_10_desc),
    Emotion("dif_11", "Confused",     "😕", EmotionFamily.DIFFICULT, "Unable to think clearly; uncertain.",       EmotionFamily.DIFFICULT.color, R.string.emotion_dif_11_name, R.string.emotion_dif_11_desc),
    Emotion("dif_12", "Drained",      "😴", EmotionFamily.DIFFICULT, "Completely exhausted emotionally.",         EmotionFamily.DIFFICULT.color, R.string.emotion_dif_12_name, R.string.emotion_dif_12_desc),
    Emotion("dif_13", "Numb",         "😐", EmotionFamily.DIFFICULT, "Unable to feel emotions; emotionally flat.", EmotionFamily.DIFFICULT.color, R.string.emotion_dif_13_name, R.string.emotion_dif_13_desc),
    Emotion("dif_14", "Jealous",      "😒", EmotionFamily.DIFFICULT, "Envious of what someone else has.",         EmotionFamily.DIFFICULT.color, R.string.emotion_dif_14_name, R.string.emotion_dif_14_desc),
    Emotion("dif_15", "Guilty",       "😓", EmotionFamily.DIFFICULT, "Feeling responsible for a wrongdoing.",     EmotionFamily.DIFFICULT.color, R.string.emotion_dif_15_name, R.string.emotion_dif_15_desc),

    // ── ACTIVATED ─────────────────────────────────────────────────────────────
    Emotion("act_1",  "Angry",        "😠", EmotionFamily.ACTIVATED, "Feeling strong annoyance or displeasure.",  EmotionFamily.ACTIVATED.color, R.string.emotion_act_1_name,  R.string.emotion_act_1_desc),
    Emotion("act_2",  "Furious",      "🤬", EmotionFamily.ACTIVATED, "Extremely angry.",                          EmotionFamily.ACTIVATED.color, R.string.emotion_act_2_name,  R.string.emotion_act_2_desc),
    Emotion("act_3",  "Resentful",    "😾", EmotionFamily.ACTIVATED, "Bitterness from a wrong not resolved.",     EmotionFamily.ACTIVATED.color, R.string.emotion_act_3_name,  R.string.emotion_act_3_desc),
    Emotion("act_4",  "Frustrated",   "😤", EmotionFamily.ACTIVATED, "Upset at being blocked or unable to change something.", EmotionFamily.ACTIVATED.color, R.string.emotion_act_4_name,  R.string.emotion_act_4_desc),
    Emotion("act_5",  "Irritated",    "😒", EmotionFamily.ACTIVATED, "Showing slight anger; annoyed.",            EmotionFamily.ACTIVATED.color, R.string.emotion_act_5_name,  R.string.emotion_act_5_desc),
    Emotion("act_6",  "Impatient",    "🙄", EmotionFamily.ACTIVATED, "Restless from having to wait.",             EmotionFamily.ACTIVATED.color, R.string.emotion_act_6_name,  R.string.emotion_act_6_desc),
    Emotion("act_7",  "Disgusted",    "🤢", EmotionFamily.ACTIVATED, "Feeling revulsion or disapproval.",         EmotionFamily.ACTIVATED.color, R.string.emotion_act_7_name,  R.string.emotion_act_7_desc),
    Emotion("act_8",  "Repulsed",     "🤮", EmotionFamily.ACTIVATED, "Intense aversion to something.",            EmotionFamily.ACTIVATED.color, R.string.emotion_act_8_name,  R.string.emotion_act_8_desc),
    Emotion("act_9",  "Contempt",     "😑", EmotionFamily.ACTIVATED, "Feeling something is beneath consideration.", EmotionFamily.ACTIVATED.color, R.string.emotion_act_9_name,  R.string.emotion_act_9_desc),
    Emotion("act_10", "Envious",      "😏", EmotionFamily.ACTIVATED, "Wanting what someone else has.",            EmotionFamily.ACTIVATED.color, R.string.emotion_act_10_name, R.string.emotion_act_10_desc),
    Emotion("act_11", "Tense",        "😰", EmotionFamily.ACTIVATED, "Mentally or emotionally strained.",         EmotionFamily.ACTIVATED.color, R.string.emotion_act_11_name, R.string.emotion_act_11_desc),
    Emotion("act_12", "Restless",     "😵", EmotionFamily.ACTIVATED, "Unable to stay still; agitated.",           EmotionFamily.ACTIVATED.color, R.string.emotion_act_12_name, R.string.emotion_act_12_desc),
    Emotion("act_13", "Defensive",    "🛡️", EmotionFamily.ACTIVATED, "Protecting yourself from criticism.",       EmotionFamily.ACTIVATED.color, R.string.emotion_act_13_name, R.string.emotion_act_13_desc),
    Emotion("act_14", "Jealous",      "😤", EmotionFamily.ACTIVATED, "Fiercely protective of what is yours.",     EmotionFamily.ACTIVATED.color, R.string.emotion_act_14_name, R.string.emotion_act_14_desc),
    Emotion("act_15", "Humiliated",   "😞", EmotionFamily.ACTIVATED, "Made to feel ashamed or foolish by others.", EmotionFamily.ACTIVATED.color, R.string.emotion_act_15_name, R.string.emotion_act_15_desc)
)