package com.emotionalintelligence.emova.model

import android.content.Context
import com.emotionalintelligence.emova.R

data class EmotionWord(
    val word: String,
    val family: EmotionFamily,
    val definition: String,
    val intensity: Int,
    val example: String,
    val relatedWords: List<String>,
    val practicalTip: String = ""
)

// ─── English baseline (used for search indexing and DB) ──────────────────────

val vocabularyList = listOf(

    // ── POSITIVE ──────────────────────────────────────────────────────────────
    EmotionWord("Elation", EmotionFamily.POSITIVE,
        "An intense feeling of happiness and excitement.", 3,
        "Feeling elation when you get unexpected great news.",
        listOf("Euphoria", "Ecstasy", "Jubilation")),
    EmotionWord("Contentment", EmotionFamily.POSITIVE,
        "A quiet sense of satisfaction and ease.", 1,
        "Feeling contentment after a peaceful Sunday at home.",
        listOf("Serenity", "Peace", "Satisfaction")),
    EmotionWord("Delight", EmotionFamily.POSITIVE,
        "A strong feeling of pleasure or joy at something charming.", 2,
        "The delight of watching a child laugh uncontrollably.",
        listOf("Joy", "Pleasure", "Enjoyment")),
    EmotionWord("Euphoria", EmotionFamily.POSITIVE,
        "An overwhelming feeling of happiness, almost beyond reality.", 3,
        "Euphoria after finishing a marathon you trained months for.",
        listOf("Elation", "Bliss", "Ecstasy")),
    EmotionWord("Gratitude", EmotionFamily.POSITIVE,
        "A warm appreciation for kindness received.", 2,
        "Gratitude toward a friend who showed up when you needed them.",
        listOf("Thankfulness", "Appreciation")),
    EmotionWord("Serenity", EmotionFamily.POSITIVE,
        "A state of calm, peacefulness and untroubled ease.", 1,
        "The serenity of sitting by a lake with no agenda.",
        listOf("Tranquility", "Contentment", "Peace")),
    EmotionWord("Admiration", EmotionFamily.POSITIVE,
        "Respect and warm approval for someone's qualities.", 2,
        "Admiration for a mentor who overcame great difficulty.",
        listOf("Respect", "Esteem", "Reverence")),
    EmotionWord("Security", EmotionFamily.POSITIVE,
        "Feeling safe, stable and free from threat.", 1,
        "The security of knowing someone always has your back.",
        listOf("Safety", "Trust", "Comfort")),
    EmotionWord("Hope", EmotionFamily.POSITIVE,
        "A feeling that something desired is possible and likely.", 2,
        "Hope when early signs suggest a difficult situation is turning around.",
        listOf("Optimism", "Expectation", "Faith")),
    EmotionWord("Eagerness", EmotionFamily.POSITIVE,
        "Enthusiastic readiness and desire to do something.", 2,
        "Eagerness before starting a project you've been excited about.",
        listOf("Enthusiasm", "Zeal", "Keenness")),
    EmotionWord("Curiosity", EmotionFamily.POSITIVE,
        "A strong desire to know or learn something.", 1,
        "Curiosity sparked by a question you've never thought about before.",
        listOf("Interest", "Wonder", "Inquisitiveness")),
    EmotionWord("Optimism", EmotionFamily.POSITIVE,
        "A tendency to expect the best possible outcome.", 1,
        "Optimism that tomorrow will bring a fresh start.",
        listOf("Hope", "Confidence", "Positivity")),

    // ── DIFFICULT ─────────────────────────────────────────────────────────────
    EmotionWord("Grief", EmotionFamily.DIFFICULT,
        "Deep sorrow from loss — of a person, relationship or chapter of life.", 3,
        "Grief after the death of a longtime friend.",
        listOf("Mourning", "Sorrow", "Bereavement"),
        practicalTip = "Allow grief its time — don't rush it. Writing a letter to what you've lost externalises the feeling and gently begins healing."),
    EmotionWord("Melancholy", EmotionFamily.DIFFICULT,
        "A pensive, lingering sadness without a clear cause.", 2,
        "Melancholy on a grey Sunday afternoon with no particular reason.",
        listOf("Wistfulness", "Pensiveness", "Sadness"),
        practicalTip = "Gentle movement and soft music can ease melancholy without forcing you to 'cheer up'. Let yourself feel it while doing something kind for your body."),
    EmotionWord("Wistfulness", EmotionFamily.DIFFICULT,
        "A gentle longing for something past or out of reach.", 1,
        "Wistfulness looking at old photos from a happier time.",
        listOf("Nostalgia", "Longing", "Yearning"),
        practicalTip = "Wistfulness is a soft emotion — honour it. Write down one good memory it brings up. Gratitude for what was is a form of joy."),
    EmotionWord("Loneliness", EmotionFamily.DIFFICULT,
        "A painful awareness of being disconnected from others.", 2,
        "Loneliness in a crowded room where nobody really knows you.",
        listOf("Isolation", "Solitude", "Alienation"),
        practicalTip = "Send one short message to someone you care about. Connection doesn't require a long conversation — a simple 'thinking of you' is enough."),
    EmotionWord("Despair", EmotionFamily.DIFFICULT,
        "A complete loss of hope about a situation.", 3,
        "Despair when efforts repeatedly fail and no path forward is visible.",
        listOf("Hopelessness", "Grief", "Anguish"),
        practicalTip = "Find one tiny thing within your control today — even making your bed. Small acts of agency rebuild hope one step at a time."),
    EmotionWord("Disappointment", EmotionFamily.DIFFICULT,
        "Sadness from unmet expectations or hopes.", 2,
        "Disappointment when a plan you worked hard on falls through.",
        listOf("Letdown", "Regret", "Disillusionment"),
        practicalTip = "Name what you hoped for. Acknowledging the expectation out loud or in writing helps release the disappointment attached to it."),
    EmotionWord("Dread", EmotionFamily.DIFFICULT,
        "Anticipatory fear of something specific and inevitable.", 3,
        "The dread before a difficult conversation you can't avoid.",
        listOf("Terror", "Apprehension", "Foreboding"),
        practicalTip = "Break the dreaded thing into the smallest possible first step and do only that. The anticipation is almost always worse than the reality."),
    EmotionWord("Apprehension", EmotionFamily.DIFFICULT,
        "Mild anxiety about something uncertain in the future.", 1,
        "Apprehension before starting a new job.",
        listOf("Unease", "Worry", "Nervousness"),
        practicalTip = "Ask yourself: what's the realistic worst case? Then ask what you'd do if that happened. Having a plan in mind reduces fear significantly."),
    EmotionWord("Vulnerability", EmotionFamily.DIFFICULT,
        "Exposure to emotional risk or harm without certainty of safety.", 2,
        "Vulnerability when sharing something deeply personal.",
        listOf("Exposure", "Openness", "Fragility"),
        practicalTip = "Vulnerability is courage, not weakness. Share what you're feeling with one trusted person — being witnessed and accepted heals."),

    // ── ACTIVATED ─────────────────────────────────────────────────────────────
    EmotionWord("Indignation", EmotionFamily.ACTIVATED,
        "Anger aroused by something perceived as unjust or unworthy.", 2,
        "Indignation at being blamed for something you didn't do.",
        listOf("Outrage", "Resentment", "Anger"),
        practicalTip = "Write down what you believe was unfair before saying it aloud. Clear thinking leads to more effective action than reactive words."),
    EmotionWord("Resentment", EmotionFamily.ACTIVATED,
        "Bitterness from a wrong or slight that hasn't been resolved.", 2,
        "Resentment that builds when effort goes unrecognized over time.",
        listOf("Bitterness", "Grudge", "Indignation"),
        practicalTip = "Resentment harms you more than the other person. Ask: what do I need to let this go? A conversation, an apology, or simply a decision to release it."),
    EmotionWord("Outrage", EmotionFamily.ACTIVATED,
        "Anger at a great injustice or moral violation.", 3,
        "Outrage at news of abuse of power or corruption.",
        listOf("Fury", "Indignation", "Rage"),
        practicalTip = "Take 10 minutes before responding to anything. Outrage narrows thinking — space lets you act with intention rather than react with emotion."),
    EmotionWord("Exasperation", EmotionFamily.ACTIVATED,
        "Intense irritation due to repeated obstacles or failures.", 3,
        "Exasperation when the same mistake happens for the fifth time.",
        listOf("Frustration", "Vexation", "Annoyance"),
        practicalTip = "Step away for 5 minutes. A short break resets your patience and gives you perspective on what truly matters versus what just feels urgent."),
    EmotionWord("Contempt", EmotionFamily.ACTIVATED,
        "A feeling that someone is beneath consideration or worthless.", 3,
        "Contempt for hypocrisy in people who preach one thing and do another.",
        listOf("Disdain", "Scorn", "Disrespect"),
        practicalTip = "Contempt creates distance. Ask whether there's a hurt or unmet need underneath it — addressing that is more useful than the contempt itself."),
    EmotionWord("Revulsion", EmotionFamily.ACTIVATED,
        "A strong feeling of disgust and repulsion.", 3,
        "Revulsion at witnessing cruelty to someone vulnerable.",
        listOf("Disgust", "Loathing", "Repulsion"),
        practicalTip = "Revulsion often signals a values violation. Use it as information: what boundary was crossed, and what do you need to feel safe again?"),
    EmotionWord("Impatience", EmotionFamily.ACTIVATED,
        "Restlessness from having to wait or tolerate delay.", 2,
        "Impatience in a slow-moving queue when you're running late.",
        listOf("Irritation", "Restlessness"),
        practicalTip = "Ask: will this delay matter in one year? If not, use this moment to practise presence. Three slow breaths can transform waiting into rest."),
    EmotionWord("Bitterness", EmotionFamily.ACTIVATED,
        "Anger mixed with sadness and a sense of betrayal.", 2,
        "Bitterness after a friendship ends in unexpected betrayal.",
        listOf("Resentment", "Cynicism", "Hurt"),
        practicalTip = "Bitterness is old pain with nowhere to go. Journalling about the original hurt — not the person — can begin to dissolve it over time.")
)

// ─── Localized vocabulary list — call from VocabularyScreen ──────────────────

fun localizedVocabularyList(context: Context): List<EmotionWord> {
    val words    = context.resources.getStringArray(R.array.vocab_words)
    val defs     = context.resources.getStringArray(R.array.vocab_defs)
    val examples = context.resources.getStringArray(R.array.vocab_examples)
    val tips     = context.resources.getStringArray(R.array.vocab_tips)

    val relatedArrayIds = listOf(
        R.array.vocab_related_00, R.array.vocab_related_01, R.array.vocab_related_02,
        R.array.vocab_related_03, R.array.vocab_related_04, R.array.vocab_related_05,
        R.array.vocab_related_06, R.array.vocab_related_07, R.array.vocab_related_08,
        R.array.vocab_related_09, R.array.vocab_related_10, R.array.vocab_related_11,
        R.array.vocab_related_12, R.array.vocab_related_13, R.array.vocab_related_14,
        R.array.vocab_related_15, R.array.vocab_related_16, R.array.vocab_related_17,
        R.array.vocab_related_18, R.array.vocab_related_19, R.array.vocab_related_20,
        R.array.vocab_related_21, R.array.vocab_related_22, R.array.vocab_related_23,
        R.array.vocab_related_24, R.array.vocab_related_25, R.array.vocab_related_26,
        R.array.vocab_related_27, R.array.vocab_related_28
    )

    return vocabularyList.mapIndexed { i, word ->
        word.copy(
            word         = words.getOrElse(i) { word.word },
            definition   = defs.getOrElse(i) { word.definition },
            example      = examples.getOrElse(i) { word.example },
            practicalTip = tips.getOrElse(i) { word.practicalTip },
            relatedWords = context.resources.getStringArray(relatedArrayIds[i]).toList()
        )
    }
}