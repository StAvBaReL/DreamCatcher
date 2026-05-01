package com.colman.dreamcatcher.view

import android.os.Bundle
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
import com.colman.dreamcatcher.R
import com.colman.dreamcatcher.databinding.FragmentLoginBinding
import com.colman.dreamcatcher.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var binding: FragmentLoginBinding? = null

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var credentialManager: CredentialManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        credentialManager = CredentialManager.create(requireContext())

        // Auto-login check
        if (authViewModel.isUserLoggedIn()) {
            navigateToFeed()
            return
        }

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        val binding = binding ?: return
        val defaultWebClientId = try {
            getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            ""
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.login(email, password)
            } else {
                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.tvRegister.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            findNavController().navigate(action)
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
        val googleIdOption: GetGoogleIdOption =
            GetGoogleIdOption.Builder()
                .setServerClientId(webClientId)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
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
                    Log.e("LoginFragment", "Unexpected type of credential")
                    Toast.makeText(
                        context,
                        "Sign in failed: unknown credential type",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: GetCredentialException) {
                Log.e("LoginFragment", "Sign in failed", e)
                Toast.makeText(context, "Google Sign In Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
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
            val currentBinding = binding ?: return@observe
            currentBinding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            currentBinding.btnLogin.isEnabled = !isLoading
            currentBinding.tvRegister.isEnabled = !isLoading
            currentBinding.etEmail.isEnabled = !isLoading
            currentBinding.etPassword.isEnabled = !isLoading
        }
    }

    private fun navigateToFeed() {
        val action = LoginFragmentDirections.actionLoginFragmentToFeedFragment()
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
