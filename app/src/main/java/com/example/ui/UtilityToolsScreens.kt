package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.content.Intent
import android.provider.MediaStore
import android.content.ContentValues
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag

// =========================================================================
// PDF TO JPG HIGH-RES CONVERTER SCREEN (100% Real, Offline Native Processing)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToJpgScreen(
    resumes: List<Resume>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isProcessing by remember { mutableStateOf(false) }
    var loadedPdfFile by remember { mutableStateOf<File?>(null) }
    var pdfPageBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var selectedPdfName by remember { mutableStateOf("") }
    
    // Resume select dropdown launcher or card selectors
    var showResumePicker by remember { mutableStateOf(false) }

    // Helper functions within state context
    fun renderPdfPages(file: File, onComplete: (List<Bitmap>) -> Unit) {
        try {
            val parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(parcelFileDescriptor)
            val pageCount = pdfRenderer.pageCount
            val list = mutableListOf<Bitmap>()
            
            for (i in 0 until pageCount) {
                val page = pdfRenderer.openPage(i)
                // Render at high clarity: increase width/height scale for 4K / HD look (approx double size)
                val scaleFactor = 2f
                val width = (page.width * scaleFactor).toInt()
                val height = (page.height * scaleFactor).toInt()
                
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(android.graphics.Color.WHITE)
                
                // Render page to bitmap
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                list.add(bitmap)
                page.close()
            }
            pdfRenderer.close()
            parcelFileDescriptor.close()
            onComplete(list)
        } catch (e: Exception) {
            Toast.makeText(context, "Error rendering PDF pages: ${e.message}", Toast.LENGTH_LONG).show()
            onComplete(emptyList())
        }
    }

    // External PDF picker launcher
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val tempFile = File(context.cacheDir, "picked_temp_${System.currentTimeMillis()}.pdf")
                    val outputStream = FileOutputStream(tempFile)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                    
                    loadedPdfFile = tempFile
                    selectedPdfName = getFileNameFromUri(context, uri) ?: "External Document"
                    renderPdfPages(tempFile) { bitmaps ->
                        pdfPageBitmaps = bitmaps
                        isProcessing = false
                        val sizeValue = bitmaps.size
                        Toast.makeText(context, "Successfully loaded $sizeValue pages!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    isProcessing = false
                }
            } catch (e: Exception) {
                isProcessing = false
                Toast.makeText(context, "Failed to load PDF: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PDF TO JPG CONVERTER",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Extract High-Density JPGs from any PDF offline",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF8FAFC), Color(0xFFEDF2F7))
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
            ) {
                // Intro banner
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Collections,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Extract JPGs Instantly",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This tool lets you load any PDF, select individual pages, and save them as crisp JPEG image files. Perfect for sharing your CV or portfolio on visual channels like LinkedIn, WhatsApp, or email.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                // File Selector Cards
                item {
                    Text(
                        text = "CHOOSE INPUT SOURCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Pick from device card
                        Card(
                            onClick = { pdfPickerLauncher.launch("application/pdf") },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Device Storage",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Browse local PDFs",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Pick from Saved Drafts
                        Card(
                            onClick = { showResumePicker = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderSpecial,
                                    contentDescription = null,
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Saved CV Drafts",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Convert your resumes",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Selected File Indicator
                if (selectedPdfName.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color.Red
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedPdfName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${pdfPageBitmaps.size} pages available",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                if (isProcessing) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                }

                // Rendered Pages Grid/List
                if (pdfPageBitmaps.isNotEmpty()) {
                    item {
                        Text(
                            text = "CONVERTED HIGH-RESOLUTION IMAGES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    itemsIndexed(pdfPageBitmaps) { index, bitmap ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        ) {
                            Column {
                                // High quality shadow wrap image of page
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFE2E8F0))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.foundation.Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Page ${index + 1}",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color.LightGray)
                                            .background(Color.White)
                                    )
                                }

                                // Interactive actions row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Page ${index + 1} of ${pdfPageBitmaps.size}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Save Button
                                        Button(
                                            onClick = {
                                                val uri = saveBitmapToGallery(context, bitmap, index + 1)
                                                if (uri != null) {
                                                    Toast.makeText(context, "Saved Page ${index + 1} to Photos Gallery!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Failed to save image.", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Save Image", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Share Button
                                        OutlinedButton(
                                            onClick = {
                                                shareBitmap(context, bitmap, index + 1)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Share", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (!isProcessing && selectedPdfName.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                modifier = Modifier.size(54.dp)
                            )
                            Text(
                                text = "No PDF File Selected",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Pick a local PDF file or choose from your saved resume CV drafts above to load pages immediately.",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                lineHeight = 14.sp,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            }

            // Modal/Dialog selector for saved resumes
            if (showResumePicker) {
                AlertDialog(
                    onDismissRequest = { showResumePicker = false },
                    title = {
                        Text(
                            text = "Select Saved CV Draft",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (resumes.isEmpty()) {
                                Text(
                                    text = "No saved portfolios found. Go ahead and draft one first!",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            } else {
                                resumes.forEach { resume ->
                                    val title = resume.title.ifBlank { "Untitled Portfolio" }
                                    val customization = com.example.data.JsonParser.fromJson<com.example.data.Customization>(resume.customization) ?: com.example.data.Customization()
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                showResumePicker = false
                                                isProcessing = true
                                                selectedPdfName = "$title.pdf"
                                                
                                                // Generate PDF quietly and render
                                                exportAndRenderResumeQuietly(context, resume) { file ->
                                                    if (file != null) {
                                                        loadedPdfFile = file
                                                        renderPdfPages(file) { bitmaps ->
                                                            pdfPageBitmaps = bitmaps
                                                            isProcessing = false
                                                            Toast.makeText(context, "Loaded CV draft pages!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } else {
                                                        isProcessing = false
                                                        Toast.makeText(context, "Fatal layout render error", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Template: ${customization.templateId.uppercase()}",
                                                    fontSize = 9.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showResumePicker = false }) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}


// =========================================================================
// JPG TO PDF BOOKLET BUILDER SCREEN (100% Real, Offline Native Processing)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JpgToPdfScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isProcessing by remember { mutableStateOf(false) }
    var bookletTitle by remember { mutableStateOf("My_Custom_Credentials_Booklet") }
    
    // List of selected image Uris
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Multi Image Photo/Gallery selection launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImageUris = selectedImageUris + uris
            Toast.makeText(context, "Added ${uris.size} image assets!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "JPG TO PDF BUILDER",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Assemble photos and scans into single PDF book",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF8FAFC), Color(0xFFEDF2F7))
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
                ) {
                    // Intro Info Banner
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981).copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "Merge Images to PDF Booklet",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = Color(0xFF10B981)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Collect certificates, ID pages, receipts and image documents from your camera gallery. Arrange their sequence, customize the booklet name, and pack them instantly as a beautiful layout-fitted PDF.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    // Booklet parameters input card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "BOOKLET SETTINGS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = bookletTitle,
                                    onValueChange = { bookletTitle = it },
                                    label = { Text("Output PDF Title", fontSize = 12.sp) },
                                    placeholder = { Text("E.g. Passport_Credentials_Comp") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    // Add image asset buttons and indicators
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "UPLOADED IMAGE ASSETS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Images", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Loaded assets rearrangeable list
                    if (selectedImageUris.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                    modifier = Modifier.size(54.dp)
                                )
                                Text(
                                    text = "Booklet Asset List is Empty",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Tap the 'Add Images' button on the right to select image files or photos from camera gallery.",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                        }
                    } else {
                        itemsIndexed(selectedImageUris) { index, uri ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Index indicator sphere
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    // Tiny thumbnail or document placeholder
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF1F5F9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.InsertDriveFile,
                                            contentDescription = null,
                                            tint = Color.Gray
                                        )
                                    }

                                    // Uri or filename detail
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Image Document Source",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Resource tag: ...${uri.lastPathSegment?.takeLast(18)}",
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    // Order Rearrangement Actions
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Move Up Arrow
                                        IconButton(
                                            onClick = {
                                                if (index > 0) {
                                                    val list = selectedImageUris.toMutableList()
                                                    val tmp = list[index]
                                                    list[index] = list[index - 1]
                                                    list[index - 1] = tmp
                                                    selectedImageUris = list
                                                }
                                            },
                                            enabled = index > 0,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(16.dp))
                                        }

                                        // Move Down Arrow
                                        IconButton(
                                            onClick = {
                                                if (index < selectedImageUris.size - 1) {
                                                    val list = selectedImageUris.toMutableList()
                                                    val tmp = list[index]
                                                    list[index] = list[index + 1]
                                                    list[index + 1] = tmp
                                                    selectedImageUris = list
                                                }
                                            },
                                            enabled = index < selectedImageUris.size - 1,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(16.dp))
                                        }

                                        // Delete Button
                                        IconButton(
                                            onClick = {
                                                val list = selectedImageUris.toMutableList()
                                                list.removeAt(index)
                                                selectedImageUris = list
                                                Toast.makeText(context, "Asset removed", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Compile and export floating segment
                if (selectedImageUris.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xFFEDF2F7).copy(alpha = 0.95f), Color(0xFFEDF2F7))
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                isProcessing = true
                                compileImagesToPdfBooklet(context, selectedImageUris, bookletTitle) { file ->
                                    isProcessing = false
                                    if (file != null) {
                                        Toast.makeText(context, "Unified Booklet Constructed successfully!", Toast.LENGTH_SHORT).show()
                                        sharePdfFile(context, file, bookletTitle)
                                    } else {
                                        Toast.makeText(context, "Booklet construction failed.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("compile_pdf_button"),
                            enabled = !isProcessing
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Constructing Booklet...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "COMPILE & EXPORT PDF (${selectedImageUris.size} ASSETS) 📄",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// =========================================================================
// REAL NATIVE CONVERSION & SHARE UTILITIES
// =========================================================================

fun saveBitmapToGallery(context: Context, bitmap: Bitmap, pageNum: Int): Uri? {
    val filename = "YOURDOC_PAGE_${pageNum}_${System.currentTimeMillis()}.jpg"
    var fos: java.io.OutputStream? = null
    var imageUri: Uri? = null
    val contentResolver = context.contentResolver
    
    // Save locally to Document Manager's persistent Vault
    try {
        val appDocs = File(context.filesDir, "AppDocuments")
        if (!appDocs.exists()) {
            appDocs.mkdirs()
        }
        val localImgFile = File(appDocs, filename)
        val localFos = FileOutputStream(localImgFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, localFos)
        localFos.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/YourDoc_Converter")
            }
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri != null) {
                fos = contentResolver.openOutputStream(imageUri)
            }
        } else {
            val imagesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES)
            val imageFile = File(imagesDir, filename)
            imageUri = Uri.fromFile(imageFile)
            fos = FileOutputStream(imageFile)
        }
        
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return imageUri
}

fun shareBitmap(context: Context, bitmap: Bitmap, pageNum: Int) {
    try {
        val cachePath = File(context.cacheDir, "shared_images")
        cachePath.mkdirs()
        val file = File(cachePath, "yourdoc_shared_page_${pageNum}.jpg")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.close()
        
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Exported JPG"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing page image: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}

fun exportAndRenderResumeQuietly(
    context: Context,
    resume: Resume,
    onComplete: (File?) -> Unit
) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        
        // Deserialize JSON fields safely
        val title = resume.title.ifBlank { "Resume" }
        val pInfo = com.example.data.JsonParser.fromJson<com.example.data.PersonalInfo>(resume.personalInfo) ?: com.example.data.PersonalInfo()
        val workList = com.example.data.JsonParser.fromJsonList<com.example.data.WorkExperience>(resume.workExperiences)
        val eduList = com.example.data.JsonParser.fromJsonList<com.example.data.Education>(resume.educations)
        val skillList = com.example.data.JsonParser.fromJsonList<com.example.data.Skill>(resume.skills)
        val langList = com.example.data.JsonParser.fromJsonList<com.example.data.Language>(resume.languages)
        val aboutMe = com.example.data.JsonParser.fromJson<com.example.data.AboutMe>(resume.aboutMe) ?: com.example.data.AboutMe()
        val custom = com.example.data.JsonParser.fromJson<com.example.data.Customization>(resume.customization) ?: com.example.data.Customization()
        
        // Start drawing the PDF
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A")
            textSize = 22f
            isFakeBoldText = true
        }
        
        val textPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#44474E")
            textSize = 10f
        }
        
        val headerPaint = Paint().apply {
            color = android.graphics.Color.parseColor(
                when (custom.templateId) {
                    "europass_blue" -> "#002F6C"
                    "europass_modern" -> "#059669"
                    "europass_dark" -> "#1E293B"
                    "ats_white" -> "#1E293B"
                    "corporate_blue" -> "#1E3A8A"
                    "elegant_black" -> "#111827"
                    "modern_minimalist" -> "#475569"
                    "luxury_dark" -> "#854D0E"
                    "ats_charcoal" -> "#334155"
                    "creative_royal" -> "#4F46E5"
                    else -> "#002F6C"
                }
            )
            textSize = 14f
            isFakeBoldText = true
        }

        // Title Line
        canvas.drawRect(0f, 0f, 595f, 90f, Paint().apply { color = headerPaint.color })
        canvas.drawText(pInfo.fullName.uppercase(), 40f, 45f, Paint().apply { color = android.graphics.Color.WHITE; textSize = 18f; isFakeBoldText = true })
        if (pInfo.bloodGroup.isNotBlank()) {
            canvas.drawText("Blood Group: ${pInfo.bloodGroup}", 40f, 65f, Paint().apply { color = android.graphics.Color.LTGRAY; textSize = 11f })
        }
        
        var currentY = 120f
        
        // Contact details
        canvas.drawText("Email: ${pInfo.email}  |  Phone: ${pInfo.phone}", 40f, currentY, textPaint)
        currentY += 16f
        canvas.drawText("Address: ${pInfo.currentAddress}, ${pInfo.city}, ${pInfo.currentCountry}", 40f, currentY, textPaint)
        currentY += 24f

        // About me
        if (aboutMe.summary.isNotEmpty()) {
            canvas.drawText("PROFESSIONAL SUMMARY", 40f, currentY, Paint().apply { color = headerPaint.color; textSize = 11f; isFakeBoldText = true })
            currentY += 6f
            canvas.drawLine(40f, currentY, 555f, currentY, Paint().apply { color = android.graphics.Color.GRAY })
            currentY += 16f
            
            // Draw summary
            val sumWords = aboutMe.summary.split(" ")
            var line = ""
            for (word in sumWords) {
                if (line.length + word.length < 90) {
                    line += "$word "
                } else {
                    canvas.drawText(line, 40f, currentY, textPaint)
                    currentY += 14f
                    line = "$word "
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, 40f, currentY, textPaint)
                currentY += 14f
            }
            currentY += 10f
        }

        // Employment
        if (workList.isNotEmpty()) {
            canvas.drawText("WORK EXPERIENCE", 40f, currentY, Paint().apply { color = headerPaint.color; textSize = 11f; isFakeBoldText = true })
            currentY += 6f
            canvas.drawLine(40f, currentY, 555f, currentY, Paint().apply { color = android.graphics.Color.GRAY })
            currentY += 16f

            for (work in workList) {
                if (currentY > 780f) break
                canvas.drawText("${work.jobPosition} - ${work.companyName}", 40f, currentY, Paint().apply { isFakeBoldText = true; textSize = 10f })
                canvas.drawText("${work.startDate} - ${work.endDate}", 420f, currentY, textPaint)
                currentY += 12f
                
                // job summary
                val descWords = work.responsibilities.split(" ")
                var wLine = ""
                for (word in descWords) {
                    if (wLine.length + word.length < 90) {
                        wLine += "$word "
                    } else {
                        canvas.drawText(wLine, 50f, currentY, textPaint)
                        currentY += 14f
                        wLine = "$word "
                    }
                }
                if (wLine.isNotEmpty()) {
                    canvas.drawText(wLine, 50f, currentY, textPaint)
                    currentY += 14f
                }
                currentY += 6f
            }
            currentY += 10f
        }

        // Education
        if (eduList.isNotEmpty() && currentY <= 780f) {
            canvas.drawText("EDUCATION & QUALIFICATIONS", 40f, currentY, Paint().apply { color = headerPaint.color; textSize = 11f; isFakeBoldText = true })
            currentY += 6f
            canvas.drawLine(40f, currentY, 555f, currentY, Paint().apply { color = android.graphics.Color.GRAY })
            currentY += 16f

            for (edu in eduList) {
                if (currentY > 780f) break
                canvas.drawText("${edu.degree} - ${edu.schoolName}", 40f, currentY, Paint().apply { isFakeBoldText = true; textSize = 10f })
                canvas.drawText("${edu.endDate}", 420f, currentY, textPaint)
                currentY += 12f
                canvas.drawText("Grade: ${edu.gpa}", 50f, currentY, textPaint)
                currentY += 14f
            }
            currentY += 10f
        }

        // Skills & Language combined
        if ((skillList.isNotEmpty() || langList.isNotEmpty()) && currentY <= 780f) {
            canvas.drawText("CORE SKILLS & LANGUAGES", 40f, currentY, Paint().apply { color = headerPaint.color; textSize = 11f; isFakeBoldText = true })
            currentY += 6f
            canvas.drawLine(40f, currentY, 555f, currentY, Paint().apply { color = android.graphics.Color.GRAY })
            currentY += 16f

            if (skillList.isNotEmpty()) {
                val skillNames = skillList.joinToString(", ") { it.name }
                canvas.drawText("SKILLS: $skillNames", 40f, currentY, textPaint)
                currentY += 14f
            }
            if (langList.isNotEmpty()) {
                val langNames = langList.joinToString(", ") { "${it.name} (${it.speakingLevel})" }
                canvas.drawText("LANGUAGES: $langNames", 40f, currentY, textPaint)
                currentY += 14f
            }
        }

        pdfDocument.finishPage(page)
        
        // Save
        val file = File(context.cacheDir, "cv_rendering_quiet_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        outputStream.close()
        
        onComplete(file)
    } catch (e: Exception) {
        e.printStackTrace()
        onComplete(null)
    }
}

fun compileImagesToPdfBooklet(
    context: Context,
    imageUris: List<Uri>,
    title: String,
    onComplete: (File?) -> Unit
) {
    try {
        val pdfDocument = PdfDocument()
        val resolver = context.contentResolver
        
        for (index in imageUris.indices) {
            val uri = imageUris[index]
            val inputStream = resolver.openInputStream(uri)
            if (inputStream != null) {
                // Decode bitmap using native decoding
                val options = android.graphics.BitmapFactory.Options().apply {
                    inJustDecodeBounds = false
                }
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
                inputStream.close()
                
                if (bitmap != null) {
                    // Standard A4 sizes: 595 x 842
                    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, index + 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas = page.canvas
                    
                    // Fit inside A4 while keeping original aspect ratio beautifully
                    val scale = Math.min(555f / bitmap.width, 802f / bitmap.height)
                    val dx = (595f - bitmap.width * scale) / 2f
                    val dy = (842f - bitmap.height * scale) / 2f
                    
                    val matrix = android.graphics.Matrix().apply {
                        postScale(scale, scale)
                        postTranslate(dx, dy)
                    }
                    
                    // Background layout white
                    canvas.drawColor(android.graphics.Color.WHITE)
                    canvas.drawBitmap(bitmap, matrix, null)
                    
                    // draw indicator footer
                    val footerPaint = Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 8f
                    }
                    canvas.drawText("Document Page ${index + 1} of ${imageUris.size} | YourDoc Assembler", 40f, 825f, footerPaint)
                    
                    pdfDocument.finishPage(page)
                }
            }
        }
        
        val safeFileName = "${title.replace(" ", "_")}_Unified_Booklet_${System.currentTimeMillis()}.pdf"
        val outFile = File(context.cacheDir, safeFileName)
        val progressStream = FileOutputStream(outFile)
        pdfDocument.writeTo(progressStream)
        pdfDocument.close()
        progressStream.close()
        
        // Save locally to Document Manager's persistent Vault
        try {
            val appDocs = File(context.filesDir, "AppDocuments")
            if (!appDocs.exists()) {
                appDocs.mkdirs()
            }
            val permanentFile = File(appDocs, safeFileName)
            outFile.copyTo(permanentFile, overwrite = true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        onComplete(outFile)
    } catch (e: Exception) {
        e.printStackTrace()
        onComplete(null)
    }
}

fun sharePdfFile(context: Context, file: File, title: String) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Save or Share Portfolio PDF Booklet"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing booklet: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
