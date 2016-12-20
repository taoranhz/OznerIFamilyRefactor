package com.ozner.cup.Chat.EaseUI.widget.chartrow;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ozner.cup.Chat.BigImageDetailActivity;
import com.ozner.cup.Chat.EaseUI.model.MessageDirect;
import com.ozner.cup.DBHelper.EMMessage;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;
import com.ozner.cup.Utils.TransitionHelper.TransitionsHeleper;

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
        percentageView = (TextView) findViewById(R.id.percentage);
        percentageView = (TextView) findViewById(R.id.percentage);
        imageView = (ImageView) findViewById(R.id.image);
    }


    @Override
    protected void onSetUpView() {
//        imgBody = (EMImageMessageBody) message.getBody();
        // received messages
//        if (message.getMDirect() == MessageDirect.RECEIVE) {
//            if (imgBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.DOWNLOADING ||
//                    imgBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.PENDING) {
//                imageView.setImageResource(R.drawable.ease_default_image);
//                setMessageReceiveCallback();
//            } else {
//                progressBar.setVisibility(View.GONE);
//                percentageView.setVisibility(View.GONE);
//                imageView.setImageResource(R.drawable.ease_default_image);
//                String thumbPath = imgBody.thumbnailLocalPath();
//                if (!new File(thumbPath).exists()) {
//                	// to make it compatible with thumbnail received in previous version
//                    thumbPath = EaseImageUtils.getThumbnailImagePath(imgBody.getLocalUrl());
//                }
//                showImageView(thumbPath, imageView, imgBody.getLocalUrl(), message);
//            }
//            return;
//        }
//
//        String filePath = imgBody.getLocalUrl();
//        String thumbPath = EaseImageUtils.getThumbnailImagePath(imgBody.getLocalUrl());
//        showImageView(thumbPath, imageView, filePath, message);

        Glide.with(context).load(message.getContent())
                .placeholder(R.drawable.ease_default_image)
                .error(R.drawable.ease_default_image)
                .override(1000,1000)
                .crossFade() //设置淡入淡出效果，默认300ms，可以传参
                .fitCenter().into(imageView);
//        handleSendMessage();
    }

    @Override
    protected void onUpdateView() {
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onBubbleClick() {
        Log.e(TAG, "onBubbleClick: ");
        Intent bigImgIntent = new Intent(context, BigImageDetailActivity.class);
        bigImgIntent.putExtra(BigImageDetailActivity.PARAMS_MSG_ID,message.getTime());
        TransitionsHeleper.getInstance().startActivity((MainActivity) context,bigImgIntent,imageView,message.getContent());
//        Intent intent = new Intent(context,)
//        Intent intent = new Intent(context, EaseShowBigImageActivity.class);
//        File file = new File(imgBody.getLocalUrl());
//        if (file.exists()) {
//            Uri uri = Uri.fromFile(file);
//            intent.putExtra("uri", uri);
//        } else {
//            // The local full size pic does not exist yet.
//            // ShowBigImage needs to download it from the server
//            // first
//            String msgId = message.getMsgId();
//            intent.putExtra("messageId", msgId);
//            intent.putExtra("localUrl", imgBody.getLocalUrl());
//        }
//        if (message != null && message.direct() == EMMessage.Direct.RECEIVE && !message.isAcked()
//                && message.getChatType() == ChatType.Chat) {
//            try {
//                EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        context.startActivity(intent);
    }

//    /**
//     * load image into image view
//     *
//     * @param thumbernailPath
//     * @param iv
//     * @param position
//     *
//     * @return the image exists or not
//     */
//    private boolean showImageView(final String thumbernailPath, final ImageView iv, final String localFullSizePath, final EMMessage message) {
//        // first check if the thumbnail image already loaded into cache
//        Bitmap bitmap = EaseImageCache.getInstance().get(thumbernailPath);
//        if (bitmap != null) {
//            // thumbnail image is already loaded, reuse the drawable
//            iv.setImageBitmap(bitmap);
//            return true;
//        } else {
//            new AsyncTask<Object, Void, Bitmap>() {
//
//                @Override
//                protected Bitmap doInBackground(Object... args) {
//                    File file = new File(thumbernailPath);
//                    if (file.exists()) {
//                        return EaseImageUtils.decodeScaleImage(thumbernailPath, 160, 160);
//                    } else if (new File(imgBody.thumbnailLocalPath()).exists()) {
//                        return EaseImageUtils.decodeScaleImage(imgBody.thumbnailLocalPath(), 160, 160);
//                    } else {
//                        if (message.direct() == EMMessage.Direct.SEND) {
//                            if (localFullSizePath != null && new File(localFullSizePath).exists()) {
//                                return EaseImageUtils.decodeScaleImage(localFullSizePath, 160, 160);
//                            } else {
//                                return null;
//                            }
//                        } else {
//                            return null;
//                        }
//                    }
//                }
//
//                protected void onPostExecute(Bitmap image) {
//                    if (image != null) {
//                        iv.setImageBitmap(image);
//                        EaseImageCache.getInstance().put(thumbernailPath, image);
//                    } else {
//                        if (message.status() == EMMessage.Status.FAIL) {
//                            if (EaseCommonUtils.isNetWorkConnected(activity)) {
//                                new Thread(new Runnable() {
//
//                                    @Override
//                                    public void run() {
//                                        EMClient.getInstance().chatManager().downloadThumbnail(message);
//                                    }
//                                }).start();
//                            }
//                        }
//
//                    }
//                }
//            }.execute();
//
//            return true;
//        }
//    }

}
