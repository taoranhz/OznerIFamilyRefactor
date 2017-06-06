package com.ozner.cup.Device.ROWaterPurifier.view;

import android.content.Context;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.ozner.cup.R;

/**
 * Created by taoran on 2017/6/2.
 * 实现item的单选
 */

public class ChoiceView extends FrameLayout implements Checkable {
    private ImageView iv_cards,iv_have_use;
    private RadioButton card_choose;
    private int useCount;//已使用的卡数
    private int noUseCount;//未使用的卡数
    public ChoiceView(Context context) {
        super(context);
        View.inflate(context, R.layout.activity_recharge_item, this);
        iv_cards = (ImageView) findViewById(R.id.iv_cards);
        card_choose = (RadioButton) findViewById(R.id.card_choose);
        iv_have_use=(ImageView)findViewById(R.id.iv_have_use);
//        card_choose.setBackgroundResource(R.drawable.abc_dialog_material_background_light);
    }

    public void setImageView(int id){
        iv_cards.setImageResource(id);
    }

    public void setData(RechargeDatas data){
        useCount=data.getUseCount();
        noUseCount=data.getNoUseCount();
        if(data.getType().equals("hy")){

        }

    }


    public void setIsUse(boolean isUse){
//        if(isUse){
//            card_choose.setVisibility(INVISIBLE);
//            iv_have_use.setVisibility(VISIBLE);
//        }else {
//            card_choose.setVisibility(VISIBLE);
//            iv_have_use.setVisibility(INVISIBLE);
//        }
    }

//    public void setText(String text) {
//        mTextView.setText(text);
//    }

    @Override
    public void setChecked(boolean checked) {
        card_choose.setChecked(checked);
        if(checked){
            card_choose.setBackgroundResource(R.drawable.group);
        }else{
            card_choose.setBackgroundResource(R.drawable.rectangle);
        }
    }

    @Override
    public boolean isChecked() {
        return card_choose.isChecked();
    }

    @Override
    public void toggle() {
        card_choose.toggle();
    }

}
