package com.camerasecuritysystem.client.gallery

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import kotlin.collections.ArrayList

const val WIDTH = 256
const val HEIGHT = 256
const val SPAN_COUNT = 3

class DashcamGalleryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewLayoutManager: RecyclerView.LayoutManager

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_dashcam_gallery, container, false)

        recyclerView = root.findViewById(R.id.recyclerView)
        recyclerViewLayoutManager = GridLayoutManager(context, SPAN_COUNT)
        recyclerView.layoutManager = recyclerViewLayoutManager

        return root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchVideosFromFiles()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun fetchVideosFromFiles() {

        val path = "${requireContext().filesDir}/dashcam/"

        val directory = File(path)
        val files = directory.listFiles()

        val videoArray = fileListToVideoList(files, MediaMetadataRetriever())

        val videoAdapter = VideoAdapter(requireContext(), videoArray, requireActivity())
        recyclerView.adapter = videoAdapter
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun fileListToVideoList(
        fileList: Array<File>?,
        retriever: MediaMetadataRetriever
    ): ArrayList<Video> {

        if (fileList == null) {
            return ArrayList()
        }

        val videoArray = ArrayList<Video>()
        var index = 0

        while (index < fileList.size) {
            try {
                val currentFile = fileList[index]
                val path = currentFile.path
                retriever.setDataSource(path)

                var embedPic: Bitmap? = null

                // API level 29 or higher required for generating thumbnails. Otherwise default thumbnail will be used.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    embedPic = retriever.getScaledFrameAtTime(0, 0, WIDTH, HEIGHT)
                }

                videoArray.add(Video(path, embedPic))
            } catch (e: Exception) {
                Log.e("Thumbnail retriever: ", "$e")
            } finally {
                index++
            }
        }

        return ArrayList(videoArray.asReversed())
    }
}
