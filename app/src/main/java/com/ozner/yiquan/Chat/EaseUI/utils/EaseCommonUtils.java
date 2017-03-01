/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ozner.yiquan.Chat.EaseUI.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.ozner.yiquan.Chat.EaseUI.model.MessageDirect;
import com.ozner.yiquan.Chat.EaseUI.model.MessageType;
import com.ozner.yiquan.DBHelper.EMMessage;
import com.ozner.yiquan.R;

import java.util.List;

public class EaseCommonUtils {
	private static final String TAG = "CommonUtils";
	/**
	 * check if network avalable
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetWorkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();
			}
		}

		return false;
	}

	/**
	 * check if sdcard exist
	 * 
	 * @return
	 */
	public static boolean isSdcardExist() {
		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}
	
	public static EMMessage createExpressionMessage(String toChatUsername, String expressioName, String identityCode){
//	    EMMessage message = EMMessage.createTxtSendMessage("["+expressioName+"]", toChatUsername);
//        if(identityCode != null){
//            message.setAttribute(EaseConstant.MESSAGE_ATTR_EXPRESSION_ID, identityCode);
//        }
//        message.setAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, true);
        return null;
	}

	/**
     * Get digest according message type and content
     * 
     * @param message
     * @param context
     * @return
     */
    public static String getMessageDigest(EMMessage message, Context context) {
        String digest = "";
        switch (message.getMType()) {
        case MessageType.LOCATION:
            if (message.getMDirect() == MessageDirect.RECEIVE) {
                digest = getString(context, R.string.location_recv);
//                digest = String.format(digest, message.getFrom());
                return digest;
            } else {
                digest = getString(context, R.string.location_prefix);
            }
            break;
        case MessageType.IMAGE:
            digest = getString(context, R.string.picture);
            break;
        case MessageType.VOICE:
            digest = getString(context, R.string.voice_prefix);
            break;
        case MessageType.VIDEO:
            digest = getString(context, R.string.video);
            break;
        case MessageType.TXT:
//            EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
//            if(message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_VOICE_CALL, false)){
//                digest = getString(context, R.string.voice_call) + txtBody.getMessage();
//            }else if(message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_VIDEO_CALL, false)){
//                digest = getString(context, R.string.video_call) + txtBody.getMessage();
//            }else if(message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)){
//                if(!TextUtils.isEmpty(txtBody.getMessage())){
//                    digest = txtBody.getMessage();
//                }else{
//                    digest = getString(context, R.string.dynamic_expression);
//                }
//            }else{
//                digest = txtBody.getMessage();
//            }
            break;
        case MessageType.FILE:
            digest = getString(context, R.string.file);
            break;
        default:
            Log.e(TAG, "getMessageDigest: error, unknow type");
            return "";
        }

        return digest;
    }
    
    static String getString(Context context, int resId){
        return context.getResources().getString(resId);
    }
	
	/**
	 * get top activity
	 * @param context
	 * @return
	 */
	public static String getTopActivity(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

		if (runningTaskInfos != null)
			return runningTaskInfos.get(0).topActivity.getClassName();
		else
			return "";
	}

}
