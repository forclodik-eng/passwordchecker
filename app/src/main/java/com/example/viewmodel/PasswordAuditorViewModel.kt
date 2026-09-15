package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.AttackSimulator
import com.example.engine.PasswordAuditorEngine
import com.example.engine.PasswordGenerator
import com.example.model.AttackMethod
import com.example.model.BruteForceProgress
import com.example.model.MethodResult
import com.example.model.PasswordAnalysis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class GenerationMode {
    UPGRADE,
    RANDOM,
    PASSPHRASE
}

class PasswordAuditorViewModel : ViewModel() {

    private val simulator = AttackSimulator(viewModelScope)

    private val _password = MutableStateFlow("P@ssw0rd2024")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isPasswordVisible = MutableStateFlow(true)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

    private val _analysis = MutableStateFlow(PasswordAuditorEngine.analyze("P@ssw0rd2024"))
    val analysis: StateFlow<PasswordAnalysis> = _analysis.asStateFlow()

    val methodResults: StateFlow<Map<AttackMethod, MethodResult>> = simulator.methodResults
    val bruteForceProgress: StateFlow<BruteForceProgress> = simulator.bruteForceProgress
    val isSimulating: StateFlow<Boolean> = simulator.isSimulating

    // Generator states
    private val _generatedPassword = MutableStateFlow("")
    val generatedPassword: StateFlow<String> = _generatedPassword.asStateFlow()

    private val _generatorLength = MutableStateFlow(16)
    val generatorLength: StateFlow<Int> = _generatorLength.asStateFlow()

    private val _includeUpper = MutableStateFlow(true)
    val includeUpper: StateFlow<Boolean> = _includeUpper.asStateFlow()

    private val _includeDigits = MutableStateFlow(true)
    val includeDigits: StateFlow<Boolean> = _includeDigits.asStateFlow()

    private val _includeSymbols = MutableStateFlow(true)
    val includeSymbols: StateFlow<Boolean> = _includeSymbols.asStateFlow()

    private val _generationMode = MutableStateFlow(GenerationMode.UPGRADE)
    val generationMode: StateFlow<GenerationMode> = _generationMode.asStateFlow()

    init {
        generateNewVariant()
    }

    fun onPasswordChanged(newPass: String) {
        _password.value = newPass
        _analysis.value = PasswordAuditorEngine.analyze(newPass)
        simulator.reset()
    }

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    fun runAttackSimulation() {
        simulator.startSimulation(_password.value)
    }

    fun stopAttackSimulation() {
        simulator.reset()
    }

    fun setGenerationMode(mode: GenerationMode) {
        _generationMode.value = mode
        generateNewVariant()
    }

    fun setGeneratorLength(length: Int) {
        _generatorLength.value = length
        if (_generationMode.value == GenerationMode.RANDOM) {
            generateNewVariant()
        }
    }

    fun toggleIncludeUpper() {
        _includeUpper.value = !_includeUpper.value
        generateNewVariant()
    }

    fun toggleIncludeDigits() {
        _includeDigits.value = !_includeDigits.value
        generateNewVariant()
    }

    fun toggleIncludeSymbols() {
        _includeSymbols.value = !_includeSymbols.value
        generateNewVariant()
    }

    fun generateNewVariant() {
        val result = when (_generationMode.value) {
            GenerationMode.UPGRADE -> PasswordGenerator.upgradePassword(_password.value)
            GenerationMode.RANDOM -> PasswordGenerator.generateRandom(
                length = _generatorLength.value,
                includeUpper = _includeUpper.value,
                includeLower = true,
                includeDigits = _includeDigits.value,
                includeSymbols = _includeSymbols.value
            )
            GenerationMode.PASSPHRASE -> PasswordGenerator.generatePassphrase(languageRu = true)
        }
        _generatedPassword.value = result
    }

    fun applyGeneratedPasswordToAuditor() {
        val gen = _generatedPassword.value
        if (gen.isNotEmpty()) {
            _password.value = gen
            _analysis.value = PasswordAuditorEngine.analyze(gen)
            simulator.startSimulation(gen)
        }
    }
}
