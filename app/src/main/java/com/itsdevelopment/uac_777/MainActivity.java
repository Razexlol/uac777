package com.itsdevelopment.uac_777;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.appsflyer.AppsFlyerLib;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Integer> bitmapArrayList = new ArrayList<>();
    private ListView listView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppsFlyerLib.getInstance().start(this);


        listView = findViewById(R.id.list_view);
        bitmapArrayList.add(R.drawable.casino_1);
        bitmapArrayList.add(R.drawable.casino_2);
        bitmapArrayList.add(R.drawable.casino_3);
        bitmapArrayList.add(R.drawable.casino_4);
        bitmapArrayList.add(R.drawable.casino_5);
        bitmapArrayList.add(R.drawable.casino_6);
        bitmapArrayList.add(R.drawable.casino_7);
        bitmapArrayList.add(R.drawable.casino_8);


        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return bitmapArrayList.size();
            }

            @Override
            public Object getItem(int position) {
                return bitmapArrayList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater layoutInflater = getLayoutInflater();
                View viewInflated = layoutInflater.inflate(R.layout.list_item_view, parent, false);
                ImageView imageView = viewInflated.findViewById(R.id.image_view);
                imageView.setImageResource(bitmapArrayList.get(position));
                return viewInflated;
            }


        });
    }


}