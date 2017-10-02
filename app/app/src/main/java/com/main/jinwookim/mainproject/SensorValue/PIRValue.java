package com.main.jinwookim.mainproject.SensorValue;

/**
 * Created by jinwookim on 2017-09-08.
 */

public class PIRValue {
    private String date;
    private String time;
    private String adu;
    private int sex;
    private int age;
    private double lon; //경도
    private double lat; //위도

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

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAdu() {
        return adu;
    }

    public void setAdu(String adu) {
        this.adu = adu;
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
}
