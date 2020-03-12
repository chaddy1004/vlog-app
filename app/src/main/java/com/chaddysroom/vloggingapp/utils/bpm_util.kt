package com.chaddysroom.vloggingapp.utils

import com.chaddysroom.vloggingapp.R


class BPMManager() {
    private var iteration = 1
    private var bpm = 0L // Not actually bpm, but the delay between taps
    private var timeAccumulated = 0L

    fun iterateCounter() {
        iteration++
    }

    fun getIteration(): Int {
        return this.iteration
    }

    fun calculateBPM() {
        this.bpm = timeAccumulated / 3
    }

    fun getBPM(): Long{
        return this.bpm
    }

    fun clear(){
        this.iteration = 1
        this.bpm = 0L
        this.timeAccumulated = 0L
    }

    fun addTime(time: Long) {
        timeAccumulated = timeAccumulated + time
    }


}