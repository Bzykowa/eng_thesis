package com.example.lockband

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lockband.adapters.AppListAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var appList: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val pm : PackageManager = packageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //List of installed apps TODO: move this
        viewManager = LinearLayoutManager(this)
        viewAdapter = AppListAdapter(pm.getInstalledApplications(PackageManager.GET_META_DATA), this)
        appList = findViewById<RecyclerView>(R.id.app_list).apply{
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }
}
