package com.main.jinwookim.mainproject;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JunNote on 2016-07-07.
 */

public class ServiceByteBuilder {
    private DataOutputStream output;
    private String type;

    public ServiceByteBuilder(DataOutputStream output ) {
        this.output = output;
    }
    public void TransmitData( String type) throws IOException {

        this.type = type;
        StringBuilder t_stringBuilder = new StringBuilder();
        if(type.equals(CodeHolder.LOGIN)){
            t_stringBuilder.append(CodeHolder.STX);
            t_stringBuilder.append(CodeHolder.IDS);
            t_stringBuilder.append(LoginDataHolder.getWorkerID().length());
            t_stringBuilder.append(LoginDataHolder.getWorkerID());
            t_stringBuilder.append(CodeHolder.ETX);
            t_stringBuilder.append("\n");
            output.writeBytes(t_stringBuilder.toString());

            t_stringBuilder = new StringBuilder();
            t_stringBuilder.append(CodeHolder.STX);
            t_stringBuilder.append(CodeHolder.PWS);
            t_stringBuilder.append(LoginDataHolder.getPasswd().length());
            t_stringBuilder.append(LoginDataHolder.getPasswd());
            t_stringBuilder.append(CodeHolder.ETX);
            t_stringBuilder.append("\n");
            output.writeBytes(t_stringBuilder.toString());
        }


        Log.d("동작", "succeess");
        /* byte array 전송 */
        //output.writeBytes(t_stringBuilder.toString());
        Log.d("동작2","succeess");
    }
}

