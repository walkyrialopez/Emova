package com.emotionalintelligence.emova.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import java.util.Calendar

@Composable
fun WellnessTipOfTheDay() {
    val titles = stringArrayResource(R.array.wellness_tip_titles)
    val bodies = stringArrayResource(R.array.wellness_tip_bodies)

    val dayOfYear = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    val index = dayOfYear % titles.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFF8ED))
            .border(1.dp, Color(0xFFE8A020).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🌱", fontSize = 18.sp)
            Text(
                text = stringResource(R.string.wellness_tip_label),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE8A020),
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = titles[index],
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = bodies[index],
            fontSize = 14.sp,
            color = Color(0xFF6B6B8A),
            lineHeight = 21.sp
        )
    }
}