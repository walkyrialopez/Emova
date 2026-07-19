package com.emotionalintelligence.emova.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.viewmodel.JournalStep
import com.emotionalintelligence.emova.viewmodel.JournalViewModel
import androidx.compose.ui.draw.clip
import com.emotionalintelligence.emova.ui.components.WhyItWorksCard
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun JournalScreen(
    checkInId: Int,
    emotionName: String, // Este es el nombre en inglés ("Angry")
    emotionEmoji: String,
    emotionFamily: String,
    initialIntensity: Int,
    onFinish: () -> Unit,
    onBack: () -> Unit,
    viewModel: JournalViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 🎯 1. Buscamos la emoción en nuestra lista modelo
    val currentEmotion = remember(emotionName) {
        com.emotionalintelligence.emova.model.emotionList
            .firstOrNull { it.name == emotionName }
    }

    // 🎯 2. Obtenemos el nombre traducido desde strings.xml
    val displayEmotionName = currentEmotion?.let { stringResource(it.nameResId) } ?: emotionName

    // 🎯 3. Inicializamos el ViewModel pasando el nombre TRADUCIDO a Groq
    LaunchedEffect(Unit) {
        viewModel.initialize(checkInId, displayEmotionName, emotionEmoji, emotionFamily, initialIntensity)
    }

    // Navegación automática cuando se completa el flujo
    LaunchedEffect(uiState.currentStep) {
        if (uiState.currentStep == JournalStep.COMPLETE) {
            onFinish()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5FA))
            .padding(24.dp)
    ) {
        // ─── Cabecera (Emoji + Emoción) ───
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emotionEmoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                // 🎯 4. Mostramos el nombre traducido en el título de la pantalla
                text = displayEmotionName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111130)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ─── Máquina de Estados de la Pantalla ───
        when (uiState.currentStep) {
            JournalStep.BRAIN_DUMP -> {
                BrainDumpStep(
                    onContinue = { text -> viewModel.submitBrainDump(text) },
                    onSkip = { viewModel.submitBrainDump("") }
                )
            }
            JournalStep.AI_CHAT -> {
                AiChatStep(
                    aiResponse = uiState.aiResponse,
                    isLoading = uiState.isLoadingAiResponse,
                    error = uiState.aiError,
                    onContinue = { viewModel.proceedToIntensityCheck() }
                )
            }
            JournalStep.INTENSITY_CHECK -> {
                IntensityCheckStep(
                    initialIntensity = uiState.initialIntensity,
                    onFinish = { finalInt -> viewModel.finishAndSave(finalInt) }
                )
            }
            JournalStep.COMPLETE -> {
                // Pantalla vacía transicional mientras el LaunchedEffect navega hacia atrás
            }
        }
    }
}

// ─── PASO 1: El Vaciado Inicial ───
@Composable
fun BrainDumpStep(onContinue: (String) -> Unit, onSkip: () -> Unit) {
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.journal_dump_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111130)
        )

        Spacer(modifier = Modifier.height(16.dp)) // Aumentamos espacio

        // 🎯 La tarjeta
        WhyItWorksCard(message = stringResource(R.string.why_it_works_journal))

        Spacer(modifier = Modifier.height(24.dp)) // Espacio antes del subtítulo

        Text(
            text = stringResource(R.string.journal_dump_subtitle),
            fontSize = 15.sp,
            color = Color(0xFF6B6B8A)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFEEEEF5),
                focusedBorderColor = Color(0xFF6C63FF),
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            placeholder = { Text("...", color = Color(0xFFB3B3C3)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onContinue(text) },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
        ) {
            Text(stringResource(R.string.journal_dump_continue), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        TextButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.journal_dump_skip), color = Color(0xFF9999B3), fontSize = 15.sp)
        }
    }
}

// ─── PASO 2: La Respuesta de la IA ───
// PASO 2: La Respuesta de la IA (Refactorizado con Diseño UI Defensivo)
@Composable
fun AiChatStep(aiResponse: String, isLoading: Boolean, error: String, onContinue: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF6C63FF))
            }
        } else if (error.isNotEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(error, color = Color(0xFFEF233C), textAlign = TextAlign.Center)
            }
        } else {
            // 🎯 Burbuja de chat de la IA
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                // 🌟 EL BLINDAJE PRO: Agregamos un Box que permite scroll vertical interno
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()) // <- ESTA ES LA MAGIA
                        .padding(20.dp)
                ) {
                    Text(
                        text = aiResponse,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = Color(0xFF111130)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Aquí está tu Disclaimer de Seguridad y Privacidad
        PrivacyDisclaimer()
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onContinue,
            enabled = !isLoading && error.isEmpty(),
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
        ) {
            Text(stringResource(R.string.journal_dump_continue), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
// ─── PASO 3: Cierre e Intensidad Final ───
@Composable
fun IntensityCheckStep(initialIntensity: Int, onFinish: (Int) -> Unit) {
    var currentVal by remember { mutableStateOf(initialIntensity.toFloat()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.journal_intensity_after),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111130),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text("${currentVal.toInt()}", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6C63FF))

        Spacer(modifier = Modifier.height(20.dp))

        Slider(
            value = currentVal,
            onValueChange = { currentVal = it },
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF6C63FF),
                activeTrackColor = Color(0xFF6C63FF),
                inactiveTrackColor = Color(0xFFEEEEF5)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { onFinish(currentVal.toInt()) },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
        ) {
            Text("Finalizar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PrivacyDisclaimer() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Si no tienes este ícono, puedes omitirlo o usar Icons.Default.Info
        Text("🔒", fontSize = 12.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.journal_ai_disclaimer),
            fontSize = 11.sp,
            color = Color(0xFF9999B3),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}
