package com.techexactly.eventmanager.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.techexactly.eventmanager.R
import com.techexactly.eventmanager.databinding.ActivityMainBinding
import com.techexactly.eventmanager.di.ServiceLocator
import com.techexactly.eventmanager.notification.FcmTokenSaver
import com.techexactly.eventmanager.ui.auth.LoginActivity
import com.techexactly.eventmanager.util.ThemeHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* optional feature */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ServiceLocator.authRepository.isLoggedIn) {
            goToLogin()
            return
        }

        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val appBar = findViewById<View>(R.id.app_bar)

        ViewCompat.setOnApplyWindowInsetsListener(appBar) { view, insets ->

            val statusBarInsets =
                insets.getInsets(WindowInsetsCompat.Type.statusBars())

            view.setPadding(
                view.paddingLeft,
                statusBarInsets.top,
                view.paddingRight,
                view.paddingBottom
            )

            insets
        }
        setSupportActionBar(binding.toolbar)

        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
        val appBarConfig = AppBarConfiguration(setOf(R.id.eventListFragment, R.id.dashboardFragment))
        setupActionBarWithNavController(navController, appBarConfig)
        binding.bottomNav.setupWithNavController(navController)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        FcmTokenSaver.saveCurrentToken()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_dark_mode -> { ThemeHelper.toggle(this); true }
        R.id.action_logout -> {
            ServiceLocator.authRepository.logout()
            goToLogin()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }
}
