package com.example.photosortingsystem.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.photosortingsystem.R;

import java.util.List;
import java.util.Map;


/**
 * 照片详情的适配器
 */
public class HorizontalScrollViewAdapter extends BaseAdapter
{
    private Context mContext;
    private LayoutInflater mInflater;
    private List<Map> mDatas;
    private String url;

    public HorizontalScrollViewAdapter(Context context, List<Map> mDatas)
    {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        this.mDatas = mDatas;
    }

    @Override
    public int getCount()
    {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = mInflater.inflate(
                    R.layout.gallery_item, parent, false);
        }
        ImageView myImageView = (ImageView) convertView
                .findViewById(R.id.id_index_gallery_item_image);
        try {
            url = (String) mDatas.get(position).get("_data");
            Glide
                    .with(mContext)
                    .load(url)
                    .centerCrop()
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.error)
                    .crossFade()
                    .thumbnail(0.1f)
                    .into(myImageView);
        } catch (Exception e) {

        }
        return convertView;
    }
}

