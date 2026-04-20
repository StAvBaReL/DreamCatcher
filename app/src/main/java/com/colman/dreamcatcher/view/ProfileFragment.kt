package com.colman.dreamcatcher.view

import android.app.AlertDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.colman.dreamcatcher.R
import com.colman.dreamcatcher.databinding.FragmentProfileBinding
import com.colman.dreamcatcher.utils.CircleTransform
import com.colman.dreamcatcher.viewmodel.AuthViewModel
import com.colman.dreamcatcher.viewmodel.ProfileViewModel
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private var binding: FragmentProfileBinding? = null

    private val authViewModel: AuthViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    private lateinit var adapter: FeedAdapter

    private var selectedImageUri: Uri? = null
    private var selectedImageBytes: ByteArray? = null
    private var isEditMode = false

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                selectedImageBytes = null
                val currentBinding = binding ?: return@let
                Picasso.get().load(it).transform(CircleTransform())
                    .into(currentBinding.ivProfileImage)
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            bitmap?.let {
                val baos = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                selectedImageBytes = baos.toByteArray()
                selectedImageUri = null
                val currentBinding = binding ?: return@let
                currentBinding.ivProfileImage.setImageBitmap(
                    CircleTransform().transform(it)
                )
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = binding ?: return
        setEditMode(false)
        setupRecyclerView()

        val user = profileViewModel.getCurrentUser()
        user?.let {
            binding.etNickname.setText(it.displayName ?: "")
            binding.tvEmail.text = it.email ?: ""
            if (it.photoUrl != null) {
                Picasso.get().load(it.photoUrl)
                    .transform(CircleTransform())
                    .into(binding.ivProfileImage)
            }
        }

        profileViewModel.fetchUserStats()

        binding.btnEditProfile.setOnClickListener {
            if (isEditMode) {
                val nickname = binding.etNickname.text.toString().trim()
                if (nickname.isEmpty()) {
                    Toast.makeText(context, "Nickname cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                profileViewModel.updateProfile(nickname, selectedImageUri, selectedImageBytes)
            } else {
                setEditMode(true)
            }
        }

        binding.btnCancelEdit.setOnClickListener {
            setEditMode(false)
            val currentUser = profileViewModel.getCurrentUser()
            currentUser?.let {
                binding.etNickname.setText(it.displayName ?: "")
                if (it.photoUrl != null) {
                    Picasso.get().load(it.photoUrl)
                        .transform(CircleTransform())
                        .into(binding.ivProfileImage)
                }
            }
        }

        binding.btnChangeImage.setOnClickListener {
            val options = arrayOf("Camera", "Gallery")
            AlertDialog.Builder(requireContext())
                .setTitle("Choose Image Source")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> takePictureLauncher.launch(null)
                        1 -> pickImageLauncher.launch("image/*")
                    }
                }
                .show()
        }

        binding.btnSignOut.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out") { _, _ ->
                    authViewModel.signOut()
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, true)
                        .build()
                    findNavController().navigate(R.id.loginFragment, null, navOptions)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        observeViewModel()
    }

    private fun setEditMode(edit: Boolean) {
        val binding = binding ?: return
        isEditMode = edit
        if (edit) {
            binding.etNickname.isEnabled = true
            binding.etNickname.requestFocus()
            binding.etNickname.setBackgroundResource(R.drawable.bg_edit_text)
            val padding = (8 * resources.displayMetrics.density).toInt()
            binding.etNickname.setPadding(padding, padding, padding, padding)

            binding.btnChangeImage.visibility = View.VISIBLE
            binding.btnCancelEdit.visibility = View.VISIBLE
            binding.btnEditProfile.setImageResource(android.R.drawable.ic_menu_save)
            binding.btnEditProfile.setColorFilter("#4285F4".toColorInt())
        } else {
            binding.etNickname.isEnabled = false
            binding.etNickname.background = null
            binding.etNickname.setPadding(0, 0, 0, 0)

            binding.btnChangeImage.visibility = View.GONE
            binding.btnCancelEdit.visibility = View.GONE
            binding.btnEditProfile.setImageResource(android.R.drawable.ic_menu_edit)
            binding.btnEditProfile.setColorFilter("#A0A0A0".toColorInt())
        }
    }

    private fun observeViewModel() {
        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            val currentBinding = this.binding ?: return@observe

            currentBinding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            currentBinding.btnEditProfile.isEnabled = !isLoading
            currentBinding.btnCancelEdit.isEnabled = !isLoading
            currentBinding.btnChangeImage.isEnabled = !isLoading
        }

        profileViewModel.dreamsCount.observe(viewLifecycleOwner) { count ->
            val currentBinding = this.binding ?: return@observe

            currentBinding.tvDreamsCount.text = count.toString()
        }

        profileViewModel.likesCount.observe(viewLifecycleOwner) { count ->
            val currentBinding = this.binding ?: return@observe
            currentBinding.tvLikesCount.text = count.toString()
        }

        profileViewModel.userDreams.observe(viewLifecycleOwner) { dreams ->
            val currentBinding = this.binding ?: return@observe
            if (dreams == null) {
                currentBinding.rvProfileDreams.visibility = View.GONE
            } else {
                currentBinding.rvProfileDreams.visibility = View.VISIBLE
                adapter.submitData(lifecycle, dreams)
            }
        }

        profileViewModel.updateState.observe(viewLifecycleOwner) { isUpdated ->
            if (isUpdated) {
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                setEditMode(false)
            }
        }

        profileViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                profileViewModel.clearErrorMessage()
            }
        }
    }

    private fun setupRecyclerView() {
        val currentBinding = binding ?: return
        adapter = FeedAdapter()
        currentBinding.rvProfileDreams.layoutManager = LinearLayoutManager(requireContext())
        currentBinding.rvProfileDreams.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
