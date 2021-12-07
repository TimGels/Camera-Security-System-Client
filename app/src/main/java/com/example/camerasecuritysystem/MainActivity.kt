package com.example.camerasecuritysystem

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.camerasecuritysystem.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import android.content.Intent
import android.util.Log
import android.view.MenuItem


import androidx.annotation.NonNull




class MainActivity : AppCompatActivity() {

    private var _binding : ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var listener : NavController.OnDestinationChangedListener

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager

        

        listener = NavController.OnDestinationChangedListener{ controller, destination, arguments ->
            if(destination.id == R.id.homeFragment){
                supportActionBar?.setBackgroundDrawable((ColorDrawable(getColor(R.color.design_default_color_primary_dark))))
            }else if(destination.id == R.id.settingsActivity){
                supportActionBar?.setBackgroundDrawable((ColorDrawable(getColor(R.color.teal_700))))
            }

        }
    }

    override fun onPause() {
        super.onPause()
        navController.removeOnDestinationChangedListener(listener)
    }

    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(listener)
    }

    override fun onStart() {
        super.onStart()
        navController = findNavController(R.id.fragmentContainerView)
        drawerLayout = binding.drawerLayout
        binding.navigationView.setupWithNavController(navController)

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            val id: Int = menuItem.itemId
            if (id == R.id.settingsActivity) {
                val newIntent = Intent(applicationContext, SettingsActivity::class.java)
                applicationContext.startActivity(newIntent)


            }
            else{
                Log.e("TAG:", "$id")
            }

            true
        }

        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        adapter = RecyclerAdapter()
        binding.recyclerView.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragmentContainerView)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}