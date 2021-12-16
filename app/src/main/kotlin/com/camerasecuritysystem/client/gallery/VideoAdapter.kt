package com.camerasecuritysystem.client.gallery

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.camerasecuritysystem.client.R
import com.camerasecuritysystem.client.models.Video

class VideoAdapter(
    private val context: Context, private val videos: ArrayList<Video>,
    private val activity: Activity
) : RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.custom_video, parent, false)
        return this.ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.image.setImageBitmap(videos[position].thumbnail)

            GlideApp.with(context).load(videos[position].thumbnail)
            .placeholder(R.color.black)
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.ALL).thumbnail(0.25f)
            .into(holder.image)

//        holder.itemTitle.text = videos[position].path
//        holder.itemImage.setImageResource(images[position])
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var image: ImageView
//        var itemTitle: TextView

        init {
//            itemTitle = itemView.findViewById(R.id.cover_image)
//            itemTitle.text
            image = itemView.findViewById(R.id.cover_image)
//            image.setImageResource()

            itemView.setOnClickListener {
//
//                val intent: Intent
//
//                when (adapterPosition) {
//                    0 -> {
//                        intent = Intent(itemView.context, CameraActivity::class.java)
//                        intent.putExtra("modus", CameraMode.DASHCAM)
//                        itemView.context.startActivity(intent)
//                    }
//                    1 -> {
//                        intent = Intent(itemView.context, CameraActivity::class.java)
//                        intent.putExtra("modus", CameraMode.IPCAMERA)
//                        itemView.context.startActivity(intent)
//                        Toast.makeText(
//                            itemView.context,
//                            adapterPosition.toString(),
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                    2 -> {
//                        intent = Intent(itemView.context, CameraActivity::class.java)
//                        intent.putExtra("modus", CameraMode.MOTIONCAMERA)
//                        itemView.context.startActivity(intent)
//                        Toast.makeText(
//                            itemView.context,
//                            adapterPosition.toString(),
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                    else -> {
//                        Toast.makeText(
//                            itemView.context,
//                            "The page could not be loaded.",
//                            Toast.LENGTH_SHORT
//                        ).show()
//
//                    }
//                }
            }

        }
    }


}
