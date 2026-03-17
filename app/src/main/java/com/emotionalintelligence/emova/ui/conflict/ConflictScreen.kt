package com.emotionalintelligence.emova.ui.conflict

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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.viewmodel.ConflictViewModel

// ─── Step definitions ─────────────────────────────────────────────────────────

data class ConflictStep(
    val number: Int,
    val emoji: String,
    val titleResId: Int,
    val questionResId: Int,
    val hintResId: Int,
    val accentColor: Color
)

val conflictSteps = listOf(
    ConflictStep(1, "📋", 0, 0, 0, Color(0xFF3A86FF)),
    ConflictStep(2, "💢", 1, 1, 1, Color(0xFFEF233C)),
    ConflictStep(3, "🫂", 2, 2, 2, Color(0xFF06D6A0)),
    ConflictStep(4, "🔍", 3, 3, 3, Color(0xFFFFD166)),
    ConflictStep(5, "🌱", 4, 4, 4, Color(0xFF8338EC))
)

// ─── Entry Screen ─────────────────────────────────────────────────────────────

@Composable
fun ConflictScreen(viewModel: ConflictViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isComplete -> ConflictSummaryScreen(
            answers = uiState.answers,
            onRestart = viewModel::restart
        )
        uiState.started -> ConflictStepScreen(
            viewModel = viewModel,
            onBack = viewModel::restart
        )
        else -> ConflictIntroScreen(onStart = viewModel::start)
    }
}

// ─── Intro Screen ─────────────────────────────────────────────────────────────

@Composable
fun ConflictIntroScreen(onStart: () -> Unit) {
    val titles = stringArrayResource(R.array.conflict_step_titles)
    val hints = stringArrayResource(R.array.conflict_step_hints)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F6FB))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🤝", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            stringResource(R.string.conflict_title),
            fontSize = 26.sp, fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E), textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.conflict_subtitle),
            fontSize = 15.sp, color = Color(0xFF6B6B8A),
            textAlign = TextAlign.Center, lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(36.dp))

        conflictSteps.forEach { step ->
            val title = titles.getOrElse(step.number - 1) { "" }
            val hint = hints.getOrElse(step.number - 1) { "" }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(step.accentColor.copy(alpha = 0.15f))
                        .border(1.dp, step.accentColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text(step.emoji, fontSize = 16.sp) }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        stringResource(R.string.conflict_step_label, step.number, title),
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E)
                    )
                    Text(hint.take(55) + "…", fontSize = 12.sp, color = Color(0xFF6B6B8A))
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
        ) {
            Text(stringResource(R.string.conflict_begin), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(stringResource(R.string.conflict_duration), fontSize = 13.sp, color = Color(0xFFAAAAAC))
    }
}

// ─── Step Screen ─────────────────────────────────────────────────────────────

@Composable
fun ConflictStepScreen(viewModel: ConflictViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val step = conflictSteps.getOrNull(uiState.currentStep) ?: return
    val titles = stringArrayResource(R.array.conflict_step_titles)
    val questions = stringArrayResource(R.array.conflict_step_questions)
    val hints = stringArrayResource(R.array.conflict_step_hints)

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB))) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.conflict_back), tint = Color(0xFF6B6B8A))
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                stringResource(R.string.conflict_step_of, uiState.currentStep + 1, conflictSteps.size),
                fontSize = 14.sp, color = Color(0xFF6B6B8A)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }

        Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color(0xFFEAE8F5))) {
            val progress by animateFloatAsState(
                targetValue = (uiState.currentStep + 1).toFloat() / conflictSteps.size,
                animationSpec = tween(400), label = "progress"
            )
            Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(step.accentColor))
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                },
                label = "stepContent"
            ) { stepIndex ->
                val s = conflictSteps.getOrNull(stepIndex) ?: return@AnimatedContent
                val title = titles.getOrElse(stepIndex) { "" }
                val question = questions.getOrElse(stepIndex) { "" }
                val hint = hints.getOrElse(stepIndex) { "" }
                Column {
                    Box(
                        modifier = Modifier.size(60.dp).clip(CircleShape)
                            .background(s.accentColor.copy(alpha = 0.15f))
                            .border(1.5.dp, s.accentColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text(s.emoji, fontSize = 26.sp) }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFFFFF))
                            .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) { Text(question, fontSize = 15.sp, color = Color(0xFF1A1A2E), lineHeight = 22.sp) }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Text("💡", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(hint, fontSize = 12.sp, color = Color(0xFF6B6B8A), lineHeight = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.currentAnswer,
                onValueChange = viewModel::setAnswer,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                placeholder = { Text(stringResource(R.string.conflict_write_thoughts), color = Color(0xFFAAAAAC), fontSize = 14.sp) },
                textStyle = LocalTextStyle.current.copy(color = Color(0xFF1A1A2E), fontSize = 15.sp, lineHeight = 22.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = step.accentColor,
                    unfocusedBorderColor = Color(0xFFEAE8F5),
                    cursorColor = step.accentColor,
                    focusedContainerColor = Color(0xFFFFFFFF),
                    unfocusedContainerColor = Color(0xFFFFFFFF)
                ),
                shape = RoundedCornerShape(16.dp), maxLines = 8
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (uiState.currentStep > 0) {
                    OutlinedButton(
                        onClick = viewModel::prevStep,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B6B8A)),
                        border = BorderStroke(1.dp, Color(0xFFEAE8F5))
                    ) { Text(stringResource(R.string.conflict_back), fontSize = 15.sp) }
                }
                Button(
                    onClick = viewModel::nextStep,
                    modifier = Modifier.weight(if (uiState.currentStep > 0) 2f else 1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = step.accentColor)
                ) {
                    Text(
                        if (uiState.currentStep == conflictSteps.size - 1)
                            stringResource(R.string.conflict_complete)
                        else stringResource(R.string.conflict_next),
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = viewModel::skipStep, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.conflict_skip_step), fontSize = 13.sp, color = Color(0xFFAAAAAC))
            }
        }
    }
}

// ─── Summary Screen ───────────────────────────────────────────────────────────

@Composable
fun ConflictSummaryScreen(answers: List<String>, onRestart: () -> Unit) {
    val titles = stringArrayResource(R.array.conflict_step_titles)

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🌱", fontSize = 52.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.conflict_done_title), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.conflict_done_body),
            fontSize = 14.sp, color = Color(0xFF6B6B8A),
            textAlign = TextAlign.Center, lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        conflictSteps.forEachIndexed { index, step ->
            val answer = answers.getOrElse(index) { "" }
            val title = titles.getOrElse(index) { "" }
            if (answer.isNotBlank()) {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFFFFF))
                        .border(1.dp, step.accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(step.emoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = step.accentColor)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(answer, fontSize = 14.sp, color = Color(0xFF1A1A2E), lineHeight = 20.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
        ) {
            Text(stringResource(R.string.conflict_restart), fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}