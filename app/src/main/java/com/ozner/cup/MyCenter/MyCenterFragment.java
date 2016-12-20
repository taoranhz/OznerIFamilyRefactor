package com.ozner.cup.MyCenter;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.ozner.cup.Base.BaseFragment;
import com.ozner.cup.Chat.EaseUI.utils.MessageCreator;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.EMMessage;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;

/**
 * Created by ozner_67 on 2016/11/1.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class MyCenterFragment extends BaseFragment {
    private static final String TAG = "MyCenterFragment";
//    String testHtml = "<div style=\"font-size:14px;font-family:微软雅黑\">\\n<img class=\"imgEmotion\" src=\"http://192.168.172.21/templates/common/images/64.gif\" data-title=\"凋谢\">测试<img class=\"imgEmotion\" src=\"http://192.168.172.21/templates/common/images/22.gif\" data-title=\"白眼\"></div>";
    String testHtml = "<div style=\"font-size:14px;font-family:微软雅黑\"><img id=\"imgUpload\" src=\"http://192.168.172.21//upload/6F8X_1481867987.PNG_600_600.PNG\"></div>";
    EMMessage testEmsg;
    String userid;

    public MyCenterFragment() {
        // Required empty public constructor
    }

    /**
     * 实例化Fragment
     *
     * @param bundle
     *
     * @return
     */
    public static MyCenterFragment newInstance(Bundle bundle) {
        MyCenterFragment fragment = new MyCenterFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        userid = UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, "");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_center, container, false);
    }

    @Override
    public void onAttach(Context context) {
//        try {
//            ((MainActivity) context).setCustomTitle(R.string.mine);
//        } catch (Exception ex) {
//
//        }
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        System.gc();
        super.onDetach();
    }

    @Override
    public void onResume() {
        try {
            setBarColor(R.color.colorAccent);
            setToolbarColor(R.color.colorAccent);
            ((MainActivity) getActivity()).setCustomTitle(R.string.mine);

            testEmsg = MessageCreator.transMsgNetToLocal(userid, testHtml);
            Log.e(TAG, "onResume:TestEMsg: " + testEmsg.getContent());
        } catch (Exception ex) {

        }
        super.onResume();
    }

    /**
     * 设置状态栏颜色
     */
    protected void setBarColor(int resId) {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = ((MainActivity) getActivity()).getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(getContext(), resId));
        }
    }

    /**
     * 设置主界面toolbar背景色
     *
     * @param resId
     */
    protected void setToolbarColor(int resId) {
        ((MainActivity) getActivity()).setToolBarColor(resId);
    }
}
