package com.example.petDiary.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.petDiary.R
import com.example.petDiary.ui.viewmodel.AuthViewModel

class AuthChoiceFragment : Fragment() {

    private lateinit var cardRegister: CardView
    private lateinit var cardLogin: CardView
    private lateinit var cardGuest: CardView
    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.auth_choice_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]

        cardRegister = view.findViewById(R.id.cardRegister)
        cardLogin = view.findViewById(R.id.cardLogin)
        cardGuest = view.findViewById(R.id.cardGuest)

        cardRegister.setOnClickListener {
            val bundle = Bundle().apply {
                putString("auth_mode", "register")
            }
            findNavController().navigate(R.id.loginFragment, bundle)
        }

        cardLogin.setOnClickListener {
            val bundle = Bundle().apply {
                putString("auth_mode", "login")
            }
            findNavController().navigate(R.id.loginFragment, bundle)
        }

        cardGuest.setOnClickListener {
            authViewModel.signInAsGuest()
        }
    }

    companion object {
        fun newInstance() = AuthChoiceFragment()
    }
}