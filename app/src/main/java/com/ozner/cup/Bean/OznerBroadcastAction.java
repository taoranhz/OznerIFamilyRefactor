package com.ozner.cup.Bean;

/**
 * Created by ozner_67 on 2016/11/4.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 广播Action
 */

public class OznerBroadcastAction {
    //后台服务初始化完成
    public static final String OBA_Service_Init = "ozner.service.init";

    //切换到商城
    public static final String OBA_SWITCH_ESHOP = "ozner.switch.eshop";

    //切换到咨询
    public static final String OBA_SWITCH_CHAT = "ozner.switch.chat";

    //绑定百度推送设备id
    public static final String OBA_BDBind = "ozner_bdpush_bind";

    //接收到咨询消息
    public static final String OBA_RECEIVE_CHAT_MSG="ozner_recieve_chat_msg";

    //个人中心我的设备选择
    public static final String OBA_CenterDeviceSelect = "ozner_center_deivce_select";
    
}
