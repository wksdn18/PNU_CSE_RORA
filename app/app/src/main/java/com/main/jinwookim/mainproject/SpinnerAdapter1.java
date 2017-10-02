package com.main.jinwookim.mainproject;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

/**
 * Created by jinwookim on 2017-01-04.
 */
public class SpinnerAdapter1 extends BaseAdapter {


    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.spinner_item, parent, false);
        ImageView imageView = (ImageView)convertView.findViewById(R.id.imageViewIcon);
        TextView textView = (TextView)convertView.findViewById(R.id.houseAddress);
        if(position == 0){
            textView.setText("Car 1");
        }
        else if(position == 1){
            textView.setText("Car 2");
        }
        return convertView;
    }
}
