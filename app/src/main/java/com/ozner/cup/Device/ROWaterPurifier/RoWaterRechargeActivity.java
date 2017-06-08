package com.ozner.cup.Device.ROWaterPurifier;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.ozner.WaterPurifier.WaterPurifier_RO_BLE;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.WebActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.UserInfo;
import com.ozner.cup.Device.ROWaterPurifier.view.HttpDatas;
import com.ozner.cup.Device.ROWaterPurifier.view.RechargeDatas;
import com.ozner.cup.R;
import com.ozner.cup.Utils.WeChatUrlUtil;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OperateCallback;
import com.ozner.device.OznerDeviceManager;

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
    @InjectView(R.id.tv_recharge_btn)
    TextView tv_recharge_btn;
//    @InjectView(R.id.tv_btn)
//    TextView tv_btn;

    private List<HashMap<String, Object>> list;
    private HashMap<String, Object> hashMap,hashMapGet;
    private ImageView[] btnArr;
    private RechargeDatas rechargeDatas;
    private HttpUtils httpUtils;
    private List<RechargeDatas> lisDatas;
    private int actualQuantity,buyQuantity;
    private String mac;
    RechargeAdapter adapter;
    private WaterPurifier_RO_BLE mWaterPurifier;
    private String userid;
    private UserInfo mUserInfo;
    private int cur_pos = -1;// 当前显示的一行,默认为0项选中


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rowater_recharge);
        ButterKnife.inject(this);
        tv_recharge_btn.setEnabled(false);
        Intent intent=getIntent();
        mac=intent.getStringExtra("MAC");
        if(mac!=null){
            mWaterPurifier = (WaterPurifier_RO_BLE) OznerDeviceManager.Instance().getDevice(mac);
        }
        userid = OznerPreference.GetValue(this, OznerPreference.UserId, "");
        mUserInfo = DBManager.getInstance(this).getUserInfo(userid);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoWaterRechargeActivity.this.finish();
            }
        });
        list = new ArrayList<HashMap<String, Object>>();
        rechargeDatas=new RechargeDatas();
        lisDatas=new ArrayList<RechargeDatas>();
        httpUtils=new HttpUtils();
        setData();
//        MyAdapter adapter = new MyAdapter(this);
        adapter=new RechargeAdapter(this);
        lv_cards.setAdapter(adapter);
//        lv_cards.setChoiceMode(ListView.CHOICE_MODE_SINGLE);// 一定要设置这个属性，否则ListView不会刷新
//        lv_cards.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1,
//                                    int position, long id) {
//                cur_pos = position;// 更新当前行
//            }
//        });

    }

    private void setData() {
        //网络获取水卡数据
        lisDatas.clear();
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("mobile",mUserInfo.getMobile());
        httpUtils.send(HttpRequest.HttpMethod.GET, Contacts.roCards, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.e("trlogin", responseInfo.result);
//                //{"status":1,"msg":null,"data":null}
                try {
                    JSONObject jsonobject = new JSONObject(responseInfo.result);
                    String result = jsonobject.getString("Result");
//                    List<RechargeDatas> list = JSON.parseArray(result,RechargeDatas.class);
                    if(result.equals("1")){
                    JSONArray jsonArray = jsonobject.getJSONArray("Data");
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject json=jsonArray.getJSONObject(i);
                        int buyQuantity = json.getInt("BuyQuantity");
                        int actualNum = json.getInt("ActualQuantity");
                        //未使用的水卡  购买数量-已使用数量
                        for (int k =0;k<(buyQuantity-actualNum);k++){
                            RechargeDatas item= new RechargeDatas();
                            item.setOrderId(json.getInt("OrderId")+"");
                            item.setProductName(json.getString("ProductName"));
                            item.setOrderDtlId(json.getInt("OrderDtlId")+"");
                            item.setProductId(json.getInt("ProductId")+"");
                            item.setLimitTimes(json.getInt("LimitTimes"));
                            item.setOrginOrderCode(json.getString("OrginOrderCode"));
                            item.setuCode(json.getString("UCode"));
                            item.setIsRecord(0);
                            item.setActualQuantity(0);
                            item.setBuyQuantity(1);
                            if (json.getInt("ActualQuantity") == 0){
                            lisDatas.add(item);}
                        }
                    }
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject json=jsonArray.getJSONObject(i);
                            int buyQuantity = json.getInt("BuyQuantity");
                            int actualNum = json.getInt("ActualQuantity");

                            //已使用
                            for(int j=0;j<actualNum;j++){
                                RechargeDatas item= new RechargeDatas();
                                item.setOrderId(json.getInt("OrderId")+"");
                                item.setProductName(json.getString("ProductName"));
                                item.setOrderDtlId(json.getInt("OrderDtlId")+"");
                                item.setProductId(json.getInt("ProductId")+"");
                                item.setLimitTimes(json.getInt("LimitTimes"));
                                item.setOrginOrderCode(json.getString("OrginOrderCode"));
                                item.setuCode(json.getString("UCode"));
                                item.setIsRecord(1);
                                item.setActualQuantity(1);
                                item.setBuyQuantity(1);
                                if (json.getInt("ActualQuantity") != 0) {
                                    lisDatas.add(item);
                                }
                            }
                        }
                    adapter.loadData(lisDatas);

                    }else{
                        Toast.makeText(RoWaterRechargeActivity.this,result,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e("tr","qingqiu_Ex:"+e.getMessage());
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(HttpException e, String s) {
                lisDatas.clear();
            }
        });
    }

    private class RechargeAdapter extends BaseAdapter{
        private List<RechargeDatas> datasList;
        private LayoutInflater inflater;
        private Context mContext;
        public RechargeAdapter(Context context){
            datasList = new ArrayList<>();
            inflater = LayoutInflater.from(context);
            this.mContext =context;
        }


        public void loadData(List<RechargeDatas> datasList){
            this.datasList = datasList;
            this.notifyDataSetChanged();
            setData();
        }

        private void setData() {
            btnArr = new ImageView[datasList.size()];
            for (int i = 0; i < datasList.size(); i++) {
                btnArr[i] = new ImageView(mContext);
                btnArr[i].setTag(i);
            }
        }
        @Override
        public int getCount() {
            return datasList.size();
        }

        @Override
        public Object getItem(int position) {
            return datasList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHold viewHold;
            if(convertView == null){
                viewHold = new ViewHold();
                convertView = inflater.inflate(R.layout.activity_recharge_item, null, false);
                viewHold.iv_cards = (ImageView) convertView
                        .findViewById(R.id.iv_cards);
                viewHold.card_choose = (ImageView) convertView
                        .findViewById(R.id.card_choose);
                viewHold.iv_have_use = (ImageView) convertView
                        .findViewById(R.id.iv_have_use);

                btnArr[position] = viewHold.card_choose;
                btnArr[position].setTag(position);
                convertView.setTag(viewHold);
            }else {
                viewHold = (ViewHold) convertView.getTag();
            }
            RechargeDatas item = datasList.get(position);
            switch (item.getLimitTimes()){
                case 1:
                    viewHold.iv_cards.setImageResource(R.drawable.trial_card);
                    break;
                case 6:
                    viewHold.iv_cards.setImageResource(R.drawable.half_year_card);
                    break;
                case 12:
                    viewHold.iv_cards.setImageResource(R.drawable.a_yearly_card);
                    break;
                default:
                    viewHold.iv_cards.setImageResource(R.drawable.half_year_card);
                    break;
            }
            Log.e("trItem",item.getIsRecord()+"======="+item.getProductName());
            if(item.getIsRecord() == 1){
                viewHold.iv_have_use.setVisibility(View.VISIBLE);
                viewHold.card_choose.setVisibility(View.INVISIBLE);
            }else {
                viewHold.iv_have_use.setVisibility(View.INVISIBLE);
                viewHold.card_choose.setVisibility(View.VISIBLE);
            }
//            if (position == cur_pos) {// 如果当前的行就是ListView中选中的一行，就更改显示样式
//                viewHold.card_choose.setBackgroundResource(R.drawable.group);
//                rechargeDatas.setLimitTimes(datasList.get(position).getLimitTimes());
//                            rechargeDatas.setOrderId(datasList.get(position).getOrderId());
//                            rechargeDatas.setOrderDtlId(datasList.get(position).getOrderDtlId());
//                            rechargeDatas.setProductId(datasList.get(position).getProductId());
//                            rechargeDatas.setuCode(datasList.get(position).getuCode());
//                            rechargeDatas.setMac(mac);
//                            rechargeDatas.setOrginOrderCode(datasList.get(position).getOrginOrderCode());
//                            tv_recharge_btn.setEnabled(true);
//                Log.e("trType","水卡111");
//            }else{
//                viewHold.card_choose.setBackgroundResource(R.drawable.rectangle);
//                Log.e("trType","水卡222");
//            }






            viewHold.card_choose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < datasList.size(); i++) {
                        if (v.getTag().equals(i)) {
                            btnArr[i].setBackgroundResource(R.drawable.group);
//                            Log.e("trType","TYPE========"+datasList.get(i).getLimitTimes());
                            rechargeDatas.setLimitTimes(datasList.get(i).getLimitTimes());
                            rechargeDatas.setOrderId(datasList.get(i).getOrderId());
                            rechargeDatas.setOrderDtlId(datasList.get(i).getOrderDtlId());
                            rechargeDatas.setProductId(datasList.get(i).getProductId());
//                            rechargeDatas.setuCode(userid);
                            rechargeDatas.setuCode(datasList.get(i).getuCode());
                            rechargeDatas.setMac(mac);
//                            rechargeDatas.setMac("123456");
                            rechargeDatas.setOrginOrderCode(datasList.get(i).getOrginOrderCode());
                            tv_recharge_btn.setEnabled(true);
                            Log.e("trType","水卡111");
                        } else {
                            btnArr[i].setBackgroundResource(R.drawable.rectangle);
//                            tv_recharge_btn.setEnabled(false);
                            Log.e("trType","水卡222");
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

    @OnClick({R.id.toolbar_buy, R.id.tv_recharge_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbar_buy:
               //TODO 弹出选择框
                if (mUserInfo != null && mUserInfo.getMobile() != null && !mUserInfo.getMobile().isEmpty()) {
                String waterUrl= WeChatUrlUtil.formatRoCardsUrl(mUserInfo.getMobile(),OznerPreference.getUserToken(RoWaterRechargeActivity.this), "zh", "zh");
                startWebActivity(waterUrl);
            } else {
                showToastCenter(R.string.userinfo_miss);
            }

                break;
            case R.id.tv_recharge_btn:
                getCards();
//                if (mWaterPurifier!=null){
//                    mWaterPurifier.addMonth(1, new OperateCallback<Void>() {
//                        @Override
//                        public void onSuccess(Void var1) {
//                            Log.e("trType","水卡写入数据成功1");
//                        }
//
//                        @Override
//                        public void onFailure(Throwable var1) {
//
//                        }
//                    });
//                }

//

                break;
//            case R.id.tv_btn:
//                if (mWaterPurifier!=null){
//                    mWaterPurifier.addMonth((-1)*1, new OperateCallback<Void>() {
//                        @Override
//                        public void onSuccess(Void var1) {
//                            Log.e("trType","水卡写入数据成功2");
//                        }
//
//                        @Override
//                        public void onFailure(Throwable var1) {
//
//                        }
//                    });
//                }
//                break;
        }
    }

    private void getCards() {
        String time="";
        switch (rechargeDatas.getLimitTimes()){
            case 1:
                time="试用";
                break;
            case 6:
                time="半年";
                break;
            case 12:
                time="一年";
                break;
            default:
                time="半年";
                rechargeDatas.setLimitTimes(6);
                break;
        }

        if(mWaterPurifier!= null && mWaterPurifier.connectStatus() == BaseDeviceIO.ConnectStatus.Connected){
            new AlertDialog.Builder(this).setTitle(getString(R.string.tips)).setMessage(getString(R.string.tips_content1)+mac+getString(R.string.tips_content2)+time+getString(R.string.tips_content3))
                    .setPositiveButton(getString(R.string.ensure), new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //TODO 写入设备数据
                            if(mWaterPurifier!=null){
                                Log.e("trType","水卡写入数据1====="+rechargeDatas.getLimitTimes());
                                //第一次去写入设备
                                mWaterPurifier.addMonth(rechargeDatas.getLimitTimes(), new OperateCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void var1) {
                                        Log.e("trType","水卡写入数据成功");
//                                            Toast.makeText(RoWaterRechargeActivity.this,"水卡写入数据成功",Toast.LENGTH_SHORT).show();
                                        RequestParams params = new RequestParams();
                                        params.addBodyParameter("OrderId",rechargeDatas.getOrderId());
                                        params.addBodyParameter("OrderDtlId", rechargeDatas.getOrderDtlId());
                                        params.addBodyParameter("ProductId",rechargeDatas.getProductId());
                                        params.addBodyParameter("UCode", rechargeDatas.getuCode());
                                        params.addBodyParameter("Mac",rechargeDatas.getMac());
                                        params.addBodyParameter("OrginOrderCode", rechargeDatas.getOrginOrderCode());
                                        httpUtils.send(HttpRequest.HttpMethod.POST, Contacts.roCardsPost, params, new RequestCallBack<String>() {
                                            @Override
                                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                                Log.e("trType","上传数据返回值=="+responseInfo.result);
                                                try {
                                                    JSONObject jsonobject = new JSONObject(responseInfo.result);
                                                   final String result = jsonobject.getString("Result");
                                                   final String msg=jsonobject.getString("Message");
                                                    if(result.equals("1")){
                                                        //水卡信息上传服务器成功刷新数据
                                                        Toast.makeText(RoWaterRechargeActivity.this,"水卡写入数据成功",Toast.LENGTH_SHORT).show();
                                                        new Handler().post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                setData();
                                                            }
                                                        });
                                                    }else{
                                                        //数据上传没成功
                                                        Log.e("trType","水卡上传数据失败"+rechargeDatas.getLimitTimes()+" , "+mWaterPurifier.settingInfo.ExpireTime.toLocaleString());

                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Log.e("tr","开始失败回复");
                                                                mWaterPurifier.addMonth(((-1)*rechargeDatas.getLimitTimes()), new OperateCallback<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void var1) {
                                                                        Log.e("trType","水卡写入数据成功2");
//                                                                new Handler().post(new Runnable() {
//                                                                    @Override
//                                                                    public void run() {
//                                                                        setData();
//                                                                    }
//                                                                });
                                                                    }
                                                                    @Override
                                                                    public void onFailure(Throwable var1) {

                                                                    }
                                                                });

                                                                HttpDatas.getStatus(RoWaterRechargeActivity.this,result,msg);
                                                            }
                                                        },1500);

                                                    }
                                                }catch (Exception e){
                                                    e.getStackTrace();
                                                }
                                            }
                                            @Override
                                            public void onFailure(HttpException error, String msg) {

                                            }
                                        });
                                    }
                                    @Override
                                    public void onFailure(Throwable var1) {
                                        Log.e("trType","水卡写入数据失败");
//                                            Toast.makeText(RoWaterRechargeActivity.this,"水卡写入数据失败",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    })
                    .setNegativeButton(getString(R.string.cancle), new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            dialog.dismiss();
                        }
                    }).show();
        }else{
            new AlertDialog.Builder(this).setTitle(R.string.tips).setMessage(R.string.device_disConnect)
                    .setPositiveButton(R.string.ensure, new AlertDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    dialog.dismiss();
                                }
                            }
                    ).setNegativeButton(getString(R.string.cancle), new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    dialog.dismiss();
                }
            }).show();



        }






    }

    private void startWebActivity(String url) {
        Intent filterIntent = new Intent(RoWaterRechargeActivity.this, WebActivity.class);
        filterIntent.putExtra(Contacts.PARMS_URL, url);
        startActivity(filterIntent);
    }

}
