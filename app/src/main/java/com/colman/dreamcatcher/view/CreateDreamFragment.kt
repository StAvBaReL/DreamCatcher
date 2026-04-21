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
import com.colman.dreamcatcher.R
import com.colman.dreamcatcher.databinding.FragmentCreateDreamBinding
import com.colman.dreamcatcher.viewmodel.CreateDreamViewModel
import com.colman.dreamcatcher.viewmodel.LoadingState
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso

class CreateDreamFragment : Fragment() {

    private var binding: FragmentCreateDreamBinding? = null
    private val viewModel: CreateDreamViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateDreamBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGradientTitle()
        setupObservers()
        setupListeners()
    }

    private fun setupGradientTitle() {
        val currentBinding = binding ?: return
        currentBinding.tvTitle.post {
            val paint = currentBinding.tvTitle.paint
            val width = paint.measureText(currentBinding.tvTitle.text.toString())
            val shader = LinearGradient(
                0f, 0f, width, 0f,
                intArrayOf(
                    ContextCompat.getColor(requireContext(), R.color.purple_primary),
                    ContextCompat.getColor(requireContext(), R.color.teal_primary)
                ),
                null,
                Shader.TileMode.CLAMP
            )
            currentBinding.tvTitle.paint.shader = shader
            currentBinding.tvTitle.invalidate()
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
            val currentBinding = binding ?: return@observe
            val secureUrl = url?.replace("http://", "https://")
            Picasso.get()
                .load(secureUrl)
                .into(currentBinding.ivGeneratedImage)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            val currentBinding = binding ?: return@observe
            Snackbar.make(currentBinding.root, message, Snackbar.LENGTH_LONG).show()
        }

        viewModel.postLoadingState.observe(viewLifecycleOwner) { state ->
            val currentBinding = binding ?: return@observe
            when (state) {
                LoadingState.LOADING -> {
                    currentBinding.btnPostDream.isEnabled = false
                    currentBinding.pbPostLoading.visibility = View.VISIBLE
                }

                LoadingState.SUCCESS -> {
                    findNavController().navigate(R.id.action_createDreamFragment_to_feedFragment)
                }

                LoadingState.ERROR -> {
                    currentBinding.btnPostDream.isEnabled = true
                    currentBinding.pbPostLoading.visibility = View.GONE
                }

                else -> {
                    currentBinding.btnPostDream.isEnabled = true
                    currentBinding.pbPostLoading.visibility = View.GONE
                }
            }
        }
    }

    private fun setupListeners() {
        val currentBinding = binding ?: return
        currentBinding.btnVisualize.setOnClickListener {
            val prompt = currentBinding.etDreamDescription.text.toString()
            viewModel.visualizeDream(prompt)
        }

        currentBinding.btnPostDream.setOnClickListener {
            val title = currentBinding.etDreamTitle.text.toString()
            val description = currentBinding.etDreamDescription.text.toString()
            val imageUrl = viewModel.generatedImageUrl.value ?: ""
            viewModel.postDream(title, description, imageUrl)
        }
    }

    private fun setIdleState() {
        val currentBinding = binding ?: return
        currentBinding.btnVisualize.isEnabled = true
        currentBinding.imageCard.visibility = View.GONE
        currentBinding.postSection.visibility = View.GONE
    }

    private fun setLoadingState() {
        val currentBinding = binding ?: return
        currentBinding.btnVisualize.isEnabled = false
        currentBinding.imageCard.visibility = View.VISIBLE
        currentBinding.pbLoading.visibility = View.VISIBLE
        currentBinding.ivGeneratedImage.visibility = View.INVISIBLE
        currentBinding.postSection.visibility = View.GONE
    }

    private fun setSuccessState() {
        val currentBinding = binding ?: return
        currentBinding.btnVisualize.isEnabled = true
        currentBinding.imageCard.visibility = View.VISIBLE
        currentBinding.pbLoading.visibility = View.GONE
        currentBinding.ivGeneratedImage.visibility = View.VISIBLE
        currentBinding.postSection.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
