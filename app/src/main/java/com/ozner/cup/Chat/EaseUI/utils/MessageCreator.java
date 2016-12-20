package com.ozner.cup.Chat.EaseUI.utils;

import android.text.Spannable;
import android.text.Spanned;
import android.util.Log;

import com.ozner.cup.Chat.ChatHttpUtils.ChatHttpBean;
import com.ozner.cup.Chat.EaseUI.model.MessageDirect;
import com.ozner.cup.Chat.EaseUI.model.MessageStatus;
import com.ozner.cup.Chat.EaseUI.model.MessageType;
import com.ozner.cup.DBHelper.EMMessage;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.Calendar;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ozner_67 on 2016/12/13.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 消息生成器
 */

public class MessageCreator {
    private static final String TAG = "MessageCreator";

    private static String StartDiv = "<div style=\"font-size:14px;font-family:微软雅黑\">";
    private static String EndDiv = "</div>";
    private static final Spannable.Factory spannableFactory = Spannable.Factory
            .getInstance();

    /**
     * 将接收到的信息转换成本地格式
     *
     * @param receiveMsg
     *
     * @return
     */
    public static EMMessage transMsgNetToLocal(@NotNull String userid, @NotNull String receiveMsg) {
        EMMessage eMsg = new EMMessage();

        String tempMsg = receiveMsg;
        try {
            eMsg.setStatus(MessageStatus.SUCCESS);
            eMsg.setTime(Calendar.getInstance().getTimeInMillis());
            eMsg.setUserid(userid);
            eMsg.setMDirect(MessageDirect.RECEIVE);
            eMsg.setMType(MessageType.TXT);

            if (!tempMsg.isEmpty() && tempMsg.contains("<div")) {
                String firstStr = "<div style=\"font-size:14px;font-family:微软雅黑\">";
                int last = tempMsg.lastIndexOf("</div>");

                tempMsg = tempMsg.substring(firstStr.length(), last);
                tempMsg = tempMsg.replace("<div><br></div>", "\n");

                tempMsg = tempMsg.replace("<br>", "");
                tempMsg = tempMsg.replace("<div>", "");
                tempMsg = tempMsg.replace("</div>", "");
                tempMsg = tempMsg.replace("&nbsp;", " ");
                tempMsg = tempMsg.trim();
                Log.e(TAG, "transMsgNetToLocal: tempMsg:" + tempMsg);
                String msgTemp = tempMsg.trim();
//                Pattern p = Pattern.compile("<img.*?src=\\\".*?\\\".*? .*?>");
                if (tempMsg.contains(".gif")) {
                    Pattern p = Pattern.compile("<img.*?src=\\\".*?>");
                    Matcher m = p.matcher(tempMsg);
                    if (m != null) {
                        while (m.find()) {
                            try {
                                String imgtag = m.group();
//                        Log.e("match", "imgtag:" + imgtag);
                                Pattern patSrc = Pattern.compile("http:.*?\\.gif");
                                Matcher mSrc = patSrc.matcher(imgtag);
                                if (mSrc != null && mSrc.find()) {
                                    String matchSrc = mSrc.group();
//                            Log.e("match", "matchSrc:" + matchSrc);
                                    int start = matchSrc.lastIndexOf("/") + 1;
                                    int end = matchSrc.indexOf(".gif");
                                    int index = Integer.parseInt(matchSrc.substring(start, end));
                                    StringBuilder giftag = new StringBuilder();
                                    giftag.append("[f_");
                                    if (index < 10) {
                                        giftag.append("0");
                                    }
                                    if (index < 100) {
                                        giftag.append("0");
                                    }
                                    giftag.append(index);
                                    giftag.append("]");
//                                String resStr = "#[face/png/f_static_" + num + ".png]#";
//                            Log.e("match", "index:" + index + " , " + resStr);
                                    msgTemp = msgTemp.replace(imgtag, giftag.toString());
                                } else {
                                    Log.e("match", "mSrc is null or not find");
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();

                            }
                        }
                    } else {
                        Log.e("match", "m is null");
                    }
                } else if (tempMsg.contains(".PNG")
                        ||tempMsg.contains(".JPEG")
                        ||tempMsg.contains(".jpg")
                        ||tempMsg.contains(".JPG")){
                    eMsg.setMType(MessageType.IMAGE);
                    Pattern p = Pattern.compile("<img.*?src=\\\"(.*).*\\\"");
                    Matcher m = p.matcher(tempMsg);
                    if (m != null) {
                        while (m.find()) {
                            String imagepath = m.group(1);
//                    Log.e("tag", "imagePath_Pre:" + imagepath);
                            int start = imagepath.indexOf("\"");
                            if (start >= 0)
                                imagepath = imagepath.substring(0, start);

//                            path.add(imagepath);
                            msgTemp = imagepath;
                        }
                    }
                }
                tempMsg = msgTemp;
            }
        } catch (Exception ex) {
            Log.e(TAG, "transMsgNetToLocal_Ex: " + ex.getMessage());
        } finally {
            eMsg.setContent(tempMsg);
            return eMsg;
        }
    }


    /**
     * 创建文本信息
     *
     * @param content
     *
     * @return
     */
    public static String createTextMessage(String content) {
        Spannable spannable = spannableFactory.newSpannable(content);
        String tempMsg = content;

        for (Map.Entry<Pattern, Object> entry : EaseSmileUtils.getEmoticonEntrySet()) {
            Matcher matcher = entry.getKey().matcher(spannable);
            while (matcher.find()) {
                String reaplceStr = spannable.toString().substring(matcher.start(), matcher.end()).substring(3, 6);
                int imgid = Integer.parseInt(reaplceStr);
                StringBuilder replaceSB = new StringBuilder();
                replaceSB.append("<img class=\"imgEmotion\" src=\"")
                        .append(ChatHttpBean.ChatBaseUrl)
                        .append("/templates/common/images/")
                        .append(String.valueOf(imgid))
                        .append(".gif\" >");
                spannable.setSpan(replaceSB.toString(), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tempMsg = tempMsg.replace(content.substring(matcher.start(), matcher.end()), replaceSB.toString());

            }
        }
        tempMsg = StartDiv + tempMsg + EndDiv;
        Log.e(TAG, "createTextMessage: " + tempMsg);

        return tempMsg;
    }
}
