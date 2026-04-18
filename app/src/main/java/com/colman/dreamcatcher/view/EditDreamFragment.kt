package com.colman.dreamcatcher.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import com.colman.dreamcatcher.databinding.FragmentEditDreamBinding
import com.colman.dreamcatcher.viewmodel.EditDreamViewModel
import com.colman.dreamcatcher.viewmodel.LoadingState
import com.google.android.material.snackbar.Snackbar

class EditDreamFragment : Fragment() {

    private var _binding: FragmentEditDreamBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditDreamViewModel by viewModels()
    private val args: EditDreamFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditDreamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadPost(args.postId)
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.post.observe(viewLifecycleOwner) { post ->
            post ?: return@observe
            binding.etEditTitle.setText(post.title)
            binding.etEditDescription.setText(post.description)
            Picasso.get()
                .load(post.imageUrl)
                .into(binding.ivEditImage)
        }

        viewModel.imageRegenState.observe(viewLifecycleOwner) { state ->
            when (state) {
                LoadingState.LOADING -> {
                    binding.pbImageRegen.visibility = View.VISIBLE
                    binding.btnRevisualize.isEnabled = false
                }

                LoadingState.SUCCESS -> {
                    binding.pbImageRegen.visibility = View.GONE
                    binding.btnRevisualize.isEnabled = true
                    viewModel.post.value?.imageUrl?.let { url ->
                        Picasso.get().load(url).into(binding.ivEditImage)
                    }
                }

                LoadingState.ERROR -> {
                    binding.pbImageRegen.visibility = View.GONE
                    binding.btnRevisualize.isEnabled = true
                    Snackbar.make(binding.root, "Failed to regenerate image", Snackbar.LENGTH_SHORT)
                        .show()
                }

                else -> {}
            }
        }

        viewModel.saveState.observe(viewLifecycleOwner) { state ->
            when (state) {
                LoadingState.LOADING -> binding.btnSaveChanges.isEnabled = false
                LoadingState.SUCCESS -> findNavController().popBackStack()
                LoadingState.ERROR -> {
                    binding.btnSaveChanges.isEnabled = true
                    Snackbar.make(
                        binding.root,
                        "Save failed. Please try again.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                else -> binding.btnSaveChanges.isEnabled = true
            }
        }
    }

    private fun setupListeners() {
        binding.btnRevisualize.setOnClickListener {
            val prompt = binding.etEditDescription.text.toString()
            viewModel.regenerateImage(prompt)
        }

        binding.btnSaveChanges.setOnClickListener {
            val title = binding.etEditTitle.text.toString()
            val description = binding.etEditDescription.text.toString()
            val imageUrl = viewModel.post.value?.imageUrl ?: ""
            viewModel.savePost(title, description, imageUrl)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
