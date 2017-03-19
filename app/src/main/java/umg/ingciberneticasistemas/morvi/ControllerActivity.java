package umg.ingciberneticasistemas.morvi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import umg.ingciberneticasistemas.morvi.drivers.BluetoothDriver;

/**
 * Created by luchavez on 18/03/2017.
 * ControllerActivity: Interfaz para el control de Morvi.
 */
public class ControllerActivity extends AppCompatActivity {

    private BluetoothDriver bt_driver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if (!bundle.isEmpty()){
            bt_driver = (BluetoothDriver) bundle.
                    getSerializable(MorviFinderActivity.KEY_BUNDLE_BT_DRIVER);
        } else{
            finish();
        }
    }

}
