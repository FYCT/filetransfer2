package com.fyp.filetransfer2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    private int REQUEST_ENABLE_BT = 1;
    private int REQUEST_CONNECT_DEVICE_SECURE = 2;

    private String plist[];

    private Button turnOnBT_btn;
    private Button boundDevices_btn;
    private Button discoverDevices_btn;
    private Button makeDiscoverable_btn;
    private Button client_btn;
    private Button server_btn;
    private Button getFile_btn;

    private Button connectScan_btn;
    private TextView message;
    private TextView firstDeviceAddress;
    private EditText textSend;

    private BluetoothAdapter myBluetoothAdapter;
    private Set<BluetoothDevice> paired_devices;

    private BroadcastReceiver myReceiver;

    //File
    private String m_chosen;

    //socket
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//UUID.randomUUID();
    private BluetoothServerSocket btServerSocket;
    private BluetoothSocket btSocket;
    private InputStream inStream;
    private OutputStream outStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        turnOnBT_btn = (Button)findViewById(R.id.turnOnBT);
        boundDevices_btn = (Button)findViewById(R.id.bondDevices);
        discoverDevices_btn = (Button)findViewById(R.id.discoverDevices);
        makeDiscoverable_btn = (Button) findViewById(R.id.makeDiscoverable);
        client_btn = (Button)findViewById(R.id.client);
        server_btn = (Button)findViewById(R.id.server);
        firstDeviceAddress = (TextView)findViewById(R.id.firstDeviceAddress);
        textSend = (EditText)findViewById(R.id.textSend);

        getFile_btn = (Button)findViewById(R.id.getFile);

        connectScan_btn = (Button)findViewById(R.id.connectScan);

        message = (TextView)findViewById(R.id.textView);

        myReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String serverName = "Bluetooth Server";

                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    message.setText(device.getName() + "\n" + device.getAddress());
                    firstDeviceAddress.setText(device.getAddress());
                    Toast.makeText(getApplicationContext(),"Found device " + device.getName(),Toast.LENGTH_LONG).show();


                }
                //server socket
                class AcceptThread extends Thread {
                    private final BluetoothServerSocket mmServerSocket;

                    public AcceptThread() {
                        // Use a temporary object that is later assigned to mmServerSocket,
                        // because mmServerSocket is final
                        String serverName = "Bluetooth Server";
                        BluetoothServerSocket tmp = null;
                        try {
                            // MY_UUID is the app's UUID string, also used by the client code
                            tmp = myBluetoothAdapter.listenUsingRfcommWithServiceRecord(serverName, MY_UUID);
                        } catch (IOException e) { Log.d("BLUETOOTH Client socket",e.getMessage());}
                        mmServerSocket = tmp;
                    }

                    public void run() {
                        BluetoothSocket socket = null;
                        // Keep listening until exception occurs or a socket is returned
                        while (true) {
                            try {
                                socket = mmServerSocket.accept();
                            } catch (IOException e) {
                                Log.d("BLUETOOTH Server socket",e.getMessage());
                                break;
                            }
                            // If a connection was accepted
                            try {
                                if (socket != null) {
                                    // Do work to manage the connection (in a separate thread)
                                    //manageConnectedSocket(socket);
                                    mmServerSocket.close();
                                    break;
                                }
                            }catch(IOException e){ Log.d("BLUETOOTH Server socket",e.getMessage());}
                        }
                    }

                    /** Will cancel the listening socket, and cause the thread to finish */
                    public void cancel() {
                        try {
                            mmServerSocket.close();
                        } catch (IOException e) { Log.d("BLUETOOTH Server socket",e.getMessage()); }
                    }
                }
            }
        };
        IntentFilter bluetoothFilter = new IntentFilter();
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(myReceiver, bluetoothFilter);

        //--------------------------------------------------------------
        turnOnBT_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnBT();
            }
        });
        //--------------------------------------------------------------
        boundDevices_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoBoundDevices();
            }
        });
        //--------------------------------------------------------------
        discoverDevices_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (myBluetoothAdapter == null) {
                    // No bluetooth Hardware found.
                }
                if (!myBluetoothAdapter.isEnabled()) {
                    // Bluetooth is OFF
                }

                if (myBluetoothAdapter.isDiscovering()) {
                    // cancel the discovery if it has already started
                    myBluetoothAdapter.cancelDiscovery();
                }

                if (myBluetoothAdapter.startDiscovery()) {
                    // bluetooth has started discovery
                }
            }
        });
        //--------------------------------------------------------------
        makeDiscoverable_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
                startActivity(discoverableIntent);

                Toast.makeText(getApplicationContext(),"Device Discoverable to others for 300 seconds",Toast.LENGTH_LONG).show();
            }
        });
        //--------------------------------------------------------------
        //Not implemented
        server_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BluetoothDevice device = myBluetoothAdapter.getRemoteDevice(firstDeviceAddress.getText().toString());
                try {
                    btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    myBluetoothAdapter.cancelDiscovery();
                    btSocket.connect();

                    //String message = "Hello.............. from....... Android......\n";
                    String msg = textSend.getText().toString();
                    outStream = btSocket.getOutputStream();
                    byte[] msgBuffer = msg.getBytes();
                    outStream.write(msgBuffer);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //--------------------------------------------------------------
        //Not implemented
        client_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Thread getSth = new Thread()
                {
                    private int x = 0;
                    public void run()
                    {
                        BufferedReader myBufferedReader;
                        String data;

                        try {
                            btServerSocket = myBluetoothAdapter.listenUsingRfcommWithServiceRecord("Bluetooth Server",MY_UUID);
                            myBluetoothAdapter.cancelDiscovery();
                            btSocket = btServerSocket.accept();
                            inStream = btSocket.getInputStream();
                            //if(mInputStream.available()>0){
                            myBufferedReader = new BufferedReader(new InputStreamReader(inStream));
                            data = myBufferedReader.readLine();
                            message.setText(data);
                            //}

                            while(x<50) {
                                if (inStream.available() > 0) {
                                    data = myBufferedReader.readLine();
                                    message.setText(data);
                                    Thread.sleep(100);
                                    x++;
                                }
                            }
                            Toast.makeText(getApplicationContext(),"Receive Thread Ended",Toast.LENGTH_LONG).show();



                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                };

            }
            class AcceptThread extends Thread {
                private final BluetoothServerSocket mmServerSocket;

                public AcceptThread() {
                    // Use a temporary object that is later assigned to mmServerSocket,
                    // because mmServerSocket is final
                    String serverName = "Bluetooth Server ";
                    BluetoothServerSocket tmp = null;
                    try {
                        // MY_UUID is the app's UUID string, also used by the client code
                        tmp = myBluetoothAdapter.listenUsingRfcommWithServiceRecord(serverName,MY_UUID);
                    } catch (IOException e) { Log.d("BLUETOOTH Client socket",e.getMessage()); }
                    mmServerSocket = tmp;
                }

                public void run() {
                    BluetoothSocket socket = null;
                    // Keep listening until exception occurs or a socket is returned
                    while (true) {
                        try {
                            try {
                                socket = mmServerSocket.accept();
                            } catch (IOException e) {
                                break;
                            }
                            // If a connection was accepted
                            if (socket != null) {
                                // Do work to manage the connection (in a separate thread)
                                // manageConnectedSocket(socket);
                                mmServerSocket.close();
                                break;
                            }
                        }catch(IOException e){  Log.d("BLUETOOTH Client socket",e.getMessage());}
                    }
                }

                /** Will cancel the listening socket, and cause the thread to finish */
                public void cancel() {
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) { Log.d("BLUETOOTH Client socket",e.getMessage()); }
                }
            }
        });

        //---------------------------------------------
        //Not Implemented
       connectScan_btn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
              //Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                //startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
               Toast.makeText(getApplicationContext(),"NOT IMPLEMENTED",Toast.LENGTH_LONG).show();
           }
       });

        //---------------------------------------------
        getFile_btn.setOnClickListener(new View.OnClickListener() {
            String m_chosen;

            @Override
            public void onClick(View v) {
                ///////////////////////////////////////////////////////////////////////////////////////////////// //Create FileOpenDialog and register a callback /////////////////////////////////////////////////////////////////////////////////////////////////
                SimpleFileDialog FileOpenDialog = new SimpleFileDialog(MainActivity.this, "FileOpen", new SimpleFileDialog.SimpleFileDialogListener() {
                    @Override
                    public void onChosenDir(String chosenDir) {
                        // The code in this function will be executed when the dialog OK button is pushed
                        m_chosen = chosenDir;
                        Toast.makeText(MainActivity.this, "Chosen FileOpenDialog File: " + m_chosen, Toast.LENGTH_LONG).show();

                        //testing
                        File file = new File(chosenDir);
                        int size = (int) file.length();
                        byte[] bytes = new byte[size];
                        try {
                            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                            buf.read(bytes, 0, bytes.length);
                            buf.close();
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                });

                //You can change the default filename using the public variable "Default_File_Name"
                FileOpenDialog.Default_File_Name = "";
                FileOpenDialog.chooseFile_or_Dir();
            }
        });
 }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ENABLE_BT)
        {
            //message.setText("onActivityResult Reached");
            if(resultCode==RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Turn On Bluetooth Request Cancelled", Toast.LENGTH_LONG).show();
            }
            if(resultCode==RESULT_OK)
            {
                Toast.makeText(getApplicationContext(), "Bluetooth Turned On Successfully", Toast.LENGTH_LONG).show();
            }

            if(resultCode==REQUEST_CONNECT_DEVICE_SECURE)
            {
                Toast.makeText(getApplicationContext(),"NOT IMPLEMENTED",Toast.LENGTH_LONG).show();
            }
        }
    }

    public void gotoBoundDevices()
    {
        if(myBluetoothAdapter!=null)
        {
            Toast.makeText(getApplicationContext(), "SUCCESS", Toast.LENGTH_LONG).show();
            paired_devices = myBluetoothAdapter.getBondedDevices();

            int count = paired_devices.size();
            plist = new String[count];
            int i = 0;
            for (BluetoothDevice device : paired_devices) {
                plist[i] = device.getName();

                if (count!=0 && i==0)
                {
                    message.setText(device.getName());
                }
                i++;
            }


            Intent intent = new Intent(MainActivity.this, BoundDevices.class);

            Bundle bundle = new Bundle();
            String key = "BlueToothPairedDevice";
            bundle.putStringArray(key, plist);

            intent.putExtras(bundle);
            startActivity(intent);
        }
        else
        {
            turnOnBT();
        }
    }

    public void turnOnBT()
    {
        //myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(myBluetoothAdapter ==null)
        {
            message.setText("Bluetooth Not Supported");
            Toast.makeText(getApplicationContext(),"FAILED",Toast.LENGTH_LONG).show();
        }
        else
        {
            if (!myBluetoothAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Bluetooth is already ON", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        myBluetoothAdapter.cancelDiscovery();
        unregisterReceiver(myReceiver);
    }

  /*  @Override
    public boolean onItemSelected(View view) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(view.getItemId())
        {
            case R.id.connectScan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                //REQUEST_CONNECT_DEVICE_SECURE = 1
                startActivityForResult(serverIntent, 1);
                Log.i("ItemSelect","connectScan starts");
                return true;
            }

        }
        return super.onOptionsItemSelected(item);
    }
*/


}
