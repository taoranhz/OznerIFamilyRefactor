package com.ozner.cup.Device;

import java.util.Date;

/**
 * Created by ozner_67 on 2016/12/2.
 * 邮箱：xinde.zhang@cftcn.com
 */

public interface IFilterStatusView {
    //显示剩余天数
    void showRemainDay(int day);

    //显示剩余百分比
    void showRemainPre(int pre);

    //显示进度条
    void showFilterProgress(Date startTime, Date endTime, Date currentTime);
}
