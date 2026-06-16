package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@JsonClass(generateAdapter = true)
data class PersonalInfo(
    val fullName: String = "",
    val profilePhotoUri: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val nationality: String = "",
    val maritalStatus: String = "",
    val email: String = "",
    val phone: String = "",
    val whatsApp: String = "",
    val website: String = "",
    val currentCountry: String = "",
    val homeCountry: String = "",
    val currentAddress: String = "",
    val city: String = "",
    val state: String = "",
    val postalCode: String = "",
    val bloodGroup: String = ""
)

@JsonClass(generateAdapter = true)
data class PassportInfo(
    val passportNumber: String = "",
    val passportCountry: String = "",
    val placeOfIssue: String = "",
    val issueDate: String = "",
    val expiryDate: String = ""
)

@JsonClass(generateAdapter = true)
data class WorkExperience(
    val companyName: String = "",
    val jobPosition: String = "",
    val country: String = "",
    val city: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isCurrentlyWorking: Boolean = false,
    val responsibilities: String = "",
    val achievements: String = "",
    val skillsUsed: String = "",
    val companyDescription: String = ""
)

@JsonClass(generateAdapter = true)
data class Education(
    val schoolName: String = "",
    val degree: String = "",
    val country: String = "",
    val city: String = "",
    val gpa: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val description: String = "",
    // Extended fields to satisfy item 40:
    val fieldOfStudy: String = "",
    val educationLevel: String = "Bachelor Degree",
    val isCurrentlyStudying: Boolean = false,
    val mainSubjects: String = "",
    val achievements: String = "",
    val certificates: String = "",
    val trainingCourses: String = ""
)

@JsonClass(generateAdapter = true)
data class Skill(
    val name: String = "",
    val percentage: Int = 80,
    val category: String = "Technical" // "Technical" or "Soft"
)

@JsonClass(generateAdapter = true)
data class Language(
    val name: String = "",
    val readingLevel: String = "Advanced", // Beginner, Intermediate, Advanced, Fluent, Native
    val writingLevel: String = "Advanced",
    val speakingLevel: String = "Advanced",
    val listeningLevel: String = "Advanced"
)

@JsonClass(generateAdapter = true)
data class AboutMe(
    val summary: String = "",
    val careerObjective: String = ""
)

@JsonClass(generateAdapter = true)
data class Declaration(
    val text: String = "",
    val fullName: String = "",
    val date: String = "",
    val signaturePathJson: String = "" // Multi-point transparent drawing json string
)

@JsonClass(generateAdapter = true)
data class CoverLetter(
    val recipientName: String = "",
    val recipientCompany: String = "",
    val jobTitle: String = "",
    val letterDate: String = "",
    val bodyText: String = "",
    val companyName: String = "",
    val companyCountry: String = "",
    val companyCity: String = "",
    val companyAddress: String = "",
    val companyEmail: String = "",
    val hiringManagerName: String = "",
    val hiringManagerPosition: String = "",
    val jobPosition: String = "",
    val jobRefNumber: String = "",
    val jobType: String = "",
    val applicantName: String = "",
    val applicantAddress: String = "",
    val applicantPhone: String = "",
    val applicantEmail: String = "",
    val date: String = "",
    val subjectLine: String = "",
    val letterTone: String = ""
)

@JsonClass(generateAdapter = true)
data class CertItem(val title: String = "", val issuer: String = "", val date: String = "")

@JsonClass(generateAdapter = true)
data class ProjectItem(val name: String = "", val description: String = "", val technologies: String = "", val link: String = "")

@JsonClass(generateAdapter = true)
data class ReferenceItem(val name: String = "", val position: String = "", val company: String = "", val contact: String = "")

@JsonClass(generateAdapter = true)
data class AwardItem(val title: String = "", val issuer: String = "", val year: String = "", val description: String = "")

@JsonClass(generateAdapter = true)
data class HobbyItem(val name: String = "")

@JsonClass(generateAdapter = true)
data class SocialLinkItem(val platform: String = "", val url: String = "")

@JsonClass(generateAdapter = true)
data class CustomSectionItem(val title: String = "", val description: String = "")

@JsonClass(generateAdapter = true)
data class CvTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: String, // "Professional", "ATS Friendly", "Corporate", "Creative", "Modern"
    val primaryColorHex: String,
    val headerColorHex: String,
    val isDarkBg: Boolean = false,
    val lightAccentHex: String = "#F1F5F9"
) {
    companion object {
        val PREDEFINED_TEMPLATES = listOf(
            CvTemplate(
                id = "europass_blue",
                name = "Europass Standard Blue",
                description = "Authoritative, standard European Union structured professional CV layout with deep navy accents.",
                category = "Professional",
                primaryColorHex = "#002F6C",
                headerColorHex = "#041E42",
                isDarkBg = false,
                lightAccentHex = "#D3E4FF"
            ),
            CvTemplate(
                id = "europass_modern",
                name = "Europass Emerald Green",
                description = "Sleek, modern emerald variation of the standard European format representing growth and focus.",
                category = "Professional",
                primaryColorHex = "#0D9488",
                headerColorHex = "#115E59",
                isDarkBg = false,
                lightAccentHex = "#CCFBF1"
            ),
            CvTemplate(
                id = "europass_dark",
                name = "Europass Dark Midnight",
                description = "High-contrast twilight accented header variant of the structured European Layout.",
                category = "Professional",
                primaryColorHex = "#1E293B",
                headerColorHex = "#0F172A",
                isDarkBg = true,
                lightAccentHex = "#334155"
            ),
            CvTemplate(
                id = "europass_ruby",
                name = "Europass Ruby Royal",
                description = "Deep burgundy/wine tone scheme highlighting luxury design, prestige executive positions, and leadership profiles.",
                category = "Professional",
                primaryColorHex = "#991B1B",
                headerColorHex = "#450A0A",
                isDarkBg = true,
                lightAccentHex = "#FEE2E2"
            ),
            CvTemplate(
                id = "europass_cyber",
                name = "Europass Cyber Violet",
                description = "An energetic high-tech violet layout tailored for software architects, AI researchers, and developers.",
                category = "Professional",
                primaryColorHex = "#6D28D9",
                headerColorHex = "#2E1065",
                isDarkBg = true,
                lightAccentHex = "#F3E8FF"
            ),
            CvTemplate(
                id = "europass_amber",
                name = "Europass Golden Amber",
                description = "Warm yellow-gold with dark contrast paneling suited perfectly for hospitality leads, culinary operators, and consultants.",
                category = "Professional",
                primaryColorHex = "#D97706",
                headerColorHex = "#451A03",
                isDarkBg = true,
                lightAccentHex = "#FEF3C7"
            ),
            CvTemplate(
                id = "europass_platinum",
                name = "Europass Platinum Executive",
                description = "Ultra-premium platinum layout with steel-blue timelines designed for forensic auditors and corporate controllers.",
                category = "Professional",
                primaryColorHex = "#475569",
                headerColorHex = "#1E293B",
                isDarkBg = false,
                lightAccentHex = "#F1F5F9"
            ),
            CvTemplate(
                id = "europass_forest",
                name = "Europass Forest Green",
                description = "Deep natural green forest template designed specifically for civil, geological, and structural site engineering.",
                category = "Professional",
                primaryColorHex = "#065F46",
                headerColorHex = "#022C22",
                isDarkBg = true,
                lightAccentHex = "#D1FAE5"
            ),
            CvTemplate(
                id = "europass_royal",
                name = "Europass Royal Indigo",
                description = "Majestic indigo template styled with gold-star emblems representing national-level management and telecommunication leads.",
                category = "Professional",
                primaryColorHex = "#3730A3",
                headerColorHex = "#1E1B4B",
                isDarkBg = true,
                lightAccentHex = "#E0E7FF"
            ),
            CvTemplate(
                id = "europass_coral",
                name = "Europass Sunset Coral",
                description = "Modern and vibrant coral-red accents paired with rich dark layout segments for innovative communicators.",
                category = "Professional",
                primaryColorHex = "#DC2626",
                headerColorHex = "#7F1D1D",
                isDarkBg = true,
                lightAccentHex = "#FFEBEE"
            ),
            CvTemplate(
                id = "europass_charcoal",
                name = "Europass Charcoal Minimal",
                description = "Slate-charcoal layout with minimal visual lines emphasizing absolute text readability and elegant spacing.",
                category = "Professional",
                primaryColorHex = "#334155",
                headerColorHex = "#0F172A",
                isDarkBg = false,
                lightAccentHex = "#F8FAFC"
            ),
            CvTemplate(
                id = "europass_chocolate",
                name = "Europass Bronze Deluxe",
                description = "Espresso and bronze executive theme mapping culinary resort leads and luxury business administrators.",
                category = "Professional",
                primaryColorHex = "#78350F",
                headerColorHex = "#451B03",
                isDarkBg = true,
                lightAccentHex = "#FEF3C7"
            ),
            CvTemplate(
                id = "europass_sky",
                name = "Europass Sky Blue",
                description = "Energetic and crisp sky blue header layout, fresh and ideal for creative designers, educators, and mentors.",
                category = "Professional",
                primaryColorHex = "#0284C7",
                headerColorHex = "#0C4A6E",
                isDarkBg = false,
                lightAccentHex = "#E0F2FE"
            ),
            CvTemplate(
                id = "ats_white",
                name = "ATS Compliant Pure White",
                description = "Ultra-clean, single column layout designed to easily pass Automated Applicant Tracking system rules.",
                category = "ATS Friendly",
                primaryColorHex = "#1E293B",
                headerColorHex = "#0F172A",
                isDarkBg = false,
                lightAccentHex = "#F8FAFC"
            ),
            CvTemplate(
                id = "corporate_blue",
                name = "Corporate Split Blue",
                description = "Sided timeline partition structure featuring navy accents and sidebar alignment.",
                category = "Corporate",
                primaryColorHex = "#1E3A8A",
                headerColorHex = "#172554",
                isDarkBg = false,
                lightAccentHex = "#DBEAFE"
            ),
            CvTemplate(
                id = "elegant_black",
                name = "Elegant Noir Monochrome",
                description = "Symmetrical charcoal highlight structure focused on luxury, high fidelity minimalist presentation.",
                category = "Creative",
                primaryColorHex = "#111827",
                headerColorHex = "#030712",
                isDarkBg = true,
                lightAccentHex = "#E5E7EB"
            ),
            CvTemplate(
                id = "modern_minimalist",
                name = "Modern Minimalist Slate",
                description = "Balanced visual architecture featuring soft slate lines for clear readable readability.",
                category = "Modern",
                primaryColorHex = "#475569",
                headerColorHex = "#334155",
                isDarkBg = false,
                lightAccentHex = "#F1F5F9"
            ),
            CvTemplate(
                id = "luxury_dark",
                name = "Luxury Gold & Cocoa",
                description = "Prestige executive layout featuring bold cocoa backgrounds and gold typography accents.",
                category = "Corporate",
                primaryColorHex = "#854D0E",
                headerColorHex = "#451A03",
                isDarkBg = true,
                lightAccentHex = "#FEF9C3"
            ),
            CvTemplate(
                id = "ats_charcoal",
                name = "ATS Slate Charcoal",
                description = "A sleek, high-performing ATS layout emphasizing space with corporate charcoal highlights.",
                category = "ATS Friendly",
                primaryColorHex = "#334155",
                headerColorHex = "#1E293B",
                isDarkBg = false,
                lightAccentHex = "#F8FAFC"
            ),
            CvTemplate(
                id = "creative_royal",
                name = "Creative Royal Indigo",
                description = "Bold, expressive dual-column portfolio with deep royal indigo and vivid lavender gradients.",
                category = "Creative",
                primaryColorHex = "#4F46E5",
                headerColorHex = "#312E81",
                isDarkBg = true,
                lightAccentHex = "#EEF2F6"
            )
        )
    }
}

@JsonClass(generateAdapter = true)
data class SectionState(
    val id: String = "",
    val name: String = "",
    val isHidden: Boolean = false,
    val isCollapsed: Boolean = false,
    val order: Int = 0,
    val iconName: String = "Star"
)

@JsonClass(generateAdapter = true)
data class Customization(
    val templateId: String = "europass_blue", // europass_blue, europass_modern, europass_dark, ats_white, corporate_blue, elegant_black, modern_minimalist, luxury_dark
    val primaryColorHex: String = "#002F6C",
    val headerColorHex: String = "#041E42",
    val fontName: String = "Roboto", // Roboto, Poppins, Open Sans, Montserrat, Lato, Inter
    val fontSizeBase: Float = 11f,
    val headingSizeBase: Float = 14f,
    val isBorderEnabled: Boolean = false,
    val isRoundedBorder: Boolean = true,
    val borderThicknessDp: Float = 1f,
    val borderColorHex: String = "#D1D5DB",
    val lineSpacingMultiplier: Float = 1.2f,
    val isSidebarOnLeft: Boolean = true,
    val showProfilePhoto: Boolean = true,
    val marginSizeDp: Float = 14f,
    val sections: List<SectionState> = emptyList(),
    val photoShape: String = "circle", // circle, square, oval, simple
    val photoPosition: String = "best_slot" // best_slot, left, right, sidebar
)

@Entity(tableName = "resumes")
data class Resume(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "My Premium Resume",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val personalInfo: String = "",
    val passportInfo: String = "",
    val workExperiences: String = "",
    val educations: String = "",
    val skills: String = "",
    val languages: String = "",
    val aboutMe: String = "",
    val declaration: String = "",
    val customization: String = "",
    val coverLetter: String = "",
    // Extended section systems
    val certifications: String = "",
    val projects: String = "",
    val references: String = "",
    val awards: String = "",
    val hobbies: String = "",
    val socialLinks: String = "",
    val customSectionData: String = ""
)

// Helper to convert objects to JSON & vice versa locally using Moshi
object JsonParser {
    val moshi: Moshi = Moshi.Builder().build()

    inline fun <reified T> toJson(value: T): String {
        return try {
            val adapter = moshi.adapter(T::class.java)
            adapter.toJson(value)
        } catch (e: Exception) {
            ""
        }
    }

    inline fun <reified T> fromJson(json: String): T? {
        if (json.isEmpty()) return null
        return try {
            val adapter = moshi.adapter(T::class.java)
            adapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    inline fun <reified T> toJsonList(value: List<T>): String {
        return try {
            val type = Types.newParameterizedType(List::class.java, T::class.java)
            val adapter = moshi.adapter<List<T>>(type)
            adapter.toJson(value)
        } catch (e: Exception) {
            "[]"
        }
    }

    inline fun <reified T> fromJsonList(json: String): List<T> {
        if (json.isEmpty()) return emptyList()
        return try {
            val type = Types.newParameterizedType(List::class.java, T::class.java)
            val adapter = moshi.adapter<List<T>>(type)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

@JsonClass(generateAdapter = true)
data class VaultDocument(
    val id: String = "${java.lang.System.currentTimeMillis()}",
    val type: String = "cv", // "cv", "passport", "experience", "certificate", "pcc", "custom"
    val title: String = "",
    val isEnabled: Boolean = true,
    // Fields for CV:
    val cvResumeId: Int? = null,
    val cvTitle: String = "",
    // Fields for Passport:
    val passportNo: String = "",
    val passportFullName: String = "",
    val passportCountry: String = "",
    val passportDob: String = "",
    val passportIssueDate: String = "",
    val passportExpiryDate: String = "",
    // Fields for Experience Letter:
    val expCompany: String = "",
    val expRole: String = "",
    val expStartDate: String = "",
    val expEndDate: String = "",
    val expDetails: String = "",
    // Fields for Certificate File:
    val certTitle: String = "",
    val certIssuer: String = "",
    val certDate: String = "",
    val certCode: String = "",
    // Fields for PCC:
    val pccFullName: String = "",
    val pccAuthority: String = "",
    val pccIssueDate: String = "",
    val pccStatus: String = "Cleared / No Criminal Record",
    // Fields for Custom Document:
    val customBody: String = "",
    // Fields for PDF uploaded document:
    val pdfFilePath: String = ""
)

@JsonClass(generateAdapter = true)
data class CvVersionSnapshot(
    val id: String = "${java.lang.System.currentTimeMillis()}",
    val resumeId: Int = 0,
    val versionName: String = "",
    val timestamp: Long = java.lang.System.currentTimeMillis(),
    val resumeDataJson: String = ""
)

@JsonClass(generateAdapter = true)
data class JobListing(
    val id: String = "${java.lang.System.currentTimeMillis()}-${(1000..9999).random()}",
    // Job Posting structure fields
    val title: String = "",
    val positionName: String = "",
    val country: String = "",
    val city: String = "",
    val fullAddress: String = "",
    val salary: String = "",
    val currency: String = "USD",
    val dutyHours: String = "8 hours",
    val shiftType: String = "Day", // e.g. Day, Night, Flexible
    val experienceRequired: String = "No experience",
    val educationRequired: String = "Bachelor Degree",
    val skillsRequired: String = "",
    val languagesRequired: String = "English",
    val visaSupport: Boolean = false,
    val accommodation: Boolean = false,
    val foodSupport: Boolean = false,
    val transportation: Boolean = false,
    val contractDuration: String = "1 Year",
    val genderRequirement: String = "Any", // Any, Male, Female
    val ageRequirement: String = "18-50",
    val deadline: String = "",
    val responsibilities: String = "",
    val benefits: String = "",
    val description: String = "",
    val postedDate: String = "",
    val contactEmail: String = "",

    // Company Profile fields
    val company: String = "",
    val companyLogo: String = "🏢", // Represented by Emoji or Symbol representation
    val companyType: String = "Private Limited", // e.g. Multinational, Startup, Government
    val companyIndustry: String = "Technology",
    val companyCountry: String = "",
    val companyCity: String = "",
    val companyAddress: String = "",
    val companyWebsite: String = "",
    val companyHrContact: String = "HR Manager",
    val companyPhone: String = "",
    val companyEmail: String = "",
    val companyAbout: String = ""
)

@JsonClass(generateAdapter = true)
data class JobApplication(
    val id: String = "${java.lang.System.currentTimeMillis()}-${(1000..9999).random()}",
    val jobId: String = "",
    val jobTitle: String = "",
    val companyName: String = "",
    val appliedDate: String = "",
    val applicantName: String = "",
    val applicantEmail: String = "",
    val applicantPhone: String = "",
    val userProfileJson: String = "",
    val status: String = "Pending Review", // Pending Review, Shortlisted, Interview Invited, Rejected
    val coverLetterText: String = "",
    val resumeIdUsed: Int = 0,
    val note: String = ""
)

@JsonClass(generateAdapter = true)
data class SmartNotification(
    val id: String = "${java.lang.System.currentTimeMillis()}-${(1000..9999).random()}",
    val title: String = "",
    val body: String = "",
    val timestamp: Long = java.lang.System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "matching" // matching, status, invitation
)

@JsonClass(generateAdapter = true)
data class UserReport(
    val id: String = "${java.lang.System.currentTimeMillis()}-${(1000..9999).random()}",
    val reporterEmail: String = "",
    val suspectTitle: String = "",
    val suspectCompany: String = "",
    val reason: String = "",
    val reportDate: String = "",
    val targetType: String = "job", // "job" or "company"
    val targetId: String = "",
    val status: String = "Open" // "Open", "Investigating", "Resolved"
)

@JsonClass(generateAdapter = true)
data class CompanyRegistrationDocument(
    val id: String = "",
    val companyName: String = "",
    val documentType: String = "", // e.g. "Trade License", "Certificate of Incorporation"
    val registrationNumber: String = "",
    val fileTitle: String = "",
    val fileContentPreview: String = "",
    val submissionDate: String = "",
    val documentStatus: String = "Pending", // "Pending", "Approved", "Rejected"
    val location: String = "",
    val employeeCount: String = "",
    val capitalRegistered: String = ""
)

@JsonClass(generateAdapter = true)
data class AppAccount(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "Candidate", // "Candidate", "Company", "Admin"
    val status: String = "Active", // "Active", "Deactivated"
    val signupDate: String = "",
    val lastActive: String = "",
    val extraDetails: String = ""
)

