package com.emotionalintelligence.emova.ui.vocabulary

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.model.EmotionFamily
import com.emotionalintelligence.emova.model.EmotionWord
import com.emotionalintelligence.emova.model.localizedVocabularyList
import com.emotionalintelligence.emova.model.vocabularyList

@Composable
fun VocabularyScreen() {
    val context = LocalContext.current
    val localizedList: List<EmotionWord> = remember(context) { localizedVocabularyList(context) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFamily by remember { mutableStateOf<EmotionFamily?>(null) }
    var selectedWord by remember { mutableStateOf<EmotionWord?>(null) }
    val currentWord = selectedWord

    if (currentWord != null) {
        WordDetailScreen(word = currentWord, onBack = { selectedWord = null })
        return
    }

    val filtered = localizedList.filter { word ->
        (searchQuery.isBlank() || word.word.contains(searchQuery, ignoreCase = true) ||
                word.definition.contains(searchQuery, ignoreCase = true)) &&
                (selectedFamily == null || word.family == selectedFamily)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB))) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
            Text(stringResource(R.string.vocab_title), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Text(stringResource(R.string.vocab_subtitle, localizedList.size), fontSize = 13.sp, color = Color(0xFF6B6B8A))

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.vocab_search), color = Color(0xFFAAAAAC)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFAAAAAC)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6C63FF), unfocusedBorderColor = Color(0xFFEAE8F5),
                    cursorColor = Color(0xFF6C63FF), focusedContainerColor = Color(0xFFFFFFFF),
                    unfocusedContainerColor = Color(0xFFFFFFFF), focusedTextColor = Color(0xFF1A1A2E), unfocusedTextColor = Color(0xFF1A1A2E)
                ),
                shape = RoundedCornerShape(14.dp), singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFamily == null, onClick = { selectedFamily = null },
                    label = { Text(stringResource(R.string.vocab_all), fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6C63FF), selectedLabelColor = Color.White,
                        containerColor = Color(0xFFFFFFFF), labelColor = Color(0xFF6B6B8A)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true, selected = selectedFamily == null,
                        borderColor = Color(0xFFEAE8F5), selectedBorderColor = Color.Transparent
                    )
                )
                EmotionFamily.values().forEach { family ->
                    val familyLabel = stringResource(family.labelResId)
                    FilterChip(
                        selected = selectedFamily == family,
                        onClick = { selectedFamily = if (selectedFamily == family) null else family },
                        label = { Text(familyLabel, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = family.color, selectedLabelColor = Color(0xFFF7F6FB),
                            containerColor = Color(0xFFFFFFFF), labelColor = Color(0xFF6B6B8A)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = selectedFamily == family,
                            borderColor = Color(0xFFEAE8F5), selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered) { word ->
                WordCard(word = word, onClick = { selectedWord = word })
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun WordCard(word: EmotionWord, onClick: () -> Unit) {
    val familyLabel = stringResource(word.family.labelResId)
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFFFF))
            .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            repeat(3) { i ->
                Box(modifier = Modifier.size(6.dp).clip(CircleShape)
                    .background(if (i < word.intensity) word.family.color else Color(0xFFEAE8F5)))
                if (i < 2) Spacer(modifier = Modifier.height(3.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(word.word, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Text(word.definition, fontSize = 12.sp, color = Color(0xFF6B6B8A), maxLines = 2)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                .background(word.family.color.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(familyLabel, fontSize = 10.sp, color = word.family.color, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun WordDetailScreen(word: EmotionWord, onBack: () -> Unit) {
    val familyLabel = stringResource(word.family.labelResId)
    val intensityLabel = when (word.intensity) {
        1 -> stringResource(R.string.lessons_mild)
        2 -> stringResource(R.string.lessons_moderate)
        else -> stringResource(R.string.lessons_intense)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB)).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF6B6B8A))
            }
            Text(stringResource(R.string.vocab_back), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    .background(word.family.color.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) { Text(familyLabel, fontSize = 12.sp, color = word.family.color, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(12.dp))
            Text(word.word, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.lessons_intensity), fontSize = 13.sp, color = Color(0xFF6B6B8A))
                repeat(3) { i ->
                    Box(modifier = Modifier.padding(horizontal = 2.dp).size(10.dp).clip(CircleShape)
                        .background(if (i < word.intensity) word.family.color else Color(0xFFEAE8F5)))
                }
                Text(intensityLabel, fontSize = 13.sp, color = word.family.color, modifier = Modifier.padding(start = 6.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionCard(title = stringResource(R.string.vocab_section_definition), color = word.family.color) {
                Text(word.definition, fontSize = 15.sp, color = Color(0xFF1A1A2E), lineHeight = 22.sp)
            }
            Spacer(modifier = Modifier.height(14.dp))

            SectionCard(title = stringResource(R.string.vocab_section_real_life), color = word.family.color) {
                Text("\"${word.example}\"", fontSize = 14.sp, color = Color(0xFF1A1A2E),
                    lineHeight = 21.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
            Spacer(modifier = Modifier.height(14.dp))

            if (word.practicalTip.isNotBlank()) {
                SectionCard(title = stringResource(R.string.vocab_section_what_to_do), color = Color(0xFF6C63FF)) {
                    Text(word.practicalTip, fontSize = 15.sp, color = Color(0xFF1A1A2E), lineHeight = 22.sp)
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            SectionCard(title = stringResource(R.string.vocab_section_related), color = word.family.color) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    word.relatedWords.forEach { related ->
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                .background(word.family.color.copy(alpha = 0.15f))
                                .border(1.dp, word.family.color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) { Text(related, fontSize = 13.sp, color = word.family.color, fontWeight = FontWeight.Medium) }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SectionCard(title: String, color: Color, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFFFF))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(16.dp)).padding(16.dp)
    ) {
        Text(title.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}