package com.example.petDiary.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.petDiary.R
import com.example.petDiary.ui.viewmodel.AuthViewModel
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var authViewModel: AuthViewModel

    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var btnBack: Button
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var btnAction: Button

    private var authMode = "register"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            authMode = it.getString("auth_mode", "register")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]

        initViews(view)
        setupUI()
        setupClickListeners()

        // ← Наблюдаем за состоянием аутентификации
        observeAuthState()
    }

    private fun initViews(view: View) {
        tvTitle = view.findViewById(R.id.tvTitle)
        tvSubtitle = view.findViewById(R.id.tvSubtitle)
        btnBack = view.findViewById(R.id.btnBack)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        emailLayout = view.findViewById(R.id.emailLayout)
        passwordLayout = view.findViewById(R.id.passwordLayout)
        btnAction = view.findViewById(R.id.btnAction)
    }

    private fun setupUI() {
        if (authMode == "login") {
            tvTitle.text = "Вход"
            tvSubtitle.text = "Введите email и пароль"
            passwordLayout.visibility = View.VISIBLE
            btnAction.text = "Войти"
        } else {
            tvTitle.text = "Регистрация"
            tvSubtitle.text = "Введите email и пароль"
            passwordLayout.visibility = View.VISIBLE
            btnAction.text = "Зарегистрироваться"
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnAction.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Введите email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(requireContext(), "Введите пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (authMode == "login") {
                login(email, password)
            } else {
                register(email, password)
            }
        }
    }

    private fun observeAuthState() {
        // ← Наблюдаем за авторизацией
        authViewModel.isAuthenticated.observe(viewLifecycleOwner) { isAuthenticated ->
            if (isAuthenticated) {
                findNavController().popBackStack(R.id.authChoiceFragment, true)
            }
        }

        // ← Наблюдаем за гостевым режимом
        authViewModel.isGuest.observe(viewLifecycleOwner) { isGuest ->
            if (isGuest) {
                findNavController().popBackStack(R.id.authChoiceFragment, true)
            }
        }

        // ← Наблюдаем за ошибками
        authViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                authViewModel.clearError()
            }
        }
    }

    private fun register(email: String, password: String) {
        btnAction.isEnabled = false
        btnAction.text = "Регистрация..."

        authViewModel.register(email, password)

        // Возвращаем кнопку в исходное состояние (успех или ошибка будет через observe)
        btnAction.isEnabled = true
        btnAction.text = if (authMode == "login") "Войти" else "Зарегистрироваться"
    }

    private fun login(email: String, password: String) {
        btnAction.isEnabled = false
        btnAction.text = "Вход..."

        authViewModel.login(email, password)

        btnAction.isEnabled = true
        btnAction.text = if (authMode == "login") "Войти" else "Зарегистрироваться"
    }

    companion object {
        fun newInstance(mode: String): LoginFragment {
            return LoginFragment().apply {
                arguments = Bundle().apply {
                    putString("auth_mode", mode)
                }
            }
        }
    }
}