package com.emotionalintelligence.emova.ui.checkin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.emotionalintelligence.emova.R
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emotionalintelligence.emova.model.Emotion
import com.emotionalintelligence.emova.model.EmotionFamily
import com.emotionalintelligence.emova.viewmodel.CheckInViewModel

@Composable
fun CheckInScreen(
    onCheckInSaved: (checkInId: Int, emotionName: String, emotionEmoji: String, emotionFamily: String) -> Unit = { _, _, _, _ -> },
    viewModel: CheckInViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hasSelection = uiState.selectedEmotion != null

    LaunchedEffect(uiState.savedCheckInId) {
        if (uiState.savedCheckInId > -1) {
            val emotion = uiState.selectedEmotion!!
            onCheckInSaved(
                uiState.savedCheckInId,
                emotion.name,
                emotion.emoji,
                emotion.family.name
            )
            viewModel.resetSaved()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5FA))) {

        // ── Main scrollable content ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                // Extra bottom padding when sheet is open so last row stays visible
                .padding(top = 32.dp, bottom = if (hasSelection) 220.dp else 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.checkin_title),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111130),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.checkin_subtitle),
                fontSize = 14.sp,
                color = Color(0xFF111130),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))

            EmotionFamilyTabs(
                families = EmotionFamily.values().toList(),
                selectedFamily = uiState.selectedEmotion?.family,
                selectedEmotion = uiState.selectedEmotion,
                emotions = viewModel.emotions,
                onEmotionSelected = viewModel::selectEmotion
            )
        }

        // ── Bottom fade hint (visible when nothing selected) ─────────────────
        AnimatedVisibility(
            visible = !hasSelection,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xFFF5F5FA))
                        )
                    )
            )
        }

        // ── Sticky bottom sheet ──────────────────────────────────────────────
        AnimatedVisibility(
            visible = hasSelection,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(220)
            )
        ) {
            uiState.selectedEmotion?.let { emotion ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(Color.White)
                        .border(
                            width = 1.dp,
                            color = emotion.color.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Drag handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(width = 40.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDDDDEE))
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Selected emotion summary
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(emotion.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emotion.emoji, fontSize = 22.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(emotion.nameResId),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111130)
                            )
                            Text(
                                text = stringResource(emotion.descResId),
                                fontSize = 12.sp,
                                color = Color(0xFF6B6B8A)
                            )
                        }
                        // Deselect button
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF0F0F8))
                                .clickable { viewModel.clearSelection() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✕", fontSize = 11.sp, color = Color(0xFF9999AA))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Intensity slider
                    IntensitySlider(
                        value = uiState.intensity,
                        accentColor = emotion.color,
                        onValueChange = viewModel::setIntensity
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save button
                    Button(
                        onClick = viewModel::saveCheckIn,
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = emotion.color)
                    ) {
                        Text(
                            text = if (uiState.isLoading) stringResource(R.string.checkin_saving) else stringResource(R.string.checkin_save),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ─── Emotion Family Tabs + Grid ───────────────────────────────────────────────

@Composable
fun EmotionFamilyTabs(
    families: List<EmotionFamily>,
    selectedFamily: EmotionFamily?,
    selectedEmotion: Emotion?,
    emotions: List<Emotion>,
    onEmotionSelected: (Emotion) -> Unit
) {
    var activeFamilyTab by remember { mutableStateOf(families.first()) }

    val familyRows = families.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        familyRows.forEach { rowFamilies ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowFamilies.forEach { family ->
                    val isActive = activeFamilyTab == family
                    FilterChip(
                        selected = isActive,
                        onClick = { activeFamilyTab = family },
                        modifier = Modifier.weight(1f),
                        label = {
                            Text(
                                text = stringResource(family.labelResId),
                                fontSize = 11.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = family.color,
                            selectedLabelColor = Color(0xFFF5F5FA),
                            containerColor = Color(0xFFFFFFFF),
                            labelColor = Color(0xFF333355)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isActive,
                            borderColor = Color(0xFFEEEEF5),
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Full natural-height grid — parent Column handles scrolling
    val filteredEmotions = emotions.filter { it.family == activeFamilyTab }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        filteredEmotions.chunked(3).forEach { rowEmotions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowEmotions.forEach { emotion ->
                    Box(modifier = Modifier.weight(1f)) {
                        EmotionChip(
                            emotion = emotion,
                            isSelected = emotion == selectedEmotion,
                            onSelected = onEmotionSelected
                        )
                    }
                }
                repeat(3 - rowEmotions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ─── Emotion Chip ─────────────────────────────────────────────────────────────

@Composable
fun EmotionChip(
    emotion: Emotion,
    isSelected: Boolean,
    onSelected: (Emotion) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) emotion.color.copy(alpha = 0.18f) else Color(0xFFFFFFFF)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) emotion.color else Color(0xFFEEEEF5),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onSelected(emotion) }
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = emotion.emoji, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = stringResource(emotion.nameResId),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) emotion.color else Color(0xFF333355),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// ─── Intensity Slider ─────────────────────────────────────────────────────────

@Composable
fun IntensitySlider(
    value: Int,
    accentColor: Color,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.checkin_intensity),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111130)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = stringResource(R.string.checkin_intensity_value, value),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color(0xFFEEEEF5)
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.checkin_mild), fontSize = 11.sp, color = Color(0xFFBBBBCC))
            Text(stringResource(R.string.checkin_intense), fontSize = 11.sp, color = Color(0xFFBBBBCC))
        }
    }
}