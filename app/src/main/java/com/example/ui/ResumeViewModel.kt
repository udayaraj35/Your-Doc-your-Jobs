package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ResumeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ResumeRepository = ResumeRepository(AppDatabase.getDatabase(application).resumeDao())

    private val _hiveResumes = MutableStateFlow<List<Resume>>(emptyList())
    val hiveResumes: StateFlow<List<Resume>> = _hiveResumes.asStateFlow()

    fun refreshHiveResumes() {
        try {
            val box = Hive.box<Resume>("resumes")
            _hiveResumes.value = box.values().sortedByDescending { it.updatedAt }
        } catch (e: Exception) {
            _hiveResumes.value = emptyList()
        }
    }

    val vaultDocumentsState = MutableStateFlow<List<VaultDocument>>(emptyList())

    fun loadVaultDocuments() {
        try {
            val file = java.io.File(getApplication<Application>().filesDir, "vault_documents.json")
            if (file.exists()) {
                val json = file.readText()
                val list = JsonParser.fromJsonList<VaultDocument>(json)
                vaultDocumentsState.value = list
            } else {
                val sampleDocs = listOf(
                    VaultDocument(
                        id = "sample_pcc",
                        type = "pcc",
                        title = "Police Clearance Certificate (PCC)",
                        isEnabled = true,
                        pccFullName = "Hari Bahadur",
                        pccAuthority = "District Police Headquarters, Kathmandu",
                        pccIssueDate = "2026-05-15",
                        pccStatus = "Cleared / No Criminal Record"
                    ),
                    VaultDocument(
                        id = "sample_passport",
                        type = "passport",
                        title = "Passport Credential Doc",
                        isEnabled = true,
                        passportNo = "N1234567",
                        passportFullName = "Hari Bahadur",
                        passportCountry = "Nepal",
                        passportDob = "1998-08-20",
                        passportIssueDate = "2022-03-10",
                        passportExpiryDate = "2032-03-09"
                    ),
                    VaultDocument(
                        id = "sample_exp",
                        type = "experience",
                        title = "Software Engineer Experience Certificate",
                        isEnabled = true,
                        expCompany = "TechMinds Global",
                        expRole = "Lead Android Developer",
                        expStartDate = "2023-01-10",
                        expEndDate = "2025-12-31",
                        expDetails = "Designed and scaled high-quality dynamic carrier portals and offline SQLite database solutions. Managed small group of developers to roll out highly successful features."
                    )
                )
                vaultDocumentsState.value = sampleDocs
                saveVaultDocuments(sampleDocs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveVaultDocuments(docs: List<VaultDocument>) {
        try {
            val file = java.io.File(getApplication<Application>().filesDir, "vault_documents.json")
            val json = JsonParser.toJsonList(docs)
            file.writeText(json)
            vaultDocumentsState.value = docs
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addVaultDocument(doc: VaultDocument) {
        val current = vaultDocumentsState.value.toMutableList()
        current.add(doc)
        saveVaultDocuments(current)
    }

    fun updateVaultDocument(updated: VaultDocument) {
        val current = vaultDocumentsState.value.map { if (it.id == updated.id) updated else it }
        saveVaultDocuments(current)
    }

    fun deleteVaultDocument(id: String) {
        val current = vaultDocumentsState.value.filter { it.id != id }
        saveVaultDocuments(current)
    }

    val userProfileState = MutableStateFlow<UserProfile?>(null)
    val jobApplicationsState = MutableStateFlow<List<JobApplication>>(emptyList())
    val notificationsState = MutableStateFlow<List<SmartNotification>>(emptyList())

    fun loadUserProfile() {
        try {
            val box = Hive.box<UserProfile>("user_profile")
            var profile = box.get("default_user_profile")
            if (profile == null) {
                profile = UserProfile(
                    fullName = "Saru Sharma",
                    email = "sarusharma@gmail.com",
                    phone = "+977-9851088888",
                    professionalSummary = "Motivated and analytical Senior Software Engineer with over 5 years of certified expertise designing high-performance mobile architectures, cross-document synchronization servers, and adaptive UI layouts in Kotlin and Jetpack Compose. Proven track record of elevating client conversion metrics by 40%.",
                    preferredJobTitle = "Senior Software Engineer",
                    country = "Nepal",
                    city = "Kathmandu",
                    education = "Bachelor of Science in Computer Science & Information Technology, Tribhuvan University, GPA 3.9/4.0",
                    experience = "Orchestrated the architectural migration of high-concurrency client-facing portals at Apex Global Enterprises. Managed cross-functional development cycles, integrated modern local caching services, and designed pristine reusable components.",
                    skills = "Kotlin, Jetpack Compose, Clean Architecture, REST APIs, Swift, CI/CD, Agile Leadership",
                    languages = "Nepali (Native), English (Fluent), Hindi (Conversational)"
                )
                box.put("default_user_profile", profile)
            }
            userProfileState.value = profile
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        try {
            val box = Hive.box<UserProfile>("user_profile")
            box.put("default_user_profile", profile)
            userProfileState.value = profile
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadJobApplications() {
        try {
            val file = java.io.File(getApplication<Application>().filesDir, "job_applications.json")
            if (file.exists()) {
                val json = file.readText()
                val list = JsonParser.fromJsonList<JobApplication>(json)
                jobApplicationsState.value = list
            } else {
                jobApplicationsState.value = emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveJobApplications(apps: List<JobApplication>) {
        try {
            val file = java.io.File(getApplication<Application>().filesDir, "job_applications.json")
            val json = JsonParser.toJsonList(apps)
            file.writeText(json)
            jobApplicationsState.value = apps
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applyToJob(
        jobId: String, 
        jobTitle: String, 
        companyName: String, 
        resumeIdUsed: Int = 0, 
        coverLetterText: String = "",
        status: String = "Pending Review"
    ) {
        val profile = userProfileState.value ?: UserProfile(fullName = "Anonymous Candidate")
        val today = java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.ENGLISH).format(java.util.Date())
        val newApp = JobApplication(
            jobId = jobId,
            jobTitle = jobTitle,
            companyName = companyName,
            appliedDate = today,
            applicantName = profile.fullName.ifBlank { "Candidate User" },
            applicantEmail = profile.email,
            applicantPhone = profile.phone,
            userProfileJson = JsonParser.toJson(profile),
            status = status,
            coverLetterText = coverLetterText,
            resumeIdUsed = resumeIdUsed
        )
        val current = jobApplicationsState.value.toMutableList()
        current.add(newApp)
        saveJobApplications(current)

        // Trigger smart notification
        addNotification(
            title = "Application Submitted Successfully",
            body = "You have successfully applied for $jobTitle position at $companyName using your active resume.",
            type = "status"
        )
    }

    fun deleteJobApplication(id: String) {
        val current = jobApplicationsState.value.filter { it.id != id }
        saveJobApplications(current)
    }

    fun updateApplicationStatus(id: String, newStatus: String, note: String = "") {
        val current = jobApplicationsState.value.map {
            if (it.id == id) {
                it.copy(status = newStatus, note = note)
            } else {
                it
            }
        }
        saveJobApplications(current)
        
        // Find updated application
        val app = current.find { it.id == id }
        if (app != null) {
            addNotification(
                title = "Application Status Updated",
                body = "Your application status for ${app.jobTitle} at ${app.companyName} has been updated to '$newStatus'.",
                type = if (newStatus.contains("Shortlist") || newStatus.contains("Invited")) "invitation" else "status"
            )
        }
    }

    fun loadNotifications() {
        try {
            val file = java.io.File(getApplication<Application>().filesDir, "notifications.json")
            if (file.exists()) {
                val json = file.readText()
                val list = JsonParser.fromJsonList<SmartNotification>(json)
                notificationsState.value = list
            } else {
                // Seed some custom sample notifications based on career matching
                val sampleNotifications = listOf(
                    SmartNotification(
                        title = "🎯 High Match Smart Recommendations",
                        body = "Our AI has identified that your active profile is an 88% structural match for the Senior UI/UX & Interaction Designer vacancy!",
                        type = "matching"
                    ),
                    SmartNotification(
                        title = "🚀 Welcome to YourDoc Career Portal",
                        body = "Begin building elite standard resume documents, optimize for CV formatting, and apply to international corporate vacancies with 1-click.",
                        type = "status"
                    )
                )
                notificationsState.value = sampleNotifications
                saveNotifications(sampleNotifications)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveNotifications(notifs: List<SmartNotification>) {
        try {
            val file = java.io.File(getApplication<Application>().filesDir, "notifications.json")
            val json = JsonParser.toJsonList(notifs)
            file.writeText(json)
            notificationsState.value = notifs
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addNotification(title: String, body: String, type: String = "matching") {
        val current = notificationsState.value.toMutableList()
        current.add(0, SmartNotification(title = title, body = body, type = type))
        saveNotifications(current)
    }

    fun deleteNotification(id: String) {
        val current = notificationsState.value.filter { it.id != id }
        saveNotifications(current)
    }

    fun markAllAsRead() {
        val current = notificationsState.value.map { it.copy(isRead = true) }
        saveNotifications(current)
    }

    fun autoGenerateCvFromProfile(profile: UserProfile, style: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val emailStr = profile.email.ifBlank { "developer@example.com" }
            val nameStr = profile.fullName.ifBlank { "Elite Professional" }

            val pi = PersonalInfo(
                fullName = nameStr,
                email = emailStr,
                phone = profile.phone,
                currentCountry = profile.country,
                city = profile.city,
                currentAddress = if (profile.city.isNotBlank()) "${profile.city}, ${profile.country}" else profile.country
            )

            val weList = if (profile.experience.isNotBlank()) {
                listOf(
                    WorkExperience(
                        companyName = "Apex Technology Solutions",
                        jobPosition = profile.preferredJobTitle.ifBlank { "Developer" },
                        country = profile.country,
                        city = profile.city,
                        startDate = "2023-01",
                        endDate = "Present",
                        isCurrentlyWorking = true,
                        responsibilities = profile.experience
                    )
                )
            } else emptyList()

            val eduList = if (profile.education.isNotBlank()) {
                listOf(
                    Education(
                        schoolName = profile.education,
                        degree = "Specialist Academic Degree",
                        country = profile.country,
                        city = profile.city,
                        gpa = "Excellent",
                        startDate = "2018",
                        endDate = "2022"
                    )
                )
            } else emptyList()

            val skList = profile.skills.split(",").mapNotNull {
                val trimmed = it.trim()
                if (trimmed.isNotBlank()) Skill(trimmed, 90, "Technical") else null
            }

            val langList = profile.languages.split(",").mapNotNull {
                val trimmed = it.trim()
                if (trimmed.isNotBlank()) Language(trimmed, "Fluent", "Fluent", "Fluent", "Fluent") else null
            }

            val ab = AboutMe(
                summary = profile.professionalSummary.ifBlank { "Passionate developer dedicated to standard software patterns and clean code design." },
                careerObjective = "To secure an elite professional assignment matching my qualifications and operational background."
            )

            val templateId = when (style) {
                "ATS" -> "3"
                "Europass" -> "2"
                else -> "1"
            }
            val template = CvTemplate.PREDEFINED_TEMPLATES.find { it.id == templateId } ?: CvTemplate.PREDEFINED_TEMPLATES[0]

            val cust = Customization(
                templateId = template.id,
                primaryColorHex = template.primaryColorHex,
                headerColorHex = template.headerColorHex,
                sections = INITIAL_SECTIONS
            )

            val resume = Resume(
                id = 0,
                title = "Auto Generated $style CV",
                updatedAt = System.currentTimeMillis(),
                personalInfo = JsonParser.toJson(pi),
                workExperiences = JsonParser.toJsonList(weList),
                educations = JsonParser.toJsonList(eduList),
                skills = JsonParser.toJsonList(skList),
                languages = JsonParser.toJsonList(langList),
                aboutMe = JsonParser.toJson(ab),
                customization = JsonParser.toJson(cust),
                declaration = JsonParser.toJson(Declaration(text = "I solemnly declare that all statements are true and correct.")),
                coverLetter = JsonParser.toJson(CoverLetter(bodyText = "Dear Hiring Committee,\n\nI am presenting my candidacy. Thank you."))
            )

            val newId = repository.insertResume(resume)
            onComplete(newId.toInt())
        }
    }

    val jobsState = MutableStateFlow<List<JobListing>>(emptyList())

    fun loadJobs() {
        try {
            val file = java.io.File(getApplication<Application>().filesDir, "jobs_listings.json")
            if (file.exists()) {
                val json = file.readText()
                val list = JsonParser.fromJsonList<JobListing>(json)
                jobsState.value = list
            } else {
                val sampleJobs = listOf(
                    JobListing(
                        id = "sample_job_1",
                        title = "Kotlin Android Developer (Junior/Mid)",
                        positionName = "Android Software Engineer",
                        country = "Nepal",
                        city = "Kathmandu",
                        fullAddress = "Lalitpur Lane 7, Kathmandu, Nepal",
                        salary = "90,000",
                        currency = "NPR",
                        dutyHours = "8 hours",
                        shiftType = "Day Shift",
                        experienceRequired = "1-2 years",
                        educationRequired = "Bachelor of Computer Science / IT Degree",
                        skillsRequired = "Kotlin, Jetpack Compose, Room DB, Retrofit, Coroutines",
                        languagesRequired = "English, Nepali",
                        visaSupport = false,
                        accommodation = true,
                        foodSupport = true,
                        transportation = false,
                        contractDuration = "2 Years",
                        genderRequirement = "Any",
                        ageRequirement = "21-35 years",
                        deadline = "July 15, 2026",
                        responsibilities = "Design and build Jetpack Compose UI frames. Work with database persistence. Optimize state flows.",
                        benefits = "Annual dynamic performance bonus, health insurance, lunch program.",
                        description = "Join our elite core development team to build next-generation career document styling engines and offline-first persistence layers. You will contribute to open source and secure mobile applications.",
                        postedDate = "June 13, 2026",
                        contactEmail = "careers@yourdoc.com",
                        company = "YourDoc Tech Solutions",
                        companyLogo = "🏢",
                        companyType = "Tech Startup",
                        companyIndustry = "Software development",
                        companyCountry = "Nepal",
                        companyCity = "Kathmandu",
                        companyAddress = "Mid-Town Lalitpur, Kathmandu",
                        companyWebsite = "https://yourdoc.com",
                        companyHrContact = "Udaya Raj Khanal (HR Lead)",
                        companyPhone = "+977-9800000000",
                        companyEmail = "hr@yourdoc.com",
                        companyAbout = "YourDoc is a growing international career tech platform specializing in digital document generation, modern resumes, and intelligent career paths."
                    ),
                    JobListing(
                        id = "sample_job_2",
                        title = "Senior UI/UX & Interaction Designer",
                        positionName = "Senior Product Designer",
                        country = "United Kingdom",
                        city = "London",
                        fullAddress = "Canary Wharf Office 4B, London, UK",
                        salary = "5,500",
                        currency = "GBP",
                        dutyHours = "Flexible",
                        shiftType = "Flexible / Remote",
                        experienceRequired = "3+ years",
                        educationRequired = "Bachelor of Fine Arts or Product Design",
                        skillsRequired = "Figma, Material Design 3, Design Systems, High-Fidelity Prototyping",
                        languagesRequired = "English",
                        visaSupport = true,
                        accommodation = false,
                        foodSupport = false,
                        transportation = true,
                        contractDuration = "Indefinite",
                        genderRequirement = "Any",
                        ageRequirement = "22-55 years",
                        deadline = "August 30, 2026",
                        responsibilities = "Architect cohesive Figma design guidelines. Coordinate micro-interactions, responsive typography spacing, and high-fidelity layouts.",
                        benefits = "Home-office setup allowance, direct international relocation support, private healthcare package.",
                        description = "Refine the interaction architecture of YourDoc's resume interfaces. Collaborate with mobile and web engineers to ensure compliance with pixel-perfect Material 3 standards.",
                        postedDate = "June 12, 2026",
                        contactEmail = "hello@yourdoclabs.co.uk",
                        company = "Antigravity Creative Lab",
                        companyLogo = "🎨",
                        companyType = "Multinational Design Studio",
                        companyIndustry = "Brand Design & Tech Products",
                        companyCountry = "United Kingdom",
                        companyCity = "London",
                        companyAddress = "Shoreditch High St, London E1",
                        companyWebsite = "https://yourdoclabs.co.uk",
                        companyHrContact = "Samantha West (Recruitment Lead)",
                        companyPhone = "+44-207-900-1111",
                        companyEmail = "careers@yourdoclabs.co.uk",
                        companyAbout = "Antigravity Creative Labs crafts award-winning user interfaces, identity assets, and fluid experience designs for mobile platforms."
                    ),
                    JobListing(
                        id = "sample_job_apex",
                        title = "Remote Crypto Mining Consultant",
                        positionName = "Mining Consultant",
                        country = "United States",
                        city = "Remote",
                        fullAddress = "New York, USA",
                        salary = "12,000",
                        currency = "USD",
                        dutyHours = "Flexible",
                        shiftType = "Any",
                        experienceRequired = "No experience",
                        educationRequired = "High School Diploma",
                        skillsRequired = "Web3, Crypto, Discord engagement",
                        languagesRequired = "English",
                        visaSupport = false,
                        accommodation = false,
                        foodSupport = false,
                        transportation = false,
                        contractDuration = "Flexible",
                        genderRequirement = "Any",
                        ageRequirement = "18-50",
                        deadline = "Immediate",
                        responsibilities = "Promote and post invitation links on social channels. Facilitate onboarding of retail miners.",
                        benefits = "High commission structure based on mining team yield.",
                        description = "Urgent hire for cryptocurrency research and mining consultancy roles. High yield payouts daily. Warning: Do not pay upfront fees.",
                        postedDate = "June 14, 2026",
                        contactEmail = "scamhr@apexcorpminers.info",
                        company = "Apex Capital LLC",
                        companyLogo = "🪙",
                        companyType = "Tech Offshore Entity",
                        companyIndustry = "Cryptocurrency & Blockchain",
                        companyCountry = "United States",
                        companyCity = "Delaware",
                        companyAddress = "Delaware Corporate Services",
                        companyAbout = "Cryptocurrency mining platform focused on retail consultancy."
                    ),
                    JobListing(
                        id = "sample_job_blockchain",
                        title = "Junior Rust Developer",
                        positionName = "Rust Developer",
                        country = "United Arab Emirates",
                        city = "Dubai",
                        fullAddress = "Dubai Internet City",
                        salary = "4,500",
                        currency = "AED",
                        dutyHours = "8 hours",
                        shiftType = "Day",
                        experienceRequired = "1 year",
                        educationRequired = "Bachelors",
                        skillsRequired = "Rust, Solana, Smart Contracts",
                        languagesRequired = "English",
                        visaSupport = true,
                        accommodation = false,
                        foodSupport = false,
                        transportation = false,
                        contractDuration = "1 Year",
                        genderRequirement = "Any",
                        ageRequirement = "21-35",
                        deadline = "June 30, 2026",
                        responsibilities = "Develop smart contracts, review code, and manage Rust dependency stacks on Solana.",
                        benefits = "Base stipend, visa, transport allowance.",
                        description = "Looking for an energetic developer eager to dive into fast blockchain execution protocols.",
                        postedDate = "June 13, 2026",
                        contactEmail = "synergy@blockchaindubai.cc",
                        company = "BlockChain Synergy",
                        companyLogo = "⛓️",
                        companyType = "Startup",
                        companyIndustry = "Web3 Solutions",
                        companyCountry = "United Arab Emirates",
                        companyCity = "Dubai",
                        companyAddress = "Silicon Oasis Block B",
                        companyAbout = "Rapidly scaling web3 solutions and smart contract development agency."
                    )
                )
                jobsState.value = sampleJobs
                saveJobs(sampleJobs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveJobs(jobs: List<JobListing>) {
        try {
            val file = java.io.File(getApplication<Application>().filesDir, "jobs_listings.json")
            val json = JsonParser.toJsonList(jobs)
            file.writeText(json)
            jobsState.value = jobs
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addJob(job: JobListing) {
        val current = jobsState.value.toMutableList()
        current.add(job)
        saveJobs(current)
    }

    fun deleteJob(id: String) {
        val current = jobsState.value.filter { it.id != id }
        saveJobs(current)
    }

    val cvVersionsState = MutableStateFlow<List<CvVersionSnapshot>>(emptyList())

    fun loadCvVersions() {
        try {
            val file = java.io.File(getApplication<Application>().filesDir, "cv_versions.json")
            if (file.exists()) {
                val json = file.readText()
                val list = JsonParser.fromJsonList<CvVersionSnapshot>(json)
                cvVersionsState.value = list
            } else {
                cvVersionsState.value = emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveCvVersions(versions: List<CvVersionSnapshot>) {
        try {
            val file = java.io.File(getApplication<Application>().filesDir, "cv_versions.json")
            val json = JsonParser.toJsonList(versions)
            file.writeText(json)
            cvVersionsState.value = versions
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addCvVersion(resume: Resume, name: String) {
        val current = cvVersionsState.value.toMutableList()
        val snapshot = CvVersionSnapshot(
            id = "${System.currentTimeMillis()}",
            resumeId = resume.id,
            versionName = name,
            timestamp = System.currentTimeMillis(),
            resumeDataJson = JsonParser.toJson(resume)
        )
        current.add(snapshot)
        saveCvVersions(current)
    }

    fun rollbackToVersion(snapshot: CvVersionSnapshot, onComplete: () -> Unit = {}) {
        val resume = JsonParser.fromJson<Resume>(snapshot.resumeDataJson) ?: return
        viewModelScope.launch {
            val updatedResume = resume.copy(updatedAt = System.currentTimeMillis())
            repository.insertResume(updatedResume)
            loadResume(resume.id)
            onComplete()
        }
    }

    fun deleteCvVersion(id: String) {
        val current = cvVersionsState.value.filter { it.id != id }
        saveCvVersions(current)
    }

    fun moveVaultDocumentUp(index: Int) {
        if (index > 0 && index < vaultDocumentsState.value.size) {
            val current = vaultDocumentsState.value.toMutableList()
            val temp = current[index]
            current[index] = current[index - 1]
            current[index - 1] = temp
            saveVaultDocuments(current)
        }
    }

    fun moveVaultDocumentDown(index: Int) {
        if (index >= 0 && index < vaultDocumentsState.value.size - 1) {
            val current = vaultDocumentsState.value.toMutableList()
            val temp = current[index]
            current[index] = current[index + 1]
            current[index + 1] = temp
            saveVaultDocuments(current)
        }
    }


    val resumes: StateFlow<List<Resume>> = repository.allResumes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentResumeId = MutableStateFlow<Int?>(null)
    val currentResumeId: StateFlow<Int?> = _currentResumeId.asStateFlow()

    private val _saveStatus = MutableStateFlow("All changes saved")
    val saveStatus: StateFlow<String> = _saveStatus.asStateFlow()

    // Live variables representing the current CV draft
    val titleState = MutableStateFlow("New Professional Resume")
    val personalInfoState = MutableStateFlow(PersonalInfo())
    val passportInfoState = MutableStateFlow(PassportInfo())
    val workExperiencesState = MutableStateFlow(emptyList<WorkExperience>())
    val educationsState = MutableStateFlow(emptyList<Education>())
    val skillsState = MutableStateFlow(emptyList<Skill>())
    val languagesState = MutableStateFlow(emptyList<Language>())
    val aboutMeState = MutableStateFlow(AboutMe())
    val declarationState = MutableStateFlow(Declaration())
    val coverLetterState = MutableStateFlow(CoverLetter())
    val customizationState = MutableStateFlow(Customization())

    // API Manager Custom Keys
    val userApiKeyTypeState = MutableStateFlow("google") // "google" or "openai"
    val userApiKeyValState = MutableStateFlow("")
    val openaiModelState = MutableStateFlow("gpt-4o-mini")

    fun loadApiSettings() {
        val prefs = getApplication<Application>().getSharedPreferences("YourDocApiPrefs", android.content.Context.MODE_PRIVATE)
        userApiKeyTypeState.value = prefs.getString("api_type", "google") ?: "google"
        userApiKeyValState.value = prefs.getString("api_key", "") ?: ""
        openaiModelState.value = prefs.getString("openai_model", "gpt-4o-mini") ?: "gpt-4o-mini"
    }

    fun saveApiSettings(type: String, key: String, model: String) {
        userApiKeyTypeState.value = type
        userApiKeyValState.value = key
        openaiModelState.value = model
        val prefs = getApplication<Application>().getSharedPreferences("YourDocApiPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("api_type", type)
            putString("api_key", key)
            putString("openai_model", model)
            apply()
        }
    }

    init {
        // Periodically sync Room to Hive box and keep the Hive as our direct state provider
        viewModelScope.launch {
            repository.allResumes.collect { roomList ->
                try {
                    val box = Hive.box<Resume>("resumes")
                    for (resume in roomList) {
                        box.put(resume.id.toString(), resume)
                    }
                    // Handle deletion sync
                    val roomIds = roomList.map { it.id.toString() }.toSet()
                    for (hiveKey in box.keys()) {
                        if (hiveKey !in roomIds) {
                            box.delete(hiveKey)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                refreshHiveResumes()
            }
        }
        loadVaultDocuments()
        loadJobs()
        loadCvVersions()
        loadApiSettings()
        loadUserProfile()
        loadJobApplications()
        loadNotifications()

        // Start auto-saving whenever user makes changes to their resume sections/data
        viewModelScope.launch {
            // Wait for initial load or VM initialization to complete safely
            kotlinx.coroutines.delay(1500)
            
            val flows = listOf(
                titleState,
                personalInfoState,
                passportInfoState,
                workExperiencesState,
                educationsState,
                skillsState,
                languagesState,
                aboutMeState,
                declarationState,
                customizationState,
                certificationsState,
                projectsState,
                referencesState,
                awardsState,
                hobbiesState,
                socialLinksState,
                customSectionDataState
            )
            
            flows.forEach { flow ->
                launch {
                    flow.collect {
                        triggerAutosave()
                    }
                }
            }
        }
    }

    private var autosaveJob: kotlinx.coroutines.Job? = null
    var isAutosaveEnabled = true

    fun triggerAutosave() {
        if (!isAutosaveEnabled) return
        _saveStatus.value = "Saving..."
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            kotlinx.coroutines.delay(1200) // 1.2 second debounce
            saveResume()
            _saveStatus.value = "All changes saved"
        }
    }

    // Extended section state flows
    val certificationsState = MutableStateFlow(emptyList<CertItem>())
    val projectsState = MutableStateFlow(emptyList<ProjectItem>())
    val referencesState = MutableStateFlow(emptyList<ReferenceItem>())
    val awardsState = MutableStateFlow(emptyList<AwardItem>())
    val hobbiesState = MutableStateFlow(emptyList<HobbyItem>())
    val socialLinksState = MutableStateFlow(emptyList<SocialLinkItem>())
    val customSectionDataState = MutableStateFlow(emptyMap<String, String>()) // Map from sectionId to customized textual/raw info

    // Dynamic Section Management Functions
    fun moveSectionUp(id: String) {
        val currentCust = customizationState.value
        val list = currentCust.sections.sortedBy { it.order }.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index > 0) {
            val element = list.removeAt(index)
            list.add(index - 1, element)
            val updated = list.mapIndexed { idx, section -> section.copy(order = idx) }
            customizationState.value = currentCust.copy(sections = updated)
        }
    }

    fun moveSectionDown(id: String) {
        val currentCust = customizationState.value
        val list = currentCust.sections.sortedBy { it.order }.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1 && index < list.size - 1) {
            val element = list.removeAt(index)
            list.add(index + 1, element)
            val updated = list.mapIndexed { idx, section -> section.copy(order = idx) }
            customizationState.value = currentCust.copy(sections = updated)
        }
    }

    fun moveSection(fromIndex: Int, toIndex: Int) {
        val currentCust = customizationState.value
        val list = currentCust.sections.sortedBy { it.order }.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices && fromIndex != toIndex) {
            val element = list.removeAt(fromIndex)
            list.add(toIndex, element)
            val updated = list.mapIndexed { idx, section -> section.copy(order = idx) }
            customizationState.value = currentCust.copy(sections = updated)
        }
    }

    fun toggleSectionVisibility(id: String) {
        val currentCust = customizationState.value
        val updated = currentCust.sections.map {
            if (it.id == id) it.copy(isHidden = !it.isHidden) else it
        }
        customizationState.value = currentCust.copy(sections = updated)
    }

    fun toggleSectionCollapse(id: String) {
        val currentCust = customizationState.value
        val updated = currentCust.sections.map {
            if (it.id == id) it.copy(isCollapsed = !it.isCollapsed) else it
        }
        customizationState.value = currentCust.copy(sections = updated)
    }

    fun deleteSection(id: String) {
        val currentCust = customizationState.value
        val updated = currentCust.sections.filter { it.id != id }
        customizationState.value = currentCust.copy(sections = updated)
    }

    fun renameSection(id: String, newName: String) {
        val currentCust = customizationState.value
        val updated = currentCust.sections.map {
            if (it.id == id) it.copy(name = newName) else it
        }
        customizationState.value = currentCust.copy(sections = updated)
    }

    fun addCustomSection(name: String, iconName: String = "Star") {
        val currentCust = customizationState.value
        val nextId = "custom_${System.currentTimeMillis()}"
        val newSection = SectionState(
            id = nextId,
            name = name,
            isHidden = false,
            isCollapsed = false,
            order = currentCust.sections.size,
            iconName = iconName
        )
        val updated = currentCust.sections + newSection
        customizationState.value = currentCust.copy(sections = updated)
    }

    // AI operation status
    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    private val _aiResult = MutableStateFlow<String?>(null)
    val aiResult: StateFlow<String?> = _aiResult.asStateFlow()

    val INITIAL_SECTIONS = listOf(
        SectionState("personal", "Personal Information", false, false, 0, "Person"),
        SectionState("about", "About Me", false, false, 1, "Info"),
        SectionState("work", "Work Experience", false, false, 2, "Work"),
        SectionState("education", "Education", false, false, 3, "School"),
        SectionState("skills", "Skills", false, false, 4, "Star"),
        SectionState("languages", "Languages", false, false, 5, "Language"),
        SectionState("passport", "Passport Information", false, false, 6, "CardMembership"),
        SectionState("certifications", "Certifications", false, false, 7, "Verified"),
        SectionState("projects", "Projects", false, false, 8, "Build"),
        SectionState("references", "References", false, false, 9, "People"),
        SectionState("declaration", "Declaration", false, false, 10, "FactCheck"),
        SectionState("awards", "Awards", false, false, 11, "EmojiEvents"),
        SectionState("hobbies", "Hobbies", false, false, 12, "SportsEsports"),
        SectionState("social", "Social Links", false, false, 13, "Link")
    )

    // Create a blank/new resume draft
    fun createNewResumeDraft(defaultTitle: String = "My Resume") {
        isAutosaveEnabled = false
        _currentResumeId.value = null
        titleState.value = defaultTitle
        
        val profile = userProfileState.value
        if (profile != null && profile.fullName.isNotBlank()) {
            autoFillFromUserProfile(profile)
        } else {
            personalInfoState.value = PersonalInfo(
                fullName = "Saru Sharma",
                email = "sarusharma@gmail.com",
                phone = "+977-9851088888",
                currentCountry = "Nepal",
                city = "Kathmandu"
            )
            passportInfoState.value = PassportInfo()
            workExperiencesState.value = listOf(
                WorkExperience(
                    companyName = "Apex Global Enterprises",
                    jobPosition = "Senior Software Engineer",
                    country = "Nepal",
                    city = "Kathmandu",
                    startDate = "2023-01",
                    endDate = "Present",
                    isCurrentlyWorking = true,
                    responsibilities = "Lead professional production software engineering teams using Kotlin and advanced Material layout metrics.",
                    achievements = "Boosted transaction indexing speeds by 40% and standardized code structures."
                )
            )
            educationsState.value = listOf(
                Education(
                    schoolName = "Tribhuvan University",
                    degree = "Bachelor of Science in Computer Science & Information Technology",
                    country = "Nepal",
                    city = "Kathmandu",
                    gpa = "3.9/4.0",
                    startDate = "2019",
                    endDate = "2023"
                )
            )
            skillsState.value = listOf(
                Skill("Project Management", 90, "Technical"),
                Skill("Kotlin / Jetpack Compose", 95, "Technical"),
                Skill("Clean Architecture", 85, "Technical"),
                Skill("Communication", 90, "Soft")
            )
            languagesState.value = listOf(
                Language("Nepali", "Native", "Native", "Native", "Native"),
                Language("English", "Fluent", "Fluent", "Fluent", "Fluent")
            )
            aboutMeState.value = AboutMe(
                summary = "Passionate and seasoned engineer with strong credentials in modern application patterns.",
                careerObjective = "To secure a challenging role where I can apply enterprise architectural practices."
            )
        }
        declarationState.value = Declaration(
            text = "I hereby declare that all information provided above is true and correct to the best of my knowledge."
        )
        coverLetterState.value = CoverLetter(
            bodyText = "Dear Hiring Manager,\n\nI am writing to express my strong interest in the position..."
        )
        
        certificationsState.value = listOf(
            CertItem("AWS Certified Solutions Architect", "Amazon Web Services", "2024")
        )
        projectsState.value = listOf(
            ProjectItem("Enterprise HR System", "Re-architected outdated mainframe compiler systems to modern REST endpoints with 99.99% availability.", "Kotlin, Spring, Postgres", "https://github.com/example/hr")
        )
        referencesState.value = listOf(
            ReferenceItem("Dr. Sarah Jenkins", "Head of AI Research", "Global Tech Corp", "sarah.jenkins@example.com")
        )
        awardsState.value = listOf(
            AwardItem("Innovator of the Year", "Global Tech Corp", "2024", "Recognized for driving engineering metrics.")
        )
        hobbiesState.value = listOf(
            HobbyItem("Open Source Contribution"),
            HobbyItem("Algorithmic Trading")
        )
        socialLinksState.value = listOf(
            SocialLinkItem("LinkedIn", "linkedin.com/in/johndoe"),
            SocialLinkItem("GitHub", "github.com/johndoe")
        )
        customSectionDataState.value = emptyMap()

        customizationState.value = Customization(sections = INITIAL_SECTIONS)
        _aiResult.value = null
        _aiError.value = null

        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            isAutosaveEnabled = true
        }
    }

    fun createNewResumeDraftWithTemplate(
        templateId: String,
        templateName: String,
        primaryColorHex: String,
        headerColorHex: String
    ) {
        createNewResumeDraft(defaultTitle = "$templateName Draft")
        val currentCust = customizationState.value
        customizationState.value = currentCust.copy(
            templateId = templateId,
            primaryColorHex = primaryColorHex,
            headerColorHex = headerColorHex
        )
    }

    fun createPresetDraft(cvType: String, jobPosition: String, country: String, city: String, bloodGroup: String) {
        isAutosaveEnabled = false
        _currentResumeId.value = null
        titleState.value = when (cvType) {
            "europass_cv" -> "Europass Standard CV"
            "normal_cv" -> "Classic Normal CV"
            "ats_cv" -> "ATS Optimized CV"
            "cover_letter" -> "Custom Cover Letter"
            else -> "Quick Preset CV"
        }
        
        val profile = userProfileState.value
        val hasMasterProfile = profile != null && profile.fullName.isNotBlank()
        
        personalInfoState.value = PersonalInfo(
            fullName = if (hasMasterProfile) profile!!.fullName else "Saru Sharma",
            email = if (hasMasterProfile) profile!!.email else "sarusharma@gmail.com",
            phone = if (hasMasterProfile) profile!!.phone else (if (country == "Nepal") "+977-9851088888" else "+1-555-019-2834"),
            currentCountry = if (hasMasterProfile) profile!!.country else country,
            homeCountry = "Nepal",
            currentAddress = if (hasMasterProfile) "${profile!!.city}, ${profile!!.country}" else "$city, $country",
            city = if (hasMasterProfile) profile!!.city else city,
            bloodGroup = bloodGroup
        )
        passportInfoState.value = PassportInfo(
            passportNumber = "N1234567",
            passportCountry = "Nepal",
            expiryDate = "2032-12-31"
        )
        
        val genericResponsibilities = when (jobPosition.lowercase()) {
            "software engineer", "senior developer", "developer" -> 
                "Architect robust mobile and backend web systems. Design modern UI interfaces with high-performance responsive layout modules."
            "project manager", "manager" -> 
                "Coordinate cross-functional technical teams. Streamline delivery processes to match corporate milestone timelines."
            "accountant" -> 
                "Reconcile monthly financial balances, prepare corporate tax submissions, and optimize cash flow efficiency."
            "civil engineer" -> 
                "Oversee physical construction site operations. Review architectural blueprint safety standards and draft resource allocation reports."
            "medical officer" -> 
                "Perform medical checkups, execute diagnostic evaluations, and guide health treatment regimens."
            "graphic designer", "designer" -> 
                "Produce visually striking brand identities, vector assets, and promotional marketing templates."
            else -> "Coordinate daily operational activities, streamline team productivity, and deliver high-quality project outputs."
        }
        
        val genericAchievements = when (jobPosition.lowercase()) {
            "software engineer", "senior developer", "developer" -> 
                "Improved system response latency by 35% and increased automated unit testing coverage to 92%."
            "project manager", "manager" -> 
                "Success rate of 100% on delivery timelines for 5 consecutive major enterprise enterprise software rollouts."
            "accountant" -> 
                "Identified tax deduction opportunities saving $45,000 in annual operational overhead expenses."
            "civil engineer" -> 
                "Completed major bypass bridge project 3 weeks ahead of schedule, respecting strict structural safety standards."
            "medical officer" -> 
                "Streamlined emergency room diagnostic procedures, reducing average patient waiting duration by 25%."
            "graphic designer", "designer" -> 
                "Rebranded the core mobile app UI which amplified user engagement metrics by 50% across App Stores."
            else -> "Successfully delivered 4 major regional milestones exceeding management KPI metrics by 15%."
        }

        workExperiencesState.value = listOf(
            WorkExperience(
                companyName = "Apex Global Enterprises",
                jobPosition = jobPosition,
                country = country,
                city = city,
                startDate = "2022-04",
                endDate = "Present",
                isCurrentlyWorking = true,
                responsibilities = genericResponsibilities,
                achievements = genericAchievements
            )
        )
        
        educationsState.value = listOf(
            Education(
                schoolName = if (country == "Nepal") "Tribhuvan University" else "Global Professional Institute",
                degree = "Bachelor of Science in Professional Studies",
                country = country,
                city = city,
                gpa = "3.8/4.0",
                startDate = "2017",
                endDate = "2021"
            )
        )
        
        skillsState.value = listOf(
            Skill(jobPosition, 95, "Technical"),
            Skill("Project Coordination", 88, "Technical"),
            Skill("Analytical Reasoning", 90, "Technical"),
            Skill("Effective Communication", 92, "Soft")
        )
        
        languagesState.value = listOf(
            Language("English", "Fluent", "Fluent", "Fluent", "Fluent"),
            Language("Nepali", "Native", "Native", "Native", "Native")
        )
        
        aboutMeState.value = AboutMe(
            summary = "Ambitious and highly analytical professional possessing concrete credentials as the chosen role of $jobPosition. Dedicated to implementing streamlined architectural solutions and amplifying operational KPIs.",
            careerObjective = "To secure an elite appointment as a $jobPosition, leveraging strong dedication to details, dynamic organizational problem-solving, and professional standards."
        )
        
        declarationState.value = Declaration(
            text = "I solemnly declare that the facts stated in this resume are true, complete and correct to the best of my knowledge."
        )
        
        val signatureName = if (hasMasterProfile) profile!!.fullName else "Saru Sharma"
        val lowercaseUsername = signatureName.lowercase().replace(" ", "-")

        coverLetterState.value = CoverLetter(
            bodyText = "Dear Hiring Committee,\n\nI am presenting my candidature for the position of $jobPosition. With extensive practical exposure in designing robust solutions, I am confident in my capacity to add value to your progressive organization.\n\nDuring my tenure as a $jobPosition at Apex Global Enterprises in $city, $country, I successfully orchestrated standard operational activities. Specifically, I $genericAchievements This was guided by high standards of delivery.\n\nMy biological blood group is $bloodGroup, which I proudly specify as part of my holistic health records. I look forward to discussing how my experience fits your requirements.\n\nYours sincerely,\n$signatureName"
        )
        
        certificationsState.value = listOf(
            CertItem("Certified Strategic Leader", "Global Institute of Standards", "2023")
        )
        projectsState.value = listOf(
            ProjectItem("Operational Enhancement System", "Orchestrated end-to-end modernization of legacy operations, boosting team efficiency indices by 25%.", "Strategy, Integration, Cloud Tools", "")
        )
        referencesState.value = listOf(
            ReferenceItem("Dr. Ramesh Thapa", "Senior Director", "Apex Global Enterprises", "ramesh@example.com")
        )
        awardsState.value = listOf(
            AwardItem("Excellence in Leadership", "Apex Global Enterprises", "2023", "Awarded for outstanding operational contributions.")
        )
        hobbiesState.value = listOf(
            HobbyItem("Professional Networking"),
            HobbyItem("Community Volunteering")
        )
        socialLinksState.value = listOf(
            SocialLinkItem("LinkedIn", "linkedin.com/in/$lowercaseUsername"),
            SocialLinkItem("GitHub", "github.com/$lowercaseUsername")
        )
        customSectionDataState.value = emptyMap()

        val selectedTemplateId = when (cvType) {
            "europass_cv" -> "2"
            "normal_cv" -> "1"
            "ats_cv" -> "3"
            else -> "1"
        }
        val template = CvTemplate.PREDEFINED_TEMPLATES.find { it.id == selectedTemplateId } ?: CvTemplate.PREDEFINED_TEMPLATES[0]
        
        customizationState.value = Customization(
            templateId = template.id,
            fontName = "Inter",
            primaryColorHex = template.primaryColorHex,
            headerColorHex = template.headerColorHex,
            sections = INITIAL_SECTIONS
        )
        
        _aiResult.value = null
        _aiError.value = null

        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            isAutosaveEnabled = true
        }
    }

    // Load resume from Room database
    fun loadResume(resumeId: Int) {
        viewModelScope.launch {
            val resume = repository.getResumeByIdSingle(resumeId) ?: return@launch
            isAutosaveEnabled = false
            _currentResumeId.value = resume.id
            titleState.value = resume.title

            personalInfoState.value = JsonParser.fromJson<PersonalInfo>(resume.personalInfo) ?: PersonalInfo()
            passportInfoState.value = JsonParser.fromJson<PassportInfo>(resume.passportInfo) ?: PassportInfo()
            workExperiencesState.value = JsonParser.fromJsonList<WorkExperience>(resume.workExperiences)
            educationsState.value = JsonParser.fromJsonList<Education>(resume.educations)
            skillsState.value = JsonParser.fromJsonList<Skill>(resume.skills)
            languagesState.value = JsonParser.fromJsonList<Language>(resume.languages)
            aboutMeState.value = JsonParser.fromJson<AboutMe>(resume.aboutMe) ?: AboutMe()
            declarationState.value = JsonParser.fromJson<Declaration>(resume.declaration) ?: Declaration()
            coverLetterState.value = JsonParser.fromJson<CoverLetter>(resume.coverLetter) ?: CoverLetter()
            
            val customized = JsonParser.fromJson<Customization>(resume.customization) ?: Customization()
            // Make sure sections list has default values if empty
            customizationState.value = if (customized.sections.isEmpty()) customized.copy(sections = INITIAL_SECTIONS) else customized

            certificationsState.value = JsonParser.fromJsonList<CertItem>(resume.certifications)
            projectsState.value = JsonParser.fromJsonList<ProjectItem>(resume.projects)
            referencesState.value = JsonParser.fromJsonList<ReferenceItem>(resume.references)
            awardsState.value = JsonParser.fromJsonList<AwardItem>(resume.awards)
            hobbiesState.value = JsonParser.fromJsonList<HobbyItem>(resume.hobbies)
            socialLinksState.value = JsonParser.fromJsonList<SocialLinkItem>(resume.socialLinks)
            
            try {
                val mapType = com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
                val mapAdapter = JsonParser.moshi.adapter<Map<String, String>>(mapType)
                customSectionDataState.value = mapAdapter.fromJson(resume.customSectionData) ?: emptyMap()
            } catch (e: Exception) {
                customSectionDataState.value = emptyMap()
            }

            _aiResult.value = null
            _aiError.value = null
            
            kotlinx.coroutines.delay(800)
            isAutosaveEnabled = true
        }
    }

    fun autoFillFromUserProfile(profile: com.example.data.UserProfile) {
        personalInfoState.value = PersonalInfo(
            fullName = profile.fullName,
            email = profile.email,
            phone = profile.phone,
            currentCountry = profile.country,
            city = profile.city
        )
        aboutMeState.value = AboutMe(
            summary = profile.professionalSummary,
            careerObjective = "To secure a challenging role as ${profile.preferredJobTitle.ifBlank { "Specialist" }} and contribute towards organizational excellence."
        )
        if (profile.experience.isNotBlank()) {
            workExperiencesState.value = listOf(
                WorkExperience(
                    companyName = "Global Professional Corp",
                    jobPosition = profile.preferredJobTitle.ifBlank { "Specialist Coordinator" },
                    responsibilities = profile.experience,
                    startDate = "2022-01",
                    endDate = "Present",
                    isCurrentlyWorking = true,
                    country = profile.country.ifBlank { "Nepal" },
                    city = profile.city.ifBlank { "Kathmandu" }
                )
            )
        }
        if (profile.education.isNotBlank()) {
            educationsState.value = listOf(
                Education(
                    schoolName = "International Tech Academy",
                    degree = profile.education,
                    startDate = "2018-01",
                    endDate = "2021-12",
                    isCurrentlyStudying = false,
                    country = profile.country.ifBlank { "Nepal" },
                    city = profile.city.ifBlank { "Kathmandu" }
                )
            )
        }
        if (profile.skills.isNotBlank()) {
            skillsState.value = profile.skills.split(",").map { name ->
                Skill(name = name.trim(), percentage = 85, category = "Technical")
            }
        }
        if (profile.languages.isNotBlank()) {
            languagesState.value = profile.languages.split(",").map { name ->
                Language(name = name.trim(), readingLevel = "Fluent", writingLevel = "Fluent", speakingLevel = "Fluent", listeningLevel = "Fluent")
            }
        }
    }

    fun autoFillFromCompanyDetails(
        name: String,
        type: String,
        industry: String,
        country: String,
        city: String,
        addr: String,
        web: String,
        hr: String,
        phone: String,
        email: String,
        about: String
    ) {
        coverLetterState.value = CoverLetter(
            recipientName = hr,
            recipientCompany = name,
            bodyText = "Dear Sir/Madam,\n\nI am extremely excited to apply for the position with $name. Given my technical background and industry expertise in $industry, I am highly confident in my ability to bring value to your $type business operations. Thank you for your consideration.\n\nSincerely,\nCandidate",
            companyName = name,
            companyCountry = country,
            companyCity = city,
            companyAddress = addr,
            companyEmail = email,
            hiringManagerName = hr,
            jobPosition = "Associate Executive",
            applicantName = "Selected Candidate",
            applicantAddress = "Mid-Town Lalitpur, Nepal",
            applicantPhone = phone,
            applicantEmail = email,
            date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH).format(java.util.Date()),
            subjectLine = "APPLICATION FOR POSITION AT $name"
        )
    }

    // Save/Update current resume into Room DB
    fun saveResume(onComplete: (Resume) -> Unit = {}) {
        _saveStatus.value = "Saving..."
        viewModelScope.launch {
            val mapType = com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
            val mapAdapter = JsonParser.moshi.adapter<Map<String, String>>(mapType)
            val customJsonSec = try {
                mapAdapter.toJson(customSectionDataState.value)
            } catch (e: Exception) {
                "{}"
            }

            val resume = Resume(
                id = _currentResumeId.value ?: 0,
                title = titleState.value,
                updatedAt = System.currentTimeMillis(),
                personalInfo = JsonParser.toJson(personalInfoState.value),
                passportInfo = JsonParser.toJson(passportInfoState.value),
                workExperiences = JsonParser.toJsonList(workExperiencesState.value),
                educations = JsonParser.toJsonList(educationsState.value),
                skills = JsonParser.toJsonList(skillsState.value),
                languages = JsonParser.toJsonList(languagesState.value),
                aboutMe = JsonParser.toJson(aboutMeState.value),
                declaration = JsonParser.toJson(declarationState.value),
                customization = JsonParser.toJson(customizationState.value),
                coverLetter = JsonParser.toJson(coverLetterState.value),
                // Extended section serialization
                certifications = JsonParser.toJsonList(certificationsState.value),
                projects = JsonParser.toJsonList(projectsState.value),
                references = JsonParser.toJsonList(referencesState.value),
                awards = JsonParser.toJsonList(awardsState.value),
                hobbies = JsonParser.toJsonList(hobbiesState.value),
                socialLinks = JsonParser.toJsonList(socialLinksState.value),
                customSectionData = customJsonSec
            )

            val newId = repository.insertResume(resume)
            if (_currentResumeId.value == null || _currentResumeId.value == 0) {
                _currentResumeId.value = newId.toInt()
            }
            onComplete(resume.copy(id = _currentResumeId.value!!))
            _saveStatus.value = "All changes saved"
        }
    }

    // Delete a resume
    fun deleteResume(resumeId: Int) {
        viewModelScope.launch {
            repository.deleteResumeById(resumeId)
            if (_currentResumeId.value == resumeId) {
                _currentResumeId.value = null
            }
        }
    }

    // Duplicate a resume
    fun duplicateResume(resumeId: Int) {
        viewModelScope.launch {
            repository.duplicateResume(resumeId)
        }
    }

    // Clear error
    fun clearAiStates() {
        _aiError.value = null
        _aiResult.value = null
    }

    // --- AI COMPLIANT METHODS (GEMINI SDK DIRECT) ---

    fun improveAboutMeAI() {
        val currentAbout = aboutMeState.value.summary
        if (currentAbout.isEmpty()) {
            _aiError.value = "Please write a draft in the Summary field first before improving."
            return
        }

        viewModelScope.launch {
            _aiLoading.value = true
            _aiError.value = null
            _aiResult.value = null

            val prompt = """
                Please professionally rewrite and optimize this resume "About Me" professional summary:
                "$currentAbout"
                
                Make it look highly qualified, polished, and executive. Ensure perfect grammar and use ATS-friendly keyword action verbs.
                Keep it between 3 to 5 sentences.
            """.trimIndent()

            val instruction = "You are an expert executive resume editor and professional copywriter specializing in ATS formatting."

            GeminiClient.generateContentUnified(
                prompt = prompt,
                systemInstruction = instruction,
                apiKeyType = userApiKeyTypeState.value,
                customApiKey = userApiKeyValState.value,
                openaiModel = openaiModelState.value
            )
                .onSuccess { text ->
                    _aiResult.value = text
                    aboutMeState.value = aboutMeState.value.copy(summary = text)
                }
                .onFailure { exception ->
                    _aiError.value = exception.message ?: "Unknown API Error"
                }
            _aiLoading.value = false
        }
    }

    fun generateCoverLetterAI(
        companyName: String,
        companyCountry: String,
        companyCity: String,
        companyAddress: String,
        companyEmail: String,
        hiringManagerName: String,
        hiringManagerPosition: String,
        jobPosition: String,
        jobRefNumber: String,
        jobType: String,
        applicantName: String,
        applicantAddress: String,
        applicantPhone: String,
        applicantEmail: String,
        date: String,
        subjectLine: String,
        letterTone: String,
        keySkills: String
    ) {
        viewModelScope.launch {
            _aiLoading.value = true
            _aiError.value = null
            _aiResult.value = null

            val pInfo = personalInfoState.value
            val finalAppName = applicantName.ifBlank { pInfo.fullName.ifEmpty { "Applicant" } }
            val finalAppEmail = applicantEmail.ifBlank { pInfo.email }
            val finalAppPhone = applicantPhone.ifBlank { pInfo.phone }
            val finalAppAddr = applicantAddress.ifBlank { pInfo.currentAddress }

            val prompt = """
                Write a pristine, highly targeted cover letter for a job application.
                
                APPLICANT INFORMATION:
                - Name: $finalAppName
                - Email: $finalAppEmail
                - Phone: $finalAppPhone
                - Address: $finalAppAddr
                - Date: $date
                
                EMPLOYER INFORMATION:
                - Company Name: $companyName
                - Company Country: $companyCountry
                - Company City: $companyCity
                - Company Address: $companyAddress
                - Company contact Email: $companyEmail
                
                HIRING MANAGER:
                - Name: $hiringManagerName
                - Position: $hiringManagerPosition
                
                JOB DETAIL:
                - Position Applying For: $jobPosition
                - Reference Number: $jobRefNumber
                - Job Type (e.g. Full-Time, Part-Time, Contract): $jobType
                - Subject Line: $subjectLine
                
                CORE SKILLS / ACHIEVEMENTS TO EMPHASIZE:
                $keySkills
                
                COMMUNICATION STYLE / TONE:
                $letterTone (Option from: Professional, Friendly, Formal, Modern)
                
                STRICT COMPILATION REQUIREMENTS:
                1. Match the SELECTED TONE ("$letterTone") precisely throughout the flow.
                   - "Professional": Authoritative, balanced, objective, and industry-standard.
                   - "Friendly": Approachable, warm, enthusiastic, yet highly business-like.
                   - "Formal": Symmetrical structure, traditional, highly respectful, and classic.
                   - "Modern": Bold, direct, future-focused, emphasizing problem-solving.
                2. Do NOT use fake placeholders or draft markers like "[Insert Name]". Use the real facts provided.
                3. Structure the letterhead content dynamically:
                   - Write a direct professional greeting (e.g., "Dear Hiring Committee," or "Dear Mr./Ms. $hiringManagerName,").
                   - Organize into exactly 3-4 professional paragraphs highlighting suitability, skill advantages, and location-fit.
                   - Finish with strong closing sign-off (e.g., "Sincerely,", "Respectfully yours,").
                4. Maximize readability by wrapping major highlights or outstanding metrics in simple double asterisks (**bold** style formatting).
                
                Generate only the complete cover letter text.
            """.trimIndent()

            val instruction = "You are an elite corporate recruitment expert and professional resume coach with 15+ years of strategic writing experience."

            GeminiClient.generateContentUnified(
                prompt = prompt,
                systemInstruction = instruction,
                apiKeyType = userApiKeyTypeState.value,
                customApiKey = userApiKeyValState.value,
                openaiModel = openaiModelState.value
            )
                .onSuccess { text ->
                    _aiResult.value = text
                    coverLetterState.value = CoverLetter(
                        recipientName = hiringManagerName,
                        recipientCompany = companyName,
                        jobTitle = jobPosition,
                        letterDate = date,
                        bodyText = text,
                        companyName = companyName,
                        companyCountry = companyCountry,
                        companyCity = companyCity,
                        companyAddress = companyAddress,
                        companyEmail = companyEmail,
                        hiringManagerName = hiringManagerName,
                        hiringManagerPosition = hiringManagerPosition,
                        jobPosition = jobPosition,
                        jobRefNumber = jobRefNumber,
                        jobType = jobType,
                        applicantName = finalAppName,
                        applicantAddress = finalAppAddr,
                        applicantPhone = finalAppPhone,
                        applicantEmail = finalAppEmail,
                        date = date,
                        subjectLine = subjectLine,
                        letterTone = letterTone
                    )
                }
                .onFailure { exception ->
                    _aiError.value = exception.message ?: "Failed to generate Cover Letter."
                }
            _aiLoading.value = false
        }
    }

    fun suggestAtsKeywordsAI() {
        val jobTitleStr = workExperiencesState.value.firstOrNull()?.jobPosition ?: "Professional"
        val skillsStr = skillsState.value.joinToString { it.name }

        viewModelScope.launch {
            _aiLoading.value = true
            _aiError.value = null
            _aiResult.value = null

            val prompt = """
                Based on my current job title "$jobTitleStr" and the skills I have listed: "$skillsStr", 
                identify the top 10 core ATS industrial keywords and advanced secondary technical skills that I should weave into my resume sections to bypass automated ATS filters.
                Provide them as an easy-to-read checklist with brief 1-sentence justifications.
            """.trimIndent()

            GeminiClient.generateContentUnified(
                prompt = prompt,
                systemInstruction = null,
                apiKeyType = userApiKeyTypeState.value,
                customApiKey = userApiKeyValState.value,
                openaiModel = openaiModelState.value
            )
                .onSuccess { text ->
                    _aiResult.value = text
                }
                .onFailure { exception ->
                    _aiError.value = exception.message ?: "Unable to get ATS keywords suggestions"
                }
            _aiLoading.value = false
        }
    }

    // Completely offline smart scoring system
    fun calculateAtsScore(): AtsScoreAnalysis {
        val p = personalInfoState.value
        val ex = workExperiencesState.value
        val ed = educationsState.value
        val sk = skillsState.value
        val lg = languagesState.value
        val ab = aboutMeState.value

        var score = 30 // base structure score
        val suggestions = mutableListOf<String>()

        // 1. Personal Info check
        if (p.fullName.length > 3) score += 10 else suggestions.add("Add a complete full name.")
        if (p.email.contains("@")) score += 10 else suggestions.add("Provide a valid primary email address (crucial for recruiters).")
        if (p.phone.isNotBlank()) score += 5 else suggestions.add("Add a mobile or phone number.")
        if (p.whatsApp.isNotBlank() || p.website.isNotBlank()) score += 5

        // 2. Summary check
        if (ab.summary.length > 50) {
            score += 10
        } else {
            suggestions.add("Write a compelling professional summary (at least 50 chars). Better yet, use YourDoc AI Rewrite to optimize it.")
        }

        // 3. Experience check
        if (ex.isNotEmpty()) {
            score += 15
            val firstExp = ex.first()
            if (firstExp.responsibilities.length < 30) {
                suggestions.add("Add detailed achievements and bullet points for your role at ${firstExp.companyName}.")
            } else {
                score += 5
            }
        } else {
            suggestions.add("Work experiences are missing! Be sure to add at least 1 job entry to increase standard target hiring rates.")
        }

        // 4. Education check
        if (ed.isNotEmpty()) score += 10 else suggestions.add("Add school or academic certifications.")

        // 5. Skills count
        if (sk.size >= 4) {
            score += 10
        } else {
            suggestions.add("List at least 4 key technical/soft skills to optimize keyword search matching.")
        }

        // Clip to [0..100]
        score = score.coerceIn(10, 100)

        val ratingText = when {
            score >= 85 -> "Outstanding"
            score >= 70 -> "Good Quality (ATS Optimized)"
            score >= 50 -> "Needs Improvement"
            else -> "Incomplete Draft"
        }

        return AtsScoreAnalysis(score, ratingText, suggestions)
    }

    // ============================================
    // SYSTEM ADMIN DASHBOARD WORKSPACE FLOWS
    // ============================================
    val flaggedJobIds = MutableStateFlow<Set<String>>(emptySet())

    val appAccounts = MutableStateFlow<List<com.example.data.AppAccount>>(listOf(
        com.example.data.AppAccount(
            id = "acc_001",
            name = "Udayaraj Khanal (Primary Admin)",
            email = "udayarajkhanal21@gmail.com",
            role = "Admin",
            status = "Active",
            signupDate = "June 01, 2026",
            lastActive = "Just now",
            extraDetails = "System administrative security control profile."
        ),
        com.example.data.AppAccount(
            id = "acc_002",
            name = "Udayaraj Developer (Backup Admin)",
            email = "udayarajkhanal25@gmail.com",
            role = "Admin",
            status = "Active",
            signupDate = "June 02, 2026",
            lastActive = "5 mins ago",
            extraDetails = "System administrator backup log profile."
        ),
        com.example.data.AppAccount(
            id = "acc_003",
            name = "Jane Doe",
            email = "candidate.doe@gmail.com",
            role = "Candidate",
            status = "Active",
            signupDate = "June 10, 2026",
            lastActive = "2 hours ago",
            extraDetails = "Completed a resume profile with europass theme."
        ),
        com.example.data.AppAccount(
            id = "acc_004",
            name = "Apex Recruit Team",
            email = "co_recruiter@apex.com",
            role = "Company",
            status = "Active",
            signupDate = "June 12, 2026",
            lastActive = "1 day ago",
            extraDetails = "Posted 1 active job of Remote Crypto Mining."
        ),
        com.example.data.AppAccount(
            id = "acc_005",
            name = "Blockchain HR",
            email = "blockchain_hr@synergy.com",
            role = "Company",
            status = "Active",
            signupDate = "June 11, 2026",
            lastActive = "Yesterday",
            extraDetails = "Registered on Dubai trade jurisdiction portal."
        ),
        com.example.data.AppAccount(
            id = "acc_006",
            name = "Spammer Account",
            email = "spammer_user@scam.com",
            role = "Candidate",
            status = "Deactivated",
            signupDate = "May 20, 2026",
            lastActive = "3 weeks ago",
            extraDetails = "System-flagged for malicious profile generation actions."
        ),
        com.example.data.AppAccount(
            id = "acc_007",
            name = "YourDoc Recruiter",
            email = "employer.smith@yourdoc.com",
            role = "Company",
            status = "Active",
            signupDate = "Jan 12, 2026",
            lastActive = "2 hours ago",
            extraDetails = "Associated with Approved YourDoc primary platform."
        ),
        com.example.data.AppAccount(
            id = "acc_008",
            name = "Antigravity Creative Lab",
            email = "antigravity_admin@creative.com",
            role = "Company",
            status = "Active",
            signupDate = "Jan 15, 2026",
            lastActive = "1 week ago",
            extraDetails = "UX and Design consultancy partner."
        )
    ))

    fun updateAccountRole(id: String, newRole: String) {
        val list = appAccounts.value.map { acc ->
            if (acc.id == id) {
                acc.copy(role = newRole)
            } else {
                acc
            }
        }
        appAccounts.value = list
    }

    fun toggleAccountStatus(id: String) {
        val list = appAccounts.value.map { acc ->
            if (acc.id == id) {
                val nextStatus = if (acc.status == "Active") "Deactivated" else "Active"
                acc.copy(status = nextStatus)
            } else {
                acc
            }
        }
        appAccounts.value = list
    }

    fun addNewAccount(name: String, email: String, role: String) {
        val exists = appAccounts.value.any { it.email.lowercase() == email.lowercase() }
        if (!exists) {
            val nextId = "acc_${System.currentTimeMillis()}"
            val newAcc = com.example.data.AppAccount(
                id = nextId,
                name = name,
                email = email,
                role = role,
                status = "Active",
                signupDate = "Today",
                lastActive = "Just now",
                extraDetails = "Dynamically registered account profile."
            )
            appAccounts.value = appAccounts.value + newAcc
        }
    }
    val verifiedCompanyNames = MutableStateFlow<Set<String>>(setOf(
        "YourDoc Tech Solutions", 
        "Antigravity Creative Lab"
    ))

    val companyRegistrationDocs = MutableStateFlow<List<com.example.data.CompanyRegistrationDocument>>(listOf(
        com.example.data.CompanyRegistrationDocument(
            id = "doc_apex",
            companyName = "Apex Capital LLC",
            documentType = "Certificate of Offshore Incorporation",
            registrationNumber = "US-DE-99881-A",
            fileTitle = "APEX_DE_REG_2024.pdf",
            fileContentPreview = "Apex Capital LLC is organized as a limited liability company in the state of Delaware. Registered Agent: Delaware Business Filings, Inc. Capital: $50,000 USD.",
            submissionDate = "June 14, 2026",
            documentStatus = "Pending",
            location = "Delaware, United States",
            employeeCount = "5 - 10 employees",
            capitalRegistered = "$50,000 USD"
        ),
        com.example.data.CompanyRegistrationDocument(
            id = "doc_blockchain",
            companyName = "BlockChain Synergy",
            documentType = "DMCC Trade License",
            registrationNumber = "UAE-DMCC-8821B",
            fileTitle = "DMCC_LIC_BLOCKCHAIN_SYNERGY.pdf",
            fileContentPreview = "License Number DMCC-8821B. Trade Name: BlockChain Synergy FZCO. Legal Status: Free Zone Company. Activity: Software Development & Blockchain Consulting. Valid till: Dec 2026.",
            submissionDate = "June 13, 2026",
            documentStatus = "Pending",
            location = "Dubai, UAE",
            employeeCount = "15 - 50 employees",
            capitalRegistered = "100,000 AED"
        ),
        com.example.data.CompanyRegistrationDocument(
            id = "doc_yourdoc",
            companyName = "YourDoc Tech Solutions",
            documentType = "National Office Operations Registrar",
            registrationNumber = "NP-KTM-543-Y",
            fileTitle = "YOURDOC_OFFICE_REGISTRATION.pdf",
            fileContentPreview = "Authorized tech platform for overseas placement and career generation. Government approved registration ID 543-Y. Active status.",
            submissionDate = "May 10, 2026",
            documentStatus = "Approved",
            location = "Kathmandu, Nepal",
            employeeCount = "50 - 100 employees",
            capitalRegistered = "$200,000 USD"
        ),
        com.example.data.CompanyRegistrationDocument(
            id = "doc_antigravity",
            companyName = "Antigravity Creative Lab",
            documentType = "UK Companies House Certificate",
            registrationNumber = "UK-CH-1290382",
            fileTitle = "CO_HOUSE_ANTIGRAVITY_LAB.pdf",
            fileContentPreview = "Incorporated under the Companies Act 2006 as a private limited company. Registered office in England and Wales. Standard industrial classification: Brand and UX Research.",
            submissionDate = "Jan 15, 2026",
            documentStatus = "Approved",
            location = "London, United Kingdom",
            employeeCount = "10 - 20 employees",
            capitalRegistered = "£50,000 GBP"
        )
    ))

    val userReports = MutableStateFlow<List<UserReport>>(listOf(
        UserReport(
            id = "rep_001",
            reporterEmail = "skeptic_candidate@gmail.com",
            suspectTitle = "Remote Crypto Mining Consultant",
            suspectCompany = "Apex Capital LLC",
            reason = "Suspected job scam requiring upfront payment for laptop shipping.",
            reportDate = "June 14, 2026",
            targetType = "job",
            targetId = "sample_job_apex",
            status = "Open"
        ),
        UserReport(
            id = "rep_002",
            reporterEmail = "developer101@hotmail.com",
            suspectTitle = "Junior Rust Developer",
            suspectCompany = "BlockChain Synergy",
            reason = "Listing has outdated dead contacts and misleading heavy background check fees.",
            reportDate = "June 13, 2026",
            targetType = "job",
            targetId = "sample_job_blockchain",
            status = "Open"
        ),
        UserReport(
            id = "rep_003",
            reporterEmail = "recruiter_security@legal.com",
            suspectTitle = "Senior Software Engineer",
            suspectCompany = "YourDoc Tech Solutions",
            reason = "A user reported a duplication of copyright on the company mission about text.",
            reportDate = "June 12, 2026",
            targetType = "company",
            targetId = "YourDoc Tech Solutions",
            status = "Resolved"
        )
    ))

    val adminAuditLogs = MutableStateFlow<List<String>>(listOf(
        "🔑 System Admin Suite initialized by Root Security Administrator.",
        "📋 Approved default trusted certificate for YourDoc Tech Solutions.",
        "📋 Approved default trusted certificate for Antigravity Creative Lab.",
        "⚠️ User report rep_001 received against suspicion vacancy Apex Capital LLC.",
        "⚠️ User report rep_002 received against Blockchain Synergy."
    ))

    fun addAuditLog(action: String) {
        val current = adminAuditLogs.value.toMutableList()
        current.add(0, action)
        adminAuditLogs.value = current
    }

    fun isCompanyVerified(companyName: String): Boolean {
        return verifiedCompanyNames.value.contains(companyName)
    }

    fun verifyCompany(companyName: String) {
        val current = verifiedCompanyNames.value.toMutableSet()
        current.add(companyName)
        verifiedCompanyNames.value = current
        
        // sync corresponding doc
        val docsUpdated = companyRegistrationDocs.value.map {
            if (it.companyName == companyName) it.copy(documentStatus = "Approved") else it
        }
        companyRegistrationDocs.value = docsUpdated
        addAuditLog("🏆 Granted verified trust certificate state to: $companyName")
    }

    fun unverifyCompany(companyName: String) {
        val current = verifiedCompanyNames.value.toMutableSet()
        current.remove(companyName)
        verifiedCompanyNames.value = current
        
        // sync corresponding doc
        val docsUpdated = companyRegistrationDocs.value.map {
            if (it.companyName == companyName) it.copy(documentStatus = "Pending") else it
        }
        companyRegistrationDocs.value = docsUpdated
        addAuditLog("⚠️ Revoked trust certificate verification status for: $companyName")
    }

    fun approveCompanyDocument(docId: String) {
        val doc = companyRegistrationDocs.value.find { it.id == docId }
        val list = companyRegistrationDocs.value.map { currentDoc ->
            if (currentDoc.id == docId) {
                verifyCompany(currentDoc.companyName)
                currentDoc.copy(documentStatus = "Approved")
            } else {
                currentDoc
            }
        }
        companyRegistrationDocs.value = list
        if (doc != null) {
            addAuditLog("📄 Approved Business Licensing Registration file [${doc.fileTitle}] for ${doc.companyName}")
        }
    }

    fun rejectCompanyDocument(docId: String) {
        val doc = companyRegistrationDocs.value.find { it.id == docId }
        val list = companyRegistrationDocs.value.map { currentDoc ->
            if (currentDoc.id == docId) {
                unverifyCompany(currentDoc.companyName)
                currentDoc.copy(documentStatus = "Rejected")
            } else {
                currentDoc
            }
        }
        companyRegistrationDocs.value = list
        if (doc != null) {
            addAuditLog("❌ Rejected Trade Registration & Tax Files [${doc.fileTitle}] for ${doc.companyName}")
        }
    }

    fun toggleCompanyDocumentStatus(docId: String) {
        val doc = companyRegistrationDocs.value.find { it.id == docId } ?: return
        if (doc.documentStatus == "Approved") {
            unverifyCompany(doc.companyName)
            val updated = companyRegistrationDocs.value.map {
                if (it.id == docId) it.copy(documentStatus = "Pending") else it
            }
            companyRegistrationDocs.value = updated
            addAuditLog("🔄 Swapped Document Trust Status of ${doc.companyName} back to Pending Audit")
        } else {
            verifyCompany(doc.companyName)
            val updated = companyRegistrationDocs.value.map {
                if (it.id == docId) it.copy(documentStatus = "Approved") else it
            }
            companyRegistrationDocs.value = updated
            addAuditLog("🏆 Approved Document Trust Status of ${doc.companyName}")
        }
    }

    fun isJobFlagged(jobId: String): Boolean {
        return flaggedJobIds.value.contains(jobId)
    }

    fun flagJob(jobId: String) {
        val current = flaggedJobIds.value.toMutableSet()
        current.add(jobId)
        flaggedJobIds.value = current
        val name = jobsState.value.find { it.id == jobId }?.title ?: jobId
        addAuditLog("🚩 Flagged / Restricted Job Listing: \"$name\" in core moderation board")
    }

    fun unflagJob(jobId: String) {
        val current = flaggedJobIds.value.toMutableSet()
        current.remove(jobId)
        flaggedJobIds.value = current
        val name = jobsState.value.find { it.id == jobId }?.title ?: jobId
        addAuditLog("✅ Cleared Flag / Unlocked Posting: \"$name\" in core moderation board")
    }

    fun resolveReport(reportId: String, newStatus: String = "Resolved") {
        val current = userReports.value.map {
            if (it.id == reportId) it.copy(status = newStatus) else it
        }
        userReports.value = current
        addAuditLog("🎫 Moderated and Marked Report Incident $reportId as $newStatus")
    }

    fun deleteReport(reportId: String) {
        userReports.value = userReports.value.filter { it.id != reportId }
        addAuditLog("🗑️ Permanently Deleted/Purged Incident Report ticket reference: $reportId")
    }

    fun submitCompanyDocument(doc: com.example.data.CompanyRegistrationDocument) {
        val current = companyRegistrationDocs.value.toMutableList()
        val index = current.indexOfFirst { it.companyName.equals(doc.companyName, ignoreCase = true) && it.documentType.equals(doc.documentType, ignoreCase = true) }
        if (index != -1) {
            current[index] = doc
        } else {
            current.add(0, doc)
        }
        companyRegistrationDocs.value = current
        addAuditLog("🔄 Submitted Registration License [${doc.fileTitle}] for ${doc.companyName}. Placed in validation queue.")
    }

    fun updateCompanySettings(
        companyName: String,
        type: String,
        industry: String,
        country: String,
        city: String,
        address: String,
        website: String,
        hrLead: String,
        phone: String,
        email: String,
        about: String
    ) {
        val updatedJobs = jobsState.value.map { job ->
            if (job.company.equals(companyName, ignoreCase = true)) {
                job.copy(
                    companyType = type,
                    companyIndustry = industry,
                    companyCountry = country,
                    companyCity = city,
                    companyAddress = address,
                    companyWebsite = website,
                    companyHrContact = hrLead,
                    companyPhone = phone,
                    companyEmail = email,
                    companyAbout = about
                )
            } else {
                job
            }
        }
        saveJobs(updatedJobs)
        addAuditLog("🏢 Recruiter updated profile card for: $companyName ($industry). Updates updated across live postings.")
    }
    
    fun fileReport(reporter: String, title: String, company: String, reason: String, targetType: String, targetId: String) {
        val list = userReports.value.toMutableList()
        val formattedDate = "June 14, 2026"
        list.add(0, UserReport(
            reporterEmail = reporter,
            suspectTitle = title,
            suspectCompany = company,
            reason = reason,
            reportDate = formattedDate,
            targetType = targetType,
            targetId = targetId,
            status = "Open"
        ))
        userReports.value = list
    }
}

data class AtsScoreAnalysis(
    val score: Int,
    val rating: String,
    val suggestions: List<String>
)
