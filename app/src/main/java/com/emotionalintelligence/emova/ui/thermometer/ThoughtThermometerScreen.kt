package com.emotionalintelligence.emova.ui.thermometer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.model.emotionList

data class ThermometerStepRes(
    val number: Int,
    val emoji: String,
    val titleResId: Int,
    val questionResId: Int,
    val hintResId: Int,
    val color: Color
)

val thermometerStepResources = listOf(
    ThermometerStepRes(1, "🔍", R.string.thermo_step1_title, R.string.thermo_step1_question, R.string.thermo_step1_hint, Color(0xFF4CC9F0)),
    ThermometerStepRes(2, "❓", R.string.thermo_step2_title, R.string.thermo_step2_question, R.string.thermo_step2_hint, Color(0xFF6C63FF)),
    ThermometerStepRes(3, "💚", R.string.thermo_step3_title, R.string.thermo_step3_question, R.string.thermo_step3_hint, Color(0xFF06D6A0))
)

@Composable
fun ThoughtThermometerScreen(
    emotionName: String,
    onFinish: () -> Unit,
    onSkip: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateListOf("", "", "") }

    // Resolve localized emotion name
    val emotion = remember(emotionName) { emotionList.firstOrNull { it.name == emotionName } }
    val displayName = emotion?.let { stringResource(it.nameResId) } ?: emotionName

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F6FB))
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        stringResource(R.string.thermo_label),
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6C63FF)
                    )
                    Text(
                        stringResource(R.string.thermo_you_felt, displayName),
                        fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)
                    )
                }
                TextButton(onClick = onSkip) {
                    Text(stringResource(R.string.thermo_skip), color = Color(0xFF6B6B8A), fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                thermometerStepResources.forEachIndexed { index, step ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    index < currentStep -> Color(0xFF06D6A0)
                                    index == currentStep -> step.color
                                    else -> Color(0xFFEAE8F5)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (index < currentStep) "✓" else step.emoji,
                            fontSize = if (index < currentStep) 14.sp else 16.sp,
                            color = if (index <= currentStep) Color.White else Color(0xFF6B6B8A)
                        )
                    }
                    if (index < thermometerStepResources.size - 1) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .align(Alignment.CenterVertically)
                                .background(
                                    if (index < currentStep) Color(0xFF06D6A0) else Color(0xFFEAE8F5)
                                )
                        )
                    }
                }
            }
        }

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                (slideInHorizontally { it } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it } + fadeOut())
            },
            label = "step",
            modifier = Modifier.weight(1f)
        ) { step ->
            if (step < thermometerStepResources.size) {
                ThermometerStepCard(
                    step = thermometerStepResources[step],
                    answer = answers[step],
                    onAnswerChange = { answers[step] = it },
                    isLast = step == thermometerStepResources.size - 1,
                    onNext = {
                        if (step < thermometerStepResources.size - 1) currentStep++
                        else onFinish()
                    }
                )
            } else {
                ThermometerDoneScreen(onFinish = onFinish)
            }
        }
    }
}

@Composable
fun ThermometerStepCard(
    step: ThermometerStepRes,
    answer: String,
    onAnswerChange: (String) -> Unit,
    isLast: Boolean,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                        .background(step.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text(step.emoji, fontSize = 22.sp) }
                Column {
                    Text(
                        stringResource(R.string.thermo_step_of, step.number),
                        fontSize = 12.sp, color = step.color, fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(step.titleResId),
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(step.questionResId),
                fontSize = 16.sp, color = Color(0xFF1A1A2E), lineHeight = 24.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(step.hintResId),
                fontSize = 13.sp, color = Color(0xFF6B6B8A), lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = answer,
            onValueChange = onAnswerChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            placeholder = { Text(stringResource(R.string.thermo_placeholder), color = Color(0xFFAAAAAC)) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = step.color,
                unfocusedBorderColor = Color(0xFFEAE8F5),
                cursorColor = step.color,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = step.color)
        ) {
            Text(
                if (isLast) stringResource(R.string.thermo_complete) else stringResource(R.string.thermo_next),
                fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp
            )
        }

        if (answer.isBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.thermo_skip_step), color = Color(0xFF6B6B8A), fontSize = 13.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ThermometerDoneScreen(onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("💚", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.thermo_done_title),
            fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.thermo_done_body),
            fontSize = 15.sp, color = Color(0xFF6B6B8A), textAlign = TextAlign.Center, lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
        ) {
            Text(stringResource(R.string.thermo_done_button), fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}