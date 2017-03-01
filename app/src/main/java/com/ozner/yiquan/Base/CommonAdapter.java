package com.ozner.yiquan.Base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ozner_67 on 2016/11/3.
 * 邮箱：xinde.zhang@cftcn.com
 */

public abstract class CommonAdapter<T> extends BaseAdapter {
    protected LayoutInflater mInflater;
    protected Context mContext;
    protected List<T> mDatas;
    protected final int mItemLayoutId;

    public CommonAdapter(Context context, int itemLayoutId) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mDatas = new ArrayList<>();
        this.mItemLayoutId = itemLayoutId;
    }

    public void loadData(List<T> datas) {
        this.mDatas.clear();
        this.mDatas = new ArrayList<>(datas);
        this.notifyDataSetChanged();
    }

    /**
     * 移除指定数据
     * @param index
     */
    public void removeData(int index) {
        if (index < mDatas.size()) {
            mDatas.remove(index);
            this.notifyDataSetChanged();
        }

    }

    public void addData(T data) {
        this.mDatas.add(data);
        this.notifyDataSetChanged();
    }

    public void clear() {
        this.mDatas.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public T getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final CommonViewHolder viewHolder = getViewHolder(position, convertView,
                parent);
        convert(viewHolder, getItem(position), position);
        return viewHolder.getConvertView();

    }

    public abstract void convert(CommonViewHolder holder, T item, int position);

    private CommonViewHolder getViewHolder(int position, View convertView,
                                           ViewGroup parent) {
        return CommonViewHolder.get(mContext, convertView, parent, mItemLayoutId,
                position);
    }

}
