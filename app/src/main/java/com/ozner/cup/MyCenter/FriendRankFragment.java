package com.ozner.cup.MyCenter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ozner.cup.Base.BaseFragment;
import com.ozner.cup.Base.CommonAdapter;
import com.ozner.cup.Base.CommonViewHolder;
import com.ozner.cup.DBHelper.FriendRankItem;
import com.ozner.cup.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class FriendRankFragment extends BaseFragment {

    @InjectView(R.id.lv_rank)
    ListView lvRank;
    RankAdapter rankAdapter;

    public static FriendRankFragment newInstance(Bundle bundle) {
        FriendRankFragment fragment = new FriendRankFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        rankAdapter = new RankAdapter(getContext(), R.layout.friend_rank_item);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend_rank, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        lvRank.setAdapter(rankAdapter);
        initTestData();
        super.onActivityCreated(savedInstanceState);
    }

    private void initTestData(){
        List<FriendRankItem>  dataList = new ArrayList<>();
        for (int i=0;i<10;i++){
            FriendRankItem item = new FriendRankItem();
            dataList.add(item);
        }
        rankAdapter.loadData(dataList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    class RankAdapter extends CommonAdapter<FriendRankItem> {

        public RankAdapter(Context context, int itemLayoutId) {
            super(context, itemLayoutId);
        }

        @Override
        public void convert(CommonViewHolder holder, FriendRankItem item, int position) {

        }
    }
}
