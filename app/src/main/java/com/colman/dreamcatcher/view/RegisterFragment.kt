package com.colman.dreamcatcher.view

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.colman.dreamcatcher.databinding.FragmentRegisterBinding
import com.colman.dreamcatcher.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var credentialManager: CredentialManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        credentialManager = CredentialManager.create(requireContext())

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        val clientIdRes =
            resources.getIdentifier("default_web_client_id", "string", requireContext().packageName)
        val defaultWebClientId = if (clientIdRes != 0) getString(clientIdRes) else ""

        var isPasswordVisible = false
        binding.ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.ivTogglePassword.alpha = 1.0f
            } else {
                binding.etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.ivTogglePassword.alpha = 0.5f
            }
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        binding.btnRegister.setOnClickListener {
            val nickname = binding.etNickname.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (nickname.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.register(email, password, nickname)
            } else {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnGoogle.setOnClickListener {
            if (defaultWebClientId.isEmpty()) {
                Toast.makeText(
                    context,
                    "Google Sign-In string not found. Did you add google-services.json?",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            signInWithGoogle(defaultWebClientId)
        }
    }

    private fun signInWithGoogle(webClientId: String) {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = requireActivity(),
                )

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)
                    authViewModel.loginWithGoogle(googleIdTokenCredential.idToken)
                } else {
                    Log.e("RegisterFragment", "Unexpected type of credential")
                    Toast.makeText(
                        context,
                        "Sign in failed: unknown credential type",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: GetCredentialException) {
                Log.e("RegisterFragment", "Sign in failed", e)
                Toast.makeText(context, "Google Sign In Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        authViewModel.registerState.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                navigateToFeed()
            }
        }

        authViewModel.loginState.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                navigateToFeed()
            }
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                authViewModel.clearErrorMessage()
            }
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !isLoading
            binding.tvLogin.isEnabled = !isLoading
            binding.etNickname.isEnabled = !isLoading
            binding.etEmail.isEnabled = !isLoading
            binding.etPassword.isEnabled = !isLoading
        }
    }

    private fun navigateToFeed() {
        val action = RegisterFragmentDirections.actionRegisterFragmentToFeedFragment()
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
