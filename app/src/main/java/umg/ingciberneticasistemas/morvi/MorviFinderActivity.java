package umg.ingciberneticasistemas.morvi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import umg.ingciberneticasistemas.morvi.adapter.DeviceAdapter;

public class MorviFinderActivity extends AppCompatActivity {

    public final static int REQUEST_ENABLE_BT = 100;
    private static final long SCAN_PERIOD = 20000;

    private BluetoothAdapter bt_adapter;
    private BluetoothLeScanner ble_scanner;
    private Handler search_handler;
    private DeviceAdapter device_adapter;
    private boolean scanning = false;

    private ProgressBar progress_bt_find;

    private ScanCallback ble_callback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            Log.i("Bluetooth", "Found: " + device.getName());
            device_adapter.addDevice(device);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.i("Bluetooth", "Found: " + results.size() + " results");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i("Bluetooth", "Failed");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morvi_finder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progress_bt_find = (ProgressBar)findViewById(R.id.progress_bt_find);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_renew);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDevices();
                Snackbar.make(view, "Searching devices", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        BluetoothManager bt_manager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bt_adapter = bt_manager.getAdapter();
        ble_scanner = bt_adapter.getBluetoothLeScanner();
        search_handler = new Handler();

        device_adapter = new DeviceAdapter(new ArrayList<BluetoothDevice>());
        RecyclerView list_devices = (RecyclerView) findViewById(R.id.recycler_view_devices);
        list_devices.setAdapter(device_adapter);

        searchDevices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_morvi_finder, menu);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                searchDevices();
            }
        }
    }

    public void searchDevices(){
        if (bt_adapter == null || !bt_adapter.isEnabled()) {
            Intent enable_bt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enable_bt, REQUEST_ENABLE_BT);
            return;
        }

        if (!scanning) {
            search_handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progress_bt_find.setVisibility(View.INVISIBLE);
                    ble_scanner.startScan(ble_callback);
                    scanning = false;
                    Log.i("Bluetooth", "Scan finished");
                }
            }, SCAN_PERIOD);

            progress_bt_find.setVisibility(View.VISIBLE);
            ble_scanner.stopScan(ble_callback);
            scanning = true;
            Log.i("Bluetooth", "Scan started");
        }
    }
}
