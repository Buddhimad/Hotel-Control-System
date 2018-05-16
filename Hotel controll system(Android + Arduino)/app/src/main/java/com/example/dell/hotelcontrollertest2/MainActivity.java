package com.example.dell.hotelcontrollertest2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Handler;

import static java.lang.Compiler.disable;


public class MainActivity extends AppCompatActivity {
    ProgressBar progressBar1, progressBar2, progressBar3;
    ToggleButton btnConnection, btnLED;
    Handler h;
    TextView txtConnection;

    final int RECEIVE_MESSAGE = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();
    private static final String TAG = "bluetooth1";
    private OutputStream outStream = null;
    private CheckBox firstfloor,secondfloor,thirdfloor,fourthfloor,fifthfloor;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = "00:21:13:00:B4:C5";
    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        runProgressBar1();
        onLights();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    public void onDestroy() {
        super.onDestroy();

    /* ... */

        // Unregister broadcast listeners
        unregisterReceiver(mReceiver);
    }

    private void runProgressBar1() {

        btAdapter = BluetoothAdapter.getDefaultAdapter();


        btnConnection = (ToggleButton) findViewById(R.id.btnConnection);
        progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
       // progressBar1.setProgressDrawable(getResources().getDrawable(R.drawable.progressbars));
        txtConnection = (TextView) findViewById(R.id.txtConnection);
        btnConnection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!btAdapter.isEnabled()) {
                        Intent enableBtIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);

                    }

                } else {
                    btAdapter.disable();
                    Toast.makeText(getBaseContext(), "Disabeling bluetooth", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        btnConnection.setChecked(false);

                        progressBar1.setIndeterminate(false);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        btnConnection.setChecked(false);
                        progressBar1.setIndeterminate(true);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        descover();
                        btnConnection.setChecked(true);
                        progressBar1.setIndeterminate(true);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        btnConnection.setChecked(true);

                        break;
                }
            }
        }
    };

    public void descover(){
        BluetoothDevice device=null;
        device= btAdapter.getRemoteDevice(address);

            Toast.makeText(getBaseContext(), "device found", Toast.LENGTH_LONG).show();
            try {
                btSocket = createBluetoothSocket(device);

            } catch (Exception ex) {
                Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            btAdapter.cancelDiscovery();
            Log.d(TAG, "...Connecting...");
            try {
                btSocket.connect();
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e1) {
                    errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e1.getMessage() + ".");
                }
            }
            Log.d(TAG, "...Create Socket...");
            try {
                outStream = btSocket.getOutputStream();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
            }
        }



    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
       if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }
    private void errorExit(String title,String message){
        Toast.makeText(getBaseContext(),title+"-"+message,Toast.LENGTH_LONG).show();
        finish();
    }
    private void sendData(String message){
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "...Send data: " + message + "...");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            errorExit("Fatal Error", msg);
        }
    }
    public void onLights(){
        firstfloor= (CheckBox) findViewById(R.id.firstfloor);
        firstfloor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    sendData("1");

                }else {
                    sendData("0");
                }
            }
        });

        secondfloor= (CheckBox) findViewById(R.id.secondfloor);
        secondfloor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    sendData("3");
                }else {
                    sendData("2");
                }
            }
        });
        thirdfloor= (CheckBox) findViewById(R.id.thirdfloor);
        thirdfloor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sendData("5");
                }else {
                    sendData("4");
                }
            }
        });
        fourthfloor= (CheckBox) findViewById(R.id.fourthfloor);
        fourthfloor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    sendData("7");
                }else{
                    sendData("6");
                }
            }
        });
        fifthfloor= (CheckBox) findViewById(R.id.fifthfloor);
        fifthfloor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    sendData("9");
                }else {
                    sendData("8");
                }
            }
        });
    }
}
