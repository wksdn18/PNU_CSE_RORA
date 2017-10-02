package com.main.jinwookim.mainproject.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.main.jinwookim.mainproject.R;
import com.main.jinwookim.mainproject.SensorValue.GASValue;

import java.util.ArrayList;

/**
 * Created by jinwookim on 2017-09-15.
 */

public class GASAdapter extends BaseAdapter{

    private ArrayList<GASValue> gasvalues;

    public GASAdapter() {
        gasvalues = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return gasvalues.size();
    }

    @Override
    public Object getItem(int position) {
        return gasvalues.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if( convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.gas_custom_item, parent, false);
        }
        TextView textTime = (TextView) convertView.findViewById(R.id.textViewTime);
        ImageView imageViewIcon = (ImageView) convertView.findViewById(R.id.imageViewIcon);
        TextView textOut = (TextView) convertView.findViewById(R.id.textViewOut);
        String t_time = gasvalues.get(position).getTime();
        textTime.setText(t_time);

        imageViewIcon.setImageResource(R.drawable.ic_smoke);

        return convertView;
    }

    public void setACCvalues(ArrayList<GASValue> gasvalues){
        this.gasvalues = gasvalues;
    }

    public void add( GASValue gasValue){
        gasvalues.add(gasValue);
    }

    public void clear(){
        gasvalues.clear();
    }
}
