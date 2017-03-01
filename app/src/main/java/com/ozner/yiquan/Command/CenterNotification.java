package com.ozner.yiquan.Command;

import android.content.Context;

/**
 * Created by xinde on 2016/1/19.
 */
public class CenterNotification {
    //新的排名通知
    public static final int NewRank = 0x01;
    //新的好友通知
    public static final int NewFriend = 0x02;
    //新的验证信息通知
    public static final int NewFriendVF = 0x04;
    //新的留言通知
    public static final int NewMessage = 0x08;

    public static final int DealNewRank = 0xfe;
    public static final int DealNewFriend = 0xfd;
    public static final int DealNewFriendVF = 0xfb;
    public static final int DealNewMessage = 0xf7;

//    public static byte centerNotify = 0;

    public static void setCenterNotify(Context context, int notify) {
        if (notify < 240) {
            byte centerTempNotify = Byte.decode(UserDataPreference.GetUserData(context, UserDataPreference.CenterNotify, "0"));
            centerTempNotify |= notify;
            UserDataPreference.SetUserData(context, UserDataPreference.CenterNotify, String.valueOf(centerTempNotify));
        }
    }

    public static void resetCenterNotify(Context context, int notify) {
        if (notify > 240) {
            byte centerTempNotify = Byte.decode(UserDataPreference.GetUserData(context, UserDataPreference.CenterNotify, "0"));
            centerTempNotify &= notify;
            UserDataPreference.SetUserData(context, UserDataPreference.CenterNotify, String.valueOf(centerTempNotify));
        }
    }

    public static byte getCenterNotifyState(Context context) {
        return Byte.decode(UserDataPreference.GetUserData(context, UserDataPreference.CenterNotify, "0"));
    }
}
