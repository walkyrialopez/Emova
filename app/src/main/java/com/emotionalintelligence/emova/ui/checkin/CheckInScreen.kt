package com.emotionalintelligence.emova.ui.checkin

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.model.Emotion
import com.emotionalintelligence.emova.model.EmotionFamily
import com.emotionalintelligence.emova.viewmodel.CheckInViewModel

@Composable
fun CheckInScreen(
    onCheckInSaved: (Int, String, String, String, Int) -> Unit, // <-- Ese ", Int" al final es la clave
    viewModel: CheckInViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hasSelection = uiState.selectedEmotion != null

    // 🎯 NUEVO: Medición dinámica de la tarjeta inferior
    val density = LocalDensity.current
    var bottomSheetHeight by remember { mutableStateOf(0.dp) }

    // 🎯 NUEVO: Elevamos el estado de la pestaña aquí para poder filtrar la cuadrícula
    var activeFamilyTab by remember { mutableStateOf(EmotionFamily.entries.first()) }

    LaunchedEffect(uiState.savedCheckInId) {
        if (uiState.savedCheckInId > -1) {
            val emotion = uiState.selectedEmotion!!
            onCheckInSaved(
                uiState.savedCheckInId,
                emotion.name, // Asegúrate de que en tu modelo de datos uses el id o texto correcto.
                emotion.emoji,
                emotion.family.name,
                uiState.intensity
            )
            viewModel.resetSaved()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5FA))) {

        val filteredEmotions = viewModel.emotions.filter { it.family == activeFamilyTab }

        // ── Main scrollable grid (La forma élite de hacer listas) ────────────
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(
                top = 32.dp,
                start = 20.dp,
                end = 20.dp,
                // Si hay selección, usamos la altura exacta de la tarjeta. Si no, 32dp normales.
                bottom = if (hasSelection) bottomSheetHeight + 16.dp else 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // HEADER (Ocupa todo el ancho)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 28.dp)
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

                    Spacer(modifier = Modifier.height(20.dp))
                    WhyItWorksCard(message = stringResource(R.string.why_it_works_checkin))
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // TABS DE FAMILIA (Ocupa todo el ancho)
            item(span = { GridItemSpan(maxLineSpan) }) {
                FamilyTabsRow(
                    families = EmotionFamily.entries,
                    activeFamilyTab = activeFamilyTab,
                    onTabSelected = { activeFamilyTab = it }
                )
            }

            // EMOCIONES (Se distribuyen solas en 3 columnas)
            items(filteredEmotions) { emotion ->
                EmotionChip(
                    emotion = emotion,
                    isSelected = emotion == uiState.selectedEmotion,
                    onSelected = viewModel::selectEmotion
                )
            }
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
            modifier = Modifier
                .align(Alignment.BottomCenter)
                // 🎯 NUEVO: Aquí medimos el alto real de esta tarjeta en la pantalla
                .onGloballyPositioned { coordinates ->
                    with(density) {
                        bottomSheetHeight = coordinates.size.height.toDp()
                    }
                },
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

// ─── Emotion Family Tabs ──────────────────────────────────────────────────────

@Composable
fun FamilyTabsRow(
    families: List<EmotionFamily>,
    activeFamilyTab: EmotionFamily,
    onTabSelected: (EmotionFamily) -> Unit
) {
    val familyRows = families.chunked(4)
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        familyRows.forEach { rowFamilies ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowFamilies.forEach { family ->
                    val isActive = activeFamilyTab == family
                    FilterChip(
                        selected = isActive,
                        onClick = { onTabSelected(family) },
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
            .padding(vertical = 14.dp, horizontal = 4.dp), // Ajustado el padding horizontal
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

@Composable
fun WhyItWorksCard(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF6C63FF).copy(alpha = 0.08f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("💡", fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            color = Color(0xFF1A1A2E),
            lineHeight = 18.sp
        )
    }
}