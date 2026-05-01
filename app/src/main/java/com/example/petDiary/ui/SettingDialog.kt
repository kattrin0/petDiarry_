package com.example.petDiary.ui

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.petDiary.R
import com.example.petDiary.ui.viewmodel.AuthViewModel

class SettingsDialog : DialogFragment() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]

        val builder = AlertDialog.Builder(requireContext())

        val items = arrayOf("Выйти из аккаунта")

        builder.setTitle("Настройки")
            .setItems(items) { _, _ ->
                showLogoutConfirmation()
            }

        return builder.create()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Выход")
            .setMessage("Вы уверены, что хотите выйти?")
            .setPositiveButton("Да") { _, _ ->
                authViewModel.signOut()
                dismiss()
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    companion object {
        const val TAG = "SettingsDialog"
    }
}