package com.techexactly.eventmanager.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.techexactly.eventmanager.databinding.ActivityRegisterBinding
import com.techexactly.eventmanager.di.ViewModelFactory
import com.techexactly.eventmanager.ui.MainActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels { ViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            viewModel.register(
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString(),
                binding.etConfirm.text.toString()
            )
        }
        binding.tvLogin.setOnClickListener { finish() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.state.collect(::render) }
        }
    }

    private fun render(state: AuthUiState) {
        binding.progress.visibility = if (state.loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !state.loading
        binding.tilEmail.error = state.emailError
        binding.tilPassword.error = state.passwordError
        binding.tilConfirm.error = state.confirmError

        state.errorMessage?.let {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            viewModel.onEventHandled()
        }
        if (state.success == AuthSuccess.REGISTER) {
            viewModel.onEventHandled()
            // Firebase signs the user in automatically after sign-up.
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }
}
