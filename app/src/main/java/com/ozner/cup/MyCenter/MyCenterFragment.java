package com.ozner.cup.MyCenter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ozner.cup.Base.BaseFragment;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;

/**
 * Created by ozner_67 on 2016/11/1.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class MyCenterFragment extends BaseFragment {
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
        ((MainActivity)context).setCustomTitle("我的");
        super.onAttach(context);
    }
}
