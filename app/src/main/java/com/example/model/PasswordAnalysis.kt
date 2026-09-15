package com.example.model

enum class AttackMethod(
    val title: String,
    val description: String,
    val order: Int
) {
    DICTIONARY(
        title = "1. Словарная атака (Dictionary)",
        description = "Проверка по базе самых частых и утекших паролей мира",
        order = 1
    ),
    SEQUENCES(
        title = "2. Последовательности и паттерны",
        description = "Поиск клавиатурных путей (qwerty, asdf), повторов и цифр подряд",
        order = 2
    ),
    DATES_AND_PINS(
        title = "3. Анализ дат, годов и PIN-кодов",
        description = "Проверка годов (1970–2030), дат рождения и шаблонных окончаний",
        order = 3
    ),
    LEET_SPEAK(
        title = "4. L33t-Speak и подстановки",
        description = "Анализ замен символов (@ вместо a, 0 вместо o, 3 вместо e, 1 вместо i)",
        order = 4
    ),
    MASK_ATTACK(
        title = "5. Масочная атака (Шаблоны)",
        description = "Проверка корпоративных масок (Заглавная буква + слово + цифры + знак)",
        order = 5
    ),
    COMBINATORIC(
        title = "6. Комбинаторный гибридный взлом",
        description = "Эвристический взлом по сокращенному пространству символов",
        order = 6
    )
}

enum class MethodStatus {
    WAITING,
    RUNNING,
    CRACKED,
    RESISTED
}

data class MethodResult(
    val method: AttackMethod,
    val status: MethodStatus = MethodStatus.WAITING,
    val details: String = "",
    val timeMs: Long = 0L,
    val attemptsTested: Long = 0L
)

data class BruteForceProgress(
    val isRunning: Boolean = false,
    val currentGuess: String = "",
    val totalAttempts: Long = 0L,
    val attemptsPerSecond: Long = 0L,
    val progressFraction: Float = 0f,
    val cracked: Boolean = false,
    val theoreticalCombinationsFormatted: String = ""
)

data class RecommendationItem(
    val id: String,
    val title: String,
    val description: String,
    val scoreImpact: String,
    val isSatisfied: Boolean
)

data class PasswordAnalysis(
    val password: String,
    val score: Float, // 0.0 to 10.0
    val scoreTier: String,
    val entropyBits: Double,
    val length: Int,
    val hasLowercase: Boolean,
    val hasUppercase: Boolean,
    val hasDigits: Boolean,
    val hasSymbols: Boolean,
    val crackTimeOnline: String,
    val crackTimeCpu: String,
    val crackTimeGpu: String,
    val crackTimeSupercomputer: String,
    val recommendations: List<RecommendationItem>,
    val vulnerabilities: List<String>
)
