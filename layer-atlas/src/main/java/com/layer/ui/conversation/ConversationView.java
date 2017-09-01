package com.layer.ui.conversation;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;

public class ConversationView extends ConstraintLayout {
    public ConversationView(Context context) {
        this(context, null);
    }

    public ConversationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConversationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
