package com.fyp.filetransfer2;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class BoundDevices extends ActionBarActivity {

       private ListView boundDeviceList;
       private String[] plist;
       private  ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bound_devices);

        boundDeviceList = (ListView)findViewById(R.id.boundDeviceList);
        Bundle bundle = this.getIntent().getExtras();
        plist = bundle.getStringArray("BlueToothPairedDevice");
        adapter =  new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,plist);
        boundDeviceList.setAdapter(adapter);

        //boundDeviceList.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bound_devices, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
