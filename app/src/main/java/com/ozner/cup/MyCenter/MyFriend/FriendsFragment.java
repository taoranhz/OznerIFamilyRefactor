package com.ozner.cup.MyCenter.MyFriend;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ozner.cup.Base.BaseFragment;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.MyCenter.MyFriend.bean.FriendItem;
import com.ozner.cup.MyCenter.MyFriend.bean.LeaveMessageItem;
import com.ozner.cup.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class FriendsFragment extends BaseFragment implements ExpandableListView.OnGroupClickListener,
        ExpandableListView.OnChildClickListener {
    private static final String TAG = "FriendsFragment";
    @InjectView(R.id.llay_noFriend)
    RelativeLayout llayNoFriend;
    @InjectView(R.id.elv_friend)
    ExpandableListView elvFriend;
    @InjectView(R.id.et_msg)
    EditText etMsg;
    @InjectView(R.id.btn_sendMsg)
    Button btnSendMsg;
    @InjectView(R.id.llay_sendMsg)
    LinearLayout llaySendMsg;
    private FriendExpandListAdapter mFriendAdapter;
    private List<LeaveMessageItem> leaveMsgList;
    private List<FriendItem> friendList;
    private int curGroupPos = -1;
    private String mUserid, mMobile;
    private FriendInfoManager infoManager;
    private String curFriendUserid;
    private HashMap<String, List<LeaveMessageItem>> leaveMsgMap;


    public static FriendsFragment newInstance(Bundle bundle) {
        FriendsFragment fragment = new FriendsFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            mUserid = UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, "");
            mMobile = DBManager.getInstance(getContext()).getUserInfo(mUserid).getMobile();
        } catch (Exception ex) {

        }
        infoManager = new FriendInfoManager(getContext(), null);
        mFriendAdapter = new FriendExpandListAdapter(getContext());
//        leaveMsgList = new ArrayList<>();
        friendList = new ArrayList<>();
        leaveMsgMap = new HashMap<>();
//        Log.e(TAG, "onCreate: "+friendList.size());
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        Log.e(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        Log.e(TAG, "onActivityCreated: ");
        super.onActivityCreated(savedInstanceState);
        etMsg.addTextChangedListener(new MyTextWatch());
        elvFriend.setEmptyView(llayNoFriend);
        elvFriend.setAdapter(mFriendAdapter);
        elvFriend.setOnGroupClickListener(this);
        elvFriend.setOnChildClickListener(this);
        //设置展开监听
        elvFriend.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                showSendMsgView();
            }
        });
        //设置关闭监听
        elvFriend.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                hideSendMsgView();
            }
        });

        if (friendList.size() == 0) {
            infoManager.getFriendList(new FriendInfoManager.FriendListListener() {
                @Override
                public void onSuccess(List<FriendItem> result) {
                    friendList = result;
                    mFriendAdapter.loadGroupData(friendList);
                }

                @Override
                public void onFail(String msg) {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            mFriendAdapter.loadGroupData(friendList);
        }
//        initGroupTest();
//        initChildTest();
    }

    private void initGroupTest() {
//        List<FriendItem> friendlist = new ArrayList<>();
        friendList.clear();
        for (int i = 0; i < 20; i++) {
            FriendItem item = new FriendItem();
            item.setMobile("13166398575");
            item.setFriendMobile("15236193732");
            item.setNickname("凌晨 " + i);
            item.setId(i);
            item.setMessageCount(i);
            item.setIcon("http://h.hiphotos.baidu.com/image/pic/item/a8ec8a13632762d0600802bfa2ec08fa513dc6f8.jpg");
            friendList.add(item);
        }
        mFriendAdapter.loadGroupData(friendList);
    }
//
//    String[] testStr = new String[]{
//            "熟悉的巷弄间 记忆中的经典",
//            "你牵车走左边 脚步为我慢一些",
//            "你右边的侧脸 我心动的景点",
//            "陪我回家的路 最好能远一点",
//            "你说遇见一个人 给你好多的快乐",
//            "对你来说 这比喜欢更温柔",
//            "你摊开了我的手 写下答案就是我",
//            "对我来说 那是绝版的感动",
//            "天使经过身边 最单纯那几年",
//            "一个人好 两个人都会笑",
//            "天使经过身边 像白纸的初恋",
//            "牢牢记得 你没说的你爱我"
//    };

//    private void initChildTest() {
//        leaveMsgList.clear();
//        for (int i = 0; i < 6; i++) {
//            LeaveMessageItem item = new LeaveMessageItem();
//            item.setIcon("http://v1.qzone.cc/avatar/201401/17/14/10/52d8c966e1d74080.jpg%21200x200.jpg");
//            item.setId(i);
//            item.setMobile("15236193732");
//            item.setMessage(testStr[i]);
//            if (i % 2 == 0) {
//                item.setNickname("小四");
//                item.setRecvuserid(mUserid);
//                item.setSenduserid("987654321");
//            } else {
//                item.setNickname("小四");
//                item.setRecvuserid("987654321");
//                item.setSenduserid(mUserid);
//            }
//            if (i < 3) {
//                item.setStime("/Date(1451232000000)/");
//            } else {
//                item.setStime("1483064151000");
//            }
//            leaveMsgList.add(item);
//        }
//    }

    @OnClick(R.id.btn_sendMsg)
    public void onClick() {
        leaveMessage(etMsg.getText().toString().trim());
    }


    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        curGroupPos = groupPosition;
        //将已经展开的关闭掉
        int groupCount = mFriendAdapter.getGroupCount();
        for (int i = 0; i < groupCount; i++) {
            if (i != groupPosition) {
                parent.collapseGroup(i);
            }
        }

        if (!parent.isGroupExpanded(groupPosition)) {
            //点击的item没有展开
            if (friendList.get(groupPosition).getCreateBy().equals(mUserid)) {
                curFriendUserid = friendList.get(groupPosition).getModifyBy();
            } else if (friendList.get(groupPosition).getModifyBy().equals(mUserid)) {
                curFriendUserid = friendList.get(groupPosition).getCreateBy();
            }
            if (leaveMsgMap.containsKey(curFriendUserid)) {
                mFriendAdapter.loadChildData(groupPosition, leaveMsgMap.get(curFriendUserid));
                parent.expandGroup(groupPosition, true);
            } else {
                getHistoryMessage(curFriendUserid);
            }
        } else {
            parent.collapseGroup(groupPosition);
        }
        return true;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        showSendMsgView();
        return true;
    }

    /**
     * 加载历史留言
     *
     * @param otherUserid
     */
    private void getHistoryMessage(String otherUserid) {
        infoManager.getHistoryMessage(otherUserid, new FriendInfoManager.HistoryMsgListener() {
            @Override
            public void onSuccess(List<LeaveMessageItem> result) {
                if (result.size() > 0) {
                    leaveMsgMap.put(curFriendUserid, result);
                    mFriendAdapter.loadChildData(curGroupPos, result);
                    if (!elvFriend.isGroupExpanded(curGroupPos)) {
                        elvFriend.expandGroup(curGroupPos, true);
                    }
                } else {
                    List<LeaveMessageItem> msgList = new ArrayList<LeaveMessageItem>();
                    LeaveMessageItem item = new LeaveMessageItem();
                    item.setMessage(getString(R.string.no_leave_msg));
                    msgList.add(item);
                    mFriendAdapter.loadChildData(curGroupPos,msgList);
                    if (!elvFriend.isGroupExpanded(curGroupPos)) {
                        elvFriend.expandGroup(curGroupPos, true);
                    }
                }
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 留言
     *
     * @param msg
     */
    private void leaveMessage(final String msg) {
        infoManager.leaveMessage(curFriendUserid, msg, new FriendInfoManager.LeaveMsgListener() {
            @Override
            public void onSuccess() {
                List<LeaveMessageItem> leaveMsgList;
                if (leaveMsgMap.containsKey(curFriendUserid)) {
                    leaveMsgList = leaveMsgMap.get(curFriendUserid);
                } else {
                    leaveMsgList = new ArrayList<LeaveMessageItem>();
                }
                FriendItem friendItem = friendList.get(curGroupPos);
                LeaveMessageItem item = new LeaveMessageItem();
                item.setIcon(friendItem.getIcon());
                item.setMessage(msg);
                if (friendItem.getMobile().equals(mMobile)) {
                    item.setMobile(friendItem.getFriendMobile());
                } else {
                    item.setMobile(friendItem.getMobile());
                }

                item.setNickname(friendItem.getNickname());
                item.setRecvuserid(curFriendUserid);
                item.setSenduserid(mUserid);
                item.setStime(String.valueOf(System.currentTimeMillis()));
                leaveMsgList.add(item);
                leaveMsgMap.put(curFriendUserid, leaveMsgList);
                friendList.get(curGroupPos).setMessageCount(friendList.get(curGroupPos).getMessageCount() + 1);
                mFriendAdapter.loadChildData(curGroupPos, leaveMsgList);
                etMsg.setText("");
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 显示发送信息布局
     */
    private void showSendMsgView() {
        llaySendMsg.setVisibility(View.VISIBLE);
        etMsg.setFocusable(true);
        etMsg.requestFocus();

        if (curGroupPos > -1) {
            FriendItem friendInfo = friendList.get(curGroupPos);
            String replay = "";
            if (friendInfo.getMobile().equals(mMobile)) {
                replay = friendInfo.getNickname() != null && !friendInfo.getNickname().isEmpty()
                        ? friendInfo.getNickname() : friendInfo.getFriendMobile();
            } else {
                replay = friendInfo.getNickname() != null && !friendInfo.getNickname().isEmpty()
                        ? friendInfo.getNickname() : friendInfo.getMobile();
            }
            etMsg.setHint(String.format(getString(R.string.replay), replay));
        }

    }

    /**
     * 隐藏发送信息布局
     */
    private void hideSendMsgView() {
        llaySendMsg.setVisibility(View.GONE);
        etMsg.clearFocus();
        hideSoftInputView();
    }

    /**
     * 隐藏输入法
     */
    public void hideSoftInputView() {
        if (this.isAdded()) {
            InputMethodManager manager = ((InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE));
            if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                if (getActivity().getCurrentFocus() != null)
                    manager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onDetach() {
        System.gc();
        super.onDetach();
    }


    /**
     * 信息输入监听
     */
    class MyTextWatch implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().trim().length() > 0) {
                btnSendMsg.setEnabled(true);
            } else {
                btnSendMsg.setEnabled(false);
            }
        }
    }
}
