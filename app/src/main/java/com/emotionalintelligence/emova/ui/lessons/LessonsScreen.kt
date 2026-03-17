package com.emotionalintelligence.emova.ui.lessons

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.model.*
import com.emotionalintelligence.emova.viewmodel.LessonViewModel

// ─── Lessons List Screen ──────────────────────────────────────────────────────

@Composable
fun LessonsScreen(onBack: (() -> Unit)? = null, viewModel: LessonViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDetail by remember { mutableStateOf(false) }

    if (showDetail && uiState.selectedLesson != null) {
        LessonDetailScreen(viewModel = viewModel, onBack = { showDetail = false })
    } else {
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp)
        ) {
            Text(stringResource(R.string.lessons_title), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Text(stringResource(R.string.lessons_subtitle), fontSize = 14.sp, color = Color(0xFF6B6B8A))

            Spacer(modifier = Modifier.height(24.dp))

            uiState.todaysLesson?.let { lesson ->
                Text(stringResource(R.string.lessons_today_label), fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = Color(0xFF6C63FF), letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(8.dp))
                TodayLessonCard(lesson = lesson, onClick = {
                    viewModel.selectLesson(lesson)
                    showDetail = true
                })
            }

            Spacer(modifier = Modifier.height(28.dp))

            LessonCategory.values().forEach { category ->
                val categoryLessons = uiState.allLessons.filter { it.category == category }
                if (categoryLessons.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(category.emoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(category.labelResId), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B6B8A))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    categoryLessons.forEach { lesson ->
                        LessonCard(lesson = lesson, onClick = {
                            viewModel.selectLesson(lesson)
                            showDetail = true
                        })
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ─── Today Lesson Card ────────────────────────────────────────────────────────

@Composable
fun TodayLessonCard(lesson: Lesson, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF6C63FF).copy(alpha = 0.15f))
            .border(1.dp, Color(0xFF6C63FF).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick).padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(lesson.category.emoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(lesson.category.labelResId), fontSize = 12.sp, color = Color(0xFF6C63FF), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.weight(1f))
                Text("⏱ ${lesson.duration}", fontSize = 12.sp, color = Color(0xFF6B6B8A))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(lesson.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Spacer(modifier = Modifier.height(4.dp))
            Text(lesson.description, fontSize = 13.sp, color = Color(0xFF6B6B8A))
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
            ) {
                Text(stringResource(R.string.lessons_start_button), fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ─── Lesson Card ──────────────────────────────────────────────────────────────

@Composable
fun LessonCard(lesson: Lesson, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFFFF))
            .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEAE8F5)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (lesson.content) {
                    is LessonContent.Reflection -> "💭"
                    is LessonContent.Scenario   -> "🎭"
                    is LessonContent.Breathing  -> "🌬️"
                    is LessonContent.Gratitude  -> "🙏"
                },
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(lesson.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E))
            Text(lesson.description, fontSize = 12.sp, color = Color(0xFF6B6B8A), maxLines = 1)
        }
        Text("⏱ ${lesson.duration}", fontSize = 11.sp, color = Color(0xFFAAAAAC))
    }
}

// ─── Lesson Detail Screen ─────────────────────────────────────────────────────

@Composable
fun LessonDetailScreen(viewModel: LessonViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lesson = uiState.selectedLesson ?: return

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB))) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF6B6B8A))
            }
            Text(stringResource(lesson.category.labelResId), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text(lesson.category.emoji, fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(lesson.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Spacer(modifier = Modifier.height(4.dp))
            Text(lesson.description, fontSize = 14.sp, color = Color(0xFF6B6B8A))
            Spacer(modifier = Modifier.height(28.dp))

            when (val content = lesson.content) {
                is LessonContent.Reflection -> ReflectionContent(
                    content = content,
                    answer = uiState.reflectionAnswer,
                    saved = uiState.reflectionSaved,
                    onAnswerChange = viewModel::setReflectionAnswer,
                    onSave = viewModel::saveReflection
                )
                is LessonContent.Scenario -> ScenarioContent(
                    content = content,
                    selectedIndex = uiState.selectedOptionIndex,
                    showFeedback = uiState.showFeedback,
                    onOptionSelected = viewModel::selectOption
                )
                is LessonContent.Breathing -> BreathingContent(
                    content = content,
                    step = uiState.breathingStep,
                    secondsLeft = uiState.breathingSecondsLeft,
                    active = uiState.breathingActive,
                    onStart = viewModel::startBreathing
                )
                is LessonContent.Gratitude -> GratitudeContent(
                    content = content,
                    answers = uiState.gratitudeAnswers,
                    currentIndex = uiState.currentGratitudeIndex,
                    done = uiState.gratitudeDone,
                    onAnswerChange = viewModel::setGratitudeAnswer,
                    onNext = viewModel::nextGratitude
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ─── Reflection Content ───────────────────────────────────────────────────────

@Composable
fun ReflectionContent(
    content: LessonContent.Reflection,
    answer: String,
    saved: Boolean,
    onAnswerChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFFFF))
            .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(16.dp)).padding(20.dp)
    ) { Text(content.prompt, fontSize = 16.sp, color = Color(0xFF1A1A2E), lineHeight = 24.sp) }
    Spacer(modifier = Modifier.height(16.dp))

    content.hints.forEach { hint ->
        Row(modifier = Modifier.padding(vertical = 4.dp)) {
            Text("•", color = Color(0xFF6C63FF), fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(hint, fontSize = 13.sp, color = Color(0xFF6B6B8A))
        }
    }
    Spacer(modifier = Modifier.height(20.dp))

    AnimatedVisibility(visible = !saved) {
        Column {
            OutlinedTextField(
                value = answer, onValueChange = onAnswerChange,
                modifier = Modifier.fillMaxWidth().height(160.dp),
                placeholder = { Text(stringResource(R.string.lessons_write_reflection), color = Color(0xFFAAAAAC)) },
                textStyle = LocalTextStyle.current.copy(color = Color(0xFF1A1A2E), fontSize = 14.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6C63FF), unfocusedBorderColor = Color(0xFFEAE8F5),
                    cursorColor = Color(0xFF6C63FF), focusedContainerColor = Color(0xFFFFFFFF), unfocusedContainerColor = Color(0xFFFFFFFF)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSave, enabled = answer.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
            ) { Text(stringResource(R.string.lessons_save_reflection), fontWeight = FontWeight.Bold, color = Color.White) }
        }
    }

    AnimatedVisibility(visible = saved, enter = fadeIn() + scaleIn()) {
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF06D6A0).copy(alpha = 0.15f))
                .border(1.dp, Color(0xFF06D6A0).copy(alpha = 0.4f), RoundedCornerShape(16.dp)).padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✅", fontSize = 32.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.lessons_reflection_saved), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                Text(stringResource(R.string.lessons_great_work), fontSize = 13.sp, color = Color(0xFF6B6B8A))
            }
        }
    }
}

// ─── Scenario Content ─────────────────────────────────────────────────────────

@Composable
fun ScenarioContent(
    content: LessonContent.Scenario,
    selectedIndex: Int,
    showFeedback: Boolean,
    onOptionSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFFFF))
            .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(16.dp)).padding(20.dp)
    ) { Text(content.situation, fontSize = 15.sp, color = Color(0xFF1A1A2E), lineHeight = 22.sp) }
    Spacer(modifier = Modifier.height(20.dp))
    Text(stringResource(R.string.lessons_how_respond), fontSize = 14.sp, color = Color(0xFF6B6B8A))
    Spacer(modifier = Modifier.height(12.dp))

    content.options.forEachIndexed { index, option ->
        val isSelected = selectedIndex == index
        val borderColor = when {
            !showFeedback -> if (isSelected) Color(0xFF6C63FF) else Color(0xFFEAE8F5)
            option.isIdeal -> Color(0xFF06D6A0)
            isSelected -> Color(0xFFEF233C)
            else -> Color(0xFFEAE8F5)
        }
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(if (isSelected) borderColor.copy(alpha = 0.1f) else Color(0xFFFFFFFF))
                .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                .clickable(enabled = !showFeedback) { onOptionSelected(index) }.padding(16.dp)
        ) { Text(option.text, fontSize = 14.sp, color = Color(0xFF1A1A2E)) }
        Spacer(modifier = Modifier.height(10.dp))
    }

    AnimatedVisibility(visible = showFeedback && selectedIndex >= 0) {
        val selectedOption = content.options.getOrNull(selectedIndex)
        selectedOption?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(if (it.isIdeal) Color(0xFF06D6A0).copy(0.1f) else Color(0xFFEF233C).copy(0.1f))
                    .border(1.dp, if (it.isIdeal) Color(0xFF06D6A0).copy(0.4f) else Color(0xFFEF233C).copy(0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        if (it.isIdeal) stringResource(R.string.lessons_great_choice) else stringResource(R.string.lessons_consider),
                        fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = if (it.isIdeal) Color(0xFF06D6A0) else Color(0xFFFFD166)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(it.feedback, fontSize = 13.sp, color = Color(0xFF1A1A2E), lineHeight = 20.sp)
                }
            }
        }
    }
}

// ─── Breathing Content ────────────────────────────────────────────────────────

@Composable
fun BreathingContent(
    content: LessonContent.Breathing,
    step: Int,
    secondsLeft: Int,
    active: Boolean,
    onStart: () -> Unit
) {
    val currentStep = content.steps.getOrNull(step)
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
        label = "scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(180.dp).clip(CircleShape)
                .background(Color(0xFF6C63FF).copy(alpha = if (active) 0.2f else 0.1f))
                .border(2.dp, Color(0xFF6C63FF).copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (active && currentStep != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(currentStep.emoji, fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(currentStep.instruction, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("$secondsLeft", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6C63FF))
                }
            } else { Text("🌬️", fontSize = 48.sp) }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (!active) {
            Text(stringResource(R.string.lessons_breathing_rounds, content.steps.size), fontSize = 13.sp, color = Color(0xFF6B6B8A))
            Spacer(modifier = Modifier.height(16.dp))
            content.steps.forEach { s ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(s.emoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${s.instruction} — ${s.durationSeconds}s", fontSize = 14.sp, color = Color(0xFF1A1A2E))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
            ) { Text(stringResource(R.string.lessons_start_breathing), fontWeight = FontWeight.Bold, color = Color.White) }
        } else {
            Text(stringResource(R.string.lessons_breathe_circle), fontSize = 14.sp, color = Color(0xFF6B6B8A))
        }
    }
}

// ─── Gratitude Content ────────────────────────────────────────────────────────

@Composable
fun GratitudeContent(
    content: LessonContent.Gratitude,
    answers: List<String>,
    currentIndex: Int,
    done: Boolean,
    onAnswerChange: (String) -> Unit,
    onNext: () -> Unit
) {
    if (done) {
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFD166).copy(alpha = 0.12f))
                .border(1.dp, Color(0xFFFFD166).copy(alpha = 0.4f), RoundedCornerShape(16.dp)).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🙏", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.lessons_gratitude_done_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                Text(stringResource(R.string.lessons_gratitude_done_body), fontSize = 13.sp, color = Color(0xFF6B6B8A))
            }
        }
        return
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        content.prompts.indices.forEach { i ->
            Box(modifier = Modifier.size(8.dp).clip(CircleShape)
                .background(if (i <= currentIndex) Color(0xFFFFD166) else Color(0xFFEAE8F5)))
        }
    }
    Spacer(modifier = Modifier.height(20.dp))

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFFFF))
            .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(16.dp)).padding(20.dp)
    ) { Text(content.prompts[currentIndex], fontSize = 16.sp, color = Color(0xFF1A1A2E), lineHeight = 24.sp) }
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = answers.getOrElse(currentIndex) { "" }, onValueChange = onAnswerChange,
        modifier = Modifier.fillMaxWidth().height(130.dp),
        placeholder = { Text(stringResource(R.string.lessons_write_answer), color = Color(0xFFAAAAAC)) },
        textStyle = LocalTextStyle.current.copy(color = Color(0xFF1A1A2E), fontSize = 14.sp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFFFD166), unfocusedBorderColor = Color(0xFFEAE8F5),
            cursorColor = Color(0xFFFFD166), focusedContainerColor = Color(0xFFFFFFFF), unfocusedContainerColor = Color(0xFFFFFFFF)
        ),
        shape = RoundedCornerShape(12.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = onNext, modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166))
    ) {
        Text(
            if (currentIndex == content.prompts.size - 1) stringResource(R.string.lessons_finish) else stringResource(R.string.lessons_next),
            fontWeight = FontWeight.Bold, color = Color(0xFFF7F6FB)
        )
    }
}