package umg.ingciberneticasistemas.morvi.application;

import android.app.Application;
import android.bluetooth.BluetoothManager;

import umg.ingciberneticasistemas.morvi.drivers.BluetoothDriver;

/**
 * Created by luchavez on 21/03/2017.
 * MorviApplication: Clase que extiende de Application con el fin de poder utilizar el driver de
 * bluetooth por toda la aplicacion sin necesidad de estar enviando el objeto, que ademas se
 * complica porque los objetos necesarios no son serializables.
 */
public class MorviApplication extends Application {

    /**
     * bt_driver: Manejador de todos los procesos relacionados a bluetooth.
     */
    private static BluetoothDriver bt_driver = null;

    /**
     * initBluetoothDriver: Inicializa el bt_driver.
     * @param bt_manager instancia de BluetoothManager inicializada en el contexto
     * @return true si no se habia creado el driver antes, false de otro modo
     */
    public static boolean initBluetoothDriver(BluetoothManager bt_manager){
        if (bt_driver == null) {
            bt_driver = new BluetoothDriver(bt_manager);
            return true;
        }
        return false;
    }

    /**
     * getBluetoothDriver: Obtiene el driver de bluetooth global.
     * @return driver
     */
    public static BluetoothDriver getBluetoothDriver(){
        return bt_driver;
    }

}
