package com.chaddysroom.vloggingapp.adapters

import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
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
import com.chaddysroom.vloggingapp.activity.MainActivity
import com.chaddysroom.vloggingapp.classes.Effect

class EffectRecyclerViewAdapter(
    private var effects: LinkedList<Effect?>,
    private val mContext: Context?,
    private val mRecyclerView: RecyclerView,
    private val mFragment: Fragment
) : RecyclerView.Adapter<EffectRecyclerViewAdapter.ViewHolder>() {
    var isPhoto = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_effect, parent, false)
        view.setOnClickListener {
            val position = mRecyclerView.getChildLayoutPosition(view)
            Toast.makeText(this.mContext, effects.get(position)!!.name, Toast.LENGTH_SHORT).show()
            val mainActivity = mFragment.activity as MainActivity
            mainActivity.setState(true)
            mainActivity.setEffectState(position)
            // sets "isPhoto" state
            mainActivity.onBackPressed()
        }
        return ViewHolder(view)
//        if (viewType == 1) {
//            val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_effect, parent, false)
//            view.setOnClickListener {
//                val position = mRecyclerView.getChildLayoutPosition(view)
//                Toast.makeText(this.mContext, effects.get(position)!!.name, Toast.LENGTH_SHORT).show()
//                val mainActivity = mFragment.activity as MainActivity
//                mainActivity.setState(true)
//                mainActivity.setEffectState(position)
//                 // sets "isPhoto" state
//                mainActivity.onBackPressed()
//            }
//            return ViewHolder(view)
//        } else {
//            val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem_effect_video, parent, false)
//            view.setOnClickListener {
//                val position = mRecyclerView.getChildLayoutPosition(view)
//                Toast.makeText(this.mContext, effects.get(position)!!.name, Toast.LENGTH_SHORT).show()
//                val mainActivity = mFragment.activity as MainActivity
////                mainActivity.EFFECT_STATE = position
//                mainActivity.setState(false)
//                mainActivity.setEffectState(position)
//                // sets "isPhoto" state to false
//                mainActivity.onBackPressed()
//            }
//            return ViewHolder(view)
//        }


    }

    fun setFilterType(effects: LinkedList<Effect?>, isPhoto: Boolean) {
        this.effects = effects
        this.isPhoto = isPhoto
        notifyItemRangeChanged(0, this.effects.size)
        notifyDataSetChanged()
    }


//    override fun getItemViewType(position: Int): Int {
//        if (isPhoto)
//            return 1
//        else
//            return 0
//    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val options = RequestOptions()
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