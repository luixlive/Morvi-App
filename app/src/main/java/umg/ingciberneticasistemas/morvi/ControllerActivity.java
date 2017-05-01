package umg.ingciberneticasistemas.morvi;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import umg.ingciberneticasistemas.morvi.application.MorviApplication;
import umg.ingciberneticasistemas.morvi.dialogs.SimpleDialog;
import umg.ingciberneticasistemas.morvi.drivers.BluetoothDriver;

import static android.Manifest.permission.CAMERA;

/**
 * Created by luchavez on 18/03/2017.
 * ControllerActivity: Interfaz para el control de Morvi.
 */
public class ControllerActivity extends AppCompatActivity implements
        CameraBridgeViewBase.CvCameraViewListener2{

    /**
     * PERMISSION_CAMERA_ID: Id para el permiso de ubicacion.
     */
    private final static int PERMISSION_CAMERA_ID = 300;

    /**
     * REQUEST_LOCATION_DIALOG_TAG: Tag que se envia al dialogo para solicitar ubicacion al usuario.
     */
    private static final String REQUEST_CAMERA_DIALOG_TAG = "rcdt";

    /**
     * bt_driver: Manejador de todos los procesos relacionados a bluetooth.
     */
    private BluetoothDriver bt_driver;

    /**
     * camera_preview: Area del layout donde se muestra el preview de la camara.
     */
    private CameraBridgeViewBase camera_preview;

    /**
     * track: Indica si el algoritmo de rastreo esta activado o no.
     */
    private boolean track = false;

    /**
     * preview_loader_listener: Manejador para eventos de la carga del area del preview.
     */
    private BaseLoaderCallback preview_loader_listener = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    camera_preview.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

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

        camera_preview = (CameraBridgeViewBase) findViewById(R.id.opencv_camera_preview_surface);
        camera_preview.setVisibility(SurfaceView.VISIBLE);
        camera_preview.setCvCameraViewListener(this);

        //Recupera el driver de bluetooth
        bt_driver = MorviApplication.getBluetoothDriver();
        bt_driver.onBluetoothDriverListener(bt_driver_listener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CAMERA_ID: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //El usuario dio permiso de acceso a la ubicacion, se inicia la busqueda
                    initCameraPreview();
                } else {
                    finish();
                }
            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (camera_preview != null)
            camera_preview.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, CAMERA) !=
                PackageManager.PERMISSION_GRANTED){
            //No hay permiso de camara, se solicita al usuario
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA)) {
                SimpleDialog dialog = SimpleDialog.newDialog(
                        getString(R.string.permission_dialog_title),
                        getString(R.string.permission_dialog_camera)
                );
                dialog.setCallback(new SimpleDialog.UserSelectionCallback() {
                    @Override
                    public void accepted(String tag) {
                        ActivityCompat.requestPermissions(ControllerActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                PERMISSION_CAMERA_ID);
                    }

                    @Override
                    public void canceled(String tag) {
                    }
                });
                dialog.show(getFragmentManager(), REQUEST_CAMERA_DIALOG_TAG);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSION_CAMERA_ID);
            }
        } else {
            initCameraPreview();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera_preview != null)
            camera_preview.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (track){
            //TODO ENCONTRAR CIRCULO FIUSHA?
        }
        return inputFrame.rgba();
    }

    /**
     * initCameraPreview: Comienza el preview de la camara.
     */
    private void initCameraPreview(){
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this,
                    preview_loader_listener);
        } else {
            preview_loader_listener.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    /**
     * fabClicked: Evento disparado cuando se pulsa el FAB de inicio de rastreo.
     * @param v vista del FAB
     */
    public void fabClicked(View v){
        if (track) {
            ((FloatingActionButton) v).setImageResource(android.R.drawable.ic_media_play);
        } else{
            ((FloatingActionButton) v).setImageResource(android.R.drawable.ic_media_pause);
        }
        track = !track;
    }

}
