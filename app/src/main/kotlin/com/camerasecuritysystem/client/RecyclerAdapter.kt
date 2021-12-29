package com.camerasecuritysystem.client

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast.*
import androidx.recyclerview.widget.RecyclerView
import com.camerasecuritysystem.client.models.CameraMode

class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private var titles = arrayOf("Dashcam", "IP Camera", "Motion Camera")
    private var images = intArrayOf(
        R.drawable.dashcam_thumbnail,
        R.drawable.ip_camera_thumbnail,
        R.drawable.motion_camera_thumbnail
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemTitle.text = titles[position]
        holder.itemImage.setImageResource(images[position])
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemImage: ImageView
        var itemTitle: TextView

        init {
            itemImage = itemView.findViewById(R.id.item_image)
            itemTitle = itemView.findViewById(R.id.item_title)

            itemView.setOnClickListener {

                val intent: Intent

                when (adapterPosition) {
                    0 -> {
                        intent = Intent(itemView.context, CameraActivity::class.java)
                        intent.putExtra("modus", CameraMode.DASHCAM)
                        itemView.context.startActivity(intent)
                    }
                    1 -> {
                        intent = Intent(itemView.context, CameraActivity::class.java)
                        intent.putExtra("modus", CameraMode.IPCAMERA)
                        itemView.context.startActivity(intent)
                    }
                    2 -> {
                        intent = Intent(itemView.context, CameraActivity::class.java)
                        intent.putExtra("modus", CameraMode.MOTIONCAMERA)
                        itemView.context.startActivity(intent)
                    }
                    else -> {
                        makeText(itemView.context, "The page could not be loaded.", LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
