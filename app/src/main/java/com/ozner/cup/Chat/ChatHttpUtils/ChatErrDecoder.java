package com.ozner.cup.Chat.ChatHttpUtils;

import java.util.HashMap;

/**
 * Created by ozner_67 on 2016/12/15.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 咨询接口返回错误信息
 */

public class ChatErrDecoder {
    private  HashMap<Integer, String> chatErrMap;
    private static ChatErrDecoder mInstance;

    public static ChatErrDecoder getInstance() {
        if (mInstance == null) {
            mInstance = new ChatErrDecoder();
        }
        return mInstance;
    }

    private ChatErrDecoder() {
        initErrMap();
    }

    /**
     * 初始化咨询错误信息
     */
    private void initErrMap() {
        if (null == chatErrMap) {
            chatErrMap = new HashMap<>();
        } else {
            chatErrMap.clear();
        }
        chatErrMap.put(0, "正确");
        chatErrMap.put(1001, "账户信息有误");
        chatErrMap.put(1002, "access_token无效");
        chatErrMap.put(1003, "签名无效");
        chatErrMap.put(1004, "参数错误");
        chatErrMap.put(1005, "access_token过期");
        chatErrMap.put(1006, "操作错误");
        chatErrMap.put(1007, "无返回信息");
    }

    public String getErrMsg(int code) {
        if (chatErrMap.containsKey(code)) {
            return chatErrMap.get(code);
        } else {
            return "";
        }
    }
}
