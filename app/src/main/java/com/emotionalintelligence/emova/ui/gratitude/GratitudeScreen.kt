package com.emotionalintelligence.emova.ui.gratitude

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.data.EmováDatabase
import com.emotionalintelligence.emova.model.GratitudeEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ─── State ────────────────────────────────────────────────────────────────────

data class GratitudeUiState(
    val entries: List<String> = List(3) { "" }, // 🎯 Reducido a 3
    val isSaved: Boolean = false,
    val currentFocus: Int = 0,
    val alreadyDoneToday: Boolean = false
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class GratitudeViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val db = EmováDatabase.getInstance(application)
    private val _uiState = MutableStateFlow(GratitudeUiState())
    val uiState: StateFlow<GratitudeUiState> = _uiState.asStateFlow()
    val allEntries = db.gratitudeEntryDao().getAll()

    init {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val existing = db.gratitudeEntryDao().getByDate(today)
            if (existing != null) {
                val loaded = existing.items.split("||").filter { it.isNotBlank() }
                val padded = loaded + List((3 - loaded.size).coerceAtLeast(0)) { "" } // 🎯 Reducido a 3
                _uiState.value = _uiState.value.copy(entries = padded, isSaved = true, alreadyDoneToday = true)
            }
        }
    }

    fun updateEntry(index: Int, text: String) {
        val updated = _uiState.value.entries.toMutableList().also { it[index] = text }
        _uiState.value = _uiState.value.copy(entries = updated, isSaved = false)
    }

    fun setFocus(index: Int) { _uiState.value = _uiState.value.copy(currentFocus = index) }

    fun save() {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            db.gratitudeEntryDao().insert(
                GratitudeEntry(date = today,
                    items = _uiState.value.entries.filter { it.isNotBlank() }.joinToString("||"))
            )
            _uiState.value = _uiState.value.copy(isSaved = true, alreadyDoneToday = true)
        }
    }

    fun reset() { _uiState.value = GratitudeUiState() }
    val filledCount get() = _uiState.value.entries.count { it.isNotBlank() }
}

// ─── Main Screen ──────────────────────────────────────────────────────────────

@Composable
fun GratitudeScreen(onBack: (() -> Unit)? = null, vm: GratitudeViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val accent = Color(0xFFE8A020)
    var showHistory by remember { mutableStateOf(false) }

    if (showHistory) {
        GratitudeHistoryScreen(onBack = { showHistory = false }, vm = vm)
        return
    }

    if (state.isSaved && state.alreadyDoneToday) {
        GratitudeDoneScreen(count = vm.filledCount, onReset = vm::reset)
        return
    }

    val allPlaceholders = stringArrayResource(R.array.gratitude_placeholders)

    // 🎯 MAGIA: Rotación de placeholders anclada al día actual.
    // Garantiza que sean aleatorias, pero constantes durante las 24 horas del mismo día.
    val dailyPlaceholders = remember(allPlaceholders) {
        val calendar = Calendar.getInstance()
        val seed = calendar.get(Calendar.DAY_OF_YEAR).toLong() + calendar.get(Calendar.YEAR)
        val random = java.util.Random(seed)
        allPlaceholders.toList().shuffled(random).take(3)
    }

    val progress = vm.filledCount / 3f // 🎯 Reducido a 3f
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(400), label = "progress")
    val canSave = vm.filledCount >= 1

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB)),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stringResource(R.string.gratitude_title), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                    Text(stringResource(R.string.gratitude_subtitle), fontSize = 14.sp, color = Color(0xFF6B6B8A))
                }
                TextButton(onClick = { showHistory = true }) {
                    Text(stringResource(R.string.gratitude_history), color = accent, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFEAE8F5))) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(3.dp)).background(accent))
                }
                Text("${vm.filledCount}/3", fontSize = 13.sp, // 🎯 Reducido a /3
                    color = if (canSave) accent else Color(0xFF6B6B8A), fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            WhyItWorksCard(message = stringResource(R.string.why_it_works_gratitude))
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(state.entries.indices.toList()) { index ->
            GratitudeEntryRow(
                index = index, value = state.entries[index],
                isFocused = state.currentFocus == index, accent = accent,
                placeholder = dailyPlaceholders.getOrElse(index) { "" }, // 🎯 Usando las diarias
                onValueChange = { vm.updateEntry(index, it) },
                onFocus = { vm.setFocus(index) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = vm::save,
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    disabledContainerColor = Color(0xFFEAE8F5)
                )
            ) {
                Text(
                    text = if (canSave) {
                        if (vm.filledCount == 1) stringResource(R.string.gratitude_save_single)
                        else stringResource(R.string.gratitude_save_plural, vm.filledCount)
                    } else stringResource(R.string.gratitude_write_prompt),
                    fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = if (canSave) Color.White else Color(0xFF6B6B8A)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─── Entry Row ────────────────────────────────────────────────────────────────

@Composable
fun GratitudeEntryRow(
    index: Int, value: String, isFocused: Boolean, accent: Color,
    placeholder: String, onValueChange: (String) -> Unit, onFocus: () -> Unit
) {
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.01f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "scale")
    Row(modifier = Modifier.fillMaxWidth().scale(scale),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape)
            .background(if (value.isNotBlank()) accent.copy(alpha = 0.15f) else Color(0xFFEAE8F5)),
            contentAlignment = Alignment.Center) {
            Text(if (value.isNotBlank()) "✓" else "${index + 1}", fontSize = 13.sp,
                fontWeight = FontWeight.Bold, color = if (value.isNotBlank()) accent else Color(0xFF6B6B8A))
        }
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.weight(1f).onFocusChanged { if (it.isFocused) onFocus() },
            placeholder = { Text(placeholder, color = Color(0xFFAAAAAC), fontSize = 14.sp) },
            textStyle = LocalTextStyle.current.copy(color = Color(0xFF1A1A2E), fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                unfocusedBorderColor = if (value.isNotBlank()) accent.copy(alpha = 0.4f) else Color(0xFFEAE8F5),
                cursorColor = accent, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp), singleLine = true
        )
    }
}

// ─── Done Screen ──────────────────────────────────────────────────────────────

@Composable
fun GratitudeDoneScreen(count: Int, onReset: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB)).padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("🌟", fontSize = 72.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.gratitude_beautiful), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.gratitude_done_body, count),
            fontSize = 16.sp, color = Color(0xFF6B6B8A), textAlign = TextAlign.Center, lineHeight = 24.sp)
        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = onReset, modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8A020))) {
            Text(stringResource(R.string.gratitude_start_fresh), fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ─── History Screen ───────────────────────────────────────────────────────────

@Composable
fun GratitudeHistoryScreen(onBack: () -> Unit, vm: GratitudeViewModel = viewModel()) {
    val entries by vm.allEntries.collectAsState(initial = emptyList())
    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB))) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.gratitude_back), color = Color(0xFF6B6B8A), fontSize = 15.sp) }
            Text(stringResource(R.string.gratitude_past_entries), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Spacer(modifier = Modifier.weight(1f))
            Text(stringResource(R.string.gratitude_days_count, entries.size), fontSize = 13.sp, color = Color(0xFF6B6B8A),
                modifier = Modifier.padding(end = 16.dp))
        }
        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.gratitude_empty),
                    color = Color(0xFF6B6B8A), textAlign = TextAlign.Center, lineHeight = 24.sp)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(entries) { entry ->
                    val isExpanded = expanded[entry.date] == true
                    val itemCount = entry.items.split("||").filter { it.isNotBlank() }.size
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp,
                                if (isExpanded) Color(0xFFE8A020).copy(alpha = 0.4f) else Color(0xFFEAE8F5),
                                RoundedCornerShape(16.dp))
                            .clickable { expanded[entry.date] = !isExpanded }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("🙏", fontSize = 20.sp)
                                Column {
                                    Text(entry.date, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                                    Text(
                                        if (itemCount == 1) stringResource(R.string.gratitude_items_single, itemCount)
                                        else stringResource(R.string.gratitude_items_plural, itemCount),
                                        fontSize = 12.sp, color = Color(0xFF6B6B8A)
                                    )
                                }
                            }
                            Text(if (isExpanded) "▲" else "▼", fontSize = 12.sp, color = Color(0xFFE8A020))
                        }
                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFFAF0))
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                entry.items.split("||").filter { it.isNotBlank() }.forEachIndexed { i, item ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("${i + 1}.", fontSize = 13.sp, color = Color(0xFFE8A020),
                                            fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                                        Text(item, fontSize = 14.sp, color = Color(0xFF1A1A2E), lineHeight = 20.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
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