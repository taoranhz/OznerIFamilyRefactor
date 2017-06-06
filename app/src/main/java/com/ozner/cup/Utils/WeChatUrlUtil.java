package com.ozner.cup.Utils;

import com.ozner.cup.Bean.Contacts;

/**
 * Created by xinde on 2016/1/11.
 */
public class WeChatUrlUtil {


    private static String getformatUrl(String gourl) {
        String result = Contacts.weChatBaseUrl + "mobile=%s&UserTalkCode=%s&Language=%s&Area=%s&goUrl=" + gourl;
        return result;
    }

    //格式化我的小金库url
    public static String formatMyMoneyUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(Contacts.myMoneyUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化我的订单url
    public static String formatMyOrderUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(Contacts.myOrderUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化领红包url
    public static String formatRedPacUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(Contacts.getRedPacUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化我的券url
    public static String formatMyTicketUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(Contacts.myTicketUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化分享礼卡url
    public static String formatShareCardUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(Contacts.shareCardUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化商城url
    public static String getMallUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(Contacts.mallUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //RO滤芯商城
    public static String formatRoShopUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(Contacts.roFilterUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //水探头滤芯商城
    public static String formatTapShopUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(Contacts.tapshopUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //空净滤芯商城
    public static String formatKjShopUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(Contacts.kjShopUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化水杯url
    public static String formatFilterCupUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(Contacts.filterCupUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化水探头
    public static String formatFilterTapUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(Contacts.filterTapUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化金色伊泉url
    public static String formatFilterGoldSpringUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(Contacts.filterGoldSpringUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化365安心服务url
    public static String formatSecurityServiceUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(Contacts.securityServiceUrl);
        result = String.format(result, mobile, usertoken, language, area);
        return result;
    }

    //格式化迷你净水器滤芯套餐url
    public static String formatMiniPurifierUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(Contacts.miniPurifierUrl);
        return String.format(result, mobile, usertoken, language, area);
    }

    //格式化台式净水器滤芯套餐url
    public static String formatDeskPurifierUrl(String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(Contacts.deskPurifierUrl);
        return String.format(result, mobile, usertoken, language, area);
    }

    public static String formatUrl(String goUrl, String mobile, String usertoken, String language, String area) {
        String result = getformatUrl(goUrl);
        return String.format(result, mobile, usertoken, language, area);
    }
}
