/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ozner.cup.Chat.EaseUI.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.ozner.cup.Chat.EaseUI.model.MessageDirect;
import com.ozner.cup.Chat.EaseUI.model.MessageType;
import com.ozner.cup.Chat.EaseUI.widget.EaseChatMessageList;
import com.ozner.cup.Chat.EaseUI.widget.chartrow.EaseChatRow;
import com.ozner.cup.Chat.EaseUI.widget.chartrow.EaseChatRowImage;
import com.ozner.cup.Chat.EaseUI.widget.chartrow.EaseChatRowText;
import com.ozner.cup.Chat.EaseUI.widget.chartrow.EaseCustomChatRowProvider;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.EMMessage;

import java.util.List;

import static com.ozner.cup.Chat.EaseUI.model.MessageType.FILE;
import static com.ozner.cup.Chat.EaseUI.model.MessageType.IMAGE;
import static com.ozner.cup.Chat.EaseUI.model.MessageType.VIDEO;
import static com.ozner.cup.Chat.EaseUI.model.MessageType.VOICE;

public class EaseMessageAdapter extends BaseAdapter {

    private final static String TAG = "msg";

    private Context context;

    private static final int HANDLER_MESSAGE_REFRESH_LIST = 0;
    private static final int HANDLER_MESSAGE_SELECT_LAST = 1;
    private static final int HANDLER_MESSAGE_SEEK_TO = 2;
    private static final int HANDLER_REFRESH_RESULT = 3;

    private static final int MESSAGE_TYPE_RECV_TXT = 0;
    private static final int MESSAGE_TYPE_SENT_TXT = 1;
    private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
    private static final int MESSAGE_TYPE_SENT_LOCATION = 3;
    private static final int MESSAGE_TYPE_RECV_LOCATION = 4;
    private static final int MESSAGE_TYPE_RECV_IMAGE = 5;
    private static final int MESSAGE_TYPE_SENT_VOICE = 6;
    private static final int MESSAGE_TYPE_RECV_VOICE = 7;
    private static final int MESSAGE_TYPE_SENT_VIDEO = 8;
    private static final int MESSAGE_TYPE_RECV_VIDEO = 9;
    private static final int MESSAGE_TYPE_SENT_FILE = 10;
    private static final int MESSAGE_TYPE_RECV_FILE = 11;
    private static final int MESSAGE_TYPE_SENT_EXPRESSION = 12;
    private static final int MESSAGE_TYPE_RECV_EXPRESSION = 13;


    public int itemTypeCount;

    // reference to conversation object in chatsdk
//	private EMConversation conversation;
    EMMessage[] messages = null;

    private String toChatUserid;

    private EaseChatMessageList.MessageListItemClickListener itemClickListener;
    private EaseCustomChatRowProvider customRowProvider;

    private boolean showUserNick;
    private boolean showAvatar;
    private Drawable myBubbleBg;
    private Drawable otherBuddleBg;

    private ListView listView;

    public EaseMessageAdapter(Context context, String userid, ListView listView) {
        this.context = context;
        this.listView = listView;
        toChatUserid = userid;
//		this.conversation = EMClient.getInstance().chatManager().getConversation(username, EaseCommonUtils.getConversationType(chatType), true);
    }

    Handler handler = new Handler() {
//		private void refreshList() {
//			// you should not call getAllMessages() in UI thread
//			// otherwise there is problem when refreshing UI and there is new message arrive
//			java.util.List<EMMessage> var = conversation.getAllMessages();
//			messages = var.toArray(new EMMessage[var.size()]);
//			conversation.markAllMessagesAsRead();
//			notifyDataSetChanged();
//		}

        @Override
        public void handleMessage(android.os.Message message) {
            switch (message.what) {
                case HANDLER_MESSAGE_REFRESH_LIST:
                    refreshList();
                    break;
                case HANDLER_MESSAGE_SELECT_LAST:
                    if (messages != null && messages.length > 0) {
                        listView.setSelection(messages.length - 1);
                    }
                    break;
                case HANDLER_MESSAGE_SEEK_TO:
                    int position = message.arg1;
                    listView.setSelection(position);
                    break;
                case HANDLER_REFRESH_RESULT:
                    List<EMMessage> msgList = (List<EMMessage>) message.obj;
                    messages = msgList.toArray(new EMMessage[msgList.size()]);
                    notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 刷新列表,执行代码
     */
    private void refreshList() {
        if (toChatUserid != null && !toChatUserid.isEmpty()) {
//            synchronized (EMMessageDao.class) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<EMMessage> msgList = DBManager.getInstance(context).getAllChatMessage(toChatUserid);
                    if (msgList != null) {
                        handler.removeMessages(HANDLER_REFRESH_RESULT);
                        Message message = handler.obtainMessage(HANDLER_REFRESH_RESULT);
                        message.obj = msgList;
                        handler.sendMessage(message);
                    }
                }
            }).start();
//            }
        } else {
            Log.e(TAG, "refreshList: userid 为空");
        }
        // you should not call getAllMessages() in UI thread
        // otherwise there is problem when refreshing UI and there is new message arrive
//		java.util.List<EMMessage> var = conversation.getAllMessages();
//		messages = var.toArray(new EMMessage[var.size()]);
//		conversation.markAllMessagesAsRead();
//		notifyDataSetChanged();
    }

    /**
     * 发送刷新列表命令
     */
    public void refresh() {
        if (handler.hasMessages(HANDLER_MESSAGE_REFRESH_LIST)) {
            return;
        }
        android.os.Message msg = handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST);
        handler.sendMessage(msg);
    }

    /**
     * refresh and select the last
     */
    public void refreshSelectLast() {
        final int TIME_DELAY_REFRESH_SELECT_LAST = 100;
        handler.removeMessages(HANDLER_MESSAGE_REFRESH_LIST);
        handler.removeMessages(HANDLER_MESSAGE_SELECT_LAST);
        handler.sendEmptyMessageDelayed(HANDLER_MESSAGE_REFRESH_LIST, TIME_DELAY_REFRESH_SELECT_LAST / 2);
        handler.sendEmptyMessageDelayed(HANDLER_MESSAGE_SELECT_LAST, TIME_DELAY_REFRESH_SELECT_LAST);
    }

    /**
     * refresh and seek to the position
     */
    public void refreshSeekTo(int position) {
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
        android.os.Message msg = handler.obtainMessage(HANDLER_MESSAGE_SEEK_TO);
        msg.arg1 = position;
        handler.sendMessage(msg);
    }


    public EMMessage getItem(int position) {
        if (messages != null && position < messages.length) {
            return messages[position];
        }
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * get count of messages
     */
    public int getCount() {
        return messages == null ? 0 : messages.length;
    }

    /**
     * get number of message type, here 14 = (MessageType) * 2
     */
    public int getViewTypeCount() {
        if (customRowProvider != null && customRowProvider.getCustomChatRowTypeCount() > 0) {
            return customRowProvider.getCustomChatRowTypeCount() + 14;
        }
        return 14;
    }


    /**
     * get type of item
     */
    public int getItemViewType(int position) {
        EMMessage message = getItem(position);
        if (message == null) {
            return -1;
        }

        if (customRowProvider != null && customRowProvider.getCustomChatRowType(message) > 0) {
            return customRowProvider.getCustomChatRowType(message) + 13;
        }

        if (message.getMType() == MessageType.TXT) {
//		    if(message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)){
//		        return message.getMDirect() == MessageDirect.RECEIVE ? MESSAGE_TYPE_RECV_EXPRESSION : MESSAGE_TYPE_SENT_EXPRESSION;
//		    }
            return message.getMDirect() == MessageDirect.RECEIVE ? MESSAGE_TYPE_RECV_TXT : MESSAGE_TYPE_SENT_TXT;
        }
        if (message.getMType() == IMAGE) {
            return message.getMDirect() == MessageDirect.RECEIVE ? MESSAGE_TYPE_RECV_IMAGE : MESSAGE_TYPE_SENT_IMAGE;

        }
        if (message.getMType() == MessageType.LOCATION) {
            return message.getMDirect() == MessageDirect.RECEIVE ? MESSAGE_TYPE_RECV_LOCATION : MESSAGE_TYPE_SENT_LOCATION;
        }
        if (message.getMType() == VOICE) {
            return message.getMDirect() == MessageDirect.RECEIVE ? MESSAGE_TYPE_RECV_VOICE : MESSAGE_TYPE_SENT_VOICE;
        }
        if (message.getMType() == VIDEO) {
            return message.getMDirect() == MessageDirect.RECEIVE ? MESSAGE_TYPE_RECV_VIDEO : MESSAGE_TYPE_SENT_VIDEO;
        }
        if (message.getMType() == FILE) {
            return message.getMDirect() == MessageDirect.RECEIVE ? MESSAGE_TYPE_RECV_FILE : MESSAGE_TYPE_SENT_FILE;
        }

        return -1;// invalid
    }

    protected EaseChatRow createChatRow(Context context, EMMessage message, int position) {
        EaseChatRow chatRow = null;
        if (customRowProvider != null && customRowProvider.getCustomChatRow(message, position, this) != null) {
            return customRowProvider.getCustomChatRow(message, position, this);
        }
        switch (message.getMType()) {
            case MessageType.TXT:
                chatRow = new EaseChatRowText(context, message, position, this);
                break;
            case MessageType.IMAGE:
            chatRow = new EaseChatRowImage(context, message, position, this);
                break;
            default:
                chatRow = new EaseChatRowText(context, message, position, this);
                break;
        }

        return chatRow;
    }


    @SuppressLint("NewApi")
    public View getView(final int position, View convertView, ViewGroup parent) {
        EMMessage message = getItem(position);
        if (convertView == null) {
            convertView = createChatRow(context, message, position);
        }

        //refresh ui with messages
        ((EaseChatRow) convertView).setUpView(message, position, itemClickListener);

        return convertView;
    }


    public String getToChatUserId() {
        return toChatUserid;
    }


    public void setShowUserNick(boolean showUserNick) {
        this.showUserNick = showUserNick;
    }


    public void setShowAvatar(boolean showAvatar) {
        this.showAvatar = showAvatar;
    }


    public void setMyBubbleBg(Drawable myBubbleBg) {
        this.myBubbleBg = myBubbleBg;
    }


    public void setOtherBuddleBg(Drawable otherBuddleBg) {
        this.otherBuddleBg = otherBuddleBg;
    }


    public void setItemClickListener(EaseChatMessageList.MessageListItemClickListener listener) {
        itemClickListener = listener;
    }

    public void setCustomChatRowProvider(EaseCustomChatRowProvider rowProvider) {
        customRowProvider = rowProvider;
    }


    public boolean isShowUserNick() {
        return showUserNick;
    }


    public boolean isShowAvatar() {
        return showAvatar;
    }


    public Drawable getMyBubbleBg() {
        return myBubbleBg;
    }


    public Drawable getOtherBuddleBg() {
        return otherBuddleBg;
    }

}
