package com.tradecheck.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradecheck.app.data.ChecklistItem
import com.tradecheck.app.data.ChecklistManager
import com.tradecheck.app.ui.theme.*
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TradeCheckTheme {
                TradeCheckApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeCheckApp() {
    val context = LocalContext.current
    val manager = remember { ChecklistManager(context) }

    var isPersian by remember { mutableStateOf(manager.isPersian()) }
    var items by remember { mutableStateOf(manager.loadItems()) }
    var newRuleText by remember { mutableStateOf("") }

    // R:R Calculator state
    var entryPrice by remember { mutableStateOf("") }
    var stopLossPrice by remember { mutableStateOf("") }
    var targetPrice by remember { mutableStateOf("") }

    val layoutDirection = if (isPersian) LayoutDirection.Rtl else LayoutDirection.Ltr

    fun save(updated: List<ChecklistItem>) {
        items = updated
        manager.saveItems(updated)
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPersian) "تریدچک | دستیار چک‌لیست" else "TradeCheck",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    },
                    actions = {
                        // Reset Checkbox state
                        IconButton(onClick = {
                            val reset = items.map { it.copy(isChecked = false) }
                            save(reset)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = if (isPersian) "ریست چک‌لیست" else "Reset",
                                tint = TextSecondary
                            )
                        }

                        // Toggle Language
                        TextButton(onClick = {
                            val newLang = !isPersian
                            isPersian = newLang
                            manager.setPersian(newLang)
                        }) {
                            Text(
                                text = if (isPersian) "EN" else "فا",
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                fontSize = 14.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
                )
            },
            containerColor = DarkBg
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Status Progress Card
                item {
                    val total = items.size
                    val checkedCount = items.count { it.isChecked }
                    val progress = if (total > 0) checkedCount.toFloat() / total.toFloat() else 0f
                    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")
                    val isReady = total > 0 && checkedCount == total

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isReady) EmeraldPrimary else CardBorder, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isPersian) "وضعیت آمادگی معامله" else "Trade Readiness",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "$checkedCount / $total",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isReady) EmeraldPrimary else TextSecondary,
                                    fontSize = 15.sp
                                )
                            }

                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isReady) EmeraldPrimary else EmeraldLight,
                                trackColor = DarkBg
                            )

                            // Status Banner
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isReady) EmeraldDark.copy(alpha = 0.25f) else AmberWarning.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isReady) EmeraldPrimary else AmberWarning
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (isReady) {
                                            if (isPersian) "مجوز ورود به معامله صادر شد 🚀" else "Ready to Trade! 🚀"
                                        } else {
                                            if (isPersian) "صبر کنید - شرایط معامله هنوز کامل نیست ⚠️" else "Wait for Setup - Incomplete checks ⚠️"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isReady) EmeraldLight else AmberWarning,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Pre-Trade Checklist Header
                item {
                    Text(
                        text = if (isPersian) "چک‌لیست قبل از ورود (Pre-Trade)" else "Pre-Trade Checklist",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }

                // Checklist Items
                items(items, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val updated = items.map {
                                    if (it.id == item.id) it.copy(isChecked = !it.isChecked) else it
                                }
                                save(updated)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isChecked) CardBg.copy(alpha = 0.6f) else CardBg
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (item.isChecked) EmeraldPrimary.copy(alpha = 0.4f) else CardBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { checked ->
                                    val updated = items.map {
                                        if (it.id == item.id) it.copy(isChecked = checked) else it
                                    }
                                    save(updated)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = EmeraldPrimary,
                                    uncheckedColor = TextSecondary,
                                    checkmarkColor = DarkBg
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = if (isPersian) item.textFa else item.textEn,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (item.isChecked) TextMuted else TextPrimary,
                                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                            )

                            if (item.isCustom) {
                                IconButton(
                                    onClick = {
                                        val updated = items.filterNot { it.id == item.id }
                                        save(updated)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = RoseError.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Add Custom Rule Box
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newRuleText,
                            onValueChange = { newRuleText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    text = if (isPersian) "افزودن قانون شخصی جدید..." else "Add custom rule...",
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = CardBg,
                                unfocusedContainerColor = CardBg
                            )
                        )

                        FilledIconButton(
                            onClick = {
                                if (newRuleText.isNotBlank()) {
                                    val newItem = ChecklistItem(
                                        id = UUID.randomUUID().toString(),
                                        textEn = newRuleText.trim(),
                                        textFa = newRuleText.trim(),
                                        isChecked = false,
                                        isCustom = true
                                    )
                                    save(items + newItem)
                                    newRuleText = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = DarkBg
                            )
                        }
                    }
                }

                // 4. Quick Risk : Reward Calculator Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (isPersian) "ماشین‌حساب ریسک به ریوارد (R:R)" else "Quick Risk : Reward Calculator",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = entryPrice,
                                    onValueChange = { entryPrice = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text(if (isPersian) "ورود" else "Entry", fontSize = 11.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EmeraldPrimary,
                                        unfocusedBorderColor = CardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedLabelColor = EmeraldPrimary,
                                        unfocusedLabelColor = TextSecondary
                                    )
                                )

                                OutlinedTextField(
                                    value = stopLossPrice,
                                    onValueChange = { stopLossPrice = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text(if (isPersian) "استاپ" else "Stop", fontSize = 11.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = RoseError,
                                        unfocusedBorderColor = CardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedLabelColor = RoseError,
                                        unfocusedLabelColor = TextSecondary
                                    )
                                )

                                OutlinedTextField(
                                    value = targetPrice,
                                    onValueChange = { targetPrice = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text(if (isPersian) "تارگت" else "Target", fontSize = 11.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EmeraldPrimary,
                                        unfocusedBorderColor = CardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedLabelColor = EmeraldPrimary,
                                        unfocusedLabelColor = TextSecondary
                                    )
                                )
                            }

                            // Calculate R:R
                            val entry = entryPrice.toDoubleOrNull()
                            val sl = stopLossPrice.toDoubleOrNull()
                            val tp = targetPrice.toDoubleOrNull()

                            if (entry != null && sl != null && tp != null && entry != sl) {
                                val risk = kotlin.math.abs(entry - sl)
                                val reward = kotlin.math.abs(tp - entry)
                                val ratio = if (risk > 0) reward / risk else 0.0
                                val isGoodRatio = ratio >= 2.0

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isGoodRatio) EmeraldDark.copy(alpha = 0.2f) else RoseError.copy(alpha = 0.15f))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isPersian) "نسبت R:R :" else "Ratio R:R :",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = String.format("1 : %.2f  %s", ratio, if (isGoodRatio) "✅" else "⚠️"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isGoodRatio) EmeraldLight else RoseError
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom padding
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
