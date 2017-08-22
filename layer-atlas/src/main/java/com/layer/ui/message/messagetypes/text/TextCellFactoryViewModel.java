package com.layer.ui.message.messagetypes.text;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.layer.ui.message.messagetypes.MessageStyle;


public class TextCellFactoryViewModel extends BaseObservable {

    private MessageStyle mMessageStyle;
    private boolean mIsMe;
    private String mText;
    private int mTextColor;
    private int mLinkTextColor;

    public TextCellFactoryViewModel(MessageStyle messageStyle, boolean isMe) {
        mMessageStyle = messageStyle;
        mIsMe = isMe;
    }

    public boolean isMyMessage() {
        return mIsMe;
    }

    public void setText(String text) {
        mText = text;
    }

    public MessageStyle getMessageStyle() {
        return mMessageStyle;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    public void setLinkTextColor(int linkTextColor) {
        mLinkTextColor = linkTextColor;
    }

    @Bindable
    public String getText() {
        return mText;
    }

    @Bindable
    public int getTextColor() {
        return mTextColor;
    }

    @Bindable
    public int getLinkTextColor() {
        return mLinkTextColor;
    }
}
