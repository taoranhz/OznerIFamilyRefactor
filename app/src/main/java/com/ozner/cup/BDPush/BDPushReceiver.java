package com.ozner.cup.BDPush;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.baidu.android.pushservice.PushMessageReceiver;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Bean.OznerBroadcastAction;
import com.ozner.cup.Chat.EaseUI.model.MessageDirect;
import com.ozner.cup.Chat.EaseUI.utils.MessageCreator;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.EMMessage;
import com.ozner.cup.Utils.LCLogUtils;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by ozner_67 on 2016/12/15.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class BDPushReceiver extends PushMessageReceiver {
    private static final String TAG = "BDPushReceiver";

    @Override
    public void onBind(Context context, int errorCode, String appid,
                       String userId, String channelId, String requestId) {
        Log.e(TAG, "onBind: errCode:" + errorCode + " , appid:" + appid + " ,userId:"
                + userId + " ,ChannelId:" + channelId + " ,requestId:" + requestId);
        boolean isBind = Boolean.parseBoolean(OznerPreference.GetValue(context, OznerPreference.ISBDBind, "false"));
        Log.e(TAG, "onBind: isBind:" + isBind);
        if (!isBind && channelId != null && !channelId.isEmpty()) {
            OznerPreference.SetValue(context, OznerPreference.BDDeivceID, channelId);
            context.sendBroadcast(new Intent(OznerBroadcastAction.OBA_BDBind));
        }
    }

    @Override
    public void onUnbind(Context context, int errorCode, String requestId) {
        Log.e(TAG, "onUnbind: errorCode->" + errorCode + ", errorCode->" + errorCode);
    }

    @Override
    public void onSetTags(Context context, int errorCode, List<String> sucessTags, List<String> failTags, String requestId) {
        Log.e(TAG, "onSetTags: errorCode->" + errorCode + ", sucessTags->" + sucessTags.size() + ", failTags->" + failTags.size() + ", requestId->" + requestId);
    }

    @Override
    public void onDelTags(Context context, int errorCode,
                          List<String> sucessTags, List<String> failTags, String requestId) {
        Log.e(TAG, "onDelTags: errorCode->" + errorCode + ", sucessTags->" + sucessTags.size() + ", failTags->" + failTags.size() + ", requestId->" + requestId);
    }

    @Override
    public void onListTags(Context context, int errorCode, List<String> tags,
                           String requestId) {

    }

    @Override
    public void onMessage(Context context, String message,
                          String customContentString) {
        Log.e(TAG, "onMessage: message->" + message + ", customContentString->" + customContentString);
        handleMessage(context, message);
    }

    @Override
    public void onNotificationClicked(final Context context, String title,
                                      final String description, final String customContentString) {
        Log.e(TAG, "onNotificationClicked: title->" + title + ", description->" + description + ", customContentString->" + customContentString);
    }

    @Override
    public void onNotificationArrived(Context context, String title,
                                      String description, String customContentString) {
        Log.e(TAG, "onNotificationArrived: title->" + title + ", description->" + description + ", customContentString->" + customContentString);
    }


    /**
     * 处理透传消息
     *
     * @param message
     */
    private void handleMessage(Context context, String message) {
        try {
            JSONObject jsonObj = new JSONObject(message);
            String action = jsonObj.getJSONObject("custom_content").getString("action");
            switch (action) {
                case PushOperationAction.Operation_Chat://咨询新的信息
                    String msg;
                    if (jsonObj.has("custom_content")) {
                        msg = jsonObj.getJSONObject("custom_content").getString("data");
                    } else {
                        msg = jsonObj.getString("data");
                    }
                    String userid = UserDataPreference.GetUserData(context, UserDataPreference.UserId, "");
                    boolean isLogin = OznerPreference.IsLogin(context);

                    if (isLogin && !msg.isEmpty()) {
                        EMMessage emMessage = MessageCreator.transMsgNetToLocal(userid, msg, MessageDirect.RECEIVE, MessageCreator.LOCAL_TIME);
                        DBManager.getInstance(context).updateEMMessage(emMessage);
                        context.sendBroadcast(new Intent(OznerBroadcastAction.OBA_RECEIVE_CHAT_MSG));
                    }
                    break;
                case PushOperationAction.Operation_LoginNotify:
                    LCLogUtils.E(TAG, "LoginNotify");
                    Intent loginIntent = new Intent(OznerBroadcastAction.OBA_Login_Notify);
                    String data = jsonObj.getJSONObject("custom_content").getString("data");
                    JSONObject dataJo = new JSONObject(data);
                    String loginUsertoken = dataJo.getString("token");
                    String miei = dataJo.getString("miei");
                    String loginUserid = dataJo.getString("userid");
                    loginIntent.putExtra(Contacts.PARMS_LOGIN_TOKEN, loginUsertoken);
                    loginIntent.putExtra(Contacts.PARMS_LOGIN_MIEI, miei);
                    loginIntent.putExtra(Contacts.PARMS_LOGIN_USERID, loginUserid);
                    context.sendBroadcast(loginIntent);
                    break;
                case PushOperationAction.Operation_NewFriendVF://新的好友验证请求
                    Intent vfIntent = new Intent(OznerBroadcastAction.OBA_NewFriendVF);
                    context.sendBroadcast(vfIntent);
                    break;
                case PushOperationAction.Operation_NewRank://新的排名
                    Intent newRankIntent = new Intent(OznerBroadcastAction.OBA_NewRank);
                    context.sendBroadcast(newRankIntent);
                    break;
                case PushOperationAction.Operation_NewMessage://新的留言
                    Intent newMsgIntent = new Intent(OznerBroadcastAction.OBA_NewCenterMsg);
                    context.sendBroadcast(newMsgIntent);
                    break;
                case PushOperationAction.Operation_NewFriend:
                    Intent newFrendIntent = new Intent(OznerBroadcastAction.OBA_NewFriend);
                    context.sendBroadcast(newFrendIntent);
                    break;
            }
        } catch (Exception ex) {
            Log.e(TAG, "handleMessage_Ex: " + ex.getMessage());
        }
    }
}
