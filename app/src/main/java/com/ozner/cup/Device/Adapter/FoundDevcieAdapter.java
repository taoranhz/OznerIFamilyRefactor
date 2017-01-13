package com.ozner.cup.Device.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.ozner.cup.R;
import com.ozner.device.BaseDeviceIO;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ozner_67 on 2016/11/8.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class FoundDevcieAdapter extends RecyclerView.Adapter<FoundDeviceHolder> {
    private static final String TAG = "FoundDevcieAdapter";
    private WeakReference<Context> mContext;
    private List<BaseDeviceIO> deviceIoList;
    private int selPos = -1;
    private int itemWidth = -1;
    private ClientClickListener mClickListenr;
    private int drawableSelId, drawableUnSelId;

    public interface ClientClickListener {
        void onItemClick(int position, BaseDeviceIO deviceIO);
    }

    /**
     * 设置默认选中
     *
     * @param pos
     */
    public void setDefaultClick(int pos) {
        if (pos >= 0 && pos < getItemCount()) {
            selPos = pos;
            this.notifyDataSetChanged();
        }
    }

    public FoundDevcieAdapter(Context context, int draSelId, int draUnSelId) {
        this.mContext = new WeakReference<Context>(context);
        deviceIoList = new ArrayList<>();
        this.drawableSelId = draSelId;
        this.drawableUnSelId = draUnSelId;
    }

    public void setItemWidth(int width) {
        itemWidth = width;
        this.notifyDataSetChanged();
    }

    public void clear() {
        deviceIoList.clear();
        selPos = -1;
        this.notifyDataSetChanged();
    }

    public boolean hasDevice(BaseDeviceIO deviceIo) {
        return deviceIoList.contains(deviceIo);
    }

    public void addItem(BaseDeviceIO deviceIO) {
        deviceIoList.add(deviceIO);
        this.notifyDataSetChanged();
    }

    public void setOnItemClickListener(ClientClickListener listener) {
        this.mClickListenr = listener;
    }

    @Override
    public FoundDeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext.get()).inflate(R.layout.found_device_item, null);
        return new FoundDeviceHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FoundDeviceHolder holder, final int position) {
        BaseDeviceIO deviceIO = deviceIoList.get(position);

        holder.iv_deviceIcon.setOnClickListener(new MyClickListener(this, position));
        if (itemWidth > 0) {
            holder.llay_root.setLayoutParams(new LinearLayout.LayoutParams(itemWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        Log.e(TAG, "onBindViewHolder: selPos:" + selPos + " ,curPos:" + position);

        if (selPos == position) {
            holder.rb_check.setChecked(true);
//            Glide.with(mContext.get()).load(R.drawable.found_tap_selected).asBitmap().into(holder.iv_deviceIcon);
            Glide.with(mContext.get()).load(drawableSelId).asBitmap().into(holder.iv_deviceIcon);
        } else {
            holder.rb_check.setChecked(false);
            Glide.with(mContext.get()).load(drawableUnSelId).asBitmap().into(holder.iv_deviceIcon);
        }


        if (deviceIO != null) {
            holder.tv_deviceName.setText(deviceIO.name);
        }
    }

    @Override
    public int getItemCount() {
        return deviceIoList.size();
    }

    class MyClickListener implements View.OnClickListener {
        private int position = -1;
        private FoundDevcieAdapter tempAdaper;

        public MyClickListener(FoundDevcieAdapter adapter, int pos) {
            this.position = pos;
            tempAdaper = adapter;
        }

        @Override
        public void onClick(View v) {
            selPos = position;
            Log.e(TAG, "onClick: selPos:" + selPos);
            tempAdaper.notifyDataSetChanged();
            if (mClickListenr != null) {
                mClickListenr.onItemClick(position, deviceIoList.get(position));
            }
        }
    }
}
