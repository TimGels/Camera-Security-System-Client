package com.camerasecuritysystem.client

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.camerasecuritysystem.client.databinding.ActivityGalleryBinding
import com.camerasecuritysystem.client.gallery.FragmentAdapter
import com.google.android.material.tabs.TabLayout

class GalleryActivity : AppCompatActivity() {

    private lateinit var tabLayout : TabLayout
    private lateinit var pager : ViewPager2
    private lateinit var adapter : FragmentAdapter

    private var binding: ActivityGalleryBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tabLayout = findViewById(R.id.tab_layout)
        pager = findViewById(R.id.view_pager)

        val fm : FragmentManager = supportFragmentManager
        adapter = FragmentAdapter(fm, lifecycle)
        pager.adapter = adapter

        tabLayout.addTab(tabLayout.newTab().setText("Dashcam"))
        tabLayout.addTab(tabLayout.newTab().setText("IP Camera"))
        tabLayout.addTab(tabLayout.newTab().setText("Motion Camera"))

        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                pager!!.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {

            }
            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        pager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return false
    }
}