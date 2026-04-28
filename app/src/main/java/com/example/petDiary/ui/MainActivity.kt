package com.example.petDiary.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.petDiary.R
import com.example.petDiary.ui.viewmodel.AuthViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var toolbar: Toolbar
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))

        // Инициализируем NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView2) as NavHostFragment
        navController = navHostFragment.navController

        // корневые фрагменты
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.calendarFragment,
                R.id.mapFragment,
                R.id.profileFragment
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        bottomNavigationView.setupWithNavController(navController)

        toolbar.visibility = android.view.View.GONE
        bottomNavigationView.visibility = android.view.View.GONE

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val label = destination.label
            if (label != null && destination.id != R.id.authChoiceFragment && destination.id != R.id.loginFragment) {
                toolbar.title = label
            }
        }

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        observeAuthState()

        checkInitialAuthState()
    }

    private fun checkInitialAuthState() {
        if (authViewModel.isAuthenticated.value == true || authViewModel.isGuest.value == true) {
            navigateToHome()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                showSettingsDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSettingsDialog() {
        val settingsDialog = SettingsDialog()
        settingsDialog.show(supportFragmentManager, SettingsDialog.TAG)
    }

    private fun observeAuthState() {
        authViewModel.isAuthenticated.observe(this) { isAuthenticated ->
            if (isAuthenticated) {
                navigateToHome()
            }
        }

        authViewModel.isGuest.observe(this) { isGuest ->
            if (isGuest) {
                navigateToHome()
            }
        }

        authViewModel.onSignOut.observe(this) { shouldSignOut ->
            if (shouldSignOut) {
                navigateToAuth()
            }
        }
    }

    private fun navigateToHome() {
        toolbar.visibility = android.view.View.VISIBLE
        bottomNavigationView.visibility = android.view.View.VISIBLE

        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val currentDestId = navController.currentDestination?.id
        val isOnMainScreen = currentDestId == R.id.homeFragment ||
                currentDestId == R.id.calendarFragment ||
                currentDestId == R.id.mapFragment ||
                currentDestId == R.id.profileFragment

        if (!isOnMainScreen) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.authChoiceFragment, true)
                .setLaunchSingleTop(true)
                .build()

            navController.navigate(R.id.homeFragment, null, navOptions)
        }
    }

    private fun navigateToAuth() {
        toolbar.visibility = android.view.View.GONE
        bottomNavigationView.visibility = android.view.View.GONE

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = null

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.homeFragment, true)
            .build()

        navController.navigate(R.id.authChoiceFragment, null, navOptions)
    }
}