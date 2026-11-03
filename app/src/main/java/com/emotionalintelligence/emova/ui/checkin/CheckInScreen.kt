package com.emotionalintelligence.emova.ui.checkin

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
import com.emotionalintelligence.emova.model.Trigger
import com.emotionalintelligence.emova.viewmodel.CheckInStep
import com.emotionalintelligence.emova.viewmodel.CheckInUiState
import com.emotionalintelligence.emova.viewmodel.CheckInViewModel

private val Ink = Color(0xFF111130)
private val Muted = Color(0xFF6B6B8A)
private val Accent = Color(0xFF6C63FF)
private val Bg = Color(0xFFF5F5FA)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CheckInScreen(
    onFinish: () -> Unit,
    onOpenMindfulness: () -> Unit = {},
    viewModel: CheckInViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(ui.isFinished) {
        if (ui.isFinished) {
            viewModel.reset()
            onFinish()
        }
    }

    // Back steps inside the flow instead of leaving the screen.
    BackHandler(enabled = ui.step != CheckInStep.EMOTION && ui.step != CheckInStep.RESULT) {
        viewModel.back()
    }

    Column(modifier = Modifier.fillMaxSize().background(Bg)) {

        // ── Header: back + progress ───────────────────────────────────────
        Column(modifier = Modifier.padding(start = 12.dp, end = 20.dp, top = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val canGoBack = ui.step != CheckInStep.EMOTION && ui.step != CheckInStep.RESULT
                if (canGoBack) {
                    IconButton(onClick = { viewModel.back() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Ink)
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
                Text(
                    text = stringResource(R.string.w_step, ui.stepNumber, ui.totalSteps),
                    fontSize = 13.sp,
                    color = Muted,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(48.dp))
            }
            LinearProgressIndicator(
                progress = { ui.stepNumber / ui.totalSteps.toFloat() },
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp).height(6.dp).clip(CircleShape),
                color = ui.selectedEmotion?.color ?: Accent,
                trackColor = Color(0xFFEAE8F5)
            )
        }

        AnimatedContent(
            targetState = ui.step,
            transitionSpec = {
                (slideInHorizontally { it / 4 } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 4 } + fadeOut())
            },
            label = "checkin_step",
            modifier = Modifier.weight(1f)
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                when (step) {
                    CheckInStep.EMOTION -> EmotionStep(ui, viewModel)
                    CheckInStep.INTENSITY_CONTEXT -> IntensityContextStep(ui, viewModel)
                    CheckInStep.REFLECT -> ReflectStep(ui, viewModel)
                    CheckInStep.PERSPECTIVE -> PerspectiveStep(ui, viewModel)
                    CheckInStep.PLAN -> PlanStep(ui, viewModel)
                    CheckInStep.RESULT -> ResultStep(ui, viewModel, onOpenMindfulness)
                }
            }
        }
    }
}

// ─── Step 1: ¿Qué sientes? (3 x 3 grid + "Otro") ─────────────────────────────

@Composable
private fun EmotionStep(
    ui: CheckInUiState,
    vm: CheckInViewModel
) {
    var showOther by remember { mutableStateOf(false) }
    var otherName by remember { mutableStateOf("") }
    var otherFamily by remember { mutableStateOf(EmotionFamily.DIFFICULT) }

    StepTitle(stringResource(R.string.w_feel_title), stringResource(R.string.w_feel_subtitle))
    Spacer(Modifier.height(24.dp))
    vm.emotions.chunked(3).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            row.forEach { emotion ->
                EmotionChip(
                    emotion = emotion,
                    isSelected = emotion == ui.selectedEmotion,
                    onSelected = vm::selectEmotion,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // "OTRO:" from the worksheet
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (showOther) Accent.copy(alpha = 0.10f) else Color.White)
            .border(1.dp, if (showOther) Accent else Color(0xFFEEEEF5), RoundedCornerShape(16.dp))
            .clickable { showOther = !showOther }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("💭 " + stringResource(R.string.w_other_name), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Ink)
    }

    if (showOther) {
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = otherName,
            onValueChange = { otherName = it.take(30) },
            placeholder = { Text(stringResource(R.string.w_other_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.w_other_family_q), fontSize = 13.sp, color = Muted)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                EmotionFamily.POSITIVE to R.string.w_fam_pos,
                EmotionFamily.DIFFICULT to R.string.w_fam_dif,
                EmotionFamily.ACTIVATED to R.string.w_fam_act
            ).forEach { (family, label) ->
                FilterChip(
                    selected = otherFamily == family,
                    onClick = { otherFamily = family },
                    label = { Text(stringResource(label), fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = family.color.copy(alpha = 0.2f),
                        selectedLabelColor = Ink
                    )
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        PrimaryButton(
            text = stringResource(R.string.w_continue),
            color = Accent,
            enabled = otherName.isNotBlank(),
            onClick = { vm.selectCustomEmotion(otherName, otherFamily) }
        )
    }
}

// ─── Step 2: ¿Por qué te sientes así? ─────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntensityContextStep(ui: CheckInUiState, vm: CheckInViewModel) {
    val emotion = ui.selectedEmotion ?: return
    SelectedEmotionHeader(emotion)
    Spacer(Modifier.height(24.dp))

    IntensitySlider(
        value = ui.intensity,
        accentColor = emotion.color,
        onValueChange = vm::setIntensity,
        title = stringResource(R.string.checkin_intensity)
    )

    // Worksheet step 1 asked which emotion is hardest to control: we capture it per check-in.
    if (!ui.isPositive) {
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.w_hard_q),
                fontSize = 14.sp,
                color = Ink,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = ui.hardToControl,
                onCheckedChange = vm::setHardToControl,
                colors = SwitchDefaults.colors(checkedTrackColor = emotion.color)
            )
        }
    }

    Spacer(Modifier.height(28.dp))
    Text(stringResource(R.string.w_context_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
    Text(stringResource(R.string.w_context_subtitle), fontSize = 13.sp, color = Muted)
    Spacer(Modifier.height(12.dp))

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Trigger.entries.forEach { trigger ->
            val selected = trigger.id in ui.triggers
            FilterChip(
                selected = selected,
                onClick = { vm.toggleTrigger(trigger.id) },
                label = { Text("${trigger.emoji} ${stringResource(trigger.labelResId)}", fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = emotion.color.copy(alpha = 0.18f),
                    selectedLabelColor = Ink
                )
            )
        }
    }

    Spacer(Modifier.height(32.dp))
    PrimaryButton(text = stringResource(R.string.w_continue), color = emotion.color, onClick = vm::next)
}

// ─── Step 3: ¿Cómo lo harías ahora? ───────────────────────────────────────────

@Composable
private fun ReflectStep(ui: CheckInUiState, vm: CheckInViewModel) {
    val emotion = ui.selectedEmotion ?: return

    if (ui.isPositive) {
        // The worksheet only covers difficult emotions; for good moments we keep one gentle question.
        StepTitle(stringResource(R.string.w_reflect_title), stringResource(R.string.w_reflect_subtitle))
        Spacer(Modifier.height(20.dp))
        QuestionBox(stringResource(R.string.w_reflect_q_positive), emotion.color)
        Spacer(Modifier.height(16.dp))
        TextArea(ui.note, vm::setNote, stringResource(R.string.w_reflect_hint), height = 150.dp)
    } else {
        StepTitle(stringResource(R.string.w_how_title), stringResource(R.string.w_how_subtitle))
        Spacer(Modifier.height(20.dp))
        QuestionBox(stringResource(R.string.w_how_q1), emotion.color)
        Spacer(Modifier.height(10.dp))
        TextArea(ui.note, vm::setNote, stringResource(R.string.w_reflect_hint), height = 120.dp)
        Spacer(Modifier.height(20.dp))
        QuestionBox(stringResource(R.string.w_how_q2), emotion.color)
        Spacer(Modifier.height(10.dp))
        TextArea(ui.couldHaveDone, vm::setCouldHaveDone, stringResource(R.string.w_reflect_hint), height = 120.dp)
    }

    Spacer(Modifier.height(28.dp))
    PrimaryButton(
        text = stringResource(if (ui.isPositive) R.string.w_see_note else R.string.w_continue),
        color = emotion.color,
        onClick = vm::next
    )
}

// ─── Another angle: what could help you see it differently? ───────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PerspectiveStep(ui: CheckInUiState, vm: CheckInViewModel) {
    val emotion = ui.selectedEmotion ?: return
    val ideas = listOf(
        R.string.w_persp_c1, R.string.w_persp_c2, R.string.w_persp_c3, R.string.w_persp_c4, R.string.w_persp_c5
    ).map { stringResource(it) }

    StepTitle(stringResource(R.string.w_persp_title), stringResource(R.string.w_persp_subtitle))
    Spacer(Modifier.height(20.dp))

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ideas.forEach { text ->
            FilterChip(
                selected = ui.perspective == text,
                onClick = { vm.setPerspective(text) },
                label = { Text(text, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = emotion.color.copy(alpha = 0.18f),
                    selectedLabelColor = Ink
                )
            )
        }
    }

    Spacer(Modifier.height(16.dp))
    TextArea(ui.perspective, vm::setPerspective, stringResource(R.string.w_persp_hint), height = 120.dp)

    Spacer(Modifier.height(28.dp))
    PrimaryButton(text = stringResource(R.string.w_continue), color = emotion.color, onClick = vm::next)
}

// ─── Last step (worksheet step 5): ¿Qué quieres lograr? ──────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanStep(ui: CheckInUiState, vm: CheckInViewModel) {
    val emotion = ui.selectedEmotion ?: return
    val suggestions = listOf(
        R.string.w_plan_s1, R.string.w_plan_s2, R.string.w_plan_s3, R.string.w_plan_s4, R.string.w_plan_s5
    ).map { stringResource(it) }

    StepTitle(stringResource(R.string.w_goal_title), stringResource(R.string.w_goal_subtitle))
    Spacer(Modifier.height(20.dp))

    // 1) La próxima vez que sienta esto:
    FieldLabel(stringResource(R.string.w_plan_cue), emotion.color)
    OutlinedTextField(
        value = ui.planCue,
        onValueChange = vm::setPlanCue,
        placeholder = { Text(stringResource(R.string.w_plan_cue_hint)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        minLines = 2
    )

    // 2) En lugar de hacer esto:
    Spacer(Modifier.height(16.dp))
    FieldLabel(stringResource(R.string.w_plan_instead), emotion.color)
    OutlinedTextField(
        value = ui.planInstead,
        onValueChange = vm::setPlanInstead,
        placeholder = { Text(stringResource(R.string.w_plan_instead_hint)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        minLines = 2
    )

    // 3) Haré esto:
    Spacer(Modifier.height(16.dp))
    FieldLabel(stringResource(R.string.w_plan_will), emotion.color)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        suggestions.forEach { text ->
            FilterChip(
                selected = ui.plan == text,
                onClick = { vm.setPlan(text) },
                label = { Text(text, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = emotion.color.copy(alpha = 0.18f),
                    selectedLabelColor = Ink
                )
            )
        }
    }
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(
        value = ui.plan,
        onValueChange = vm::setPlan,
        placeholder = { Text(stringResource(R.string.w_plan_hint)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        minLines = 2
    )

    Spacer(Modifier.height(28.dp))
    PrimaryButton(text = stringResource(R.string.w_see_note), color = emotion.color, onClick = vm::next)
}

@Composable
private fun QuestionBox(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(16.dp)
    ) { Text(text, fontSize = 15.sp, color = Ink, lineHeight = 22.sp) }
}

@Composable
private fun TextArea(value: String, onChange: (String) -> Unit, hint: String, height: androidx.compose.ui.unit.Dp) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(hint) },
        modifier = Modifier.fillMaxWidth().height(height),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun FieldLabel(text: String, color: Color) {
    Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.padding(bottom = 6.dp))
}

// ─── Result: AI note + "how do you feel now?" ─────────────────────────────────

@Composable
private fun ResultStep(ui: CheckInUiState, vm: CheckInViewModel, onOpenMindfulness: () -> Unit) {
    val emotion = ui.selectedEmotion ?: return
    StepTitle(stringResource(R.string.w_result_title), null)
    Spacer(Modifier.height(16.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, emotion.color.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        when {
            ui.showCrisisCard -> {
                Text(stringResource(R.string.w_crisis_title), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.w_crisis_body), fontSize = 15.sp, color = Ink, lineHeight = 22.sp)
            }
            ui.aiLoading -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = emotion.color)
                    Spacer(Modifier.size(12.dp))
                    Text(stringResource(R.string.w_result_loading), fontSize = 14.sp, color = Muted)
                }
            }
            else -> {
                Text(
                    text = if (ui.aiFailed || ui.aiComment.isBlank()) stringResource(R.string.w_ai_fallback) else ui.aiComment,
                    fontSize = 16.sp,
                    color = Ink,
                    lineHeight = 24.sp
                )
                if (!ui.aiFailed && ui.aiComment.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.w_ai_disclaimer), fontSize = 11.sp, color = Muted, lineHeight = 15.sp)
                }
            }
        }
    }

    // "How strong is it now?" only makes sense for difficult emotions (it measures relief), and never
    // while a crisis message is on screen or the note is still loading.
    if (!ui.aiLoading && !ui.showCrisisCard && !ui.isPositive) {
        Spacer(Modifier.height(28.dp))
        IntensitySlider(
            value = ui.finalIntensity,
            accentColor = emotion.color,
            onValueChange = vm::setFinalIntensity,
            title = stringResource(R.string.w_after_title)
        )

        run {
            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = onOpenMindfulness,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text(stringResource(R.string.w_mindful_cta), color = Accent) }
        }
    }

    Spacer(Modifier.height(16.dp))
    PrimaryButton(
        text = stringResource(R.string.w_finish),
        color = emotion.color,
        enabled = !ui.aiLoading,
        onClick = vm::finish
    )
}

// ─── Shared bits ──────────────────────────────────────────────────────────────

@Composable
private fun StepTitle(title: String, subtitle: String?) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 14.sp, color = Muted)
        }
    }
}

@Composable
private fun SelectedEmotionHeader(emotion: Emotion) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.size(52.dp).clip(CircleShape).background(emotion.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Text(emotion.emoji, fontSize = 26.sp) }
        Column {
            val isCustom = emotion.id == "other"
            Text(
                if (isCustom) emotion.name else stringResource(emotion.nameResId),
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink
            )
            if (!isCustom) Text(stringResource(emotion.descResId), fontSize = 13.sp, color = Muted)
        }
    }
}

@Composable
private fun PrimaryButton(text: String, color: Color, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) { Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
}

@Composable
fun EmotionChip(
    emotion: Emotion,
    isSelected: Boolean,
    onSelected: (Emotion) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "scale"
    )
    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) emotion.color.copy(alpha = 0.18f) else Color.White)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) emotion.color else Color(0xFFEEEEF5),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelected(emotion) }
            .padding(vertical = 18.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emotion.emoji, fontSize = 34.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(emotion.nameResId),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) emotion.color else Color(0xFF333355),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun IntensitySlider(
    value: Int,
    accentColor: Color,
    onValueChange: (Int) -> Unit,
    title: String = stringResource(R.string.checkin_intensity)
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Ink)
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.checkin_mild), fontSize = 11.sp, color = Color(0xFFAAAABB))
            Text(stringResource(R.string.checkin_intense), fontSize = 11.sp, color = Color(0xFFAAAABB))
        }
    }
}
