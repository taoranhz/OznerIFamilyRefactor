package com.ozner.cup.MyCenter.MyFriend;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.MyCenter.MyFriend.bean.FriendItem;
import com.ozner.cup.MyCenter.MyFriend.bean.LeaveMessageItem;
import com.ozner.cup.R;
import com.ozner.cup.Utils.DateUtils;
import com.ozner.cup.Utils.LCLogUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by ozner_67 on 2016/12/29.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class FriendExpandListAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "FriendExpandListAdapter";
    private String mUserid, mMobile;
    private WeakReference<Context> mContext;
    private List<FriendItem> friendList;
    private List<LeaveMessageItem> leMsgList;
    private Calendar cal;
    private int theYear;


    public FriendExpandListAdapter(Context context) {
        mContext = new WeakReference<Context>(context);
        friendList = new ArrayList<>();
        leMsgList = new ArrayList<>();
        try {
            mUserid = UserDataPreference.GetUserData(mContext.get(), UserDataPreference.UserId, "");
            mMobile = DBManager.getInstance(mContext.get()).getUserInfo(mUserid).getMobile();
        } catch (Exception ex) {
            LCLogUtils.E(TAG, "FriendExpandListAdapter_Ex" + ex.getMessage());
        }
        cal = Calendar.getInstance();
        theYear = cal.get(Calendar.YEAR);
    }

    /**
     * 加载组数据
     *
     * @param friendInfo
     */
    public void loadGroupData(List<FriendItem> friendInfo) {
        this.friendList.clear();
        this.friendList.addAll(friendInfo);
        this.notifyDataSetChanged();
    }

    /**
     * 加载消息数据
     *
     * @param groupPos
     * @param childData
     */
    public void loadChildData(int groupPos, List<LeaveMessageItem> childData) {
        this.leMsgList.clear();
        this.leMsgList.addAll(childData);
        this.notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return friendList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return leMsgList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return friendList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return leMsgList.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final GroupHolder groupHolder;
        if (convertView == null) {
            groupHolder = new GroupHolder();
            convertView = LayoutInflater.from(mContext.get()).inflate(R.layout.friend_list_item, null);
            groupHolder.iv_headImg = (ImageView) convertView.findViewById(R.id.iv_headImg);
            groupHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            groupHolder.tv_msgNum = (TextView) convertView.findViewById(R.id.tv_msgNum);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }

        Glide.with(mContext.get()).load(friendList.get(groupPosition).getIcon()).asBitmap().placeholder(R.drawable.icon_default_headimage).centerCrop().into(new BitmapImageViewTarget(groupHolder.iv_headImg) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(mContext.get().getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                groupHolder.iv_headImg.setImageDrawable(circularBitmapDrawable);
            }
        });

        if (friendList.get(groupPosition).getNickname() != null && !friendList.get(groupPosition).getNickname().isEmpty()) {
            groupHolder.tv_name.setText(friendList.get(groupPosition).getNickname());
        } else {
            if (friendList.get(groupPosition).getMobile().equals(mMobile)) {
                groupHolder.tv_name.setText(friendList.get(groupPosition).getFriendMobile());
            } else if (friendList.get(groupPosition).getFriendMobile().equals(mMobile)) {
                groupHolder.tv_name.setText(friendList.get(groupPosition).getMobile());
            }
        }

        groupHolder.tv_msgNum.setText(String.valueOf(friendList.get(groupPosition).getMessageCount()));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder childHolder;
        if (convertView == null) {
            childHolder = new ChildHolder();
            convertView = LayoutInflater.from(mContext.get()).inflate(R.layout.friend_list_child_item, null);
            childHolder.tv_message = (TextView) convertView.findViewById(R.id.tv_message);
            childHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            convertView.setTag(childHolder);
        } else {
            childHolder = (ChildHolder) convertView.getTag();
        }

        long time = DateUtils.formatDateFromString(leMsgList.get(childPosition).getStime());
        cal.setTimeInMillis(time);
        int year = cal.get(Calendar.YEAR);
        if (year < theYear) {
            childHolder.tv_time.setText(DateUtils.yearTimeFormat.format(cal.getTime()));
        } else {
            childHolder.tv_time.setText(DateUtils.monthTimeFormat.format(cal.getTime()));
        }

        childHolder.tv_message.setText(leMsgList.get(childPosition).getMessage());
        String from = "";
        String to = "";
        if (leMsgList.get(childPosition).getSenduserid() != null &&
                leMsgList.get(childPosition).getSenduserid().equals(mUserid)) {
            from = "我";
            to = friendList.get(groupPosition).getNickname() != null &&
                    !friendList.get(groupPosition).getNickname().isEmpty()
                    ? friendList.get(groupPosition).getNickname() : leMsgList.get(childPosition).getMobile();
        } else {
            from = friendList.get(groupPosition).getNickname() != null &&
                    !friendList.get(groupPosition).getNickname().isEmpty()
                    ? friendList.get(groupPosition).getNickname() : leMsgList.get(childPosition).getMobile();
        }

        SpannableStringBuilder span_Desc = new SpannableStringBuilder();
        span_Desc.append(from);
        if (to != null && !to.isEmpty()) {
            span_Desc.append("回复");
        }
        to += ":";
        span_Desc.append(to);
        span_Desc.append(leMsgList.get(childPosition).getMessage());
        ForegroundColorSpan fromSpan = new ForegroundColorSpan(ContextCompat.getColor(mContext.get(), R.color.faq_text_blue));
        span_Desc.setSpan(fromSpan, 0, from.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        ForegroundColorSpan toSpan = new ForegroundColorSpan(ContextCompat.getColor(mContext.get(), R.color.faq_text_blue));
        int toStart = span_Desc.toString().indexOf(to);
        span_Desc.setSpan(toSpan, toStart, toStart + to.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        childHolder.tv_message.setText(span_Desc);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    class GroupHolder {
        public TextView tv_name;
        public ImageView iv_headImg;
        public TextView tv_msgNum;
    }

    class ChildHolder {
        public TextView tv_message;
        public TextView tv_time;
    }
}
