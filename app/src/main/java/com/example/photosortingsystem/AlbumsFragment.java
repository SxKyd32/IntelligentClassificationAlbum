package com.example.photosortingsystem;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;

import com.example.photosortingsystem.adapter.AlbumAdapter;
import com.example.photosortingsystem.entity.AlbumItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.photosortingsystem.utils.ImagesScaner.getAlbumInfo;


/**
 * 相册界面fragment
 */
public class AlbumsFragment extends Fragment {
    private String content;
    private FragmentManager manager;
    private FragmentTransaction ft;
    private AlbumAdapter adapter;


    public AlbumsFragment() {
    }

    private String[] data = { "ALBUMS", "Banana", "Orange", "Watermelon",
            "Pear", "Grape", "Pineapple", "Strawberry", "Cherry", "Mango" };

    private List<Map<String, String>> result;

    private List<AlbumItem> albumList = new ArrayList<AlbumItem>();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_albums,container,false);
        initAlbums();
        GridView listView = (GridView) view.findViewById(com.example.photosortingsystem.R.id.album_list);
        manager = getFragmentManager();
        adapter = new AlbumAdapter(getActivity(), R.layout.album_item, albumList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                System.out.println(position+ " " +id);
                // 进入一个新的fregment，这个fregment就是photosFragment.java
                // photosFragment.java中显示具体相册的具体照片
                //创建新的photosFragment对象

                String type = result.get(position).get("album_name");
                Log.d("Album_Name", type);
                PhotosFragment myJDEditFragment = new PhotosFragment(type);
                ft = manager.beginTransaction();
                ft.add(R.id.ly_content , myJDEditFragment);
                ft.setTransition(FragmentTransaction. TRANSIT_FRAGMENT_OPEN);
                try {
                    ActionBar actionBar = MainActivity.actionBar;
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    actionBar.setTitle("");
                } catch (Exception e) {
                    ;
                }
                ft.addToBackStack( null);
                ft.commit();
            }
        });
        return view;
    }


    private void initAlbums() {
        AlbumItem album;
        if (getActivity().getApplicationContext() == null)
            Log.d("getContext() in Album", "null");
        result = getAlbumInfo(getActivity().getApplicationContext());
        for (Map<String, String> s: result) {
            album = new AlbumItem(s.get("album_name"), s.get("show_image"));
            albumList.add(album);
        }
    }


    public void onRefresh() {
        albumList.clear();
        initAlbums();
        adapter.notifyDataSetChanged();
    }
}
