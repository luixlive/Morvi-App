package umg.ingciberneticasistemas.morvi.drivers;

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
import android.os.Handler;

/**
 * Created by luchavez on 18/03/2017.
 * BluetoothDriver: Driver para manejar lo relacionado a blouetooth, implementa parcelable para
 * poder enviar a traves de intents.
 */

public class BluetoothDriver {

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
     * listener: Instancia de BluetoothDriverListener suscrita a los eventos del driver.
     */
    private BluetoothDriverListener listener;

    /**
     * scan_time: Tiempo que dura la busqueda de dispositivos.
     */
    private int scan_time = 10000;


    /**
     * ble_scan_callback: Escucha eventos de busqueda de dispositivos.
     */
    private ScanCallback ble_scan_callback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            //Encontro un dispositivo
            BluetoothDevice device = result.getDevice();
            listener.newDeviceScanned(device);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

            listener.scanFailed();
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
                    listener.connectedToDevice(gatt);
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    listener.disconnectedFromDevice(gatt);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
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

    };


    /**
     * @param bt_manager manejador de configuracinoes de BT
     */
    public BluetoothDriver(BluetoothManager bt_manager){
        bt_adapter = bt_manager.getAdapter();
        ble_scanner = bt_adapter.getBluetoothLeScanner();
        search_handler = new Handler();
    }

    public void onBluetoothDriverListener(BluetoothDriverListener listener){
        this.listener = listener;
    }

    /**
     * scan: Inicia el escaneo
     */
    public void scan(){
        //Si no se puede acceder al modulo de bt
        if (bt_adapter == null || !bt_adapter.isEnabled()) {
            listener.bluetoothOff();
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
                    listener.scanFinished();
                }
            }, scan_time);

            ble_scanner.startScan(ble_scan_callback);
            scanning = true;
            listener.scanning();
        } else {
            listener.alreadyScanning();
        }
    }

    /**
     * stopScan: Detiene un escaneo que no ha concluido.
     */
    public void stopScan(){
        if(scanning) {
            ble_scanner.stopScan(ble_scan_callback);
            scanning = false;
            listener.scanFinished();
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
            listener.alreadyConnecting();
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
     * BluetoothDriverListener: Para escuchar los eventos generados por el driver.
     */
    public interface BluetoothDriverListener{
        void bluetoothOff();
        void newDeviceScanned(BluetoothDevice device);
        void scanning();
        void alreadyScanning();
        void scanFinished();
        void scanFailed();
        void alreadyConnecting();
        void connectedToDevice(BluetoothGatt ble_gatt);
        void disconnectedFromDevice(BluetoothGatt ble_gatt);
    }
}
