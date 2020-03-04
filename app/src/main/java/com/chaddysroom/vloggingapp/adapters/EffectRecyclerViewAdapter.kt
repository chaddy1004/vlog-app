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
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.request.RequestOptions
import java.util.*
import com.chaddysroom.vloggingapp.R
import com.chaddysroom.vloggingapp.classes.Effect

class EffectRecyclerViewAdapter(
    var effects: Array<Effect?>,
    private val mContext: Context?
) : RecyclerView.Adapter<EffectRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_effect, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val options = RequestOptions()

        Glide.with(mContext!!)
            .load(effects[position]!!.thumbnail)
            .apply(options.fitCenter())
            .into(holder.effectThumbnail)

        holder.effect_name.text = effects[position]!!.name
        holder.effect_description.text = effects[position]!!.description


//        } else if (!isPhoto) {
//            Glide.with(mContext!!)
//                .load(video_effects[position].thumbnail)
//                .apply(options.fitCenter())
//                .into(holder.effectThumbnail)
//        }
//        if(isPhoto){
//            holder.effect_name.text = photo_effects[position].name
//            holder.effect_description.text = photo_effects[position].description
//        }
//        else{
//            holder.effect_name.text = video_effects[position].name
//            holder.effect_description.text = video_effects[position].description
//        }

    }

    override fun getItemCount(): Int {
        return effects.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var effectThumbnail: ImageView = itemView.findViewById(R.id.effect_thumbnail)
        internal var effect_name: TextView = itemView.findViewById<TextView>(R.id.effect_name)
        internal var effect_description: TextView = itemView.findViewById<TextView>(R.id.effect_description)
        internal var parentLayout: ConstraintLayout = itemView.findViewById(R.id.effect_parent_layout)
    }


}