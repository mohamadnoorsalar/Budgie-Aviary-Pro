package com.example.feature.inventory

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.database.entity.InventoryCategory
import com.example.data.database.entity.InventoryItemEntity
import java.util.UUID

@Composable
fun AddEditInventoryItemDialog(
    itemToEdit: InventoryItemEntity? = null,
    isPersian: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (InventoryItemEntity) -> Unit
) {
    var name by remember { mutableStateOf(itemToEdit?.name ?: "") }
    var sku by remember { mutableStateOf(itemToEdit?.sku ?: "SKU-${System.currentTimeMillis() % 10000}") }
    var category by remember { mutableStateOf(itemToEdit?.category ?: InventoryCategory.FOOD) }
    var currentStock by remember { mutableStateOf(itemToEdit?.currentStock?.toString() ?: "0.0") }
    var unit by remember { mutableStateOf(itemToEdit?.unit ?: "KG") }
    var minStock by remember { mutableStateOf(itemToEdit?.minStockThreshold?.toString() ?: "2.0") }
    var costPerUnit by remember { mutableStateOf(itemToEdit?.costPerUnit?.toString() ?: "0.0") }
    var location by remember { mutableStateOf(itemToEdit?.storageLocation ?: "") }
    var notes by remember { mutableStateOf(itemToEdit?.notes ?: "") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var unitDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        InventoryCategory.FOOD to if (isPersian) "دان و خوراک" else "Food",
        InventoryCategory.SUPPLEMENTS to if (isPersian) "مکمل‌ها و ویتامین" else "Supplements",
        InventoryCategory.MEDICINE to if (isPersian) "دارو و بهداشت" else "Medicine",
        InventoryCategory.EQUIPMENT to if (isPersian) "تجهیزات و قفس" else "Equipment",
        InventoryCategory.CONSUMABLES to if (isPersian) "ملزومات مصرفی" else "Consumables"
    )

    val units = listOf("KG", "G", "PIECES", "LITER", "ML", "PACK")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Inventory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (itemToEdit == null) {
                        if (isPersian) "افزودن کالا به انبار" else "Add Stock Item"
                    } else {
                        if (isPersian) "ویرایش کالا در انبار" else "Edit Stock Item"
                    }
                )
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
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isPersian) "نام کالا *" else "Item Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_item_name")
                )

                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text(if (isPersian) "کد کالا (SKU) *" else "SKU Code *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_item_sku")
                )

                // Category selector
                Box {
                    OutlinedTextField(
                        value = categories.find { it.first == category }?.second ?: category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isPersian) "دسته‌بندی *" else "Category *") },
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = currentStock,
                        onValueChange = { currentStock = it },
                        label = { Text(if (isPersian) "موجودی فعلی" else "Current Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1.2f).testTag("input_current_stock")
                    )

                    // Unit dropdown
                    Box(modifier = Modifier.weight(0.8f)) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isPersian) "واحد" else "Unit") },
                            trailingIcon = {
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { unitDropdownExpanded = true })
                            },
                            modifier = Modifier.fillMaxWidth().clickable { unitDropdownExpanded = true }
                        )
                        DropdownMenu(
                            expanded = unitDropdownExpanded,
                            onDismissRequest = { unitDropdownExpanded = false }
                        ) {
                            units.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unit = u
                                        unitDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minStock,
                        onValueChange = { minStock = it },
                        label = { Text(if (isPersian) "حداقل موجودی (هشدار)" else "Min Stock Alert") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("input_min_stock")
                    )

                    OutlinedTextField(
                        value = costPerUnit,
                        onValueChange = { costPerUnit = it },
                        label = { Text(if (isPersian) "قیمت هر واحد ($)" else "Cost/Unit ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("input_cost_unit")
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(if (isPersian) "محل نگهداری / قفسه" else "Storage Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isPersian) "یادداشت‌ها" else "Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && sku.isNotBlank()) {
                        val item = (itemToEdit ?: InventoryItemEntity(
                            id = UUID.randomUUID().toString(),
                            sku = sku.trim(),
                            name = name.trim()
                        )).copy(
                            sku = sku.trim(),
                            name = name.trim(),
                            category = category,
                            currentStock = currentStock.toDoubleOrNull() ?: 0.0,
                            unit = unit,
                            minStockThreshold = minStock.toDoubleOrNull() ?: 0.0,
                            costPerUnit = costPerUnit.toDoubleOrNull() ?: 0.0,
                            storageLocation = location.takeIf { it.isNotBlank() },
                            notes = notes.takeIf { it.isNotBlank() },
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(item)
                    }
                },
                enabled = name.isNotBlank() && sku.isNotBlank(),
                modifier = Modifier.testTag("btn_save_item")
            ) {
                Text(if (isPersian) "ذخیره کالا" else "Save Item")
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
fun StockInDialog(
    item: InventoryItemEntity,
    isPersian: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Double, unitPrice: Double, notes: String?, createExpense: Boolean) -> Unit
) {
    var quantityText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf(item.costPerUnit.toString()) }
    var notes by remember { mutableStateOf("") }
    var createExpense by remember { mutableStateOf(true) }

    val qty = quantityText.toDoubleOrNull() ?: 0.0
    val price = priceText.toDoubleOrNull() ?: 0.0
    val totalCost = qty * price

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AddShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isPersian) "ورود کالا به انبار (شارژ موجودی)" else "Stock In (Restock)")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${item.name} (${item.sku})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isPersian) "موجودی فعلی: ${item.currentStock} ${item.unit}" else "Current Stock: ${item.currentStock} ${item.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text(if (isPersian) "مقدار وارده (${item.unit}) *" else "Quantity (${item.unit}) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_stock_in_qty")
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text(if (isPersian) "قیمت خرید هر واحد ($)" else "Cost per Unit ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                if (totalCost > 0) {
                    Text(
                        text = if (isPersian) "مجموع هزینه: $${"%.2f".format(totalCost)}" else "Total Cost: $${"%.2f".format(totalCost)}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = createExpense,
                        onCheckedChange = { createExpense = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPersian) "ثبت خودکار در بخش هزینه‌های مالی" else "Automatically record in Finance Expenses",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isPersian) "توضیحات خرید / فاکتور" else "Purchase / Invoice Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (qty > 0) {
                        onConfirm(qty, price, notes.takeIf { it.isNotBlank() }, createExpense)
                    }
                },
                enabled = qty > 0,
                modifier = Modifier.testTag("btn_confirm_stock_in")
            ) {
                Text(if (isPersian) "تأیید ورود به انبار" else "Confirm Stock In")
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
fun StockOutDialog(
    item: InventoryItemEntity,
    isPersian: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Double, reason: String, notes: String?) -> Unit
) {
    var quantityText by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf(if (isPersian) "ضایعات و خرابی" else "Damaged / Spoiled") }
    var notes by remember { mutableStateOf("") }

    val qty = quantityText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.RemoveShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isPersian) "خروج کالا از انبار" else "Stock Out (Deduct)")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${item.name} (${item.sku})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isPersian) "موجودی فعلی: ${item.currentStock} ${item.unit}" else "Current Stock: ${item.currentStock} ${item.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text(if (isPersian) "مقدار کسر (${item.unit}) *" else "Quantity to Deduct (${item.unit}) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_stock_out_qty")
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(if (isPersian) "علت خروج *" else "Reason *") },
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
                    if (qty > 0 && reason.isNotBlank()) {
                        onConfirm(qty, reason, notes.takeIf { it.isNotBlank() })
                    }
                },
                enabled = qty > 0 && reason.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_stock_out")
            ) {
                Text(if (isPersian) "تأیید خروج" else "Confirm Stock Out")
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
fun LogConsumptionDialog(
    item: InventoryItemEntity,
    isPersian: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Double, purpose: String, notes: String?) -> Unit
) {
    var quantityText by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf(if (isPersian) "تغذیه روزانه سالن" else "Daily Flock Feeding") }
    var notes by remember { mutableStateOf("") }

    val qty = quantityText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isPersian) "ثبت مصرف موجودی" else "Log Item Consumption")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${item.name} (${item.sku})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isPersian) "موجودی قابل استفاده: ${item.currentStock} ${item.unit}" else "Available Stock: ${item.currentStock} ${item.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text(if (isPersian) "مقدار مصرف شده (${item.unit}) *" else "Amount Consumed (${item.unit}) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_consume_qty")
                )

                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text(if (isPersian) "مورد مصرف / قفس *" else "Purpose / Target Cage *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isPersian) "توضیحات تکمیلی" else "Additional Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (qty > 0) {
                        onConfirm(qty, purpose, notes.takeIf { it.isNotBlank() })
                    }
                },
                enabled = qty > 0,
                modifier = Modifier.testTag("btn_confirm_consume")
            ) {
                Text(if (isPersian) "ثبت و کسر از انبار" else "Log & Deduct Stock")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isPersian) "انصراف" else "Cancel")
            }
        }
    )
}
