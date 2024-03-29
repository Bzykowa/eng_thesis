package com.example.lockband

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.lockband.adapters.PagerAdapter
import com.example.lockband.databinding.FragmentViewPagerBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment which hosts all app fragments and allows easy switching between them
 */
@AndroidEntryPoint
class HomeViewPagerFragment : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentViewPagerBinding.inflate(inflater,container,false)
        val viewPager = binding.viewPager

        viewPager.adapter = PagerAdapter(this)

        return binding.root
    }
}