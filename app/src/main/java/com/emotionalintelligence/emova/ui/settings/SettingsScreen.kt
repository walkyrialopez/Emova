package com.emotionalintelligence.emova.ui.settings

import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.notifications.NotificationScheduler
import com.emotionalintelligence.emova.util.AppLanguage
import com.emotionalintelligence.emova.util.saveLanguage
import com.emotionalintelligence.emova.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current // 🎯 Herramienta pro para abrir links

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setNotificationsEnabled(true)
            NotificationScheduler.scheduleDailyReminder(context, uiState.reminderHour, uiState.reminderMinute)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5FA))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 32.dp)
    ) {
        Text(stringResource(R.string.settings_title), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111130))
        Text(stringResource(R.string.settings_subtitle), fontSize = 13.sp, color = Color(0xFF666688))

        Spacer(modifier = Modifier.height(28.dp))

        // ── Language ─────────────────────────────────────────────────────────
        SectionHeader("🌐 ${stringResource(R.string.settings_language)}")
        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppLanguage.entries.forEach { language ->
                    val isSelected = uiState.language == language
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Color(0xFF6C63FF).copy(alpha = 0.1f)
                                else Color(0xFFF7F6FB)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF6C63FF) else Color(0xFFEEEEF5),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                viewModel.setLanguage(language)
                                saveLanguage(context, language)
                                (context as? Activity)?.recreate()
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (language == AppLanguage.ENGLISH) "🇺🇸" else "🇪🇸",
                                fontSize = 26.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = language.displayName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF6C63FF) else Color(0xFF666688)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Notifications ─────────────────────────────────────────────────────
        SectionHeader("🔔 Notifications")
        SettingsCard {
            // ... (El código de notificaciones queda exactamente igual) ...
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.settings_notif_title), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111130))
                    Text(stringResource(R.string.settings_notif_subtitle), fontSize = 12.sp, color = Color(0xFF666688))
                }
                Switch(
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val permission = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.POST_NOTIFICATIONS
                                )
                                if (permission != PackageManager.PERMISSION_GRANTED) {
                                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.setNotificationsEnabled(true)
                                    NotificationScheduler.scheduleDailyReminder(context, uiState.reminderHour, uiState.reminderMinute)
                                }
                            } else {
                                viewModel.setNotificationsEnabled(true)
                                NotificationScheduler.scheduleDailyReminder(context, uiState.reminderHour, uiState.reminderMinute)
                            }
                        } else {
                            viewModel.setNotificationsEnabled(false)
                            NotificationScheduler.cancelReminder(context)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF6C63FF)
                    )
                )
            }

            if (uiState.notificationsEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFEEEEF5))
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.settings_reminder_time), fontSize = 14.sp, color = Color(0xFF666688))
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(8 to "8 AM", 12 to "12 PM", 18 to "6 PM", 20 to "8 PM", 22 to "10 PM").forEach { (hour, label) ->
                        val isSelected = uiState.reminderHour == hour
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF6C63FF) else Color(0xFFEEEEF5))
                                .clickable {
                                    viewModel.setReminderTime(hour, 0)
                                    NotificationScheduler.scheduleDailyReminder(context, hour, 0)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else Color(0xFF666688),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── 🌟 Apoya el Proyecto (Reemplaza a Export Data) ───────────────────
        SectionHeader("💖 " + stringResource(R.string.insights_support_title))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFEEEEF5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF4E5)), // Un fondo cálido para el emoji
                        contentAlignment = Alignment.Center
                    ) {
                        Text("☕", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            stringResource(R.string.insights_support_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF111130)
                        )
                        Text(
                            stringResource(R.string.insights_support_subtitle),
                            fontSize = 12.sp,
                            color = Color(0xFF666688)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    // 🎯 Aquí colocas tu link real de Ko-fi más adelante
                    onClick = { uriHandler.openUri("https://ko-fi.com/brunildalopez") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                ) {
                    Text(stringResource(R.string.insights_support_button), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── About ─────────────────────────────────────────────────────────────
        SectionHeader(stringResource(R.string.settings_about_section))
        SettingsCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { // Aumenté un poco el espaciado para que respire
                AboutRow(stringResource(R.string.settings_about_app), stringResource(R.string.settings_about_app_value))
                AboutRow(stringResource(R.string.settings_about_version), "1.0.0")
                AboutRow(stringResource(R.string.settings_about_built), "Jetpack Compose + Room")
            }
        }
    }
}

// ... (SectionHeader, SettingsCard y AboutRow se mantienen exactamente igual)
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF666688),
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFFFF))
            .border(1.dp, Color(0xFFEEEEF5), RoundedCornerShape(16.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color(0xFF666688))
        Text(value, fontSize = 14.sp, color = Color(0xFF111130), fontWeight = FontWeight.Medium)
    }
}