package com.ozner.cup.Chat;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.ozner.cup.Base.BaseFragment;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;

public class ChatFragment extends BaseFragment {


    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * 实例化Fragment
     *
     * @param bundle
     *
     * @return
     */
    public static ChatFragment newInstance(Bundle bundle) {
        ChatFragment fragment = new ChatFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
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
            ((MainActivity) getActivity()).setCustomTitle(R.string.chat);
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
