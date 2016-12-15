package com.ozner.cup.Chat;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.Toast;

import com.ozner.cup.Chat.ChatHttpUtils.FuckChatHttpClient;
import com.ozner.cup.Chat.EaseUI.UI.EaseBaseFragment;
import com.ozner.cup.Chat.EaseUI.controller.EaseUI;
import com.ozner.cup.Chat.EaseUI.domain.EaseEmojicon;
import com.ozner.cup.Chat.EaseUI.model.MessageDirect;
import com.ozner.cup.Chat.EaseUI.model.MessageStatus;
import com.ozner.cup.Chat.EaseUI.model.MessageType;
import com.ozner.cup.Chat.EaseUI.utils.EaseCommonUtils;
import com.ozner.cup.Chat.EaseUI.utils.MessageCreator;
import com.ozner.cup.Chat.EaseUI.widget.EaseAlertDialog;
import com.ozner.cup.Chat.EaseUI.widget.EaseChatExtendMenu;
import com.ozner.cup.Chat.EaseUI.widget.EaseChatInputMenu;
import com.ozner.cup.Chat.EaseUI.widget.EaseChatMessageList;
import com.ozner.cup.Chat.EaseUI.widget.chartrow.EaseCustomChatRowProvider;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.EMMessage;
import com.ozner.cup.DBHelper.UserInfo;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;

import java.io.File;
import java.util.Calendar;
import java.util.List;

/**
 * you can new an EaseChatFragment to use or you can inherit it to expand.
 * You need call setArguments to pass chatType and userId
 * <br/>
 * <br/>
 * you can see ChatActivity in demo for your reference
 */
public class EaseChatFragment extends EaseBaseFragment {
    protected static final String TAG = "EaseChatFragment";
    protected static final int REQUEST_CODE_MAP = 1;
    protected static final int REQUEST_CODE_CAMERA = 2;
    protected static final int REQUEST_CODE_LOCAL = 3;
//    private ChatOkManager chatOkManager;

    /**
     * params to fragment
     */
    protected Bundle fragmentArgs;
    //    protected int chatType;
    protected String toChatUsername;
    protected EaseChatMessageList messageList;
    protected EaseChatInputMenu inputMenu;

    protected InputMethodManager inputManager;
    protected ClipboardManager clipboard;

    protected Handler handler = new Handler();
    protected File cameraFile;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected ListView listView;

    protected boolean isloading;
    protected boolean haveMoreData = true;
    protected int pagesize = 20;
    protected EMMessage contextMenuMessage;

    static final int ITEM_TAKE_PICTURE = 1;
    static final int ITEM_PICTURE = 2;

    protected int[] itemStrings = {R.string.attach_take_pic, R.string.attach_picture};
    protected int[] itemdrawables = {R.drawable.camera, R.drawable.picture};
    protected int[] itemIds = {ITEM_TAKE_PICTURE, ITEM_PICTURE};
    private boolean isMessageListInited;
    protected MyItemClickListener extendMenuItemClickListener;
    private String userid;
    private String mMobile;

    private FuckChatHttpClient fuckChatHttpClient;

    /**
     * 实例化Fragment
     *
     * @param bundle
     *
     * @return
     */
    public static EaseChatFragment newInstance(Bundle bundle) {
        EaseChatFragment fragment = new EaseChatFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        userid = UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, null);
        UserInfo userInfo = DBManager.getInstance(getContext()).getUserInfo(userid);
        mMobile = userInfo.getMobile();
        fuckChatHttpClient = new FuckChatHttpClient();
        fuckChatHttpClient.setChatHttpListener(new MyChatHttpListener());
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onDetach() {
        System.gc();
        super.onDetach();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ease_fragment_chat, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        fragmentArgs = getArguments();

        toChatUsername = UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, "");
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * init view
     */
    protected void initView() {
        // hold to record voice
        //noinspection ConstantConditions

        // message list layout
        messageList = (EaseChatMessageList) getView().findViewById(R.id.message_list);
        messageList.setShowUserNick(true);
        listView = messageList.getListView();

        extendMenuItemClickListener = new MyItemClickListener();
        inputMenu = (EaseChatInputMenu) getView().findViewById(R.id.input_menu);
        registerExtendMenuItem();
        // init input menu
        inputMenu.init(null);

        //发送信息按钮监听，大图点击监听
        inputMenu.setChatInputMenuListener(new EaseChatInputMenu.ChatInputMenuListener() {

            @Override
            public void onSendMessage(String content) {
                sendTextMessage(content);
            }


            @Override
            public void onBigExpressionClicked(EaseEmojicon emojicon) {
                sendBigExpressionMessage(emojicon.getName(), emojicon.getIdentityCode());
            }
        });

        swipeRefreshLayout = messageList.getSwipeRefreshLayout();
        swipeRefreshLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_light,
                R.color.holo_orange_light, R.color.holo_red_light);

        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    protected void setUpView() {

        onMessageListInit();
        setRefreshLayoutListener();

    }

    /**
     * register extend menu, item id need > 3 if you override this method and keep exist item
     */
    protected void registerExtendMenuItem() {
        for (int i = 0; i < itemStrings.length; i++) {
            inputMenu.registerExtendMenuItem(itemStrings[i], itemdrawables[i], itemIds[i], extendMenuItemClickListener);
        }
    }

    /**
     * 初始化消息列表
     */
    protected void onMessageListInit() {
        messageList.init(toChatUsername, chatFragmentHelper != null ?
                chatFragmentHelper.onSetCustomChatRowProvider() : null);
        setListItemClickListener();

        messageList.getListView().setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                inputMenu.hideExtendMenuContainer();
                return false;
            }
        });

        isMessageListInited = true;
    }

    protected void setListItemClickListener() {
        messageList.setItemClickListener(new EaseChatMessageList.MessageListItemClickListener() {

            @Override
            public void onUserAvatarClick(String username) {
                if (chatFragmentHelper != null) {
                    chatFragmentHelper.onAvatarClick(username);
                }
            }

            @Override
            public void onUserAvatarLongClick(String username) {
                if (chatFragmentHelper != null) {
                    chatFragmentHelper.onAvatarLongClick(username);
                }
            }

            @Override
            public void onResendClick(final EMMessage message) {
                new EaseAlertDialog(getActivity(), R.string.resend, R.string.confirm_resend, null, new EaseAlertDialog.AlertDialogUser() {
                    @Override
                    public void onResult(boolean confirmed, Bundle bundle) {
                        if (!confirmed) {
                            return;
                        }
                        resendMessage(message);
                    }
                }, true).show();
            }

            @Override
            public void onBubbleLongClick(EMMessage message) {
                contextMenuMessage = message;
                if (chatFragmentHelper != null) {
                    chatFragmentHelper.onMessageBubbleLongClick(message);
                }
            }

            @Override
            public boolean onBubbleClick(EMMessage message) {
                if (chatFragmentHelper == null) {
                    return false;
                }
                return chatFragmentHelper.onMessageBubbleClick(message);
            }

        });
    }

    /**
     * 设置下拉刷新监听
     */
    protected void setRefreshLayoutListener() {
        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (listView.getFirstVisiblePosition() == 0 && !isloading && haveMoreData) {
                            List<EMMessage> messages;
//                            try {
//                                if (chatType == EaseConstant.CHATTYPE_SINGLE) {
//                                    messages = conversation.loadMoreMsgFromDB(messageList.getItem(0).getMsgId(),
//                                            pagesize);
//                                } else {
//                                    messages = conversation.loadMoreMsgFromDB(messageList.getItem(0).getMsgId(),
//                                            pagesize);
//                                }
//                            } catch (Exception e1) {
//                                swipeRefreshLayout.setRefreshing(false);
//                                return;
//                            }
//                            if (messages.size() > 0) {
//                                messageList.refreshSeekTo(messages.size() - 1);
//                                if (messages.size() != pagesize) {
//                                    haveMoreData = false;
//                                }
//                            } else {
//                                haveMoreData = false;
//                            }

                            isloading = false;

                        } else {
                            Toast.makeText(getActivity(), getResources().getString(R.string.no_more_messages),
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 600);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) { // capture new image
                if (cameraFile != null && cameraFile.exists())
                    sendImageMessage(cameraFile.getAbsolutePath());
            } else if (requestCode == REQUEST_CODE_LOCAL) { // send local image
                if (data != null) {
                    Uri selectedImage = data.getData();
                    if (selectedImage != null) {
                        sendPicByUri(selectedImage);
                    }
                }
            }
        }
    }

    @Override
    public void onStart() {
        try {
            Log.e(TAG, "onStart: " + mMobile);
//        chatOkManager.initChat(mMobile,"");
            fuckChatHttpClient.getAccessToken(mMobile, "");
//            ChatHttpMethods.getInstance().initChat(mMobile, new ChatHttpMethods.ChatResultListener() {
//                @Override
//                public void onSuccess() {
//                    Log.e(TAG, "initChat_onSuccess: ");
//                }
//
//                @Override
//                public void onFail(int state, String msg) {
//                    Log.e(TAG, "initChat_onFail: state:" + state + " , msg:" + msg);
//                }
//            });
        } catch (Exception ex) {
            Log.e(TAG, "onStart_ex: " + ex.getMessage());
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        try {
            setBarColor(R.color.colorAccent);
            setToolbarColor(R.color.colorAccent);
            ((MainActivity) getActivity()).setCustomTitle(R.string.chat);
        } catch (Exception ex) {

        }
        super.onResume();
        if (isMessageListInited)
            messageList.refresh();
    }

    /**
     * 设置状态栏颜色
     */
    protected void setBarColor(int resId) {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = ((MainActivity) getActivity()).getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(getContext(), resId));
        }
    }

    /**
     * 设置主界面toolbar背景色
     *
     * @param resId
     */
    protected void setToolbarColor(int resId) {
        ((MainActivity) getActivity()).setToolBarColor(resId);
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister this event listener when this activity enters the
        // background
//        EMClient.getInstance().chatManager().removeMessageListener(this);

        // remove activity from foreground activity list
        EaseUI.getInstance().popActivity(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public void onBackPressed() {
        if (inputMenu.onBackPressed()) {
            getActivity().finish();
        }
    }


    protected void showChatroomToast(final String toastContent) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getActivity(), toastContent, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * handle the click event for extend menu
     */
    class MyItemClickListener implements EaseChatExtendMenu.EaseChatExtendMenuItemClickListener {

        @Override
        public void onClick(int itemId, View view) {
            if (chatFragmentHelper != null) {
                if (chatFragmentHelper.onExtendMenuItemClick(itemId, view)) {
                    return;
                }
            }
            switch (itemId) {
                case ITEM_TAKE_PICTURE:
                    selectPicFromCamera();
                    break;
                case ITEM_PICTURE:
                    selectPicFromLocal();
                    break;
//                case ITEM_LOCATION:
//                    startActivityForResult(new Intent(getActivity(), EaseBaiduMapActivity.class), REQUEST_CODE_MAP);
//                    break;

                default:
                    break;
            }
        }

    }

//    /**
//     * input @
//     *
//     * @param username
//     */
//    protected void inputAtUsername(String username, boolean autoAddAtSymbol) {
//        EaseAtMessageHelper.get().addAtUser(username);
//        EaseUser user = EaseUserUtils.getUserInfo(username);
//        if (user != null) {
//            username = user.getNick();
//        }
//        if (autoAddAtSymbol)
//            inputMenu.insertText("@" + username + " ");
//        else
//            inputMenu.insertText(username + " ");
//    }


//    /**
//     * input @
//     *
//     * @param username
//     */
//    protected void inputAtUsername(String username) {
//        inputAtUsername(username, true);
//    }


    /**
     * userid是否为空
     *
     * @return
     */
    private boolean isUserIDisEmpty() {
        return userid != null && !userid.isEmpty();
    }

    /**
     * 发送文本信息
     *
     * @param content
     */
    //send message
    protected void sendTextMessage(String content) {
        Log.e(TAG, "sendTextMessage: " + content);
        if (isUserIDisEmpty()) {
            String sendMsg = MessageCreator.createTextMessage(content);
            EMMessage message = new EMMessage();
            message.setContent(content);
            message.setmType(MessageType.TXT);
            message.setmDirect(MessageDirect.SEND);
            message.setUserid(userid);
            message.setTime(Calendar.getInstance().getTimeInMillis());
            message.setStatus(MessageStatus.SUCCESS);
            sendMessage(sendMsg, message);
        }
//        if (EaseAtMessageHelper.get().containsAtUsername(content)) {
//            sendAtMessage(content);
//        } else {
//            EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
//            sendMessage(message);
//        }
    }


    /**
     * 发送大表情
     *
     * @param name
     * @param identityCode
     */
    protected void sendBigExpressionMessage(String name, String identityCode) {
//        EMMessage message = EaseCommonUtils.createExpressionMessage(toChatUsername, name, identityCode);
        String message = "";
        sendMessage(message, null);
    }

//    protected void sendVoiceMessage(String filePath, int length) {
//        EMMessage message = EMMessage.createVoiceSendMessage(filePath, length, toChatUsername);
//        sendMessage(message);
//    }

    /**
     * 发送图片信息
     *
     * @param imagePath
     */
    protected void sendImageMessage(String imagePath) {
        // TODO: 2016/12/12 发送图片信息 
        Log.e(TAG, "sendImageMessage: 发送图片信息");
//        EMMessage message = EMMessage.createImageSendMessage(imagePath, false, toChatUsername);
//        sendMessage(message);
    }

//    protected void sendLocationMessage(double latitude, double longitude, String locationAddress) {
//        EMMessage message = EMMessage.createLocationSendMessage(latitude, longitude, locationAddress, toChatUsername);
//        sendMessage(message);
//    }
//
//    protected void sendVideoMessage(String videoPath, String thumbPath, int videoLength) {
//        EMMessage message = EMMessage.createVideoSendMessage(videoPath, thumbPath, videoLength, toChatUsername);
//        sendMessage(message);
//    }
//
//    protected void sendFileMessage(String filePath) {
//        EMMessage message = EMMessage.createFileSendMessage(filePath, toChatUsername);
//        sendMessage(message);
//    }


    /**
     * 真正的发送信息,并将信息实体保存到本地数据库
     *
     * @param sendMessage
     * @param saveMessage
     */
    protected void sendMessage(String sendMessage, EMMessage saveMessage) {

        if (saveMessage != null) {
            DBManager.getInstance(getContext()).updateEMMessage(saveMessage);
        }
//        if (sendMessage == null) {
//
//            //发送信息，并更新本地信息发送状态
//            return;
//        }

//        if (chatFragmentHelper != null) {
//            //set extension
//            chatFragmentHelper.onSetMessageAttributes(message);
//        }

        //send message
//        EMClient.getInstance().chatManager().sendMessage(message);
        // TODO: 2016/12/12 发送信息
        //刷新UI
        if (isMessageListInited) {
//            Log.e(TAG, "sendMessage: refreshSelectLast");
            messageList.refreshSelectLast();
        }
    }


    /**
     * 重新发送信息
     *
     * @param message
     */
    public void resendMessage(EMMessage message) {
//        message.setStatus(EMMessage.Status.CREATE);
//        EMClient.getInstance().chatManager().sendMessage(message);
        messageList.refresh();
    }

    //===================================================================================


    /**
     * send image
     *
     * @param selectedImage
     */
    protected void sendPicByUri(Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            cursor = null;

            if (picturePath == null || picturePath.equals("null")) {
                Toast toast = Toast.makeText(getActivity(), R.string.cant_find_pictures, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            sendImageMessage(picturePath);
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                Toast toast = Toast.makeText(getActivity(), R.string.cant_find_pictures, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;

            }
            sendImageMessage(file.getAbsolutePath());
        }

    }

    /**
     * send file
     *
     * @param uri
     */
    protected void sendFileByUri(Uri uri) {
        String filePath = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = null;

            try {
                cursor = getActivity().getContentResolver().query(uri, filePathColumn, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        if (filePath == null) {
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(getActivity(), R.string.File_does_not_exist, Toast.LENGTH_SHORT).show();
            return;
        }
        //limit the size < 10M
        if (file.length() > 10 * 1024 * 1024) {
            Toast.makeText(getActivity(), R.string.The_file_is_not_greater_than_10_m, Toast.LENGTH_SHORT).show();
            return;
        }
//        sendFileMessage(filePath);
    }

    /**
     * capture new image
     */
    protected void selectPicFromCamera() {
        try {
            if (!EaseCommonUtils.isSdcardExist()) {
                Toast.makeText(getActivity(), R.string.sd_card_does_not_exist, Toast.LENGTH_SHORT).show();
                return;
            }

            cameraFile = new File(Environment.getExternalStorageDirectory().getPath() + "/OnzerCache/", UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, "ozner")
                    + System.currentTimeMillis() + ".jpg");
            //noinspection ResultOfMethodCallIgnored
            cameraFile.getParentFile().mkdirs();
            startActivityForResult(
                    new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
                    REQUEST_CODE_CAMERA);
        } catch (Exception ex) {
            Log.e(TAG, "selectPicFromCamera_Ex: " + ex.getMessage());
        }
    }

    /**
     * select local image
     */
    protected void selectPicFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_LOCAL);
    }


    /**
     * 清空历史记录
     * clear the conversation history
     */
    protected void clearHistory() {
        String msg = getResources().getString(R.string.Whether_to_empty_all_chats);
        new EaseAlertDialog(getActivity(), null, msg, null, new EaseAlertDialog.AlertDialogUser() {

            @Override
            public void onResult(boolean confirmed, Bundle bundle) {
                if (confirmed && userid != null && !userid.isEmpty()) {
                    DBManager.getInstance(getContext()).clearEMMessage(userid);
                }
            }
        }, true).show();
    }


    /**
     * hide
     */
    protected void hideKeyboard() {
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * forward message
     *
     * @param forward_msg_id
     */
    protected void forwardMessage(String forward_msg_id) {
        final EMMessage forward_msg = null;
        int msgtype = forward_msg.getmType();
        switch (msgtype) {
            case MessageType.TXT:
//                if (forward_msg.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)) {
//                    sendBigExpressionMessage(((EMTextMessageBody) forward_msg.getBody()).getMessage(),
//                            forward_msg.getStringAttribute(EaseConstant.MESSAGE_ATTR_EXPRESSION_ID, null));
//                } else {
//                    // get the content and send it
//                    String content = ((EMTextMessageBody) forward_msg.getBody()).getMessage();
//                    sendTextMessage(content);
//                }
                break;
            case MessageType.IMAGE:
//                // send image
//                String filePath = ((EMImageMessageBody) forward_msg.getBody()).getLocalUrl();
//                if (filePath != null) {
//                    File file = new File(filePath);
//                    if (!file.exists()) {
//                        // send thumb nail if original image does not exist
//                        filePath = ((EMImageMessageBody) forward_msg.getBody()).thumbnailLocalPath();
//                    }
//                    sendImageMessage(filePath);
//                }
                break;
            default:
                break;
        }
    }


    protected EaseChatFragmentHelper chatFragmentHelper;

    public void setChatFragmentListener(EaseChatFragmentHelper chatFragmentHelper) {
        this.chatFragmentHelper = chatFragmentHelper;
    }

    public class MyChatHttpListener implements FuckChatHttpClient.FuckChatHttpListener {

        @Override
        public void initChatSuccess() {

        }

        @Override
        public void onFail(int code, String msg) {

        }
    }

    public interface EaseChatFragmentHelper {
        /**
         * set message attribute
         */
        void onSetMessageAttributes(EMMessage message);

        /**
         * enter to chat detail
         */
        void onEnterToChatDetails();

        /**
         * on avatar clicked
         *
         * @param username
         */
        void onAvatarClick(String username);

        /**
         * on avatar long pressed
         *
         * @param username
         */
        void onAvatarLongClick(String username);

        /**
         * on message bubble clicked
         */
        boolean onMessageBubbleClick(EMMessage message);

        /**
         * on message bubble long pressed
         */
        void onMessageBubbleLongClick(EMMessage message);

        /**
         * on extend menu item clicked, return true if you want to override
         *
         * @param view
         * @param itemId
         *
         * @return
         */
        boolean onExtendMenuItemClick(int itemId, View view);

        /**
         * on set custom chat row provider
         *
         * @return
         */
        EaseCustomChatRowProvider onSetCustomChatRowProvider();
    }

}
