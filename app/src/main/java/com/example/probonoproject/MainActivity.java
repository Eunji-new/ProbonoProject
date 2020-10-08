package com.example.probonoproject;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

// 비콘 쓰이는 클래스는 BeaconConsumer 인터페이스 구현해야함
// 하단에 onBeaconServiceConnect() 함수 구현해야함
public class MainActivity extends AppCompatActivity implements BeaconConsumer {
    //비콘매니저 객체 초기화
    private BeaconManager beaconManager;

    public static final String TAG = "BeaconTest";

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

       // Button inform = (Button)findViewById(R.id.inform);
        Switch btSwitch = (Switch)findViewById(R.id.bluetoothSwitch);

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
                    else {
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
        try {
            //모든 비콘 감지 (UUID, Major, Minor 전부 null이면 모든 비콘)
            beaconManager.startMonitoringBeaconsInRegion(new Region("myRegion", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}