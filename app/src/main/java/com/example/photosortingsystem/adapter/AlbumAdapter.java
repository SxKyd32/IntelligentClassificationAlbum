package com.example.photosortingsystem.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.photosortingsystem.R;
import com.example.photosortingsystem.entity.AlbumItem;
import com.example.photosortingsystem.view.GlideRoundTransform;

import java.util.List;


/**
 * 相册适配器
 */
public class AlbumAdapter extends ArrayAdapter<AlbumItem> {
    private int resourceId;
    private Context context;
    public AlbumAdapter(Context context, int textViewResourceId,
                        List<AlbumItem> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        this.context = context;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        final AlbumItem album = getItem(position);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(resourceId, null);
            holder.img = (ImageView) (ImageView) convertView.findViewById(R.id.album_image);
            holder.tv = (TextView) convertView.findViewById(R.id.album_name);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.tv.setText(album.getName());

        String url = album.getImageId();
        Glide
                .with(context)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .crossFade()
                .transform(new GlideRoundTransform(context))
                .thumbnail(0.1f).into(holder.img);
        return convertView;
    }
    private static class ViewHolder
    {
        public ImageView img;
        public TextView tv;
    }


}