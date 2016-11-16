package com.ozner.cup.Chat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    public void onAttach(Context context) {
        ((MainActivity)context).setCustomTitle("咨询");
        super.onAttach(context);
    }

}
