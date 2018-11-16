package com.android.kalina.ui.adapter

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.kalina.R
import com.android.kalina.audiorecord.AudioPlayerManager
import com.android.kalina.database.message.Message
import com.android.kalina.ui.adapter.viewholder.AudioMessageItemViewHolder
import com.android.kalina.ui.adapter.viewholder.TextMessageItemViewHolder

class MessageRecyclerViewAdapter(private val playerManager: AudioPlayerManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val INCOME_TEXT_MESSAGE_ITEM_TYPE = 0
    private val INCOME_AUDIO_MESSAGE_ITEM_TYPE = 1
    private val OUT_TEXT_MESSAGE_ITEM_TYPE = 2
    private val OUT_AUDIO_MESSAGE_ITEM_TYPE = 3

    private var items: List<Message> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            INCOME_TEXT_MESSAGE_ITEM_TYPE -> {
                TextMessageItemViewHolder(createViewHolderView(parent, R.layout.v_income_text_message_item))
            }
            INCOME_AUDIO_MESSAGE_ITEM_TYPE -> {
                AudioMessageItemViewHolder(createViewHolderView(parent, R.layout.v_income_audio_message_item))
            }
            OUT_TEXT_MESSAGE_ITEM_TYPE -> {
                TextMessageItemViewHolder(createViewHolderView(parent, R.layout.v_out_text_message_item))
            }
            else -> {
                AudioMessageItemViewHolder(createViewHolderView(parent, R.layout.v_out_audio_message_item))
            }
        }
    }

    private fun createViewHolderView(parent: ViewGroup, @LayoutRes layoutId: Int) =
            LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is TextMessageItemViewHolder -> holder.bind(item)
            is AudioMessageItemViewHolder -> holder.bind(item, playerManager)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]

        if (item.isIncome()) {
            if (item.isAudio()) {
                return INCOME_AUDIO_MESSAGE_ITEM_TYPE
            } else {
                return INCOME_TEXT_MESSAGE_ITEM_TYPE
            }
        } else {
            if (item.isAudio()) {
                return OUT_AUDIO_MESSAGE_ITEM_TYPE
            } else {
                return OUT_TEXT_MESSAGE_ITEM_TYPE
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        if (holder is AudioMessageItemViewHolder) {
            holder.onRecycled()
        }
    }

    fun setItems(items: List<Message>) {
        this.items = items
    }

    fun getItems() = items

}