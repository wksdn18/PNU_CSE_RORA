package com.main.jinwookim.mainproject.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.main.jinwookim.mainproject.R;
import com.main.jinwookim.mainproject.SensorValue.ACCValue;

import java.util.ArrayList;

/**
 * Created by jinwookim on 2016-11-23.
 */
public class ACCAdapter extends BaseAdapter {

    private ArrayList<ACCValue> accvalues;

    public ACCAdapter() {
        accvalues = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return accvalues.size();
    }

    @Override
    public Object getItem(int position) {
        return accvalues.get(position);
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
            convertView = inflater.inflate(R.layout.acc_cutsom_item, parent, false);
        }
            TextView textTime = (TextView) convertView.findViewById(R.id.textViewTime);
            TextView textScale = (TextView) convertView.findViewById(R.id.textViewScale);
            ImageView imageViewIcon = (ImageView) convertView.findViewById(R.id.imageViewIcon);
            TextView textOut = (TextView) convertView.findViewById(R.id.textViewOut);
            String t_time = accvalues.get(position).getTime();
            textTime.setText(t_time);

            int t_scale = accvalues.get(position).getScale();
            textScale.setText(Integer.toString(t_scale));

            if(t_scale>=17000){
                imageViewIcon.setImageResource(R.drawable.ic_circle_red);
            }
            else if(t_scale>=16000){
                imageViewIcon.setImageResource(R.drawable.ic_circle_yellow);
            }
            else{
                imageViewIcon.setImageResource(R.drawable.ic_circle_greeen);
            }

            textOut.setText(accvalues.get(position).getTextOutput(0));

        return convertView;
    }

    public void setACCvalues(ArrayList<ACCValue> accvalues){
        this.accvalues = accvalues;
    }

    public void add( ACCValue accValue){
        accvalues.add(accValue);
    }

    public void clear(){
        accvalues.clear();
    }
}
