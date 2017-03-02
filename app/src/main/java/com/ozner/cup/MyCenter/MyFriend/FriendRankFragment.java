package com.ozner.cup.MyCenter.MyFriend;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.nineoldandroids.view.ViewHelper;
import com.ozner.cup.Base.BaseFragment;
import com.ozner.cup.Base.CommonAdapter;
import com.ozner.cup.Base.CommonViewHolder;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Bean.RankType;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.FriendRankItem;
import com.ozner.cup.R;
import com.ozner.cup.Utils.DateUtils;
import com.ozner.cup.Utils.LCLogUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.ozner.cup.R.string.rank_cup_tds;
import static com.ozner.cup.R.string.rank_cup_vol;


public class FriendRankFragment extends BaseFragment implements IRankView, AdapterView.OnItemLongClickListener {
    private static final String TAG = "FriendRankFragment";

    @InjectView(R.id.lv_rank)
    ListView lvRank;
    RankAdapter rankAdapter;
    @InjectView(R.id.tv_data_empty)
    TextView tvDataEmpty;
    private String userid;
    private FriendInfoManager infoManager;
    List<FriendRankItem> dataList = new ArrayList<>();

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
        userid = UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, "");
        infoManager = new FriendInfoManager(getContext(), this);
        infoManager.loadFriendRank();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend_rank, container, false);
        ButterKnife.inject(this, view);
        lvRank.setEmptyView(tvDataEmpty);
        lvRank.setOnItemLongClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        lvRank.setAdapter(rankAdapter);
        refreshRankData();
//        initTestData();
        super.onActivityCreated(savedInstanceState);
    }

//    /**
//     * 测试数据
//     */
//    private void initTestData() {
//
//        dataList.clear();
//        for (int i = 0; i < 10; i++) {
//            FriendRankItem item = new FriendRankItem();
//            item.setType(RankType.CupType);
//            item.setIcon("http://e.hiphotos.baidu.com/zhidao/wh%3D450%2C600/sign=bd008c2468061d957d133f3c4ec426e7/dcc451da81cb39dba2fc924ad6160924ab1830e9.jpg");
//            item.setId(String.valueOf(i));
//            item.setLikenumaber(String.valueOf(i));
//            item.setMax(String.valueOf(i * 5 + 3));
//            item.setNotime(String.valueOf(Calendar.getInstance().getTimeInMillis()));
//            item.setUserid(userid);
//            item.setVuserid(userid);
//            item.setRank(String.valueOf(i));
//            item.setNotify("1");
//            dataList.add(item);
//        }
//        rankAdapter.loadData(dataList);
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void refreshRankData() {
        try {
            dataList.clear();
            dataList = DBManager.getInstance(getContext()).getFriendRankList(userid);
            rankAdapter.loadData(dataList);
        } catch (Exception ex) {
            LCLogUtils.E(TAG, "refreshRankData_Ex: " + ex.getMessage());
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
        new AlertDialog.Builder(getContext(), AlertDialog.THEME_HOLO_LIGHT)
                .setMessage(R.string.is_delete)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            DBManager.getInstance(getContext()).deleteFriendRank(rankAdapter.getItem(position));
                            rightRemoveAnimation(view, position);
                        } catch (Exception ex) {
                            LCLogUtils.E(TAG, "onItemLongClick_Ex:" + ex.getMessage());
                        }
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
        return true;
    }


    /**
     * item删除动画
     */
    private void rightRemoveAnimation(final View view, final int position) {
        final Animation animation = (Animation) AnimationUtils.loadAnimation(
                getContext(), R.anim.chatto_remove_anim);
        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                view.setAlpha(0);
                performDismiss(view, position);
                animation.cancel();
            }
        });

        view.startAnimation(animation);
    }

    /**
     * 在此方法中执行item删除之后，其他的item向上或者向下滚动的动画，并且将position回调到方法onDismiss()中
     *
     * @param dismissView
     * @param dismissPosition
     */
    private void performDismiss(final View dismissView, final int dismissPosition) {
        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();// 获取item的布局参数
        final int originalHeight = dismissView.getHeight();// item的高度

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0)
                .setDuration(500);
        animator.start();

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                rankAdapter.remove(dismissPosition);
                rankAdapter.notifyDataSetChanged();

                // 这段代码很重要，因为我们并没有将item从ListView中移除，而是将item的高度设置为0
                // 所以我们在动画执行完毕之后将item设置回来
                ViewHelper.setAlpha(dismissView, 1f);
                ViewHelper.setTranslationX(dismissView, 0);
                ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
                lp.height = originalHeight;
                dismissView.setLayoutParams(lp);
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // 这段代码的效果是ListView删除某item之后，其他的item向上滑动的效果
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                dismissView.setLayoutParams(lp);
            }
        });

    }

    @Override
    public void onDetach() {
        System.gc();
        super.onDetach();
    }


    class RankAdapter extends CommonAdapter<FriendRankItem> {
        private final String[] monthsStr = {"Jan.", "Feb.", "Mar.", "Apr.", "May", "Jun.", "Jul.", "Aug.", "Sep.", "Oct.", "Nov.", "Dec."};
        private Calendar calendar;

        public RankAdapter(Context context, int itemLayoutId) {
            super(context, itemLayoutId);
            calendar = Calendar.getInstance();
        }

        public void remove(int index) {
            this.removeData(index);
        }

        @Override
        public void convert(CommonViewHolder holder, FriendRankItem item, int position) {
            if (item != null) {
                holder.setText(R.id.tv_title, getTitleStrId(item.getType()));
                holder.setImageResource(R.id.iv_deviceIcon, getTitleIconResId(item.getType()));
                holder.setText(R.id.tv_rankValue, item.getRank());
                holder.setText(R.id.tv_tdsBestValue, item.getMax());
                holder.setText(R.id.tv_lickNum, item.getLikenumaber());
                holder.getView(R.id.llay_bottom).setOnClickListener(new ItemClick(item.getType()));
                holder.getView(R.id.llay_likeNum).setOnClickListener(new ItemClick(item.getType()));
                if (item.getLikenumaber().equals("0")) {
                    holder.setTextColor(R.id.tv_lickNum, Color.GRAY);
                } else {
                    holder.setTextColor(R.id.tv_lickNum, ContextCompat.getColor(mContext, R.color.new_message_red));
                }
                final ImageView ivHeadImg = holder.getView(R.id.iv_firstImg);

                if (item.getIcon() != null && !item.getIcon().isEmpty()) {
                    Glide.with(getContext()).load(item.getIcon()).asBitmap().placeholder(R.drawable.icon_default_headimage).centerCrop().into(new BitmapImageViewTarget(ivHeadImg) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            ivHeadImg.setImageDrawable(circularBitmapDrawable);

                        }
                    });
                }
                String you = "";
                if (item.getUserid() != null && item.getUserid().equals(userid)) {
                    you += getString(R.string.nin);
                }
                calendar.setTimeInMillis(DateUtils.formatDateFromString(item.getNotime()));
                int month = calendar.get(Calendar.MONTH) + 1;
                String monthStr = "";
                if (month < 10) {
                    monthStr += "0";
                }
                monthStr += String.valueOf(month);
                monthStr = String.format(getString(R.string.obtainChamp), you, monthStr);
                holder.setText(R.id.tv_first_desc, monthStr);
            }
        }

        /**
         * 获取标题字符串id
         *
         * @param type
         *
         * @return
         */
        private int getTitleStrId(String type) {
            int titleid = rank_cup_tds;
            switch (type) {
                case RankType.CupType:
                    titleid = rank_cup_tds;
                    break;
                case RankType.TapType:
                    titleid = R.string.rank_tap_tds;
                    break;
                case RankType.WaterType:
                    titleid = R.string.rank_water_purifier_tds;
                    break;
                case RankType.CupVolumType:
                    titleid = rank_cup_vol;
                    break;
                default:
                    titleid = rank_cup_tds;
                    break;
            }
            return titleid;
        }

        /**
         * 获取标题图标id
         *
         * @param type
         *
         * @return
         */
        private int getTitleIconResId(String type) {
            int resId = R.mipmap.icon_cup_on;
            switch (type) {
                case RankType.CupType:
                    resId = R.mipmap.icon_cup_on;
                    break;
                case RankType.TapType:
                    resId = R.mipmap.icon_tap_on;
                    break;
                case RankType.WaterType:
                    resId = R.mipmap.icon_water_purifier_on;
                    break;
                case RankType.CupVolumType:
                    resId = R.mipmap.icon_cup_on;
                    break;
                default:
                    resId = R.mipmap.icon_cup_on;
                    break;
            }
            return resId;
        }

        class ItemClick implements View.OnClickListener {
            private String rankType;

            public ItemClick(String rankType) {
                this.rankType = rankType;
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.llay_bottom:
                        Intent rankIntent = new Intent(mContext, RankActivity.class);
                        if (rankIntent.resolveActivity(mContext.getPackageManager()) != null) {
                            rankIntent.putExtra(Contacts.PARMS_RANK_TYPE, rankType);
                            startActivity(rankIntent);
                        }
                        break;
                    case R.id.llay_likeNum:
                        Intent likeMeintent = new Intent(mContext, LikeMeActivity.class);
                        if (likeMeintent.resolveActivity(mContext.getPackageManager()) != null) {
                            likeMeintent.putExtra(Contacts.PARMS_RANK_TYPE, rankType);
                            startActivity(likeMeintent);
                        }
                        break;
                }
            }
        }

    }
}
