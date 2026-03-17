package com.emotionalintelligence.emova.ui.tips

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emotionalintelligence.emova.R

// ─── Data ─────────────────────────────────────────────────────────────────────

data class WellnessTip(
    val index: Int,   // index into wellness_tip_titles / wellness_tip_bodies arrays
    val category: TipCategory
)

enum class TipCategory(val labelResId: Int, val emoji: String, val color: Color) {
    ENVIRONMENT(R.string.tip_cat_environment, "🏠", Color(0xFF4CC9F0)),
    BODY(R.string.tip_cat_body, "💪", Color(0xFF06D6A0)),
    MIND(R.string.tip_cat_mind, "🧠", Color(0xFF6C63FF)),
    RELATIONSHIPS(R.string.tip_cat_relationships, "🤝", Color(0xFFE8A020)),
    SLEEP(R.string.tip_cat_sleep, "🌙", Color(0xFF7B74FF)),
    FOCUS(R.string.tip_cat_focus, "🎯", Color(0xFFEF233C))
}

// Indices match wellness_tip_titles / wellness_tip_bodies array order (0–33)
val allTips = listOf(
    WellnessTip(0,  TipCategory.ENVIRONMENT),
    WellnessTip(1,  TipCategory.ENVIRONMENT),
    WellnessTip(2,  TipCategory.MIND),
    WellnessTip(3,  TipCategory.FOCUS),
    WellnessTip(4,  TipCategory.BODY),
    WellnessTip(5,  TipCategory.BODY),
    WellnessTip(6,  TipCategory.BODY),
    WellnessTip(7,  TipCategory.ENVIRONMENT),
    WellnessTip(8,  TipCategory.BODY),
    WellnessTip(9,  TipCategory.SLEEP),
    WellnessTip(10, TipCategory.RELATIONSHIPS),
    WellnessTip(11, TipCategory.MIND),
    WellnessTip(12, TipCategory.FOCUS),
    WellnessTip(13, TipCategory.SLEEP),
    WellnessTip(14, TipCategory.ENVIRONMENT),
    WellnessTip(15, TipCategory.MIND),
    WellnessTip(16, TipCategory.SLEEP),
    WellnessTip(17, TipCategory.RELATIONSHIPS),
    WellnessTip(18, TipCategory.FOCUS),
    WellnessTip(19, TipCategory.MIND),
    WellnessTip(20, TipCategory.MIND),
    WellnessTip(21, TipCategory.BODY),
    WellnessTip(22, TipCategory.MIND),
    WellnessTip(23, TipCategory.FOCUS),
    WellnessTip(24, TipCategory.FOCUS),
    WellnessTip(25, TipCategory.RELATIONSHIPS),
    WellnessTip(26, TipCategory.MIND),
    WellnessTip(27, TipCategory.ENVIRONMENT),
    WellnessTip(28, TipCategory.RELATIONSHIPS),
    WellnessTip(29, TipCategory.RELATIONSHIPS),
    WellnessTip(30, TipCategory.BODY),
    WellnessTip(31, TipCategory.BODY),
    WellnessTip(32, TipCategory.BODY),
    WellnessTip(33, TipCategory.BODY)
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun DailyTipsScreen() {
    val titles = stringArrayResource(R.array.wellness_tip_titles)
    val bodies = stringArrayResource(R.array.wellness_tip_bodies)

    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<TipCategory?>(null) }

    val filtered = allTips.filter { tip ->
        val title = titles.getOrElse(tip.index) { "" }
        val body = bodies.getOrElse(tip.index) { "" }
        val matchesSearch = search.isBlank() ||
                title.contains(search, ignoreCase = true) ||
                body.contains(search, ignoreCase = true)
        val matchesCategory = selectedCategory == null || tip.category == selectedCategory
        matchesSearch && matchesCategory
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F6FB)),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.tips_title), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Text(stringResource(R.string.tips_subtitle), fontSize = 14.sp, color = Color(0xFF6B6B8A))
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.tips_search), color = Color(0xFFAAAAAC)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF6B6B8A)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6C63FF),
                    unfocusedBorderColor = Color(0xFFEAE8F5),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = Color(0xFF6C63FF)
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            // "All" chip
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text(stringResource(R.string.tips_filter_all), fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6C63FF),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = Color(0xFF6B6B8A)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true, selected = selectedCategory == null,
                        borderColor = Color(0xFFEAE8F5), selectedBorderColor = Color.Transparent
                    )
                )
            }
            // First row of category chips
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipCategory.entries.take(3).forEach { cat ->
                    val label = stringResource(cat.labelResId)
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label = { Text("${cat.emoji} $label", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = cat.color,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color(0xFF6B6B8A)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = selectedCategory == cat,
                            borderColor = Color(0xFFEAE8F5), selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
            // Second row of category chips
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipCategory.entries.drop(3).forEach { cat ->
                    val label = stringResource(cat.labelResId)
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label = { Text("${cat.emoji} $label", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = cat.color,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color(0xFF6B6B8A)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = selectedCategory == cat,
                            borderColor = Color(0xFFEAE8F5), selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            Text(stringResource(R.string.tips_count, filtered.size), fontSize = 12.sp, color = Color(0xFF6B6B8A))
            Spacer(modifier = Modifier.height(4.dp))
        }

        itemsIndexed(filtered) { _, tip ->
            TipCard(
                title = titles.getOrElse(tip.index) { "" },
                body = bodies.getOrElse(tip.index) { "" },
                category = tip.category
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ─── Tip Card ─────────────────────────────────────────────────────────────────

@Composable
fun TipCard(title: String, body: String, category: TipCategory) {
    val categoryLabel = stringResource(category.labelResId)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEAE8F5), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E), modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .background(category.color.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("${category.emoji} $categoryLabel", fontSize = 11.sp,
                    color = category.color, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(body, fontSize = 14.sp, color = Color(0xFF6B6B8A), lineHeight = 21.sp)
    }
}