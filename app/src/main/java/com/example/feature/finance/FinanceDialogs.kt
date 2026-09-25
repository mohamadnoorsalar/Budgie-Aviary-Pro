package com.example.feature.finance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.database.entity.ExpenseCategory
import com.example.data.database.entity.ExpenseEntity
import com.example.data.database.entity.IncomeCategory
import com.example.data.database.entity.IncomeEntity
import java.util.UUID

@Composable
fun AddExpenseDialog(
    isPersian: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (ExpenseEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var amountText by remember { mutableStateOf("") }
    var pairId by remember { mutableStateOf("") }
    var birdRing by remember { mutableStateOf("") }
    var receiptNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        ExpenseCategory.BIRD_PURCHASE to if (isPersian) "خرید پرنده جدید" else "Bird Purchase",
        ExpenseCategory.FOOD to if (isPersian) "هزینه دان و بذر" else "Food Costs",
        ExpenseCategory.SUPPLEMENTS to if (isPersian) "مکمل‌ها و ویتامین" else "Supplement Costs",
        ExpenseCategory.MEDICINE to if (isPersian) "دارو و ویزیت دامپزشک" else "Medicine Costs",
        ExpenseCategory.EQUIPMENT to if (isPersian) "تجهیزات، قفس و لانه" else "Equipment Costs",
        ExpenseCategory.OTHER to if (isPersian) "سایر هزینه‌های سالن" else "Other Expenses"
    )

    val amount = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.TrendingDown, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isPersian) "ثبت هزینه جدید" else "Record New Expense")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isPersian) "عنوان هزینه *" else "Expense Title *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_expense_title")
                )

                // Category selector
                Box {
                    OutlinedTextField(
                        value = categories.find { it.first == category }?.second ?: category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isPersian) "دسته‌بندی هزینه *" else "Expense Category *") },
                        trailingIcon = {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { categoryDropdownExpanded = true })
                        },
                        modifier = Modifier.fillMaxWidth().clickable { categoryDropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { (catKey, catLabel) ->
                            DropdownMenuItem(
                                text = { Text(catLabel) },
                                onClick = {
                                    category = catKey
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (isPersian) "مبلغ هزینه ($) *" else "Amount ($) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_expense_amount")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pairId,
                        onValueChange = { pairId = it },
                        label = { Text(if (isPersian) "کد جفت (اختیاری)" else "Pair ID (optional)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = birdRing,
                        onValueChange = { birdRing = it },
                        label = { Text(if (isPersian) "شماره حلقه (اختیاری)" else "Bird Ring (optional)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = receiptNumber,
                    onValueChange = { receiptNumber = it },
                    label = { Text(if (isPersian) "شماره فاکتور / رسید" else "Invoice / Receipt #") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isPersian) "توضیحات" else "Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && amount > 0) {
                        val exp = ExpenseEntity(
                            id = UUID.randomUUID().toString(),
                            title = title.trim(),
                            category = category,
                            amount = amount,
                            pairId = pairId.takeIf { it.isNotBlank() },
                            birdRingNumber = birdRing.takeIf { it.isNotBlank() },
                            receiptNumber = receiptNumber.takeIf { it.isNotBlank() },
                            notes = notes.takeIf { it.isNotBlank() }
                        )
                        onConfirm(exp)
                    }
                },
                enabled = title.isNotBlank() && amount > 0,
                modifier = Modifier.testTag("btn_confirm_save_expense")
            ) {
                Text(if (isPersian) "ثبت هزینه" else "Save Expense")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isPersian) "انصراف" else "Cancel")
            }
        }
    )
}

@Composable
fun AddIncomeDialog(
    isPersian: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (IncomeEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(IncomeCategory.BIRD_SALE) }
    var amountText by remember { mutableStateOf("") }
    var soldRingNumber by remember { mutableStateOf("") }
    var buyerName by remember { mutableStateOf("") }
    var buyerContact by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        IncomeCategory.BIRD_SALE to if (isPersian) "فروش پرنده بالغ" else "Bird Sales",
        IncomeCategory.CHICK_SALE to if (isPersian) "فروش جوجه سرلاکی/دانخور" else "Chick Sales",
        IncomeCategory.OTHER to if (isPersian) "سایر درآمدهای سالن" else "Other Income"
    )

    val amount = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isPersian) "ثبت درآمد و دریافتی" else "Record New Income")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isPersian) "عنوان درآمد *" else "Income Title *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_income_title")
                )

                // Category selector
                Box {
                    OutlinedTextField(
                        value = categories.find { it.first == category }?.second ?: category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isPersian) "دسته‌بندی درآمد *" else "Income Category *") },
                        trailingIcon = {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { categoryDropdownExpanded = true })
                        },
                        modifier = Modifier.fillMaxWidth().clickable { categoryDropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { (catKey, catLabel) ->
                            DropdownMenuItem(
                                text = { Text(catLabel) },
                                onClick = {
                                    category = catKey
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (isPersian) "مبلغ درآمد ($) *" else "Amount ($) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_income_amount")
                )

                OutlinedTextField(
                    value = soldRingNumber,
                    onValueChange = { soldRingNumber = it },
                    label = { Text(if (isPersian) "شماره حلقه پرنده فروخته‌شده" else "Sold Bird Ring Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = buyerName,
                        onValueChange = { buyerName = it },
                        label = { Text(if (isPersian) "نام خریدار" else "Buyer Name") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = buyerContact,
                        onValueChange = { buyerContact = it },
                        label = { Text(if (isPersian) "شماره تماس" else "Buyer Contact") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isPersian) "توضیحات" else "Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && amount > 0) {
                        val inc = IncomeEntity(
                            id = UUID.randomUUID().toString(),
                            title = title.trim(),
                            category = category,
                            amount = amount,
                            soldBirdRingNumber = soldRingNumber.takeIf { it.isNotBlank() },
                            buyerName = buyerName.takeIf { it.isNotBlank() },
                            buyerContact = buyerContact.takeIf { it.isNotBlank() },
                            notes = notes.takeIf { it.isNotBlank() }
                        )
                        onConfirm(inc)
                    }
                },
                enabled = title.isNotBlank() && amount > 0,
                modifier = Modifier.testTag("btn_confirm_save_income")
            ) {
                Text(if (isPersian) "ثبت درآمد" else "Save Income")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isPersian) "انصراف" else "Cancel")
            }
        }
    )
}
