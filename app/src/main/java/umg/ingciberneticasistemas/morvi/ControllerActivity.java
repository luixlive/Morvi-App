package umg.ingciberneticasistemas.morvi;

import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import umg.ingciberneticasistemas.morvi.application.MorviApplication;
import umg.ingciberneticasistemas.morvi.drivers.BluetoothDriver;

/**
 * Created by luchavez on 18/03/2017.
 * ControllerActivity: Interfaz para el control de Morvi.
 */
public class ControllerActivity extends AppCompatActivity {

    /**
     * bt_driver: Manejador de todos los procesos relacionados a bluetooth.
     */
    private BluetoothDriver bt_driver;

    /**
     * bt_driver_listener: Manejo de eventos del driver del bt
     */
    private BluetoothDriver.BluetoothDriverCommunicationListener bt_driver_listener =
            new BluetoothDriver.BluetoothDriverCommunicationListener() {

        @Override
        public void disconnectedFromDevice(BluetoothGatt ble_gatt) {
            toastInUIThread(getString(R.string.toast_gatt_disconnected));
            finish();
            startActivity(new Intent(ControllerActivity.this, MorviFinderActivity.class));
        }

        @Override
        public void didntFindService() {

        }

        @Override
        public void didntFindCharacteristic() {

        }

        @Override
        public void characteristicWrote(char command) {

        }

        /**
         * showInUIThread: Mostrar dialogos Toast en el hiilo de UI.
         * @param message message to show
         */
        private void toastInUIThread(final String message){
            ControllerActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(ControllerActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        //Recupera el driver de bluetooth
        bt_driver = MorviApplication.getBluetoothDriver();
        bt_driver.onBluetoothDriverListener(bt_driver_listener);

        //TODO INICIAR LAS OPENCV PARA VER EN CAMARA Y ESCRIBIR DATOS POR BT (BOTONES PARA INICIAR Y PAUSAR EL PROCESO DE OPENCV, MORVI)
    }

}
