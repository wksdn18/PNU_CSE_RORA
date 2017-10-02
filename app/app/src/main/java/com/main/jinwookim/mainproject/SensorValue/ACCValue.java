package com.main.jinwookim.mainproject.SensorValue;

import java.util.ArrayList;

/**
 * Created by jinwookim on 2017-09-08.
 */

public class ACCValue {
    private String date;
    private String time;
    private String adu;
    private int X;
    private int Y;
    private int Z;
    private int scale;
    private ArrayList<String> textOutput;
    private double lon; //경도
    private double lat; //위도

    public ACCValue() {
        this.textOutput = new ArrayList();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x;
    }

    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y;
    }

    public int getZ() {
        return Z;
    }

    public void setZ(int z) {
        Z = z;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String getTextOutput(int index) {
        return textOutput.get(index);
    }

    public void insertTextOutput(String text){
        textOutput.add(text);
    }

    public void setTextOutput(ArrayList textOutput) {
        this.textOutput = textOutput;
    }

    public String getAdu() {
        return adu;
    }

    public void setAdu(String adu) {
        this.adu = adu;
    }
}
