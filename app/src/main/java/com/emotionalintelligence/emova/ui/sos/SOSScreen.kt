package com.emotionalintelligence.emova.ui.sos

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.emotionalintelligence.emova.R

/**
 * 5-4-3-2-1 grounding exercise as a full-screen dialog.
 * [onDismiss] closes it; [onCompleted] fires when the person reaches the last step and taps "I feel calmer".
 */
@Composable
fun GroundingExercise(onDismiss: () -> Unit, onCompleted: () -> Unit = {}) {
    var groundingStep by remember { mutableIntStateOf(0) }
    val groundingEmojis = stringArrayResource(R.array.sos_grounding_emojis)
    val groundingTexts = stringArrayResource(R.array.sos_grounding_steps)

    run {
        Dialog(
            onDismissRequest = { onDismiss() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F6FB))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Step indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    groundingEmojis.forEachIndexed { i, _ ->
                        Box(
                            modifier = Modifier.size(8.dp).clip(CircleShape)
                                .background(
                                    if (i <= groundingStep) Color(0xFF6C63FF)
                                    else Color(0xFFDDDBF5)
                                )
                        )
                    }
                }

                AnimatedContent(
                    targetState = groundingStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "grounding"
                ) { step ->
                    val emoji = groundingEmojis.getOrElse(step) { "" }
                    val text = groundingTexts.getOrElse(step) { "" }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(emoji, fontSize = 72.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "${5 - step}",
                            fontSize = 48.sp, fontWeight = FontWeight.Bold,
                            color = Color(0xFF6C63FF)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text,
                            fontSize = 20.sp, color = Color(0xFF1A1A2E),
                            textAlign = TextAlign.Center, lineHeight = 30.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                if (groundingStep < groundingEmojis.size - 1) {
                    Button(
                        onClick = { groundingStep++ },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                    ) {
                        Text(stringResource(R.string.sos_next), fontWeight = FontWeight.Bold,
                            color = Color.White, fontSize = 16.sp)
                    }
                } else {
                    Button(
                        onClick = { onCompleted(); onDismiss() },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06D6A0))
                    ) {
                        Text(stringResource(R.string.sos_calmer), fontWeight = FontWeight.Bold,
                            color = Color.White, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { onDismiss() }) {
                    Text(stringResource(R.string.sos_close), color = Color(0xFF6B6B8A))
                }
            }
        }
    }

}
