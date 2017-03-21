package umg.ingciberneticasistemas.morvi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        //Recupera el driver de bluetooth
        bt_driver = MorviApplication.getBluetoothDriver();

        //TODO NUEVO LISTENER PARA COSAS DEL DRIVER (EN LUGAR DE SOLO CAMBIAR EL LISTENER, CREAR
        // UNA INTERFAZ NUEVA PARA LOS EVENTOS QUE NOS INTERESAN ACA?
    }

}
