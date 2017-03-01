package com.ozner.yiquan.Chat.EaseUI.widget.chartrow;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ozner.yiquan.Chat.BigImageDetailActivity;
import com.ozner.yiquan.Chat.EaseUI.model.MessageDirect;
import com.ozner.yiquan.Chat.EaseUI.model.MessageStatus;
import com.ozner.yiquan.DBHelper.EMMessage;
import com.ozner.yiquan.Main.MainActivity;
import com.ozner.yiquan.R;
import com.ozner.yiquan.Utils.TransitionHelper.TransitionsHeleper;

public class EaseChatRowImage extends EaseChatRow {
//    protected TextView fileNameView;
//    protected TextView fileSizeView;
//    protected TextView fileStateView;

    protected ImageView imageView;
//    private EMImageMessageBody imgBody;

    public EaseChatRowImage(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(message.getMDirect() == MessageDirect.RECEIVE ? R.layout.ease_row_received_picture : R.layout.ease_row_sent_picture, this);
    }

    @Override
    protected void onFindViewById() {
//        fileNameView = (TextView) findViewById(R.id.tv_file_name);
//        fileSizeView = (TextView) findViewById(R.id.tv_file_size);
//        fileStateView = (TextView) findViewById(R.id.tv_file_state);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        percentageView = (TextView) findViewById(R.id.percentage);
//        percentageView = (TextView) findViewById(R.id.percentage);
        imageView = (ImageView) findViewById(R.id.image);
        statusView = (ImageView) findViewById(R.id.msg_status);
    }


    @Override
    protected void onSetUpView() {
        Glide.with(context).load(message.getContent())
                .placeholder(R.drawable.ease_default_image)
                .error(R.drawable.ease_default_image)
                .override(700, 700)
//                .crossFade() //设置淡入淡出效果，默认300ms，可以传参
                .fitCenter().into(imageView);
        handleSendMessage();
    }


    /**
     * handle sending message
     */
    protected void handleSendMessage() {
        if (message.getMDirect() == MessageDirect.SEND) {
            setMessageSendCallback();
            switch (message.getStatus()) {
                case MessageStatus.SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    if (percentageView != null)
                        percentageView.setVisibility(View.GONE);
                    if (statusView != null)
                        statusView.setVisibility(View.GONE);
                    break;
                case MessageStatus.FAIL:
                    progressBar.setVisibility(View.GONE);
                    if (percentageView != null)
                        percentageView.setVisibility(View.GONE);
                    if (statusView != null)
                        statusView.setVisibility(View.VISIBLE);
                    break;
                case MessageStatus.INPROGRESS:
                    progressBar.setVisibility(View.VISIBLE);
                    if (percentageView != null) {
                        percentageView.setVisibility(View.VISIBLE);
                        percentageView.setText("正在发送");
                    }
                    if (statusView != null)
                        statusView.setVisibility(View.GONE);
                    break;
                default:
                    progressBar.setVisibility(View.GONE);
                    if (percentageView != null)
                        percentageView.setVisibility(View.GONE);
                    if (statusView != null)
                        statusView.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    @Override
    protected void onUpdateView() {
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onBubbleClick() {
        Intent bigImgIntent = new Intent(context, BigImageDetailActivity.class);
        bigImgIntent.putExtra(BigImageDetailActivity.PARAMS_MSG_ID, message.getTime());
        TransitionsHeleper.getInstance().startActivity((MainActivity) context, bigImgIntent, imageView, message.getContent());
    }
}
