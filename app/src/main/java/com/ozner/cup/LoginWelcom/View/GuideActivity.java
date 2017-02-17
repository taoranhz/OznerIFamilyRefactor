package com.ozner.cup.LoginWelcom.View;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.R;
import com.ozner.cup.Utils.OznerFileImageHelper;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class GuideActivity extends BaseActivity {
    private static final String TAG = "GuideActivity";
    @InjectView(R.id.cb_guide)
    ConvenientBanner cbGuide;

    private ArrayList<Integer> imgList = new ArrayList<>();
    private boolean isFinish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        ButterKnife.inject(this);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.guide_color));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.guide_color));
        }
        OznerPreference.SetValue(this, OznerPreference.IsFirstStart, "false");
        initBanner();
    }

    private void initBanner() {
        imgList.clear();
        imgList.add(R.drawable.guide1);
        imgList.add(R.drawable.guide2);
        imgList.add(R.drawable.guide3);
        cbGuide.setPageIndicator(new int[]{R.drawable.banner_bottom_dot_unselected, R.drawable.banner_bottom_dot_selected});
        //加载本地图片
        cbGuide.setPages(new CBViewHolderCreator<LocalImageHolderView>() {
            @Override
            public LocalImageHolderView createHolder() {
                return new LocalImageHolderView();
            }
        }, imgList);
        cbGuide.setCanLoop(false);
        cbGuide.setcurrentitem(0);
        cbGuide.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (isFinish) {
                    startActivity(new Intent(GuideActivity.this, LoginActivity.class));
                    GuideActivity.this.finish();
                }
            }
        });
        cbGuide.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.e(TAG, "onPageSelected: " + position);
                if (position == 2)
                    isFinish = true;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public class LocalImageHolderView implements Holder<Integer> {
        private ImageView imageView;

        @Override
        public View createView(Context context) {
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            return imageView;
        }

        @Override
        public void UpdateUI(Context context, final int position, Integer data) {
            if (imageView.getDrawingCache() != null && !imageView.getDrawingCache().isRecycled()) {
                imageView.getDrawingCache().recycle();
                System.gc();
            }
            imageView.setImageBitmap(OznerFileImageHelper.readBitMap(context, data));
        }
    }

}
