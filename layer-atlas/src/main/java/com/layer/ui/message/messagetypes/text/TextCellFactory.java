package com.layer.ui.message.messagetypes.text;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.layer.sdk.LayerClient;
import com.layer.sdk.listeners.LayerProgressListener;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.ui.R;
import com.layer.ui.databinding.UiMessageItemCellTextBinding;
import com.layer.ui.message.messagetypes.CellFactory;
import com.layer.ui.message.messagetypes.MessageStyle;
import com.layer.ui.util.Log;
import com.layer.ui.util.Util;

import java.util.Map;
import java.util.WeakHashMap;

public class TextCellFactory extends
        CellFactory<TextCellFactory.CellHolder, TextCellFactory.TextInfo> implements View.OnLongClickListener {
    public final static String MIME_TYPE = "text/plain";
    //This is used to bind TextView  to the exact message to ensure the right TextView is updated
    private Map<TextView, Uri> mTextViewUriHashMap =  new WeakHashMap<>();

    public TextCellFactory() {
        super(256 * 1024);
    }

    @Override
    public boolean isBindable(Message message) {
        return isType(message);
    }

    @Override
    public CellHolder createCellHolder(ViewGroup cellView, boolean isMe, LayoutInflater layoutInflater) {

        UiMessageItemCellTextBinding uiMessageItemCellTextBinding = UiMessageItemCellTextBinding
                .inflate(layoutInflater, cellView, true);

        TextCellFactoryViewModel textCellFactoryViewModel = new TextCellFactoryViewModel(mMessageStyle, isMe);
        uiMessageItemCellTextBinding.setViewModel(textCellFactoryViewModel);
        return new CellHolder(uiMessageItemCellTextBinding, textCellFactoryViewModel);
    }

    @Override
    public TextInfo parseContent(LayerClient layerClient, Message message) {
        MessagePart part = message.getMessageParts().get(0);
        String text = part.isContentReady() ? new String(part.getData()) : null;
        String name;
        Identity sender = message.getSender();
        if (sender != null) {
            name = Util.getDisplayName(sender) + ": ";
        } else {
            name = "";
        }
        return new TextInfo(text, name);
    }

    @Override
    public void bindCellHolder(CellHolder cellHolder, final TextInfo parsed, Message message, CellHolderSpecs specs) {

        //Checking if the TextView is being recycled, replace the value in the map with the new message id
        if (mTextViewUriHashMap.containsKey(cellHolder.mTextView)) {
            mTextViewUriHashMap.put(cellHolder.mTextView, message.getId());
            cellHolder.mProgressBar.hide();
        }

        String textMessage = parsed.getString();
        //This string will be null if the message part content is not Ready
        if (textMessage == null) {
            if (message.getMessageParts().get(0).isContentReady()) {
                textMessage = new String(message.getMessageParts().get(0).getData());
            } else {
                downloadMessage(message, cellHolder, parsed);
                cellHolder.bind(null, null, true, null);
            }
        }

        cellHolder.bind(textMessage, parsed, false, this);
    }

    private void downloadMessage(final Message message, final CellHolder cellHolder, final TextInfo parsed) {
        final MessagePart part = message.getMessageParts().get(0);
        final TextView textView = cellHolder.mTextView;
        mTextViewUriHashMap.put(textView, message.getId());
        LayerProgressListener layerProgressListener = new LayerProgressListener.Weak() {
            @Override
            public void onProgressStart(MessagePart messagePart, Operation operation) {}

            @Override
            public void onProgressUpdate(MessagePart messagePart, Operation operation, long l) {}

            @Override
            public void onProgressComplete(MessagePart messagePart, Operation operation) {
                //Check the downloaded message to ensure the TextView has not been recycled
                Uri messageId = messagePart.getMessage().getId();
                Uri uriValueInMap = mTextViewUriHashMap.get(textView);
                if (uriValueInMap != null && uriValueInMap.equals(messageId) ) {
                    mTextViewUriHashMap.remove(textView);
                    cellHolder.bind(new String(part.getData()), parsed, false, TextCellFactory.this);
                }
            }

            @Override
            public void onProgressError(MessagePart messagePart, Operation operation, Throwable throwable) {
                mTextViewUriHashMap.remove(textView);
                cellHolder.bind(null, null, false, TextCellFactory.this);

                if (Log.isLoggable(Log.ERROR)) {
                    Log.e("Message part download error: " + messagePart.getId(), throwable);
                }
            }
        };
        part.download(layerProgressListener);
    }

    public boolean isType(Message message) {
        return message.getMessageParts().size() == 1 &&  message.getMessageParts().get(0).getMimeType().equals(MIME_TYPE);
    }

    @Override
    public String getPreviewText(Context context, Message message) {
        if (isType(message)) {
            MessagePart part = message.getMessageParts().get(0);
            // For large text content, the MessagePart may not be downloaded yet.
            return part.isContentReady() ? new String(part.getData()) : "";
        }
        else {
            throw new IllegalArgumentException("Message is not of the correct type - Text");
        }
    }

    /**
     * Long click copies message text and sender name to clipboard
     */
    @Override
    public boolean onLongClick(View v) {
        TextInfo parsed = (TextInfo) v.getTag();
        String text = parsed.getClipboardPrefix() + parsed.getString();
        Util.copyToClipboard(v.getContext(), R.string.layer_ui_text_cell_factory_clipboard_description, text);
        Toast.makeText(v.getContext(), R.string.layer_ui_text_cell_factory_copied_to_clipboard, Toast.LENGTH_SHORT).show();
        return true;
    }

    public static class CellHolder extends CellFactory.CellHolder {
        UiMessageItemCellTextBinding mUiMessageItemCellTextBinding;
        TextCellFactoryViewModel mTextCellFactoryViewModel;
        boolean isMe;
        public TextView mTextView;
        public ContentLoadingProgressBar mProgressBar;
        public MessageStyle mMessageStyle;

        public CellHolder(UiMessageItemCellTextBinding uiMessageItemCellTextBinding, TextCellFactoryViewModel textCellFactoryViewModel) {
            mTextCellFactoryViewModel = textCellFactoryViewModel;
            mUiMessageItemCellTextBinding = uiMessageItemCellTextBinding;
            mTextView = uiMessageItemCellTextBinding.cellText;
            mProgressBar = uiMessageItemCellTextBinding.textCellProgress;
            isMe = mTextCellFactoryViewModel.isMyMessage();
            mMessageStyle = mTextCellFactoryViewModel.getMessageStyle();
        }

        public void bind(String textMessage, TextInfo parsed, boolean shouldProgressBarBeVisible, View.OnLongClickListener listener) {
            if (textMessage == null) {
                if (shouldProgressBarBeVisible) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.show();
                } else {
                    mProgressBar.hide();
                }
            } else {

                mTextCellFactoryViewModel.setText(textMessage);
                View v = mUiMessageItemCellTextBinding.getRoot();
                TextView textView = mUiMessageItemCellTextBinding.cellText;
                textView.setTypeface(isMe ? mMessageStyle.getMyTextTypeface() :
                        mMessageStyle.getOtherTextTypeface(), isMe ? mMessageStyle.getMyTextStyle()
                        : mMessageStyle.getOtherTextStyle());
                textView.setTag(parsed);
                textView.setOnLongClickListener(listener);
                v.setBackgroundResource(isMe ? R.drawable.ui_message_item_cell_me : R.drawable.ui_message_item_cell_them);
                ((GradientDrawable) v.getBackground()).setColor(isMe ? mMessageStyle.getMyBubbleColor() : mMessageStyle.getOtherBubbleColor());

                mTextCellFactoryViewModel.setTextColor(isMe ? mMessageStyle.getMyTextColor() : mMessageStyle.getOtherTextColor());
                mTextCellFactoryViewModel.setLinkTextColor(isMe ? mMessageStyle.getMyTextColor() : mMessageStyle.getOtherTextColor());
                mUiMessageItemCellTextBinding.notifyChange();
                mUiMessageItemCellTextBinding.setViewModel(mTextCellFactoryViewModel);
            }

        }
    }

    public static class TextInfo implements CellFactory.ParsedContent {
        private final String mString;
        private final String mClipboardPrefix;
        private final int mSize;

        public TextInfo(String string, String clipboardPrefix) {
            mString = string;
            mClipboardPrefix = clipboardPrefix;
            int clipboardLength = mClipboardPrefix.getBytes().length;
            mSize = (mString != null) ? mString.getBytes().length + clipboardLength : clipboardLength;
        }

        public String getString() {
            return mString;
        }

        public String getClipboardPrefix() {
            return mClipboardPrefix;
        }

        @Override
        public int sizeOf() {
            return mSize;
        }
    }
}