package com.chaddysroom.vloggingapp.adapters

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.request.RequestOptions
import java.util.*
import com.chaddysroom.vloggingapp.R
import com.chaddysroom.vloggingapp.classes.Effect

class EffectRecyclerViewAdapter(
    private val photo_effects: LinkedList<Effect>,
    private val video_effects: LinkedList<Effect>,
    private val mContext: Context?
) : RecyclerView.Adapter<EffectRecyclerViewAdapter.ViewHolder>() {

    var isPhoto = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_effect, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val options = RequestOptions()
        if (isPhoto) {
            Glide.with(mContext!!)
                .load(photo_effects[position].thumbnail)
                .apply(options.fitCenter())
                .into(holder.effectThumbnail)
        } else if (!isPhoto) {
            Glide.with(mContext!!)
                .load(video_effects[position].thumbnail)
                .apply(options.fitCenter())
                .into(holder.effectThumbnail)
        }

    }

    override fun getItemCount(): Int {
        if (isPhoto) {
            return photo_effects.size
        } else if (!isPhoto) {
            return video_effects.size
        }
        return -1
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var effectThumbnail: ImageView = itemView.findViewById(R.id.effect_thumbnail)
        internal var parentLayout: ConstraintLayout = itemView.findViewById(R.id.effect_parent_layout)
    }


}