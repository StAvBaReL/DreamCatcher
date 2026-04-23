package com.colman.dreamcatcher.view

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import com.colman.dreamcatcher.R
import com.colman.dreamcatcher.databinding.FragmentCreateDreamBinding
import com.colman.dreamcatcher.viewmodel.CreateDreamViewModel
import com.colman.dreamcatcher.viewmodel.LoadingState
import com.google.android.material.snackbar.Snackbar

class CreateDreamFragment : Fragment() {

    private var _binding: FragmentCreateDreamBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateDreamViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateDreamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGradientTitle()
        setupObservers()
        setupListeners()
    }

    private fun setupGradientTitle() {
        binding.tvTitle.post {
            val paint = binding.tvTitle.paint
            val width = paint.measureText(binding.tvTitle.text.toString())
            val shader = LinearGradient(
                0f, 0f, width, 0f,
                intArrayOf(
                    ContextCompat.getColor(requireContext(), R.color.purple_primary),
                    ContextCompat.getColor(requireContext(), R.color.teal_primary)
                ),
                null,
                Shader.TileMode.CLAMP
            )
            binding.tvTitle.paint.shader = shader
            binding.tvTitle.invalidate()
        }
    }

    private fun setupObservers() {
        viewModel.loadingState.observe(viewLifecycleOwner) { state ->
            when (state) {
                LoadingState.LOADING -> setLoadingState()
                LoadingState.SUCCESS -> setSuccessState()
                else -> setIdleState()
            }
        }

        viewModel.generatedImageUrl.observe(viewLifecycleOwner) { url ->
            val secureUrl = url?.replace("http://", "https://")
            Picasso.get()
                .load(secureUrl)
                .into(binding.ivGeneratedImage)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }

        viewModel.postLoadingState.observe(viewLifecycleOwner) { state ->
            when (state) {
                LoadingState.LOADING -> {
                    binding.btnPostDream.isEnabled = false
                    binding.pbPostLoading.visibility = View.VISIBLE
                }

                LoadingState.SUCCESS -> {
                    findNavController().navigate(CreateDreamFragmentDirections.actionCreateDreamFragmentToJournalFragment())
                }

                LoadingState.ERROR -> {
                    binding.btnPostDream.isEnabled = true
                    binding.pbPostLoading.visibility = View.GONE
                }

                else -> {
                    binding.btnPostDream.isEnabled = true
                    binding.pbPostLoading.visibility = View.GONE
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnVisualize.setOnClickListener {
            val prompt = binding.etDreamDescription.text.toString()
            viewModel.visualizeDream(prompt)
        }

        binding.btnPostDream.setOnClickListener {
            val title = binding.etDreamTitle.text.toString()
            val description = binding.etDreamDescription.text.toString()
            val imageUrl = viewModel.generatedImageUrl.value ?: ""
            viewModel.postDream(title, description, imageUrl)
        }
    }

    private fun setIdleState() {
        binding.btnVisualize.isEnabled = true
        binding.imageCard.visibility = View.GONE
        binding.postSection.visibility = View.GONE
    }

    private fun setLoadingState() {
        binding.btnVisualize.isEnabled = false
        binding.imageCard.visibility = View.VISIBLE
        binding.pbLoading.visibility = View.VISIBLE
        binding.ivGeneratedImage.visibility = View.INVISIBLE
        binding.postSection.visibility = View.GONE
    }

    private fun setSuccessState() {
        binding.btnVisualize.isEnabled = true
        binding.imageCard.visibility = View.VISIBLE
        binding.pbLoading.visibility = View.GONE
        binding.ivGeneratedImage.visibility = View.VISIBLE
        binding.postSection.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
