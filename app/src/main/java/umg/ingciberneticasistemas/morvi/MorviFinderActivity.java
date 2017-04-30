package umg.ingciberneticasistemas.morvi;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.LinkedList;

import umg.ingciberneticasistemas.morvi.application.MorviApplication;
import umg.ingciberneticasistemas.morvi.dialogs.SimpleDialog;
import umg.ingciberneticasistemas.morvi.adapters.DeviceAdapter;
import umg.ingciberneticasistemas.morvi.drivers.BluetoothDriver;

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
    private final static int REQUEST_ENABLE_BT = 100;

    /**
     * PERMISSION_LOCATION_ID: Id para el permiso de ubicacion.
     */
    private final static int PERMISSION_LOCATION_ID = 200;

    /**
     * REQUEST_LOCATION_DIALOG_TAG: Tag que se envia al dialogo para solicitar ubicacion al usuario.
     */
    private static final String REQUEST_LOCATION_DIALOG_TAG = "rldt";


    /**
     * device_adapter: Adaptador para la lista RecyclerVIew que muestra los dispositivos
     * encontrados.
     */
    private DeviceAdapter device_adapter;

    /**
     * bt_driver: Manejador de todos los procesos relacionados a bluetooth.
     */
    private BluetoothDriver bt_driver;


    /**
     * progress_bt_find: Barra de progreso que se muestra durante la busqueda.
     */
    private ProgressBar progress_bt_find;

    /**
     * app_bar: Barra superior de la aplicacion. Es expandible.
     */
    private AppBarLayout app_bar;

    /**
     * position_clicked: Dispositivo clickeado, se almacena para cambiar su estado de conexion
     */
    private int position_clicked;


    /**
     * bt_driver_listener: Manejo de eventos del driver de bt.
     */
    private BluetoothDriver.BluetoothDriverConnectionListener bt_driver_listener =
            new BluetoothDriver.BluetoothDriverConnectionListener() {

        private final static char CLEAR_LIST = 0;
        private final static char HIDE_PROGRESS_BAR = 1;
        private final static char CHANGE_CONNECTION = 2;

        @Override
        public void bluetoothOff() {
            //No se puede acceder al bt, se pide al usuario que lo encienda
            Intent enable_bt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enable_bt, REQUEST_ENABLE_BT);
        }

        @Override
        public void newDeviceScanned(BluetoothDevice device) {
            device_adapter.addDevice(device);
        }

        @Override
        public void scanning() {
            device_adapter.clearList();
            progress_bt_find.setVisibility(View.VISIBLE);
            Toast.makeText(MorviFinderActivity.this, getString(R.string.toast_searching_devices1),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void alreadyScanning() {
            Toast.makeText(MorviFinderActivity.this, getString(R.string.toast_searching_devices2),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void scanFinished() {
            progress_bt_find.setVisibility(View.INVISIBLE);
        }

        @Override
        public void scanFailed() {
            Toast.makeText(MorviFinderActivity.this, getString(R.string.toast_scan_failed),
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void alreadyConnecting() {
            Toast.makeText(MorviFinderActivity.this, getString(R.string.toast_connect_wait),
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void connectedToDevice(BluetoothGatt ble_gatt) {
            listActionstInUIThread(CHANGE_CONNECTION);
        }

        @Override
        public void servicesDiscoveredDevice() {
            //Se conecto a un dispositivo, se borra la lista y se inicia la nueva activity
            toastInUIThread(getString(R.string.toast_gatt_connected));
            listActionstInUIThread(CLEAR_LIST);

            startActivity(new Intent(MorviFinderActivity.this, ControllerActivity.class));
        }

                /**
         * showInUIThread: Mostrar dialogos Toast en el hiilo de UI.
         * @param message message to show
         */
        private void toastInUIThread(final String message){
            MorviFinderActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MorviFinderActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * listActionstInUIThread: Ejecuta acciones sobre la lista en el hilo de UI.
         */
        private void listActionstInUIThread(final char selection){
            MorviFinderActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    if (selection == CLEAR_LIST) {
                        device_adapter.clearList();
                    } else if (selection == HIDE_PROGRESS_BAR){
                        device_adapter.hideProgressBar();
                    } else if (selection == CHANGE_CONNECTION){
                        device_adapter.setDeviceConnected(position_clicked);
                    }
                }
            });
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morvi_finder);

        //Se inician comaponentes graficos (barra superior, boton, barra de progreso, lista)
        initViews();

        //Inicializa el manejador de bt
        BluetoothManager bt_manager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        MorviApplication.initBluetoothDriver(bt_manager);
        bt_driver = MorviApplication.getBluetoothDriver();
        bt_driver.onBluetoothDriverListener(bt_driver_listener);

        //Se comienza la busqueda
        searchDevices();
    }

    @Override
    public void onResume(){
        //Cada que se resuma la actividad se checa si cambiaron las preferencias
        SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(this);
        int bt_search_time = Integer.valueOf(shared_pref.getString(
                SettingsActivity.KEY_PREF_SEARCH_TIME,
                SettingsActivity.KEY_PREF_SEARCH_TIME_DEFAULT));

        bt_driver.changeScanTime(bt_search_time);

        super.onResume();
    }

    @Override
    public void onDestroy(){
        bt_driver.close();

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
            startActivity(new Intent(this, SettingsActivity.class));
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

    private void initViews(){
        //Colocamos la barra superior
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        //Obtenemos los componentes de la barra de progreso y de la barra expandible
        app_bar = (AppBarLayout)findViewById(R.id.app_bar_morvi_finder);
        progress_bt_find = (ProgressBar)findViewById(R.id.progress_bt_find);

        //Se obtiene la lista para dispositivos
        RecyclerView list_devices = (RecyclerView)findViewById(R.id.recycler_view_devices);

        //Se indica el adaptador
        device_adapter = new DeviceAdapter(new LinkedList<BluetoothDevice>(), this);
        device_adapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onClick(View v, int position) {
                //Si pulsan un dispositivo se contrae la barra superior
                app_bar.setExpanded(false);
                //Se inicia la conexion GATT
                bt_driver.stopScan();
                device_adapter.showProgressBar(position);
                BluetoothDevice device = device_adapter.getDeviceInPosition(position);
                bt_driver.connectGATT(device, MorviFinderActivity.this);
                position_clicked = position;
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
    }

    /**
     * searchDevices: Busqueda de dispositivos. Para poder hacer la busqueda, se requiere que el
     * bt este encendido y que el usuario otorgue permiso de ubicacion (estandar de Google por
     * seguridad al usuario).
     */
    private void searchDevices(){
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

        bt_driver.scan();
    }

    /**
     * fabClicked: Evento disparado cuando se pulsa el FAB de busqueda de dispositivos.
     * @param v vista del FAB
     */
    public void fabClicked(View v){
        searchDevices();
    }
}
