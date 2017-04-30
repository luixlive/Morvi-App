package umg.ingciberneticasistemas.morvi;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.SurfaceView;
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



    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
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
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return inputFrame.rgba();
    }

    private void initCameraPreview(){
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.opencv_camera_preview_surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

}
