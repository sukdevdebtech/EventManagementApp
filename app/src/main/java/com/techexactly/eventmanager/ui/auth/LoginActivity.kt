package com.techexactly.eventmanager.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.techexactly.eventmanager.databinding.ActivityLoginBinding
import com.techexactly.eventmanager.di.ViewModelFactory
import com.techexactly.eventmanager.ui.MainActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels { ViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Persisted session -> skip the login screen.

        if (viewModel.isLoggedIn) {
            openMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            viewModel.login(binding.etEmail.text.toString(), binding.etPassword.text.toString())
        }
        binding.tvForgot.setOnClickListener { startActivity(Intent(this, ForgotPasswordActivity::class.java)) }
        binding.tvRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.state.collect(::render) }
        }
    }

    private fun render(state: AuthUiState) {
        binding.progress.visibility = if (state.loading) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnLogin.isEnabled = !state.loading
        binding.tilEmail.error = state.emailError
        binding.tilPassword.error = state.passwordError

        state.errorMessage?.let {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            viewModel.onEventHandled()
        }
        if (state.success == AuthSuccess.LOGIN) {
            viewModel.onEventHandled()
            openMain()
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
