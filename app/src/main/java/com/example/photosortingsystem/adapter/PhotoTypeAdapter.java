package com.example.photosortingsystem.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.photosortingsystem.Config;
import com.example.photosortingsystem.R;

import org.tensorflow.demo.Classifier;

import java.util.List;


/**
 * 照片类别适配器
 */
public class PhotoTypeAdapter extends ArrayAdapter<Classifier.Recognition> {
    private int resourceId;
    public PhotoTypeAdapter(Context context, int textViewResourceId,
                            List<Classifier.Recognition> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Classifier.Recognition type= getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        ImageView memoryImage = (ImageView) view.findViewById(R.id.type_image);
        int i;
        for (i = 0; i < Config.tf_type_times; ++i) {
            if (Config.tf_type_name[i].equals(type.getTitle())) {
                break;
            }
        }

        memoryImage.setImageResource(Config.tf_type_image[i]);
        TextView tv = (TextView) view.findViewById(R.id.type_name);
        tv.setText("图片类别: " + type.getTitle() + "\n自信程度: " + type.getConfidence());
        return view;
    }
}
