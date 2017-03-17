package umg.ingciberneticasistemas.morvi;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import umg.ingciberneticasistemas.morvi.Dialog.SimpleDialog;
import umg.ingciberneticasistemas.morvi.adapter.DeviceAdapter;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MorviFinderActivity extends AppCompatActivity {

    public final static int REQUEST_ENABLE_BT = 100;
    public final static int PERMISSION_LOCATION_ID = 200;
    private static final long SCAN_PERIOD = 20000;
    private static final String REQUEST_LOCATION_DIALOG_TAG = "rldt";

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

        RecyclerView list_devices = (RecyclerView) findViewById(R.id.recycler_view_devices);

        device_adapter = new DeviceAdapter(new ArrayList<BluetoothDevice>());
        list_devices.setAdapter(device_adapter);

        LinearLayoutManager layout_manager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        list_devices.setLayoutManager(layout_manager);

        DividerItemDecoration divider = new DividerItemDecoration(this,
                layout_manager.getOrientation());
        list_devices.addItemDecoration(divider);

        searchDevices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_morvi_finder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION_ID: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    searchDevices();
                }
            }
        }
    }

    public void searchDevices(){
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                SimpleDialog dialog = SimpleDialog.newDialog(
                        getString(R.string.permission_dialog_title),
                        getString(R.string.permission_dialog_location)
                );
                dialog.setCallback(new SimpleDialog.UserSelectionCallback() {
                    @Override
                    public void accepted(String tag) {
                        Log.i("PermissionDialog", "Requesting permission");
                        ActivityCompat.requestPermissions(MorviFinderActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_LOCATION_ID);
                    }

                    @Override
                    public void canceled(String tag) {
                    }
                });
                dialog.show(getFragmentManager(), REQUEST_LOCATION_DIALOG_TAG);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        PERMISSION_LOCATION_ID);
            }
            return;
        }

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
                    ble_scanner.stopScan(ble_callback);
                    scanning = false;
                    Log.i("Bluetooth", "Scan finished");
                }
            }, SCAN_PERIOD);

            progress_bt_find.setVisibility(View.VISIBLE);
            ble_scanner.startScan(ble_callback);
            scanning = true;
            Log.i("Bluetooth", "Scan started");
        }
    }
}
