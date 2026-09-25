package com.techexactly.eventmanager.ui.auth

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.techexactly.eventmanager.R
import com.techexactly.eventmanager.databinding.ActivityForgotPasswordBinding
import com.techexactly.eventmanager.di.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: AuthViewModel by viewModels { ViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSend.setOnClickListener { viewModel.resetPassword(binding.etEmail.text.toString()) }
        binding.tvBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.state.collect(::render) }
        }
    }

    private fun render(state: AuthUiState) {
        binding.progress.visibility = if (state.loading) View.VISIBLE else View.GONE
        binding.btnSend.isEnabled = !state.loading
        binding.tilEmail.error = state.emailError

        state.errorMessage?.let {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            viewModel.onEventHandled()
        }
        if (state.success == AuthSuccess.RESET_EMAIL_SENT) {
            viewModel.onEventHandled()
            Snackbar.make(binding.root, R.string.reset_email_sent, Snackbar.LENGTH_LONG).show()
        }
    }
}
