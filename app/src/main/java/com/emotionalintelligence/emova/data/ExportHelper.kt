package com.emotionalintelligence.emova.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.emotionalintelligence.emova.model.CheckIn
import com.emotionalintelligence.emova.model.JournalEntry
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object ExportHelper {

    fun exportToCsv(
        context: Context,
        checkIns: List<CheckIn>,
        journalEntries: List<JournalEntry>
    ) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()

        // Check-ins section
        sb.appendLine("=== CHECK-INS ===")
        sb.appendLine("Date,Emotion,Family,Intensity,Note")
        checkIns.forEach { c ->
            val date = dateFormat.format(Date(c.timestamp))
            sb.appendLine("\"$date\",\"${c.emotionName}\",\"${c.emotionFamily}\",${c.intensity},\"${c.note}\"")
        }

        sb.appendLine()
        sb.appendLine("=== JOURNAL ENTRIES ===")
        sb.appendLine("Date,Emotion,Prompt,Answer")
        journalEntries.forEach { j ->
            val date = dateFormat.format(Date(j.timestamp))
            sb.appendLine("\"$date\",\"${j.emotionName}\",\"${j.prompt}\",\"${j.answer}\"")
        }

        // Write to file
        val fileName = "emova_export_${
            SimpleDateFormat(
                "yyyyMMdd",
                Locale.getDefault()
            ).format(Date())}.csv"
        val file = File(context.cacheDir, fileName)
        file.writeText(sb.toString())

        // Share via intent
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            setDataAndType(uri, "text/csv")
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Emova Journal Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Export Journal"))
    }
}