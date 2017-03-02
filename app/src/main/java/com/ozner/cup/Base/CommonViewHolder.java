package com.ozner.cup.Base;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;

/**
 * Created by ozner_67 on 2016/11/3.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class CommonViewHolder {
    private SparseArray<View> mViews;
    private int mPosition;
    private View mConvertView;
    private WeakReference<Context> mWeakContext;

    private CommonViewHolder(Context context, ViewGroup parent, int layoutId, int position) {
        mWeakContext = new WeakReference<Context>(context);
        this.mPosition = position;
        this.mViews = new SparseArray<>();
//        this.mConvertView = parent;
        this.mConvertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        mConvertView.setTag(this);
    }

    /**
     * 获取ViewHolder对象
     *
     * @param context
     * @param convertView
     * @param parent
     * @param layoutId
     * @param position
     *
     * @return
     */
    public static CommonViewHolder get(Context context, View convertView,
                                       ViewGroup parent, int layoutId, int position) {
        if (convertView == null) {
            return new CommonViewHolder(context, parent, layoutId, position);
        }
        return (CommonViewHolder) convertView.getTag();
    }

    public View getConvertView() {
        return mConvertView;
    }

    /**
     * 通过控件的Id获取对应的控件，如果没有则加入views
     *
     * @param viewId
     *
     * @return
     */
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }


    /**
     * 为TextView设置字符串
     *
     * @param viewId
     * @param text
     *
     * @return
     */
    public CommonViewHolder setText(int viewId, String text) {
        TextView view = getView(viewId);
        view.setText(text);
        return this;
    }

    /**
     * 为TextView设置字符串
     *
     * @param viewId
     * @param textResId
     *
     * @return
     */
    public CommonViewHolder setText(int viewId, int textResId) {
        TextView view = getView(viewId);
        view.setText(mWeakContext.get().getString(textResId));
        return this;
    }

    /**
     * 设置背景色
     *
     * @param viewId
     * @param color
     *
     * @return
     */
    public CommonViewHolder setTextColor(int viewId, int color) {
        TextView view = getView(viewId);
        view.setTextColor(color);
        return this;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @param imageResId
     *
     * @return
     */
    public CommonViewHolder setImageResource(int viewId, int imageResId) {
//        ImageView view = getView(viewId);
//        view.setImageResource(drawableId);
        Glide.with(mWeakContext.get()).load(imageResId).asBitmap().into((ImageView) getView(viewId));
        return this;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @param bm
     *
     * @return
     */
    public CommonViewHolder setImageBitmap(int viewId, Bitmap bm) {
        ImageView view = getView(viewId);
        view.setImageBitmap(bm);
        return this;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @param url
     *
     * @return
     */
    public CommonViewHolder setImageByUrl(int viewId, String url) {
        Glide.with(mWeakContext.get()).load(url).asBitmap().into((ImageView) getView(viewId));
        return this;
    }

    public int getPosition() {
        return mPosition;
    }

}
