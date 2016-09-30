package com.ohyoon.smartct;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MainActivity extends Activity {
    // Debugging
    private static final String TAG = "Main";
    private static final boolean D = true;

    // Intent request code
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    public static final int MESSAGE_TOAST = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;

    Button Device_Connect;

    LinearLayout success_view;

    Switch windowSwitch, consentSwitch, room1Switch, room2Switch;
    Button livingRoomButton;
    TextView tempTextView, dustTextView;

    int lightLevel = 1;

    String tempStr = "0";
    String dustStr = "0";

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MESSAGE_TOAST:
                    switch (btService.getState()) {
                        case 0:
                            Toast.makeText(getApplicationContext(), "아무것도 안하는중!", Toast.LENGTH_SHORT).show();
                            Device_Connect.setVisibility(View.VISIBLE);
                            success_view.setVisibility(View.GONE);
                            break;
                        case 1:
                            Toast.makeText(getApplicationContext(), "연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            Device_Connect.setEnabled(true);
                            Device_Connect.setText("기기 연결");
                            Device_Connect.setVisibility(View.VISIBLE);
                            success_view.setVisibility(View.GONE);
                            break;
                        case 2:
                            Toast.makeText(getApplicationContext(), "연결 중입니다.", Toast.LENGTH_SHORT).show();
                            Device_Connect.setEnabled(false);
                            Device_Connect.setText("연결 중...");
                            break;
                        case 3:
                            Toast.makeText(getApplicationContext(), "연결 되었습니다.", Toast.LENGTH_SHORT).show();
                            Device_Connect.setVisibility(View.GONE);
                            success_view.setVisibility(View.VISIBLE);
                            break;
                    }
                    break;

                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    if (readMessage.substring(0,1).equals("a")){
                        tempStr = readMessage.substring(readMessage.indexOf("=") + 1,readMessage.length());
                    }else {
                        dustStr = readMessage.substring(readMessage.indexOf("=") + 1,readMessage.length());
                    }

                    tempTextView.setText(tempStr);
                    dustTextView.setText(dustStr);

                    break;

                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
            }
        }

    };

    private BluetoothService btService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Device_Connect = (Button)findViewById(R.id.Device_Connect);
        success_view = (LinearLayout)findViewById(R.id.success_view);

        windowSwitch = (Switch)findViewById(R.id.windowSwitch);
        consentSwitch = (Switch)findViewById(R.id.consentSwitch);
        livingRoomButton = (Button)findViewById(R.id.livingRoomButton);
        room1Switch = (Switch)findViewById(R.id.room1Switch);
        room2Switch = (Switch)findViewById(R.id.room2Switch);

        tempTextView = (TextView)findViewById(R.id.tempTextView);
        dustTextView = (TextView)findViewById(R.id.dustTextView);



        Device_Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btService.getDeviceState()) {
                    // 블루투스가 지원 가능한 기기일 때
                    btService.enableBluetooth();
                } else {
                    finish();
                }
            }
        });

        // BluetoothService 클래스 생성
        if(btService == null) {
            btService = new BluetoothService(this, mHandler);
        }

        windowSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendMessage("a");
                }else{
                    sendMessage("b");
                }
            }
        });

        consentSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendMessage("c");
                }else{
                    sendMessage("C");
                }
            }
        });

        livingRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lightLevel == 1){
                    sendMessage("z");
                    lightLevel++;
                    livingRoomButton.setText("1단계");
                }else if (lightLevel == 2){
                    sendMessage("x");
                    lightLevel++;
                    livingRoomButton.setText("2단계");
                }else {
                    sendMessage("v");
                    lightLevel = 1;
                    livingRoomButton.setText("3단계");
                }
            }
        });

        room1Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendMessage("e");
                }else{
                    sendMessage("E");
                }
            }
        });

        room2Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendMessage("f");
                }else{
                    sendMessage("F");
                }
            }
        });
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }
    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    private void sendMessage(String message){
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            btService.write(send);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    btService.getDeviceInfo(data);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
//                    btService.scanDevice();
                } else {
                    Log.d(TAG, "Bluetooth is not enabled");
                }
                break;
        }
    }
}
