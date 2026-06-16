package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import coil.compose.AsyncImage

// Parse Color from hex safely
fun safeParseColor(hex: String, fallback: Color = Color(0xFF002F6C)): Color {
    return try {
        if (hex.startsWith("#")) {
            Color(android.graphics.Color.parseColor(hex))
        } else {
            Color(android.graphics.Color.parseColor("#$hex"))
        }
    } catch (e: Exception) {
        fallback
    }
}

// Get standard native font family
fun getFontFamily(fontName: String): FontFamily {
    return when (fontName) {
        "Poppins" -> FontFamily.SansSerif
        "Montserrat" -> FontFamily.SansSerif
        "Open Sans" -> FontFamily.SansSerif
        "Lato" -> FontFamily.SansSerif
        "Inter" -> FontFamily.Default
        "Roboto" -> FontFamily.Default
        else -> FontFamily.Default
    }
}

@Composable
fun ResumeDocumentView(
    personalInfo: PersonalInfo,
    passportInfo: PassportInfo,
    workExperiences: List<WorkExperience>,
    educations: List<Education>,
    skills: List<Skill>,
    languages: List<Language>,
    aboutMe: AboutMe,
    declaration: Declaration,
    customization: Customization,
    modifier: Modifier = Modifier,
    certifications: List<CertItem> = emptyList(),
    projects: List<ProjectItem> = emptyList(),
    references: List<ReferenceItem> = emptyList(),
    awards: List<AwardItem> = emptyList(),
    hobbies: List<HobbyItem> = emptyList(),
    socialLinks: List<SocialLinkItem> = emptyList(),
    customSectionsData: Map<String, String> = emptyMap()
) {
    val primColor = safeParseColor(customization.primaryColorHex)
    val headColor = safeParseColor(customization.headerColorHex)
    val fontFam = getFontFamily(customization.fontName)
    val baseFontSize = customization.fontSizeBase.sp
    val baseHeadingSize = customization.headingSizeBase.sp
    val marginSize = customization.marginSizeDp.dp

    // Apply template structure
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = if (customization.isRoundedBorder) RoundedCornerShape(12.dp) else RoundedCornerShape(0.dp),
        border = if (customization.isBorderEnabled) {
            when (customization.borderThicknessDp) {
                2f -> androidx.compose.foundation.BorderStroke(3.dp, primColor) // Double thicker primary outline
                3f -> androidx.compose.foundation.BorderStroke(2.dp, primColor.copy(alpha = 0.6f)) // Distinct accent boundary
                4f -> androidx.compose.foundation.BorderStroke(4.dp, Color(0xFF1E293B)) // Executive charcoal solid frame
                else -> androidx.compose.foundation.BorderStroke(customization.borderThicknessDp.dp, safeParseColor(customization.borderColorHex))
            }
        } else null
    ) {
        // Double nest border overlay offset layout inside (nested inner frame)
        val overlayModifier = if (customization.isBorderEnabled && customization.borderThicknessDp == 2f) {
            Modifier
                .padding(3.dp)
                .border(
                    1.dp, 
                    safeParseColor(customization.borderColorHex), 
                    if (customization.isRoundedBorder) RoundedCornerShape(10.dp) else RoundedCornerShape(0.dp)
                )
        } else Modifier

        Box(modifier = overlayModifier) {
            when (customization.templateId) {
            "ats_white" -> AtsWhiteLayout(
                personalInfo = personalInfo,
                passportInfo = passportInfo,
                workExperiences = workExperiences,
                educations = educations,
                skills = skills,
                languages = languages,
                aboutMe = aboutMe,
                declaration = declaration,
                fontFam = fontFam,
                baseFontSize = baseFontSize,
                baseHeaderSize = baseHeadingSize,
                marginSize = marginSize,
                customization = customization,
                primColor = primColor,
                certifications = certifications,
                projects = projects,
                references = references,
                awards = awards,
                hobbies = hobbies,
                socialLinks = socialLinks,
                customSectionsData = customSectionsData
            )
            "europass_blue", "europass_modern", "europass_dark", "europass_ruby", "europass_cyber", "europass_amber", "europass_platinum", "europass_forest", "europass_royal", "europass_coral", "europass_charcoal", "europass_chocolate", "europass_sky" -> EuropassLayout(
                personalInfo = personalInfo,
                passportInfo = passportInfo,
                workExperiences = workExperiences,
                educations = educations,
                skills = skills,
                languages = languages,
                aboutMe = aboutMe,
                declaration = declaration,
                customization = customization,
                primaryColor = primColor,
                headerColor = headColor,
                fontFam = fontFam,
                baseFontSize = baseFontSize,
                baseHeaderSize = baseHeadingSize,
                marginSize = marginSize
            )
            "elegant_black" -> ElegantBlackLayout(
                personalInfo = personalInfo,
                passportInfo = passportInfo,
                workExperiences = workExperiences,
                educations = educations,
                skills = skills,
                languages = languages,
                aboutMe = aboutMe,
                declaration = declaration,
                customization = customization,
                primaryColor = primColor,
                fontFam = fontFam,
                baseFontSize = baseFontSize,
                baseHeaderSize = baseHeadingSize,
                marginSize = marginSize
            )
            "modern_minimalist" -> ModernMinimalistLayout(
                personalInfo = personalInfo,
                passportInfo = passportInfo,
                workExperiences = workExperiences,
                educations = educations,
                skills = skills,
                languages = languages,
                aboutMe = aboutMe,
                declaration = declaration,
                customization = customization,
                primaryColor = primColor,
                fontFam = fontFam,
                baseFontSize = baseFontSize,
                baseHeaderSize = baseHeadingSize,
                marginSize = marginSize
            )
            "luxury_dark" -> LuxuryDarkLayout(
                personalInfo = personalInfo,
                passportInfo = passportInfo,
                workExperiences = workExperiences,
                educations = educations,
                skills = skills,
                languages = languages,
                aboutMe = aboutMe,
                declaration = declaration,
                customization = customization,
                primaryColor = primColor,
                fontFam = fontFam,
                baseFontSize = baseFontSize,
                baseHeaderSize = baseHeadingSize,
                marginSize = marginSize
            )
            "creative_royal" -> CreativeRoyalLayout(
                personalInfo = personalInfo,
                passportInfo = passportInfo,
                workExperiences = workExperiences,
                educations = educations,
                skills = skills,
                languages = languages,
                aboutMe = aboutMe,
                declaration = declaration,
                customization = customization,
                primaryColor = primColor,
                fontFam = fontFam,
                baseFontSize = baseFontSize,
                baseHeaderSize = baseHeadingSize,
                marginSize = marginSize
            )
            else -> SidebarSplitLayout( // corporate_blue
                personalInfo = personalInfo,
                passportInfo = passportInfo,
                workExperiences = workExperiences,
                educations = educations,
                skills = skills,
                languages = languages,
                aboutMe = aboutMe,
                declaration = declaration,
                customization = customization,
                primaryColor = primColor,
                fontFam = fontFam,
                baseFontSize = baseFontSize,
                baseHeaderSize = baseHeadingSize,
                marginSize = marginSize
            )
        }
        }
    }
}


// 1. ATS WHITE TEMPLATE (Classic highly scanner optimized layout, pure simplicity)
@Composable
fun AtsWhiteLayout(
    personalInfo: PersonalInfo,
    passportInfo: PassportInfo,
    workExperiences: List<WorkExperience>,
    educations: List<Education>,
    skills: List<Skill>,
    languages: List<Language>,
    aboutMe: AboutMe,
    declaration: Declaration,
    fontFam: FontFamily,
    baseFontSize: androidx.compose.ui.unit.TextUnit,
    baseHeaderSize: androidx.compose.ui.unit.TextUnit,
    marginSize: Dp,
    customization: Customization,
    primColor: Color,
    certifications: List<CertItem> = emptyList(),
    projects: List<ProjectItem> = emptyList(),
    references: List<ReferenceItem> = emptyList(),
    awards: List<AwardItem> = emptyList(),
    hobbies: List<HobbyItem> = emptyList(),
    socialLinks: List<SocialLinkItem> = emptyList(),
    customSectionsData: Map<String, String> = emptyMap()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(marginSize)
    ) {
        // Centered Header/Left Aligned Header with Profile photo support for ATS matching
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val showPhoto = customization.showProfilePhoto && personalInfo.profilePhotoUri.isNotBlank()
            if (showPhoto) {
                ProfilePhotoView(
                    photoUri = personalInfo.profilePhotoUri,
                    shapeName = customization.photoShape,
                    sizeDp = 65,
                    borderColor = primColor
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(
                modifier = if (showPhoto) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                horizontalAlignment = if (showPhoto) Alignment.Start else Alignment.CenterHorizontally
            ) {
                Text(
                    text = personalInfo.fullName.uppercase(),
                    fontSize = (baseHeaderSize.value + 6).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFam,
                    color = primColor,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = if (showPhoto) TextAlign.Left else TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                val contactItemsList = mutableListOf(
                    personalInfo.email,
                    personalInfo.phone,
                    personalInfo.currentAddress,
                    personalInfo.website
                )
                if (personalInfo.bloodGroup.isNotBlank()) {
                    contactItemsList.add("Blood Group: ${personalInfo.bloodGroup}")
                }
                val contactItems = contactItemsList.filter { it.isNotBlank() }
                Text(
                    text = contactItems.joinToString("  |  "),
                    fontSize = (baseFontSize.value - 1).sp,
                    fontFamily = fontFam,
                    color = Color.DarkGray,
                    textAlign = if (showPhoto) TextAlign.Left else TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = primColor, thickness = 1.5.dp)

        // Dynamic, Reorderable Sections Rendering
        val orderedSections = (customization.sections.ifEmpty { 
            listOf(
                SectionState("about", "About Me", false, false, 0),
                SectionState("work", "Work Experience", false, false, 1),
                SectionState("education", "Education", false, false, 2),
                SectionState("skills", "Skills", false, false, 3),
                SectionState("languages", "Languages", false, false, 4),
                SectionState("passport", "Passport Information", false, false, 5),
                SectionState("certifications", "Certifications", false, false, 6),
                SectionState("projects", "Projects", false, false, 7),
                SectionState("references", "References", false, false, 8),
                SectionState("declaration", "Declaration", false, false, 9),
                SectionState("awards", "Awards", false, false, 10),
                SectionState("hobbies", "Hobbies", false, false, 11),
                SectionState("social", "Social Links", false, false, 12)
            )
        }).sortedBy { it.order }.filter { !it.isHidden }

        orderedSections.forEach { sec ->
            when (sec.id) {
                "about" -> {
                    if (aboutMe.summary.isNotEmpty() || aboutMe.careerObjective.isNotEmpty()) {
                        AtsHeader(sec.name.uppercase(), fontFam, baseHeaderSize, primColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (aboutMe.summary.isNotEmpty()) {
                            Text(
                                text = aboutMe.summary,
                                fontSize = baseFontSize,
                                fontFamily = fontFam,
                                color = Color(0xFF222222),
                                lineHeight = (baseFontSize.value * 1.3).sp
                            )
                        }
                        if (aboutMe.careerObjective.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Objective: ${aboutMe.careerObjective}",
                                fontSize = baseFontSize,
                                fontFamily = fontFam,
                                fontStyle = FontStyle.Italic,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
                "work" -> {
                    if (workExperiences.isNotEmpty()) {
                        AtsHeader(sec.name.uppercase(), fontFam, baseHeaderSize, primColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        workExperiences.forEachIndexed { idx, exp ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${exp.jobPosition} - ${exp.companyName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = (baseFontSize.value + 1).sp,
                                        fontFamily = fontFam,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "${exp.startDate} – ${if (exp.isCurrentlyWorking) "Present" else exp.endDate}",
                                        fontSize = baseFontSize,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = fontFam,
                                        color = Color.Black
                                    )
                                }
                                if (exp.city.isNotBlank() || exp.country.isNotBlank()) {
                                    Text(
                                        text = listOf(exp.city, exp.country).filter { it.isNotBlank() }.joinToString(", "),
                                        fontSize = (baseFontSize.value - 1).sp,
                                        fontStyle = FontStyle.Italic,
                                        fontFamily = fontFam,
                                        color = Color.Gray
                                    )
                                }
                                if (exp.responsibilities.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val formattedResp = if (!exp.responsibilities.startsWith("•") && exp.responsibilities.contains("\n")) {
                                        "• " + exp.responsibilities.replace("\n", "\n• ")
                                    } else {
                                        exp.responsibilities
                                    }
                                    Text(
                                        text = formattedResp,
                                        fontSize = baseFontSize,
                                        fontFamily = fontFam,
                                        color = Color(0xFF333333)
                                    )
                                }
                                if (exp.achievements.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Achievements: ${exp.achievements}",
                                        fontSize = baseFontSize,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = fontFam,
                                        color = Color.Black
                                    )
                                }
                            }
                            if (idx < workExperiences.size - 1) Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
                "education" -> {
                    if (educations.isNotEmpty()) {
                        AtsHeader(sec.name.uppercase(), fontFam, baseHeaderSize, primColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        educations.forEach { edu ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${edu.degree} in ${edu.fieldOfStudy.ifEmpty { "General Study" }} (${edu.educationLevel})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = baseFontSize,
                                            fontFamily = fontFam,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "${edu.schoolName}, ${edu.city}, ${edu.country}",
                                            fontSize = baseFontSize,
                                            fontFamily = fontFam,
                                            color = Color.DarkGray
                                        )
                                    }
                                    Text(
                                        text = "${edu.startDate} – ${if (edu.isCurrentlyStudying) "Present" else edu.endDate}",
                                        fontSize = baseFontSize,
                                        fontFamily = fontFam,
                                        color = Color.Black
                                    )
                                }
                                if (edu.gpa.isNotEmpty()) {
                                    Text(text = "GPA: ${edu.gpa}", fontSize = (baseFontSize.value - 1).sp, fontFamily = fontFam)
                                }
                                if (edu.description.isNotEmpty()) {
                                    Text(text = edu.description, fontSize = (baseFontSize.value - 1).sp, fontFamily = fontFam, color = Color.Gray)
                                }
                                if (edu.mainSubjects.isNotEmpty()) {
                                    Text(text = "Main Subjects: ${edu.mainSubjects}", fontSize = (baseFontSize.value - 1).sp, fontFamily = fontFam)
                                }
                                if (edu.achievements.isNotEmpty()) {
                                    Text(text = "Achievements: ${edu.achievements}", fontSize = (baseFontSize.value - 1).sp, fontFamily = fontFam)
                                }
                                if (edu.certificates.isNotEmpty()) {
                                    Text(text = "Certificates: ${edu.certificates}", fontSize = (baseFontSize.value - 1).sp, fontFamily = fontFam)
                                }
                                if (edu.trainingCourses.isNotEmpty()) {
                                    Text(text = "Training Courses: ${edu.trainingCourses}", fontSize = (baseFontSize.value - 1).sp, fontFamily = fontFam)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                "skills" -> {
                    if (skills.isNotEmpty()) {
                        AtsHeader(sec.name.uppercase(), fontFam, baseHeaderSize, primColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        val techSkillsList = skills.filter { it.category == "Technical" }.map { it.name }
                        val softSkillsList = skills.filter { it.category == "Soft" }.map { it.name }
                        if (techSkillsList.isNotEmpty()) {
                            Text(
                                text = "Technical Expertise: " + techSkillsList.joinToString(", "),
                                fontSize = baseFontSize,
                                fontFamily = fontFam,
                                color = Color(0xFF111111)
                            )
                        }
                        if (softSkillsList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Core Skills: " + softSkillsList.joinToString(", "),
                                fontSize = baseFontSize,
                                fontFamily = fontFam,
                                color = Color(0xFF111111)
                            )
                        }
                    }
                }
                "languages" -> {
                    if (languages.isNotEmpty()) {
                        AtsHeader(sec.name.uppercase(), fontFam, baseHeaderSize, primColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            languages.forEach { lang ->
                                LanguageSkillRow(
                                    name = lang.name,
                                    valueText = lang.speakingLevel,
                                    fontFam = fontFam,
                                    baseFontSize = baseFontSize,
                                    color = Color(0xFF111111)
                                )
                            }
                        }
                    }
                }
                "passport" -> {
                    if (passportInfo.passportNumber.isNotBlank()) {
                        AtsHeader(sec.name.uppercase(), fontFam, baseHeaderSize, primColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Passport No: ${passportInfo.passportNumber} | Nationality: ${passportInfo.passportCountry} | Expiry: ${passportInfo.expiryDate}",
                            fontSize = baseFontSize,
                            fontFamily = fontFam,
                            color = Color(0xFF222222)
                        )
                    }
                }
                "certifications" -> {
                    if (certifications.isNotEmpty()) {
                        AtsHeader(sec.name.uppercase(), fontFam, baseHeaderSize, primColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        certifications.forEach { cert ->
                            Text(
                                text = "• ${cert.title} – Issued by ${cert.issuer} (${cert.date})",
                                fontSize = baseFontSize,
                                fontFamily = fontFam,
                                color = Color.Black
                            )
                        }
                    }
                }
                "projects" -> {
                    if (projects.isNotEmpty()) {
                        AtsHeader(sec.name.uppercase(), fontFam, baseHeaderSize, primColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        projects.forEach { proj ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = proj.name, fontWeight = FontWeight.Bold, fontSize = baseFontSize, fontFamily = fontFam)
                                    if (proj.link.isNotEmpty()) {
                                        Text(text = proj.link, fontSize = (baseFontSize.value - 1).sp, fontStyle = FontStyle.Italic, fontFamily = fontFam, color = Color.Gray)
                                    }
                                }
                                Text(text = proj.description, fontSize = baseFontSize, fontFamily = fontFam, color = Color.DarkGray)
                                if (proj.technologies.isNotEmpty()) {
                                    Text(text = "Tech Stack: ${proj.technologies}", fontSize = (baseFontSize.value - 1).sp, fontFamily = fontFam, fontStyle = FontStyle.Italic)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
                "references" -> {
                    if (references.isNotEmpty()) {
                        AtsHeader(sec.name.uppercase(), fontFam, baseHeaderSize, primColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        references.forEach { ref ->
                            Text(
                                text = "• ${ref.name} – ${ref.position}, ${ref.company} (${ref.contact})",
                                fontSize = baseFontSize,
                                fontFamily = fontFam,
                                color = Color.Black
                            )
                        }
                    }
                }
                "awards" -> {
                    if (awards.isNotEmpty()) {
                        AtsHeader(sec.name.uppercase(), fontFam, baseHeaderSize, primColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        awards.forEach { award ->
                            Text(
                                text = "• ${award.title} by ${award.issuer} in ${award.year} (${award.description})",
                                fontSize = baseFontSize,
                                fontFamily = fontFam,
                                color = Color.Black
                            )
                        }
                    }
                }
                "hobbies" -> {
                    if (hobbies.isNotEmpty()) {
                        AtsHeader(sec.name.uppercase(), fontFam, baseHeaderSize, primColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = hobbies.joinToString(", ") { it.name },
                            fontSize = baseFontSize,
                            fontFamily = fontFam,
                            color = Color.Black
                        )
                    }
                }
                "social" -> {
                    if (socialLinks.isNotEmpty()) {
                        AtsHeader(sec.name.uppercase(), fontFam, baseHeaderSize, primColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = socialLinks.joinToString("   |   ") { "${it.platform}: ${it.url}" },
                            fontSize = baseFontSize,
                            fontFamily = fontFam,
                            color = Color.Black
                        )
                    }
                }
                "declaration" -> {
                    if (declaration.text.isNotEmpty()) {
                        AtsHeader(sec.name.uppercase(), fontFam, baseHeaderSize, primColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = declaration.text,
                            fontSize = (baseFontSize.value - 1).sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = fontFam,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Date: ${declaration.date}", fontSize = baseFontSize, fontFamily = fontFam)
                            Text(text = "Declarant: ${declaration.fullName}", fontSize = baseFontSize, fontWeight = FontWeight.Bold, fontFamily = fontFam)
                        }
                    }
                }
                else -> {
                    // Custom Section Content
                    val cText = customSectionsData[sec.id] ?: ""
                    if (cText.isNotEmpty()) {
                        AtsHeader(sec.name.uppercase(), fontFam, baseHeaderSize, primColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cText,
                            fontSize = baseFontSize,
                            fontFamily = fontFam,
                            color = Color.Black,
                            lineHeight = (baseFontSize.value * 1.3).sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AtsHeader(text: String, fontFam: FontFamily, size: androidx.compose.ui.unit.TextUnit, primaryColor: Color = Color.Black) {
    Column {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = text,
            fontSize = size,
            fontWeight = FontWeight.Black,
            fontFamily = fontFam,
            color = primaryColor
        )
        Spacer(modifier = Modifier.height(3.dp))
        HorizontalDivider(color = primaryColor.copy(alpha = 0.5f), thickness = 1.dp)
    }
}

// 2. CLASSIC EUROPASS STYLE TEMPLATE (Europass banner left/right split blue headers)
@Composable
fun EuropassLayout(
    personalInfo: PersonalInfo,
    passportInfo: PassportInfo,
    workExperiences: List<WorkExperience>,
    educations: List<Education>,
    skills: List<Skill>,
    languages: List<Language>,
    aboutMe: AboutMe,
    declaration: Declaration,
    customization: Customization,
    primaryColor: Color,
    headerColor: Color,
    fontFam: FontFamily,
    baseFontSize: androidx.compose.ui.unit.TextUnit,
    baseHeaderSize: androidx.compose.ui.unit.TextUnit,
    marginSize: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // TOP Europass Blue Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerColor)
                .padding(horizontal = marginSize, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (customization.showProfilePhoto && personalInfo.profilePhotoUri.isNotBlank()) {
                        ProfilePhotoView(
                            photoUri = personalInfo.profilePhotoUri,
                            shapeName = customization.photoShape,
                            sizeDp = 80,
                            borderColor = Color(0xFFFFCC00)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    Column {
                        Text(
                            text = personalInfo.fullName.uppercase(),
                            fontSize = (baseHeaderSize.value + 6).sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = fontFam
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "★ Official Standard Layout",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFFCC00),
                            fontFamily = fontFam
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Official-Style Europass Star Circle Brand Logo on the Right
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Canvas(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(4.dp))) {
                        // Background adapts to active theme primary color!
                        drawRect(color = primaryColor)
                        
                        // Draw a lovely ring of 12 tiny gold-colored stars
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val radius = size.width * 0.28f
                        val goldColor = Color(0xFFFFCC00)
                        for (i in 0 until 12) {
                            val angleRad = (i * 30f) * (Math.PI / 180f)
                            val x = cx + radius * Math.cos(angleRad).toFloat()
                            val y = cy + radius * Math.sin(angleRad).toFloat()
                            drawCircle(color = goldColor, radius = 2.0f, center = androidx.compose.ui.geometry.Offset(x, y))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Europass CV",
                        letterSpacing = 0.5.sp,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFCC00),
                        fontFamily = fontFam
                    )
                }
            }
        }

        // Two-column split layout or full layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(marginSize)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Contact Block
                EuropassSectionHeader("CONTACT INFO", primaryColor, fontFam, baseHeaderSize)
                ContactDetailRow(Icons.Default.Email, personalInfo.email, fontFam, baseFontSize)
                ContactDetailRow(Icons.Default.Phone, personalInfo.phone, fontFam, baseFontSize)
                ContactDetailRow(Icons.Default.LocationOn, personalInfo.currentAddress, fontFam, baseFontSize)
                if (personalInfo.bloodGroup.isNotBlank()) {
                    ContactDetailRow(Icons.Default.Favorite, "Blood Group: " + personalInfo.bloodGroup, fontFam, baseFontSize)
                }
                if (personalInfo.website.isNotEmpty()) {
                    ContactDetailRow(Icons.Default.Launch, personalInfo.website, fontFam, baseFontSize)
                }

                // Summary
                if (aboutMe.summary.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    EuropassSectionHeader("ABOUT ME", primaryColor, fontFam, baseHeaderSize)
                    Text(
                        text = aboutMe.summary,
                        fontSize = baseFontSize,
                        fontFamily = fontFam,
                        color = Color.DarkGray
                    )
                }

                // Skills with progress/percentage indicators as per criteria!
                if (skills.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    EuropassSectionHeader("PERSONAL & CORE SKILLS", primaryColor, fontFam, baseHeaderSize)
                    skills.forEach { sk ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sk.name,
                                    fontSize = baseFontSize,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = fontFam,
                                    color = Color.Black
                                )
                                Text(
                                    text = "${sk.percentage}%",
                                    fontSize = (baseFontSize.value - 1).sp,
                                    fontFamily = fontFam,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            LinearProgressIndicator(
                                progress = { sk.percentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = primaryColor,
                                trackColor = Color(0xFFECEFF1)
                            )
                        }
                    }
                }

                // Passport Info
                if (passportInfo.passportNumber.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    EuropassSectionHeader("PASSPORT", primaryColor, fontFam, baseHeaderSize)
                    Text(
                        text = "No: ${passportInfo.passportNumber}\nCountry: ${passportInfo.passportCountry}\nExpires: ${passportInfo.expiryDate}",
                        fontSize = baseFontSize,
                        fontFamily = fontFam,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
            VerticalDivider(color = Color(0xFFDDDDDD), thickness = 1.dp)
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1.5f)) {
                // Employment
                if (workExperiences.isNotEmpty()) {
                    EuropassSectionHeader("WORK EXPERIENCE", primaryColor, fontFam, baseHeaderSize)
                    workExperiences.forEachIndexed { i, exp ->
                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                            Text(
                                text = exp.jobPosition,
                                fontWeight = FontWeight.Bold,
                                fontSize = (baseFontSize.value + 1).sp,
                                color = primaryColor,
                                fontFamily = fontFam
                            )
                            Text(
                                text = "${exp.companyName} | ${exp.startDate} - ${if (exp.isCurrentlyWorking) "Present" else exp.endDate}",
                                fontSize = (baseFontSize.value - 1).sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                fontFamily = fontFam
                            )
                            if (exp.responsibilities.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = exp.responsibilities,
                                    fontSize = baseFontSize,
                                    fontFamily = fontFam,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }

                // Education
                if (educations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    EuropassSectionHeader("EDUCATION", primaryColor, fontFam, baseHeaderSize)
                    educations.forEach { edu ->
                        Column(modifier = Modifier.padding(bottom = 10.dp)) {
                            Text(
                                text = edu.degree,
                                fontWeight = FontWeight.Bold,
                                fontSize = baseFontSize,
                                color = Color.Black,
                                fontFamily = fontFam
                            )
                            Text(
                                text = edu.schoolName,
                                fontSize = (baseFontSize.value - 1).sp,
                                color = Color.Gray,
                                fontFamily = fontFam
                            )
                            Text(
                                text = "${edu.startDate} - ${edu.endDate} | GPA ${edu.gpa}",
                                fontSize = (baseFontSize.value - 1).sp,
                                fontStyle = FontStyle.Italic,
                                fontFamily = fontFam
                            )
                        }
                    }
                }

                // Languages Section (Reading/Writing Levels)
                if (languages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    EuropassSectionHeader("LANGUAGES", primaryColor, fontFam, baseHeaderSize)
                    languages.forEach { lang ->
                        Column(modifier = Modifier.padding(bottom = 6.dp)) {
                            LanguageSkillRow(
                                name = lang.name,
                                valueText = lang.speakingLevel,
                                fontFam = fontFam,
                                baseFontSize = baseFontSize,
                                color = Color.Black
                            )
                        }
                    }
                }

                // Declaration Signature Block
                if (declaration.text.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    EuropassSectionHeader("DECLARATION", primaryColor, fontFam, baseHeaderSize)
                    Text(
                        text = declaration.text,
                        fontSize = (baseFontSize.value - 1).sp,
                        fontStyle = FontStyle.Italic,
                        fontFamily = fontFam,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Date: ${declaration.date}", fontSize = (baseFontSize.value - 1).sp, fontFamily = fontFam)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Signed: ${declaration.fullName}", fontSize = (baseFontSize.value - 1).sp, fontWeight = FontWeight.Bold, fontFamily = fontFam)
                            if (declaration.signaturePathJson.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                if (declaration.signaturePathJson == "signed") {
                                    Text(
                                        text = "[Signed Electronically]",
                                        fontSize = 10.sp,
                                        color = primaryColor,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = fontFam
                                    )
                                } else if (java.io.File(declaration.signaturePathJson).exists()) {
                                    AsyncImage(
                                        model = declaration.signaturePathJson,
                                        contentDescription = "Signature",
                                        modifier = Modifier.height(35.dp).widthIn(max = 120.dp),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                    )
                                } else {
                                    Text(
                                        text = "[Signed Electronically]",
                                        fontSize = 10.sp,
                                        color = primaryColor,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = fontFam
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

@Composable
fun EuropassSectionHeader(text: String, color: Color, fontFam: FontFamily, size: androidx.compose.ui.unit.TextUnit) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = text,
            fontSize = size,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = fontFam
        )
        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(color = color, thickness = 1.5.dp)
    }
}

@Composable
fun ContactDetailRow(icon: ImageVector, value: String, fontFam: FontFamily, size: androidx.compose.ui.unit.TextUnit) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, size = 14.dp, tint = Color.Gray)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value,
            fontSize = size,
            fontFamily = fontFam,
            color = Color.Black
        )
    }
}

@Composable
fun Icon(imageVector: ImageVector, contentDescription: String?, size: Dp, tint: Color) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        androidx.compose.material3.Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.fillMaxSize()
        )
    }
}


// 3. SIDEBAR SPLIT LAYOUT (Corporate, Creative, Slate Dark, elegantly colored Left/Right division)
@Composable
fun SidebarSplitLayout(
    personalInfo: PersonalInfo,
    passportInfo: PassportInfo,
    workExperiences: List<WorkExperience>,
    educations: List<Education>,
    skills: List<Skill>,
    languages: List<Language>,
    aboutMe: AboutMe,
    declaration: Declaration,
    customization: Customization,
    primaryColor: Color,
    fontFam: FontFamily,
    baseFontSize: androidx.compose.ui.unit.TextUnit,
    baseHeaderSize: androidx.compose.ui.unit.TextUnit,
    marginSize: Dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // Colored Sidebar Content Card
        Column(
            modifier = Modifier
                .weight(1f)
                .background(primaryColor)
                .fillMaxHeight()
                .padding(vertical = 20.dp, horizontal = 12.dp)
        ) {
            // Profile image & Name Row beautifully integrated
            val hasPhoto = customization.showProfilePhoto && personalInfo.profilePhotoUri.isNotBlank()
            if (hasPhoto) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    ProfilePhotoView(
                        photoUri = personalInfo.profilePhotoUri,
                        shapeName = customization.photoShape,
                        sizeDp = 52,
                        borderColor = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = personalInfo.fullName,
                        fontSize = (baseHeaderSize.value + 1).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = fontFam,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(
                    text = personalInfo.fullName,
                    fontSize = (baseHeaderSize.value + 1).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = fontFam,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Sidebar headers
            SidebarHeading("CONTACT", fontFam, baseHeaderSize)
            SidebarContactRow(Icons.Default.Email, personalInfo.email, fontFam, baseFontSize)
            SidebarContactRow(Icons.Default.Phone, personalInfo.phone, fontFam, baseFontSize)
            SidebarContactRow(Icons.Default.LocationOn, personalInfo.currentAddress, fontFam, baseFontSize)
            if (personalInfo.bloodGroup.isNotBlank()) {
                SidebarContactRow(Icons.Default.Favorite, "Blood: " + personalInfo.bloodGroup, fontFam, baseFontSize)
            }

            if (skills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SidebarHeading("EXPERTISE", fontFam, baseHeaderSize)
                skills.forEach { sk ->
                    Text(
                        text = "• ${sk.name}",
                        fontSize = (baseFontSize.value - 1).sp,
                        color = Color.White,
                        fontFamily = fontFam,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            if (languages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SidebarHeading("LANGUAGES", fontFam, baseHeaderSize)
                languages.forEach { lg ->
                    LanguageSkillRow(
                        name = lg.name,
                        valueText = lg.speakingLevel,
                        fontFam = fontFam,
                        baseFontSize = (baseFontSize.value - 1).sp,
                        color = Color.White
                    )
                }
            }
        }

        // Main content column
        Column(
            modifier = Modifier
                .weight(1.8f)
                .padding(marginSize)
        ) {
            if (aboutMe.summary.isNotEmpty()) {
                MainSectionHeading("SUMMARY", primaryColor, fontFam, baseHeaderSize)
                Text(
                    text = aboutMe.summary,
                    fontSize = baseFontSize,
                    fontFamily = fontFam,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (workExperiences.isNotEmpty()) {
                MainSectionHeading("EXPERIENCE", primaryColor, fontFam, baseHeaderSize)
                workExperiences.forEach { exp ->
                    Column(modifier = Modifier.padding(bottom = 10.dp)) {
                        Text(
                            text = exp.jobPosition,
                            fontWeight = FontWeight.Bold,
                            fontSize = baseFontSize,
                            color = Color.Black,
                            fontFamily = fontFam
                        )
                        Text(
                            text = "${exp.companyName} | ${exp.startDate} – ${if (exp.isCurrentlyWorking) "Present" else exp.endDate}",
                            fontSize = (baseFontSize.value - 1).sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryColor,
                            fontFamily = fontFam
                        )
                        if (exp.responsibilities.isNotEmpty()) {
                            Text(
                                text = exp.responsibilities,
                                fontSize = (baseFontSize.value - 1).sp,
                                fontFamily = fontFam,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            if (educations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                MainSectionHeading("EDUCATION", primaryColor, fontFam, baseHeaderSize)
                educations.forEach { edu ->
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = edu.degree,
                            fontWeight = FontWeight.Bold,
                            fontSize = baseFontSize,
                            color = Color.Black,
                            fontFamily = fontFam
                        )
                        Text(
                            text = "${edu.schoolName} (${edu.startDate} - ${edu.endDate})",
                            fontSize = (baseFontSize.value - 1).sp,
                            fontFamily = fontFam,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            if (declaration.text.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                MainSectionHeading("DECLARATION", primaryColor, fontFam, baseHeaderSize)
                Text(
                    text = declaration.text,
                    fontSize = (baseFontSize.value - 1).sp,
                    fontStyle = FontStyle.Italic,
                    fontFamily = fontFam,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Date: ${declaration.date}", fontSize = 10.sp, fontFamily = fontFam)
                    Text(text = "Name: ${declaration.fullName}", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = fontFam)
                }
            }
        }
    }
}

@Composable
fun SidebarHeading(text: String, fontFam: FontFamily, size: androidx.compose.ui.unit.TextUnit) {
    Column(modifier = Modifier.padding(bottom = 6.dp)) {
        Text(
            text = text,
            fontSize = (size.value - 1).sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = fontFam
        )
        HorizontalDivider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)
    }
}

@Composable
fun SidebarContactRow(icon: ImageVector, text: String, fontFam: FontFamily, size: androidx.compose.ui.unit.TextUnit) {
    if (text.isBlank()) return
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, size = 12.dp, tint = Color.White.copy(alpha = 0.9f))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = (size.value - 1).sp,
            color = Color.White,
            fontFamily = fontFam,
            maxLines = 1
        )
    }
}

@Composable
fun MainSectionHeading(text: String, color: Color, fontFam: FontFamily, size: androidx.compose.ui.unit.TextUnit) {
    Column(modifier = Modifier.padding(bottom = 6.dp)) {
        Text(
            text = text,
            fontSize = size,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = fontFam
        )
        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(color = color.copy(alpha = 0.3f), thickness = 1.dp)
    }
}

@Composable
fun ProfilePhotoView(
    photoUri: String,
    shapeName: String,
    sizeDp: Int = 65,
    borderColor: Color = Color.White
) {
    val shape = when (shapeName) {
        "square" -> RoundedCornerShape(8.dp)
        "oval" -> RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 8.dp, bottomEnd = 8.dp) // Dome oval portrait
        "simple" -> RoundedCornerShape(2.dp) // crisp sharp passport border
        else -> CircleShape // circular
    }
    
    val borderWidth = when (shapeName) {
        "simple" -> 3.dp
        else -> 1.5.dp
    }

    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .border(borderWidth, borderColor, shape)
            .clip(shape)
            .background(Color.White)
    ) {
        if (photoUri.startsWith("avatar_")) {
            val emoji = when (photoUri) {
                "avatar_male_1" -> "👨‍💼"
                "avatar_male_2" -> "🧔"
                "avatar_female_1" -> "👩‍💼"
                "avatar_female_2" -> "👩"
                "avatar_neutral" -> "👤"
                else -> "👤"
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = (sizeDp * 0.45f).sp
                )
            }
        } else {
            AsyncImage(
                model = photoUri,
                contentDescription = "Passport Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                alignment = Alignment.Center
            )
        }
    }
}

@Composable
fun LanguageSkillRow(
    name: String,
    valueText: String,
    fontFam: FontFamily,
    baseFontSize: androidx.compose.ui.unit.TextUnit,
    color: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = baseFontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = fontFam,
            color = color
        )
        
        // Render 1 to 5 Stars or %
        if (valueText.endsWith("%")) {
            // Render text percentage or progress indicator
            Text(
                text = valueText,
                fontSize = baseFontSize,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = fontFam,
                color = color
            )
        } else if (valueText.lowercase().contains("star") || valueText.contains("★") || valueText.contains("⭐")) {
            // Render 1-5 Star icons in Golden color!
            val starCount = when {
                valueText.contains("5") || valueText.count { it == '★' || it == '⭐' } == 5 -> 5
                valueText.contains("4") || valueText.count { it == '★' || it == '⭐' } == 4 -> 4
                valueText.contains("3") || valueText.count { it == '★' || it == '⭐' } == 3 -> 3
                valueText.contains("2") || valueText.count { it == '★' || it == '⭐' } == 2 -> 2
                valueText.contains("1") || valueText.count { it == '★' || it == '⭐' } == 1 -> 1
                else -> 5
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (i in 1..5) {
                    Text(
                        text = "★",
                        color = if (i <= starCount) Color(0xFFFFD700) else Color.LightGray,
                        fontSize = (baseFontSize.value + 1).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Default level format like "Fluent", "Native", "Advanced"
            Text(
                text = valueText,
                fontSize = (baseFontSize.value - 1).sp,
                fontStyle = FontStyle.Italic,
                fontFamily = fontFam,
                color = color
            )
        }
    }
}

@Composable
fun ElegantBlackLayout(
    personalInfo: PersonalInfo,
    passportInfo: PassportInfo,
    workExperiences: List<WorkExperience>,
    educations: List<Education>,
    skills: List<Skill>,
    languages: List<Language>,
    aboutMe: AboutMe,
    declaration: Declaration,
    customization: Customization,
    primaryColor: Color,
    fontFam: FontFamily,
    baseFontSize: androidx.compose.ui.unit.TextUnit,
    baseHeaderSize: androidx.compose.ui.unit.TextUnit,
    marginSize: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(marginSize),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Centered Elegant Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val hasPhoto = customization.showProfilePhoto && personalInfo.profilePhotoUri.isNotBlank()
            if (hasPhoto) {
                ProfilePhotoView(
                    photoUri = personalInfo.profilePhotoUri,
                    shapeName = customization.photoShape,
                    sizeDp = 70,
                    borderColor = primaryColor
                )
            }
            Text(
                text = personalInfo.fullName.uppercase(),
                fontSize = (baseHeaderSize.value + 4).sp,
                fontWeight = FontWeight.ExtraBold,
                color = primaryColor,
                fontFamily = fontFam,
                textAlign = TextAlign.Center
            )
            // Subtitle Contact Details inline or single row
            Text(
                text = listOfNotNull(
                    personalInfo.email.ifBlank { null },
                    personalInfo.phone.ifBlank { null },
                    personalInfo.city.ifBlank { null }
                ).joinToString("  •  "),
                fontSize = (baseFontSize.value - 1).sp,
                fontFamily = fontFam,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        HorizontalDivider(color = primaryColor.copy(alpha = 0.2f), thickness = 2.dp)

        // Summary Centered Box
        if (aboutMe.summary.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(primaryColor.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PROFESSIONAL COGNITIVE PROFILE",
                    fontSize = (baseFontSize.value - 1).sp,
                    fontWeight = FontWeight.Black,
                    color = primaryColor,
                    fontFamily = fontFam
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = aboutMe.summary,
                    fontSize = baseFontSize,
                    fontFamily = fontFam,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }

        // List items
        if (workExperiences.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MainSectionHeading("SELECTED PROFESSIONAL RECORD", primaryColor, fontFam, baseHeaderSize)
                workExperiences.forEach { exp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = exp.jobPosition,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = baseFontSize,
                                    color = Color.Black,
                                    fontFamily = fontFam
                                )
                                Text(
                                    text = "${exp.startDate} – ${if (exp.isCurrentlyWorking) "Present" else exp.endDate}",
                                    fontSize = (baseFontSize.value - 1.5).sp,
                                    fontWeight = FontWeight.Medium,
                                    color = primaryColor,
                                    fontFamily = fontFam
                                )
                            }
                            Text(
                                text = exp.companyName,
                                fontSize = (baseFontSize.value - 1).sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                fontFamily = fontFam
                            )
                            if (exp.responsibilities.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = exp.responsibilities,
                                    fontSize = (baseFontSize.value - 1).sp,
                                    fontFamily = fontFam,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }

        if (educations.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MainSectionHeading("ACADEMIC MILESTONES", primaryColor, fontFam, baseHeaderSize)
                educations.forEach { edu ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = edu.degree,
                                fontWeight = FontWeight.Bold,
                                fontSize = baseFontSize,
                                fontFamily = fontFam
                            )
                            Text(
                                text = edu.schoolName,
                                fontSize = (baseFontSize.value - 1).sp,
                                color = Color.Gray,
                                fontFamily = fontFam
                            )
                        }
                        Text(
                            text = "${edu.startDate} - ${edu.endDate}",
                            fontSize = (baseFontSize.value - 1).sp,
                            fontFamily = fontFam,
                            color = primaryColor
                        )
                    }
                }
            }
        }

        // Skills & languages horizontal grid
        if (skills.isNotEmpty() || languages.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (skills.isNotEmpty()) {
                    Column(modifier = Modifier.weight(1f)) {
                        MainSectionHeading("COMPETENCIES", primaryColor, fontFam, baseHeaderSize)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val skillsChunks = skills.take(6).chunked(3)
                            skillsChunks.forEach { chunk ->
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    chunk.forEach { sk ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFF3F4F6))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = sk.name,
                                                fontSize = (baseFontSize.value - 2).sp,
                                                fontFamily = fontFam,
                                                color = Color.DarkGray,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (languages.isNotEmpty()) {
                    Column(modifier = Modifier.weight(1f)) {
                        MainSectionHeading("LINGUISTICS", primaryColor, fontFam, baseHeaderSize)
                        languages.forEach { lg ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(lg.name, fontSize = (baseFontSize.value - 1).sp, fontWeight = FontWeight.Bold, fontFamily = fontFam)
                                Text(lg.speakingLevel, fontSize = (baseFontSize.value - 2).sp, fontFamily = fontFam, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        if (declaration.text.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                MainSectionHeading("LEGAL DECLARATION", primaryColor, fontFam, baseHeaderSize)
                Text(
                    text = declaration.text,
                    fontSize = (baseFontSize.value - 1.5).sp,
                    fontStyle = FontStyle.Italic,
                    fontFamily = fontFam,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Date: ${declaration.date}", fontSize = 10.sp, fontFamily = fontFam)
                    Text("Verified: ${declaration.fullName}", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = fontFam)
                }
            }
        }
    }
}

@Composable
fun ModernMinimalistLayout(
    personalInfo: PersonalInfo,
    passportInfo: PassportInfo,
    workExperiences: List<WorkExperience>,
    educations: List<Education>,
    skills: List<Skill>,
    languages: List<Language>,
    aboutMe: AboutMe,
    declaration: Declaration,
    customization: Customization,
    primaryColor: Color,
    fontFam: FontFamily,
    baseFontSize: androidx.compose.ui.unit.TextUnit,
    baseHeaderSize: androidx.compose.ui.unit.TextUnit,
    marginSize: Dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // Main Content (Left) - Takes more space
        Column(
            modifier = Modifier
                .weight(1.8f)
                .padding(marginSize)
        ) {
            // Big Elegant Left-aligned Header
            Text(
                text = personalInfo.fullName,
                fontSize = (baseHeaderSize.value + 4).sp,
                fontWeight = FontWeight.Black,
                color = primaryColor,
                fontFamily = fontFam
            )
            Text(
                text = "CANDIDATE DOSSIER / EXECUTED CAPABILITIES",
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontFamily = fontFam
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (aboutMe.summary.isNotEmpty()) {
                MainSectionHeading("DOSSIER SUMMARY", primaryColor, fontFam, baseHeaderSize)
                Text(
                    text = aboutMe.summary,
                    fontSize = baseFontSize,
                    fontFamily = fontFam,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (workExperiences.isNotEmpty()) {
                MainSectionHeading("CHRONOLOGICAL EXPERIENCE", primaryColor, fontFam, baseHeaderSize)
                workExperiences.forEach { exp ->
                    Column(modifier = Modifier.padding(bottom = 10.dp)) {
                        Text(
                            text = exp.jobPosition,
                            fontWeight = FontWeight.Bold,
                            fontSize = baseFontSize,
                            color = Color.Black,
                            fontFamily = fontFam
                        )
                        Text(
                            text = "${exp.companyName} | ${exp.startDate} – ${if (exp.isCurrentlyWorking) "Present" else exp.endDate}",
                            fontSize = (baseFontSize.value - 1).sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryColor,
                            fontFamily = fontFam
                        )
                        if (exp.responsibilities.isNotEmpty()) {
                            Text(
                                text = exp.responsibilities,
                                fontSize = (baseFontSize.value - 1).sp,
                                fontFamily = fontFam,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            if (educations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                MainSectionHeading("ACADEMIC PREPARATION", primaryColor, fontFam, baseHeaderSize)
                educations.forEach { edu ->
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = edu.degree,
                            fontWeight = FontWeight.Bold,
                            fontSize = baseFontSize,
                            color = Color.Black,
                            fontFamily = fontFam
                        )
                        Text(
                            text = "${edu.schoolName} (${edu.startDate} - ${edu.endDate})",
                            fontSize = (baseFontSize.value - 1).sp,
                            fontFamily = fontFam,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            if (declaration.text.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                MainSectionHeading("LEGAL COMPLIANCE", primaryColor, fontFam, baseHeaderSize)
                Text(
                    text = declaration.text,
                    fontSize = (baseFontSize.value - 1).sp,
                    fontStyle = FontStyle.Italic,
                    fontFamily = fontFam,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Date: ${declaration.date}", fontSize = 10.sp, fontFamily = fontFam)
                    Text(text = "Name: ${declaration.fullName}", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = fontFam)
                }
            }
        }

        // Right Sidebar (Contact, Skills, Languages) - Sided partition on the right!
        Column(
            modifier = Modifier
                .weight(1f)
                .background(primaryColor)
                .fillMaxHeight()
                .padding(vertical = 20.dp, horizontal = 12.dp)
        ) {
            val hasPhoto = customization.showProfilePhoto && personalInfo.profilePhotoUri.isNotBlank()
            if (hasPhoto) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProfilePhotoView(
                        photoUri = personalInfo.profilePhotoUri,
                        shapeName = customization.photoShape,
                        sizeDp = 56,
                        borderColor = Color.White
                    )
                }
            }

            SidebarHeading("DIRECT CONTACT", fontFam, baseHeaderSize)
            SidebarContactRow(Icons.Default.Email, personalInfo.email, fontFam, baseFontSize)
            SidebarContactRow(Icons.Default.Phone, personalInfo.phone, fontFam, baseFontSize)
            SidebarContactRow(Icons.Default.LocationOn, personalInfo.currentAddress, fontFam, baseFontSize)

            if (skills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SidebarHeading("EXPERTISE", fontFam, baseHeaderSize)
                skills.forEach { sk ->
                    Text(
                        text = "▪ ${sk.name}",
                        fontSize = (baseFontSize.value - 1).sp,
                        color = Color.White,
                        fontFamily = fontFam,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            if (languages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SidebarHeading("LINGUISTICS", fontFam, baseHeaderSize)
                languages.forEach { lg ->
                    LanguageSkillRow(
                        name = lg.name,
                        valueText = lg.speakingLevel,
                        fontFam = fontFam,
                        baseFontSize = (baseFontSize.value - 1).sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun LuxuryDarkLayout(
    personalInfo: PersonalInfo,
    passportInfo: PassportInfo,
    workExperiences: List<WorkExperience>,
    educations: List<Education>,
    skills: List<Skill>,
    languages: List<Language>,
    aboutMe: AboutMe,
    declaration: Declaration,
    customization: Customization,
    primaryColor: Color,
    fontFam: FontFamily,
    baseFontSize: androidx.compose.ui.unit.TextUnit,
    baseHeaderSize: androidx.compose.ui.unit.TextUnit,
    marginSize: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // Full width bold dark Luxury Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryColor)
                .padding(vertical = 24.dp, horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val hasPhoto = customization.showProfilePhoto && personalInfo.profilePhotoUri.isNotBlank()
            if (hasPhoto) {
                ProfilePhotoView(
                    photoUri = personalInfo.profilePhotoUri,
                    shapeName = "square", // Luxury signature look
                    sizeDp = 64,
                    borderColor = Color(0xFFEAB308) // Gold accent border
                )
            }
            Text(
                text = personalInfo.fullName.uppercase(),
                fontSize = (baseHeaderSize.value + 4).sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFEF08A), // Luxury gold name
                fontFamily = fontFam,
                textAlign = TextAlign.Center,
                letterSpacing = 1.5.sp
            )
            Text(
                text = personalInfo.email + "   |   " + personalInfo.phone + "   |   " + personalInfo.city,
                fontSize = (baseFontSize.value - 1).sp,
                color = Color.White.copy(alpha = 0.85f),
                fontFamily = fontFam,
                textAlign = TextAlign.Center
            )
        }

        // Contents Grid layout
        Column(modifier = Modifier.fillMaxWidth().padding(marginSize), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (aboutMe.summary.isNotEmpty()) {
                MainSectionHeading("EXECUTIVE SUMMARY", primaryColor, fontFam, baseHeaderSize)
                Text(
                    text = aboutMe.summary,
                    fontSize = baseFontSize,
                    fontFamily = fontFam,
                    color = Color.DarkGray,
                    lineHeight = 15.sp
                )
            }

            if (workExperiences.isNotEmpty()) {
                MainSectionHeading("PROFESSIONAL TENURE", primaryColor, fontFam, baseHeaderSize)
                workExperiences.forEach { exp ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFEF08A).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .background(primaryColor.copy(alpha = 0.02f))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = exp.jobPosition,
                                fontWeight = FontWeight.Bold,
                                fontSize = baseFontSize,
                                color = Color.Black,
                                fontFamily = fontFam
                            )
                            Text(
                                text = "${exp.startDate} – ${if (exp.isCurrentlyWorking) "Present" else exp.endDate}",
                                fontSize = (baseFontSize.value - 1).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor,
                                fontFamily = fontFam
                            )
                        }
                        Text(
                            text = exp.companyName,
                            fontSize = (baseFontSize.value - 1).sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            fontFamily = fontFam
                        )
                        if (exp.responsibilities.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = exp.responsibilities,
                                fontSize = (baseFontSize.value - 1).sp,
                                fontFamily = fontFam,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }

            if (skills.isNotEmpty() || languages.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (skills.isNotEmpty()) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            MainSectionHeading("CORE COMPETENCIES", primaryColor, fontFam, baseHeaderSize)
                            skills.forEach { sk ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                    Box(modifier = Modifier.size(5.dp).background(Color(0xFFEAB308), CircleShape))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(sk.name, fontSize = (baseFontSize.value - 1).sp, fontFamily = fontFam)
                                }
                            }
                        }
                    }

                    if (languages.isNotEmpty()) {
                        Column(modifier = Modifier.weight(0.8f)) {
                            MainSectionHeading("LINGUISTICS", primaryColor, fontFam, baseHeaderSize)
                            languages.forEach { lg ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(lg.name, fontSize = (baseFontSize.value - 1).sp, fontWeight = FontWeight.Bold, fontFamily = fontFam)
                                    Text(lg.speakingLevel, fontSize = (baseFontSize.value - 2).sp, fontFamily = fontFam, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreativeRoyalLayout(
    personalInfo: PersonalInfo,
    passportInfo: PassportInfo,
    workExperiences: List<WorkExperience>,
    educations: List<Education>,
    skills: List<Skill>,
    languages: List<Language>,
    aboutMe: AboutMe,
    declaration: Declaration,
    customization: Customization,
    primaryColor: Color,
    fontFam: FontFamily,
    baseFontSize: androidx.compose.ui.unit.TextUnit,
    baseHeaderSize: androidx.compose.ui.unit.TextUnit,
    marginSize: Dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // Fancy Left Sidebar
        Column(
            modifier = Modifier
                .weight(1.1f)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(primaryColor, primaryColor.copy(alpha = 0.85f))
                    )
                )
                .fillMaxHeight()
                .padding(vertical = 24.dp, horizontal = 12.dp)
        ) {
            val hasPhoto = customization.showProfilePhoto && personalInfo.profilePhotoUri.isNotBlank()
            if (hasPhoto) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProfilePhotoView(
                        photoUri = personalInfo.profilePhotoUri,
                        shapeName = "oval", // Creative shape
                        sizeDp = 60,
                        borderColor = Color.White
                    )
                }
            }

            Text(
                text = personalInfo.fullName,
                fontSize = (baseHeaderSize.value + 1.5).sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontFamily = fontFam,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            SidebarHeading("CONNECT ME 🚀", fontFam, baseHeaderSize)
            SidebarContactRow(Icons.Default.Email, personalInfo.email, fontFam, baseFontSize)
            SidebarContactRow(Icons.Default.Phone, personalInfo.phone, fontFam, baseFontSize)
            SidebarContactRow(Icons.Default.LocationOn, personalInfo.city, fontFam, baseFontSize)

            if (skills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                SidebarHeading("SUPERPOWERS ⚡", fontFam, baseHeaderSize)
                skills.forEach { sk ->
                    Box(
                        modifier = Modifier
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = sk.name,
                            fontSize = (baseFontSize.value - 2).sp,
                            color = Color.White,
                            fontFamily = fontFam,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Timeline Right Content
        Column(
            modifier = Modifier
                .weight(1.7f)
                .padding(marginSize)
        ) {
            if (aboutMe.summary.isNotEmpty()) {
                MainSectionHeading("WHO AM I?", primaryColor, fontFam, baseHeaderSize)
                Text(
                    text = aboutMe.summary,
                    fontSize = baseFontSize,
                    fontFamily = fontFam,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            if (workExperiences.isNotEmpty()) {
                MainSectionHeading("CAREER JOURNEY", primaryColor, fontFam, baseHeaderSize)
                workExperiences.forEach { exp ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Decorative Timeline Line
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(8.dp).background(primaryColor, CircleShape))
                            Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(primaryColor.copy(alpha = 0.3f)))
                        }

                        Column {
                            Text(
                                text = exp.jobPosition,
                                fontWeight = FontWeight.Bold,
                                fontSize = baseFontSize,
                                color = Color.Black,
                                fontFamily = fontFam
                            )
                            Text(
                                text = "${exp.companyName}  |  ${exp.startDate} – ${if (exp.isCurrentlyWorking) "Present" else exp.endDate}",
                                fontSize = (baseFontSize.value - 1.5).sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                fontFamily = fontFam
                            )
                            if (exp.responsibilities.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = exp.responsibilities,
                                    fontSize = (baseFontSize.value - 1).sp,
                                    fontFamily = fontFam,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }

            if (educations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                MainSectionHeading("KNOWLEDGE INCEPTION", primaryColor, fontFam, baseHeaderSize)
                educations.forEach { edu ->
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(6.dp).background(primaryColor.copy(alpha = 0.6f), CircleShape))
                            Box(modifier = Modifier.width(1.5.dp).fillMaxHeight().background(primaryColor.copy(alpha = 0.15f)))
                        }
                        Column {
                            Text(edu.degree, fontWeight = FontWeight.Bold, fontSize = baseFontSize, fontFamily = fontFam)
                            Text("${edu.schoolName} (${edu.startDate} - ${edu.endDate})", fontSize = (baseFontSize.value - 1).sp, color = Color.Gray, fontFamily = fontFam)
                        }
                    }
                }
            }
        }
    }
}
