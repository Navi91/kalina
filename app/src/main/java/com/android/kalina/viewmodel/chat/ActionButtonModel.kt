package com.android.kalina.viewmodel.chat

class ActionButtonModel(val state: State) {

    fun setState(state: State) = ActionButtonModel(state)

    enum class State {
        TEXT, AUDIO
    }
}