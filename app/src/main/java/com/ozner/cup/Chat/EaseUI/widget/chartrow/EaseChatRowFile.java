package com.ozner.cup.Chat.EaseUI.widget.chartrow;

import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ozner.cup.Chat.EaseUI.model.MessageDirect;
import com.ozner.cup.DBHelper.EMMessage;
import com.ozner.cup.R;

public class EaseChatRowFile extends EaseChatRow{

    protected TextView fileNameView;
	protected TextView fileSizeView;
    protected TextView fileStateView;
    
//    protected EMCallBack sendfileCallBack;
    
    protected boolean isNotifyProcessed;
//    private String fileMessageBody;

    public EaseChatRowFile(Context context, EMMessage message, int position, BaseAdapter adapter) {
		super(context, message, position, adapter);
	}

	@Override
	protected void onInflateView() {
	    inflater.inflate(message.getMDirect() == MessageDirect.RECEIVE ?
	            R.layout.ease_row_received_file : R.layout.ease_row_sent_file, this);
	}

	@Override
	protected void onFindViewById() {
	    fileNameView = (TextView) findViewById(R.id.tv_file_name);
        fileSizeView = (TextView) findViewById(R.id.tv_file_size);
        fileStateView = (TextView) findViewById(R.id.tv_file_state);
        percentageView = (TextView) findViewById(R.id.percentage);
	}


	@Override
	protected void onSetUpView() {
//	    fileMessageBody =  message.getContent();
//        String filePath = message.getContent();
//        fileNameView.setText(fileMessageBody.getFileName());
//        fileSizeView.setText(TextFormater.getDataSize(fileMessageBody.getFileSize()));
//        if (message.getMDirect() == MessageDirect.RECEIVE) {
//            File file = new File(filePath);
//            if (file.exists()) {
//                fileStateView.setText(R.string.Have_downloaded);
//            } else {
//                fileStateView.setText(R.string.Did_not_download);
//            }
//            return;
//        }

        // until here, to sending message
        handleSendMessage();
	}

	/**
	 * handle sending message
	 */
    protected void handleSendMessage() {
        setMessageSendCallback();
//        switch (message.getStatus()) {
//        case MessageStatus.SUCCESS:
//            progressBar.setVisibility(View.INVISIBLE);
//            if(percentageView != null)
//                percentageView.setVisibility(View.INVISIBLE);
////            statusView.setVisibility(View.INVISIBLE);
//            break;
//        case MessageStatus.FAIL:
//            progressBar.setVisibility(View.INVISIBLE);
//            if(percentageView != null)
//                percentageView.setVisibility(View.INVISIBLE);
////            statusView.setVisibility(View.VISIBLE);
//            break;
//        case MessageStatus.INPROGRESS:
//            progressBar.setVisibility(View.VISIBLE);
//            if(percentageView != null){
//                percentageView.setVisibility(View.VISIBLE);
//                percentageView.setText("正在发送");
//            }
//            statusView.setVisibility(View.INVISIBLE);
//            break;
//        default:
//            progressBar.setVisibility(View.INVISIBLE);
//            if(percentageView != null)
//                percentageView.setVisibility(View.INVISIBLE);
//            statusView.setVisibility(View.VISIBLE);
//            break;
//        }
    }
	

	@Override
    protected void onUpdateView() {
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onBubbleClick() {
//        String filePath = fileMessageBody.getLocalUrl();
//        File file = new File(filePath);
//        if (file.exists()) {
//            // open files if it exist
//            FileUtils.openFile(file, (Activity) context);
//        } else {
//            // download the file
//            context.startActivity(new Intent(context, EaseShowNormalFileActivity.class).putExtra("msgbody", message.getBody()));
//        }
//        if (message.direct() == EMMessage.Direct.RECEIVE && !message.isAcked() && message.getChatType() == ChatType.Chat) {
//            try {
//                EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
//            } catch (HyphenateException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
        
    }
}
