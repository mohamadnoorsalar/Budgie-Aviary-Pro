package com.example.feature.pedigree

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.common.BirdGender
import com.example.core.common.displayName
import com.example.core.genetics.GeneticsCalculator
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.pedigree.InteractivePedigreeTreeData
import com.example.core.pedigree.PedigreeNodeRole
import com.example.core.pedigree.PedigreeTreeNode
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.PedigreeRecordEntity
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedigreeScreen(
    modifier: Modifier = Modifier,
    initialBirdRing: String? = null,
    viewModel: PedigreeViewModel = viewModel(),
    onNavigateToBird: (String) -> Unit = {}
) {
    val currentLang = LocalAppLanguage.current
    val isFa = currentLang == AppLanguage.PERSIAN

    val allBirds by viewModel.allBirds.collectAsState()
    val allPedigrees by viewModel.allPedigrees.collectAsState()
    val selectedBirdRing by viewModel.selectedBirdRing.collectAsState()
    val treeData by viewModel.treeData.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedNodeDetail by viewModel.selectedNodeDetail.collectAsState()

    var birdSelectorExpanded by remember { mutableStateOf(false) }
    var showEditPedigreeDialog by remember { mutableStateOf(false) }

    // Synchronize initial selection if passed from bird profile
    remember(initialBirdRing) {
        if (!initialBirdRing.isNullOrBlank() && initialBirdRing != selectedBirdRing) {
            viewModel.selectBird(initialBirdRing)
        }
        true
    }

    val tabs = listOf(
        if (isFa) "درخت اجداد (والدین و اجداد)" else "Ancestors Tree",
        if (isFa) "فرزندان و نوه‌ها (Descendants)" else "Descendants & Future",
        if (isFa) "شناسنامه و ضرایب تبارشناسی" else "Lineage Certificate & COI"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("pedigree_screen")
    ) {
        // Top Bird Selection Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = birdSelectorExpanded,
                    onExpandedChange = { birdSelectorExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentBird = allBirds.firstOrNull { it.ringNumber == selectedBirdRing }
                    OutlinedTextField(
                        value = if (currentBird != null) {
                            "${currentBird.ringNumber} • ${currentBird.name ?: currentBird.color} (${currentBird.gender.name})"
                        } else {
                            if (isFa) "انتخاب پرنده برای نمایش شجره..." else "Select bird to view pedigree..."
                        },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = birdSelectorExpanded) },
                        leadingIcon = { Icon(Icons.Default.AccountTree, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        label = { Text(if (isFa) "پرنده اصلی شجره‌نامه" else "Subject Bird") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = birdSelectorExpanded,
                        onDismissRequest = { birdSelectorExpanded = false }
                    ) {
                        allBirds.forEach { bird ->
                            DropdownMenuItem(
                                text = {
                                    Text("${bird.ringNumber} - ${bird.name ?: bird.color} (${bird.variety?.displayName ?: bird.gender.name})")
                                },
                                onClick = {
                                    viewModel.selectBird(bird.ringNumber)
                                    birdSelectorExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        if (treeData != null) {
            val data = treeData!!

            // Generational Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { viewModel.setSelectedTab(index) },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTab) {
                0 -> AncestorsTreeTab(
                    data = data,
                    isFa = isFa,
                    onNodeClick = { node -> viewModel.selectNodeDetail(node) }
                )
                1 -> DescendantsTab(
                    data = data,
                    allBirds = allBirds,
                    isFa = isFa,
                    onNavigateToBird = onNavigateToBird,
                    onSelectSubject = { ring -> viewModel.selectBird(ring) }
                )
                2 -> LineageCertificateTab(
                    data = data,
                    isFa = isFa,
                    onEditPedigree = { showEditPedigreeDialog = true }
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isFa) "لطفاً برای رسم شجره‌نامه، یک پرنده را از منوی بالا انتخاب کنید." else "Please select a registered bird above to build an interactive genealogical pedigree tree.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Node Detail Inspection Dialog (Center tree on this bird or view profile)
    if (selectedNodeDetail != null) {
        val node = selectedNodeDetail!!
        AlertDialog(
            onDismissRequest = { viewModel.selectNodeDetail(null) },
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val sexIcon = if (node.gender == BirdGender.MALE) Icons.Default.Male else Icons.Default.Female
                    val sexColor = if (node.gender == BirdGender.MALE) Color(0xFF1976D2) else Color(0xFFE91E63)
                    Icon(sexIcon, contentDescription = null, tint = sexColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(node.ringNumber, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isFa) "نقش در شجره: ${node.role.labelFa}" else "Lineage Role: ${node.role.labelEn}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = "Name: ${node.name ?: "Unknown"}")
                    Text(text = "Color / Mutation: ${node.color} • ${node.mutation}")
                    Text(text = "Variety: ${node.variety?.displayName ?: "Standard"}")
                    Text(text = "Recorded in Aviary: ${if (node.isPresentInAviary) "YES" else "External Bloodline Record"}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.selectBird(node.ringNumber)
                        viewModel.selectNodeDetail(null)
                    }
                ) {
                    Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isFa) "انتقال مرکز شجره به این پرنده" else "Center Tree Here")
                }
            },
            dismissButton = {
                if (node.isPresentInAviary) {
                    OutlinedButton(
                        onClick = {
                            viewModel.selectNodeDetail(null)
                            onNavigateToBird(node.ringNumber)
                        }
                    ) {
                        Text(if (isFa) "مشاهده شناسنامه کامل" else "View Profile")
                    }
                }
            }
        )
    }

    // Edit Pedigree Record Dialog
    if (showEditPedigreeDialog && treeData != null) {
        val currentPed = treeData!!.pedigreeRecord
        val subjectRing = treeData!!.subjectBird.ringNumber
        EditPedigreeDialog(
            subjectRing = subjectRing,
            currentPedigree = currentPed,
            onDismiss = { showEditPedigreeDialog = false },
            onSave = { updated ->
                viewModel.savePedigree(updated)
                showEditPedigreeDialog = false
            }
        )
    }
}

// -------------------------------------------------------------------------
// TAB 1: INTERACTIVE MULTI-GENERATIONAL ANCESTORS TREE
// -------------------------------------------------------------------------
@Composable
private fun AncestorsTreeTab(
    data: InteractivePedigreeTreeData,
    isFa: Boolean,
    onNodeClick: (PedigreeTreeNode) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Subject Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isFa) "پرنده مبنا (Subject)" else "Lineage Root (Subject)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${data.subjectBird.ringNumber} ${data.subjectBird.name?.let { "($it)" } ?: ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${data.subjectBird.color} • ${data.subjectBird.mutation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text(
                        text = "${data.ancestryCompletenessPercent}% Complete",
                        color = Color.White,
                        modifier = Modifier.padding(4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // GENERATION 1: PARENTS
        Text(
            text = if (isFa) "نسل اول: والدین (Parents)" else "Generation 1: Parents (Sire & Dam)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PedigreeNodeCard(
                node = data.sireNode,
                fallbackRole = PedigreeNodeRole.SIRE,
                isFa = isFa,
                modifier = Modifier.weight(1f),
                onClick = { data.sireNode?.let(onNodeClick) }
            )
            PedigreeNodeCard(
                node = data.damNode,
                fallbackRole = PedigreeNodeRole.DAM,
                isFa = isFa,
                modifier = Modifier.weight(1f),
                onClick = { data.damNode?.let(onNodeClick) }
            )
        }

        // GENERATION 2: GRANDPARENTS
        Text(
            text = if (isFa) "نسل دوم: پدربزرگ‌ها و مادربزرگ‌ها (Grandparents)" else "Generation 2: Grandparents (4 Nodes)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PedigreeNodeCard(
                    node = data.paternalGrandsireNode,
                    fallbackRole = PedigreeNodeRole.PATERNAL_GRANDSIRE,
                    isFa = isFa,
                    modifier = Modifier.weight(1f),
                    onClick = { data.paternalGrandsireNode?.let(onNodeClick) }
                )
                PedigreeNodeCard(
                    node = data.paternalGranddamNode,
                    fallbackRole = PedigreeNodeRole.PATERNAL_GRANDDAM,
                    isFa = isFa,
                    modifier = Modifier.weight(1f),
                    onClick = { data.paternalGranddamNode?.let(onNodeClick) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PedigreeNodeCard(
                    node = data.maternalGrandsireNode,
                    fallbackRole = PedigreeNodeRole.MATERNAL_GRANDSIRE,
                    isFa = isFa,
                    modifier = Modifier.weight(1f),
                    onClick = { data.maternalGrandsireNode?.let(onNodeClick) }
                )
                PedigreeNodeCard(
                    node = data.maternalGranddamNode,
                    fallbackRole = PedigreeNodeRole.MATERNAL_GRANDDAM,
                    isFa = isFa,
                    modifier = Modifier.weight(1f),
                    onClick = { data.maternalGranddamNode?.let(onNodeClick) }
                )
            }
        }

        // GENERATION 3: GREAT-GRANDPARENTS (ANCESTORS)
        Text(
            text = if (isFa) "نسل سوم: اجداد (Great-Grandparents / ۸ جد)" else "Generation 3: Great-Grandparents (8 Nodes)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (data.greatGrandparents.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                data.greatGrandparents.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { ggNode ->
                            PedigreeNodeCard(
                                node = ggNode,
                                fallbackRole = PedigreeNodeRole.GREAT_GRANDPARENT,
                                isFa = isFa,
                                modifier = Modifier.weight(1f),
                                onClick = { onNodeClick(ggNode) }
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isFa) "اجداد نسل سوم در پایگاه داده ثبت نشده‌اند." else "No great-grandparent records recorded for this bloodline.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PedigreeNodeCard(
    node: PedigreeTreeNode?,
    fallbackRole: PedigreeNodeRole,
    isFa: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isPresent = node != null

    OutlinedCard(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = isPresent, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isPresent) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFa) (node?.role?.labelFa ?: fallbackRole.labelFa) else (node?.role?.labelEn ?: fallbackRole.labelEn),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                if (node != null) {
                    val sexIcon = if (node.gender == BirdGender.MALE) Icons.Default.Male else Icons.Default.Female
                    val sexColor = if (node.gender == BirdGender.MALE) Color(0xFF1976D2) else Color(0xFFE91E63)
                    Icon(sexIcon, contentDescription = null, tint = sexColor, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (node != null) {
                Text(
                    text = node.ringNumber,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = node.name ?: "${node.color} • ${node.mutation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            } else {
                Text(
                    text = if (isFa) "ثبت نشده" else "Not Recorded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 2: DESCENDANTS (CHILDREN, GRANDCHILDREN, FUTURE GENERATIONS)
// -------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DescendantsTab(
    data: InteractivePedigreeTreeData,
    allBirds: List<BirdEntity>,
    isFa: Boolean,
    onNavigateToBird: (String) -> Unit,
    onSelectSubject: (String) -> Unit
) {
    var candidateMateRing by remember { mutableStateOf("") }
    var mateMenuExpanded by remember { mutableStateOf(false) }

    val oppositeSex = if (data.subjectBird.gender == BirdGender.MALE) BirdGender.FEMALE else BirdGender.MALE
    val candidateMates = allBirds.filter { it.ringNumber != data.subjectBird.ringNumber && (it.gender == oppositeSex || it.gender == BirdGender.UNKNOWN) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Direct Offspring (Children)
        item {
            Text(
                text = if (isFa) "فرزندان مستقیم ثبت شده (${data.directChildren.size})" else "Direct Offspring / Children (${data.directChildren.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (data.directChildren.isNotEmpty()) {
            items(data.directChildren) { child ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToBird(child.ringNumber) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val sexIcon = if (child.gender == BirdGender.MALE) Icons.Default.Male else Icons.Default.Female
                            val sexColor = if (child.gender == BirdGender.MALE) Color(0xFF1976D2) else Color(0xFFE91E63)
                            Icon(sexIcon, contentDescription = null, tint = sexColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = child.ringNumber,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${child.color} • ${child.mutation}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { onSelectSubject(child.ringNumber) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isFa) "شجره فرزند" else "Tree")
                        }
                    }
                }
            }
        } else {
            item {
                Text(
                    text = if (isFa) "هیچ فرزند مستقیمی برای این پرنده در پایگاه داده ثبت نشده است." else "No direct offspring recorded for this bird yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section: Grandchildren (2nd Gen Descendants)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isFa) "نوه‌ها (نسل دوم نوچه‌ها - Grandchildren / ${data.grandchildren.size})" else "Grandchildren (2nd Gen Descendants / ${data.grandchildren.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (data.grandchildren.isNotEmpty()) {
            items(data.grandchildren) { gChild ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = gChild.ringNumber,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${gChild.color} • ${gChild.mutation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onSelectSubject(gChild.ringNumber) }) {
                            Icon(Icons.Default.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        } else {
            item {
                Text(
                    text = if (isFa) "هنوز نوه‌ای از طریق فرزندان این پرنده متولد نشده است." else "No grandchildren recorded from this bloodline yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section: Future Generation Breeding Simulation
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isFa) "شبیه‌سازی نسل آینده (Future Generation Simulation)" else "Future Generations: Planned Pairing Simulation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isFa)
                            "یک جفت بالقوه برای این پرنده انتخاب کنید تا احتمالات فنوتیپ‌های نسل بعدی به همراه ضریب هم‌خونی پیش‌بینی شوند."
                        else
                            "Select a prospective mate for this bird to project future offspring genotype distributions and evaluate inbreeding coefficient.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = mateMenuExpanded,
                        onExpandedChange = { mateMenuExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = candidateMateRing.ifBlank { if (isFa) "انتخاب جفت احتمالی..." else "Select Prospective Mate..." },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mateMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = mateMenuExpanded,
                            onDismissRequest = { mateMenuExpanded = false }
                        ) {
                            candidateMates.forEach { mate ->
                                DropdownMenuItem(
                                    text = { Text("${mate.ringNumber} (${mate.color} • ${mate.mutation})") },
                                    onClick = {
                                        candidateMateRing = mate.ringNumber
                                        mateMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (candidateMateRing.isNotBlank()) {
                        val mateBird = allBirds.firstOrNull { it.ringNumber == candidateMateRing }
                        if (mateBird != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val isSire = data.subjectBird.gender == BirdGender.MALE
                            val sireBird = if (isSire) data.subjectBird else mateBird
                            val damBird = if (isSire) mateBird else data.subjectBird

                            val sireGen = GeneticsCalculator.parseGenotype(sireBird, null)
                            val damGen = GeneticsCalculator.parseGenotype(damBird, null)
                            val sim = GeneticsCalculator.predictOffspring(sireGen, damGen)

                            Text(
                                text = "Projected Future Offspring (Top Odds):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            sim.outcomes.take(3).forEach { outcome ->
                                Text(
                                    text = "• ${outcome.phenotypeNameEn}: ${outcome.probabilityPercent} per egg",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 3: LINEAGE CERTIFICATE & COI STATS
// -------------------------------------------------------------------------
@Composable
private fun LineageCertificateTab(
    data: InteractivePedigreeTreeData,
    isFa: Boolean,
    onEditPedigree: () -> Unit
) {
    val ped = data.pedigreeRecord

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isFa) "شناسنامه رسمی اصالت و نسب" else "Official Lineage Certificate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onEditPedigree) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    DetailRow(label = if (isFa) "شماره پلاک / حلقه" else "Bird Ring Number", value = data.subjectBird.ringNumber)
                    DetailRow(label = if (isFa) "نام پرنده" else "Name", value = data.subjectBird.name ?: "-")
                    DetailRow(label = if (isFa) "گونه و نژاد" else "Variety", value = data.subjectBird.variety?.displayName ?: "Standard")
                    DetailRow(label = if (isFa) "نسل فیلیکال" else "Generation", value = data.subjectBird.generation ?: "P (Foundation)")
                    DetailRow(label = if (isFa) "نام پرورش‌دهنده" else "Breeder Name", value = ped?.breederName ?: "Aviary Facility")
                    DetailRow(label = if (isFa) "کد ملی/اتحادیه پرورش‌دهنده" else "Breeder Code", value = ped?.breederCode ?: "IR-AV-01")
                    if (!ped?.lineageNotes.isNullOrBlank()) {
                        DetailRow(label = if (isFa) "یادداشت‌های خط خونی" else "Bloodline Notes", value = ped!!.lineageNotes!!)
                    }
                }
            }
        }

        // COI Analysis Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isFa) "تحلیل ضریب هم‌خونی رایت (Wright's Inbreeding Coefficient - F)" else "Wright's Coefficient of Inbreeding (COI / F)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.2f%%", data.inbreedingCoefficientF * 100),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Badge(
                            containerColor = if (data.inbreedingCoefficientF < 0.0625) Color(0xFF2E7D32) else Color(0xFFC62828)
                        ) {
                            Text(
                                text = if (data.inbreedingCoefficientF < 0.0625) "SAFE / LOW COI" else "ELEVATED COI",
                                color = Color.White,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (data.inbreedingCoefficientF.toFloat() * 2f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isFa)
                            "ضریب زیر ۶.۲۵٪ نشان‌دهنده تنوع ژنتیکی مطلوب و سلامت کامل است. ضرایب بالای ۱۲.۵٪ نیازمند پایش از نظر کاهش باروری و بنیه جوجه‌ها می‌باشد."
                        else
                            "COI < 6.25% indicates ideal genetic diversity. COI >= 12.5% indicates close inbreeding and warrants monitoring for reduced embryo hatchability.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

// -------------------------------------------------------------------------
// EDIT PEDIGREE RECORD DIALOG
// -------------------------------------------------------------------------
@Composable
private fun EditPedigreeDialog(
    subjectRing: String,
    currentPedigree: PedigreeRecordEntity?,
    onDismiss: () -> Unit,
    onSave: (PedigreeRecordEntity) -> Unit
) {
    var sire by remember { mutableStateOf(currentPedigree?.sireRing ?: "") }
    var dam by remember { mutableStateOf(currentPedigree?.damRing ?: "") }
    var patGrandsire by remember { mutableStateOf(currentPedigree?.paternalGrandsire ?: "") }
    var patGranddam by remember { mutableStateOf(currentPedigree?.paternalGranddam ?: "") }
    var matGrandsire by remember { mutableStateOf(currentPedigree?.maternalGrandsire ?: "") }
    var matGranddam by remember { mutableStateOf(currentPedigree?.maternalGranddam ?: "") }
    var breederName by remember { mutableStateOf(currentPedigree?.breederName ?: "Aviary Facility") }
    var breederCode by remember { mutableStateOf(currentPedigree?.breederCode ?: "IR-AV-01") }
    var notes by remember { mutableStateOf(currentPedigree?.lineageNotes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Lineage Certificate", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = sire, onValueChange = { sire = it }, label = { Text("Sire Ring (Father)") }, singleLine = true)
                OutlinedTextField(value = dam, onValueChange = { dam = it }, label = { Text("Dam Ring (Mother)") }, singleLine = true)
                OutlinedTextField(value = patGrandsire, onValueChange = { patGrandsire = it }, label = { Text("Paternal Grandsire") }, singleLine = true)
                OutlinedTextField(value = patGranddam, onValueChange = { patGranddam = it }, label = { Text("Paternal Granddam") }, singleLine = true)
                OutlinedTextField(value = matGrandsire, onValueChange = { matGrandsire = it }, label = { Text("Maternal Grandsire") }, singleLine = true)
                OutlinedTextField(value = matGranddam, onValueChange = { matGranddam = it }, label = { Text("Maternal Granddam") }, singleLine = true)
                OutlinedTextField(value = breederName, onValueChange = { breederName = it }, label = { Text("Breeder Name") }, singleLine = true)
                OutlinedTextField(value = breederCode, onValueChange = { breederCode = it }, label = { Text("Breeder Code") }, singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Lineage Notes") }, maxLines = 2)
            }
        },
        confirmButton = {
            Button(onClick = {
                val entity = (currentPedigree ?: PedigreeRecordEntity(birdRingNumber = subjectRing)).copy(
                    sireRing = sire.takeIf { it.isNotBlank() },
                    damRing = dam.takeIf { it.isNotBlank() },
                    paternalGrandsire = patGrandsire.takeIf { it.isNotBlank() },
                    paternalGranddam = patGranddam.takeIf { it.isNotBlank() },
                    maternalGrandsire = matGrandsire.takeIf { it.isNotBlank() },
                    maternalGranddam = matGranddam.takeIf { it.isNotBlank() },
                    breederName = breederName,
                    breederCode = breederCode,
                    lineageNotes = notes.takeIf { it.isNotBlank() },
                    updatedAt = System.currentTimeMillis()
                )
                onSave(entity)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
