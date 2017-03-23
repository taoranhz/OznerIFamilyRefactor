package com.ozner.cup.Chat;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;
import com.ozner.cup.Bean.OznerBroadcastAction;
import com.ozner.cup.Chat.ChatHttpUtils.FuckChatHttpClient;
import com.ozner.cup.Chat.EaseUI.UI.EaseBaseFragment;
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
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.EMMessage;
import com.ozner.cup.DBHelper.UserInfo;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;
import com.ozner.cup.Utils.LCLogUtils;
import com.ozner.cup.Utils.OznerFileImageHelper;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;

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
    protected static final int UploadImg_Success = 4;
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
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    private boolean isMessageListInited;
    protected MyItemClickListener extendMenuItemClickListener;
    private String userid;
    private String mMobile, mDeviceId;
    private boolean isSending = false;

    private FuckChatHttpClient fuckChatHttpClient;
    private ChatMessageReciever chatMonitor;
    PermissionUtil.PermissionRequestObject perReqResult;//, picReqResult, cameraReqResult;
    private HashMap<Long, EMMessage> waitSendMsg = new HashMap<>();

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
        isSending = false;
        userid = OznerPreference.GetValue(getContext(), OznerPreference.UserId, null);
        UserInfo userInfo = DBManager.getInstance(getContext()).getUserInfo(userid);
        mMobile = userInfo.getMobile();
        mDeviceId = OznerPreference.GetValue(getContext(), OznerPreference.BDDeivceID, "");
        fuckChatHttpClient = new FuckChatHttpClient();
        fuckChatHttpClient.setChatHttpListener(new MyChatHttpListener());
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (perReqResult != null) {
            perReqResult.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
//        if (picReqResult != null) {
//            picReqResult.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//        if (cameraReqResult != null) {
//            cameraReqResult.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDetach() {
        Log.e(TAG, "onDetach: ");
        //将未发送的信息设置为发送失败
        for (long key : waitSendMsg.keySet()) {
            final long removtime = waitSendMsg.get(key).getTime();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    EMMessage msg = DBManager.getInstance(getContext()).getChatMessage(userid, removtime);
                    msg.setStatus(MessageStatus.FAIL);
                    DBManager.getInstance(getContext()).updateEMMessage(msg);
                }
            }).start();
            waitSendMsg.remove(key);
            Log.e(TAG, "onDetach: 移除：" + key);
        }
        System.gc();
        super.onDetach();
    }

    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UploadImg_Success:
                    Log.e(TAG, "handleMessage: 图片上传成功");
                    EMMessage chatMsg = (EMMessage) msg.obj;
                    String sendMsg = MessageCreator.createImageMessae(chatMsg.getContent());
                    Log.e(TAG, "handleMessage: imgUrl:" + sendMsg);
                    waitSendMsg.put(chatMsg.getTime(), chatMsg);
                    chatSendMsg(sendMsg, chatMsg.getTime());
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 注册广播接收器
     */
    private void registerReceiver() {
        chatMonitor = new ChatMessageReciever();
        IntentFilter filter = new IntentFilter();
        filter.addAction(OznerBroadcastAction.OBA_RECEIVE_CHAT_MSG);
        filter.addAction(OznerBroadcastAction.OBA_OBTAIN_CHAT_HISTORY);
        getActivity().registerReceiver(chatMonitor, filter);
    }

    /**
     * 注销广播接收器
     */
    private void unRegisterReceiver() {
        try {
            getActivity().unregisterReceiver(chatMonitor);
        } catch (Exception ex) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ease_fragment_chat, container, false);
        ButterKnife.inject(this, view);
        toolbar.setTitle("");
        title.setTextColor(ContextCompat.getColor(getContext(), R.color.light_black));
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        fragmentArgs = getArguments();

        toChatUsername = OznerPreference.GetValue(getContext(), OznerPreference.UserId, "");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        MenuItem item = menu.add(0, 0, 0, R.string.chat_call);
        item.setIcon(R.mipmap.chat_call);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
//                Toast.makeText(getContext(), "打电话", Toast.LENGTH_SHORT).show();
                takePhone();
                break;
        }
        return true;
    }

    /**
     * 拨打客服电话
     */
    private void takePhone() {
        perReqResult = PermissionUtil.with(this).request(Manifest.permission.CALL_PHONE)
                .onAllGranted(new Func() {
                    @Override
                    protected void call() {
                        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:4008202667"));
                        startActivity(callIntent);
                    }
                }).onAnyDenied(new Func() {
                    @Override
                    protected void call() {
                        Toast.makeText(getContext(), R.string.permission_call_phone_denied, Toast.LENGTH_SHORT).show();
                    }
                }).ask(1);
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
//                if (isSending) {
                sendTextMessage(content);
//                }
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
//                        if (!swipeRefreshLayout.isRefreshing()) {
                        int curPage = Integer.parseInt(UserDataPreference.GetUserData(getContext(), UserDataPreference.ChatCurPage, "0"));
                        int totalHistory = Integer.parseInt(UserDataPreference.GetUserData(getContext(), UserDataPreference.ChatHistoryCount, "-1"));
                        boolean hasMoreData = curPage * FuckChatHttpClient.DEFAULT_PAGESIZE < totalHistory;
                        LCLogUtils.E(TAG, "hasMoreData:" + hasMoreData + " ,curPage:" + curPage + " ,totalHistory:" + totalHistory + " ,isRefreshing:" + swipeRefreshLayout.isRefreshing());
                        if (hasMoreData) {
                            fuckChatHttpClient.chatGetHistoryMsg(getContext(), userid, ++curPage);
                        } else {
                            Toast toast = Toast.makeText(getContext(), R.string.no_more_messages, Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, 300);
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
        registerReceiver();
        try {
            Log.e(TAG, "onStart: mobile:" + mMobile + " , deviceId:" + mDeviceId);
            fuckChatHttpClient.initChat(mMobile, mDeviceId);
        } catch (Exception ex) {
            Log.e(TAG, "onStart_ex: " + ex.getMessage());
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        try {
            setBarColor(R.color.colorAccent);
            setToolbarColor(R.color.white);
            title.setText(R.string.chat);
            clearNotifaction();
        } catch (Exception ex) {

        }
        super.onResume();
        if (isMessageListInited)
            messageList.refresh();
    }

    /**
     * 清除通知
     */
    private void clearNotifaction() {
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
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
     * 设置toolbar背景色
     *
     * @param resId
     */
    protected void setToolbarColor(int resId) {
        toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), resId));
    }

    @Override
    public void onStop() {

        unRegisterReceiver();
        super.onStop();
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
        if (EaseChatFragment.this.isAdded()) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getActivity(), toastContent, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
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
        return userid == null || userid.isEmpty();
    }

    /**
     * 发送文本信息
     *
     * @param content
     */
    //send message
    protected void sendTextMessage(String content) {

        if (!isUserIDisEmpty()) {
            EMMessage message = new EMMessage();
            message.setContent(content);
            message.setMType(MessageType.TXT);
            message.setMDirect(MessageDirect.SEND);
            message.setUserid(userid);
            message.setTime(Calendar.getInstance().getTimeInMillis());
            message.setStatus(MessageStatus.INPROGRESS);
            sendMessage(message);
        }

    }


    /**
     * 发送大表情
     *
     * @param name
     * @param identityCode
     */
    protected void sendBigExpressionMessage(String name, String identityCode) {
//        EMMessage message = EaseCommonUtils.createExpressionMessage(toChatUsername, name, identityCode);
//        String message = "";
//        sendMessage(message, null);
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
        Log.e(TAG, "sendImageMessage: 发送图片信息:" + imagePath);

        EMMessage imgMsg = new EMMessage();
        imgMsg.setUserid(userid);
        imgMsg.setStatus(MessageStatus.INPROGRESS);
        imgMsg.setMDirect(MessageDirect.SEND);
        imgMsg.setMType(MessageType.IMAGE);
        imgMsg.setTime(Calendar.getInstance().getTimeInMillis());

        if (imagePath.contains("http:") || imagePath.contains("https:")) {
            String sendPath = OznerFileImageHelper.getSmallBitmapPath(getContext(), imagePath);
            imgMsg.setContent(sendPath);
            //保存信息
            if (imgMsg != null) {
                DBManager.getInstance(getContext()).updateEMMessage(imgMsg);
            }
            waitSendMsg.put(imgMsg.getTime(), imgMsg);
            chatSendMsg(MessageCreator.createImageMessae(sendPath), imgMsg.getTime());
        } else {
            imgMsg.setContent(imagePath);
            //保存信息
            if (imgMsg != null) {
                DBManager.getInstance(getContext()).updateEMMessage(imgMsg);
            }
            chatUploadImage(imgMsg);
        }

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
     * @param saveMessage
     */
    protected void sendMessage(@NonNull EMMessage saveMessage) {
        DBManager.getInstance(getContext()).updateEMMessage(saveMessage);
        waitSendMsg.put(saveMessage.getTime(), saveMessage);
        String sendMsg = MessageCreator.createTextMessage(saveMessage.getContent());
        chatSendMsg(sendMsg, saveMessage.getTime());

        // TODO: 2016/12/12 发送信息
        //刷新UI
        if (isMessageListInited) {
            messageList.refreshSelectLast();
        }
    }


    /**
     * 重新发送信息
     *
     * @param message
     */
    public void resendMessage(EMMessage message) {
        DBManager.getInstance(getContext()).deleteEMMessage(message);
        switch (message.getMType()) {
            case MessageType.TXT:
                sendTextMessage(message.getContent());
                break;
            case MessageType.IMAGE:
                sendImageMessage(message.getContent());
                break;
            default:
                break;
        }
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
//
//    /**
//     * send file
//     *
//     * @param uri
//     */
//    protected void sendFileByUri(Uri uri) {
//        String filePath = null;
//        if ("content".equalsIgnoreCase(uri.getScheme())) {
//            String[] filePathColumn = {MediaStore.Images.Media.DATA};
//            Cursor cursor = null;
//
//            try {
//                cursor = getActivity().getContentResolver().query(uri, filePathColumn, null, null, null);
//                int column_index = cursor.getColumnIndexOrThrow("_data");
//                if (cursor.moveToFirst()) {
//                    filePath = cursor.getString(column_index);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (cursor != null) {
//                    cursor.close();
//                }
//            }
//        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            filePath = uri.getPath();
//        }
//        if (filePath == null) {
//            return;
//        }
//        File file = new File(filePath);
//        if (!file.exists()) {
//            Toast.makeText(getActivity(), R.string.File_does_not_exist, Toast.LENGTH_SHORT).show();
//            return;
//        }
//        //limit the size < 10M
//        if (file.length() > 10 * 1024 * 1024) {
//            Toast.makeText(getActivity(), R.string.The_file_is_not_greater_than_10_m, Toast.LENGTH_SHORT).show();
//            return;
//        }
////        sendFileMessage(filePath);
//    }

    /**
     * 咨询发送信息，网络任务
     *
     * @param msgContent 要发送的消息内容，已经处理成需要的格式
     * @param msgTime
     */
    private void chatSendMsg(String msgContent, long msgTime) {
        if (!EaseCommonUtils.isNetWorkConnected(getContext())) {
//            isSending = false;
            showChatroomToast("网络未连接");
            EMMessage msg = DBManager.getInstance(getContext()).getChatMessage(userid, msgTime);
            if (msg != null) {
                msg.setStatus(MessageStatus.FAIL);
                DBManager.getInstance(getContext()).updateEMMessage(msg);
            }
            if (waitSendMsg.containsKey(msgTime)) {
                waitSendMsg.remove(msgTime);
            }
            return;
        }

//        isSending = true;
        fuckChatHttpClient.chatSendMessage(msgContent, msgTime, new FuckChatHttpClient.SendMessageListener() {
            @Override
            public void onSuccess(long messageTime) {
//                isSending = false;
                // TODO: 2016/12/15 处理信息发送成功
                //更新消息数据库
                EMMessage msg = DBManager.getInstance(getContext()).getChatMessage(userid, messageTime);
                if (msg != null) {
                    msg.setStatus(MessageStatus.SUCCESS);
                    DBManager.getInstance(getContext()).updateEMMessage(msg);
                }
                if (waitSendMsg.containsKey(messageTime)) {
                    waitSendMsg.remove(messageTime);
                }
                if (EaseChatFragment.this.isAdded()) {
                    //刷新UI
                    if (isMessageListInited) {
                        messageList.refreshSelectLast();
                    }
                }
            }

            @Override
            public void onFail(long messageTime, int errCode, String errMsg) {
                Log.e(TAG, "onFail: errcode:" + errCode + " , errMsg:" + errMsg);
//                isSending = false;
                //更新消息数据库
                EMMessage msg = DBManager.getInstance(getContext()).getChatMessage(userid, messageTime);
                if (msg != null) {
                    msg.setStatus(MessageStatus.FAIL);
                    DBManager.getInstance(getContext()).updateEMMessage(msg);
                }
                if (waitSendMsg.containsKey(messageTime)) {
                    waitSendMsg.remove(messageTime);
                }
                if (EaseChatFragment.this.isAdded()) {
                    //刷新UI
                    if (isMessageListInited) {
                        messageList.refreshSelectLast();
                    }
                }
            }
        });
    }

    /**
     * 上传图片
     *
     * @param emMessage
     */
    private void chatUploadImage(EMMessage emMessage) {
        waitSendMsg.put(emMessage.getTime(), emMessage);
        Log.e(TAG, "chatUploadImage: 发送图片：" + emMessage.getContent());
        if (!EaseCommonUtils.isNetWorkConnected(getContext())) {
            Log.e(TAG, "chatUploadImage: 网络未连接");

            showChatroomToast("网络未连接");
            EMMessage msg = DBManager.getInstance(getContext()).getChatMessage(userid, emMessage.getTime());
            if (msg != null) {
                msg.setStatus(MessageStatus.FAIL);
                DBManager.getInstance(getContext()).updateEMMessage(msg);
            }
            if (waitSendMsg.containsKey(emMessage.getTime())) {
                waitSendMsg.remove(emMessage.getTime());
            }
            return;
        }

        fuckChatHttpClient.chatUploadImage(emMessage.getTime(), emMessage.getContent(), new FuckChatHttpClient.UploadImageListener() {
            @Override
            public void onSuccess(final long messageTime, final String imgUrl) {
                //更新消息数据库
                Log.e(TAG, "onSuccess: 上传成功:" + imgUrl);
                EMMessage msg = DBManager.getInstance(getContext()).getChatMessage(userid, messageTime);
                if (msg != null) {
                    msg.setStatus(MessageStatus.INPROGRESS);
                    msg.setContent(imgUrl);
                    DBManager.getInstance(getContext()).updateEMMessage(msg);
                    Message message = handler.obtainMessage(UploadImg_Success);
                    message.obj = msg;
                    handler.sendMessage(message);
                }
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        EMMessage msg = DBManager.getInstance(getContext()).getChatMessage(userid, messageTime);
//                        msg.setStatus(MessageStatus.SUCCESS);
//                        msg.setContent(imgUrl);
//                        DBManager.getInstance(getContext()).updateEMMessage(msg);
//
//                    }
//                });
//                EMMessage msg = DBManager.getInstance(getContext()).getChatMessage(userid, messageTime);
//                msg.setStatus(MessageStatus.SUCCESS);
//                DBManager.getInstance(getContext()).updateEMMessage(msg);
//
//                if (EaseChatFragment.this.isAdded()) {
//                    //刷新UI
//                    if (isMessageListInited) {
//                        messageList.refreshSelectLast();
//                    }
//                }
            }

            @Override
            public void onFail(long messageTime, int errCode, String errMsg) {
                EMMessage msg = DBManager.getInstance(getContext()).getChatMessage(userid, messageTime);
                msg.setStatus(MessageStatus.FAIL);
                DBManager.getInstance(getContext()).updateEMMessage(msg);
                if (waitSendMsg.containsKey(messageTime)) {
                    waitSendMsg.remove(messageTime);
                }
                if (EaseChatFragment.this.isAdded()) {
                    //刷新UI
                    if (isMessageListInited) {
                        messageList.refreshSelectLast();
                    }
                }
            }
        });

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
            perReqResult = PermissionUtil.with((MainActivity) getActivity()).request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                    .onAllGranted(new Func() {
                        @Override
                        protected void call() {
                            cameraFile = new File(Environment.getExternalStorageDirectory().getPath() + "/OnzerCache/", OznerPreference.GetValue(getContext(), OznerPreference.UserId, "ozner")
                                    + System.currentTimeMillis() + ".jpg");
                            //noinspection ResultOfMethodCallIgnored
                            cameraFile.getParentFile().mkdirs();
                            startActivityForResult(
                                    new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
                                    REQUEST_CODE_CAMERA);
                        }
                    }).onAnyDenied(new Func() {
                        @Override
                        protected void call() {
                            Log.e(TAG, "call: 权限被拒绝");

                            if (EaseChatFragment.this.isAdded())
                                Toast.makeText(getContext(), R.string.no_permission, Toast.LENGTH_SHORT).show();
                        }
                    }).ask(2);
//            cameraFile = new File(Environment.getExternalStorageDirectory().getPath() + "/OnzerCache/", UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, "ozner")
//                    + System.currentTimeMillis() + ".jpg");
//            //noinspection ResultOfMethodCallIgnored
//            cameraFile.getParentFile().mkdirs();
//            startActivityForResult(
//                    new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
//                    REQUEST_CODE_CAMERA);
        } catch (Exception ex) {
            Log.e(TAG, "selectPicFromCamera_Ex: " + ex.getMessage());
        }
    }

    /**
     * select local image
     */
    protected void selectPicFromLocal() {
        try {
            perReqResult = PermissionUtil.with((MainActivity) getActivity()).request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .onAllGranted(new Func() {
                        @Override
                        protected void call() {
                            Intent intent;
                            if (Build.VERSION.SDK_INT < 19) {
                                intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                            } else {
                                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            }
                            startActivityForResult(intent, REQUEST_CODE_LOCAL);
                        }
                    }).onAnyDenied(new Func() {
                        @Override
                        protected void call() {
                            if (EaseChatFragment.this.isAdded())
                                Toast.makeText(getContext(), R.string.no_permission, Toast.LENGTH_SHORT).show();
                        }
                    }).ask(3);
        } catch (Exception ex) {
            Log.e(TAG, "selectPicFromLocal_Ex: " + ex.getMessage());
        }
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
        int msgtype = forward_msg.getMType();
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
        public void onLoginSuccess(String kfid, String kfname) {
//            showChatroomToast(kfid + " : " + kfname);
            LCLogUtils.E(TAG, "kfid:" + kfid + " ,kfname:" + kfname);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int curMsgCount = DBManager.getInstance(getContext()).getAllChatMessage(userid).size();
                    int historyCount = Integer.parseInt(UserDataPreference.GetUserData(getContext(), UserDataPreference.ChatHistoryCount, "-1"));
                    //本地没有消息，并且未获取过历史记录，则加载第一页数据
                    if (0 == curMsgCount && -1 == historyCount) {
                        swipeRefreshLayout.setRefreshing(true);
                        fuckChatHttpClient.chatGetHistoryMsg(getContext(), userid, 1);
                    }
                }
            });
        }

        @Override
        public void onFail(int code, String msg) {
            showChatroomToast(code + " : " + msg);
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

    class ChatMessageReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            swipeRefreshLayout.setRefreshing(false);
            switch (intent.getAction()) {
                case OznerBroadcastAction.OBA_RECEIVE_CHAT_MSG:
                    //刷新UI
                    if (isMessageListInited) {
                        messageList.refreshSelectLast();
                    }
                    break;
                case OznerBroadcastAction.OBA_OBTAIN_CHAT_HISTORY:
                    int getcount = intent.getIntExtra(FuckChatHttpClient.GET_COUNT, 0);
                    LCLogUtils.E(TAG, "ChatMessageReciever_goutCount:" + getcount);
                    if (isMessageListInited) {
                        messageList.refreshSeekTo(getcount);
                    }
                    break;
            }
        }
    }

}
