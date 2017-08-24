package com.layer.ui.message;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.SortDescriptor;
import com.layer.ui.R;
import com.layer.ui.TypingIndicatorLayout;
import com.layer.ui.message.messagetypes.CellFactory;
import com.layer.ui.message.messagetypes.MessageStyle;
import com.layer.ui.util.views.SwipeableItem;

import java.util.List;

public class MessageItemListView  extends ConstraintLayout {

    protected boolean mShouldShowAvatarsInOneOnOneConversations;
    protected MessageStyle mMessageStyle;
    protected MessagesRecyclerView mMessagesRecyclerView;
    protected LinearLayoutManager mLinearLayoutManager;
    protected MessagesAdapter mAdapter;

    public MessageItemListView(Context context) {
        this(context, null, 0);
    }

    public MessageItemListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageItemListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseStyle(context, attrs, defStyleAttr);

        inflate(getContext(), R.layout.ui_message_items_list, this);
        mMessagesRecyclerView = (MessagesRecyclerView) findViewById(R.id.ui_message_recycler);

        mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessagesRecyclerView.setLayoutManager(mLinearLayoutManager);

        DefaultItemAnimator noChangeAnimator = new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull List<Object> payloads) {
                return true;
            }
        };
        noChangeAnimator.setSupportsChangeAnimations(false);
        mMessagesRecyclerView.setItemAnimator(noChangeAnimator);
    }

    public void setItemSwipeListener(SwipeableItem.OnItemSwipeListener<Message> itemSwipeListener) {
        mMessagesRecyclerView.setItemSwipeListener(itemSwipeListener);
    }

    public void onDestroy() {
        mMessagesRecyclerView.onDestroy();
    }

    public void setAdapter(final MessagesAdapter adapter) {
        adapter.setStyle(mMessageStyle);
        mAdapter = adapter;
        mMessagesRecyclerView.setAdapter(adapter);
        setShouldShowAvatarInOneOnOneConversations(mShouldShowAvatarsInOneOnOneConversations);

        mMessagesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                for (CellFactory factory : adapter.getCellFactories()) {
                    factory.onScrollStateChanged(newState);
                }
            }
        });

        // Create an adapter that auto-scrolls if we're already at the bottom
        adapter.setRecyclerView(mMessagesRecyclerView)
                .setOnMessageAppendListener(new MessagesAdapter.OnMessageAppendListener() {
                    @Override
                    public void onMessageAppend(MessagesAdapter adapter, Message message) {
                        autoScroll();
                    }
                });
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#getShouldShowAvatarInOneOnOneConversations()
     */
    public boolean getShouldShowAvatarInOneOnOneConversations() {
        return mAdapter.getShouldShowAvatarInOneOnOneConversations();
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#setShouldShowAvatarInOneOnOneConversations(boolean)
     */
    public void setShouldShowAvatarInOneOnOneConversations(boolean shouldShowAvatarInOneOnOneConversations) {
        mAdapter.setShouldShowAvatarInOneOnOneConversations(shouldShowAvatarInOneOnOneConversations);
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#addCellFactories(List)
     */
    public void setCellFactories(List<CellFactory> cellFactories) {
        mAdapter.addCellFactories(cellFactories);
    }

    public void setTextTypeface(Typeface myTypeface, Typeface otherTypeface) {
        mMessageStyle.setMyTextTypeface(myTypeface);
        mMessageStyle.setOtherTextTypeface(otherTypeface);
    }

    /**
     * Scrolls if the user is at the end
     */
    private void autoScroll() {
        int end = mAdapter.getItemCount() - 1;
        if (end <= 0) return;
        int visible = findLastVisibleItemPosition();
        // -3 because -1 seems too finicky
        if (visible >= (end - 3)) mMessagesRecyclerView.scrollToPosition(end);
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#setFooterView(View)
     */
    public void setFooterView(View footerView) {
        mAdapter.setFooterView(footerView);
        autoScroll();
    }

    /**
     * Convenience pass-through to this list's LinearLayoutManager.
     *
     * @see LinearLayoutManager#findLastVisibleItemPosition()
     */
    private int findLastVisibleItemPosition() {
        return mLinearLayoutManager.findLastVisibleItemPosition();
    }

    /**
     * Updates the underlying MessagesAdapter with a Query for Messages in the given
     * Conversation.
     *
     * @param conversation Conversation to display Messages for.
     * @return This AtlasMessagesRecyclerView.
     */
    public void setConversation(Conversation conversation) {
        if (conversation != null) {
            mAdapter.setReadReceiptsEnabled(conversation.isReadReceiptsEnabled());
        }
        mAdapter.setQuery(Query.builder(Message.class)
                .predicate(new Predicate(Message.Property.CONVERSATION, Predicate.Operator.EQUAL_TO, conversation))
                .sortDescriptor(new SortDescriptor(Message.Property.POSITION, SortDescriptor.Order.ASCENDING))
                .build()).refresh();
    }

    public void setFooterView(TypingIndicatorLayout footerView) {
        mAdapter.setFooterView(footerView);
    }

    public void parseStyle(Context context, AttributeSet attrs, int defStyle) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageItemListView, R.attr.MessageItemListView, defStyle);
        MessageStyle.Builder messageStyleBuilder = new MessageStyle.Builder();
        messageStyleBuilder.myTextColor(ta.getColor(R.styleable.MessageItemListView_myTextColor, context.getResources().getColor(R.color.layer_ui_text_black)));
        int myTextStyle = ta.getInt(R.styleable.MessageItemListView_myTextStyle, Typeface.NORMAL);
        messageStyleBuilder.myTextStyle(myTextStyle);
        String myTextTypefaceName = ta.getString(R.styleable.MessageItemListView_myTextTypeface);
        messageStyleBuilder.myTextTypeface(myTextTypefaceName != null ? Typeface.create(myTextTypefaceName, myTextStyle) : null);
        messageStyleBuilder.myTextSize(ta.getDimensionPixelSize(R.styleable.MessageItemListView_myTextSize, context.getResources().getDimensionPixelSize(R.dimen.layer_ui_text_size_message_item)));

        messageStyleBuilder.otherTextColor(ta.getColor(R.styleable.MessageItemListView_theirTextColor, context.getResources().getColor(R.color.layer_ui_color_primary_blue)));
        int otherTextStyle = ta.getInt(R.styleable.MessageItemListView_theirTextStyle, Typeface.NORMAL);
        messageStyleBuilder.otherTextStyle(otherTextStyle);
        String otherTextTypefaceName = ta.getString(R.styleable.MessageItemListView_theirTextTypeface);
        messageStyleBuilder.otherTextTypeface(otherTextTypefaceName != null ? Typeface.create(otherTextTypefaceName, otherTextStyle) : null);
        messageStyleBuilder.otherTextSize(ta.getDimensionPixelSize(R.styleable.MessageItemListView_theirTextSize, context.getResources().getDimensionPixelSize(R.dimen.layer_ui_text_size_message_item)));

        messageStyleBuilder.myBubbleColor(ta.getColor(R.styleable.MessageItemListView_myBubbleColor, context.getResources().getColor(R.color.layer_ui_color_primary_blue)));
        messageStyleBuilder.otherBubbleColor(ta.getColor(R.styleable.MessageItemListView_theirBubbleColor, context.getResources().getColor(R.color.layer_ui_color_primary_gray)));

        mShouldShowAvatarsInOneOnOneConversations = ta.getBoolean(R.styleable.MessageItemListView_shouldShowAvatarsInOneOnOneConversations, false);
        ta.recycle();
        mMessageStyle = messageStyleBuilder.build();
    }
}
