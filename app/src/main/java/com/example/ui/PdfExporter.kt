package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.*
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    // Helper context class to carry document state, automatic page management and margins
    private class PdfContext(
        val pdfDocument: PdfDocument,
        val pageInfo: PdfDocument.PageInfo,
        val leftMargin: Float,
        val rightMargin: Float,
        val contentWidth: Float,
    ) {
        var currentPage: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var y: Float = 50f

        fun checkAndStartPageIfNeeded(requiredHeight: Float) {
            // Check if room runs out (A4 is 842f tall, we reserve bottom margin under 780f)
            if (currentPage == null || y + requiredHeight > 780f) {
                // Draw footer on old page before finishing it
                currentPage?.let { prevPage ->
                    canvas?.let { drawFooter(it) }
                    pdfDocument.finishPage(prevPage)
                }
                // Initialize a new standard A4 page
                val newPage = pdfDocument.startPage(pageInfo)
                currentPage = newPage
                canvas = newPage.canvas
                y = 50f
            }
        }
    }

    private fun drawFooter(canvas: Canvas) {
        val footerPaint = Paint().apply {
            isAntiAlias = true
            textSize = 8f
            color = android.graphics.Color.parseColor("#94A3B8")
        }
        val linePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#E2E8F0")
            strokeWidth = 0.5f
        }
        // Footer positioned perfectly at the bottom of the A4 layout (842f)
        canvas.drawLine(40f, 805f, 555f, 805f, linePaint)
        canvas.drawText("YOURDOC   |   BUILD YOUR FUTURE PROFESSIONALLY", 40f, 818f, footerPaint)
        canvas.drawText("ATS-Friendly Europass Standard", 410f, 818f, footerPaint)
    }

    // Wrap text elegantly and insert dynamic page breaks if the wrap spills into sequential pages
    private fun drawWrappedText(
        context: PdfContext,
        text: String,
        indentX: Float,
        paint: Paint,
        maxW: Float = -1f
    ) {
        val lines = text.split("\n")
        val maxWidth = if (maxW > 0f) maxW else context.contentWidth - (indentX - context.leftMargin)
        val lineSpacing = paint.textSize * 1.35f

        for (rawLine in lines) {
            val words = rawLine.split(" ")
            var line = ""
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(testLine) > maxWidth) {
                    context.checkAndStartPageIfNeeded(lineSpacing)
                    context.canvas?.drawText(line, indentX, context.y, paint)
                    context.y += lineSpacing
                    line = word
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) {
                context.checkAndStartPageIfNeeded(lineSpacing)
                context.canvas?.drawText(line, indentX, context.y, paint)
                context.y += lineSpacing
            }
        }
    }

    private fun drawWrappedTextInColumn(
        canvas: Canvas,
        text: String,
        indentX: Float,
        currentY: Float,
        paint: Paint,
        maxWidth: Float,
        lineSpacingMultiplier: Float = 1.35f
    ): Float {
        var yVal = currentY
        val lines = text.split("\n")
        val lineSpacing = paint.textSize * lineSpacingMultiplier
        for (rawLine in lines) {
            val words = rawLine.split(" ")
            var line = ""
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(testLine) > maxWidth) {
                    canvas.drawText(line, indentX, yVal, paint)
                    yVal += lineSpacing
                    line = word
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, indentX, yVal, paint)
                yVal += lineSpacing
            }
        }
        return yVal
    }

    private fun drawProfilePhoto(
        canvas: Canvas,
        photoPath: String,
        rect: android.graphics.RectF,
        shapeName: String
    ) {
        try {
            val file = java.io.File(photoPath)
            if (!file.exists()) return
            // High Definition: decode without scaling down to preserve crisp contours
            val options = android.graphics.BitmapFactory.Options().apply {
                inScaled = false
            }
            val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath, options) ?: return
            
            val paint = Paint().apply {
                isAntiAlias = true
                isFilterBitmap = true
                isDither = true
            }
            
            canvas.save()
            val path = android.graphics.Path()
            when (shapeName) {
                "square" -> {
                    path.addRoundRect(rect, 8f, 8f, android.graphics.Path.Direction.CW)
                    canvas.clipPath(path)
                }
                "oval" -> {
                    val radii = floatArrayOf(20f, 20f, 20f, 20f, 6f, 6f, 6f, 6f)
                    path.addRoundRect(rect, radii, android.graphics.Path.Direction.CW)
                    canvas.clipPath(path)
                }
                "simple" -> {
                    path.addRect(rect, android.graphics.Path.Direction.CW)
                    canvas.clipPath(path)
                }
                else -> { // circle
                    val cx = rect.centerX()
                    val cy = rect.centerY()
                    val radius = rect.width() / 2f
                    path.addCircle(cx, cy, radius, android.graphics.Path.Direction.CW)
                    canvas.clipPath(path)
                }
            }
            
            canvas.drawBitmap(bitmap, null, rect, paint)
            canvas.restore()
            
            // Draw premium border around the cropped shape
            val borderPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = if (shapeName == "simple") 3f else 1.5f
                color = android.graphics.Color.parseColor("#FFCC00") // Europass Gold accent
                isAntiAlias = true
            }
            
            val borderPath = android.graphics.Path()
            when (shapeName) {
                "square" -> {
                    borderPath.addRoundRect(rect, 8f, 8f, android.graphics.Path.Direction.CW)
                    canvas.drawPath(borderPath, borderPaint)
                }
                "oval" -> {
                    val radii = floatArrayOf(20f, 20f, 20f, 20f, 6f, 6f, 6f, 6f)
                    borderPath.addRoundRect(rect, radii, android.graphics.Path.Direction.CW)
                    canvas.drawPath(borderPath, borderPaint)
                }
                "simple" -> {
                    borderPath.addRect(rect, android.graphics.Path.Direction.CW)
                    canvas.drawPath(borderPath, borderPaint)
                }
                else -> { // circle
                    val cx = rect.centerX()
                    val cy = rect.centerY()
                    val radius = rect.width() / 2f
                    borderPath.addCircle(cx, cy, radius, android.graphics.Path.Direction.CW)
                    canvas.drawPath(borderPath, borderPaint)
                }
            }
            bitmap.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun drawTextLine(
        context: PdfContext,
        text: String,
        indentX: Float,
        paint: Paint,
        spacing: Float = 14f
    ) {
        context.checkAndStartPageIfNeeded(spacing)
        context.canvas?.drawText(text, indentX, context.y, paint)
        context.y += spacing
    }

    fun exportToPdfAndShare(
        context: Context,
        title: String,
        personalInfo: PersonalInfo,
        passportInfo: PassportInfo,
        workExperiences: List<WorkExperience>,
        educations: List<Education>,
        skills: List<Skill>,
        languages: List<Language>,
        aboutMe: AboutMe,
        declaration: Declaration,
        templateId: String = "europass_blue",
        showProfilePhoto: Boolean = true,
        photoShape: String = "circle",
        primaryColorHex: String? = null
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            
            val leftMargin = 40f
            val rightMargin = 555f
            val contentWidth = rightMargin - leftMargin

            val pdfContext = PdfContext(
                pdfDocument = pdfDocument,
                pageInfo = pageInfo,
                leftMargin = leftMargin,
                rightMargin = rightMargin,
                contentWidth = contentWidth
            )

            // Resolve beautiful thematic colors matching screen presets
            val themeColorHex = if (primaryColorHex != null && primaryColorHex.isNotBlank() && primaryColorHex.startsWith("#")) {
                primaryColorHex
            } else {
                when (templateId) {
                    "europass_blue" -> "#002F6C"
                    "europass_modern" -> "#059669"
                    "europass_dark" -> "#1E293B"
                    "europass_ruby" -> "#991B1B"
                    "europass_cyber" -> "#6D28D9"
                    "europass_amber" -> "#D97706"
                    "europass_platinum" -> "#475569"
                    "europass_forest" -> "#065F46"
                    "europass_royal" -> "#3730A3"
                    "europass_coral" -> "#DC2626"
                    "europass_charcoal" -> "#334155"
                    "europass_chocolate" -> "#78350F"
                    "europass_sky" -> "#0284C7"
                    "ats_white" -> "#1E293B"
                    "corporate_blue" -> "#1E3A8A"
                    "elegant_black" -> "#111827"
                    "modern_minimalist" -> "#475569"
                    "luxury_dark" -> "#854D0E"
                    "ats_charcoal" -> "#334155"
                    "creative_royal" -> "#4F46E5"
                    else -> "#002F6C"
                }
            }
            val primaryColorInt = android.graphics.Color.parseColor(themeColorHex)
            val headerColorHex = when (templateId) {
                "europass_dark" -> "#1E293B"
                "europass_ruby" -> "#450A0A"
                "europass_cyber" -> "#2E1065"
                "europass_amber" -> "#451A03"
                "europass_platinum" -> "#1E293B"
                "europass_forest" -> "#022C22"
                "europass_royal" -> "#1E1B4B"
                "europass_coral" -> "#7F1D1D"
                "europass_charcoal" -> "#0F172A"
                "europass_chocolate" -> "#451B03"
                "europass_sky" -> "#0C4A6E"
                "europass_modern" -> "#064E3B"
                else -> "#0A2540"
            }
            val headerColorInt = android.graphics.Color.parseColor(headerColorHex)

            // Core A4 canvas initialization
            pdfContext.checkAndStartPageIfNeeded(0f)
            val canvas = pdfContext.canvas!!

            // Check template category routing
            val isEuropass = templateId.contains("europass")
            val isSidebar = templateId.contains("sidebar") || templateId.startsWith("corporate_") || templateId.startsWith("elegant_") || templateId.startsWith("modern_") || templateId.startsWith("luxury_")

            if (isEuropass) {
                // ===================================
                // 📄 1. PREMIUM EUROPASS LAYOUT
                // ===================================
                // Top full-width modern header banner
                val bannerPaint = Paint().apply { color = headerColorInt }
                canvas.drawRect(30f, 25f, 565f, 125f, bannerPaint)

                // Render user Profile Picture left-aligned within the container
                val hasPhoto = showProfilePhoto && personalInfo.profilePhotoUri.isNotBlank()
                if (hasPhoto) {
                    val photoRect = android.graphics.RectF(50f, 37f, 130f, 117f)
                    drawProfilePhoto(canvas, personalInfo.profilePhotoUri, photoRect, photoShape)
                }

                // Render name text inside the banner
                val nameX = if (hasPhoto) 150f else 50f
                val namePaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 15f
                    isFakeBoldText = true
                    color = android.graphics.Color.WHITE
                }
                val fullnameStr = personalInfo.fullName.uppercase()
                canvas.drawText(fullnameStr, nameX, 70f, namePaint)

                val subNamePaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 8.5f
                    color = android.graphics.Color.parseColor("#FFFFCC00") // Gold accent subheader
                    isFakeBoldText = true
                }
                canvas.drawText("★ OFFICIAL EUROPEAN STANDARD CV", nameX, 86f, subNamePaint)

                // Europass Logo Brand Decoration: circular ring of stars on the far right
                val logoSize = 32f
                val logoX = 510f
                val logoY = 48f
                val logoRect = android.graphics.RectF(logoX, logoY, logoX + logoSize, logoY + logoSize)
                val logoBgPaint = Paint().apply { color = primaryColorInt; isAntiAlias = true }
                canvas.drawRoundRect(logoRect, 4f, 4f, logoBgPaint)
                
                val cx = logoX + logoSize / 2f
                val cy = logoY + logoSize / 2f
                val starPaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#FFFFCC00")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                for (i in 0 until 12) {
                    val angleRad = (i * 30f) * (Math.PI / 180f)
                    val sx = cx + (logoSize * 0.28f) * Math.cos(angleRad).toFloat()
                    val sy = cy + (logoSize * 0.28f) * Math.sin(angleRad).toFloat()
                    canvas.drawCircle(sx, sy, 1.5f, starPaint)
                }
                val euroLabelPaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#FFFFCC00")
                    textSize = 6.5f
                    isFakeBoldText = true
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("Europass CV", cx, logoY + logoSize + 8f, euroLabelPaint)

                // Two-column drawing vertical separation timeline line
                val timelinePaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#E2E8F0")
                    strokeWidth = 1f
                }
                canvas.drawLine(215f, 150f, 215f, 795f, timelinePaint)

                // Paints for column items
                val sectionHeadingPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 10f
                    isFakeBoldText = true
                    color = primaryColorInt
                }
                val bodyPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 8.5f
                    color = android.graphics.Color.BLACK
                }
                val datePaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 7.5f
                    color = android.graphics.Color.parseColor("#64748B")
                    isFakeBoldText = true
                }
                val boldBodyPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 8.5f
                    isFakeBoldText = true
                    color = android.graphics.Color.BLACK
                }

                // Render LEFT COLUMN (from 40f to 200f - Width = 160f)
                var yLeft = 160f

                // L1. CONTACT INFO
                canvas.drawText("CONTACT INFO", 40f, yLeft, sectionHeadingPaint)
                yLeft += 5f
                canvas.drawLine(40f, yLeft, 190f, yLeft, timelinePaint)
                yLeft += 14f

                val contactItems = mutableListOf<String>()
                contactItems.add("✉ ${personalInfo.email}")
                contactItems.add("📞 ${personalInfo.phone}")
                if (personalInfo.currentAddress.isNotBlank()) contactItems.add("📍 ${personalInfo.currentAddress}")
                if (personalInfo.website.isNotBlank()) contactItems.add("🌐 ${personalInfo.website}")
                if (personalInfo.bloodGroup.isNotBlank()) contactItems.add("🩸 Blood Group: ${personalInfo.bloodGroup}")

                for (item in contactItems) {
                    yLeft = drawWrappedTextInColumn(canvas, item, 40f, yLeft, bodyPaint, 150f) + 4f
                }

                // L2. ABOUT ME summaries
                if (aboutMe.summary.isNotBlank()) {
                    yLeft += 10f
                    canvas.drawText("ABOUT ME", 40f, yLeft, sectionHeadingPaint)
                    yLeft += 5f
                    canvas.drawLine(40f, yLeft, 190f, yLeft, timelinePaint)
                    yLeft += 14f
                    yLeft = drawWrappedTextInColumn(canvas, aboutMe.summary, 40f, yLeft, bodyPaint, 150f)
                }

                // L3. CORE SKILLS & EXPERTISE
                if (skills.isNotEmpty()) {
                    yLeft += 12f
                    canvas.drawText("CORE SKILLS", 40f, yLeft, sectionHeadingPaint)
                    yLeft += 5f
                    canvas.drawLine(40f, yLeft, 190f, yLeft, timelinePaint)
                    yLeft += 14f

                    val trackPaint = Paint().apply { color = android.graphics.Color.parseColor("#ECEFF1") }
                    val progressPaint = Paint().apply { color = primaryColorInt }

                    for (sk in skills) {
                        canvas.drawText(sk.name, 40f, yLeft, boldBodyPaint)
                        canvas.drawText("${sk.percentage}%", 175f, yLeft, datePaint)
                        yLeft += 5f
                        canvas.drawRect(40f, yLeft, 190f, yLeft + 3f, trackPaint)
                        canvas.drawRect(40f, yLeft, 40f + (150f * sk.percentage / 100f), yLeft + 3f, progressPaint)
                        yLeft += 12f
                    }
                }

                // L4. PASSPORT INFO
                if (passportInfo.passportNumber.isNotBlank()) {
                    yLeft += 8f
                    canvas.drawText("PASSPORT", 40f, yLeft, sectionHeadingPaint)
                    yLeft += 5f
                    canvas.drawLine(40f, yLeft, 190f, yLeft, timelinePaint)
                    yLeft += 14f
                    val passStr = "Passport No: ${passportInfo.passportNumber}\nCountry: ${passportInfo.passportCountry}\nExpires: ${passportInfo.expiryDate}"
                    yLeft = drawWrappedTextInColumn(canvas, passStr, 40f, yLeft, bodyPaint, 150f)
                }

                // Render RIGHT COLUMN (from 230f to 555f - Width = 325f)
                var yRight = 160f

                // R1. WORK EXPERIENCE
                if (workExperiences.isNotEmpty()) {
                    canvas.drawText("WORK EXPERIENCE", 230f, yRight, sectionHeadingPaint)
                    yRight += 5f
                    canvas.drawLine(230f, yRight, 555f, yRight, timelinePaint)
                    yRight += 14f

                    for (exp in workExperiences) {
                        canvas.drawText(exp.jobPosition.uppercase(), 230f, yRight, boldBodyPaint)
                        yRight += 11f
                        val metaStr = "${exp.companyName}  |  ${exp.startDate} - ${if (exp.isCurrentlyWorking) "Present" else exp.endDate}"
                        canvas.drawText(metaStr, 230f, yRight, datePaint)
                        yRight += 11f
                        if (exp.responsibilities.isNotBlank()) {
                            yRight = drawWrappedTextInColumn(canvas, exp.responsibilities, 230f, yRight, bodyPaint, 320f)
                        }
                        yRight += 6f
                    }
                }

                // R2. EDUCATION
                if (educations.isNotEmpty()) {
                    yRight += 8f
                    canvas.drawText("EDUCATION & DEGREES", 230f, yRight, sectionHeadingPaint)
                    yRight += 5f
                    canvas.drawLine(230f, yRight, 555f, yRight, timelinePaint)
                    yRight += 14f

                    for (edu in educations) {
                        canvas.drawText(edu.degree, 230f, yRight, boldBodyPaint)
                        yRight += 11f
                        val eduMeta = "${edu.schoolName} (${edu.startDate} - ${edu.endDate})"
                        canvas.drawText(eduMeta, 230f, yRight, datePaint)
                        yRight += 11f
                        if (edu.description.isNotBlank()) {
                            yRight = drawWrappedTextInColumn(canvas, edu.description, 230f, yRight, bodyPaint, 320f)
                        }
                        yRight += 6f
                    }
                }

                // R3. LANGUAGES
                if (languages.isNotEmpty()) {
                    yRight += 8f
                    canvas.drawText("LANGUAGES", 230f, yRight, sectionHeadingPaint)
                    yRight += 5f
                    canvas.drawLine(230f, yRight, 555f, yRight, timelinePaint)
                    yRight += 14f

                    val langStr = languages.joinToString(", ") { "${it.name} (${it.speakingLevel})" }
                    yRight = drawWrappedTextInColumn(canvas, langStr, 230f, yRight, bodyPaint, 320f)
                }

                // R4. DECLARATION
                if (declaration.text.isNotBlank()) {
                    yRight += 12f
                    canvas.drawText("DECLARATION", 230f, yRight, sectionHeadingPaint)
                    yRight += 5f
                    canvas.drawLine(230f, yRight, 555f, yRight, timelinePaint)
                    yRight += 14f
                    yRight = drawWrappedTextInColumn(canvas, declaration.text, 230f, yRight, bodyPaint, 320f) + 10f

                    canvas.drawText("Date: ${declaration.date}", 230f, yRight, bodyPaint)
                    canvas.drawText("Signature: ${declaration.fullName}", 410f, yRight, boldBodyPaint)
                    
                    val hasImgSig = declaration.signaturePathJson.isNotEmpty() && declaration.signaturePathJson != "signed" && File(declaration.signaturePathJson).exists()
                    if (hasImgSig) {
                        try {
                            val sigFile = File(declaration.signaturePathJson)
                            val sigBmp = android.graphics.BitmapFactory.decodeFile(sigFile.absolutePath)
                            if (sigBmp != null) {
                                val targetH = 22f
                                val targetW = sigBmp.width * (targetH / sigBmp.height)
                                val destRect = android.graphics.RectF(555f - targetW, yRight + 4f, 555f, yRight + 4f + targetH)
                                canvas.drawBitmap(sigBmp, null, destRect, Paint().apply { isFilterBitmap = true })
                                sigBmp.recycle()
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }

                pdfContext.y = maxOf(yLeft, yRight)

            } else if (isSidebar) {
                // ===================================
                // 📄 2. PREMIUM CORPORATE SIDEBAR LAYOUT
                // ===================================
                // Solid Sidebar Colored column panel on the left (height: 25f to 815f)
                val sidebarRectPaint = Paint().apply { color = primaryColorInt }
                canvas.drawRect(30f, 25f, 195f, 815f, sidebarRectPaint)

                // Sidebar coordinates
                var yLeft = 45f

                // Profile photo circle-cropped at top of sidebar
                val hasPhoto = showProfilePhoto && personalInfo.profilePhotoUri.isNotBlank()
                if (hasPhoto) {
                    val photoRect = android.graphics.RectF(72f, yLeft, 148f, yLeft + 76f)
                    drawProfilePhoto(canvas, personalInfo.profilePhotoUri, photoRect, "circle")
                    yLeft += 90f
                }

                // Style paints for white sidebar text
                val sideTitlePaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 10.5f
                    isFakeBoldText = true
                    color = android.graphics.Color.WHITE
                }
                val sideHeaderLinePaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#66FFFFFF")
                    strokeWidth = 0.8f
                }
                val sideBodyPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 8.5f
                    color = android.graphics.Color.parseColor("#F2FFFFFF")
                }

                // Render name inside the sidebar
                val nameLines = personalInfo.fullName.split(" ")
                for (nLine in nameLines) {
                    canvas.drawText(nLine.uppercase(), 45f, yLeft, sideTitlePaint.apply { textSize = 11.5f })
                    yLeft += 15f
                }
                yLeft += 8f

                // S1. CONTACT DETAILS
                canvas.drawText("CONTACT DETAILS", 45f, yLeft, sideTitlePaint)
                yLeft += 4f
                canvas.drawLine(45f, yLeft, 180f, yLeft, sideHeaderLinePaint)
                yLeft += 12f

                val sideContact = mutableListOf(
                    "✉  ${personalInfo.email}",
                    "📞  ${personalInfo.phone}"
                )
                if (personalInfo.currentAddress.isNotBlank()) sideContact.add("📍  ${personalInfo.currentAddress}")
                if (personalInfo.website.isNotBlank()) sideContact.add("🌐  ${personalInfo.website}")
                if (personalInfo.bloodGroup.isNotBlank()) sideContact.add("🩸  Blood: ${personalInfo.bloodGroup}")

                for (item in sideContact) {
                    yLeft = drawWrappedTextInColumn(canvas, item, 45f, yLeft, sideBodyPaint, 140f) + 4f
                }

                // S2. EXPERTISE / SKILLS
                if (skills.isNotEmpty()) {
                    yLeft += 14f
                    canvas.drawText("LANGUAGES & SKILLS", 45f, yLeft, sideTitlePaint)
                    yLeft += 4f
                    canvas.drawLine(45f, yLeft, 180f, yLeft, sideHeaderLinePaint)
                    yLeft += 12f

                    for (sk in skills.take(7)) {
                        canvas.drawText("• ${sk.name} (${sk.percentage}%)", 45f, yLeft, sideBodyPaint)
                        yLeft += 13f
                    }
                }

                // S3. LANGUAGES
                if (languages.isNotEmpty()) {
                    yLeft += 14f
                    canvas.drawText("LANGUAGES", 45f, yLeft, sideTitlePaint)
                    yLeft += 4f
                    canvas.drawLine(45f, yLeft, 180f, yLeft, sideHeaderLinePaint)
                    yLeft += 12f

                    for (lang in languages) {
                        canvas.drawText("• ${lang.name} (${lang.speakingLevel})", 45f, yLeft, sideBodyPaint)
                        yLeft += 13f
                    }
                }

                // Right Main column coordinates (starts at x = 215f, width = 340f)
                var yRight = 45f

                val rightTitlePaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 11.5f
                    isFakeBoldText = true
                    color = primaryColorInt
                }
                val rUnderlinePaint = Paint().apply {
                    color = (primaryColorInt and 0x00FFFFFF) or (51 shl 24)
                    strokeWidth = 1f
                }
                val rBoldBodyPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 9f
                    isFakeBoldText = true
                    color = android.graphics.Color.parseColor("#1E293B")
                }
                val dateGrayPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 8f
                    color = android.graphics.Color.parseColor("#475569")
                }
                val bodyTextPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 8.5f
                    color = android.graphics.Color.BLACK
                }

                // R1. SUMMARY
                if (aboutMe.summary.isNotBlank()) {
                    canvas.drawText("SUMMARY", 215f, yRight, rightTitlePaint)
                    yRight += 5f
                    canvas.drawLine(215f, yRight, 555f, yRight, rUnderlinePaint)
                    yRight += 14f
                    yRight = drawWrappedTextInColumn(canvas, aboutMe.summary, 215f, yRight, bodyTextPaint, 335f) + 12f
                }

                // R2. WORK EXPERIENCE
                if (workExperiences.isNotEmpty()) {
                    canvas.drawText("PROFESSIONAL EXPERIENCE", 215f, yRight, rightTitlePaint)
                    yRight += 5f
                    canvas.drawLine(215f, yRight, 555f, yRight, rUnderlinePaint)
                    yRight += 14f

                    for (exp in workExperiences) {
                        canvas.drawText(exp.jobPosition.uppercase(), 215f, yRight, rBoldBodyPaint)
                        canvas.drawText("${exp.companyName}   |   ${exp.startDate} – ${if (exp.isCurrentlyWorking) "Present" else exp.endDate}", 215f, yRight + 11f, dateGrayPaint)
                        yRight += 22f

                        if (exp.responsibilities.isNotBlank()) {
                            yRight = drawWrappedTextInColumn(canvas, exp.responsibilities, 215f, yRight, bodyTextPaint, 335f)
                        }
                        yRight += 6f
                    }
                }

                // R3. EDUCATION & ACADEMIC
                if (educations.isNotEmpty()) {
                    canvas.drawText("EDUCATION & ACADEMICS", 215f, yRight, rightTitlePaint)
                    yRight += 5f
                    canvas.drawLine(215f, yRight, 555f, yRight, rUnderlinePaint)
                    yRight += 14f

                    for (edu in educations) {
                        canvas.drawText(edu.degree, 215f, yRight, rBoldBodyPaint)
                        canvas.drawText("${edu.schoolName} (${edu.startDate} – ${edu.endDate})", 215f, yRight + 11f, dateGrayPaint)
                        yRight += 22f
                        if (edu.description.isNotBlank()) {
                            yRight = drawWrappedTextInColumn(canvas, edu.description, 215f, yRight, bodyTextPaint, 335f)
                        }
                        yRight += 6f
                    }
                }

                // R4. DECLARATION
                if (declaration.text.isNotBlank()) {
                    canvas.drawText("DECLARATION", 215f, yRight, rightTitlePaint)
                    yRight += 5f
                    canvas.drawLine(215f, yRight, 555f, yRight, rUnderlinePaint)
                    yRight += 14f
                    yRight = drawWrappedTextInColumn(canvas, declaration.text, 215f, yRight, bodyTextPaint, 335f) + 12f

                    canvas.drawText("Date: ${declaration.date}", 215f, yRight, dateGrayPaint)
                    canvas.drawText("Name: ${declaration.fullName}", 380f, yRight, rBoldBodyPaint)
                    
                    val hasImgSig = declaration.signaturePathJson.isNotEmpty() && declaration.signaturePathJson != "signed" && File(declaration.signaturePathJson).exists()
                    if (hasImgSig) {
                        try {
                            val sigFile = File(declaration.signaturePathJson)
                            val sigBmp = android.graphics.BitmapFactory.decodeFile(sigFile.absolutePath)
                            if (sigBmp != null) {
                                val targetH = 20f
                                val targetW = sigBmp.width * (targetH / sigBmp.height)
                                val destRect = android.graphics.RectF(555f - targetW, yRight + 4f, 555f, yRight + 4f + targetH)
                                canvas.drawBitmap(sigBmp, null, destRect, Paint().apply { isFilterBitmap = true })
                                sigBmp.recycle()
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }

                pdfContext.y = maxOf(yLeft, yRight)

            } else {
                // ===================================
                // 📄 3. ATS WHITE HIGH-COMPLIANT LAYOUT
                // ===================================
                val headerPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 15f
                    isFakeBoldText = true
                    color = primaryColorInt
                }
                val boldTextPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 9f
                    isFakeBoldText = true
                    color = android.graphics.Color.BLACK
                }
                val regularTextPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 9f
                    color = android.graphics.Color.BLACK
                }
                val itemTitlePaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 10.5f
                    isFakeBoldText = true
                    color = primaryColorInt
                }
                val dividerLinePaint = Paint().apply {
                    color = primaryColorInt
                    strokeWidth = 1.2f
                }

                // Photo at top center or left corner
                val hasPhoto = showProfilePhoto && personalInfo.profilePhotoUri.isNotBlank()
                if (hasPhoto) {
                    val photoRect = android.graphics.RectF(rightMargin - 65f, pdfContext.y - 12f, rightMargin, pdfContext.y - 12f + 65f)
                    drawProfilePhoto(canvas, personalInfo.profilePhotoUri, photoRect, photoShape)
                }

                // ATS centered header
                val boundW = if (hasPhoto) rightMargin - 75f else rightMargin
                val centerName = personalInfo.fullName.uppercase()
                canvas.drawText(centerName, leftMargin, pdfContext.y, headerPaint)
                pdfContext.y += 15f

                val contactDetails = mutableListOf(
                    personalInfo.email,
                    personalInfo.phone,
                    personalInfo.currentAddress
                )
                if (personalInfo.website.isNotBlank()) contactDetails.add(personalInfo.website)
                if (personalInfo.bloodGroup.isNotBlank()) contactDetails.add("Blood Group: ${personalInfo.bloodGroup}")
                val infoLine = contactDetails.filter { it.isNotBlank() }.joinToString("   |   ")

                pdfContext.y = drawWrappedTextInColumn(canvas, infoLine, leftMargin, pdfContext.y, regularTextPaint.apply { color = android.graphics.Color.parseColor("#475569") }, boundW - leftMargin) + 6f
                canvas.drawLine(leftMargin, pdfContext.y, rightMargin, pdfContext.y, dividerLinePaint)
                pdfContext.y += 18f

                // S1. SUMMARY
                if (aboutMe.summary.isNotBlank()) {
                    canvas.drawText("PROFESSIONAL SUMMARY", leftMargin, pdfContext.y, itemTitlePaint)
                    pdfContext.y += 4f
                    canvas.drawLine(leftMargin, pdfContext.y, rightMargin, pdfContext.y, Paint().apply { color = android.graphics.Color.parseColor("#E2E8F0") })
                    pdfContext.y += 12f

                    pdfContext.y = drawWrappedTextInColumn(canvas, aboutMe.summary, leftMargin, pdfContext.y, regularTextPaint, contentWidth) + 12f
                }

                // S2. WORK EXPERIENCE
                if (workExperiences.isNotEmpty()) {
                    canvas.drawText("WORK EXPERIENCE", leftMargin, pdfContext.y, itemTitlePaint)
                    pdfContext.y += 4f
                    canvas.drawLine(leftMargin, pdfContext.y, rightMargin, pdfContext.y, Paint().apply { color = android.graphics.Color.parseColor("#E2E8F0") })
                    pdfContext.y += 12f

                    for (exp in workExperiences) {
                        canvas.drawText("${exp.jobPosition} @ ${exp.companyName}", leftMargin, pdfContext.y, boldTextPaint)
                        val metaRange = "${exp.startDate} – ${if (exp.isCurrentlyWorking) "Present" else exp.endDate}"
                        canvas.drawText(metaRange, rightMargin - 150f, pdfContext.y, regularTextPaint)
                        pdfContext.y += 11f
                        if (exp.city.isNotBlank() || exp.country.isNotBlank()) {
                            canvas.drawText("${exp.city}, ${exp.country}", leftMargin, pdfContext.y, regularTextPaint.apply { color = android.graphics.Color.parseColor("#475569") })
                            pdfContext.y += 11f
                        }

                        if (exp.responsibilities.isNotBlank()) {
                            pdfContext.y = drawWrappedTextInColumn(canvas, "Key responsibilities: ${exp.responsibilities}", leftMargin + 8f, pdfContext.y, regularTextPaint.apply { color = android.graphics.Color.BLACK }, contentWidth - 8f)
                        }
                        pdfContext.y += 6f
                    }
                }

                // S3. EDUCATION
                if (educations.isNotEmpty()) {
                    canvas.drawText("EDUCATION", leftMargin, pdfContext.y, itemTitlePaint)
                    pdfContext.y += 4f
                    canvas.drawLine(leftMargin, pdfContext.y, rightMargin, pdfContext.y, Paint().apply { color = android.graphics.Color.parseColor("#E2E8F0") })
                    pdfContext.y += 12f

                    for (edu in educations) {
                        canvas.drawText("${edu.degree} — ${edu.schoolName}", leftMargin, pdfContext.y, boldTextPaint)
                        canvas.drawText("${edu.startDate} – ${edu.endDate}", rightMargin - 100f, pdfContext.y, regularTextPaint)
                        pdfContext.y += 11f
                        if (edu.description.isNotBlank()) {
                            pdfContext.y = drawWrappedTextInColumn(canvas, edu.description, leftMargin + 8f, pdfContext.y, regularTextPaint, contentWidth - 8f)
                        }
                        pdfContext.y += 4f
                    }
                    pdfContext.y += 8f
                }

                // S4. SKILLS & LANGUAGES SIDES BY LINE
                if (skills.isNotEmpty() || languages.isNotEmpty()) {
                    canvas.drawText("SKILLS & KEY COMPETENCIES", leftMargin, pdfContext.y, itemTitlePaint)
                    pdfContext.y += 4f
                    canvas.drawLine(leftMargin, pdfContext.y, rightMargin, pdfContext.y, Paint().apply { color = android.graphics.Color.parseColor("#E2E8F0") })
                    pdfContext.y += 12f

                    if (skills.isNotEmpty()) {
                        val skillsStr = skills.joinToString(", ") { "${it.name} (${it.percentage}%)" }
                        pdfContext.y = drawWrappedTextInColumn(canvas, "Skills: $skillsStr", leftMargin, pdfContext.y, regularTextPaint, contentWidth) + 6f
                    }
                    if (languages.isNotEmpty()) {
                        val langStr = languages.joinToString(", ") { "${it.name} (${it.speakingLevel})" }
                        pdfContext.y = drawWrappedTextInColumn(canvas, "Languages: $langStr", leftMargin, pdfContext.y, regularTextPaint, contentWidth) + 6f
                    }
                    pdfContext.y += 8f
                }

                // S5. DECLARATION
                if (declaration.text.isNotBlank()) {
                    canvas.drawText("DECLARATION", leftMargin, pdfContext.y, itemTitlePaint)
                    pdfContext.y += 4f
                    canvas.drawLine(leftMargin, pdfContext.y, rightMargin, pdfContext.y, Paint().apply { color = android.graphics.Color.parseColor("#E2E8F0") })
                    pdfContext.y += 12f

                    pdfContext.y = drawWrappedTextInColumn(canvas, declaration.text, leftMargin, pdfContext.y, regularTextPaint, contentWidth) + 12f

                    canvas.drawText("Date: ${declaration.date}", leftMargin, pdfContext.y, regularTextPaint)
                    canvas.drawText("Signature: ${declaration.fullName}", rightMargin - 180f, pdfContext.y, boldTextPaint)

                    val hasImgSig = declaration.signaturePathJson.isNotEmpty() && declaration.signaturePathJson != "signed" && File(declaration.signaturePathJson).exists()
                    if (hasImgSig) {
                        try {
                            val sigFile = File(declaration.signaturePathJson)
                            val sigBmp = android.graphics.BitmapFactory.decodeFile(sigFile.absolutePath)
                            if (sigBmp != null) {
                                val targetH = 22f
                                val targetW = sigBmp.width * (targetH / sigBmp.height)
                                val destRect = android.graphics.RectF(rightMargin - targetW, pdfContext.y + 4f, rightMargin, pdfContext.y + 4f + targetH)
                                canvas.drawBitmap(sigBmp, null, destRect, Paint().apply { isFilterBitmap = true })
                                sigBmp.recycle()
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }
            }

            // Draw final footer and seal the document pages
            pdfContext.currentPage?.let { lastPage ->
                pdfContext.canvas?.let { drawFooter(it) }
                pdfDocument.finishPage(lastPage)
            }

            // Save PDF locally to the cache directory for robust sharing
            val cacheDir = context.cacheDir
            val file = File(cacheDir, "${title.replace(" ", "_")}_Resume.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()

            // Open intent for instantaneous, native system-wide sharing and saving options
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Save or Send Resume PDF via"))

        } catch (e: Exception) {
            Toast.makeText(context, "Error creating offline PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportMergedPortfolioToPdfAndShare(
        context: Context,
        title: String,
        documents: List<VaultDocument>,
        savedResumes: List<Resume>
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            
            val leftMargin = 40f
            val rightMargin = 555f
            val contentWidth = rightMargin - leftMargin

            val pdfContext = PdfContext(
                pdfDocument = pdfDocument,
                pageInfo = pageInfo,
                leftMargin = leftMargin,
                rightMargin = rightMargin,
                contentWidth = contentWidth
            )

            val paint = Paint()
            val textPaint = Paint().apply {
                isAntiAlias = true
                textSize = 9.5f
                color = android.graphics.Color.BLACK
            }
            
            val headerPaint = Paint().apply {
                isAntiAlias = true
                textSize = 15f
                isFakeBoldText = true
                color = android.graphics.Color.parseColor("#002F6C")
            }

            val subtitlePaint = Paint().apply {
                isAntiAlias = true
                textSize = 10.5f
                isFakeBoldText = true
                color = android.graphics.Color.parseColor("#0F172A")
            }

            val italicPaint = Paint().apply {
                isAntiAlias = true
                textSize = 9f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
                color = android.graphics.Color.parseColor("#64748B")
            }

            val linePaint = Paint().apply {
                color = android.graphics.Color.parseColor("#CBD5E1")
                strokeWidth = 1f
            }

            pdfContext.currentPage?.let { prevPage ->
                drawFooter(prevPage.canvas)
                pdfDocument.finishPage(prevPage)
            }
            pdfContext.currentPage = null

            // Draw each document on separate pages
            for (doc in documents) {
                if (!doc.isEnabled) continue

                // Support real PDF page combining inside Vault Documents
                if (doc.type.equals("pdf", ignoreCase = true)) {
                    if (doc.pdfFilePath.isNotEmpty()) {
                        try {
                            val pdfFile = File(doc.pdfFilePath)
                            if (pdfFile.exists()) {
                                val fileDescriptor = android.os.ParcelFileDescriptor.open(pdfFile, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
                                val renderer = android.graphics.pdf.PdfRenderer(fileDescriptor)
                                val pageCount = renderer.pageCount
                                for (pageIndex in 0 until pageCount) {
                                    val rendererPage = renderer.openPage(pageIndex)
                                    val targetPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                                    val targetPage = pdfDocument.startPage(targetPageInfo)
                                    val canvas = targetPage.canvas
                                    
                                    canvas.drawColor(android.graphics.Color.WHITE)
                                    
                                    val bitmap = android.graphics.Bitmap.createBitmap(595, 842, android.graphics.Bitmap.Config.ARGB_8888)
                                    bitmap.eraseColor(android.graphics.Color.WHITE)
                                    
                                    rendererPage.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                                    
                                    canvas.drawText("VERIFIED VAULT PORTFOLIO  |  ${doc.title.uppercase()}", leftMargin, 25f, Paint().apply {
                                        color = android.graphics.Color.parseColor("#002F6C")
                                        textSize = 7f
                                        isFakeBoldText = true
                                        isAntiAlias = true
                                    })
                                    canvas.drawLine(leftMargin, 29f, rightMargin, 29f, Paint().apply {
                                        color = android.graphics.Color.parseColor("#CBD5E1")
                                        strokeWidth = 0.5f
                                    })
                                    
                                    pdfDocument.finishPage(targetPage)
                                    rendererPage.close()
                                    bitmap.recycle()
                                }
                                renderer.close()
                                fileDescriptor.close()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    continue
                }

                pdfContext.currentPage = pdfDocument.startPage(pageInfo)
                pdfContext.canvas = pdfContext.currentPage!!.canvas
                pdfContext.y = 70f

                pdfContext.canvas?.drawText("YOURDOC VERIFIED RESOURCE  |  TYPE: ${doc.type.uppercase()}", leftMargin, 40f, Paint().apply { color = android.graphics.Color.parseColor("#002F6C"); textSize = 8f; isFakeBoldText = true; isAntiAlias = true })
                pdfContext.canvas?.drawLine(leftMargin, 48f, rightMargin, 48f, linePaint)

                when (doc.type) {
                    "cv" -> {
                        val resume = savedResumes.find { it.id == doc.cvResumeId }
                        if (resume != null) {
                            val pInfo = try { JsonParser.fromJson<PersonalInfo>(resume.personalInfo) } catch(e: java.lang.Exception) { PersonalInfo() } ?: PersonalInfo()
                            val abMe = try { JsonParser.fromJson<AboutMe>(resume.aboutMe) } catch(e: java.lang.Exception) { AboutMe() } ?: AboutMe()
                            val exps = try { JsonParser.fromJsonList<WorkExperience>(resume.workExperiences) } catch(e: java.lang.Exception) { emptyList() }
                            val edus = try { JsonParser.fromJsonList<Education>(resume.educations) } catch(e: java.lang.Exception) { emptyList() }
                            val sks = try { JsonParser.fromJsonList<com.example.data.Skill>(resume.skills) } catch(e: java.lang.Exception) { emptyList() }

                            val customization = try { JsonParser.fromJson<Customization>(resume.customization) } catch(e: java.lang.Exception) { Customization() } ?: Customization()
                            val resumeTemplateId = customization.templateId
                            val showProfilePhoto = customization.showProfilePhoto
                            val photoShape = customization.photoShape

                            val hasPhoto = showProfilePhoto && pInfo.profilePhotoUri.isNotBlank()
                            val drawEuropassLogo = resumeTemplateId.contains("europass")

                            val cvThemeColorHex = if (customization.primaryColorHex.isNotBlank() && customization.primaryColorHex.startsWith("#")) {
                                customization.primaryColorHex
                            } else {
                                when (resumeTemplateId) {
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
                            }
                            val cvPrimaryColorInt = android.graphics.Color.parseColor(cvThemeColorHex)

                            if (drawEuropassLogo) {
                                val logoSize = 34f
                                val logoX = if (hasPhoto) rightMargin - 65f - 14f - logoSize else rightMargin - logoSize
                                val logoY = pdfContext.y - 12f
                                
                                val logoRect = android.graphics.RectF(logoX, logoY, logoX + logoSize, logoY + logoSize)
                                val logoBgPaint = Paint().apply {
                                    color = cvPrimaryColorInt
                                    isAntiAlias = true
                                }
                                pdfContext.canvas?.drawRoundRect(logoRect, 4f, 4f, logoBgPaint)
                                
                                val cx = logoX + logoSize / 2f
                                val cy = logoY + logoSize / 2f
                                val radius = logoSize * 0.28f
                                val starPaint = Paint().apply {
                                    color = android.graphics.Color.parseColor("#FFFFCC00") // Europass Gold
                                    style = Paint.Style.FILL
                                    isAntiAlias = true
                                }
                                for (i in 0 until 12) {
                                    val angleRad = (i * 30f) * (Math.PI / 180f)
                                    val starX = cx + radius * Math.cos(angleRad).toFloat()
                                    val starY = cy + radius * Math.sin(angleRad).toFloat()
                                    pdfContext.canvas?.drawCircle(starX, starY, 1.8f, starPaint)
                                }
                                
                                val labelPaint = Paint().apply {
                                    color = android.graphics.Color.parseColor("#FFFFCC00")
                                    textSize = 7f
                                    isFakeBoldText = true
                                    isAntiAlias = true
                                    textAlign = Paint.Align.CENTER
                                }
                                pdfContext.canvas?.drawText("Europass CV", cx, logoY + logoSize + 9f, labelPaint)
                            }

                            if (hasPhoto) {
                                val photoSize = 65f
                                val photoRect = android.graphics.RectF(rightMargin - photoSize, pdfContext.y - 12f, rightMargin, pdfContext.y - 12f + photoSize)
                                drawProfilePhoto(pdfContext.canvas!!, pInfo.profilePhotoUri, photoRect, photoShape)
                            }

                            // Bound left text so it won't overlap the rightside corner photo or logo
                            val headerRightBound = if (hasPhoto || drawEuropassLogo) {
                                if (hasPhoto && drawEuropassLogo) rightMargin - 65f - 14f - 34f - 10f else rightMargin - 75f
                            } else {
                                rightMargin
                            }

                            pdfContext.canvas?.drawText(doc.title.uppercase(), leftMargin, pdfContext.y, headerPaint.apply { color = cvPrimaryColorInt; textSize = 14f })
                            pdfContext.y += 16f

                            drawTextLine(pdfContext, "Full Name: ${pInfo.fullName}", leftMargin, textPaint.apply { isFakeBoldText = true })
                            val contactStr = "Contact: ${pInfo.email} | ${pInfo.phone} | ${pInfo.currentAddress}" + (if (pInfo.bloodGroup.isNotBlank()) " | Blood Group: ${pInfo.bloodGroup}" else "")
                            drawWrappedText(pdfContext, contactStr, leftMargin, textPaint.apply { isFakeBoldText = false; color = android.graphics.Color.parseColor("#475569") }, maxW = headerRightBound - leftMargin)
                            pdfContext.y += 12f

                            if (abMe.summary.isNotEmpty()) {
                                pdfContext.canvas?.drawText("About Me / Profile Summary:", leftMargin, pdfContext.y, subtitlePaint.apply { textSize = 11f })
                                pdfContext.y += 14f
                                drawWrappedText(pdfContext, abMe.summary, leftMargin + 10f, textPaint)
                                pdfContext.y += 10f
                            }

                            if (exps.isNotEmpty()) {
                                pdfContext.canvas?.drawText("Work Experience:", leftMargin, pdfContext.y, subtitlePaint)
                                pdfContext.y += 14f
                                for (exp in exps.take(4)) {
                                    pdfContext.canvas?.drawText("${exp.jobPosition} at ${exp.companyName} (${exp.startDate} - ${if (exp.isCurrentlyWorking) "Present" else exp.endDate})", leftMargin + 10f, pdfContext.y, textPaint.apply { isFakeBoldText = true })
                                    pdfContext.y += 12f
                                    if (exp.responsibilities.isNotEmpty()) {
                                        drawWrappedText(pdfContext, exp.responsibilities, leftMargin + 20f, textPaint.apply { isFakeBoldText = false })
                                        pdfContext.y += 4f
                                    }
                                }
                                pdfContext.y += 10f
                            }

                            if (edus.isNotEmpty()) {
                                pdfContext.canvas?.drawText("Education qualifications:", leftMargin, pdfContext.y, subtitlePaint)
                                pdfContext.y += 14f
                                for (edu in edus.take(3)) {
                                    pdfContext.canvas?.drawText("${edu.degree} - ${edu.schoolName} (${edu.startDate} - ${edu.endDate})", leftMargin + 10f, pdfContext.y, textPaint.apply { isFakeBoldText = true })
                                    pdfContext.y += 12f
                                }
                                pdfContext.y += 10f
                            }

                            if (sks.isNotEmpty()) {
                                val skillLine = sks.joinToString(", ") { "${it.name} (${it.percentage}%)" }
                                pdfContext.canvas?.drawText("Core Skills Panel:", leftMargin, pdfContext.y, subtitlePaint)
                                pdfContext.y += 14f
                                drawWrappedText(pdfContext, skillLine, leftMargin + 10f, textPaint.apply { isFakeBoldText = false })
                            }
                        } else {
                            pdfContext.canvas?.drawText("CURRICULUM VITAE (CV)", leftMargin, pdfContext.y, headerPaint.apply { textSize = 16f })
                            pdfContext.y += 30f
                            drawTextLine(pdfContext, "No saved CV is selected. Please select a resume CV draft from your YourDoc list.", leftMargin, italicPaint)
                        }
                    }
                    "passport" -> {
                        pdfContext.canvas?.drawText("IDENTIFICATION CREDENTIAL (PASSPORT)", leftMargin, pdfContext.y, headerPaint.apply { textSize = 16f })
                        pdfContext.y += 30f

                        val pBoxPaint = Paint().apply {
                            color = android.graphics.Color.parseColor("#F1F5F9")
                        }
                        pdfContext.canvas?.drawRect(leftMargin, pdfContext.y, rightMargin, pdfContext.y + 200f, pBoxPaint)

                        val pBorder = Paint().apply {
                            color = android.graphics.Color.parseColor("#002F6C")
                            style = Paint.Style.STROKE
                            strokeWidth = 1.5f
                        }
                        pdfContext.canvas?.drawRect(leftMargin, pdfContext.y, rightMargin, pdfContext.y + 200f, pBorder)

                        val pYStart = pdfContext.y + 30f
                        pdfContext.canvas?.drawText("PASSPORT DOCUMENT SECURED IN VAULT", leftMargin + 20f, pYStart, subtitlePaint.apply { textSize = 12f; color = android.graphics.Color.parseColor("#002F6C") })
                        
                        val tPaint = textPaint.apply { isFakeBoldText = true; color = android.graphics.Color.BLACK }
                        pdfContext.canvas?.drawText("Passport Number: ${doc.passportNo}", leftMargin + 20f, pYStart + 30f, tPaint)
                        pdfContext.canvas?.drawText("Full Holder Name: ${doc.passportFullName}", leftMargin + 20f, pYStart + 55f, tPaint)
                        pdfContext.canvas?.drawText("Issuing Nationality: ${doc.passportCountry}", leftMargin + 20f, pYStart + 80f, tPaint)
                        pdfContext.canvas?.drawText("Date of Birth: ${doc.passportDob}", leftMargin + 20f, pYStart + 105f, tPaint)
                        pdfContext.canvas?.drawText("Date of Issue: ${doc.passportIssueDate}", leftMargin + 20f, pYStart + 130f, tPaint)
                        pdfContext.canvas?.drawText("Date of Expiry: ${doc.passportExpiryDate}", leftMargin + 20f, pYStart + 155f, tPaint)

                        pdfContext.y += 240f
                        drawTextLine(pdfContext, "This declares that the passport credentials entered above are safe-kept", leftMargin, italicPaint)
                        drawTextLine(pdfContext, "and managed secure locally. Perfect for recruitment verification.", leftMargin, italicPaint)
                    }
                    "experience" -> {
                        pdfContext.canvas?.drawText("OFFICIAL WORK EXPERIENCE CERTIFICATE", leftMargin, pdfContext.y, headerPaint.apply { textSize = 16f })
                        pdfContext.y += 40f

                        drawTextLine(pdfContext, "Issue Date: ${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())}", leftMargin, italicPaint)
                        pdfContext.y += 20f

                        val body = "To Whom It May Concern,\n\nThis is to certify that ${doc.passportFullName.ifEmpty { "the holder" }} was formally employed at the organization ${doc.expCompany} as a professional ${doc.expRole} starting from ${doc.expStartDate} until ${doc.expEndDate}.\n\nDuring their professional tenure, they demonstrated magnificent hard work, outstanding mastery over core technologies, and high level team cohesion. Their key details and contributions included:\n\n${doc.expDetails}\n\nWe provide this experience certificate on their humble request, and wish them stellar achievements ahead in their career trajectory."
                        
                        drawWrappedText(pdfContext, body, leftMargin, textPaint.apply { isFakeBoldText = false })

                        pdfContext.y += 80f
                        pdfContext.canvas?.drawLine(leftMargin, pdfContext.y, leftMargin + 200f, pdfContext.y, linePaint)
                        pdfContext.y += 14f
                        drawTextLine(pdfContext, "Authorized HR / Director Signatures", leftMargin, textPaint.apply { isFakeBoldText = true })
                        drawTextLine(pdfContext, doc.expCompany, leftMargin, italicPaint)
                    }
                    "certificate" -> {
                        pdfContext.canvas?.drawText("CERTIFICATE OF COURSE COMPLETION", leftMargin, pdfContext.y, headerPaint.apply { textSize = 16f })
                        pdfContext.y += 40f

                        val certBody = "This qualifies that the career applicant has successfully completed the coursework :\n\n${doc.certTitle}\n\nConducted and Certified By: ${doc.certIssuer}\nDate of completion: ${doc.certDate}\nGlobal Verification ID: ${doc.certCode}\n\nThis validates structural expertise in the field. Authentic digital credential."
                        
                        drawWrappedText(pdfContext, certBody, leftMargin, textPaint.apply { isFakeBoldText = false })

                        pdfContext.y += 120f
                        pdfContext.canvas?.drawCircle(rightMargin - 100f, pdfContext.y, 40f, Paint().apply { color = android.graphics.Color.parseColor("#F1F5F9"); style = Paint.Style.FILL })
                        pdfContext.canvas?.drawCircle(rightMargin - 100f, pdfContext.y, 40f, Paint().apply { color = android.graphics.Color.parseColor("#002F6C"); style = Paint.Style.STROKE; strokeWidth = 1f })
                        pdfContext.canvas?.drawText("VERIFIED", rightMargin - 124f, pdfContext.y - 5f, Paint().apply { textSize = 8f; isFakeBoldText = true; color = android.graphics.Color.parseColor("#002F6C") })
                        pdfContext.canvas?.drawText("CREDENT", rightMargin - 122f, pdfContext.y + 10f, Paint().apply { textSize = 8f; isFakeBoldText = true; color = android.graphics.Color.parseColor("#002F6C") })
                    }
                    "pcc" -> {
                        pdfContext.canvas?.drawText("POLICE REPUTATION & BACKGROUND CLEARANCE", leftMargin, pdfContext.y, headerPaint.apply { textSize = 16f })
                        pdfContext.y += 40f

                        val pccBody = "To Whom It May Concern,\n\nThis is to certify that a comprehensive criminal record check and biometric background verification has been queried from the databases of ${doc.pccAuthority} for the applicant:\n\nHolder Full Name: ${doc.pccFullName}\nCertificate Date: ${doc.pccIssueDate}\nResulting Status: ${doc.pccStatus.uppercase()}\n\nVerified background check shows the holder has zero pending court files or past criminal cases on records. Clear report is secure."

                        drawWrappedText(pdfContext, pccBody, leftMargin, textPaint.apply { isFakeBoldText = false })

                        pdfContext.y += 100f
                        pdfContext.canvas?.drawLine(leftMargin, pdfContext.y, leftMargin + 200f, pdfContext.y, linePaint)
                        pdfContext.y += 14f
                        drawTextLine(pdfContext, "Chief of Police Station / Verification Authority", leftMargin, textPaint.apply { isFakeBoldText = true })
                        drawTextLine(pdfContext, doc.pccAuthority, leftMargin, italicPaint)
                    }
                    "custom" -> {
                        pdfContext.canvas?.drawText(doc.title.uppercase(), leftMargin, pdfContext.y, headerPaint.apply { textSize = 16f })
                        pdfContext.y += 30f

                        drawWrappedText(pdfContext, doc.customBody, leftMargin, textPaint.apply { isFakeBoldText = false })
                    }
                }

                pdfContext.currentPage?.let { prevPage ->
                    drawFooter(prevPage.canvas)
                    pdfDocument.finishPage(prevPage)
                }
                pdfContext.currentPage = null
            }

            // Save PDF locally
            val cacheDir = context.cacheDir
            val file = File(cacheDir, "${title.replace(" ", "_")}_Portfolio.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()

            // Open intent for sharing
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Your Combined Document Portfolio"))

        } catch (e: Exception) {
            Toast.makeText(context, "Error creating merged portfolio: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportCoverLetterToPdfAndShare(
        context: Context,
        fullName: String,
        email: String,
        phone: String,
        address: String,
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
        date: String,
        subjectLine: String,
        bodyText: String,
        primaryColorHex: String = "#002F6C",
        secondaryColorHex: String = "#002F6C",
        headerFontHex: String = "#002F6C"
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            
            val leftMargin = 50f
            val rightMargin = 545f
            val contentWidth = rightMargin - leftMargin

            val pdfContext = PdfContext(
                pdfDocument = pdfDocument,
                pageInfo = pageInfo,
                leftMargin = leftMargin,
                rightMargin = rightMargin,
                contentWidth = contentWidth
            )

            // Parse template hex colors
            val primaryColor = try {
                android.graphics.Color.parseColor(primaryColorHex)
            } catch (e: Exception) {
                android.graphics.Color.parseColor("#002F6C")
            }
            val secondaryColor = try {
                android.graphics.Color.parseColor(secondaryColorHex)
            } catch (e: Exception) {
                android.graphics.Color.parseColor("#002F6C")
            }
            val headerColor = try {
                android.graphics.Color.parseColor(headerFontHex)
            } catch (e: Exception) {
                android.graphics.Color.parseColor("#002F6C")
            }

            // Paint styles
            val textPaint = Paint().apply {
                isAntiAlias = true
                textSize = 10.5f
                color = android.graphics.Color.BLACK
            }
            
            val headerPaint = Paint().apply {
                isAntiAlias = true
                textSize = 18f
                isFakeBoldText = true
                color = headerColor
            }

            val accentPaint = Paint().apply {
                isAntiAlias = true
                textSize = 11f
                isFakeBoldText = true
                color = secondaryColor
            }

            val subPaint = Paint().apply {
                isAntiAlias = true
                textSize = 9f
                color = android.graphics.Color.DKGRAY
            }

            val linePaint = Paint().apply {
                color = primaryColor
                strokeWidth = 1.5f
            }

            pdfContext.checkAndStartPageIfNeeded(10f) // Ensures first page is initialized

            // 1. Draw Applicant Header Information
            val senderName = fullName.ifBlank { "APPLICANT" }
            pdfContext.canvas?.drawText(senderName.uppercase(), leftMargin, pdfContext.y, headerPaint)
            pdfContext.y += 18f
            
            // Sub-info contact details line
            val contactLine = listOfNotNull(
                email.takeIf { it.isNotBlank() },
                phone.takeIf { it.isNotBlank() },
                address.takeIf { it.isNotBlank() }
            ).joinToString("  |  ")
            
            pdfContext.canvas?.drawText(contactLine, leftMargin, pdfContext.y, subPaint)
            pdfContext.y += 10f
            
            // Draw matching line
            pdfContext.canvas?.drawLine(leftMargin, pdfContext.y, rightMargin, pdfContext.y, linePaint)
            pdfContext.y += 25f

            // 2. Draw Recipient / Target meta
            val displayDate = date.ifBlank {
                val sdf = java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.ENGLISH)
                sdf.format(java.util.Date())
            }
            pdfContext.canvas?.drawText("Date: $displayDate", leftMargin, pdfContext.y, textPaint)
            pdfContext.y += 15f
            
            val managerLine = when {
                hiringManagerName.isNotBlank() && hiringManagerPosition.isNotBlank() -> "$hiringManagerName ($hiringManagerPosition)"
                hiringManagerName.isNotBlank() -> hiringManagerName
                hiringManagerPosition.isNotBlank() -> hiringManagerPosition
                else -> "The Hiring Manager / Recruiting Committee"
            }
            pdfContext.canvas?.drawText("To,", leftMargin, pdfContext.y, textPaint.apply { isFakeBoldText = true })
            pdfContext.y += 14f
            pdfContext.canvas?.drawText(managerLine, leftMargin, pdfContext.y, textPaint.apply { isFakeBoldText = false })
            pdfContext.y += 14f
            
            if (companyName.isNotBlank()) {
                pdfContext.canvas?.drawText(companyName, leftMargin, pdfContext.y, accentPaint)
                pdfContext.y += 14f
                
                val locationList = listOfNotNull(
                    companyAddress.takeIf { it.isNotBlank() },
                    listOfNotNull(companyCity.takeIf { it.isNotBlank() }, companyCountry.takeIf { it.isNotBlank() }).joinToString(", ").takeIf { it.isNotBlank() },
                    companyEmail.takeIf { it.isNotBlank() }
                )
                for (loc in locationList) {
                    pdfContext.canvas?.drawText(loc, leftMargin, pdfContext.y, textPaint.apply { isFakeBoldText = false })
                    pdfContext.y += 14f
                }
            }
            pdfContext.y += 6f
            
            val finalSubject = subjectLine.ifBlank {
                if (jobPosition.isNotBlank()) {
                    val typeSuffix = if (jobType.isNotBlank()) " [$jobType]" else ""
                    val refSuffix = if (jobRefNumber.isNotBlank()) " (Ref No: $jobRefNumber)" else ""
                    "Subject: Application for the position of $jobPosition$typeSuffix$refSuffix"
                } else "Subject: Application for Professional Employment"
            }
            
            if (finalSubject.isNotBlank()) {
                pdfContext.canvas?.drawText(finalSubject, leftMargin, pdfContext.y, textPaint.apply { isFakeBoldText = true })
                pdfContext.y += 22f
            }

            // 3. Write Core Body Paragraphs
            // Remove "*" characters from text and normalize double spaces
            val cleanedBody = bodyText.replace("*", "").replace("##", "")
            drawWrappedText(pdfContext, cleanedBody, leftMargin, textPaint.apply { isFakeBoldText = false })
            pdfContext.y += 30f

            // 4. Sign-off and name placeholder
            pdfContext.checkAndStartPageIfNeeded(60f)
            pdfContext.canvas?.drawText("Sincerely / Respectfully Yours,", leftMargin, pdfContext.y, textPaint)
            pdfContext.y += 35f
            
            // Sign line
            pdfContext.canvas?.drawLine(leftMargin, pdfContext.y, leftMargin + 150f, pdfContext.y, Paint().apply { color = android.graphics.Color.GRAY; strokeWidth = 0.5f })
            pdfContext.y += 14f
            pdfContext.canvas?.drawText(senderName, leftMargin, pdfContext.y, textPaint.apply { isFakeBoldText = true })

            // Finish up
            pdfContext.currentPage?.let { prevPage ->
                drawFooter(prevPage.canvas)
                pdfDocument.finishPage(prevPage)
            }
            pdfContext.currentPage = null

            // Save PDF locally
            val cacheDir = context.cacheDir
            val file = File(cacheDir, "Cover_Letter_${senderName.replace(" ", "_")}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()

            // Open intent for sharing
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Your Official Cover Letter"))

        } catch (e: Exception) {
            Toast.makeText(context, "Error creating cover letter PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
