package com.example.engine

import java.security.SecureRandom
import kotlin.random.asKotlinRandom

object PasswordGenerator {

    private val random = SecureRandom().asKotlinRandom()

    private val LOWERCASE = "abcdefghjkmnpqrstuvwxyz"
    private val UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    private val DIGITS = "23456789"
    private val SYMBOLS = "!@#$%^&*()-_=+[]{}<>?"

    private val ALL_LOWER = "abcdefghijklmnopqrstuvwxyz"
    private val ALL_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val ALL_DIGITS = "0123456789"

    private val RU_WORDS = listOf(
        "маяк", "сокол", "орбита", "квант", "вулкан", "тайга", "гранит", "шторм",
        "каскад", "вектор", "парус", "изумруд", "искра", "рассвет", "базальт", "атлас",
        "модуль", "компас", "вихрь", "пульсар", "север", "сапфир", "циклон", "магнит",
        "титан", "зеркало", "аметист", "радар", "галактика", "нептун", "созвездие"
    )

    private val EN_WORDS = listOf(
        "galaxy", "shield", "falcon", "matrix", "beacon", "aurora", "timber", "harbor",
        "plasma", "cobalt", "cipher", "summit", "vortex", "nebula", "zenith", "crystal",
        "anchor", "meteor", "canyon", "quartz", "shadow", "rocket", "forest", "arctic"
    )

    fun generateRandom(
        length: Int = 16,
        includeUpper: Boolean = true,
        includeLower: Boolean = true,
        includeDigits: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        val charPool = StringBuilder()
        val guaranteedChars = mutableListOf<Char>()

        if (includeLower) {
            charPool.append(LOWERCASE)
            guaranteedChars.add(LOWERCASE[random.nextInt(LOWERCASE.length)])
        }
        if (includeUpper) {
            charPool.append(UPPERCASE)
            guaranteedChars.add(UPPERCASE[random.nextInt(UPPERCASE.length)])
        }
        if (includeDigits) {
            charPool.append(DIGITS)
            guaranteedChars.add(DIGITS[random.nextInt(DIGITS.length)])
        }
        if (includeSymbols) {
            charPool.append(SYMBOLS)
            guaranteedChars.add(SYMBOLS[random.nextInt(SYMBOLS.length)])
        }

        if (charPool.isEmpty()) {
            charPool.append(LOWERCASE).append(DIGITS)
        }

        val poolStr = charPool.toString()
        val result = mutableListOf<Char>()
        result.addAll(guaranteedChars)

        while (result.size < length) {
            result.add(poolStr[random.nextInt(poolStr.length)])
        }

        result.shuffle(random)
        return result.joinToString("")
    }

    fun generatePassphrase(languageRu: Boolean = true): String {
        val wordPool = if (languageRu) RU_WORDS else EN_WORDS
        val chosenWords = mutableListOf<String>()

        while (chosenWords.size < 4) {
            val word = wordPool[random.nextInt(wordPool.size)]
            if (!chosenWords.contains(word)) {
                chosenWords.add(word)
            }
        }

        val separator = listOf("-", "_", ".").random(random)
        val saltNumber = random.nextInt(90) + 10 // 10..99
        val symbol = listOf("!", "#", "$", "?").random(random)

        // Capitalize first letter of one word
        val capitalizedIndex = random.nextInt(chosenWords.size)
        chosenWords[capitalizedIndex] = chosenWords[capitalizedIndex].replaceFirstChar { it.uppercase() }

        return chosenWords.joinToString(separator) + separator + saltNumber + symbol
    }

    fun upgradePassword(basePassword: String): String {
        if (basePassword.isBlank()) {
            return generateRandom(length = 16)
        }

        // Clean base and inject entropy
        val cleanBase = basePassword.trim()
        val builder = StringBuilder()

        // Distribute characters and inject random symbols & numbers in unpredictable spots
        for (i in cleanBase.indices) {
            val ch = cleanBase[i]
            builder.append(ch)

            // Inject non-trivial symbol at 1/3 and 2/3 of string
            if (i == cleanBase.length / 3) {
                builder.append(SYMBOLS[random.nextInt(SYMBOLS.length)])
                builder.append(DIGITS[random.nextInt(DIGITS.length)])
            } else if (i == (2 * cleanBase.length) / 3) {
                builder.append(UPPERCASE[random.nextInt(UPPERCASE.length)])
                builder.append(SYMBOLS[random.nextInt(SYMBOLS.length)])
            }
        }

        // Ensure minimum 16 length
        while (builder.length < 16) {
            val set = listOf(UPPERCASE, LOWERCASE, DIGITS, SYMBOLS).random(random)
            builder.append(set[random.nextInt(set.length)])
        }

        return builder.toString()
    }
}
