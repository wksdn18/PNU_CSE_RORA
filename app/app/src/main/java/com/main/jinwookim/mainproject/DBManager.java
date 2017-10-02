package com.main.jinwookim.mainproject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.main.jinwookim.mainproject.SensorValue.ACCValue;
import com.main.jinwookim.mainproject.SensorValue.GASValue;
import com.main.jinwookim.mainproject.SensorValue.PIRValue;

import java.util.ArrayList;

/**
 * Created by jinwookim on 2016-11-23.
 */
public class DBManager extends SQLiteOpenHelper {

    public DBManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블을 생성한다.
        // create table 테이블명 (컬럼명 타입 옵션);
        db.execSQL("CREATE TABLE ACC_LIST( _id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT, time TEXT, adu TEXT, x INTEGER, y INTEGER, z INTEGER, scale INTEGER, textout1 TEXT, textout2 TEXT, textout3 TEXT, lon REAL, lat REAL);");
        db.execSQL("CREATE TABLE GAS_LIST( _id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT, time TEXT, adu TEXT, lon REAL, lat REAL);");
        db.execSQL("CREATE TABLE PIR_LIST( _id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT, time TEXT, adu TEXT, sex INTEGER, age INTEGER, lon REAL, lat REAL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insertACC(ACCValue accValue) {
        SQLiteDatabase db = getWritableDatabase();
        //Log.d("time insert",tvsValue.getTime());
        String sql = "insert into ACC_LIST values(null, '"+accValue.getDate()+"', '"+accValue.getTime()+"', '"
                +accValue.getAdu()+"', "+accValue.getX()+", "+accValue.getY()+", "+accValue.getZ()+", "+accValue.getScale()+", '"+accValue.getTextOutput(0)+"', '"+accValue.getTextOutput(1)+"', '"+accValue.getTextOutput(2)+"', "+accValue.getLat()+", "+accValue.getLat()+");";
        db.execSQL(sql);
        db.close();
    }

    public void insertGAS(GASValue gasValue) {
        SQLiteDatabase db = getWritableDatabase();
        //Log.d("time insert",tvsValue.getTime());
        String sql = "insert into GAS_LIST values(null, '"+gasValue.getDate()+"', '"+gasValue.getTime()+"', '"
                +gasValue.getAdu()+"', "+gasValue.getLon()+", "+gasValue.getLat()+");";
        db.execSQL(sql);
        db.close();
    }

    public void insertPIR(PIRValue pirValue) {
        SQLiteDatabase db = getWritableDatabase();
        //Log.d("time insert",tvsValue.getTime());
        String sql = "insert into PIR_LIST values(null, '"+pirValue.getDate()+"', '"+pirValue.getTime()+"', '"
                +pirValue.getAdu()+"', "+pirValue.getSex()+", "+pirValue.getAge()+", "+pirValue.getLon()+", "+pirValue.getLat()+");";
        db.execSQL(sql);
        db.close();
    }

    public void update(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public void delete(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public void deleteAll(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from ACC_LIST");
        db.execSQL("delete from GAS_LIST");
        db.execSQL("delete from PIR_LIST");
        db.close();
    }

    public ArrayList<ACCValue> returnACC(String date, String adu) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ACCValue> accValues = new ArrayList<>();

        Log.d("던진값", date);
        Log.d("던진값2", adu);
        String sql = "select * from ACC_LIST where date = '"+date+"' and adu = '"+adu+"';";
        Log.d("쿼리동작", "동작");

        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while(cursor.moveToNext()) {
            ACCValue tempACCValue = new ACCValue();

            tempACCValue.setDate(cursor.getString(1));
            tempACCValue.setTime(cursor.getString(2));
            tempACCValue.setAdu(cursor.getString(3));
            tempACCValue.setX(cursor.getInt(4));
            tempACCValue.setY(cursor.getInt(5));
            tempACCValue.setZ(cursor.getInt(6));
            tempACCValue.setScale(cursor.getInt(7));
            tempACCValue.insertTextOutput(cursor.getString(8));
            tempACCValue.insertTextOutput(cursor.getString(9));
            tempACCValue.insertTextOutput(cursor.getString(10));
            tempACCValue.setLon(cursor.getDouble(11));
            tempACCValue.setLon(cursor.getDouble(12));

            Log.d("DB확인",tempACCValue.getTime());
            Log.d("DB확인",tempACCValue.getDate());
            accValues.add(tempACCValue);
        }
        return accValues;
    }

    public ArrayList<GASValue> returnGAS(String date, String adu) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<GASValue> gasValues = new ArrayList<>();

        Log.d("던진값", date);
        Log.d("던진값2", adu);
        String sql = "select * from GAS_LIST where date = '"+date+"' and adu = '"+adu+"';";
        Log.d("쿼리동작", "동작");
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while(cursor.moveToNext()) {
            GASValue tempGASValue = new GASValue();

            tempGASValue.setDate(cursor.getString(1));
            tempGASValue.setTime(cursor.getString(2));
            tempGASValue.setAdu(cursor.getString(3));
            tempGASValue.setLon(cursor.getDouble(4));
            tempGASValue.setLat(cursor.getDouble(5));
            gasValues.add(tempGASValue);
        }
        return gasValues;
    }

    public ArrayList<PIRValue> returnPIR(String date, String adu) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<PIRValue> pirValues = new ArrayList<>();

        Log.d("던진값", date);
        Log.d("던진값2", adu);
        String sql = "select * from PIR_LIST where date = '"+date+"' and adu = '"+adu+"';";
        Log.d("쿼리동작", "동작");
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while(cursor.moveToNext()) {
            PIRValue tempPIRValue = new PIRValue();

            tempPIRValue.setDate(cursor.getString(1));
            tempPIRValue.setTime(cursor.getString(2));
            tempPIRValue.setAdu(cursor.getString(3));
            tempPIRValue.setSex(cursor.getInt(4));
            tempPIRValue.setAge(cursor.getInt(5));
            tempPIRValue.setLon(cursor.getDouble(6));
            tempPIRValue.setLat(cursor.getInt(7));
            pirValues.add(tempPIRValue);
        }
        return pirValues;
    }
}
