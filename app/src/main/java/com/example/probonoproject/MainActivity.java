package com.example.probonoproject;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// 비콘 쓰이는 클래스는 BeaconConsumer 인터페이스 구현해야함
// 하단에 onBeaconServiceConnect() 함수 구현해야함
public class MainActivity extends AppCompatActivity implements BeaconConsumer {
    //비콘매니저 객체 초기화
    private BeaconManager beaconManager;
    private List<Beacon> beaconList = new ArrayList<>();
    public static final String TAG = "BeaconTest";
    TextView distance;
    //블루투스 Activity에는 BluetoothAdapter가 필요함
    //BluetoothAdapter 가져오려면 getDefaultAdapter() 호출
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //객체 초기화
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.bind(this);

        // 기기에 따라 setBeaconLayout 안의 내용을 바꿔줘야 함
        // 그냥 전부 add
        //iBeacon일경우
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        //eddystone_uid일경우
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));
        //eddystone_tlm일경우
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"));
        //eddystone_url일경우
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v"));
        //uribeacon 일경우
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=fed8,m:2-2=00,p:3-3:-41,i:4-21v"));
        //아래 세 개 다 iBeacon인듯..?
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));


        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            //didEnterRegion을 통해 비콘검색
            @Override
            public void didEnterRegion(final Region region) {
                Log.d(TAG, "ENTERED REGION: "+region);
            }

            @Override
            public void didExitRegion(Region region) {}

            @Override
            public void didDetermineStateForRegion(int i, Region region) {}
        });
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                Log.d(TAG, "BEACONS: "+collection.size());
            }
        });

       // Button inform = (Button)findViewById(R.id.inform);
        Switch btSwitch = (Switch)findViewById(R.id.bluetoothSwitch);
        Button btConnect = (Button)findViewById(R.id.connect);
        distance = (TextView)findViewById(R.id.distance);

/* inform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SeatInformation.class);
                startActivity(intent);
            }
        });*/

        btSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //블루투스가 이미 켜져있는 상태인지 확인
                if(isChecked){
                    //블루투스가 현재 비활성화 상태라면
                    if (!bluetoothAdapter.isEnabled()) {
                        //블루투스 활성화 요청 작업
                        //활성화 대화상자 띄우기
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                }
                else
                {
                    bluetoothAdapter.disable();
                }
            }
        });

    }


    // 비콘 감지시 호출되는 함수
    @Override
    public void onBeaconServiceConnect() {

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            // 비콘이 감지되면 해당 함수가 호출된다. Collection<Beacon> beacons에는 감지된 비콘의 리스트가,
            // region에는 비콘들에 대응하는 Region 객체가 들어온다.
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    beaconList.clear();
                    for (Beacon beacon : beacons) {
                        beaconList.add(beacon);
                    }
                }
            }
        });
    }
    // 연결버튼 클릭하면 handleMessage를 부르는 함수. 맨 처음에는 0초간격이지만 한번 호출되고 나면
    // 1초마다 불러온다.
    public void OnButtonClicked(View view){
            // 아래에 있는 handleMessage를 부르는 함수. 맨 처음에는 0초간격이지만 한번 호출되고 나면
            // 1초마다 불러온다.
            handler.sendEmptyMessage(0);
            distance.setText("비콘리스트 크기 : " + beaconList.size());
        }
        Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if(beaconList.size()>0) {
                    double minDistance = 1000;

                    Beacon minBeacon = beaconList.get(0);
                    int major = minBeacon.getId2().toInt(); //beacon major
                    int minor = minBeacon.getId3().toInt();// beacon minor

                    // 비콘의 아이디와 거리를 측정하여 textView에 넣는다.
                    for (Beacon beacon : beaconList) {
                        //최소 거리 비콘 구하기
                        if (beacon.getDistance() < minDistance) {
                            minBeacon = beacon;
                            try {
                                //모든 비콘 감지 (UUID, Major, Minor 전부 null이면 모든 비콘)
                                beaconManager.startMonitoringBeaconsInRegion(new Region("myRegion", null, null, null));
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                        String uuid = minBeacon.getId1().toString(); //beacon uuid
                        String address = minBeacon.getBluetoothAddress();
                        distance.append("ID 1 : " + minBeacon.getId2() + " / " + "Distance : " + Double.parseDouble(String.format("%.2f", minBeacon.getDistance())) + "m\n");
                        distance.append("Beacon Bluetooth Id : " + address + "\n");
                        distance.append("Beacon UUID : " + uuid + "\n");

                        if (major == 40001) {
                            //beacon 의 식별을 위하여 major값으로 확인
                            //이곳에 필요한 기능 구현
                            //textView.append("ID 1 : " + beacon.getId2() + " / " + "Distance : " + Double.parseDouble(String.format("%.3f", beacon.getDistance())) + "m\n");
                            //  textView.append("Beacon Bluetooth Id : "+address+"\n");
                            // textView.append("Beacon UUID : "+uuid+"\n");

                        } else {
                            //나머지 비콘검색
                            //textView.append("ID 2: " + beacon.getId2() + " / " + "Distance : " + Double.parseDouble(String.format("%.3f", beacon.getDistance())) + "m\n");
                        }
                    }
                    // 자기 자신을 1초마다 호출
                    handler.sendEmptyMessageDelayed(0, 1000);
                }
            }
        };

}