package com.main.jinwookim.mainproject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import android.app.FragmentManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.main.jinwookim.mainproject.Adapter.ACCAdapter;
import com.main.jinwookim.mainproject.Adapter.GASAdapter;
import com.main.jinwookim.mainproject.Adapter.PIRAdapter;
import com.main.jinwookim.mainproject.SensorValue.ACCValue;
import com.main.jinwookim.mainproject.SensorValue.GASValue;
import com.main.jinwookim.mainproject.SensorValue.PIRValue;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final int S_TIMEOUT = 10000; // socket timeout 시간
    private static final int S_RCTIME = 2000; // sokcet 접속 요청 반복 시간

    /* 핸들러 요청 상수값 */
    private static final int H_LOGIN = 100; // 로그인
    private static final int H_ACC = 200; // 상태값
    private static final int H_GAS = 300;
    private static final int H_PIR = 400;
    private static final int H_CFAIL = 102; // 연결 실패
    private static final int H_RCONNECT = 103; // 재접속 요청

    private static final int RSEND_TIME = 5000; // state 재요청 시간

    private boolean B_run = true; // 스레드 동작 boolean. true일때 thread동작
    private boolean LoginState = false; // Login상태 boolean. login성공시 true

    private Handler msghandler; // msgHandler. 수신된 데이터의 UI처리
    private Timer timer; // 5초마다 데이터를 수신하기 위한 timer
    private TimerTask adTast;

    private SocketClient client; // Socket연결 thread
    private ReceiveThread receive; // 수신 thread
    private SendThread send; // 전송 thread
    private Socket socket; // Socekt 변수

    private LinearLayout ACCLinear;
    private LinearLayout PIRLinear;
    private LinearLayout GASLinear;

    private ListView ACC_listView;
    private ListView PIR_listView;
    private ListView GAS_listView;

    private LineChart chart;
    private BarChart barchart;

    private ACCAdapter accAdapter; // listView Adapter
    private GASAdapter gasAdapter;
    private PIRAdapter pirAdapter;

    private Spinner spinner; //연결된 동 호수 선택 스피너
    private Spinner spinner2;
    private ArrayList<String> listview_items; //스피너 관련
    private ArrayAdapter<String> listview_adapter; //스피너 관련


    private MapFragment mapFragment; //구글맵
    private FragmentManager fragmentManager;
    private FragmentTransaction ft;

    private LinkedList<SocketClient> threadList; // SocketClient LinkedList
    //private Logger logger = new Logger(); // log 정보를 txt에 저장하기 위한 method
    private ProgressDialog asyncDialog; // "연결중..." 표출 dialog

    private DBManager dbManager;
    private Calendar calendar;
    private CalendarView calendarView;
    private TextView DateView;

    private String selectDate;
    private String selectadu;

    private ACCValue accValue;
    private GASValue gasValue;
    private PIRValue pirValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("");
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.color6));
        calendarView = (CalendarView) findViewById(R.id.calendarView);
        calendar = Calendar.getInstance();
        DateView = (TextView) findViewById(R.id.selectedDate);
        DateView.setText(Integer.toString(calendar.get(Calendar.YEAR))+
                "-"+Integer.toString(calendar.get(Calendar.MONTH)+1)+"-"+Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));
        selectDate = Integer.toString(calendar.get(Calendar.YEAR))+"-"+
                Integer.toString(calendar.get(Calendar.MONTH)+1)+"-"+Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
        selectadu = "car 1";
        dbManager = new DBManager(getApplicationContext(),"CAR.db",null,1);
        chart = (LineChart) findViewById(R.id.chart);
        barchart = (BarChart) findViewById(R.id.barchart);

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner2 = (Spinner) findViewById(R.id.spinner2);

        ACCLinear = (LinearLayout) findViewById(R.id.acc_linear);
        PIRLinear = (LinearLayout) findViewById(R.id.pir_linear);
        GASLinear = (LinearLayout) findViewById(R.id.gas_linear);

        ACC_listView = (ListView) findViewById(R.id.acclistView);
        PIR_listView = (ListView) findViewById(R.id.pirlistView);
        GAS_listView = (ListView) findViewById(R.id.gaslistView);

        accAdapter = new ACCAdapter();
        gasAdapter = new GASAdapter();
        pirAdapter = new PIRAdapter();
        // 구글 맵 관련
        fragmentManager = getFragmentManager();
        ft = fragmentManager.beginTransaction();
        mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ACC_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ArrayList<ACCValue> t_accvalues = dbManager.returnACC(selectDate, selectadu);
                ACCDialog t_ACCDialog = new ACCDialog(MainActivity.this);
                t_ACCDialog.setAccValue(t_accvalues.get(position));
                t_ACCDialog.show();
            }
        });
        ACC_listView.setAdapter(accAdapter);
        GAS_listView.setAdapter(gasAdapter);
        PIR_listView.setAdapter(pirAdapter);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                DateView.setText(Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(dayOfMonth));
                selectDate = Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(dayOfMonth);
                ACCDataUpdate();
                listSetup();
            }
        });
        //데이터를 저장하게 되는 리스트
        List<String> spinner_items = new ArrayList<>();
        //스피너와 리스트를 연결하기 위해 사용되는 어댑터
        /*
        ArrayAdapter<String> spinner_adapter=new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinner_items);
        spinner_items.add("104-1801");
        spinner_items.add("104-1901");
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);*/
        SpinnerAdapter1 spinnerAdapter1 = new SpinnerAdapter1();
        //스피너의 어댑터 지정
        spinner.setAdapter(spinnerAdapter1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectadu = "car 1";
                } else if (position == 1) {
                    selectadu = "car 2";
                }
                ACCDataUpdate();
                listSetup();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectadu = "car 1";
                ACCDataUpdate();
                listSetup();
            }
        });

        //데이터를 저장하게 되는 리스트
        List<String> spinner_items2 = new ArrayList<>();
        //스피너와 리스트를 연결하기 위해 사용되는 어댑터
        ArrayAdapter<String> spinner_adapter2=new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinner_items2);
        spinner_items2.add("Real");
        spinner_items2.add("Day");
        spinner_items2.add("Anual");
        spinner_adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //스피너의 어댑터 지정
        spinner2.setAdapter(spinner_adapter2);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    ACCDataUpdate();
                } else if (position == 1) {
                    dayChart();
                }
                  else if (position == 2) {
                    anualChart();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ACCDataUpdate();
            }
        });

        threadList = new LinkedList<MainActivity.SocketClient>();

        asyncDialog = new ProgressDialog(MainActivity.this);
        asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        asyncDialog.setMessage("연결중입니다..");
        asyncDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //exitActivity();
            }
        });


        msghandler = new Handler() {
            @Override
            public void handleMessage(Message hdmsg) {
                if (hdmsg.what == H_LOGIN) {
                    String loginresult = hdmsg.obj.toString();
                    if (loginresult.equals("OK")) {
                        Toast.makeText(getApplicationContext(), "로그인 성공 ", Toast.LENGTH_SHORT).show();
                        LoginState = true;
                    } else if (loginresult.equals("FAIL")) {
                        Toast.makeText(getApplicationContext(), "로그인 실패 ", Toast.LENGTH_SHORT).show();
                        LoginState = true;
                        //finish();
                    }
                } else if (hdmsg.what == H_ACC) {
                    accAdapter.clear();

                    Toast ToastMessage = Toast.makeText(getApplicationContext(), " ACC 수신됨 ", Toast.LENGTH_SHORT);
                    ToastMessage.show();

                    String values = hdmsg.obj.toString();
                    String value_array[] = values.split(",");
                    calendar = Calendar.getInstance();
                    String t_date = Integer.toString(calendar.get(Calendar.YEAR))+"-"+
                            Integer.toString(calendar.get(Calendar.MONTH)+1)+"-"+Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
                    String t_time = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY))+":"+Integer.toString(calendar.get(Calendar.MINUTE))+
                            ":"+Integer.toString(calendar.get(Calendar.SECOND));
                    accValue = new ACCValue();
                    accValue.setDate(t_date);
                    accValue.setTime(t_time);
                    accValue.setAdu("car 1");
                    accValue.setX(Integer.parseInt(value_array[0]));
                    accValue.setY(Integer.parseInt(value_array[1]));
                    accValue.setZ(Integer.parseInt(value_array[2]));

                    accValue.setScale(Integer.parseInt(value_array[3]));
                    int lonA = Integer.parseInt(value_array[4]);
                    int lonB = Integer.parseInt(value_array[5])/1000;
                    accValue.setLon(lonA + lonB);
                    int latA = Integer.parseInt(value_array[6]);
                    int latB = Integer.parseInt(value_array[7])/1000;
                    accValue.setLat(latA+latB);
                    accValue.insertTextOutput(value_array[8]);
                    accValue.insertTextOutput(value_array[9]);
                    accValue.insertTextOutput(value_array[10]);

                    if(accValue.getScale()>=17000) {
                        NotificationSomethings("car 1");
                    }

                    dbManager.insertACC(accValue);
                    ACCDataUpdate();

                } else if (hdmsg.what == H_GAS) {

                    Toast ToastMessage = Toast.makeText(getApplicationContext(), " GAS 수신됨 ", Toast.LENGTH_SHORT);
                    ToastMessage.show();

                    String values = hdmsg.obj.toString();
                    String value_array[] = values.split(",");
                    calendar = Calendar.getInstance();
                    String t_date = Integer.toString(calendar.get(Calendar.YEAR))+"-"+
                            Integer.toString(calendar.get(Calendar.MONTH)+1)+"-"+Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
                    String t_time = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY))+":"+Integer.toString(calendar.get(Calendar.MINUTE))+
                            ":"+Integer.toString(calendar.get(Calendar.SECOND));
                    gasValue = new GASValue();
                    gasValue.setDate(t_date);
                    gasValue.setTime(t_time);
                    gasValue.setAdu("car 1");

                    int lonA = Integer.parseInt(value_array[0]);
                    int lonB = Integer.parseInt(value_array[1])/1000;
                    gasValue.setLon(lonA + lonB);
                    int latA = Integer.parseInt(value_array[2]);
                    int latB = Integer.parseInt(value_array[3])/1000;
                    gasValue.setLat(latA+latB);

                    dbManager.insertGAS(gasValue);
                    ArrayList<GASValue> t_gasvalues = dbManager.returnGAS(selectDate, selectadu);
                    gasAdapter.clear();
                    gasAdapter.setACCvalues(t_gasvalues);
                    gasAdapter.notifyDataSetChanged();
                }

                else if (hdmsg.what == H_PIR) {
                    Toast ToastMessage = Toast.makeText(getApplicationContext(), " PIR 수신됨 ", Toast.LENGTH_SHORT);
                    ToastMessage.show();

                    String values = hdmsg.obj.toString();
                    String value_array[] = values.split(",");
                    calendar = Calendar.getInstance();
                    String t_date = Integer.toString(calendar.get(Calendar.YEAR))+"-"+
                            Integer.toString(calendar.get(Calendar.MONTH)+1)+"-"+Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
                    String t_time = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY))+":"+Integer.toString(calendar.get(Calendar.MINUTE))+
                            ":"+Integer.toString(calendar.get(Calendar.SECOND));
                    pirValue = new PIRValue();
                    pirValue.setDate(t_date);
                    pirValue.setTime(t_time);
                    pirValue.setAdu("car 1");

                    int lonA = Integer.parseInt(value_array[0]);
                    int lonB = Integer.parseInt(value_array[1])/1000;
                    pirValue.setLon(lonA + lonB);
                    int latA = Integer.parseInt(value_array[2]);
                    int latB = Integer.parseInt(value_array[3])/1000;
                    pirValue.setLat(latA+latB);

                    pirValue.setSex(Integer.parseInt(value_array[4]));
                    pirValue.setAge(Integer.parseInt(value_array[5]));
                    dbManager.insertPIR(pirValue);
                    ArrayList<PIRValue> t_pirvalues = dbManager.returnPIR(selectDate, selectadu);
                    pirAdapter.clear();
                    pirAdapter.setPIRvalues(t_pirvalues);
                    pirAdapter.notifyDataSetChanged();
                    listSetup();

                }
                else if (hdmsg.what == H_CFAIL) {
                    Toast.makeText(getApplicationContext(), "서버 연결 실패 ", Toast.LENGTH_SHORT).show();
                    /*
                    if ((!asyncDialog.isShowing()) && B_run) {
                        asyncDialog.setMessage("접속 중입니다");
                        asyncDialog.show();
                    }*/
                } else if (hdmsg.what == H_RCONNECT) {
                    Toast.makeText(getApplicationContext(), "서버 연결 끊김 ", Toast.LENGTH_SHORT).show();
                    /*
                    if ((!asyncDialog.isShowing()) && B_run) {
                        asyncDialog.setMessage(" 재접속 중입니다 ");
                        asyncDialog.show();
                    }*/
                }
            }
        };
        client = new SocketClient( LoginDataHolder.getWasIP(), LoginDataHolder.getWasPort());
        threadList.add(client);
        client.start();
        /*
        // 종료버튼 설정
        B_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder d = new android.app.AlertDialog.Builder(MainActivity.this);
                d.setMessage("종료 하시겠습니까?");
                d.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            finishAffinity();
                        }
                        finish();
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        System.exit(0);
                        finish();
                    }
                });

                d.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                d.show();
            }
        });

        asyncDialog.show();
        client = new SocketClient( LoginDataHolder.getWasIP(), LoginDataHolder.getWasPort());
        threadList.add(client);
        client.start();

        */

    /*
    public String getNow() {
        // 현재 시간을 msec으로 구한다.
        long now = System.currentTimeMillis();
        // 현재 시간을 저장 한다.
        Date date = new Date(now);
        // 시간 포맷으로 만든다.
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String strNow = sdfNow.format(date);
        return strNow;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 화살표 뒤로가기 ( 로그인창으로 돌아가기
        switch (item.getItemId()){
            case R.id.back_button: {
                // 모든 스레드종료
                exitActivity();
                break;
            }

            case R.id.circular_button:{
                send = new SendThread(socket, CodeHolder.C_ReQuest);
                send.start();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void exitActivity() {
        B_run = false;
        timer.cancel();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finish();
        Toast.makeText(getApplicationContext(), " 연결 종료 ", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        B_run = false;
        timer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        B_run = true;
        adTast = new TimerTask() {
            @Override
            public void run() {
                if( LoginState ) {
                    send = new SendThread(socket, CodeHolder.C_ReQuest);
                    send.start();
                }
            }
        };
        timer = new Timer();
        timer.schedule(adTast, 0 ,RSEND_TIME);
    }

    //액티비티가 stop될때 연결을 끊음. 예) home버튼은 눌러서 화면전환을 할때
    @Override
    protected void onStop() {
        super.onStop();
        B_run = false;
        timer.cancel();
    }

    //뒤로가기 눌렀을때 처리
    @Override
    public void onBackPressed() {
        android.app.AlertDialog.Builder d = new android.app.AlertDialog.Builder(this);
        d.setMessage("종료 하시겠습니까?");
        d.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    //버전 체크후 부모 activity 모두 종료 ( jelly bean이하 버전 처리 불가능 )
                    finishAffinity();
                }
                // 모든 스레드종료
                exitActivity();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                System.exit(0);
                finish();
            }
        });

        d.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        d.show();
    }
    */
    }
    class SocketClient extends Thread {

        boolean threadAlive;
        String ip;
        String port;
        String mac;
        private DataOutputStream output = null;
        int timeout = S_TIMEOUT;
        public SocketClient(String ip, String port) {
            threadAlive = true;
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {

            try {
                /* 진행 프로그레스바 진행 */
                SocketAddress socketAddress = new InetSocketAddress(ip, Integer.parseInt(port));
                Log.d("소켓접속","시도");
                socket = new Socket();
                socket.connect( socketAddress ,timeout);
                Log.d("소켓접속","시도2");
                //inputStream = socket.getInputStream();
                output = new DataOutputStream(socket.getOutputStream());
                receive = new ReceiveThread(socket);
                receive.start();

                send = new SendThread(socket,CodeHolder.LOGIN);
                //send.start();

            } catch (IOException e) {
                if( B_run ) {
                    Message hdmsg = msghandler.obtainMessage();
                    hdmsg.what = H_CFAIL;
                    hdmsg.obj = "";
                    msghandler.sendMessage(hdmsg);
                    e.printStackTrace();
                    try {
                        client.sleep(S_RCTIME);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    client.run();
                }
            }
        }
    }

    class ReceiveThread extends Thread {
        private Socket socket = null;
        private DataInputStream input;

        public ReceiveThread(Socket socket) {
            this.socket = socket;
            try{
                input = new DataInputStream(socket.getInputStream());
            }catch(Exception e){
            }
        }
        // 메세지 수신후 Handler로 전달
        public void run() {
            String S_code = "";
            String S_data = "";
            String temp;
            try {
                while (((temp = input.readLine()) != null)) {
                    Log.d("수신됨", temp);
                    S_code = temp.substring(0,3);
                    int length = temp.length();
                    S_data = temp.substring(4,length);
                    Log.d("수신됨", S_code);
                    Log.d("수신됨", S_data);

                    ReceiveDataHandler(S_code,S_data);
                }
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                Message hdmsg = msghandler.obtainMessage();
                hdmsg.what = H_RCONNECT;
                hdmsg.obj = "";
                msghandler.sendMessage(hdmsg);
                e.printStackTrace();
                try {
                    client.sleep(S_RCTIME);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                client.run();
            }
        }
    }

    public void ReceiveDataHandler( String code, String data) throws UnsupportedEncodingException {
        /*
        if( code.equals( CodeHolder.PIR) ){
            Message hdmsg = msghandler.obtainMessage();
            hdmsg.what = H_LOGIN;
            hdmsg.obj = data;
            msghandler.sendMessage(hdmsg);
        }*/
        if( code.equals( CodeHolder.ACC)){
            Message hdmsg = msghandler.obtainMessage();
            hdmsg.what = H_ACC;
            hdmsg.obj = data;
            msghandler.sendMessage(hdmsg);
        }

        else if( code.equals( CodeHolder.GAS)){
            Message hdmsg = msghandler.obtainMessage();
            hdmsg.what = H_GAS;
            hdmsg.obj = data;
            msghandler.sendMessage(hdmsg);
        }

        else if( code.equals( CodeHolder.PIR )){
            Message hdmsg = msghandler.obtainMessage();
            hdmsg.what = H_PIR;
            hdmsg.obj = data;
            msghandler.sendMessage(hdmsg);
        }
    }

    class SendThread extends Thread {
        private Socket socket;
        private DataOutputStream output ;
        private String type;
        private ServiceByteBuilder serviceByteBuilder;

        public SendThread(Socket socket, String type) {
            this.socket = socket;
            this.type = type;
            try {
                output = new DataOutputStream(socket.getOutputStream());
            } catch (Exception e) {
            }
        }

        public void run() {
            try {
                serviceByteBuilder = new ServiceByteBuilder(output);
                if (output != null  ) {
                    /*전송시킴*/
                    serviceByteBuilder.TransmitData(type);
                }
            }  catch (NullPointerException npe) {
                npe.printStackTrace();
            } catch (IOException e) {
                //Log.d("소켓연결시도","시도");
                Message hdmsg = msghandler.obtainMessage();
                hdmsg.what = H_RCONNECT;
                hdmsg.obj = "";
                msghandler.sendMessage(hdmsg);
                e.printStackTrace();
                try {
                    client.sleep(S_RCTIME);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                client.run();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.acc: {
                ACCLinear.setVisibility(View.VISIBLE);
                PIRLinear.setVisibility(View.GONE);
                GASLinear.setVisibility(View.GONE);
                ft.hide(mapFragment);
                spinner.setVisibility(View.VISIBLE);
                spinner2.setVisibility(View.GONE);
                chart.setVisibility(View.GONE);
                barchart.setVisibility(View.GONE);
                calendarView.setVisibility(View.GONE);
                break;
            }
            case R.id.pir:{
                ACCLinear.setVisibility(View.GONE);
                PIRLinear.setVisibility(View.VISIBLE);
                GASLinear.setVisibility(View.GONE);
                ft.hide(mapFragment);
                spinner.setVisibility(View.VISIBLE);
                spinner2.setVisibility(View.GONE);
                chart.setVisibility(View.GONE);
                barchart.setVisibility(View.GONE);
                calendarView.setVisibility(View.GONE);
                break;
            }
            case R.id.gas:{
                ACCLinear.setVisibility(View.GONE);
                PIRLinear.setVisibility(View.GONE);
                GASLinear.setVisibility(View.VISIBLE);
                ft.hide(mapFragment);
                spinner.setVisibility(View.VISIBLE);
                spinner2.setVisibility(View.GONE);
                chart.setVisibility(View.GONE);
                barchart.setVisibility(View.GONE);
                calendarView.setVisibility(View.GONE);
                break;
            }

            case R.id.gps:{
                ACCLinear.setVisibility(View.GONE);
                PIRLinear.setVisibility(View.GONE);
                GASLinear.setVisibility(View.GONE);
                ft.show(mapFragment);
                spinner.setVisibility(View.VISIBLE);
                spinner2.setVisibility(View.GONE);
                chart.setVisibility(View.GONE);
                barchart.setVisibility(View.GONE);
                calendarView.setVisibility(View.GONE);
                break;
            }

            case R.id.chart:{
                ACCLinear.setVisibility(View.GONE);
                PIRLinear.setVisibility(View.GONE);
                GASLinear.setVisibility(View.GONE);
                ft.hide(mapFragment);
                spinner.setVisibility(View.GONE);
                spinner2.setVisibility(View.VISIBLE);
                chart.setVisibility(View.VISIBLE);
                barchart.setVisibility(View.GONE);
                calendarView.setVisibility(View.GONE);
                chart.animateXY(1000, 1000);
                break;
            }
            case R.id.calendar:{
                ACCLinear.setVisibility(View.GONE);
                PIRLinear.setVisibility(View.GONE);
                GASLinear.setVisibility(View.GONE);
                ft.hide(mapFragment);
                spinner.setVisibility(View.VISIBLE);
                spinner2.setVisibility(View.GONE);
                chart.setVisibility(View.GONE);
                barchart.setVisibility(View.GONE);
                calendarView.setVisibility(View.VISIBLE);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void ACCDataUpdate(){
        final ArrayList<ACCValue> t_accvalues = dbManager.returnACC(selectDate, selectadu);
        accAdapter.clear();
        accAdapter.setACCvalues(t_accvalues);
        accAdapter.notifyDataSetChanged();

        final List<Entry> tvsEntries = new ArrayList<Entry>();
        for(int i=0; i<t_accvalues.size(); i++) {
            String t_time = t_accvalues.get(i).getTime();
            String[] values = t_time.split(":");
            int new_time = 0;
            new_time += Integer.parseInt(values[0]) * 10000;
            new_time += Integer.parseInt(values[1]) * 100;
            new_time += Integer.parseInt(values[2]);

            Entry t_tvsEntry = new Entry(new_time,t_accvalues.get(i).getScale());
            tvsEntries.add(t_tvsEntry);
        }
        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis){
                int i_value = (int) value;
                Log.d("t_time",Integer.toString(i_value));
                int hour = i_value/10000;
                int minute = (i_value-(hour*10000))/100;
                int second = (i_value-(hour*10000)-(minute*100));
                String t_time = Integer.toString(hour)+":"+Integer.toString(minute)+":"+Integer.toString(second);
                Log.d("t_time",t_time);
                for(int i=0; i<t_accvalues.size(); i++){
                    if(t_accvalues.get(i).getTime().equals(t_time)){
                        return t_time;
                    }
                }
                return "";
            }
            // we don't draw numbers, so no decimal digits needed
        };
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        YAxis leftAxis = chart.getAxisLeft();
        LimitLine ll = new LimitLine(17000f, "Critical Impulse");
        ll.setLineColor(Color.RED);
        ll.setLineWidth(4f);
        ll.setTextColor(Color.BLACK);
        ll.setTextSize(12f);
        leftAxis.addLimitLine(ll);

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int hour = (int) e.getX()/10000;
                int minute = ((int) e.getX()-(hour*10000))/100;
                int second = ((int) e.getX()-(hour*10000)-(minute*100));
                String time = Integer.toString(hour) + "H" + Integer.toString(minute) + "M" + Integer.toString(second) +"S";
                String db = Integer.toString((int) e.getY()) + "scale";
                Toast.makeText(getApplicationContext(), time + "\n" + db, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected() {
            }
        });

        LineDataSet t_dataSet = new LineDataSet(tvsEntries,"Impulse");
        t_dataSet.setDrawFilled(true);
        t_dataSet.setFillColor(R.color.color3);
        LineData t_lineData = new LineData(t_dataSet);
        if(t_accvalues.size() !=0){
            chart.setData(t_lineData);
        }
        chart.invalidate();
        chart.setVisibility(View.VISIBLE);
        barchart.setVisibility(View.GONE);
    }

    public void anualChart(){
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 27f));
        entries.add(new BarEntry(1f, 81f));
        entries.add(new BarEntry(2f, 65f));
        entries.add(new BarEntry(3f, 53f));
        entries.add(new BarEntry(4f, 66f));
        // gap of 2f
        entries.add(new BarEntry(5f, 70f));
        entries.add(new BarEntry(6f, 62f));
        entries.add(new BarEntry(7f, 22f));
        entries.add(new BarEntry(8f, 41f));
        entries.add(new BarEntry(9f, 80f));
        entries.add(new BarEntry(10f, 35f));
        // gap of 2f
        entries.add(new BarEntry(11f, 42f));

        BarDataSet set = new BarDataSet(entries, "BarDataSet");
        set.setColors(ColorTemplate.COLORFUL_COLORS);
        BarData data = new BarData(set);
        data.setBarWidth(0.9f); // set custom bar width
        barchart.setData(data);
        barchart.setFitBars(true); // make the x-axis fit exactly all bars

        // the labels that should be drawn on the XAxis
        final String[] quarters = new String[] { "Jan", "Feb", "Mar", "Apr",
                "May","Jun","Jul","Aug","Sept","Oct","Nov","Dec" };

        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return quarters[(int) value];
            }
            // we don't draw numbers, so no decimal digits needed
        };

        XAxis xAxis = barchart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);
        barchart.invalidate();
        chart.setVisibility(View.GONE);
        barchart.setVisibility(View.VISIBLE);
    }

    public void dayChart(){
        List<Entry> tvsEntries = new ArrayList<Entry>();

        tvsEntries.add(new Entry(0f,15600));
        tvsEntries.add(new Entry(6f,20020));
        tvsEntries.add(new Entry(7f, 17500));
        tvsEntries.add(new Entry(9f,18300));
        tvsEntries.add(new Entry(10f,15700));
        tvsEntries.add(new Entry(12f,16400));
        tvsEntries.add(new Entry(14f,17300));
        tvsEntries.add(new Entry(18f,19400));
        tvsEntries.add(new Entry(19f,16300));
        tvsEntries.add(new Entry(22f, 15030));
        tvsEntries.add(new Entry(24f, 16400));

        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if(value == 0f){
                    return "00h";
                }
                else if(value == 6f){
                    return "06h";
                }
                else if(value == 12f){
                    return "12h";
                }
                else if(value == 18f){
                    return "18h";
                }
                else if(value == 24f) {
                    return "24h";
                }
                return "";
            }
        };
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String time = Integer.toString((int)e.getX()) + "H" + "00M" + "00S";
                String db = Integer.toString((int)e.getY()) + "scale";
                Toast.makeText(getApplicationContext(), time + "\n" + db, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected() {
            }
        });

        LineDataSet t_dataSet = new LineDataSet(tvsEntries,"Impulse");
        t_dataSet.setDrawFilled(true);
        t_dataSet.setFillColor(R.color.color3);
        LineData t_lineData = new LineData(t_dataSet);
        chart.setData(t_lineData);
        chart.invalidate();
        chart.setVisibility(View.VISIBLE);
        barchart.setVisibility(View.GONE);
    }

    public void NotificationSomethings(String t_arduino) {


        Resources res = getResources();


        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        String message = "";
        if(t_arduino.equals("car 1")){
            message = "1번 차량에서 충격이 감지되었습니다.";
        }
        else if(t_arduino.equals("car 2")){
            message = "2번 차량에서 충격이 감지되었습니다.";
        }

        builder.setContentTitle(message)
                .setContentText("자세한 내용을 보실려면 알림 클릭")
                .setTicker(message)
                .setSmallIcon(R.drawable.ic_stat_lora)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_stat_lora))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);
        Uri soundUri= RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(soundUri);
        //알림에 진동 기능 추가
        //진동 추가시에는 AndroidManifest 파일에 uses-permission 을 통해 사용권한 받아야함  "android.permission.VIBRATE"
        builder.setVibrate(new long[]{0, 3000});



        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1234, builder.build());
    }

    @Override
    public void onMapReady(final GoogleMap map) {


        PolylineOptions polylineOptions;
        ArrayList<LatLng> arrayPoints = new ArrayList<LatLng>();;

        LatLng DISTINCT1 = new LatLng(35.231708, 129.083484);
        LatLng DISTINCT2 = new LatLng(35.231703, 129.083485);
        LatLng DISTINCT3 = new LatLng(35.232082, 129.083177);
        LatLng DISTINCT4 = new LatLng(35.233518, 129.083756);
        LatLng DISTINCT5 = new LatLng(35.234089, 129.082641);
        LatLng DISTINCT6 = new LatLng(35.233563, 129.082300);
        LatLng DISTINCT7 = new LatLng(35.232724, 129.082040);
        LatLng DISTINCT8 = new LatLng(35.231635, 129.081450);
        LatLng DISTINCT9 = new LatLng(35.231280, 129.082896);
        LatLng DISTINCT10 = new LatLng(35.231707, 129.083467);
        LatLng DISTINCT11 = new LatLng(35.231580, 129.084266);
        LatLng START = new LatLng(35.231592, 129.084235);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(START);
        markerOptions.title("시작점");
        markerOptions.snippet("부산대학교 정문");
        map.addMarker(markerOptions);

        MarkerOptions markerOptions2 = new MarkerOptions();
        markerOptions2.position(DISTINCT3);
        markerOptions2.title("충격감지");
        markerOptions2.snippet("충격 강도 : 17000");
        map.addMarker(markerOptions2);

        MarkerOptions markerOptions3 = new MarkerOptions();
        markerOptions3.position(DISTINCT5);
        markerOptions3.title("흡연감지");
        markerOptions3.snippet("가스가 감지되었습니다");
        map.addMarker(markerOptions3);

        MarkerOptions markerOptions4 = new MarkerOptions();
        markerOptions4.position(DISTINCT7);
        markerOptions4.title("인체감지");
        markerOptions4.snippet("10대 남성으로 판별되었습니다");
        map.addMarker(markerOptions4);

        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(5);


        arrayPoints.add(START);
        arrayPoints.add(DISTINCT1);
        arrayPoints.add(DISTINCT2);
        arrayPoints.add(DISTINCT3);
        arrayPoints.add(DISTINCT4);
        arrayPoints.add(DISTINCT5);
        arrayPoints.add(DISTINCT6);
        arrayPoints.add(DISTINCT7);
        arrayPoints.add(DISTINCT8);
        arrayPoints.add(DISTINCT9);
        arrayPoints.add(DISTINCT10);
        arrayPoints.add(DISTINCT11);
        polylineOptions.addAll(arrayPoints);

        map.moveCamera(CameraUpdateFactory.newLatLng(START));
        map.animateCamera(CameraUpdateFactory.zoomTo(10));
        map.addPolyline(polylineOptions);
    }

    public void listSetup(){
        accAdapter.clear();
        gasAdapter.clear();
        pirAdapter.clear();
        final ArrayList<ACCValue> t_accvalues = dbManager.returnACC(selectDate, selectadu);
        accAdapter.setACCvalues(t_accvalues);
        accAdapter.notifyDataSetChanged();
        final ArrayList<GASValue> t_gasvalues = dbManager.returnGAS(selectDate, selectadu);
        gasAdapter.setACCvalues(t_gasvalues);
        gasAdapter.notifyDataSetChanged();
        final ArrayList<PIRValue> t_pirvalues = dbManager.returnPIR(selectDate, selectadu);
        pirAdapter.setPIRvalues(t_pirvalues);
        pirAdapter.notifyDataSetChanged();
    }

}

