package com.camerasecuritysystem.client.gallery

import android.graphics.BitmapFactory
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
import android.media.MediaMetadataRetriever


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
    private fun fetchVideosFromFiles() {

        val path = "${requireContext().filesDir}/dashcam/"

        val directory = File(path)

        val files = directory.listFiles()
        val retriever = MediaMetadataRetriever()

        val videoArray = ArrayList<Video>()
        var index = 0

        while (index < files.size) {
            try{
                val currentFile = files[index]
                val path = currentFile.path
                retriever.setDataSource(path)

                val embedPic = retriever.getScaledFrameAtTime(0,0,256,256)

                videoArray.add(Video(path,embedPic))
            }catch (e : Exception){
                Log.e("Thumbnail retriever: ", "$e")
            }finally {
                index++
            }
        }
        val videoAdapter = VideoAdapter(requireContext(), videoArray, requireActivity())
        recyclerView.adapter = videoAdapter

    }


}