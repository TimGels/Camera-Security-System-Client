package com.camerasecuritysystem.client

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.camerasecuritysystem.client.gallery.FragmentAdapter
import com.google.android.material.tabs.TabLayout

class GalleryActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var pager: ViewPager2
    private lateinit var adapter: FragmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        supportActionBar?.hide()

        tabLayout = findViewById(R.id.tab_layout)
        pager = findViewById(R.id.view_pager)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener { onBackPressed() }

        tabLayout.addTab(tabLayout.newTab().setText("Dashcam"))
        tabLayout.addTab(tabLayout.newTab().setText("IP Camera"))
        tabLayout.addTab(tabLayout.newTab().setText("Motion Camera"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                pager.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {
                // not implemented
            }
            override fun onTabReselected(tab: TabLayout.Tab) {
                // not implemented
            }
        })

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val fm: FragmentManager = supportFragmentManager
        adapter = FragmentAdapter(fm, lifecycle)
        pager.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return false
    }
}
