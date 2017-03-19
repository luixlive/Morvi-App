package umg.ingciberneticasistemas.morvi;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import umg.ingciberneticasistemas.morvi.dialogs.SimpleDialog;
import umg.ingciberneticasistemas.morvi.adapters.DeviceAdapter;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by luchavez on 15/03/2017.
 * MorviFinderActivity: Activity para establecer comunicacion con el controlador de Morvi. Muestra
 * una lista de dispositivos bluetooth cercanos usando la tecnologia BLE.
 */
public class MorviFinderActivity extends AppCompatActivity {

    /**
     * REQUEST_ENABLE_BT: Id para la solicitud al usuario para que active el bt.
     */
    public final static int REQUEST_ENABLE_BT = 100;

    /**
     * PERMISSION_LOCATION_ID: Id para el permiso de ubicacion.
     */
    public final static int PERMISSION_LOCATION_ID = 200;

    /**
     * SCAN_PERIOD: Tiempo que busca dispositivos cercanos cada vez que se solicita.
     */
    private static final long SCAN_PERIOD = 20000;

    /**
     * REQUEST_LOCATION_DIALOG_TAG: Tag que se envia al dialogo para solicitar ubicacion al usuario.
     */
    private static final String REQUEST_LOCATION_DIALOG_TAG = "rldt";


    /**
     * bt_adapter: Adaptador para uso de ble.
     */
    private BluetoothAdapter bt_adapter;

    /**
     * ble_scanner: Objeto que busca dispositivos cercanos por medio de ble.
     */
    private BluetoothLeScanner ble_scanner;

    /**
     * ble_gatt: Driver para la conexion a GATT server (BLE).
     */
    private BluetoothGatt ble_gatt;

    /**
     * search_handler: Hilo en segundo plano para pausar la busqueda cuando pase el tiempo indicado.
     */
    private Handler search_handler;

    /**
     * device_adapter: Adaptador para la lista RecyclerVIew que muestra los dispositivos
     * encontrados.
     */
    private DeviceAdapter device_adapter;

    /**
     * scanning: Bandera que indica sei actualmente se estan buscando dispositivos.
     */
    private boolean scanning = false;


    /**
     * progress_bt_find: Barra de progreso que se muestra durante la busqueda.
     */
    private ProgressBar progress_bt_find;


    /**
     * ble_scan_callback: Escucha eventos de busqueda de dispositivos.
     */
    private ScanCallback ble_scan_callback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            //Encontro un dispositivo y este se agrega a la lista
            BluetoothDevice device = result.getDevice();
            device_adapter.addDevice(device);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

            Toast.makeText(MorviFinderActivity.this, getString(R.string.toast_scan_failed),
                    Toast.LENGTH_LONG).show();
        }
    };

    /**
     * ble_gatt_callback: Callback para manejar conexiones GATT de ble.
     */
    private final BluetoothGattCallback ble_gatt_callback = new BluetoothGattCallback() {

        //TODO remover los callback innecesarios

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            String message = "";

            //Checa cual es el nuevo estado y notifica al usuario
            switch(newState){
                case BluetoothGatt.STATE_CONNECTING:
                    message = getString(R.string.toast_gatt_connecting);
                    break;
                case BluetoothGatt.STATE_CONNECTED:
                    message = getString(R.string.toast_gatt_connected);
                    break;
                case BluetoothGatt.STATE_DISCONNECTING:
                    message = getString(R.string.toast_gatt_disconnecting);
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    message = getString(R.string.toast_gatt_disconnected);
            }

            showInUIThread(message);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }

        private void showInUIThread(final String message){
            MorviFinderActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MorviFinderActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morvi_finder);

        //Se inician comaponentes graficos (barra superior, boton, barra de progreso, lista)

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progress_bt_find = (ProgressBar)findViewById(R.id.progress_bt_find);

        //Se obtienen instancias de los drivers de bluetooth
        BluetoothManager bt_manager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bt_adapter = bt_manager.getAdapter();
        ble_scanner = bt_adapter.getBluetoothLeScanner();
        search_handler = new Handler();

        //La lista es tipo RecyclerView
        RecyclerView list_devices = (RecyclerView)findViewById(R.id.recycler_view_devices);

        //Se indica el adaptador
        device_adapter = new DeviceAdapter(new LinkedList<BluetoothDevice>(), this);
        device_adapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onClick(View v, int position) {
                //Si pulsan un dispositivo se inicia la conexion GATT
                BluetoothDevice device = device_adapter.getDeviceInPosition(position);
                Log.i("MORVI", "CLICK A " + device.getAddress());
                ble_gatt = device.connectGatt(MorviFinderActivity.this, false, ble_gatt_callback);
            }
        });
        list_devices.setAdapter(device_adapter);

        //Driver de layout
        LinearLayoutManager layout_manager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        list_devices.setLayoutManager(layout_manager);

        //Division entre dispositivos
        DividerItemDecoration divider = new DividerItemDecoration(this,
                layout_manager.getOrientation());
        list_devices.addItemDecoration(divider);

        //Se comienza la busqueda
        searchDevices();
    }

    @Override
    public void onDestroy(){
        if (scanning){
            ble_scanner.stopScan(ble_scan_callback);
        }

        if (ble_gatt != null) {
            ble_gatt.close();
            ble_gatt = null;
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Se infla el menu superior
        getMenuInflater().inflate(R.menu.menu_morvi_finder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Se pulso un boton del menu
        int id = item.getItemId();
        //TODO MENU
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                //El usuario encendio el bt, se inicia la busqueda
                searchDevices();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION_ID: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //El usuario dio permiso de acceso a la ubicacion, se inicia la busqueda
                    searchDevices();
                }
            }
        }
    }

    /**
     * searchDevices: Busqueda de dispositivos. Para poder hacer la busqueda, se requiere que el
     * bt este encendido y que el usuario otorgue permiso de ubicacion (estandar de Google por
     * seguridad al usuario).
     */
    public void searchDevices(){
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED){
            //No hay permiso de ubicacion, se solicita al usuario
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                SimpleDialog dialog = SimpleDialog.newDialog(
                        getString(R.string.permission_dialog_title),
                        getString(R.string.permission_dialog_location)
                );
                dialog.setCallback(new SimpleDialog.UserSelectionCallback() {
                    @Override
                    public void accepted(String tag) {
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
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_LOCATION_ID);
            }
            return;
        }

        if (bt_adapter == null || !bt_adapter.isEnabled()) {
            //No se puede acceder al bt, se pide al usuario que lo encienda
            Intent enable_bt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enable_bt, REQUEST_ENABLE_BT);
            return;
        }

        if (!scanning) {
            //Si no se esta buscando ya, se comienza a buscar
            search_handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Despues de SCAN_PERIOD segundos, se detiene la busqueda
                    scan(false);
                }
            }, SCAN_PERIOD);

            scan(true);
        }
    }

    /**
     * scan: Hace las acciones correspondientes para escanear/dejar de escanear dispositivos.
     * @param scan escanear ?
     */
    private void scan(boolean scan){
        //Si se quiere escanear, se muestra la barra de progreso, se bloquea el boton y comienza
        //a buscar. Si se desea detener, hace justo lo contrario
        if (scan){
            progress_bt_find.setVisibility(View.VISIBLE);
            ble_scanner.startScan(ble_scan_callback);
            scanning = true;
            Toast.makeText(MorviFinderActivity.this,
                    getString(R.string.toast_searching_devices1), Toast.LENGTH_SHORT).show();
        } else {
            progress_bt_find.setVisibility(View.INVISIBLE);
            ble_scanner.stopScan(ble_scan_callback);
            scanning = false;
        }
    }

    /**
     * fabClicked: Evento disparado cuando se pulsa el FAB de busqueda de dispositivos.
     * @param v vista del FAB
     */
    public void fabClicked(View v){
        //Inicia busqueda si esta no esta en ejecucion ya y muestra un texto indicador
        if (!scanning){
            device_adapter.clearList();
            searchDevices();
        } else {
            Toast.makeText(MorviFinderActivity.this,
                    getString(R.string.toast_searching_devices2), Toast.LENGTH_SHORT).show();
        }
    }
}
