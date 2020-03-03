package com.chaddysroom.vloggingapp.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.chaddysroom.vloggingapp.R
import com.chaddysroom.vloggingapp.adapters.BackPressInterface
import com.chaddysroom.vloggingapp.adapters.EffectRecyclerViewAdapter
import com.chaddysroom.vloggingapp.classes.Effect
import com.chaddysroom.vloggingapp.utils.MovableFloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.util.Log
import android.view.InflateException


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [EffectsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [EffectsFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class EffectsFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private var PHOTO_EFFECTS = LinkedList<Effect>()
    private var VIDEO_EFFECTS = LinkedList<Effect>()
    lateinit private var effectRecyclerView: RecyclerView
    lateinit private var effectRecyclerViewAdapter: EffectRecyclerViewAdapter
    private var isPhoto = true

    private fun initButtons(view: View?) {
        val photo_effect = view!!.findViewById<TextView>(R.id.photo_effect)
        photo_effect.setOnClickListener {
            if (isPhoto) {
                isPhoto = false
                effectRecyclerViewAdapter.isPhoto = isPhoto
            }
        }

        val video_effect = view!!.findViewById<TextView>(R.id.video_effect)
        photo_effect.setOnClickListener {
            if (!isPhoto) {
                isPhoto = true
                effectRecyclerViewAdapter.isPhoto = isPhoto
            }
        }


    }

    private fun initEffects(){
        val bm_camera = BitmapFactory.decodeResource(context!!.resources, R.drawable.logo_character)
        val bm_video = BitmapFactory.decodeResource(context!!.resources, R.drawable.logo_character)
        PHOTO_EFFECTS.apply {
            add(Effect(0, bm_camera))
            add(Effect(1, bm_camera))
            add(Effect(2, bm_camera))
            add(Effect(3, bm_camera))
            add(Effect(4, bm_camera))
            add(Effect(5, bm_camera))
            add(Effect(6, bm_camera))
        }

        VIDEO_EFFECTS.apply {
            add(Effect(0, bm_video))
            add(Effect(1, bm_video))
            add(Effect(2, bm_video))
            add(Effect(3, bm_video))
            add(Effect(4, bm_video))
            add(Effect(5, bm_video))
            add(Effect(6, bm_video))
        }
    }

    private fun initUI(view: View?) {
        effectRecyclerView = view!!.findViewById(R.id.effect_list)
        effectRecyclerViewAdapter = EffectRecyclerViewAdapter(PHOTO_EFFECTS, VIDEO_EFFECTS, this.context)
        effectRecyclerView.adapter = effectRecyclerViewAdapter
        effectRecyclerView.layoutManager =
            LinearLayoutManager(activity!!.applicationContext, LinearLayoutManager.VERTICAL, false)
    }

    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this
        initEffects()
        val rootView = inflater.inflate(R.layout.fragment_effects, container, false)
        initUI(view = rootView)

        Log.i("FRAG", "UI CREATEDR")
        return rootView
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        fun onFragmentInteraction() {

        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EffectsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EffectsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
