package com.ozner.cup.Device.ROWaterPurifier;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Device.ROWaterPurifier.view.RechargeDatas;
import com.ozner.cup.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by taoran on 2017/6/1.
 * ro水机（水芯片）充值页面
 */

public class RoWaterRechargeActivity extends BaseActivity implements View.OnClickListener {
    @InjectView(R.id.toolbar_ro)
    Toolbar toolbar;
    @InjectView(R.id.toolbar_buy)
    TextView toolbar_buy;
    @InjectView(R.id.lv_cards)
    ListView lv_cards;

    private List<HashMap<String, Object>> list;
    private HashMap<String, Object> hashMap,hashMapGet;
    private ImageView[] btnArr;
    private RechargeDatas rechargeDatas;
    private HttpUtils httpUtils;
    private List<RechargeDatas> lisDatas;
    private int actualQuantity,buyQuantity;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rowater_recharge);
        ButterKnife.inject(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoWaterRechargeActivity.this.finish();
            }
        });
        list = new ArrayList<HashMap<String, Object>>();
        setData();
        rechargeDatas=new RechargeDatas();
        MyAdapter adapter = new MyAdapter(this);
        lv_cards.setAdapter(adapter);
        lisDatas=new ArrayList<RechargeDatas>();
    }

    private void setData() {
        //网络获取水卡数据
        httpUtils=new HttpUtils();
        RequestParams params = new RequestParams();
        params.addBodyParameter("uid","872743");
        httpUtils.send(HttpRequest.HttpMethod.GET, Contacts.roCards, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.e("trlogin", responseInfo.result);
                //{"status":1,"msg":null,"data":null}
                try {
                    JSONObject jsonobject = new JSONObject(responseInfo.result);
                    String result = jsonobject.getString("Result");
                    JSONArray jsonArray = jsonobject.getJSONArray("Data");
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject json=jsonArray.getJSONObject(i);
                        rechargeDatas.setOrderId(json.getInt("OrderId")+"");
                        rechargeDatas.setProductName(json.getString("ProductName"));
                        rechargeDatas.setOrderDtlId(json.getInt("OrderDtlId")+"");
                        rechargeDatas.setProductId(json.getInt("ProductId")+"");
                        rechargeDatas.setLimitTimes(json.getInt("LimitTimes"));
                        rechargeDatas.setActualQuantity(json.getInt("ActualQuantity"));
                        rechargeDatas.setBuyQuantity(json.getInt("BuyQuantity"));
                        rechargeDatas.setOrginOrderCode(json.getInt("OrginOrderCode"));
                        lisDatas.add(rechargeDatas);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
//                Log.e("tr", s);
            }
        });


        for(int i=0;i<lisDatas.size();i++){
            int limitTimes=lisDatas.get(i).getLimitTimes();
            switch (limitTimes){
                case 1:
                    buyQuantity=lisDatas.get(i).getBuyQuantity();
                        for(int j=0;j<buyQuantity;j++){
                            hashMap = new HashMap<String, Object>();
                            hashMap.put("type","Tyear");
                            hashMap.put("isUse","no");
                            hashMap.put("image", R.drawable.trial_card);
                            list.add(hashMap);
                        }

                    actualQuantity=lisDatas.get(i).getActualQuantity();
                    if(actualQuantity==0){
                    }else {
                        for (int j = 0; j < buyQuantity; j++) {
                            hashMap = new HashMap<String, Object>();
                            hashMap.put("type", "Tyear");
                            hashMap.put("isUse", "yes");
                            hashMap.put("image", R.drawable.trial_card);
                            list.add(hashMap);
                        }
                    }
                    break;
                case 3:
                    buyQuantity=lisDatas.get(i).getBuyQuantity();
                    for(int j=0;j<buyQuantity;j++){
                        hashMap = new HashMap<String, Object>();
                        hashMap.put("type","Hyear");
                        hashMap.put("isUse","no");
                        hashMap.put("image", R.drawable.trial_card);
                        list.add(hashMap);
                    }

                    actualQuantity=lisDatas.get(i).getActualQuantity();
                    if(actualQuantity==0){
                    }else {
                        for (int j = 0; j < buyQuantity; j++) {
                            hashMap = new HashMap<String, Object>();
                            hashMap.put("type", "Hyear");
                            hashMap.put("isUse", "yes");
                            hashMap.put("image", R.drawable.trial_card);
                            list.add(hashMap);
                        }
                    }
                    break;
                case 6:
                    buyQuantity=lisDatas.get(i).getBuyQuantity();
                    for(int j=0;j<buyQuantity;j++){
                        hashMap = new HashMap<String, Object>();
                        hashMap.put("type","Hyear");
                        hashMap.put("isUse","no");
                        hashMap.put("image", R.drawable.trial_card);
                        list.add(hashMap);
                    }

                    actualQuantity=lisDatas.get(i).getActualQuantity();
                    if(actualQuantity==0){
                    }else {
                        for (int j = 0; j < buyQuantity; j++) {
                            hashMap = new HashMap<String, Object>();
                            hashMap.put("type", "Hyear");
                            hashMap.put("isUse", "yes");
                            hashMap.put("image", R.drawable.trial_card);
                            list.add(hashMap);
                        }
                    }
                    break;
                case 12:
                    buyQuantity=lisDatas.get(i).getBuyQuantity();
                    for(int j=0;j<buyQuantity;j++){
                        hashMap = new HashMap<String, Object>();
                        hashMap.put("type","Ayear");
                        hashMap.put("isUse","no");
                        hashMap.put("image", R.drawable.trial_card);
                        list.add(hashMap);
                    }

                    actualQuantity=lisDatas.get(i).getActualQuantity();
                    if(actualQuantity==0){
                    }else {
                        for (int j = 0; j < buyQuantity; j++) {
                            hashMap = new HashMap<String, Object>();
                            hashMap.put("type", "Ayear");
                            hashMap.put("isUse", "yes");
                            hashMap.put("image", R.drawable.trial_card);
                            list.add(hashMap);
                        }
                    }
                    break;
            }
        }











//        for (int i = 0; i < 4; i++) {
//            hashMap = new HashMap<String, Object>();
//            hashMap.put("type","Ayear");
//            hashMap.put("isUse","no");
//            hashMap.put("image", R.drawable.a_yearly_card);
//            list.add(hashMap);
//        }

//        for(int i=0;i<3;i++){
//            hashMap = new HashMap<String, Object>();
//            hashMap.put("type","Hyear");
//            hashMap.put("isUse","no");
//            hashMap.put("image", R.drawable.half_year_card);
//            list.add(hashMap);
//        }

//        hashMap = new HashMap<String, Object>();
//        hashMap.put("type","Tyear");
//        hashMap.put("isUse","yes");
//        hashMap.put("image", R.drawable.trial_card);
//        list.add(hashMap);

    }



    private class MyAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private Context myContent = null;

        public MyAdapter(Context context) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.myContent = context;
            setData();
        }

        private void setData() {
            btnArr = new ImageView[list.size()];
            for (int i = 0; i < list.size(); i++) {
                btnArr[i] = new ImageView(myContent);
                btnArr[i].setTag(i);
            }
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.activity_recharge_item, null, false);
            hashMapGet = new HashMap<String, Object>();
            hashMapGet = list.get(position);
            ViewHold viewHold = new ViewHold();
            viewHold.iv_cards = (ImageView) convertView
                    .findViewById(R.id.iv_cards);
            viewHold.card_choose = (ImageView) convertView
                    .findViewById(R.id.card_choose);
            viewHold.iv_have_use = (ImageView) convertView
                    .findViewById(R.id.iv_have_use);
            viewHold.iv_cards
                    .setBackgroundResource((Integer) (hashMapGet.get("image")));

            String isUse= (String) hashMapGet.get("isUse");
            if(isUse.equals("no")){
                viewHold.iv_have_use.setVisibility(View.INVISIBLE);
            }else if(isUse.equals("yes")){
                viewHold.iv_have_use.setVisibility(View.VISIBLE);
                viewHold.card_choose.setVisibility(View.INVISIBLE);
            }
            btnArr[position] = viewHold.card_choose;
            btnArr[position].setTag(position);
            convertView.setTag(viewHold);
            viewHold.card_choose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    for (int i = 0; i < list.size(); i++) {
                        if (v.getTag().equals(i)) {
                            btnArr[i]
                                    .setBackgroundResource(R.drawable.group);
                            rechargeDatas.setProductName((String) list.get(i).get("type"));
                            Log.e("trType","TYPE========"+list.get(i).get("type"));
                        } else {
                            btnArr[i].setBackgroundResource(R.drawable.rectangle);
                        }
                    }
                }
            });
            return convertView;
        }

        class ViewHold {
            private ImageView iv_cards;
            private ImageView iv_have_use;
            private ImageView card_choose;
        }
    }

    @OnClick({R.id.toolbar_buy,R.id.tv_recharge_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbar_buy:
               //TODO 弹出选择框


                break;
            case R.id.tv_recharge_btn:
                new AlertDialog.Builder(this).setTitle(getString(R.string.tips)).setMessage(getString(R.string.tips_content1)+"12213234"+getString(R.string.tips_content2)+rechargeDatas.getProductName()+getString(R.string.tips_content3))
                        .setPositiveButton(getString(R.string.ensure), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO 写入设备数据

                            }
                        })
                        .setNegativeButton(getString(R.string.cancle), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                dialog.dismiss();
                            }
                        }).show();
                break;
        }
    }
}