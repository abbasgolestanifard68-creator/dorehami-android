package ir.dorehami.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.dorehami.app.util.Jalali

private val weekdayLetters = listOf("ش", "ی", "د", "س", "چ", "پ", "ج") // شنبه تا جمعه

/** فیلد انتخاب تاریخ شمسی؛ با لمس آن، تقویم انتخاب باز می‌شود. */
@Composable
fun JalaliDateField(value: Triple<Int, Int, Int>?, onChange: (Triple<Int, Int, Int>) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { open = true }, modifier = modifier.height(56.dp)) {
        Text(
            if (value == null) "تاریخ تولد (شمسی) را انتخاب کنید"
            else "تاریخ تولد: " + Jalali.toPersianDigits(Jalali.format(value.first, value.second, value.third))
        )
    }
    if (open) {
        JalaliDateDialog(value, onDismiss = { open = false }) { onChange(it); open = false }
    }
}

@Composable
private fun JalaliDateDialog(initial: Triple<Int, Int, Int>?, onDismiss: () -> Unit, onPick: (Triple<Int, Int, Int>) -> Unit) {
    val today = remember { Jalali.today() }
    var year by remember { mutableStateOf(initial?.first ?: (today.first - 25)) }
    var month by remember { mutableStateOf(initial?.second ?: 1) }
    var day by remember { mutableStateOf(initial?.third) }
    val years = remember { (today.first downTo 1300).toList() }
    val len = Jalali.monthLength(year, month)
    val offset = Jalali.firstWeekday(year, month)
    val rows = (offset + len + 6) / 7

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تاریخ تولد (شمسی)") },
        text = {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PickerMenu(
                        label = Jalali.toPersianDigits(year.toString()),
                        items = years,
                        itemText = { Jalali.toPersianDigits(it.toString()) },
                        modifier = Modifier.weight(1f),
                    ) {
                        year = it
                        if ((day ?: 0) > Jalali.monthLength(it, month)) day = null
                    }
                    PickerMenu(
                        label = Jalali.monthNames[month - 1],
                        items = (1..12).toList(),
                        itemText = { Jalali.monthNames[it - 1] },
                        modifier = Modifier.weight(1f),
                    ) {
                        month = it
                        if ((day ?: 0) > Jalali.monthLength(year, it)) day = null
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth()) {
                    weekdayLetters.forEach {
                        Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                    }
                }
                for (r in 0 until rows) {
                    Row(Modifier.fillMaxWidth()) {
                        for (c in 0 until 7) {
                            val d = r * 7 + c - offset + 1
                            if (d in 1..len) {
                                DayCell(
                                    d = d,
                                    selected = d == day,
                                    enabled = !Jalali.isAfter(year, month, d, today),
                                    modifier = Modifier.weight(1f),
                                ) { day = d }
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = day != null, onClick = { day?.let { onPick(Triple(year, month, it)) } }) { Text("تأیید") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}

@Composable
private fun DayCell(d: Int, selected: Boolean, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier.aspectRatio(1f).padding(2.dp).clip(CircleShape)
            .background(if (selected) scheme.primary else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            Jalali.toPersianDigits(d.toString()),
            color = when {
                selected -> scheme.onPrimary
                !enabled -> scheme.onSurface.copy(alpha = 0.38f)
                else -> scheme.onSurface
            },
        )
    }
}

@Composable
private fun <T> PickerMenu(label: String, items: List<T>, itemText: (T) -> String, modifier: Modifier, onPick: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text("$label ▾") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(text = { Text(itemText(item)) }, onClick = { expanded = false; onPick(item) })
            }
        }
    }
}
