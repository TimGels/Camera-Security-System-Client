package com.camerasecuritysystem.client.gallery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.camerasecuritysystem.client.BuildConfig
import com.camerasecuritysystem.client.R
import com.camerasecuritysystem.client.VideoPlayerActivity
import com.camerasecuritysystem.client.models.Video
import java.io.File


class VideoAdapter(
    private val context: Context, private val videos: ArrayList<Video>,
    private val activity: Activity
) : RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    private val videoList: ArrayList<Video> = videos
    private var isSelectMode = false
    private var selectedItems = ArrayList<Video>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.custom_video, parent, false)
        return this.ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        GlideApp.with(context).load(videos[position].thumbnail)
            .placeholder(R.color.black)
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.ALL).thumbnail(0.25f)
            .into(holder.image)

        var deleteButton = activity.findViewById<ImageButton>(R.id.delete_btn)
        var shareButton = activity.findViewById<ImageButton>(R.id.share_btn)

        holder.image.setOnClickListener {

            if (isSelectMode) {

                deleteButton.visibility = View.VISIBLE
                shareButton.visibility = View.VISIBLE

                if (selectedItems.contains(videoList[position])) {
                    holder.overlay.setBackgroundColor(Color.TRANSPARENT)
                    selectedItems.remove(videoList[position])
                } else {
                    holder.overlay.setBackgroundResource(R.color.purple_500)
                    selectedItems.add(videoList[position])
                }
                if (selectedItems.size == 0) {
                    isSelectMode = false
                    deleteButton.visibility = View.INVISIBLE
                    shareButton.visibility = View.INVISIBLE
                }
            } else {
                val intent = Intent(context, VideoPlayerActivity::class.java)
                intent.putExtra("videoPath", videos[position].path)
                activity.startActivity(intent)
            }
        }

        holder.image.setOnLongClickListener {

            isSelectMode = true
            deleteButton.visibility = View.VISIBLE
            shareButton.visibility = View.VISIBLE

            if (selectedItems.contains(videoList[position])) {
                holder.overlay.setBackgroundColor(Color.TRANSPARENT)
                selectedItems.remove(videoList[position])
            } else {
                holder.overlay.setBackgroundResource(R.color.purple_500)
                selectedItems.add(videoList[position])
            }

            if (selectedItems.size == 0) {
                isSelectMode = false
                deleteButton.visibility = View.INVISIBLE
                shareButton.visibility = View.INVISIBLE
            }
            true
        }

        deleteButton.setOnClickListener {

            for (video in selectedItems) {

                //Remove from videoList
                videoList.remove(video)

                //Remove from internal storage
                val dashcamDir = "${context.filesDir}/dashcam/"
                val fileToDelete = File(dashcamDir, video.path.substring(video.path.lastIndexOf("/")+1))
                fileToDelete.delete()

                //Remove from selectedItems
                selectedItems.remove(video)

                val view = activity.findViewById<RecyclerView>(R.id.recyclerView)


            }
            isSelectMode = false
            deleteButton.visibility = View.INVISIBLE
            shareButton.visibility = View.INVISIBLE
        }

        shareButton.setOnClickListener {
            try {

                //Videos to send: the selected items
                var videosToSend = selectedItems

                //Files that will be send
                var uris = ArrayList<Uri>()

                for (video in videosToSend) {

                    val file = File(video.path)

                    if (file.exists()) {
                        val uri = FileProvider.getUriForFile(
                            context,
                            BuildConfig.APPLICATION_ID + ".provider",
                            file
                        )
                        uris.add(uri)
                    }
                }

                val intent = Intent(Intent.ACTION_SEND)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.setType("*/*")
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    context.resources.getString(R.string.share_text)
                )
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int {
        return videos.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var image: ImageView
        var overlay: ImageView

        init {
            image = itemView.findViewById(R.id.cover_image)
            overlay = itemView.findViewById(R.id.overlay)
        }
    }


}
