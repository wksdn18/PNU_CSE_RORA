package com.main.jinwookim.mainproject;

/**
 * Created by jinwookim on 2016-11-23.
 */
public class TVSValue {
    private String date;
    private String time;
    private String adu;
    private int ratio;
    private int vive;
    private int sound;

    public TVSValue() {
    }

    public TVSValue(String date, String time, String adu, int ratio, int vive, int sound) {
        this.date = date;
        this.time = time;
        this.adu = adu;
        this.ratio = ratio;
        this.vive = vive;
        this.sound = sound;
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

    public String getAdu() {
        return adu;
    }

    public void setAdu(String adu) {
        this.adu = adu;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public int getVive() {
        return vive;
    }

    public void setVive(int vive) {
        this.vive = vive;
    }

    public int getSound() {
        return sound;
    }

    public void setSound(int sound) {
        this.sound = sound;
    }
}
