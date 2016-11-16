package com.ozner.cup.EShop;

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

public class EShopFragment extends BaseFragment {
    public EShopFragment() {
        // Required empty public constructor
    }

    /**
     * 实例化Fragment
     *
     * @param bundle
     *
     * @return
     */
    public static EShopFragment newInstance(Bundle bundle) {
        EShopFragment fragment = new EShopFragment();
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
        return inflater.inflate(R.layout.fragment_eshop, container, false);
    }
    @Override
    public void onAttach(Context context) {
        ((MainActivity)context).setCustomTitle("商城");
        super.onAttach(context);
    }
}
