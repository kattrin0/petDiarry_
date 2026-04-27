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

        // Связываем BottomNavigationView с NavController
        bottomNavigationView.setupWithNavController(navController)

        // Настраиваем ActionBar с NavController
        setupActionBarWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val label = destination.label
            if (label != null) {
                toolbar.title = label
            }
        }

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        observeAuthState()

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    private fun observeAuthState() {
        // Наблюдаем за состоянием аутентификации
        authViewModel.isAuthenticated.observe(this) { isAuthenticated ->
            if (isAuthenticated) {
                navigateToHome()
            } else {
                // Проверяем гостевой режим
                authViewModel.isGuest.observe(this) { isGuest ->
                    if (isGuest) {
                        navigateToHome()
                    } else {
                        navigateToAuth()
                    }
                }
            }
        }

        // Если гость уже залогинен при запуске
        authViewModel.isGuest.observe(this) { isGuest ->
            if (isGuest) {
                navigateToHome()
            }
        }
    }

    private fun navigateToHome() {
        // Показываем тулбар и нижнюю навигацию
        toolbar.visibility = android.view.View.VISIBLE
        bottomNavigationView.visibility = android.view.View.VISIBLE

        // Проверяем, не находимся ли мы уже на home
        if (navController.currentDestination?.id != R.id.homeFragment) {
            // Создаем NavOptions для очистки back stack
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.authChoiceFragment, true)
                .build()

            // Переходим на homeFragment
            navController.navigate(R.id.homeFragment, null, navOptions)
        }
    }

    private fun navigateToAuth() {
        // Скрываем тулбар и нижнюю навигацию
        toolbar.visibility = android.view.View.GONE
        bottomNavigationView.visibility = android.view.View.GONE

        // Проверяем, не находимся ли мы уже на auth
        if (navController.currentDestination?.id != R.id.authChoiceFragment) {
            // Создаем NavOptions для очистки back stack
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, true)
                .build()

            // Переходим на экран авторизации
            navController.navigate(R.id.authChoiceFragment, null, navOptions)
        }
    }


}