package com.example.data

object SmartFormatEngine {

    /**
     * Fixes spacing around punctuation (commas, periods, semi-colons, colons).
     * Ensures exactly one space after punctuation and NO spaces before it.
     */
    fun fixPunctuationSpacing(text: String): String {
        if (text.isBlank()) return ""
        var temp = text.trim()
        
        // Remove spaces before punctuation
        temp = temp.replace(Regex("\\s+([.,;:!?])"), "$1")
        
        // Ensure a space after punctuation unless followed by another punctuation or numbers
        temp = temp.replace(Regex("([.,;:!?])(?=[a-zA-Z])"), "$1 ")
        
        // Remove duplicate spaces
        temp = temp.replace(Regex("\\s+"), " ")
        
        return temp.trim()
    }

    /**
     * Converts a title or position heading into Title Case.
     * Capitalizes major words, keeps prepositions/articles lowercase.
     */
    fun formatTitle(title: String): String {
        if (title.isBlank()) return ""
        val minorWords = setOf(
            "a", "an", "the", "and", "but", "or", "for", "nor", "on", "in", "at", "to", 
            "by", "of", "with", "from", "into", "onto", "at", "as", "via"
        )
        
        val cleaned = fixPunctuationSpacing(title)
        val words = cleaned.split(" ")
        
        val formattedWords = words.mapIndexed { index, word ->
            val cleanWord = word.trim()
            if (cleanWord.isEmpty()) return@mapIndexed ""
            
            // Handle lowercase for minor words, but always capitalize the first and last word
            val lowerVersion = cleanWord.lowercase()
            if (index > 0 && index < words.size - 1 && minorWords.contains(lowerVersion)) {
                lowerVersion
            } else {
                // If it's an acronym (like "IT", "CEO", "ATS", "USA", "EU"), keep it uppercase
                if (cleanWord.length in 2..4 && cleanWord.all { it.isUpperCase() }) {
                    cleanWord
                } else if (cleanWord.length in 2..4 && cleanWord.uppercase() in setOf("CEO", "CTO", "CFO", "COO", "IT", "ATS", "USA", "UK", "EU", "PHP", "AWS")) {
                    cleanWord.uppercase()
                } else {
                    // Standard Title Capitalization
                    cleanWord.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }
        }
        
        return formattedWords.filter { it.isNotEmpty() }.joinToString(" ")
    }

    /**
     * Formats paragraphs, correcting sentence capitalization, spacing, and minor common mistakes.
     */
    fun formatSentenceParagraphs(text: String): String {
        if (text.isBlank()) return ""
        
        val lineNormalized = text.replace(Regex("\r\n?"), "\n")
        val lines = lineNormalized.split("\n")
        
        val formattedLines = lines.map { line ->
            if (line.isBlank()) return@map ""
            
            val cleaned = fixPunctuationSpacing(line)
            // Split line by sentence terminators
            val sentences = cleaned.split(Regex("(?<=[.!?])\\s+"))
            
            val formattedSentences = sentences.map { sentence ->
                val s = sentence.trim()
                if (s.isEmpty()) return@map ""
                
                // Capitalize first letter of the sentence
                var outcome = s.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                
                // Capitalize standard singular personal pronoun "I"
                outcome = outcome.replace(Regex("\\bi\\b"), "I")
                outcome = outcome.replace(Regex("\\bi'm\\b"), "I'm")
                outcome = outcome.replace(Regex("\\bi've\\b"), "I've")
                outcome = outcome.replace(Regex("\\bi'll\\b"), "I'll")
                outcome = outcome.replace(Regex("\\bi'd\\b"), "I'd")
                
                // Capitalize common countries or brands for elevated aesthetic
                val properNouns = setOf(
                    "google", "apple", "microsoft", "amazon", "netflix", "meta", "stripe", "spotify",
                    "nepal", "dubai", "london", "tokyo", "canada", "america", "usa", "europe", "europass", "germany", "france"
                )
                properNouns.forEach { noun ->
                    outcome = outcome.replace(Regex("\\b$noun\\b", RegexOption.IGNORE_CASE)) { match ->
                        match.value.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    }
                }
                
                outcome
            }
            
            formattedSentences.filter { it.isNotEmpty() }.joinToString(" ")
        }
        
        return formattedLines.joinToString("\n")
    }
}
