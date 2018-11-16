package com.android.kalina.ui.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.android.kalina.api.util.getTimeFormatString
import com.android.kalina.api.util.setVisible
import com.android.kalina.database.message.Message
import kotlinx.android.synthetic.main.v_out_text_message_item.view.*

class TextMessageItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(message: Message) {
        itemView.textTextView.text = message.text
        itemView.timeTextView.text = message.getCreateTimeInMilliseconds().getTimeFormatString()
        itemView.progressImageView?.setVisible(message.isUnsent())
    }
}