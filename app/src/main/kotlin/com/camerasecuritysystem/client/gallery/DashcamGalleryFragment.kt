package com.camerasecuritysystem.client.gallery

import android.media.ThumbnailUtils
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.camerasecuritysystem.client.R
import com.camerasecuritysystem.client.models.Video
import java.io.File


class DashcamGalleryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewLayoutManager: RecyclerView.LayoutManager

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var root = inflater.inflate(R.layout.fragment_dashcam_gallery, container, false)

        recyclerView = root.findViewById(R.id.recyclerView)
        recyclerViewLayoutManager = GridLayoutManager(context, 3)
        recyclerView.layoutManager = recyclerViewLayoutManager

        fetchVideosFromFiles()

        return root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun fetchVideosFromFiles(){

        val path = "${requireContext().filesDir}/dashcam/"

        var directory = File(path)

        val files = directory.listFiles()

        var videoArray = ArrayList<Video>()
        var index = 0

        while (index < files.size){

            Log.e("VIDEO", "$index")
            val currentFile = files[index]

            val path = currentFile.path
            val thumb = ThumbnailUtils.createVideoThumbnail(path,
                MediaStore.Images.Thumbnails.MINI_KIND); //ThumbnailUtils.createVideoThumbnail(File(path), Size.parseSize("3*+6"), CancellationSignal())

            Log.e("THUMB", "$thumb")

            videoArray.add(Video(path, thumb))
            index++

        }

        val videoAdapter = VideoAdapter(requireContext(), videoArray, requireActivity())
        recyclerView.adapter = videoAdapter

    }


}