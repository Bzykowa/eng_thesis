package com.example.lockband

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lockband.adapters.AppListAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appListView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private fun selectAppsByName(item: ApplicationInfo): String {
        val result = item.loadLabel(packageManager).toString()
        //locale to set?
        return if (result.indexOf('.') != result.lastIndexOf('.')) result else result.capitalize()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //List of installed apps TODO: move this
        val appList: MutableList<ApplicationInfo> =
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        appList.sortBy { selectAppsByName(it) }
        viewManager = LinearLayoutManager(this)
        viewAdapter = AppListAdapter(appList, this)
        appListView = findViewById<RecyclerView>(R.id.app_list).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }
}


