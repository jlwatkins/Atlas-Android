package com.layer.ui.conversation;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Message;
import com.layer.ui.message.MessageItemsListViewModel;
import com.layer.ui.message.messagetypes.CellFactory;
import com.layer.ui.util.DateFormatter;
import com.layer.ui.util.imagecache.ImageCacheWrapper;
import com.layer.ui.util.views.SwipeableItem;

import java.util.List;

public class ConversationViewModel extends BaseObservable {
    protected Conversation mConversation;
    protected MessageItemsListViewModel mMessageItemsListViewModel;
    protected LayerClient mLayerClient;

    public ConversationViewModel(Context context, LayerClient layerClient, List<CellFactory> cellFactories,
                                 ImageCacheWrapper imageCacheWrapper, DateFormatter dateFormatter,
                                 SwipeableItem.OnItemSwipeListener<Message> onItemSwipeListener) {
        mMessageItemsListViewModel = new MessageItemsListViewModel(context, layerClient, imageCacheWrapper, dateFormatter);
        mMessageItemsListViewModel.setCellFactories(cellFactories);
        mMessageItemsListViewModel.setOnItemSwipeListener(onItemSwipeListener);
        mLayerClient = layerClient;
    }

    public void setConversation(Conversation conversation) {
        mConversation = conversation;
        notifyChange();
    }

    @Bindable
    public LayerClient getLayerClient() {
        return mLayerClient;
    }

    @Bindable
    public Conversation getConversation() {
        return mConversation;
    }

    public MessageItemsListViewModel getMessageItemsListViewModel() {
        return mMessageItemsListViewModel;
    }
}
