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
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.request.RequestOptions
import java.util.*
import com.chaddysroom.vloggingapp.R
import com.chaddysroom.vloggingapp.classes.Effect

class EffectRecyclerViewAdapter(
    private var effects: LinkedList<Effect?>,
    private val mContext: Context?,
    private val mRecyclerView: RecyclerView
) : RecyclerView.Adapter<EffectRecyclerViewAdapter.ViewHolder>() {
    var isPhoto = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 1){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_effect, parent, false)
            view.setOnClickListener {
                val position = mRecyclerView.getChildLayoutPosition(view)
                Toast.makeText(this.mContext, effects.get(position)!!.name, Toast.LENGTH_SHORT).show()
            }
            return ViewHolder(view)
        }
        else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_effect_video, parent, false)
            view.setOnClickListener {
                val position = mRecyclerView.getChildLayoutPosition(view)
                Toast.makeText(this.mContext, effects.get(position)!!.name, Toast.LENGTH_SHORT).show()
            }
            return ViewHolder(view)
        }


    }

    fun setFilterType(effects: LinkedList<Effect?>, isPhoto: Boolean){
        this.effects = effects
        this.setState(isPhoto)

    }

    private fun setState(isPhoto: Boolean){
        this.isPhoto = isPhoto
//        Log.e("hahaha_size",this.effects.size.toString())
        notifyItemRangeChanged(0, this.effects.size)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        if(isPhoto)
            return 1
        else
            return 0
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val options = RequestOptions()
//        if (!isPhoto){
////            Log.e("hahaha", position.toString())
//        }


        Glide.with(mContext!!)
            .load(this.effects[position]!!.thumbnail)
            .apply(options.fitCenter())
            .into(holder.effectThumbnail)

        holder.effect_name.text = effects[position]!!.name
        holder.effect_description.text = effects[position]!!.description

    }

    override fun getItemCount(): Int {
        return effects.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var effectThumbnail: ImageView = itemView.findViewById(R.id.effect_thumbnail)
        internal var effect_name: TextView = itemView.findViewById<TextView>(R.id.effect_name)
        internal var effect_description: TextView = itemView.findViewById<TextView>(R.id.effect_description)
    }


}