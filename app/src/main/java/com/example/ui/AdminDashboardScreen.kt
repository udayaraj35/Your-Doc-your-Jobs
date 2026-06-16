package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.JobListing
import com.example.data.UserReport

@Composable
fun AdminDashboardScreen(
    viewModel: ResumeViewModel,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Core data state from ViewModel
    val jobsList by viewModel.jobsState.collectAsStateWithLifecycle()
    val flaggedJobIds by viewModel.flaggedJobIds.collectAsStateWithLifecycle()
    val verifiedCompanyNames by viewModel.verifiedCompanyNames.collectAsStateWithLifecycle()
    val userReports by viewModel.userReports.collectAsStateWithLifecycle()
    
    // Admin Dashboard Internal Tabs: "jobs", "companies", "reports"
    var activeSubTab by remember { mutableStateOf("jobs") }
    
    // Admin Actions search queries
    var jobsSearchQuery by remember { mutableStateOf("") }
    var companySearchQuery by remember { mutableStateOf("") }
    var reportsSearchQuery by remember { mutableStateOf("") }
    var docsSearchQuery by remember { mutableStateOf("") }
    
    // Interactive Edit Description state
    var editJobIdForDesc by remember { mutableStateOf<String?>(null) }
    var editedJobDescText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))
                )
            )
            .padding(top = 8.dp)
    ) {
        // 1. TOP EXECUTIVE SHIELD ACCENTED BANNER
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Deep Slate Black
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp)
                .shadow(6.dp, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEF08A).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Admin Shield Logo",
                                tint = Color(0xFFFEF08A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "SYSTEM ADMINISTRATOR SUITE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFEF08A),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Compliance & Integrity Center",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onBackToHome,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Return to Home",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Text(
                    text = "Enforce quality standards, moderate suspicious employment vacancies, approve legitimate developer profiles, and adjudicate user reports to maintain global ecosystem credibility.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 15.sp
                )
            }
        }
        
        // 2. METRIC COUNTERS RIBBON
        val pendingCount = jobsList.size - flaggedJobIds.size
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Metric: All Postings
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.weight(1f).border(0.5.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Listed Vacancies", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("${jobsList.size}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                }
            }
            // Metric: Flagged / Restricted
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.weight(1f).border(0.5.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Flagged / Hidden", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("${flaggedJobIds.size}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444))
                }
            }
            // Metric: Unresolved Reports
            val activeReports = userReports.count { it.status == "Open" }
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.weight(1f).border(0.5.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Open Tickets", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("$activeReports", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 3. ADMINISTRATIVE WORKSPACE TAB SWITCHER BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(4.dp)
                .border(0.5.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val tabsInfo = listOf(
                Triple("jobs", Icons.Default.Work, "Moderate Jobs"),
                Triple("companies", Icons.Default.Business, "Verify Profiles"),
                Triple("docs", Icons.Default.Description, "Verify Docs"),
                Triple("audit", Icons.Default.Shield, "Security Logs"),
                Triple("reports", Icons.Default.Warning, "User Reports")
            )
            
            tabsInfo.forEach { (tabId, icon, label) ->
                val isSelected = activeSubTab == tabId
                val activeBg = when (tabId) {
                    "jobs" -> Color(0xFF3B82F6).copy(alpha = 0.12f)
                    "companies" -> Color(0xFF10B981).copy(alpha = 0.12f)
                    "docs" -> Color(0xFF6366F1).copy(alpha = 0.12f)
                    "audit" -> Color(0xFF8B5CF6).copy(alpha = 0.12f)
                    else -> Color(0xFFEF4444).copy(alpha = 0.12f)
                }
                val activeText = when (tabId) {
                    "jobs" -> Color(0xFF1D4ED8)
                    "companies" -> Color(0xFF047857)
                    "docs" -> Color(0xFF4338CA)
                    "audit" -> Color(0xFF6D28D9)
                    else -> Color(0xFFB91C1C)
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) activeBg else Color.Transparent)
                        .clickable { activeSubTab = tabId }
                        .padding(vertical = 8.dp)
                        .testTag("admin_subtab_$tabId"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) activeText else Color.Gray,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = label,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (isSelected) activeText else Color.Gray
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // 4. SELECTED WORKSPACE PANEL CONTAINER
        when (activeSubTab) {
            "jobs" -> {
                // ============================================
                // WORKSPACE: MODERATE JOB POSTINGS
                // ============================================
                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    // Search box
                    OutlinedTextField(
                        value = jobsSearchQuery,
                        onValueChange = { jobsSearchQuery = it },
                        placeholder = { Text("Filter vacancies by title or company Name...", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .testTag("admin_jobs_search_input")
                    )
                    
                    val filteredJobs = jobsList.filter {
                        jobsSearchQuery.isBlank() ||
                        it.title.contains(jobsSearchQuery, ignoreCase = true) ||
                        it.company.contains(jobsSearchQuery, ignoreCase = true)
                    }
                    
                    if (filteredJobs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                                Text("No matched vacancies in database.", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            items(filteredJobs) { job ->
                                val isFlagged = flaggedJobIds.contains(job.id)
                                val isCompanyVerified = verifiedCompanyNames.contains(job.company)
                                
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (isFlagged) 1.5.dp else 0.5.dp,
                                            color = if (isFlagged) Color(0xFFEF4444).copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        // Header Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = job.companyLogo,
                                                    fontSize = 18.sp
                                                )
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text(
                                                            text = job.company,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFE28743)
                                                        )
                                                        if (isCompanyVerified) {
                                                            Icon(
                                                                imageVector = Icons.Default.CheckCircle,
                                                                contentDescription = "Verified Company",
                                                                tint = Color(0xFF10B981),
                                                                modifier = Modifier.size(11.dp)
                                                            )
                                                            Text("Verified Profile", fontSize = 8.sp, color = Color(0xFF047857), fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                    Text(
                                                        text = "ID: ${job.id}",
                                                        fontSize = 8.sp,
                                                        color = Color.LightGray
                                                    )
                                                }
                                            }
                                            
                                            // Status Badge
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (isFlagged) Color(0xFFEF4444).copy(alpha = 0.12f) else Color(0xFF10B981).copy(alpha = 0.12f)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isFlagged) "🚨 FLAGGED / HIDDEN" else "✅ LIVE & UNRESTRICTED",
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isFlagged) Color(0xFFB91C1C) else Color(0xFF047857)
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        Text(
                                            text = job.title,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0F172A)
                                        )
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text("📍 ${job.city}, ${job.country}", fontSize = 10.sp, color = Color.Gray)
                                            Text("💸 ${job.currency} ${job.salary}", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        }
                                        
                                        if (editJobIdForDesc == job.id) {
                                            OutlinedTextField(
                                                value = editedJobDescText,
                                                onValueChange = { editedJobDescText = it },
                                                label = { Text("Update Job Description & Guidelines", fontSize = 10.sp) },
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                TextButton(
                                                    onClick = { editJobIdForDesc = null }
                                                ) {
                                                    Text("Cancel", fontSize = 10.sp)
                                                }
                                                Button(
                                                    onClick = {
                                                        val updated = jobsList.map {
                                                            if (it.id == job.id) it.copy(description = editedJobDescText) else it
                                                        }
                                                        viewModel.saveJobs(updated)
                                                        editJobIdForDesc = null
                                                        Toast.makeText(context, "Vacancy text revised successfully!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                                                ) {
                                                    Text("Save Description", fontSize = 10.sp, color = Color.White)
                                                }
                                            }
                                        } else {
                                            Text(
                                                text = job.description,
                                                fontSize = 10.sp,
                                                color = Color.DarkGray,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                lineHeight = 13.sp,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                        
                                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.4f))
                                        
                                        // Actions Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                // Toggle Flag Button
                                                Button(
                                                    onClick = {
                                                        if (isFlagged) {
                                                            viewModel.unflagJob(job.id)
                                                            Toast.makeText(context, "Post approved & restored live! ✅", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            viewModel.flagJob(job.id)
                                                            Toast.makeText(context, "Post hidden from candidates list. 🚨", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isFlagged) Color(0xFF10B981) else Color(0xFFEF4444)
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(30.dp).testTag("btn_toggle_flag_${job.id}")
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        val btnIcon = if (isFlagged) Icons.Default.Check else Icons.Default.Block
                                                        Icon(btnIcon, contentDescription = null, modifier = Modifier.size(11.dp), tint = Color.White)
                                                        Text(if (isFlagged) "APPROVE POST" else "FLAG / HIDE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }
                                                
                                                // Edit button
                                                OutlinedButton(
                                                    onClick = {
                                                        editJobIdForDesc = job.id
                                                        editedJobDescText = job.description
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(30.dp).testTag("btn_edit_job_desc_${job.id}")
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(11.dp))
                                                        Text("EDIT TEXT", fontSize = 9.sp)
                                                    }
                                                }
                                            }
                                            
                                            // Delete Permanent Button
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteJob(job.id)
                                                    Toast.makeText(context, "Vacancy definitively purged.", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp))
                                                    .testTag("btn_purge_job_${job.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Permanent",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            "companies" -> {
                // ============================================
                // WORKSPACE: VERIFY NEW COMPANY PROFILES
                // ============================================
                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    OutlinedTextField(
                        value = companySearchQuery,
                        onValueChange = { companySearchQuery = it },
                        placeholder = { Text("Search employers or enterprise recruiters...", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .testTag("admin_companies_search_input")
                    )
                    
                    // Extract unique company registries from Job Listings in system
                    val uniqueCompanies = jobsList.groupBy { it.company }.map { entry ->
                        val sampleJob = entry.value.first()
                        sampleJob // Serves as the blueprint profile model for the company
                    }.filter {
                        companySearchQuery.isBlank() ||
                        it.company.contains(companySearchQuery, ignoreCase = true) ||
                        it.companyIndustry.contains(companySearchQuery, ignoreCase = true)
                    }
                    
                    if (uniqueCompanies.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No enterprise corporate registries found.", fontSize = 11.sp, color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            items(uniqueCompanies) { comp ->
                                val isVerified = verifiedCompanyNames.contains(comp.company)
                                
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (isVerified) 1.dp else 0.5.dp,
                                            color = if (isVerified) Color(0xFF10B981).copy(alpha = 0.4f) else Color.LightGray.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(42.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFF1F5F9)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = comp.companyLogo, fontSize = 24.sp)
                                                }
                                                Column {
                                                    Text(
                                                        text = comp.company,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                    Text(
                                                        text = "${comp.companyType} • ${comp.companyIndustry}",
                                                        fontSize = 10.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                            
                                            // Badge
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (isVerified) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFF59E0B).copy(alpha = 0.12f)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isVerified) "VERIFIED" else "PENDING AUDIT",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isVerified) Color(0xFF047857) else Color(0xFFD97706)
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Column(
                                            modifier = Modifier
                                                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                                .padding(8.dp)
                                                .fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Office location:", fontSize = 9.sp, color = Color.Gray)
                                                Text("${comp.companyCity}, ${comp.companyCountry}", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Compliance Contact HR:", fontSize = 9.sp, color = Color.Gray)
                                                Text(comp.companyHrContact, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("HR Registry Email:", fontSize = 9.sp, color = Color.Gray)
                                                Text(comp.companyEmail, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Corporate Web:", fontSize = 9.sp, color = Color.Gray)
                                                Text(comp.companyWebsite, fontSize = 9.sp, color = Color(0xFFE28743), fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = comp.companyAbout,
                                            fontSize = 9.5.sp,
                                            color = Color.Gray,
                                            lineHeight = 12.sp,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                        
                                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color.LightGray.copy(alpha = 0.4f))
                                        
                                        // Verification Action button
                                        Button(
                                            onClick = {
                                                if (isVerified) {
                                                    viewModel.unverifyCompany(comp.company)
                                                    Toast.makeText(context, "${comp.company} verification status revoked.", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    viewModel.verifyCompany(comp.company)
                                                    Toast.makeText(context, "${comp.company} Profile Verified successfully! ✅", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isVerified) Color(0xFFF59E0B) else Color(0xFF10B981)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(34.dp)
                                                .testTag("btn_verify_comp_${comp.company}")
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                val bIcon = if (isVerified) Icons.Default.Warning else Icons.Default.Check
                                                Icon(bIcon, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.White)
                                                Text(
                                                    text = if (isVerified) "REVOKE TRUST CERTIFICATE" else "GRANT ELITE VERIFIED CERTIFICATE  🏆",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            "docs" -> {
                // ============================================
                // WORKSPACE: MODERATE COMPANY DOCUMENTS
                // ============================================
                val companyRegistrationDocs by viewModel.companyRegistrationDocs.collectAsStateWithLifecycle()
                var docFilterStatus by remember { mutableStateOf("All") }
                
                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    // Search bar
                    OutlinedTextField(
                        value = docsSearchQuery,
                        onValueChange = { docsSearchQuery = it },
                        placeholder = { Text("Search by company name, ID or registration number...", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .testTag("admin_docs_search_input")
                    )
                    
                    // Filter row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val statusFilters = listOf("All", "Pending", "Approved", "Rejected")
                        statusFilters.forEach { status ->
                            val isFilterSelected = docFilterStatus == status
                            val filterColor = when (status) {
                                "Approved" -> Color(0xFF10B981)
                                "Pending" -> Color(0xFFF59E0B)
                                "Rejected" -> Color(0xFFEF4444)
                                else -> Color(0xFF475569)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isFilterSelected) filterColor else Color.White)
                                    .border(
                                        width = 0.5.dp,
                                        color = if (isFilterSelected) Color.Transparent else Color.LightGray,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { docFilterStatus = status }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("doc_filter_$status"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = status.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFilterSelected) Color.White else Color.Gray
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val filteredDocs = companyRegistrationDocs.filter { doc ->
                        val matchesSearch = docsSearchQuery.isBlank() ||
                                doc.companyName.contains(docsSearchQuery, ignoreCase = true) ||
                                doc.registrationNumber.contains(docsSearchQuery, ignoreCase = true) ||
                                doc.documentType.contains(docsSearchQuery, ignoreCase = true)
                        
                        val matchesStatus = docFilterStatus == "All" || doc.documentStatus.equals(docFilterStatus, ignoreCase = true)
                        matchesSearch && matchesStatus
                    }
                    
                    if (filteredDocs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                                Text("No company registration documents matched.", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            items(filteredDocs) { doc ->
                                val isDocApproved = doc.documentStatus == "Approved"
                                val isDocRejected = doc.documentStatus == "Rejected"
                                val isVerified = verifiedCompanyNames.contains(doc.companyName)
                                
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(1.dp, RoundedCornerShape(16.dp))
                                        .border(
                                            width = if (isDocApproved) 1.5.dp else 0.5.dp,
                                            color = when {
                                                isDocApproved -> Color(0xFF10B981).copy(alpha = 0.5f)
                                                isDocRejected -> Color(0xFFEF4444).copy(alpha = 0.5f)
                                                else -> Color.LightGray.copy(alpha = 0.5f)
                                            },
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        // Header: Name & Badge
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Business,
                                                        contentDescription = null,
                                                        tint = Color(0xFF475569),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = doc.companyName,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                    Text(
                                                        text = "Submitted: ${doc.submissionDate}",
                                                        fontSize = 9.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                            
                                            // Document Status Badge
                                            val badgeBg = when (doc.documentStatus) {
                                                "Approved" -> Color(0xFF10B981).copy(alpha = 0.12f)
                                                "Rejected" -> Color(0xFFEF4444).copy(alpha = 0.12f)
                                                else -> Color(0xFFF59E0B).copy(alpha = 0.12f)
                                            }
                                            val badgeText = when (doc.documentStatus) {
                                                "Approved" -> Color(0xFF047857)
                                                "Rejected" -> Color(0xFFB91C1C)
                                                else -> Color(0xFFD97706)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(badgeBg)
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = doc.documentStatus.uppercase(),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = badgeText
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        // Metadata Fields Registry
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Document Type:", fontSize = 9.5.sp, color = Color.Gray)
                                                Text(doc.documentType, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Registration ID:", fontSize = 9.5.sp, color = Color.Gray)
                                                Text(doc.registrationNumber, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Address Jurisdiction:", fontSize = 9.5.sp, color = Color.Gray)
                                                Text(doc.location, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Corporate Registry Capital:", fontSize = 9.5.sp, color = Color.Gray)
                                                Text(doc.capitalRegistered, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        // OFFICIAL ATTACHMENT CARD PREVIEW
                                        Text(
                                            text = "ATTACHED FILE PREVIEW:",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF475569),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Card(
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                            border = BorderStroke(0.5.dp, Color(0xFFFEF3C7)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.padding(bottom = 6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Description,
                                                        contentDescription = "PDF Document",
                                                        tint = Color(0xFFD97706),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(
                                                        text = doc.fileTitle,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF92400E)
                                                    )
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    Text(
                                                        text = "SHA-256 Validated ✓",
                                                        fontSize = 8.sp,
                                                        color = Color(0xFF059669),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                
                                                Text(
                                                    text = doc.fileContentPreview,
                                                    fontSize = 9.5.sp,
                                                    color = Color.DarkGray,
                                                    lineHeight = 13.sp,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        // SINGLE CLICK TOGGLE VERIFICATION STATUS ROW
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (!isDocRejected) {
                                                Button(
                                                    onClick = {
                                                        viewModel.rejectCompanyDocument(doc.id)
                                                        Toast.makeText(context, "Documents rejected. Company verification revoked.", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(34.dp)
                                                        .testTag("btn_reject_doc_${doc.id}")
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                                        Text("REJECT FILE", fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                    }
                                                }
                                            }
                                            
                                            val actionColor = if (isDocApproved) Color(0xFFF59E0B) else Color(0xFF10B981)
                                            val actionText = if (isDocApproved) "REVOKE STATUS" else "APPROVE & VERIFY"
                                            val actionIcon = if (isDocApproved) Icons.Default.Close else Icons.Default.CheckCircle
                                            
                                            Button(
                                                onClick = {
                                                    viewModel.toggleCompanyDocumentStatus(doc.id)
                                                    val statusMsg = if (isDocApproved) {
                                                        "Registration status revoked for ${doc.companyName}."
                                                    } else {
                                                        "Registration approved & ${doc.companyName} is verified successfully! ✓"
                                                    }
                                                    Toast.makeText(context, statusMsg, Toast.LENGTH_LONG).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = actionColor),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .weight(1.5f)
                                                    .height(34.dp)
                                                    .testTag("btn_toggle_verify_doc_${doc.id}")
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Icon(actionIcon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                                    Text(actionText, fontSize = 9.sp, fontWeight = FontWeight.Black) } } } } } } } } } } /*
                                                }
                                            }
                                        }
                                    }
                                }
                    }
                }
            }
            
            */ "audit" -> {
                // ============================================
                // WORKSPACE: COMPLIANCE & SECURITY AUDIT TRAIL LOGS
                // ============================================
                val auditLogs by (if (viewModel != null) viewModel.adminAuditLogs.collectAsStateWithLifecycle() else remember { mutableStateOf(emptyList<String>()) })
                var logSearchQuery by remember { mutableStateOf("") }
                
                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Text(
                        text = "🛡️ SECURE SYSTEM AUDIT TRAIL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF6D28D9),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Real-time cryptographically traceable system records. Reflects user actions, corporate credentials validation submissions, and automated moderation alerts.",
                        fontSize = 9.5.sp,
                        color = Color.Gray,
                        lineHeight = 13.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    
                    // Search box for filters
                    OutlinedTextField(
                        value = logSearchQuery,
                        onValueChange = { logSearchQuery = it },
                        placeholder = { Text("Filter system event logs...", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    )
                    
                    // Stats Summary card for Compliance health
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SYSTEM TRUST STATUS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("SECURE ✅", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF047857))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("COMPLIANCE RATE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("98.4%", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF1D4ED8))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("AUDIT COVERAGE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("100% LIVE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF6D28D9))
                            }
                        }
                    }
                    
                    // The Console box!
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF22C55E)))
                                    Text("live_compliance_stream.sh", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                }
                                Text("UTC TIME MONITORED", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            }
                            
                            val filteredLogs = auditLogs.filter {
                                logSearchQuery.isBlank() || it.contains(logSearchQuery, ignoreCase = true)
                            }
                            
                            if (filteredLogs.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No matching audit logs found in stream buffer.", fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(filteredLogs) { log ->
                                        Text(
                                            text = log,
                                            color = if (log.contains("⚠️") || log.contains("❌") || log.contains("🚩")) Color(0xFFFCA5A5) else Color(0xFF86EFAC),
                                            fontSize = 9.5.sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            lineHeight = 12.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Option to trigger mock activity
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Sec-Audit Node: Node-039-Kat", fontSize = 8.5.sp, color = Color.Gray)
                        Button(
                            onClick = {
                                if (viewModel != null) {
                                    viewModel.addAuditLog("🔋 Compliance officer requested system memory status check. Node health is 100%. Buffer clean.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("Trigger Health Check ⚡", fontSize = 8.5.sp, color = Color.White)
                        }
                    }
                }
            }
            
            "ignored_placeholder_dead_code" -> {
                val appAccounts by (if (viewModel != null) viewModel.appAccounts.collectAsStateWithLifecycle() else remember { mutableStateOf(emptyList<com.example.data.AppAccount>()) })
                var userSearchQuery by remember { mutableStateOf("") }
                var userFilterRole by remember { mutableStateOf("All") }
                var userFilterStatus by remember { mutableStateOf("All") }
                var expandedId by remember { mutableStateOf<String?>(null) }
                
                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    // Search bar
                    OutlinedTextField(
                        value = userSearchQuery,
                        onValueChange = { userSearchQuery = it },
                        placeholder = { Text("Search by user name, email, or credentials...", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .testTag("admin_users_search_input")
                    )
                    
                    // Filter row: Role & Status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Role filter chips
                        listOf("All", "Candidate", "Company", "Admin").forEach { role ->
                            val isSelected = userFilterRole == role
                            val filterColor = when (role) {
                                "Admin" -> Color(0xFF8B5CF6)
                                "Company" -> Color(0xFF10B981)
                                "Candidate" -> Color(0xFF3B82F6)
                                else -> Color(0xFF475569)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) filterColor else Color(0xFFF1F5F9))
                                    .clickable { userFilterRole = role }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("user_filter_role_$role")
                            ) {
                                Text(
                                    text = role.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color.Gray
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status Filter:", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        listOf("All", "Active", "Deactivated").forEach { status ->
                            val isSelected = userFilterStatus == status
                            val filterColor = if (status == "Active") Color(0xFF10B981) else if (status == "Deactivated") Color(0xFFEF4444) else Color(0xFF475569)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) filterColor.copy(alpha = 0.2f) else Color.Transparent)
                                    .border(1.dp, if (isSelected) filterColor else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .clickable { userFilterStatus = status }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("user_filter_status_$status")
                            ) {
                                Text(
                                    text = status,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) filterColor else Color.Gray
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val filteredAccounts = appAccounts.filter { acc ->
                        val matchesSearch = userSearchQuery.isBlank() ||
                                acc.name.contains(userSearchQuery, ignoreCase = true) ||
                                acc.email.contains(userSearchQuery, ignoreCase = true)
                        
                        val matchesRole = userFilterRole == "All" || acc.role.equals(userFilterRole, ignoreCase = true)
                        val matchesStatus = userFilterStatus == "All" || acc.status.equals(userFilterStatus, ignoreCase = true)
                        matchesSearch && matchesRole && matchesStatus
                    }
                    
                    if (filteredAccounts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                                Text("No user accounts matched the filter.", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // SECURE ADMINISTRATIVE TABLE GRID
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .shadow(2.dp, RoundedCornerShape(16.dp))
                                .border(0.5.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // 1. TABLE COLUMN HEADERS HEADER ROW
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F172A))
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(modifier = Modifier.weight(3.2f), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFFEF08A), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("NAME & SECURITY INFO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                        Row(modifier = Modifier.weight(2.0f), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFFEF08A), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("PLATFORM ROLE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                        Row(modifier = Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFEF08A), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("STATUS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                        Row(modifier = Modifier.weight(3.3f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                                            Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFFFEF08A), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ACTIONS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }

                                // 2. SCROLLABLE DATABASE ROWS
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(filteredAccounts) { acc ->
                                        val isDeactivated = acc.status == "Deactivated"
                                        val isSelf = acc.email.lowercase() == "udayarajkhanal21@gmail.com" || 
                                                     acc.email.lowercase() == "udayarajkhanal25@gmail.com" || 
                                                     acc.email.lowercase() == "info7stargo@gmail.com"
                                        val isExpanded = expandedId == acc.id

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (isExpanded) Color(0xFFF1F5F9) else Color.White)
                                                .clickable { expandedId = if (isExpanded) null else acc.id }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Column 1: Info (Name & Email details) - 3.2f
                                                Row(
                                                    modifier = Modifier.weight(3.2f),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(if (isDeactivated) Color(0xFFFEE2E2) else Color(0xFFEFF6FF)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        val cellIcon = when (acc.role) {
                                                            "Admin" -> Icons.Default.Shield
                                                            "Company" -> Icons.Default.Business
                                                            else -> Icons.Default.Person
                                                        }
                                                        val cellColor = when (acc.role) {
                                                            "Admin" -> Color(0xFF7C3AED)
                                                            "Company" -> Color(0xFF0D9488)
                                                            else -> Color(0xFF2563EB)
                                                        }
                                                        Icon(cellIcon, contentDescription = null, tint = if (isDeactivated) Color.Gray else cellColor, modifier = Modifier.size(14.dp))
                                                    }
                                                    
                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            Text(
                                                                text = acc.name,
                                                                fontSize = 11.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isDeactivated) Color.Gray else Color(0xFF1E293B),
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            if (isSelf) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(4.dp))
                                                                        .background(Color(0xFFEDE9FE))
                                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                                ) {
                                                                    Text("YOU", fontSize = 6.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF6D28D9))
                                                                }
                                                            }
                                                        }
                                                        Text(
                                                            text = acc.email,
                                                            fontSize = 9.5.sp,
                                                            color = Color.Gray,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                                
                                                // Column 2: Platform Role badge - 2.0f
                                                Box(modifier = Modifier.weight(2.0f)) {
                                                    val (roleBg, roleText) = when (acc.role) {
                                                        "Admin" -> Color(0xFFEDE9FE) to Color(0xFF6D28D9)
                                                        "Company" -> Color(0xFFD1FAE5) to Color(0xFF065F46)
                                                        else -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
                                                    }
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(roleBg)
                                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                                    ) {
                                                        Text(
                                                            text = acc.role.uppercase(),
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = roleText
                                                        )
                                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = roleText, modifier = Modifier.size(10.dp))
                                                    }
                                                }
                                                
                                                // Column 3: Security Status badge - 1.5f
                                                Box(modifier = Modifier.weight(1.5f)) {
                                                    val (statusBg, statusText) = if (isDeactivated) {
                                                        Color(0xFFFEE2E2) to Color(0xFF991B1B)
                                                    } else {
                                                        Color(0xFFECFDF5) to Color(0xFF065F46)
                                                    }
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(statusBg)
                                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(RoundedCornerShape(50))
                                                                .background(statusText)
                                                        )
                                                        Text(
                                                            text = acc.status.uppercase(),
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = statusText
                                                        )
                                                    }
                                                }
                                                
                                                // Column 4: Security Action Triggers - 3.3f
                                                Row(
                                                    modifier = Modifier.weight(3.3f),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Promote Option
                                                    OutlinedButton(
                                                        onClick = {
                                                            if (isSelf) {
                                                                Toast.makeText(context, "Administrative sessions are permanently authorized.", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                val newRole = when (acc.role) {
                                                                    "Candidate" -> "Company"
                                                                    "Company" -> "Admin"
                                                                    else -> "Candidate"
                                                                }
                                                                viewModel.updateAccountRole(acc.id, newRole)
                                                                Toast.makeText(context, "Elevated ${acc.name} status to $newRole! 🚀", Toast.LENGTH_SHORT).show()
                                                            }
                                                        },
                                                        colors = ButtonDefaults.outlinedButtonColors(
                                                            contentColor = if (isDeactivated) Color.Gray else Color(0xFF6D28D9)
                                                        ),
                                                        border = BorderStroke(1.dp, if (isDeactivated) Color.LightGray else Color(0xFFC084FC)),
                                                        shape = RoundedCornerShape(8.dp),
                                                        enabled = !isSelf,
                                                        modifier = Modifier
                                                            .height(28.dp)
                                                            .testTag("btn_promote_user_${acc.id}"),
                                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = if (isDeactivated) Color.Gray else Color(0xFF6D28D9), modifier = Modifier.size(10.dp))
                                                            Text("PROMOTE", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                    
                                                    // Lock/Unlock Toggle
                                                    Button(
                                                        onClick = {
                                                            if (isSelf) {
                                                                Toast.makeText(context, "Can't lock own local administrator terminal!", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                viewModel.toggleAccountStatus(acc.id)
                                                                val finalMsg = if (isDeactivated) "Account Restored!" else "Account Locked & Restricted!"
                                                                Toast.makeText(context, finalMsg, Toast.LENGTH_SHORT).show()
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (isDeactivated) Color(0xFF10B981) else Color(0xFFEF4444)
                                                        ),
                                                        shape = RoundedCornerShape(8.dp),
                                                        enabled = !isSelf,
                                                        modifier = Modifier
                                                            .height(28.dp)
                                                            .testTag("btn_deactivate_user_${acc.id}"),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = if (isDeactivated) "RESTORE" else "LOCK",
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.ExtraBold
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                                            
                                            // Expanding Detail Row drawer
                                            if (isExpanded) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color(0xFFF8FAFC))
                                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                                ) {
                                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                            Text("System Sign-up Date:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                                            Text(acc.signupDate.ifEmpty { "N/A" }, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                                        }
                                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                            Text("Last Heartbeat Check:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                                            Text(acc.lastActive.ifEmpty { "N/A" }, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                                        }
                                                        if (acc.extraDetails.isNotEmpty()) {
                                                            Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                                                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(10.dp).padding(top = 1.dp))
                                                                Text(
                                                                    text = "System Operator Notes: " + acc.extraDetails,
                                                                    fontSize = 9.sp,
                                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                                    color = Color.DarkGray,
                                                                    lineHeight = 12.sp
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                Divider(color = Color.LightGray.copy(alpha = 0.4f), thickness = 0.5.dp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            "reports" -> {
                // ============================================
                // WORKSPACE: MANAGE USER SPAM & FLAG REPORTS
                // ============================================
                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    OutlinedTextField(
                        value = reportsSearchQuery,
                        onValueChange = { reportsSearchQuery = it },
                        placeholder = { Text("Filter reports by suspect name or email...", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .testTag("admin_reports_search_input")
                    )
                    
                    val filteredReports = userReports.filter {
                        reportsSearchQuery.isBlank() ||
                        it.suspectTitle.contains(reportsSearchQuery, ignoreCase = true) ||
                        it.suspectCompany.contains(reportsSearchQuery, ignoreCase = true) ||
                        it.reporterEmail.contains(reportsSearchQuery, ignoreCase = true)
                    }
                    
                    if (filteredReports.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No open compliance flags.", fontSize = 11.sp, color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            items(filteredReports) { report ->
                                val isResolved = report.status == "Resolved"
                                
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (isResolved) 0.5.dp else 1.5.dp,
                                            color = if (isResolved) Color.LightGray else Color(0xFFF59E0B).copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Ticket: ${report.id}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Gray
                                            )
                                            
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (isResolved) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = report.status.uppercase(),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isResolved) Color(0xFF047857) else Color(0xFFEF4444)
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Text(
                                            text = "Suspect Target: ${report.suspectTitle}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "Employer: ${report.suspectCompany} • Type: ${report.targetType.uppercase()}",
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                        
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        // Cause Card
                                        Card(
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "REPORTED REASON & ALLEGATION:",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFEF4444)
                                                )
                                                Text(
                                                    text = report.reason,
                                                    fontSize = 9.5.sp,
                                                    color = Color.DarkGray,
                                                    lineHeight = 12.sp,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Reporter: ${report.reporterEmail}", fontSize = 8.5.sp, color = Color.Gray)
                                                Text("Date filed: ${report.reportDate}", fontSize = 8.5.sp, color = Color.Gray)
                                            }
                                            
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                // Resolve button
                                                if (!isResolved) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.resolveReport(report.id)
                                                            Toast.makeText(context, "Complaint status changed to Resolved! ✅", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                        shape = RoundedCornerShape(6.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                                        modifier = Modifier.height(28.dp).testTag("btn_resolve_report_${report.id}")
                                                    ) {
                                                        Text("RESOLVE TICKET", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }
                                                
                                                // Take down or flag suspect if job exists and not already flagged
                                                val isTargetJobFlagged = flaggedJobIds.contains(report.targetId)
                                                if (report.targetType == "job" && !isTargetJobFlagged) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.flagJob(report.targetId)
                                                            viewModel.resolveReport(report.id)
                                                            Toast.makeText(context, "Content flagged & hidden immediately. Ticket solved.", Toast.LENGTH_SHORT).show()
                                                        },
                                                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                                        shape = RoundedCornerShape(6.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                                        modifier = Modifier.height(28.dp).testTag("btn_takedown_${report.id}")
                                                    ) {
                                                        Text("TAKE DOWN", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                                    }
                                                }
                                                
                                                // Purge report ticket
                                                IconButton(
                                                    onClick = {
                                                        viewModel.deleteReport(report.id)
                                                        Toast.makeText(context, "Complaint ticket deleted.", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                                        .testTag("btn_delete_report_${report.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Clear,
                                                        contentDescription = "Delete Ticket",
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(12.dp)
                                                    )
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
        }
    }
}
