package com.main.jinwookim.mainproject;

/**
 * Created by jinwookim on 2016-11-23.
 */
public class LoginDataHolder {
    private static String workerID;
    private static String passwd;
    private static String wasIP;
    private static String wasPort;

    public static String getWorkerID() {
        return workerID;
    }

    public static void setWorkerID(String workerID) {
        LoginDataHolder.workerID = workerID;
    }

    public static String getPasswd() {
        return passwd;
    }

    public static void setPasswd(String passwd) {
        LoginDataHolder.passwd = passwd;
    }

    public static String getWasIP() {
        return wasIP;
    }

    public static void setWasIP(String wasIP) {
        LoginDataHolder.wasIP = wasIP;
    }

    public static String getWasPort() {
        return wasPort;
    }

    public static void setWasPort(String wasPort) {
        LoginDataHolder.wasPort = wasPort;
    }
}
