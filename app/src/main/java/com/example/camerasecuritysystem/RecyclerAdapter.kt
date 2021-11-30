package com.example.camerasecuritysystem

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast.*
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private var titles = arrayOf("Dashcam", "IP Camera", "Motion Camera")
    private var images = intArrayOf(
        R.drawable.dashcam_thumbnail,
        R.drawable.dashcam_thumbnail,
        R.drawable.dashcam_thumbnail
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
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
                        makeText(itemView.context, adapterPosition.toString(), LENGTH_SHORT).show()
                    }
                    1 -> {
                        intent = Intent(itemView.context, CameraActivity::class.java)
                        intent.putExtra("modus", CameraMode.IPCAMERA)
                        itemView.context.startActivity(intent)
                        makeText(itemView.context, adapterPosition.toString(), LENGTH_SHORT).show()
                    }
                    2 -> {
                        intent = Intent(itemView.context, CameraActivity::class.java)
                        intent.putExtra("modus", CameraMode.MOTIONCAMERA)
                        itemView.context.startActivity(intent)
                        makeText(itemView.context, adapterPosition.toString(), LENGTH_SHORT).show()
                    }
                    else -> {
                        makeText(itemView.context, "The page could not be loaded.", LENGTH_SHORT).show()

                    }
                }
            }

        }
    }
}