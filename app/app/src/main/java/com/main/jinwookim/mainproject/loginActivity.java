package com.main.jinwookim.mainproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

public class loginActivity extends AppCompatActivity {

    SharedPreferences setting;
    SharedPreferences.Editor editor;

    private EditText E_workerID;
    private EditText E_passwd;
    private EditText E_wasIP;
    private EditText E_wasPort;
    private CheckBox C_saveState;
    private Button B_login;

    private LinearLayout L_ipLinear;
    private LinearLayout L_portLinear;
    private LinearLayout L_dbLinear;
    private Button dbdelte;

    private DBManager dbManager;

    private boolean isOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("");
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.color6));

        E_workerID = (EditText) findViewById(R.id.editText);
        E_passwd = (EditText) findViewById(R.id.editText2);
        E_wasIP = (EditText) findViewById(R.id.editText3);
        E_wasPort = (EditText) findViewById(R.id.editText4);
        C_saveState = (CheckBox) findViewById(R.id.checkBox);
        B_login = (Button) findViewById(R.id.loginButton);

        L_ipLinear = (LinearLayout) findViewById(R.id.ipLinear);
        L_portLinear = (LinearLayout) findViewById(R.id.portLinear);
        L_dbLinear = (LinearLayout) findViewById(R.id.dbLinear);
        dbdelte = (Button) findViewById(R.id.dbdelete);

        dbdelte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbManager = new DBManager(getApplicationContext(),"CAR.db",null,1);
                dbManager.deleteAll();
            }
        });

        /* 프리퍼런스 설정 */
        setting = getSharedPreferences("setting" , 0);
        editor = setting.edit();

        if(setting.getBoolean("State_Login_enabled", false)){
            E_workerID.setText( setting.getString("workerID", ""));
            E_passwd.setText(setting.getString("passwd",""));
            E_wasIP.setText(setting.getString("wasIP",""));
            E_wasPort.setText(setting.getString("wasPort",""));
            C_saveState.setChecked(true);
        }

        B_login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String workerID = E_workerID.getText().toString();
                String passwd = E_passwd.getText().toString();
                String wasIP = E_wasIP.getText().toString();
                String wasPort = E_wasPort.getText().toString();

                if( C_saveState.isChecked() ){
                    editor.putString( "workerID", workerID);
                    editor.putString( "passwd", passwd);
                    editor.putString( "wasIP", wasIP);
                    editor.putString( "wasPort", wasPort);
                    editor.putBoolean( "State_Login_enabled", true);
                    editor.commit();
                }
                else{
                    editor.clear();
                    editor.commit();
                }

                LoginDataHolder.setWorkerID( workerID );
                LoginDataHolder.setPasswd( passwd );
                LoginDataHolder.setWasIP( wasIP );
                LoginDataHolder.setWasPort( wasPort );

                Intent i = new Intent(loginActivity.this, MainActivity.class); //인텐트 생성(현 액티비티, 새로 실행할 액티비티)
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.loginmenu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* 화살표 뒤로가기 ( 로그인창으로 돌아가기 */
        switch (item.getItemId()){
            case R.id.setting: {
                /* 모든 스레드종료 */
                if( !isOpen ) {
                    L_ipLinear.setVisibility(View.VISIBLE);
                    L_portLinear.setVisibility(View.VISIBLE);
                    L_dbLinear.setVisibility(View.VISIBLE);
                    isOpen = true;
                }
                else{
                    L_ipLinear.setVisibility(View.GONE);
                    L_portLinear.setVisibility(View.GONE);
                    L_dbLinear.setVisibility(View.GONE);
                    isOpen = false;
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }


}
