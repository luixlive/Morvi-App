package umg.ingciberneticasistemas.morvi.drivers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Created by luchavez on 18/03/2017.
 * BluetoothDriver: Driver para manejar lo relacionado a blouetooth, implementa parcelable para
 * poder enviar a traves de intents.
 */

public class BluetoothDriver {

    /**
     * MORVI_UUID_CHAR: Constante para ubicar la caracteristica de Morvi donde escribir los
     * comandos.
     */
    private static final String MORVI_UUID_CHAR = "0000ffe1-0000-1000-8000-00805f9b34fb";

    /**
     * MORVI_UUID_SERV: Constante para ubicar el servicio de Morvi que contiene la caracteristica
     * de comunicacion
     */
    private static final String MORVI_UUID_SERV = "0000ffe0-0000-1000-8000-00805f9b34fb";


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
     * scanning: Bandera que indica sei actualmente se estan buscando dispositivos.
     */
    private boolean scanning = false;

    /**
     * scanning: Bandera que indica sei actualmente se esta conectando a un dispositivo.
     */
    private boolean connecting = false;

    /**
     * conn_listener: Suscripcion a los eventos de conexion del driver.
     */
    private BluetoothDriverConnectionListener conn_listener;

    /**
     * comm_listener: Suscripcion a los eventos de comunicacion del driver.
     */
    private BluetoothDriverCommunicationListener comm_listener;

    /**
     * scan_time: Tiempo que dura la busqueda de dispositivos.
     */
    private int scan_time = 10000;

    /**
     * can_write: Indica si es posible escribir a Morvi en cada instante.
     */
    private boolean can_write = false;


    /**
     * ble_scan_callback: Escucha eventos de busqueda de dispositivos.
     */
    private ScanCallback ble_scan_callback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            //Encontro un dispositivo
            BluetoothDevice device = result.getDevice();
            conn_listener.newDeviceScanned(device);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

            conn_listener.scanFailed();
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

            connecting = false;

            //Checa cual es el nuevo estado y notifica al usuario
            switch(newState){
                case BluetoothGatt.STATE_CONNECTED:
                    conn_listener.connectedToDevice(gatt);
                    ble_gatt.discoverServices();
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    comm_listener.disconnectedFromDevice(gatt);
                    can_write = false;
                    comm_listener.cantWrite();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            conn_listener.servicesDiscoveredDevice();

            //Lee los servicios del BLE
            List<BluetoothGattService> dev_services = ble_gatt.getServices();
            for (BluetoothGattService service : dev_services) {
                //Si encuentra el servicio de Morvi, busca la característica
                if (service.getUuid().toString().equals(MORVI_UUID_SERV)) {
                    List<BluetoothGattCharacteristic> serv_characteristics =
                            service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : serv_characteristics) {
                        if (characteristic.getUuid().toString().equals(MORVI_UUID_CHAR)) {
                            can_write = true;
                            comm_listener.readyToWrite();
                        }
                    }
                }
            }
        }

    };

    /**
     * @param bt_manager manejador de configuracinoes de BT
     */
    public BluetoothDriver(BluetoothManager bt_manager){
        bt_adapter = bt_manager.getAdapter();
        ble_scanner = bt_adapter.getBluetoothLeScanner();
        search_handler = new Handler();
    }

    /**
     * onBluetoothDriverListener: Suscribirse a lso eventos de conexion
     * @param listener BluetoothDriverConnectionListener a suscribir
     */
    public void onBluetoothDriverListener(BluetoothDriverConnectionListener listener){
        conn_listener = listener;
    }

    /**
     * onBluetoothDriverListener: Suscribirse a los eventos de comunicacion
     * @param listener BluetoothDriverCommunicationListener a suscribir
     */
    public void onBluetoothDriverListener(BluetoothDriverCommunicationListener listener){
        comm_listener = listener;
    }

    /**
     * scan: Inicia el escaneo
     */
    public void scan(){
        //Si no se puede acceder al modulo de bt
        if (bt_adapter == null || !bt_adapter.isEnabled()) {
            conn_listener.bluetoothOff();
            return;
        }

        if (!scanning) {
            //Si no se esta buscando ya, se comienza a buscar
            search_handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Despues de SCAN_PERIOD segundos, se detiene la busqueda
                    ble_scanner.stopScan(ble_scan_callback);
                    scanning = false;
                    conn_listener.scanFinished();
                }
            }, scan_time);

            ble_scanner.startScan(ble_scan_callback);
            scanning = true;
            conn_listener.scanning();
        } else {
            conn_listener.alreadyScanning();
        }
    }

    /**
     * stopScan: Detiene un escaneo que no ha concluido.
     */
    public void stopScan(){
        if(scanning) {
            ble_scanner.stopScan(ble_scan_callback);
            scanning = false;
            conn_listener.scanFinished();
        }
    }

    /**
     * connectGATT: Intenta hacer una conexion tipo GATT con el dispositivo indicado.
     * @param device dispositivo
     * @param context contexto actual de la aplicacion
     */
    public void connectGATT(BluetoothDevice device, Context context){
        if (!connecting) {
            ble_gatt = device.connectGatt(context, false, ble_gatt_callback);
        } else {
            conn_listener.alreadyConnecting();
        }
    }

    /**
     * changeScanTime: Cambia el tiempo de escaneo
     * @param scan_time tiempo en segundos
     */
    public void changeScanTime(int scan_time){
        this.scan_time = scan_time * 1000;
    }

    /**
     * write: Escribe en la caracteristica de Morvi el comando indicado.
     * @param command comando segun el protocolo
     */
    public void write(char command){
        if (can_write) {
            BluetoothGattService service = ble_gatt.getService(UUID.fromString(MORVI_UUID_SERV));
            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(
                        UUID.fromString(MORVI_UUID_CHAR));
                characteristic.setValue(command,
                        android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                if (ble_gatt.writeCharacteristic(characteristic)) {
                    comm_listener.characteristicWrote(command);
                } else{
                    comm_listener.didntFindCharacteristic();
                }
            } else {
                comm_listener.didntFindService();
            }
        }
    }

    /**
     * canWrite: Indica si es posible escribir en este momento a Morvi.
     * @return true si se puede escribir, false de otro modo.
     */
    public boolean canWrite(){
        return can_write;
    }

    /**
     * close: Cierra los componentes del bt.
     */
    public void close(){
        if (scanning){
            ble_scanner.stopScan(ble_scan_callback);
        }

        if (ble_gatt != null) {
            ble_gatt.close();
            ble_gatt = null;
        }
    }

    /**
     * BluetoothDriverConnectionListener: Para escuchar los eventos generados por el driver.
     */
    public interface BluetoothDriverConnectionListener{
        void bluetoothOff();
        void newDeviceScanned(BluetoothDevice device);
        void scanning();
        void alreadyScanning();
        void scanFinished();
        void scanFailed();
        void alreadyConnecting();
        void connectedToDevice(BluetoothGatt ble_gatt);
        void servicesDiscoveredDevice();
    }

    /**
     * BluetoothDriverListener: Para escuchar los eventos generados por el driver.
     */
    public interface BluetoothDriverCommunicationListener{
        void disconnectedFromDevice(BluetoothGatt ble_gatt);
        void didntFindService();
        void didntFindCharacteristic();
        void readyToWrite();
        void cantWrite();
        void characteristicWrote(char command);
    }
}
