package com.example.lockband

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.lockband.adapters.AppListAdapter
import com.example.lockband.data.AppState
import com.example.lockband.databinding.FragmentAppListBinding
import com.example.lockband.viewmodels.AppListViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AppListFragment : Fragment() {

    private val viewModel: AppListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAppListBinding.inflate(inflater, container, false)
        context ?: return binding.root

        val pm = requireContext().packageManager
        val appList: List<ApplicationInfo> =
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .sortedBy { it.loadLabel(pm).toString() }
        val icons = mutableListOf<Drawable>()
        
        appList.forEach {
            icons.add(it.loadIcon(pm))
            if (viewModel.appStates.value?.find { state -> state.packageName == it.packageName } == null)
                viewModel.insertAppState(it.packageName, it.loadLabel(pm).toString(), false)
        }

        val adapter = AppListAdapter({ item -> updateLockState(item as AppState) }, icons)
        binding.appList.adapter = adapter
        subscribeUi(adapter)
        return binding.root
    }

    private fun subscribeUi(adapter: AppListAdapter) {
        viewModel.appStates.observe(viewLifecycleOwner) { appStates ->
            adapter.submitList(appStates)
        }
    }

    private fun updateLockState(item: AppState) {
        viewModel.updateAppState(item)
    }

}