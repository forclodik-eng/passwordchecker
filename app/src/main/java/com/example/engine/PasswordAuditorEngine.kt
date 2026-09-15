package com.example.engine

import com.example.model.PasswordAnalysis
import com.example.model.RecommendationItem
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

object PasswordAuditorEngine {

    val COMMON_PASSWORDS = setOf(
        "123456", "password", "12345678", "qwerty", "123456789", "12345", "1234", "111111",
        "1234567", "dragon", "welcome", "ninja", "monkey", "sunshine", "football", "princess",
        "solo", "shadow", "master", "michael", "jordan", "superman", "harley", "batman",
        "trustno1", "admin", "root", "guest", "default", "parol", "letmein", "iloveyou",
        "pass123", "password123", "123123", "654321", "secret", "computer", "internet",
        "freedom", "starwars", "pokemon", "liverpool", "arsenal", "chelsea", "test", "testing",
        "hello", "matrix", "access", "love", "family", "orange", "yellow", "coffee",
        "йцукен", "пароль", "привет", "любовь", "солнце", "админ", "1234567890", "000000"
    )

    private val KEYBOARD_SEQUENCES = listOf(
        "qwertyuiop", "asdfghjkl", "zxcvbnm",
        "1234567890", "0987654321",
        "йцукенгшщзхъ", "фывапролджэ", "ячсмитьбю",
        "abcdefghijklmnopqrstuvwxyz"
    )

    fun analyze(password: String): PasswordAnalysis {
        if (password.isEmpty()) {
            return PasswordAnalysis(
                password = "",
                score = 0.0f,
                scoreTier = "Введите пароль",
                entropyBits = 0.0,
                length = 0,
                hasLowercase = false,
                hasUppercase = false,
                hasDigits = false,
                hasSymbols = false,
                crackTimeOnline = "0 сек",
                crackTimeCpu = "0 сек",
                crackTimeGpu = "0 сек",
                crackTimeSupercomputer = "0 сек",
                recommendations = getInitialRecommendations(),
                vulnerabilities = emptyList()
            )
        }

        val length = password.length
        val hasLower = password.any { it.isLowerCase() }
        val hasUpper = password.any { it.isUpperCase() }
        val hasDigits = password.any { it.isDigit() }
        val hasSymbols = password.any { !it.isLetterOrDigit() }

        var poolSize = 0
        if (hasLower) poolSize += 26
        if (hasUpper) poolSize += 26
        if (hasDigits) poolSize += 10
        if (hasSymbols) poolSize += 33
        if (password.any { it in 'а'..'я' || it in 'А'..'Я' }) poolSize += 33
        if (poolSize == 0) poolSize = 10

        var theoreticalEntropy = length * log2(poolSize.toDouble())

        // Detect vulnerabilities
        val vulnerabilities = mutableListOf<String>()
        var scoreDeduction = 0.0f

        val lowerPass = password.lowercase()

        // 1. Direct dictionary match
        if (COMMON_PASSWORDS.contains(lowerPass)) {
            vulnerabilities.add("Пароль находится в списке 1000 самых утекших паролей мира")
            scoreDeduction += 6.5f
            theoreticalEntropy = theoreticalEntropy.coerceAtMost(10.0)
        }

        // 2. Contains common password substring
        val matchedCommonSub = COMMON_PASSWORDS.firstOrNull { it.length >= 4 && lowerPass.contains(it) }
        if (matchedCommonSub != null && !COMMON_PASSWORDS.contains(lowerPass)) {
            vulnerabilities.add("Содержит распространенное слово: '$matchedCommonSub'")
            scoreDeduction += 2.0f
            theoreticalEntropy = (theoreticalEntropy * 0.7).coerceAtLeast(15.0)
        }

        // 3. Repeated characters (e.g. aaaa, 1111)
        if (hasRepeatingChars(password)) {
            vulnerabilities.add("Имеет повторяющиеся подряд символы (например: 'aaa' или '111')")
            scoreDeduction += 1.5f
            theoreticalEntropy *= 0.8
        }

        // 4. Keyboard walks & sequences
        if (hasSequentialPatterns(lowerPass)) {
            vulnerabilities.add("Содержит последовательность клавиш или цифр (qwerty, 12345 и т.д.)")
            scoreDeduction += 2.0f
            theoreticalEntropy *= 0.75
        }

        // 5. Date / year patterns (e.g. 1985, 2024, DDMMYYYY)
        if (hasDateOrYearPattern(password)) {
            vulnerabilities.add("Обнаружен шаблон года или даты рождения (например, 1990–2026)")
            scoreDeduction += 1.8f
            theoreticalEntropy *= 0.8
        }

        // 6. L33t speak substitution match
        val leetNormalized = normalizeLeet(lowerPass)
        if (leetNormalized != lowerPass && COMMON_PASSWORDS.any { leetNormalized.contains(it) }) {
            vulnerabilities.add("L33t-подстановки (@ -> a, 0 -> o) легко восстанавливаются хакерскими правилами")
            scoreDeduction += 1.8f
            theoreticalEntropy *= 0.85
        }

        // 7. Corporate mask pattern: [Capital][lowercase][digits][symbol?]
        val isCorporateMask = Regex("^[A-ZА-Я][a-zа-я]+[0-9]{1,4}[^A-Za-zА-Яа-я0-9]?$").matches(password)
        if (isCorporateMask) {
            vulnerabilities.add("Типовая предсказуемая маска: Заглавная буква + слово + цифры в конце")
            scoreDeduction += 1.5f
            theoreticalEntropy *= 0.85
        }

        // Score Calculation (0.0 to 10.0 scale)
        var rawScore = 0.0f

        // Length contribution (up to 4.5 points)
        rawScore += when {
            length < 6 -> (length * 0.3f)
            length in 6..8 -> 1.8f + (length - 6) * 0.5f // up to 2.8
            length in 9..12 -> 2.8f + (length - 8) * 0.4f // up to 4.4
            length in 13..16 -> 4.4f + (length - 12) * 0.25f // up to 5.4
            else -> 5.4f + ((length - 16) * 0.15f).coerceAtMost(1.6f) // up to 7.0
        }

        // Character diversity contribution (up to 3.5 points)
        var diversityCount = 0
        if (hasLower) diversityCount++
        if (hasUpper) diversityCount++
        if (hasDigits) diversityCount++
        if (hasSymbols) diversityCount++

        rawScore += when (diversityCount) {
            1 -> 0.4f
            2 -> 1.2f
            3 -> 2.4f
            4 -> 3.5f
            else -> 0.0f
        }

        // Entropy bonus (up to 1.5 points)
        if (theoreticalEntropy > 70) rawScore += 1.5f
        else if (theoreticalEntropy > 50) rawScore += 1.0f
        else if (theoreticalEntropy > 35) rawScore += 0.5f

        // Deduct penalties
        rawScore -= scoreDeduction

        // Clamp to 0.0 - 10.0
        val finalScore = ((rawScore.coerceIn(0.1f, 10.0f) * 10).roundToInt()) / 10.0f

        val scoreTier = when {
            finalScore < 2.5f -> "Критически слабый ❌"
            finalScore < 5.0f -> "Слабый пароль ⚠️"
            finalScore < 7.0f -> "Средняя стойкость 🟡"
            finalScore < 9.0f -> "Высокая стойкость 🛡️"
            else -> "Максимальная защита 💎"
        }

        // Calculate crack times based on theoretical combinations
        val combinations = (poolSize.toDouble().pow(length)).coerceAtLeast(1.0)
        val crackTimes = calculateCrackTimes(combinations, theoreticalEntropy)

        val recommendations = generateRecommendations(
            password = password,
            length = length,
            hasLower = hasLower,
            hasUpper = hasUpper,
            hasDigits = hasDigits,
            hasSymbols = hasSymbols,
            vulnerabilities = vulnerabilities
        )

        return PasswordAnalysis(
            password = password,
            score = finalScore,
            scoreTier = scoreTier,
            entropyBits = ((theoreticalEntropy * 10).roundToInt()) / 10.0,
            length = length,
            hasLowercase = hasLower,
            hasUppercase = hasUpper,
            hasDigits = hasDigits,
            hasSymbols = hasSymbols,
            crackTimeOnline = crackTimes[0],
            crackTimeCpu = crackTimes[1],
            crackTimeGpu = crackTimes[2],
            crackTimeSupercomputer = crackTimes[3],
            recommendations = recommendations,
            vulnerabilities = vulnerabilities
        )
    }

    private fun hasRepeatingChars(pass: String): Boolean {
        var count = 1
        for (i in 1 until pass.length) {
            if (pass[i] == pass[i - 1]) {
                count++
                if (count >= 3) return true
            } else {
                count = 1
            }
        }
        return false
    }

    private fun hasSequentialPatterns(lower: String): Boolean {
        if (lower.length < 3) return false
        for (seq in KEYBOARD_SEQUENCES) {
            for (i in 0..seq.length - 3) {
                val sub = seq.substring(i, i + 3)
                if (lower.contains(sub)) return true
                if (lower.contains(sub.reversed())) return true
            }
        }
        return false
    }

    private fun hasDateOrYearPattern(pass: String): Boolean {
        // Matches 4-digit years between 1940 and 2035
        val yearRegex = Regex("(19[4-9][0-9]|20[0-3][0-9])")
        if (yearRegex.containsMatchIn(pass)) return true

        // Matches DDMM or MMDD like 0112, 3112, etc.
        val dateRegex = Regex("(0[1-9]|[12][0-9]|3[01])(0[1-9]|1[0-2])")
        if (dateRegex.containsMatchIn(pass) && pass.length in 4..8) return true

        return false
    }

    fun normalizeLeet(text: String): String {
        return text
            .replace('@', 'a')
            .replace('4', 'a')
            .replace('0', 'o')
            .replace('3', 'e')
            .replace('1', 'i')
            .replace('!', 'i')
            .replace('$', 's')
            .replace('5', 's')
            .replace('7', 't')
            .replace('+', 't')
    }

    private fun calculateCrackTimes(combinations: Double, entropy: Double): List<String> {
        // Rates:
        // Online rate limited: 100 attempts / sec
        // Standard PC CPU: 10,000,000 hashes / sec
        // GPU Rig: 100,000,000,000 hashes / sec (100 GH/s)
        // Supercomputer cluster: 100,000,000,000,000 hashes / sec (100 TH/s)
        val adjustedComb = if (entropy < 20) 1000.0 else combinations.coerceAtMost(1e30)

        val secOnline = adjustedComb / 100.0
        val secCpu = adjustedComb / 10_000_000.0
        val secGpu = adjustedComb / 100_000_000_000.0
        val secSuper = adjustedComb / 100_000_000_000_000.0

        return listOf(
            formatDuration(secOnline),
            formatDuration(secCpu),
            formatDuration(secGpu),
            formatDuration(secSuper)
        )
    }

    private fun formatDuration(seconds: Double): String {
        return when {
            seconds < 0.001 -> "Мгновенно (< 1 мс)"
            seconds < 1.0 -> "Менее 1 сек"
            seconds < 60.0 -> "${seconds.roundToInt()} сек"
            seconds < 3600.0 -> "${(seconds / 60).roundToInt()} мин"
            seconds < 86400.0 -> "${(seconds / 3600).roundToInt()} ч"
            seconds < 31536000.0 -> "${(seconds / 86400).roundToInt()} дн"
            seconds < 31536000.0 * 100 -> "${(seconds / 31536000.0).roundToInt()} лет"
            seconds < 31536000.0 * 1_000_000 -> "${((seconds / 31536000.0) / 1000).roundToInt()} тыс. лет"
            seconds < 31536000.0 * 1_000_000_000 -> "${((seconds / 31536000.0) / 1_000_000).roundToInt()} млн лет"
            else -> "> 1 млрд лет (вечность)"
        }
    }

    private fun generateRecommendations(
        password: String,
        length: Int,
        hasLower: Boolean,
        hasUpper: Boolean,
        hasDigits: Boolean,
        hasSymbols: Boolean,
        vulnerabilities: List<String>
    ): List<RecommendationItem> {
        val list = mutableListOf<RecommendationItem>()

        // 1. Length recommendation
        list.add(
            RecommendationItem(
                id = "rec_length",
                title = "Длина не менее 12-16 символов",
                description = "Каждый дополнительный символ увеличивает пространство перебора экспоненциально.",
                scoreImpact = if (length >= 14) "+2.5" else if (length >= 12) "+1.8" else "+2.5 (требуется)",
                isSatisfied = length >= 14
            )
        )

        // 2. Character diversity
        val hasAllTypes = hasLower && hasUpper && hasDigits && hasSymbols
        list.add(
            RecommendationItem(
                id = "rec_diversity",
                title = "Смешение 4 типов символов (A-Z, a-z, 0-9, !@#)",
                description = "Комбинируйте прописные, строчные буквы, цифры и спецзнаки для максимального пула.",
                scoreImpact = "+2.0",
                isSatisfied = hasAllTypes
            )
        )

        // 3. Absence of common words
        val hasCommonWord = vulnerabilities.any { it.contains("утекших") || it.contains("распространенное") }
        list.add(
            RecommendationItem(
                id = "rec_no_dictionary",
                title = "Исключить словарные слова и имена",
                description = "Хакерские словари проверяют миллионы популярных слов и фраз в первую очередь.",
                scoreImpact = "+2.2",
                isSatisfied = !hasCommonWord
            )
        )

        // 4. No predictable sequences
        val hasSequences = vulnerabilities.any { it.contains("последовательность") || it.contains("повторяющиеся") }
        list.add(
            RecommendationItem(
                id = "rec_no_sequences",
                title = "Избегать клавиатурных дорожек и повторов",
                description = "Паттерны вроде 'qwerty', '12345', 'aaa' мгновенно отсекаются умными алгоритмами.",
                scoreImpact = "+1.5",
                isSatisfied = !hasSequences
            )
        )

        // 5. No personal dates or years
        val hasDates = vulnerabilities.any { it.contains("года или даты") }
        list.add(
            RecommendationItem(
                id = "rec_no_dates",
                title = "Убрать памятные года и даты",
                description = "Года (1990-2026) и дни рождения проверяются автоматическими масками за миллисекунды.",
                scoreImpact = "+1.8",
                isSatisfied = !hasDates
            )
        )

        // 6. Passphrase recommendation
        list.add(
            RecommendationItem(
                id = "rec_passphrase",
                title = "Используйте метод парольной фразы (Passphrase)",
                description = "Например: 4 случайных несвязанных слова с дефисами легче запомнить и практически невозможно подобрать.",
                scoreImpact = "+2.0",
                isSatisfied = length >= 18 && !hasCommonWord
            )
        )

        return list
    }

    private fun getInitialRecommendations(): List<RecommendationItem> {
        return listOf(
            RecommendationItem(
                id = "rec_length",
                title = "Длина не менее 12-16 символов",
                description = "Экспоненциально усложняет математический перебор.",
                scoreImpact = "+2.5",
                isSatisfied = false
            ),
            RecommendationItem(
                id = "rec_diversity",
                title = "Комбинация A-Z, a-z, 0-9 и спецсимволов",
                description = "Расширяет мощность алфавита до 94+ символов на позицию.",
                scoreImpact = "+2.0",
                isSatisfied = false
            ),
            RecommendationItem(
                id = "rec_no_dictionary",
                title = "Отсутствие словарных слов",
                description = "Исключает успешность словарных rainbow-table атак.",
                scoreImpact = "+2.2",
                isSatisfied = false
            )
        )
    }
}
