package com.layer.ui.message;

import android.content.Context;
import android.util.AttributeSet;

import com.layer.sdk.messaging.Message;
import com.layer.ui.recyclerview.ItemsRecyclerView;

public class MessagesRecyclerView extends ItemsRecyclerView<Message> {

    public MessagesRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MessagesRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessagesRecyclerView(Context context) {
        super(context);
    }

}