package com.ozner.cup.Device.ROWaterPurifier;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import java.util.Calendar;
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
    private HashMap<String, Object> hashMap, hashMapGet;
    private ImageView[] btnArr;
    private RechargeDatas rechargeDatas;
    private HttpUtils httpUtils;
    private List<RechargeDatas> lisDatas;
    private int actualQuantity, buyQuantity;
    private String mac;
    RechargeAdapter adapter;
    private WaterPurifier_RO_BLE mWaterPurifier;
    private String userid;
    private UserInfo mUserInfo;
    private List<List<RechargeDatas>> listCard;
    private NetworkInfo networkInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rowater_recharge);
        ButterKnife.inject(this);
        tv_recharge_btn.setEnabled(false);
        Intent intent = getIntent();
        mac = intent.getStringExtra("MAC");
        if (mac != null) {
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
        rechargeDatas = new RechargeDatas();
        lisDatas = new ArrayList<RechargeDatas>();
        httpUtils = new HttpUtils();
        httpUtils.configHttpCacheSize(0);
        //获取网络连接管理者
        Context context=(Context)this;
        ConnectivityManager connectionManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取网络的状态信息，有下面三种方式
        if(connectionManager!=null){
            networkInfo = connectionManager.getActiveNetworkInfo();
            if(networkInfo!=null){
                if(networkInfo.isConnected()){
                    setData();//刚进来的时候
                    adapter = new RechargeAdapter(this);
                    lv_cards.setAdapter(adapter);
                }else{
                    new AlertDialog.Builder(RoWaterRechargeActivity.this).setTitle(R.string.tips).setMessage(R.string.no_onlie)
                            .setPositiveButton(R.string.ensure, new AlertDialog.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
//                                                            RoWaterRechargeActivity.this.finish();
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
            else {
                Log.e("trNet","networkInfo-----------null");
                new AlertDialog.Builder(RoWaterRechargeActivity.this).setTitle(R.string.tips).setMessage(R.string.no_onlie)
                        .setPositiveButton(R.string.ensure, new AlertDialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
//                                                            RoWaterRechargeActivity.this.finish();
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
        }else {
            Log.e("trNet","connectionManager-----------null");
        }






    }

    private void setData() {

        //网络获取水卡数据
        if (!lisDatas.isEmpty()) {
            lisDatas.clear();
            adapter.notifyDataSetChanged();
        }
        final ProgressDialog progressDialog = new ProgressDialog(RoWaterRechargeActivity.this);
        progressDialog.setTitle("数据加载中......");
        progressDialog.show();
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("mobile", mUserInfo.getMobile());
//        params.addQueryStringParameter("mobile", "13764839962");
        httpUtils.send(HttpRequest.HttpMethod.GET, Contacts.roCards, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.e("myactivity", "onsetData" + responseInfo.result);
                try {
                    JSONObject jsonobject = new JSONObject(responseInfo.result);
                    String result = jsonobject.getString("Result");
                    if (result.equals("1")) {
                        progressDialog.cancel();
                        progressDialog.dismiss();
                        JSONArray jsonArray = jsonobject.getJSONArray("Data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject json = jsonArray.getJSONObject(i);
                            int buyQuantity = json.getInt("BuyQuantity");
                            int actualNum = json.getInt("ActualQuantity");
                            if ((buyQuantity - actualNum) > 0) {
                                //未使用的水卡  购买数量-已使用数量
                                for (int k = 0; k < (buyQuantity - actualNum); k++) {
                                    RechargeDatas item = new RechargeDatas();
                                    item.setOrderId(json.getInt("OrderId") + "");
                                    item.setProductName(json.getString("ProductName"));
                                    item.setOrderDtlId(json.getInt("OrderDtlId") + "");
                                    item.setProductId(json.getInt("ProductId") + "");
                                    item.setLimitTimes(json.getInt("LimitTimes"));
                                    item.setOrginOrderCode(json.getString("OrginOrderCode"));
                                    item.setuCode(json.getString("UCode"));
                                    item.setIsRecord(0);
                                    item.setActualQuantity(0);
                                    item.setBuyQuantity(1);
//                                    if ((buyQuantity - actualNum)>0) {
                                    lisDatas.add(item);
//                                    }
                                }
                            }
                        }
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject json = jsonArray.getJSONObject(i);
                            int buyQuantity = json.getInt("BuyQuantity");
                            int actualNum = json.getInt("ActualQuantity");
                            //已使用
                            for (int j = 0; j < actualNum; j++) {
                                RechargeDatas item = new RechargeDatas();
                                item.setOrderId(json.getInt("OrderId") + "");
                                item.setProductName(json.getString("ProductName"));
                                item.setOrderDtlId(json.getInt("OrderDtlId") + "");
                                item.setProductId(json.getInt("ProductId") + "");
                                item.setLimitTimes(json.getInt("LimitTimes"));
                                item.setOrginOrderCode(json.getString("OrginOrderCode"));
                                item.setuCode(json.getString("UCode"));
                                item.setIsRecord(1);
                                item.setActualQuantity(1);
                                item.setBuyQuantity(1);
//                                if (json.getInt("ActualQuantity") != 0) {
                                lisDatas.add(item);
//                                }
                            }
                        }
                        adapter.loadData(lisDatas);

                    } else {

                    }
                } catch (JSONException e) {
                    Log.e("tr", "qingqiu_Ex:" + e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                progressDialog.cancel();
                progressDialog.dismiss();
                lisDatas.clear();
                Toast.makeText(RoWaterRechargeActivity.this,"数据加载失败!请检查网络！",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class RechargeAdapter extends BaseAdapter {
        private List<RechargeDatas> datasList;
        private LayoutInflater inflater;
        private Context mContext;
        private boolean[] btnSel;

        public RechargeAdapter(Context context) {
            datasList = new ArrayList<>();
            inflater = LayoutInflater.from(context);
            this.mContext = context;
        }

        public void loadData(List<RechargeDatas> datasList) {
            this.datasList = datasList;
            this.notifyDataSetChanged();
            btnSel = new boolean[datasList.size()];
//            setImgData();
        }

        private void setImgData() {
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHold viewHold;
            if (convertView == null) {
                viewHold = new ViewHold();
                convertView = inflater.inflate(R.layout.activity_recharge_item, null, false);
                viewHold.iv_cards = (ImageView) convertView
                        .findViewById(R.id.iv_cards);
                viewHold.card_choose = (ImageView) convertView
                        .findViewById(R.id.card_choose);
                viewHold.iv_have_use = (ImageView) convertView
                        .findViewById(R.id.iv_have_use);
                convertView.setTag(viewHold);
            } else {
                viewHold = (ViewHold) convertView.getTag();
            }
//            btnArr[position] = viewHold.card_choose;
//            btnArr[position].setTag(position);
            RechargeDatas item = datasList.get(position);
            switch (item.getLimitTimes()) {
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
//            Log.e("trItem", item.getIsRecord() + "=======" + item.getProductName());
            if (item.getIsRecord() == 1) {
                viewHold.iv_have_use.setVisibility(View.VISIBLE);
                viewHold.card_choose.setVisibility(View.INVISIBLE);
            } else {
                viewHold.iv_have_use.setVisibility(View.INVISIBLE);
                viewHold.card_choose.setVisibility(View.VISIBLE);
            }

            if (btnSel[position]) {
                viewHold.card_choose.setBackgroundResource(R.drawable.group);
                rechargeDatas.setLimitTimes(datasList.get(position).getLimitTimes());
                rechargeDatas.setOrderId(datasList.get(position).getOrderId());
                rechargeDatas.setOrderDtlId(datasList.get(position).getOrderDtlId());
                rechargeDatas.setProductId(datasList.get(position).getProductId());
                rechargeDatas.setuCode(datasList.get(position).getuCode());
                rechargeDatas.setMac(mac);
                rechargeDatas.setOrginOrderCode(datasList.get(position).getOrginOrderCode());
                tv_recharge_btn.setEnabled(true);
            } else {
                viewHold.card_choose.setBackgroundResource(R.drawable.rectangle);
            }
            viewHold.card_choose.setOnClickListener(new MyClickListener(position));
            return convertView;
        }

        class ViewHold {
            private ImageView iv_cards;
            private ImageView iv_have_use;
            private ImageView card_choose;
        }

        class MyClickListener implements View.OnClickListener {
            private int pos;
            public MyClickListener(int position) {
                this.pos = position;
            }

            @Override
            public void onClick(View v) {
                Log.e("trposition", "position===" + pos);
                for (int i = 0; i < btnSel.length; i++) {
                    if (pos != i) {
                        btnSel[i] = false;
                    }
                }
                btnSel[pos] = !btnSel[pos];
                adapter.notifyDataSetChanged();
            }
        }
    }

    @OnClick({R.id.toolbar_buy, R.id.tv_recharge_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbar_buy:
                //TODO 弹出选择框
                if (mUserInfo != null && mUserInfo.getMobile() != null && !mUserInfo.getMobile().isEmpty()) {
                    String waterUrl = WeChatUrlUtil.formatRoCardsUrl(mUserInfo.getMobile(), OznerPreference.getUserToken(RoWaterRechargeActivity.this), "zh", "zh");
                    Log.e("url",waterUrl);
                    startWebActivity(waterUrl);

                } else {
                    showToastCenter(R.string.userinfo_miss);
                }
                break;
            case R.id.tv_recharge_btn:
                if(networkInfo.isConnected()){
                    getCards();
                }else{
                    new AlertDialog.Builder(RoWaterRechargeActivity.this).setTitle(R.string.tips).setMessage(R.string.no_onlie)
                            .setPositiveButton(R.string.ensure, new AlertDialog.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
//                                                            RoWaterRechargeActivity.this.finish();
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

                break;
        }
    }

    private void getCards() {
        String time = "";
        switch (rechargeDatas.getLimitTimes()) {
            case 1:
                time = "试用";
                break;
            case 6:
                time = "半年";
                break;
            case 12:
                time = "一年";
                break;
            default:
                time = "半年";
                rechargeDatas.setLimitTimes(6);
                break;
        }

        if (mWaterPurifier != null && mWaterPurifier.connectStatus() == BaseDeviceIO.ConnectStatus.Connected) {
            new AlertDialog.Builder(this).setTitle(getString(R.string.tips)).setMessage(getString(R.string.tips_content1) + mac + getString(R.string.tips_content2) + time + getString(R.string.tips_content3))
                    .setPositiveButton(getString(R.string.ensure), new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            //TODO 写入设备数据
                            //判断是否有网络
                                if(networkInfo.isConnected()){
                                    if (mWaterPurifier != null&& mWaterPurifier.connectStatus() == BaseDeviceIO.ConnectStatus.Connected) {
                                        Log.e("trType", "水卡写入数据1=====" + rechargeDatas.getLimitTimes());
                                        //第一次去写入设备
                                        final Calendar calTime = Calendar.getInstance();
                                        calTime.setTime(mWaterPurifier.settingInfo.ExpireTime);
                                        calTime.add(Calendar.MONTH, rechargeDatas.getLimitTimes());
                                        final ProgressDialog progressDialog = new ProgressDialog(RoWaterRechargeActivity.this);
                                        progressDialog.setTitle("数据上传中。。。");
                                        progressDialog.show();

                                    mWaterPurifier.addMonth(rechargeDatas.getLimitTimes(), new OperateCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void var1) {
                                            Log.e("trType", "水卡写入数据成功");
                                            RequestParams params = new RequestParams();
                                            params.addBodyParameter("OrderId", rechargeDatas.getOrderId());
                                            params.addBodyParameter("OrderDtlId", rechargeDatas.getOrderDtlId());
                                            params.addBodyParameter("ProductId", rechargeDatas.getProductId());
                                            params.addBodyParameter("UCode", rechargeDatas.getuCode());
                                            params.addBodyParameter("Mac", rechargeDatas.getMac());
                                            params.addBodyParameter("OrginOrderCode", rechargeDatas.getOrginOrderCode());
                                            httpUtils.send(HttpRequest.HttpMethod.POST, Contacts.roCardsPost, params, new RequestCallBack<String>() {
                                                @Override
                                                public void onSuccess(ResponseInfo<String> responseInfo) {
                                                    Log.e("trType", "上传数据返回值==" + responseInfo.result);
                                                    try {
                                                        JSONObject jsonobject = new JSONObject(responseInfo.result);
                                                        final String result = jsonobject.getString("Result");
                                                        final String msg = jsonobject.getString("Message");
                                                        if (result.equals("1")) {
                                                            //水卡信息上传服务器成功刷新数据
                                                            progressDialog.cancel();
                                                            progressDialog.dismiss();
                                                            tv_recharge_btn.setEnabled(false);
                                                            Toast.makeText(RoWaterRechargeActivity.this, "水卡写入数据成功", Toast.LENGTH_SHORT).show();
                                                            refreshView();
                                                        } else {
//                                                        tv_recharge_btn.setEnabled(true);
                                                            //数据上传没成功
                                                            //刷新listview界面
                                                            refreshView();
                                                            progressDialog.cancel();
                                                            progressDialog.dismiss();
//                                                        Toast.makeText(RoWaterRechargeActivity.this, "数据上传失败", Toast.LENGTH_SHORT).show();
                                                            Log.e("trType", "水卡上传数据失败" + rechargeDatas.getLimitTimes() + " , " + mWaterPurifier.settingInfo.ExpireTime.toLocaleString());
                                                            new Handler().postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Log.e("tr", "开始失败回复");
                                                                    int count = 10;
                                                                    while (count > 0 && ((calTime.get(Calendar.YEAR) - 1900) != mWaterPurifier.settingInfo.ExpireTime.getYear())
                                                                            || (calTime.get(Calendar.MONTH) + 0 != mWaterPurifier.settingInfo.ExpireTime.getMonth())) {
                                                                        //数据不同
                                                                        count--;
                                                                        mWaterPurifier.requestSettingInfo();
                                                                        try {
                                                                            Thread.sleep(500);
                                                                        } catch (InterruptedException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                    mWaterPurifier.addMonth(((-1) * rechargeDatas.getLimitTimes()), new OperateCallback<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void var1) {
                                                                            Log.e("trType", "水卡写入数据成功2");
                                                                            progressDialog.cancel();
                                                                            progressDialog.dismiss();

                                                                            int count = 10;
                                                                            while (count > 0 && ((calTime.get(Calendar.YEAR) - 1900) == mWaterPurifier.settingInfo.ExpireTime.getYear())
                                                                                    && (calTime.get(Calendar.MONTH) + 0 == mWaterPurifier.settingInfo.ExpireTime.getMonth())) {
                                                                                //数据不同
                                                                                count--;
                                                                                mWaterPurifier.requestSettingInfo();
                                                                                try {
                                                                                    Thread.sleep(500);
                                                                                } catch (InterruptedException e) {
                                                                                    e.printStackTrace();
                                                                                }
                                                                            }
//                                                                        refreshView();
                                                                        }

                                                                        @Override
                                                                        public void onFailure(Throwable var1) {
                                                                            progressDialog.cancel();
                                                                            progressDialog.dismiss();


//                                                                        refreshView();
                                                                        }
                                                                    });
                                                                    HttpDatas.getStatus(RoWaterRechargeActivity.this, result, msg);
                                                                }
                                                            }, 500);
                                                        }
                                                    } catch (Exception e) {
                                                        e.getStackTrace();
                                                    }
                                                }
                                                @Override
                                                public void onFailure(HttpException error, String msg) {
                                                    progressDialog.cancel();
                                                    progressDialog.dismiss();
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Log.e("tr", "开始失败回复");
                                                            int count = 10;
                                                            while (count > 0 && ((calTime.get(Calendar.YEAR) - 1900) != mWaterPurifier.settingInfo.ExpireTime.getYear())
                                                                    || (calTime.get(Calendar.MONTH) + 0 != mWaterPurifier.settingInfo.ExpireTime.getMonth())) {
                                                                //数据不同
                                                                count--;
                                                                mWaterPurifier.requestSettingInfo();
                                                                try {
                                                                    Thread.sleep(500);
                                                                } catch (InterruptedException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                            mWaterPurifier.addMonth(((-1) * rechargeDatas.getLimitTimes()), new OperateCallback<Void>() {
                                                                @Override
                                                                public void onSuccess(Void var1) {
                                                                    Log.e("trType", "水卡写入数据成功2");
                                                                    progressDialog.cancel();
                                                                    progressDialog.dismiss();

                                                                    int count = 10;
                                                                    while (count > 0 && ((calTime.get(Calendar.YEAR) - 1900) == mWaterPurifier.settingInfo.ExpireTime.getYear())
                                                                            && (calTime.get(Calendar.MONTH) + 0 == mWaterPurifier.settingInfo.ExpireTime.getMonth())) {
                                                                        //数据不同
                                                                        count--;
                                                                        mWaterPurifier.requestSettingInfo();
                                                                        try {
                                                                            Thread.sleep(500);
                                                                        } catch (InterruptedException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
//                                                                        refreshView();
                                                                }

                                                                @Override
                                                                public void onFailure(Throwable var1) {
                                                                    progressDialog.cancel();
                                                                    progressDialog.dismiss();


//                                                                        refreshView();
                                                                }
                                                            });
                                                        }
                                                    }, 500);
                                                    refreshView();
                                                    Toast.makeText(RoWaterRechargeActivity.this, "数据上传失败", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFailure(Throwable var1) {
                                            progressDialog.cancel();
                                            progressDialog.dismiss();
                                            refreshView();
                                            Log.e("trType", "水卡写入数据失败");
//                                        tv_recharge_btn.setEnabled(true);
                                            Toast.makeText(RoWaterRechargeActivity.this, "水卡写入水机失败", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }else{
                                        refreshView();

                                        new AlertDialog.Builder(RoWaterRechargeActivity.this).setTitle(R.string.tips).setMessage(R.string.device_disConnect)
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
                                }else{
                                    new AlertDialog.Builder(RoWaterRechargeActivity.this).setTitle(R.string.tips).setMessage(R.string.no_onlie)
                                            .setPositiveButton(R.string.ensure, new AlertDialog.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
//                                                            RoWaterRechargeActivity.this.finish();
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
                    })
                    .setNegativeButton(getString(R.string.cancle), new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            dialog.dismiss();
                        }
                    }).show();
        } else {
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

    //用于listview的刷新
    private void refreshView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!lisDatas.isEmpty()) {
                    lisDatas.clear();
                    adapter.notifyDataSetChanged();
                    setData();
                }
            }
        });

    }

    private void startWebActivity(String url) {
        Intent filterIntent = new Intent(RoWaterRechargeActivity.this, WebActivity.class);
        filterIntent.putExtra(Contacts.PARMS_URL, url);
        startActivity(filterIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("activity", "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("activity", "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("activity", "onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!lisDatas.isEmpty()) {
            lisDatas.clear();
            adapter.notifyDataSetChanged();
        }

    }
}
