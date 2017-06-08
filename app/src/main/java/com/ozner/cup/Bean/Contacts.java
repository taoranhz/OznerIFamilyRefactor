package com.ozner.cup.Bean;

/**
 * Created by ozner_67 on 2016/11/1.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class Contacts {
    //登录通知相关参数
    public final static String PARMS_LOGIN_TOKEN = "parms_login_token";
    public final static String PARMS_LOGIN_MIEI = "parms_login_miei";
    public final static String PARMS_LOGIN_USERID = "parms_login_userid";

    //蓝牙配对类型参数
    public final static String PARMS_MATCHTYPE = "parms_matchtype";
    //传递参数mac
    public final static String PARMS_MAC = "parms_mac";
    //传递参数url
    public final static String PARMS_URL = "parms_url";
    //传递饮水量排名
    public final static String PARMS_RANK = "parms_rank";
    //传递手机号
    public final static String PARMS_PHONE = "parms_phone";
    //传递点击位置
    public final static String PARMS_CLICK_POS = "parms_click_pos";
    //排名类型
    public final static String PARMS_RANK_TYPE = "parms_rank_type";
    //activity返回的设备名字
    public final static String RESULT_NAME = "result_name";
    //activity返回的设备位置
    public final static String RESULT_POS = "result_pos";
    //设备使用位置
    public final static String DEV_USE_POS = "device_use_pos";
    //水杯用户体重
    public final static String DEV_USER_WEIGHT = "device_user_weight";
    //饮水目标
    public final static String DEV_USER_WATER_GOAL = "device_user_water_goal";
    //补水仪性别
    public final static String DEV_REPLEN_GENDER = "dev_replen_gender";
    //补水仪上次脸部水分检测结果
    public final static String DEV_REPLEN_FACE_LAST_MOIS = "dev_replen_face_last_mois";
    //补水仪上次脸部油分检测结果
    public final static String DEV_REPLEN_FACE_LAST_OIL = "dev_replen_face_last_oil";
    //补水仪脸部检测总次数
    public final static String DEV_REPLEN_FACE_TEST_COUNT = "dev_replen_face_test_count";
    //补水仪脸部总数据
    public final static String DEV_REPLEN_FACE_MOIS_TOTAL = "dev_replen_face_mois_total";
    //补水仪上次眼部水分检测结果
    public final static String DEV_REPLEN_EYE_LAST_MOIS = "dev_replen_eye_last_mois";
    //补水仪上次眼部油分检测结果
    public final static String DEV_REPLEN_EYE_LAST_OIL = "dev_replen_eye_last_oil";
    //补水仪眼部检测总次数
    public final static String DEV_REPLEN_EYE_TEST_COUNT = "dev_replen_eye_test_count";
    //补水仪眼部总数据
    public final static String DEV_REPLEN_EYE_MOIS_TOTAL = "dev_replen_eye_mois_total";
    //补水仪上次手部水分检测结果
    public final static String DEV_REPLEN_HAND_LAST_MOIS = "dev_replen_hand_last_mois";
    //补水仪上次手部油分检测结果
    public final static String DEV_REPLEN_HAND_LAST_OIL = "dev_replen_hand_last_oil";
    //补水仪手部检测总次数
    public final static String DEV_REPLEN_HAND_TEST_COUNT = "dev_replen_hand_test_count";
    //补水仪手部总数据
    public final static String DEV_REPLEN_HAND_MOIS_TOTAL = "dev_replen_hand_mois_total";
    //补水仪上次颈部水分检测结果
    public final static String DEV_REPLEN_NECK_LAST_MOIS = "dev_replen_neck_last_mois";
    //补水仪上次颈部油分检测结果
    public final static String DEV_REPLEN_NECK_LAST_OIL = "dev_replen_neck_last_oil";
    //补水仪颈部检测总次数
    public final static String DEV_REPLEN_NECK_TEST_COUNT = "dev_replen_neck_test_count";
    //补水仪颈部总数据
    public final static String DEV_REPLEN_NECK_MOIS_TOTAL = "dev_replen_neck_mois_total";
    //补水仪提醒时间
    public final static String DEV_REPLEN_REMIND_TIME_1 = "dev_remind_time_1";
    public final static String DEV_REPLEN_REMIND_TIME_2 = "dev_remind_time_2";
    public final static String DEV_REPLEN_REMIND_TIME_3 = "dev_remind_time_3";
    //补水提醒是否启用
    public final static String DEV_REPLEN_IS_REMIND_1 = "dev_remind_switch_1";
    public final static String DEV_REPLEN_IS_REMIND_2 = "dev_remind_switch_2";
    public final static String DEV_REPLEN_IS_REMIND_3 = "dev_remind_switch_3";

    //水杯今日状态
    public final static String Cup_Today_Status = "cup_today_status";
    //水探头滤芯开始时间
    public final static String TAP_FILTER_START_TIME = "tap_start_time";
    //水探头滤芯使用时间
    public final static String TAP_FILTER_USEDAY = "tap_filter_useday";
    //水探头滤芯信息更新时间
    public final static String TAP_FILTER_UPDATE_TIMEMILLS = "tap_update_timemills";

    //ro水机滤芯地址
    public final static String roFilterUrl="http://www.oznerwater.com/lktnew/wapnew/Mall/goodsDetail.aspx?gid=249";
    //网络请求基础url
    public final static String HttpBaseUrl = "http://app.ozner.net:888/";
//    public final static String HttpBaseUrl = "http://app.joyro.com.cn:8282/";

    //公众号基础url
    public static String weChatBaseUrl = "http://test.oznerwater.com/lktnew/wap/app/Oauth2.aspx?";
//    public static String weChatBaseUrl = "http://www.oznerwater.com/lktnew/wap/app/Oauth2.aspx?";
    //我的小金库
    public static String myMoneyUrl = "http://www.oznerwater.com/lktnew/wapnew/Member/MyCoffers.aspx";
    //我的订单
    public static String myOrderUrl = "http://www.oznerwater.com/lktnew/wapnew/Orders/OrderList.aspx";
    //领红包
    public static String getRedPacUrl = "http://www.oznerwater.com/lktnew/wapnew/Member/GrapRedPackages.aspx";
    public static String getShareHBUrl = "http://www.oznerwater.com/lktnew/wap/wxoauth.aspx?gourl=http://www.oznerwater.com/lktnew/wap/Member/InvitedMemberBrand.aspx";


    //水质检测
    public static String Water_Analysis = "http://erweima.ozner.net:85/index.aspx?tel=%s";

    // 健康水知道
    public static String waterHealthUrl = "http://cup.ozner.net/app/cn/jxszd.html";

    //我的券
    public static String myTicketUrl = "http://www.oznerwater.com/lktnew/wapnew/Member/AwardList.aspx";
    //分享礼卡
    public static String shareCardUrl = "http://www.oznerwater.com/lktnew/wapnew/ShareLk/ShareTicketList.aspx";

    // 商城
    public static String mallUrl = "http://www.oznerwater.com/lktnew/wap/mall/mallHomePage.aspx";

    //水探头滤芯商城
    public static String tapshopUrl = "http://www.oznerwater.com/lktnew/wap/mall/goodsDetail.aspx?gid=39";
    //空净滤芯商城
    public static String kjShopUrl = "http://www.oznerwater.com/lktnew/wap/mall/goodsDetail.aspx?gid=64&il=1";

    //智能杯
    public static String filterCupUrl = "http://www.oznerwater.com/lktnew/wap/mall/goodsDetail.aspx?gid=7";
    //滤芯状态金色伊泉
    public static String filterGoldSpringUrl = "http://www.oznerwater.com/lktnew/wap/mall/goodsDetail.aspx?gid=43";
    //滤芯状态谁探头
    public static String filterTapUrl = "http://www.oznerwater.com/lktnew/wap/mall/goodsDetail.aspx?gid=36";

    //365安心服务
    public static String securityServiceUrl = "http://www.oznerwater.com/lktnew/wap/mall/goodsDetail.aspx?gid=9";
    //迷你净水器滤芯购买链接
    public static String miniPurifierUrl = "http://www.oznerwater.com/lktnew/wap/shopping/confirmOrderFromQrcode.aspx?gid=68";
    //台式净水器滤芯购买链接
//    public static String deskPurifierUrl = "http://www.oznerwater.com/lktnew/wap/shopping/confirmOrderFromQrcode.aspx?gid=69";
    public static String deskPurifierUrl = "http://www.oznerwater.com/lktnew/wap/shopping/confirmOrderFromQrcode.aspx?gid=65";
    //补水仪精华液购买链接
    public static String buyReplenWaterUrl = "http://www.oznerwater.com/lktnew/wap/mall/goodsDetail.aspx?gid=203";

    //空净常见问题
    public static String air_faq = "file:///android_asset/air_faq.html";

    //关于水探头
    public static String aboutTap = "http://cup.ozner.net/app/gystt/gystt.html";
    //关于智能杯
    public static String aboutCup = "http://cup.ozner.net/app/gyznb/gyznb.html";//http://cup.ozner.net/app/us/gyznb_us.html英文版
    //关于净水器
    public static String aboutWaterPurifier = "http://cup.ozner.net/app/gyysj/gyysj.html";
    public static String aboutRo = "http://app.ozner.net:888/RoWaterPurifier.html";
    //关于补水仪
//    public static String aboutWRM = "http://app.ozner.net:888//Public/Index";
    public static String aboutWRM = "http://app.joyro.com.cn:8282/BeautyInstrument.html";
    //关于立式空净
    public static String aboutAirVer = "file:///android_asset/hz_l.html";
    //关于台式空净
    public static String aboutAirDesk = "file:///android_asset/hz_t.html";
    //关于TDS笔
    public static String aboutTdsPen = "file:///android_asset/hz_tdspen.html";

    //ro水机获取充值水卡连接
    public static String roCards="http://192.168.173.9:8025/PlatformTestWebApi/api/order/GetUserOnlineRechargeWaterOrderList";
    public static String roCardsPost="http://192.168.173.9:8025/PlatformTestWebApi/api/order/OnlineRechargeWaterOrderConfirm";
    public static String buyCards="http://test.oznerwater.com/lktnew//wapnew/Hot_product/Hot_list.aspx?typeid=7&flag=1";
    // 免责条款

//    public static String URL_BASE_EXCEPTIONS = "http://cup.ozner.net/app/cn/";

    public static String exceptions_url = "http://cup.ozner.net/app/cn/mzsm.html";
//    public static String exceptions_url = "http://app.joyro.com.cn:8282/jrAPPterm.html";

//    /*
//    *可单独使用链接
//     */
//    //浩泽365安心服务
//    public static String SecurityService = "http://www.oznerwater.com/lktnew/wap/other/FAQGD.aspx";
}
