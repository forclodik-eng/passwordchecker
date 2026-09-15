package com.example

import com.example.engine.PasswordAuditorEngine
import com.example.engine.PasswordGenerator
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testWeakCommonPasswordScore() {
    val result = PasswordAuditorEngine.analyze("123456")
    assertTrue(result.score <= 1.0f)
    assertTrue(result.vulnerabilities.isNotEmpty())
  }

  @Test
  fun testStrongPasswordScore() {
    val strong = "K9#mQ!8z\$vL2^xR"
    val result = PasswordAuditorEngine.analyze(strong)
    assertTrue("Expected high score for $strong, got ${result.score}", result.score >= 8.5f)
  }

  @Test
  fun testGeneratedPasswordHasHighQuality() {
    val randomPass = PasswordGenerator.generateRandom(16)
    val analysis = PasswordAuditorEngine.analyze(randomPass)
    assertTrue("Generated pass should be strong: ${analysis.score}", analysis.score >= 8.0f)

    val upgraded = PasswordGenerator.upgradePassword("pass123")
    val upgradedAnalysis = PasswordAuditorEngine.analyze(upgraded)
    assertTrue(upgradedAnalysis.score >= 7.0f)
  }
}

