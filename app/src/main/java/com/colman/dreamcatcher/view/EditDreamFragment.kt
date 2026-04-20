package com.colman.dreamcatcher.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.colman.dreamcatcher.databinding.FragmentEditDreamBinding
import com.colman.dreamcatcher.viewmodel.EditDreamViewModel
import com.colman.dreamcatcher.viewmodel.LoadingState
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso

class EditDreamFragment : Fragment() {

    private var binding: FragmentEditDreamBinding? = null
    private val viewModel: EditDreamViewModel by viewModels()
    private val args: EditDreamFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditDreamBinding.inflate(inflater, container, false)
        return binding?.root
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
            val currentBinding = this.binding ?: return@observe
            currentBinding.etEditTitle.setText(post.title)
            currentBinding.etEditDescription.setText(post.description)
            Picasso.get()
                .load(post.imageUrl)
                .into(currentBinding.ivEditImage)
        }

        viewModel.imageRegenState.observe(viewLifecycleOwner) { state ->
            val currentBinding = this.binding ?: return@observe
            when (state) {
                LoadingState.LOADING -> {
                    currentBinding.pbImageRegen.visibility = View.VISIBLE
                    currentBinding.btnRevisualize.isEnabled = false
                }

                LoadingState.SUCCESS -> {
                    currentBinding.pbImageRegen.visibility = View.GONE
                    currentBinding.btnRevisualize.isEnabled = true
                    viewModel.post.value?.imageUrl?.let { url ->
                        Picasso.get().load(url).into(currentBinding.ivEditImage)
                    }
                }

                LoadingState.ERROR -> {
                    currentBinding.pbImageRegen.visibility = View.GONE
                    currentBinding.btnRevisualize.isEnabled = true
                    Snackbar.make(
                        currentBinding.root,
                        "Failed to regenerate image",
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }

                else -> {}
            }
        }

        viewModel.saveState.observe(viewLifecycleOwner) { state ->
            val currentBinding = this.binding ?: return@observe
            when (state) {
                LoadingState.LOADING -> currentBinding.btnSaveChanges.isEnabled = false
                LoadingState.SUCCESS -> findNavController().popBackStack()
                LoadingState.ERROR -> {
                    currentBinding.btnSaveChanges.isEnabled = true
                    Snackbar.make(
                        currentBinding.root,
                        "Save failed. Please try again.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                else -> currentBinding.btnSaveChanges.isEnabled = true
            }
        }
    }

    private fun setupListeners() {
        val binding = binding ?: return
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
        binding = null
    }
}
