package cn.udesk.config;


/**
 * Created by user on 2016/8/12.
 */
public class UdeskConfig {

    public static final int DEFAULT = -1;

    // 标题栏TitleBar的背景色  通过颜色设置
    public static int udeskTitlebarBgResId = DEFAULT;

    // 标题栏TitleBar，左右两侧文字的颜色
    public static int udeskTitlebarTextLeftRightResId = DEFAULT;

    //IM界面，左侧文字的字体颜色
    public static int udeskIMLeftTextColorResId = DEFAULT;

    //IM界面，右侧文字的字体颜色
    public static int udeskIMRightTextColorResId = DEFAULT;

    //IM界面，左侧客服昵称文字的字体颜色
    public static int udeskIMAgentNickNameColorResId = DEFAULT;

    //IM界面，时间文字的字体颜色
    public static int udeskIMTimeTextColorResId = DEFAULT;

    // IM界面，提示语文字的字体颜色，比如客服转移
    public static int udeskIMTipTextColorResId = DEFAULT;

    // 返回箭头图标资源id
    public static int udeskbackArrowIconResId = DEFAULT;

    // 咨询商品item的背景颜色
    public static int udeskCommityBgResId = DEFAULT;

    //    商品介绍Title的字样颜色
    public static int udeskCommityTitleColorResId = DEFAULT;

    //  商品咨询页面中，商品介绍子Title的字样颜色
    public static int udeskCommitysubtitleColorResId = DEFAULT;

    //    商品咨询页面中，发送链接的字样颜色
    public static int udeskCommityLinkColorResId = DEFAULT;

    //配置 是否使用推送服务  true 表示使用  false表示不使用
    public  static  boolean isUserSDkPush = true;

    //配置放弃排队的策略
    public  static  String  UdeskQuenuMode = UdeskQuenuFlag.Mark;

    //配置开启留言时的    留言表单留言提示语
    public  static  String  UdeskLeavingMsg = "";

    //配置是否把domain 和 appid 和 appkey 和 sdktoken 存在sharePrefence中， ftrue保存，false 不存
    public  static  boolean  isUseShare = true;

    //mode: mark (默认,标记放弃)/ cannel_mark(取消标记) / force_quit(强制立即放弃)
    public static  class  UdeskQuenuFlag{
        public static final String Mark ="mark";
        public static final String FORCE_QUIT = "force_quit";
        public static final String CANNEL_MARK = "cannel_mark";
    }

    public static class UdeskPushFlag{
        public static final String ON ="on";
        public static final String OFF ="off";
    }
}
