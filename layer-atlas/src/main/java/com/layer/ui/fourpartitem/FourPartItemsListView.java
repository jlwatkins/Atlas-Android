package com.layer.ui.fourpartitem;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.layer.sdk.query.Queryable;
import com.layer.ui.R;
import com.layer.ui.adapters.ItemRecyclerViewAdapter;
import com.layer.ui.recyclerview.ItemsRecyclerView;
import com.layer.ui.style.FourPartItemStyle;
import com.layer.ui.util.views.SwipeableItem;

import java.util.List;

public abstract class FourPartItemsListView<ITEM extends Queryable, ADAPTER extends ItemRecyclerViewAdapter> extends ConstraintLayout {

    protected FourPartItemStyle mFourPartItemStyle;
    protected ItemsRecyclerView<ITEM> mItemsRecyclerView;
    protected LinearLayoutManager mLinearLayoutManager;
    protected boolean shouldStackFromEnd = false;
    protected ADAPTER mAdapter;

    public FourPartItemsListView(Context context) {
        super(context);
        init();
    }

    public FourPartItemsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        mFourPartItemStyle = new FourPartItemStyle(context, attrs, 0);
    }

    protected void init() {
        inflate(getContext(), R.layout.ui_four_part_items_list, this);
        mItemsRecyclerView = (ItemsRecyclerView<ITEM>) findViewById(R.id.ui_items_recycler);

        mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mLinearLayoutManager.setStackFromEnd(shouldStackFromEnd);
        mItemsRecyclerView.setLayoutManager(mLinearLayoutManager);

        DefaultItemAnimator noChangeAnimator = new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull List<Object> payloads) {
                return true;
            }
        };
        noChangeAnimator.setSupportsChangeAnimations(false);
        mItemsRecyclerView.setItemAnimator(noChangeAnimator);
    }

    public void setAdapter(ADAPTER adapter) {
        mAdapter = adapter;
        adapter.setStyle(mFourPartItemStyle);
        mItemsRecyclerView.setAdapter(adapter);
        adapter.refresh();
    }

    public void setItemSwipeListener(SwipeableItem.OnItemSwipeListener<ITEM> itemSwipeListener) {
        mItemsRecyclerView.setItemSwipeListener(itemSwipeListener);
    }

    public void onDestroy() {
        mItemsRecyclerView.onDestroy();
    }
}
