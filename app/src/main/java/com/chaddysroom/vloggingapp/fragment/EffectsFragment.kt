package com.chaddysroom.vloggingapp.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
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
import java.util.*
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast


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
class EffectsFragment : Fragment(), BackPressInterface {
    // TODO: Rename and change types of parameters
    override fun onBackPressed() {

        Toast.makeText(this.context, "back_pressed, fragment", Toast.LENGTH_SHORT).show()
    }
    private var PHOTO_EFFECTS = LinkedList<Effect?>()
    private var VIDEO_EFFECTS = LinkedList<Effect?>()
    private var EFFECTS = LinkedList<Effect?>()
    lateinit private var effectRecyclerView: RecyclerView
    lateinit private var effectRecyclerViewAdapter: EffectRecyclerViewAdapter
    private var isPhoto = false



    private fun initButtons(view: View?) {
        val photo_effect = view!!.findViewById<TextView>(R.id.photo_effect)
        photo_effect.setOnClickListener {
            if (!isPhoto) {
                isPhoto = true
                effectRecyclerViewAdapter.setFilterType(PHOTO_EFFECTS, isPhoto)
            }
        }

        val video_effect = view!!.findViewById<TextView>(R.id.video_effect)
        video_effect.setOnClickListener {
            if (isPhoto) {
                isPhoto = false
                effectRecyclerViewAdapter.setFilterType(VIDEO_EFFECTS, isPhoto)
            }
        }


    }


    private fun initEffects() {
        val bm_camera = BitmapFactory.decodeResource(context!!.resources, R.drawable.logo_character)
        val bm_video = BitmapFactory.decodeResource(context!!.resources, R.drawable.logo_name)

        val effect2 = BitmapFactory.decodeResource(context!!.resources, R.drawable.effect2)
        val effect3 = BitmapFactory.decodeResource(context!!.resources, R.drawable.effect2)
        val effect4 = BitmapFactory.decodeResource(context!!.resources, R.drawable.effect4)
        val effect5 = BitmapFactory.decodeResource(context!!.resources, R.drawable.effect5)
        val effect6 = BitmapFactory.decodeResource(context!!.resources, R.drawable.effect6)
        val effect7 = BitmapFactory.decodeResource(context!!.resources, R.drawable.effect7)
        val effect8 = BitmapFactory.decodeResource(context!!.resources, R.drawable.effect8)
        val effect9 = BitmapFactory.decodeResource(context!!.resources, R.drawable.effect9)
        val effect10 = BitmapFactory.decodeResource(context!!.resources, R.drawable.effect10)
        val effect11 = BitmapFactory.decodeResource(context!!.resources, R.drawable.effect11)

        EFFECTS.apply {
            add(0, Effect(0, getString(R.string.effect_0), getString(R.string.effect_0_description), bm_camera))
            add(1, Effect(0, getString(R.string.effect_1), getString(R.string.effect_1_description), bm_camera))
            add(2, Effect(0, getString(R.string.effect_2), getString(R.string.effect_2_description), effect2))
            add(3, Effect(0, getString(R.string.effect_3), getString(R.string.effect_3_description), effect3))
            add(4, Effect(0, getString(R.string.effect_4), getString(R.string.effect_4_description), effect4))
            add(5, Effect(0, getString(R.string.effect_5), getString(R.string.effect_5_description), effect5))
            add(6, Effect(0, getString(R.string.effect_6), getString(R.string.effect_6_description), effect6))
            add(7, Effect(0, getString(R.string.effect_7), getString(R.string.effect_7_description), effect7))
            add(8, Effect(0, getString(R.string.effect_8), getString(R.string.effect_8_description), effect8))
            add(9, Effect(0, getString(R.string.effect_9), getString(R.string.effect_9_description), effect9))
            add(10, Effect(0, getString(R.string.effect_10), getString(R.string.effect_10_description), effect10))
            add(11, Effect(0, getString(R.string.effect_11), getString(R.string.effect_11_description), effect11))

        }



        PHOTO_EFFECTS.apply {
            add(0, Effect(0, getString(R.string.photo_effect_0), getString(R.string.photo_effect_1_description), bm_camera))
            add(1, Effect(0, getString(R.string.photo_effect_1), getString(R.string.photo_effect_2_description), bm_camera))
            add(2, Effect(0, getString(R.string.photo_effect_2), getString(R.string.photo_effect_3_description), bm_camera))
            add(3, Effect(0, getString(R.string.photo_effect_3), getString(R.string.photo_effect_4_description), bm_camera))
            add(4, Effect(0, getString(R.string.photo_effect_4), getString(R.string.photo_effect_5_description), bm_camera))
            add(5, Effect(0, getString(R.string.photo_effect_5), getString(R.string.photo_effect_6_description), bm_camera))
            add(6, Effect(0, getString(R.string.photo_effect_6), getString(R.string.photo_effect_6_description), bm_camera))
        }

        VIDEO_EFFECTS.apply {
            add(0, Effect(0, getString(R.string.video_effect_1), getString(R.string.video_effect_1_description), bm_video))
            add(1, Effect(0, getString(R.string.video_effect_2), getString(R.string.video_effect_2_description), bm_video))
            add(2, Effect(0, getString(R.string.video_effect_3), getString(R.string.video_effect_3_description), bm_video))
            add(3, Effect(0, getString(R.string.video_effect_4), getString(R.string.video_effect_4_description), bm_video))
            add(4, Effect(0, getString(R.string.video_effect_5), getString(R.string.video_effect_5_description), bm_video))
            add(5, Effect(0, getString(R.string.video_effect_6), getString(R.string.video_effect_6_description), bm_video))
//            set(6, Effect(0, getString(R.string.video_effect_6), getString(R.string.video_effect_6_description), bm_video))
        }
    }



    private fun initUI(view: View?) {
        effectRecyclerView = view!!.findViewById(R.id.effect_list)
        effectRecyclerViewAdapter = EffectRecyclerViewAdapter(VIDEO_EFFECTS, this.context, effectRecyclerView, this)
        effectRecyclerView.adapter = effectRecyclerViewAdapter
        effectRecyclerView.layoutManager =
            LinearLayoutManager(activity!!.applicationContext, LinearLayoutManager.VERTICAL, false)
    }

    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initEffects()

        arguments?.let {
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this
        val rootView = inflater.inflate(R.layout.fragment_effects, container, false)
        initUI(view = rootView)
        initButtons(view = rootView)

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
        val N_EFFECTS_PHOTO = 7
        val N_EFFECTS_VIDEO = 6
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
