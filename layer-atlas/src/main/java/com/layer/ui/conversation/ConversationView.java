package com.layer.ui.conversation;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.ui.TypingIndicatorLayout;
import com.layer.ui.composebar.ComposeBar;
import com.layer.ui.databinding.UiConversationViewBinding;
import com.layer.ui.message.MessageItemsListView;
import com.layer.ui.message.MessageItemsListViewModel;
import com.layer.ui.typingindicators.BubbleTypingIndicatorFactory;

import java.util.Set;

public class ConversationView extends ConstraintLayout {

    protected LayerClient mLayerClient;
    protected MessageItemsListView mMessageItemListView;
    protected ComposeBar mComposeBar;
    protected TypingIndicatorLayout mTypingIndicator;

    protected Conversation mConversation;
    protected UiConversationViewBinding mBinding;

    public ConversationView(Context context) {
        this(context, null);
    }

    public ConversationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConversationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBinding = UiConversationViewBinding.inflate(LayoutInflater.from(context), this, true);

        mMessageItemListView = mBinding.messagesList;
        mComposeBar = mBinding.composeBar;

        mTypingIndicator = new TypingIndicatorLayout(context);
        mTypingIndicator.setTypingIndicatorFactory(new BubbleTypingIndicatorFactory());
        mTypingIndicator.setTypingActivityListener(new TypingIndicatorLayout.TypingActivityListener() {
            @Override
            public void onTypingActivityChange(TypingIndicatorLayout typingIndicator, boolean active, Set<Identity> users) {
                mMessageItemListView.setFooterView(active ? typingIndicator : null, users);
            }
        });
    }

    public void setLayerClient(LayerClient layerClient) {
        mLayerClient = layerClient;
    }

    public void setMessageItemsListViewModel(MessageItemsListViewModel viewModel) {
        mBinding.setViewModel(viewModel);
        mBinding.executePendingBindings();
    }

    public void setConversation(Conversation conversation) {
        mConversation = conversation;
        mMessageItemListView.setConversation(mLayerClient, conversation);
        mComposeBar.setConversation(mLayerClient, conversation);
        mTypingIndicator.setConversation(mLayerClient, conversation);
    }

    public MessageItemsListView getMessageItemListView() {
        return mMessageItemListView;
    }

    public ComposeBar getComposeBar() {
        return mComposeBar;
    }

    public TypingIndicatorLayout getTypingIndicator() {
        return mTypingIndicator;
    }

    public void setTypingIndicator(TypingIndicatorLayout typingIndicator) {
        mTypingIndicator = typingIndicator;
    }

    public void onDestroy() {
        mMessageItemListView.onDestroy();
    }
}
