package com.example.photosortingsystem.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.photosortingsystem.R;
import com.example.photosortingsystem.entity.PhotoItem;

import java.util.List;

/**
 * 照片栏目适配器
 */
public class PhotoAdapter extends ArrayAdapter<PhotoItem> {
    private int resourceId;

    List<PhotoItem> mImageList;
    private Context context;

    public PhotoAdapter(Context context, int textViewResourceId,
                        List<PhotoItem> objects) {
        super(context, textViewResourceId, objects);

        this.context = context;
        mImageList = objects;
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final PhotoItem photo = getItem(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(resourceId, null);
            holder.img = (ImageView) convertView.findViewById(R.id.photo_small);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }
        String url = photo.getImageId();
        Glide
                .with(context)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .crossFade()
                .thumbnail(0.1f).into(holder.img);
        return convertView;
    }
    private static class ViewHolder
    {
        public ImageView img;
    }


}

