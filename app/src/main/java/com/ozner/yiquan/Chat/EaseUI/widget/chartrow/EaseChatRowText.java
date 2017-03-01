package com.ozner.yiquan.Chat.EaseUI.widget.chartrow;

import android.content.Context;
import android.text.Spannable;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ozner.yiquan.Chat.EaseUI.model.MessageDirect;
import com.ozner.yiquan.Chat.EaseUI.model.MessageStatus;
import com.ozner.yiquan.Chat.EaseUI.utils.EaseSmileUtils;
import com.ozner.yiquan.DBHelper.EMMessage;
import com.ozner.yiquan.R;


public class EaseChatRowText extends EaseChatRow {

    private TextView contentView;

    public EaseChatRowText(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(message.getMDirect() == MessageDirect.RECEIVE ?
                R.layout.ease_row_received_message : R.layout.ease_row_sent_message, this);
    }

    @Override
    protected void onFindViewById() {
        contentView = (TextView) findViewById(R.id.tv_chatcontent);
    }

    @Override
    public void onSetUpView() {
//        EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
        Spannable span = EaseSmileUtils.getSmiledText(context, message.getContent());
        // 设置内容
        contentView.setText(span, TextView.BufferType.SPANNABLE);

        handleTextMessage();
    }

    protected void handleTextMessage() {
        if (message.getMDirect() == MessageDirect.SEND) {
            setMessageSendCallback();
            switch (message.getStatus()) {
                case MessageStatus.CREATE:
                    progressBar.setVisibility(View.GONE);
                    statusView.setVisibility(View.VISIBLE);
                    break;
                case MessageStatus.SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    statusView.setVisibility(View.GONE);
                    break;
                case MessageStatus.FAIL:
                    progressBar.setVisibility(View.GONE);
                    statusView.setVisibility(View.VISIBLE);
                    break;
                case MessageStatus.INPROGRESS:
                    progressBar.setVisibility(View.VISIBLE);
                    statusView.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        } else {
//            if (!message.isAcked() && message.getChatType() == ChatType.Chat) {
//                try {
//                    EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
//                } catch (HyphenateException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }

    @Override
    protected void onUpdateView() {
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onBubbleClick() {
        // TODO Auto-generated method stub

    }


}
