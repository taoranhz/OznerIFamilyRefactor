package com.ozner.cup.MyCenter.MyFriend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ozner.cup.Base.BaseFragment;
import com.ozner.cup.R;

public class FriendsFragment extends BaseFragment {


    public static FriendsFragment newInstance(Bundle bundle) {
        FriendsFragment fragment = new FriendsFragment();
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
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }


}
