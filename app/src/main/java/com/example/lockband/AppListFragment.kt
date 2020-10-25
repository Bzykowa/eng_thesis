package com.example.lockband

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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

        val adapter = AppListAdapter()
        binding.appList.adapter = adapter
        val apps = updateAppList()
        apps.sortBy { selectAppsByName(it) }
        adapter.submitList(apps)
        subscribeUi(adapter)

        return binding.root
    }

    private fun subscribeUi(adapter: AppListAdapter) {
        viewModel.appStates.observe(viewLifecycleOwner) { appStates ->
            adapter.submitList(appStates)
        }
    }

    //sort
    private fun selectAppsByName(item: AppState): String {
        val result = item.label
        //locale to set?
        return if (result.indexOf('.') != result.lastIndexOf('.')) result else result.capitalize()
    }

    private fun updateAppList() : MutableList<AppState>{
        val pm = requireContext().packageManager
        val appList: MutableList<ApplicationInfo> = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val entities : MutableList<AppState> = mutableListOf()
        appList.forEach{
            entities.add(AppState(it.packageName, it.loadLabel(pm).toString(), it.icon))
        }
        return entities
    }
}