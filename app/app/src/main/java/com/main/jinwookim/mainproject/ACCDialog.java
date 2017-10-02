package com.main.jinwookim.mainproject;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;

import com.main.jinwookim.mainproject.SensorValue.ACCValue;


/**
 * Created by jinwookim on 2017-09-18.
 */

public class ACCDialog extends Dialog {

    private ACCValue accValue;
    private ImageView car_imageView;
    private TextView textView1;
    private TextView textView2;
    private TextView textView3;

    public ACCDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.custom_dialog);

        car_imageView = (ImageView) findViewById(R.id.car_image);
        textView1 = (TextView) findViewById(R.id.textOut1);
        textView2 = (TextView) findViewById(R.id.textOut2);
        textView3 = (TextView) findViewById(R.id.textOut3);

        int x = accValue.getX();
        int y = accValue.getY();
        int z = accValue.getZ();

        if(x==-1 && y==0 && z==0){
            car_imageView.setImageResource(R.drawable.car_back);
        }
        else if(x== -1 && y==-1 && z==0){
            car_imageView.setImageResource(R.drawable.car_back_left);
        }
        else if(x== -1 && y==1 && z==0){
            car_imageView.setImageResource(R.drawable.car_back_right);
        }
        else if(x==1 && y==0 && z==0){
            car_imageView.setImageResource(R.drawable.car_front);
        }
        else if(x==1 && y==0 && z==1){
            car_imageView.setImageResource(R.drawable.car_front_z);
        }
        else if(x==0 && y==-1 && z==0){
            car_imageView.setImageResource(R.drawable.car_left);
        }
        else if(x==1 && y==-1 && z==0){
            car_imageView.setImageResource(R.drawable.car_left_front);
        }
        else if(x==0 && y==1 && z==0){
            car_imageView.setImageResource(R.drawable.car_right);
        }
        else if(x==1 && y==1 && z==0){
            car_imageView.setImageResource(R.drawable.car_right_front);
        }
        else if(x==0 && y==0 && z==1){
            car_imageView.setImageResource(R.drawable.car_z);
        }
        else if(x==1 && y==1 && z==1){
            car_imageView.setImageResource(R.drawable.car_all);
        }

        textView1.setText(accValue.getTextOutput(0));
        textView2.setText(accValue.getTextOutput(1));
        textView3.setText(accValue.getTextOutput(2));
    }

    public ACCValue getAccValue() {
        return accValue;
    }

    public void setAccValue(ACCValue accValue) {
        this.accValue = accValue;
    }
}
