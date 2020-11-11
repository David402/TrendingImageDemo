package com.github.david402.giphydemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.github.david402.giphydemo.databinding.GifDetailsFrgBinding
import com.github.david402.giphydemo.util.getViewModelFactory

class GifDetailsFragment : Fragment() {
    private lateinit var viewDataBinding: GifDetailsFrgBinding

    private val args: GifDetailsFragmentArgs by navArgs()

    private val viewModel by viewModels<GifDetailsViewModel> { getViewModelFactory() }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupNavigation()
    }

    private fun setupNavigation() {
//        viewModel.deleteTaskEvent.observe(this, EventObserver {
//            val action = TaskDetailFragmentDirections
//                .actionTaskDetailFragmentToTasksFragment(DELETE_RESULT_OK)
//            findNavController().navigate(action)
//        })
//        viewModel.editTaskEvent.observe(this, EventObserver {
//            val action = TaskDetailFragmentDirections
//                .actionTaskDetailFragmentToAddEditTaskFragment(
//                    args.taskId,
//                    resources.getString(R.string.edit_task)
//                )
//            findNavController().navigate(action)
//        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.gif_details_frg, container, false)
        viewDataBinding = GifDetailsFrgBinding.bind(view).apply {
            viewmodel = viewModel
        }
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner

        viewModel.start(args.id)
        viewModel.gif.observe(viewLifecycleOwner) {
            Glide
                .with(this)
                .load(it.images.original.url)
                .centerCrop()
                .placeholder(R.drawable.user_profile_placeholder)
                .into(viewDataBinding.imageView)
        }


        setHasOptionsMenu(true)
        return view
    }

}