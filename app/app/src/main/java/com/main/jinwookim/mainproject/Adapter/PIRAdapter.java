package com.main.jinwookim.mainproject.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.main.jinwookim.mainproject.R;
import com.main.jinwookim.mainproject.SensorValue.PIRValue;

import java.util.ArrayList;

/**
 * Created by jinwookim on 2017-09-15.
 */

public class PIRAdapter extends BaseAdapter {

    private ArrayList<PIRValue> pirvalues;

    public PIRAdapter() {
        pirvalues = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return pirvalues.size();
    }

    @Override
    public Object getItem(int position) {
        return pirvalues.get(position);
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
            convertView = inflater.inflate(R.layout.pir_custom_item, parent, false);
        }
        TextView textTime = (TextView) convertView.findViewById(R.id.textViewTime);
        TextView textGender = (TextView) convertView.findViewById(R.id.textViewGender);
        TextView textAge = (TextView) convertView.findViewById(R.id.textViewAge);
        ImageView imageViewIcon = (ImageView) convertView.findViewById(R.id.imageViewIcon);

        String t_time = pirvalues.get(position).getTime();
        textTime.setText(t_time);

        int t_sex = pirvalues.get(position).getSex();
        if(t_sex == 0){
            textGender.setText("남성");
        }
        else{
            textGender.setText("여성");
        }

        int t_age = pirvalues.get(position).getAge();
        if(t_age == 0){
            textAge.setText("10대");
        }
        else if(t_age == 1){
            textAge.setText("2,30대");
        }
        else if(t_age == 2){
            textAge.setText("40대");
        }

        if(t_age == 0){
            imageViewIcon.setImageResource(R.drawable.ic_circle_red);
        }
        else if(t_age == 1){
            imageViewIcon.setImageResource(R.drawable.ic_circle_yellow);
        }
        else{
            imageViewIcon.setImageResource(R.drawable.ic_circle_greeen);
        }

        return convertView;
    }

    public void setPIRvalues(ArrayList<PIRValue> pirvalues){
        this.pirvalues = pirvalues;
    }

    public void add( PIRValue pirValue){
        pirvalues.add(pirValue);
    }

    public void clear(){
        pirvalues.clear();
    }
}
