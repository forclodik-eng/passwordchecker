package com.example.engine

import com.example.model.AttackMethod
import com.example.model.BruteForceProgress
import com.example.model.MethodResult
import com.example.model.MethodStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.security.SecureRandom
import kotlin.math.pow
import kotlin.math.roundToLong

class AttackSimulator(private val coroutineScope: CoroutineScope) {

    private val random = SecureRandom()

    private val _methodResults = MutableStateFlow<Map<AttackMethod, MethodResult>>(emptyMap())
    val methodResults: StateFlow<Map<AttackMethod, MethodResult>> = _methodResults.asStateFlow()

    private val _bruteForceProgress = MutableStateFlow(BruteForceProgress())
    val bruteForceProgress: StateFlow<BruteForceProgress> = _bruteForceProgress.asStateFlow()

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private var simulationJob: Job? = null

    fun reset() {
        simulationJob?.cancel()
        simulationJob = null
        _isSimulating.value = false
        _methodResults.value = emptyMap()
        _bruteForceProgress.value = BruteForceProgress()
    }

    fun startSimulation(password: String) {
        simulationJob?.cancel()

        if (password.isEmpty()) {
            reset()
            return
        }

        val initialMethods = AttackMethod.values().associateWith { method ->
            MethodResult(method = method, status = MethodStatus.WAITING)
        }
        _methodResults.value = initialMethods
        _isSimulating.value = true

        // Calculate theoretical combinations
        val pool = calculatePoolSize(password)
        val len = password.length
        val combValue = pool.toDouble().pow(len.toDouble())
        val combFormatted = if (combValue > 1e12) {
            String.format("%.2e", combValue)
        } else {
            String.format("%,d", combValue.roundToLong())
        }

        _bruteForceProgress.value = BruteForceProgress(
            isRunning = true,
            totalAttempts = 0L,
            attemptsPerSecond = 0L,
            cracked = false,
            theoreticalCombinationsFormatted = combFormatted
        )

        simulationJob = coroutineScope.launch {
            try {
                // Launch parallel Brute Force worker
                val bruteForceJob = launch {
                    runParallelBruteForce(password, pool)
                }

                // Run Smart Attack pipeline (sequentially from easiest to hardest)
                runSmartAttackPipeline(password)

                // Wait a moment then wrap up brute force if still running
                delay(300)
                bruteForceJob.cancel()
                _bruteForceProgress.value = _bruteForceProgress.value.copy(isRunning = false)
                _isSimulating.value = false
            } catch (e: CancellationException) {
                _isSimulating.value = false
                _bruteForceProgress.value = _bruteForceProgress.value.copy(isRunning = false)
            }
        }
    }

    private suspend fun runSmartAttackPipeline(password: String) {
        val lowerPass = password.lowercase()
        val analysis = PasswordAuditorEngine.analyze(password)
        var hasBeenCracked = false

        for (method in AttackMethod.values().sortedBy { it.order }) {
            // Mark current method as RUNNING
            updateMethod(method, MethodStatus.RUNNING, "Анализ алгоритмов...", 0, 0)
            delay(400) // Visual animation cadence

            when (method) {
                AttackMethod.DICTIONARY -> {
                    val isCommon = PasswordAuditorEngine.COMMON_PASSWORDS.contains(lowerPass)
                    val containsCommon = PasswordAuditorEngine.COMMON_PASSWORDS.firstOrNull { it.length >= 4 && lowerPass.contains(it) }

                    if (isCommon) {
                        hasBeenCracked = true
                        updateMethod(
                            method,
                            MethodStatus.CRACKED,
                            "ВЗЛОМАН: Пароль найден в базе утекших паролей (Топ-1000)!",
                            timeMs = 45,
                            attempts = 142
                        )
                    } else if (containsCommon != null) {
                        updateMethod(
                            method,
                            MethodStatus.CRACKED,
                            "ВЗЛОМАН: Базовое слово '$containsCommon' быстро подобрано словарем!",
                            timeMs = 120,
                            attempts = 1840
                        )
                    } else {
                        updateMethod(
                            method,
                            MethodStatus.RESISTED,
                            "ЗАЩИЩЕН: Проверено 10 000+ популярных паролей, совпадений нет",
                            timeMs = 380,
                            attempts = 10000
                        )
                    }
                }

                AttackMethod.SEQUENCES -> {
                    val hasPattern = analysis.vulnerabilities.any { it.contains("последовательность") || it.contains("повторяющиеся") }
                    if (hasPattern) {
                        hasBeenCracked = true
                        updateMethod(
                            method,
                            MethodStatus.CRACKED,
                            "ВЗЛОМАН: Обнаружена клавиатурная цепочка или повторение символов!",
                            timeMs = 85,
                            attempts = 350
                        )
                    } else {
                        updateMethod(
                            method,
                            MethodStatus.RESISTED,
                            "ЗАЩИЩЕН: Клавиатурных дорожек (qwerty) и тривиальных повторов нет",
                            timeMs = 290,
                            attempts = 4500
                        )
                    }
                }

                AttackMethod.DATES_AND_PINS -> {
                    val hasDates = analysis.vulnerabilities.any { it.contains("года или даты") }
                    if (hasDates) {
                        hasBeenCracked = true
                        updateMethod(
                            method,
                            MethodStatus.CRACKED,
                            "ВЗЛОМАН: Пароль содержит шаблон года (19xx/20xx) или дату",
                            timeMs = 110,
                            attempts = 820
                        )
                    } else {
                        updateMethod(
                            method,
                            MethodStatus.RESISTED,
                            "ЗАЩИЩЕН: Шаблоны годов (1940–2035) и календарных дат не найдены",
                            timeMs = 310,
                            attempts = 6200
                        )
                    }
                }

                AttackMethod.LEET_SPEAK -> {
                    val normalized = PasswordAuditorEngine.normalizeLeet(lowerPass)
                    val isLeetCommon = normalized != lowerPass && PasswordAuditorEngine.COMMON_PASSWORDS.any { normalized.contains(it) }
                    if (isLeetCommon) {
                        hasBeenCracked = true
                        updateMethod(
                            method,
                            MethodStatus.CRACKED,
                            "ВЗЛОМАН: L33t-подстановка раскрыта (@ -> a, 0 -> o, 3 -> e)",
                            timeMs = 190,
                            attempts = 2400
                        )
                    } else {
                        updateMethod(
                            method,
                            MethodStatus.RESISTED,
                            "ЗАЩИЩЕН: Правила псевдозамен символов не привели к словарным корням",
                            timeMs = 350,
                            attempts = 15000
                        )
                    }
                }

                AttackMethod.MASK_ATTACK -> {
                    val isCorporateMask = Regex("^[A-ZА-Я][a-zа-я]+[0-9]{1,4}[^A-Za-zА-Яа-я0-9]?$").matches(password)
                    if (isCorporateMask) {
                        hasBeenCracked = true
                        updateMethod(
                            method,
                            MethodStatus.CRACKED,
                            "ВЗЛОМАН: Стандартная корпоративная маска (Заглавная + слово + цифры)",
                            timeMs = 240,
                            attempts = 8900
                        )
                    } else {
                        updateMethod(
                            method,
                            MethodStatus.RESISTED,
                            "ЗАЩИЩЕН: Не соответствует типовым эвристическим маскам смены пароля",
                            timeMs = 410,
                            attempts = 45000
                        )
                    }
                }

                AttackMethod.COMBINATORIC -> {
                    if (password.length <= 4) {
                        hasBeenCracked = true
                        updateMethod(
                            method,
                            MethodStatus.CRACKED,
                            "ВЗЛОМАН: Сверхмалая длина (≤ 4), полный перебор завершен!",
                            timeMs = 320,
                            attempts = 150000
                        )
                    } else if (password.length <= 6 && !analysis.hasSymbols && !analysis.hasUppercase) {
                        hasBeenCracked = true
                        updateMethod(
                            method,
                            MethodStatus.CRACKED,
                            "ВЗЛОМАН: Узкий алфавит без спецсимволов перебран за минуты",
                            timeMs = 450,
                            attempts = 850000
                        )
                    } else {
                        updateMethod(
                            method,
                            MethodStatus.RESISTED,
                            "ЗАЩИЩЕН: Комбинаторный взрыв. Потребуется: ${analysis.crackTimeGpu}",
                            timeMs = 500,
                            attempts = 1200000
                        )
                    }
                }
            }
            delay(150)
        }
    }

    private suspend fun runParallelBruteForce(targetPassword: String, poolSize: Int) {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()"
        var attempts = 0L
        val startTime = System.currentTimeMillis()
        val len = targetPassword.length.coerceAtLeast(1)

        while (coroutineScope.isActive) {
            // Generate quick random guesses
            val batchSize = 120
            var matched = false

            val guessSb = StringBuilder(len)
            for (i in 0 until len) {
                guessSb.append(chars[random.nextInt(chars.length)])
            }
            val currentGuess = guessSb.toString()

            attempts += batchSize

            if (currentGuess == targetPassword) {
                matched = true
            }

            val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000.0).coerceAtLeast(0.001)
            val speed = (attempts / elapsedSec).toLong()

            val maxCombinations = poolSize.toDouble().pow(len.toDouble())
            val fraction = (attempts / maxCombinations).toFloat().coerceIn(0f, 1f)

            _bruteForceProgress.value = _bruteForceProgress.value.copy(
                isRunning = true,
                currentGuess = currentGuess,
                totalAttempts = attempts,
                attemptsPerSecond = speed,
                progressFraction = fraction,
                cracked = matched
            )

            if (matched) break
            delay(50) // Update UI at ~20fps
        }
    }

    private fun updateMethod(
        method: AttackMethod,
        status: MethodStatus,
        details: String,
        timeMs: Long,
        attempts: Long
    ) {
        val current = _methodResults.value.toMutableMap()
        current[method] = MethodResult(
            method = method,
            status = status,
            details = details,
            timeMs = timeMs,
            attemptsTested = attempts
        )
        _methodResults.value = current
    }

    private fun calculatePoolSize(password: String): Int {
        var pool = 0
        if (password.any { it.isLowerCase() }) pool += 26
        if (password.any { it.isUpperCase() }) pool += 26
        if (password.any { it.isDigit() }) pool += 10
        if (password.any { !it.isLetterOrDigit() }) pool += 33
        return if (pool == 0) 10 else pool
    }
}
