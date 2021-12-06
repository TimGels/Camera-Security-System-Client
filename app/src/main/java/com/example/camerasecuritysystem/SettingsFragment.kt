package com.example.camerasecuritysystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceFragmentCompat

import com.example.camerasecuritysystem.databinding.CardLayoutBinding
import com.example.camerasecuritysystem.databinding.FragmentDashcamBinding

class SettingsFragment : PreferenceFragmentCompat(){


    private var binding: CardLayoutBinding? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

    }


}