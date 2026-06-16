package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.data.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

// =========================================================================
// LOCAL FILE METADATA ENTITY & CONVERTER
// =========================================================================

data class LocalDocMetadata(
    val id: String = "${System.currentTimeMillis()}",
    val name: String = "",
    val relativePath: String = "", // relative to filesDir
    val fileType: String = "PDF", // "PDF", "JPG", "PNG", "DOCX", etc.
    val category: String = "Uncategorized", // "Resumes", "Cover Letters", "Certificates", "Receipts", "Uncategorized"
    val notes: String = "",
    val dateAdded: Long = System.currentTimeMillis(),
    val sizeInBytes: Long = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentManagerScreen(
    viewModel: ResumeViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val resumes by viewModel.resumes.collectAsState(initial = emptyList())
    val cvVersions by viewModel.cvVersionsState.collectAsState(initial = emptyList())

    // Tabs: 0 -> Converted & Stored Files, 1 -> Saved CV drafts (Room)
    var selectedTab by remember { mutableStateOf(0) }
    
    // Search query & Category filtering
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    // List of local documents loaded from disk & JSON list
    var localDocsList by remember { mutableStateOf<List<LocalDocMetadata>>(emptyList()) }
    
    // Dialogue states
    var showRenameDialog by remember { mutableStateOf<LocalDocMetadata?>(null) }
    var renameInputName by remember { mutableStateOf("") }
    
    var showCategoryDialog by remember { mutableStateOf<LocalDocMetadata?>(null) }
    
    var showNotesDialog by remember { mutableStateOf<LocalDocMetadata?>(null) }
    var notesInputText by remember { mutableStateOf("") }
    
    var fileToDelete by remember { mutableStateOf<LocalDocMetadata?>(null) }
    var resumeToDelete by remember { mutableStateOf<Resume?>(null) }
    
    // Immersive bottom preview sheet state
    var previewDocument by remember { mutableStateOf<LocalDocMetadata?>(null) }
    var previewPdfBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isRenderingPreview by remember { mutableStateOf(false) }

    // CV-specific dialogs
    var showCvCategoryDialog by remember { mutableStateOf<Resume?>(null) }
    var showCvVersionsDialog by remember { mutableStateOf<Resume?>(null) }

    // Helper functions to manage the local files directory & metadata list
    val appDocsDir = File(context.filesDir, "AppDocuments")
    
    // Function to load local files and sync lists
    fun loadAndSyncLocalDocs() {
        if (!appDocsDir.exists()) {
            appDocsDir.mkdirs()
        }
        
        // Load existing metadata map
        val metadataFile = File(context.filesDir, "local_document_metadata.json")
        var metadataMap = mutableMapOf<String, LocalDocMetadata>()
        
        if (metadataFile.exists()) {
            try {
                val json = metadataFile.readText()
                val list = JsonParser.fromJsonList<LocalDocMetadata>(json)
                list.forEach { doc ->
                    metadataMap[doc.relativePath] = doc
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Scan the directory
        val actualFiles = appDocsDir.listFiles() ?: emptyArray()
        val syncedList = mutableListOf<LocalDocMetadata>()
        
        actualFiles.forEach { file ->
            val relPath = "AppDocuments/${file.name}"
            val existingMeta = metadataMap[relPath]
            
            if (existingMeta != null) {
                // Update size and compile in list
                syncedList.add(existingMeta.copy(sizeInBytes = file.length()))
            } else {
                // Discover new file & create metadata
                val extension = file.extension.uppercase(Locale.ROOT)
                val cleanName = file.nameWithoutExtension.replace("_", " ")
                val newMeta = LocalDocMetadata(
                    id = "${System.currentTimeMillis()}_${file.hashCode()}",
                    name = cleanName,
                    relativePath = relPath,
                    fileType = if (extension.isBlank()) "PDF" else extension,
                    category = "Uncategorized",
                    notes = "Automatically discovered file.",
                    dateAdded = file.lastModified(),
                    sizeInBytes = file.length()
                )
                syncedList.add(newMeta)
            }
        }
        
        // Save back synced state to clean metadata.json
        try {
            val json = JsonParser.toJsonList(syncedList)
            metadataFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        localDocsList = syncedList.sortedByDescending { it.dateAdded }
    }
    
    // Save metadata change
    fun updateDocMetadata(updated: LocalDocMetadata) {
        val updatedList = localDocsList.map { if (it.id == updated.id) updated else it }
        val metadataFile = File(context.filesDir, "local_document_metadata.json")
        try {
            val json = JsonParser.toJsonList(updatedList)
            metadataFile.writeText(json)
            localDocsList = updatedList
            Toast.makeText(context, "Document updated successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving changes: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Copy external file into our manager securely
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val contentResolver = context.contentResolver
                // Get filename
                var sourceName = getFileNameFromUri(context, uri) ?: "imported_doc_${System.currentTimeMillis()}"
                var extension = File(sourceName).extension.lowercase()
                if (extension.isBlank()) {
                    extension = "pdf"
                    sourceName += ".pdf"
                }
                
                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val uuid = UUID.randomUUID().toString().take(6)
                    val destinationFile = File(appDocsDir, "imported_${uuid}_${sourceName}")
                    val outputStream = FileOutputStream(destinationFile)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                    
                    // Create metadata
                    val relativePath = "AppDocuments/${destinationFile.name}"
                    val newMeta = LocalDocMetadata(
                        id = "${System.currentTimeMillis()}",
                        name = sourceName.replace(".$extension", "").replace("_", " "),
                        relativePath = relativePath,
                        fileType = extension.uppercase(Locale.ROOT),
                        category = "Uncategorized",
                        notes = "Manually imported on ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}",
                        dateAdded = System.currentTimeMillis(),
                        sizeInBytes = destinationFile.length()
                    )
                    
                    // Save to local metadata list
                    val metadataFile = File(context.filesDir, "local_document_metadata.json")
                    val currentList = localDocsList.toMutableList()
                    currentList.add(0, newMeta)
                    metadataFile.writeText(JsonParser.toJsonList(currentList))
                    localDocsList = currentList
                    
                    Toast.makeText(context, "Successfully stored '$sourceName' locally!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to import file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Trigger load on start
    LaunchedEffect(Unit) {
        loadAndSyncLocalDocs()
        viewModel.loadCvVersions()
    }

    // Process PDF previews dynamically
    LaunchedEffect(previewDocument) {
        if (previewDocument != null && previewDocument?.fileType == "PDF") {
            isRenderingPreview = true
            previewPdfBitmaps = emptyList()
            
            val targetFile = File(context.filesDir, previewDocument!!.relativePath)
            if (targetFile.exists()) {
                // Run dispatcher thread rendering
                try {
                    val fd = ParcelFileDescriptor.open(targetFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(fd)
                    val pageCount = renderer.pageCount
                    val rendered = mutableListOf<Bitmap>()
                    
                    // Render first 2 pages for speedy preview
                    val pagesToRender = Math.min(pageCount, 3)
                    for (i in 0 until pagesToRender) {
                        val page = renderer.openPage(i)
                        val ratio = 1.5f
                        val width = (page.width * ratio).toInt()
                        val height = (page.height * ratio).toInt()
                        
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(android.graphics.Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        rendered.add(bitmap)
                        page.close()
                    }
                    renderer.close()
                    fd.close()
                    previewPdfBitmaps = rendered
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            isRenderingPreview = false
        } else {
            previewPdfBitmaps = emptyList()
        }
    }

    // Categories list for folders & tags representation
    val categoriesList = listOf("All", "Cvs", "Cover Letters", "Certificates", "Receipts", "Other", "Uncategorized")

    // Filtered lists
    val filteredDocs = localDocsList.filter { doc ->
        val nameMatches = doc.name.contains(searchQuery, ignoreCase = true) || doc.notes.contains(searchQuery, ignoreCase = true)
        val categoryMatches = selectedCategoryFilter == "All" || doc.category.equals(selectedCategoryFilter, ignoreCase = true)
        nameMatches && categoryMatches
    }

    val filteredResumes = resumes.filter { resume ->
        val resumeTitle = resume.title.ifBlank { "Untitled Resume" }
        val categoryMatches = selectedCategoryFilter == "All" || selectedCategoryFilter == "Cvs"
        resumeTitle.contains(searchQuery, ignoreCase = true) && categoryMatches
    }

    // Format size
    fun formatFileSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024
        if (kb < 1024) return "$kb KB"
        val mb = kb / 1024f
        return String.format(Locale.getDefault(), "%.1f MB", mb)
    }

    // Format Date
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Local Document Vault",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "View, organize, and persistent store your CVs and converted files",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                    }
                },
                actions = {
                    // Quick stats
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF3B82F6).copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${localDocsList.size} Saved Files",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3B82F6)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    containerColor = Color(0xFF3B82F6),
                    contentColor = Color.White,
                    modifier = Modifier.testTag("import_file_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Import external document")
                        Text("Import File", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // CENTRAL WORKSPACE SELECTOR TAB
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF0F172A),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF3B82F6),
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderZip,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 0) Color(0xFF3B82F6) else Color.Gray
                            )
                            Text(
                                "Local Converted Files (${localDocsList.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 1) Color(0xFF3B82F6) else Color.Gray
                            )
                            Text(
                                "Created Portfolios (${resumes.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                )
            }

            // SEARCH AND FILTER CONTAINER (ASPINNING CARD ELEGANCE)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search files by name, tags, description...", fontSize = 12.sp) },
                        prefix = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp), tint = Color.Gray) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            focusedIndicatorColor = Color(0xFF3B82F6)
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))

                    // Categories Horizontal slider chips
                    Text(
                        text = "ORGANIZATION DIRECTORIES",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.1.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoriesList.forEach { category ->
                            val isSelected = selectedCategoryFilter.equals(category, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF3B82F6) else Color(0xFFF1F5F9))
                                    .border(1.dp, if (isSelected) Color(0xFF3B82F6) else Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedCategoryFilter = category
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = when (category.lowercase(Locale.ROOT)) {
                                            "all" -> Icons.Default.AllInclusive
                                            "cvs" -> Icons.Default.HistoryEdu
                                            "cover letters" -> Icons.Default.Mail
                                            "certificates" -> Icons.Default.WorkspacePremium
                                            "receipts" -> Icons.Default.ReceiptLong
                                            "other" -> Icons.Default.MoreHoriz
                                            else -> Icons.Default.FolderOpen
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = if (isSelected) Color.White else Color(0xFF475569)
                                    )
                                    Text(
                                        text = category.uppercase(Locale.ROOT),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // MAIN LIST VIEWPORTS
            if (selectedTab == 0) {
                // LOCAL CONVERTED & SAVED FILES
                if (filteredDocs.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Converted Files Saved",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Store PDF booklets, high-res converted JPG files, or receipts locally to categorize they efficiently here.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { filePickerLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Store New Document", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredDocs) { doc ->
                            LocalDocManagerItemCard(
                                doc = doc,
                                sizeLabel = formatFileSize(doc.sizeInBytes),
                                dateLabel = formatDate(doc.dateAdded),
                                onPreview = {
                                    previewDocument = doc
                                },
                                onRename = {
                                    renameInputName = doc.name
                                    showRenameDialog = doc
                                },
                                onRecategorize = {
                                    showCategoryDialog = doc
                                },
                                onEditNotes = {
                                    notesInputText = doc.notes
                                    showNotesDialog = doc
                                },
                                onShare = {
                                    val file = File(context.filesDir, doc.relativePath)
                                    if (file.exists()) {
                                        try {
                                            val uri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file
                                            )
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = if (doc.fileType == "PDF") "application/pdf" else "image/*"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(intent, "Share Document via:"))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error sharing file: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "File does not physically exist on local storage.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onDelete = {
                                    fileToDelete = doc
                                }
                            )
                        }
                    }
                }
            } else {
                // SAVED CV DRAFTS (ROOM INTEGRATING)
                if (filteredResumes.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Portfolios / CVs Found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Select templates inside the center panel to draft responsive resumes.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredResumes) { resume ->
                            val custom = com.example.data.JsonParser.fromJson<com.example.data.Customization>(resume.customization) ?: com.example.data.Customization()
                            val personal = com.example.data.JsonParser.fromJson<com.example.data.PersonalInfo>(resume.personalInfo) ?: com.example.data.PersonalInfo()
                            
                            val versionsForThis = cvVersions.filter { it.resumeId == resume.id }
                            
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("cv_manager_card_${resume.id}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF10B981))
                                                )
                                                Text(
                                                    text = resume.title.ifBlank { "Untitled Portfolio" },
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF0F172A)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = "Applicant: ${personal.fullName.ifBlank { "Unspecified Candidate" }}",
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp,
                                                color = Color(0xFF475569)
                                            )
                                            Text(
                                                text = "Layout Template: ${custom.templateId.uppercase()}  |  ${versionsForThis.size} snap versions",
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }

                                        // Edit drafting action
                                        IconButton(onClick = {
                                            viewModel.loadResume(resume.id)
                                            onBack()
                                            Toast.makeText(context, "Editing '${resume.title}'", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Resume", tint = Color(0xFF3B82F6))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Divider(color = Color(0xFFF1F5F9))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Action buttons for CV manager
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Category chip (simulated local categories inside customized titles/tags)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF3B82F6).copy(alpha = 0.08f))
                                                .clickable {
                                                    Toast.makeText(context, "Resumes fall under CV category", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("CATEGORY: PORTFOLIO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Quiet converter to JPG directly inside Vault!
                                            IconButton(
                                                onClick = {
                                                    isRenderingPreview = true
                                                    exportAndRenderResumeQuietly(context, resume) { file ->
                                                        isRenderingPreview = false
                                                        if (file != null) {
                                                            try {
                                                                // Render pages & save inside Local Converted Files
                                                                val destFile = File(appDocsDir, "CV_Export_${resume.title.replace(" ", "_")}_${System.currentTimeMillis()}.pdf")
                                                                file.copyTo(destFile, overwrite = true)
                                                                
                                                                loadAndSyncLocalDocs()
                                                                Toast.makeText(context, "Quietly stored PDF inside conformed files vault!", Toast.LENGTH_LONG).show()
                                                            } catch (e: Exception) {
                                                                e.printStackTrace()
                                                            }
                                                        } else {
                                                            Toast.makeText(context, "Failed to compile CV PDF.", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.SaveAlt, contentDescription = "Store CV locally in Converted vault", modifier = Modifier.size(16.dp), tint = Color(0xFF0F172A))
                                            }

                                            // History check snapshot versions
                                            IconButton(
                                                onClick = {
                                                    showCvVersionsDialog = resume
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.History, contentDescription = "Rollback / Version History", modifier = Modifier.size(16.dp), tint = Color(0xFF8B5CF6))
                                            }

                                            // Duplicate CV
                                            IconButton(
                                                onClick = {
                                                    viewModel.duplicateResume(resume.id)
                                                    Toast.makeText(context, "CV Draft Duplicated!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Duplicate Resume", modifier = Modifier.size(16.dp), tint = Color(0xFF475569))
                                            }

                                            // Delete CV
                                            IconButton(
                                                onClick = {
                                                    resumeToDelete = resume
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Resume", modifier = Modifier.size(16.dp), tint = Color(0xFFEF4444))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // FULLSCREEN IMMERSIVE PREVIEW BOTTOM MODAL / SHEET
        // =========================================================================
        if (previewDocument != null) {
            val doc = previewDocument!!
            val file = File(context.filesDir, doc.relativePath)
            
            Dialog(onDismissRequest = { previewDocument = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Title header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = doc.name.uppercase(Locale.ROOT),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Category: ${doc.category.uppercase()}  |  ${formatFileSize(doc.sizeInBytes)}",
                                    color = Color.LightGray,
                                    fontSize = 9.sp
                                )
                            }
                            IconButton(onClick = { previewDocument = null }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                        
                        // Main Viewer Pane
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isRenderingPreview) {
                                CircularProgressIndicator(color = Color(0xFF3B82F6))
                            } else {
                                if (doc.fileType == "PDF") {
                                    if (previewPdfBitmaps.isNotEmpty()) {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize().padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            items(previewPdfBitmaps) { map ->
                                                Card(
                                                    shape = RoundedCornerShape(6.dp),
                                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                                    modifier = Modifier.fillMaxWidth().border(0.5.dp, Color.LightGray)
                                                ) {
                                                    Image(
                                                        bitmap = map.asImageBitmap(),
                                                        contentDescription = "PDF Page View Unit",
                                                        modifier = Modifier.fillMaxWidth().background(Color.White),
                                                        contentScale = ContentScale.FillWidth
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Text("Page layout empty or file corrupted.", fontSize = 12.sp, color = Color.Gray)
                                    }
                                } else {
                                    // Image JPG/PNG
                                    if (file.exists()) {
                                        val originalBmp = remember(file) {
                                            try {
                                                val options = android.graphics.BitmapFactory.Options()
                                                android.graphics.BitmapFactory.decodeFile(file.absolutePath, options)
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }
                                        if (originalBmp != null) {
                                            Image(
                                                bitmap = originalBmp.asImageBitmap(),
                                                contentDescription = "Visual Asset Converted Display",
                                                modifier = Modifier.fillMaxSize().padding(14.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                        } else {
                                            Text("Failed to read image bitmap.", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    } else {
                                        Text("Local physical disk asset not found.", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                        
                        // Actions toolbar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (file.exists()) {
                                        try {
                                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = if (doc.fileType == "PDF") "application/pdf" else "image/*"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(intent, "Share Document via:"))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Out", fontSize = 12.sp)
                            }
                            
                            Button(
                                onClick = { previewDocument = null },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B))
                            ) {
                                Text("Done", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // DIALOG: RENAME LOCAL FILE
        // =========================================================================
        if (showRenameDialog != null) {
            val doc = showRenameDialog!!
            Dialog(onDismissRequest = { showRenameDialog = null }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Rename Document", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = renameInputName,
                            onValueChange = { renameInputName = it },
                            placeholder = { Text("E.g. Computer Science Resume 2026") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showRenameDialog = null }) {
                                Text("Cancel", color = Color.Gray)
                            }
                            Button(
                                onClick = {
                                    if (renameInputName.isNotBlank()) {
                                        updateDocMetadata(doc.copy(name = renameInputName))
                                        showRenameDialog = null
                                    } else {
                                        Toast.makeText(context, "Please write a safe filename", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                            ) {
                                Text("Save New")
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // DIALOG: EDIT CATEGORIES
        // =========================================================================
        if (showCategoryDialog != null) {
            val doc = showCategoryDialog!!
            Dialog(onDismissRequest = { showCategoryDialog = null }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Select Document Category", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val tags = listOf("Resumes", "Cover Letters", "Certificates", "Receipts", "Other", "Uncategorized")
                        tags.forEach { tag ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        updateDocMetadata(doc.copy(category = tag))
                                        showCategoryDialog = null
                                    }
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (doc.category.equals(tag, ignoreCase = true)) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (doc.category.equals(tag, ignoreCase = true)) Color(0xFF3B82F6) else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(tag, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // DIALOG: EDIT NOTES
        // =========================================================================
        if (showNotesDialog != null) {
            val doc = showNotesDialog!!
            Dialog(onDismissRequest = { showNotesDialog = null }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add Document Notes & Tags", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Add custom description details or ATS tags to this compiled asset below:", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        OutlinedTextField(
                            value = notesInputText,
                            onValueChange = { notesInputText = it },
                            placeholder = { Text("Search tags, application status, recruiter feedback...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            maxLines = 4
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showNotesDialog = null }) {
                                Text("Close", color = Color.Gray)
                            }
                            Button(
                                onClick = {
                                    updateDocMetadata(doc.copy(notes = notesInputText))
                                    showNotesDialog = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                            ) {
                                Text("Save Notes")
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // DIALOG: CONFIRM DELETE LOCAL FILE
        // =========================================================================
        if (fileToDelete != null) {
            val doc = fileToDelete!!
            AlertDialog(
                onDismissRequest = { fileToDelete = null },
                title = { Text("Permanent Delete File?", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                text = { Text("This will permanently delete physical file '${doc.name}' (${doc.fileType}) from your local Vault storage. This action cannot be reversed.", fontSize = 12.sp) },
                confirmButton = {
                    Button(
                        onClick = {
                            val file = File(context.filesDir, doc.relativePath)
                            if (file.exists()) {
                                file.delete()
                            }
                            // Update metadata
                            val remaining = localDocsList.filter { it.id != doc.id }
                            val metadataFile = File(context.filesDir, "local_document_metadata.json")
                            metadataFile.writeText(JsonParser.toJsonList(remaining))
                            localDocsList = remaining
                            fileToDelete = null
                            Toast.makeText(context, "Asset permanently deleted.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.testTag("confirm_delete_button")
                    ) {
                        Text("Delete Permanent", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { fileToDelete = null }) {
                        Text("Keep File")
                    }
                }
            )
        }

        // =========================================================================
        // DIALOG: CONFIRM DELETE CV DRAFT
        // =========================================================================
        if (resumeToDelete != null) {
            val resume = resumeToDelete!!
            AlertDialog(
                onDismissRequest = { resumeToDelete = null },
                title = { Text("Delete Portfolio Resume Draft?", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to permanently delete local resume draft '${resume.title}'? All data will be removed from your database.", fontSize = 12.sp) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteResume(resume.id)
                            resumeToDelete = null
                            Toast.makeText(context, "Resume draft deleted successfully.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Delete CV", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { resumeToDelete = null }) {
                        Text("Keep CV")
                    }
                }
            )
        }

        // =========================================================================
        // DIALOG: HISTORY VERSION CORNER CHANGER & ROLLBACK
        // =========================================================================
        if (showCvVersionsDialog != null) {
            val resume = showCvVersionsDialog!!
            val versionsForThis = cvVersions.filter { it.resumeId == resume.id }
            
            Dialog(onDismissRequest = { showCvVersionsDialog = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Snapshots & Version Control",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Created snap backups of your resume variations. Rollback or inspect details anytime.",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        if (versionsForThis.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.Timeline, contentDescription = null, tint = Color.LightGray)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("No snapshot backups created yet.", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .heightIn(max = 240.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(versionsForThis) { snap ->
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(snap.versionName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                                Text(formatDate(snap.timestamp), fontSize = 9.sp, color = Color.Gray)
                                            }
                                            Row {
                                                TextButton(
                                                    onClick = {
                                                        viewModel.rollbackToVersion(snap) {
                                                            Toast.makeText(context, "Rollback successful! Resume draft replaced.", Toast.LENGTH_LONG).show()
                                                            showCvVersionsDialog = null
                                                        }
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Rollback", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                                                }
                                                IconButton(
                                                    onClick = {
                                                        viewModel.deleteCvVersion(snap.id)
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Delete backup", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(
                                onClick = {
                                    viewModel.addCvVersion(resume, "Manual Snapshot Draft ${versionsForThis.size + 1}")
                                    Toast.makeText(context, "Snapshot backup compiled!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("New Snapshot", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = { showCvVersionsDialog = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Close", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocalDocManagerItemCard(
    doc: LocalDocMetadata,
    sizeLabel: String,
    dateLabel: String,
    onPreview: () -> Unit,
    onRename: () -> Unit,
    onRecategorize: () -> Unit,
    onEditNotes: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("local_file_card_${doc.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // File Type badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (doc.fileType == "PDF") {
                                Color(0xFFEF4444).copy(alpha = 0.08f)
                            } else {
                                Color(0xFF3B82F6).copy(alpha = 0.08f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (doc.fileType == "PDF") Icons.Default.PictureAsPdf else Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (doc.fileType == "PDF") Color(0xFFEF4444) else Color(0xFF3B82F6)
                    )
                }
                
                // Document details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.name,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sizeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(Color.Gray))
                        Text(
                            text = dateLabel,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    
                    if (doc.notes.isNotBlank() && doc.notes != "Automatically discovered file.") {
                        Text(
                            text = "Notes: ${doc.notes}",
                            fontSize = 9.5.sp,
                            color = Color(0xFF475569),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // Preview Action clicker
                Button(
                    onClick = onPreview,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6).copy(alpha = 0.1f), contentColor = Color(0xFF3B82F6)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(6.dp))
            
            // Subactions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF64748B).copy(alpha = 0.08f))
                        .clickable { onRecategorize() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color(0xFF475569))
                        Text(doc.category.uppercase(), fontSize = 8.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
                    }
                }
                
                // Toolbar icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onRename, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.DriveFileRenameOutline, contentDescription = "Rename", tint = Color(0xFF475569), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onEditNotes, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Label, contentDescription = "Add tags or notes", tint = Color(0xFF475569), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share File Provider", tint = Color(0xFF475569), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete File", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
