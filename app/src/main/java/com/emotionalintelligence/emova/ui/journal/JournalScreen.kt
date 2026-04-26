package com.emotionalintelligence.emova.ui.journal

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.emotionalintelligence.emova.model.emotionList
import com.emotionalintelligence.emova.viewmodel.JournalViewModel

@Composable
fun JournalScreen(
    checkInId: Int,
    emotionName: String,
    emotionEmoji: String,
    emotionFamily: String,
    onFinished: (showThermometer: Boolean, emotionName: String) -> Unit,
    onBack: () -> Unit,
    viewModel: JournalViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.initialize(checkInId, emotionName, emotionEmoji, emotionFamily)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved && !uiState.isLoadingAiResponse && uiState.aiResponse.isBlank()) {
            // Still loading AI — wait for it before navigating
        }
    }

// NEW: navigate only after AI response is ready (or failed)
    LaunchedEffect(uiState.isSaved, uiState.isLoadingAiResponse) {
        if (uiState.isSaved && !uiState.isLoadingAiResponse) {
            // AI response is ready — screen will show it, user taps Continue
        }
    }

    // Look up localized emotion name by English name (stored in navigation route)
    val localizedEmotionName = remember(emotionName) {
        null // resolved below with stringResource
    }
    val emotion = remember(emotionName) { emotionList.firstOrNull { it.name == emotionName } }
    val displayName = emotion?.let { stringResource(it.nameResId) } ?: emotionName

    val accentColor = Color(0xFF6C63FF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F6FB))
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.journal_back),
                    tint = Color(0xFF6B6B8A)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.journal_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Emotion badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFFFFFFFF))
                .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(50.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(text = uiState.emotionEmoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.journal_feeling, displayName),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A2E)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Progress dots
        if (uiState.promptResIds.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.promptResIds.indices.forEach { index ->
                    val isActive = index == uiState.currentPromptIndex
                    val isPast = index < uiState.currentPromptIndex
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isActive -> accentColor
                                    isPast -> accentColor.copy(alpha = 0.5f)
                                    else -> Color(0xFFEAE8F5)
                                }
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Animated prompt question
        AnimatedContent(
            targetState = uiState.currentPromptIndex,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
            },
            label = "prompt"
        ) { promptIndex ->
            val promptText = uiState.promptResIds.getOrNull(promptIndex)
                ?.let { stringResource(it) } ?: ""
            Text(
                text = promptText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E),
                textAlign = TextAlign.Center,
                lineHeight = 30.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Answer text field
        OutlinedTextField(
            value = uiState.currentAnswer,
            onValueChange = viewModel::setAnswer,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            placeholder = {
                Text(
                    stringResource(R.string.journal_placeholder),
                    color = Color(0xFFAAAAAC),
                    fontSize = 14.sp
                )
            },
            textStyle = LocalTextStyle.current.copy(
                color = Color(0xFF1A1A2E),
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color(0xFFEAE8F5),
                cursorColor = accentColor,
                focusedContainerColor = Color(0xFFFFFFFF),
                unfocusedContainerColor = Color(0xFFFFFFFF)
            ),
            shape = RoundedCornerShape(16.dp),
            maxLines = 8
        )

        Spacer(modifier = Modifier.weight(1f))
        // Show AI response card when saved
        AnimatedVisibility(
            visible = uiState.isSaved,
            enter = fadeIn() + slideInVertically { it / 2 }
        ) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF6C63FF).copy(alpha = 0.06f))
                        .border(1.dp, Color(0xFF6C63FF).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    if (uiState.isLoadingAiResponse) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color(0xFF6C63FF),
                                strokeWidth = 2.dp
                            )
                            Text(
                                "Reflecting on what you shared...",
                                fontSize = 13.sp,
                                color = Color(0xFF6B6B8A)
                            )
                        }
                    } else if (uiState.aiError.isNotBlank()) {
                        // Error state
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("⚠️", fontSize = 16.sp)
                            Text(
                                uiState.aiError,
                                fontSize = 13.sp,
                                color = Color(0xFF6B6B8A),
                                lineHeight = 19.sp
                            )
                        }
                    } else {
                        // Success state — full text, no truncation
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("✨", fontSize = 14.sp)
                                Text(
                                    "A reflection for you",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6C63FF),
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = uiState.aiResponse,
                                fontSize = 14.sp,
                                color = Color(0xFF1A1A2E),
                                lineHeight = 22.sp
                                // No maxLines — shows everything
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Replace the nav buttons with a single Continue button when saved
                Button(
                    onClick = {
                        val isDifficult = emotionFamily == "DIFFICULT" || emotionFamily == "ACTIVATED"
                        onFinished(isDifficult, emotionName)
                    },
                    enabled = !uiState.isLoadingAiResponse,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(
                        "Continue",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.currentPromptIndex > 0) {
                OutlinedButton(
                    onClick = viewModel::prevPrompt,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B6B8A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEAE8F5))
                ) {
                    Text(stringResource(R.string.journal_back), fontSize = 15.sp)
                }
            } else {
                OutlinedButton(
                    onClick = viewModel::skip,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B6B8A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEAE8F5))
                ) {
                    Text(stringResource(R.string.journal_skip), fontSize = 15.sp)
                }
            }

            val isLast = uiState.currentPromptIndex == uiState.promptResIds.size - 1
            Button(
                onClick = viewModel::nextPrompt,
                modifier = Modifier.weight(2f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(
                    text = if (isLast) stringResource(R.string.journal_finish) else stringResource(R.string.journal_next),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
            }
        }
    }
}