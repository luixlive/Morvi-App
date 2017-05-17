package umg.ingciberneticasistemas.morvi;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import umg.ingciberneticasistemas.morvi.application.MorviApplication;
import umg.ingciberneticasistemas.morvi.dialogs.SimpleDialog;
import umg.ingciberneticasistemas.morvi.drivers.BluetoothDriver;

import static android.Manifest.permission.CAMERA;
import static org.opencv.imgproc.Imgproc.CV_HOUGH_GRADIENT;
import static org.opencv.imgproc.Imgproc.HoughCircles;

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
     * can_write: Indica si se puede escribir caracteristicas a Morvi por bluetooth.
     */
    private boolean can_write = false;

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
            Log.i("BLEDRIVER", "No se encontro servicio");
        }

        @Override
        public void didntFindCharacteristic() {
            Log.i("BLEDRIVER", "No se encontro caracteristica");
        }

        @Override
        public void readyToWrite() {
            can_write = true;
        }

        @Override
        public void cantWrite() {
            can_write = false;
        }

        @Override
        public void characteristicWrote(char command) {
            Log.i("BLEDRIVER", "Caracteristica escrita");
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
        can_write = bt_driver.canWrite();
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
        Mat result = inputFrame.rgba();
        //Si esta en modo deteccion
        if (track){
            //Obtiene la imagen en blanco y negro
            Mat gray = new Mat();
            Imgproc.cvtColor(result, gray, Imgproc.COLOR_RGBA2GRAY);

            //OBtiene los circuilos con Hough
            Mat circles = new Mat();
            HoughCircles(gray, circles, CV_HOUGH_GRADIENT, 1, result.rows()/8, 200, 100, 10, 100);

            //Itera los circulos
            for (int i = 0; i < circles.cols(); i++)
            {
                double vCircle[] = circles.get(0,i);
                int center_x = (int)vCircle[0];
                int center_y = (int)vCircle[1];

                //Ubica los centros y checa sin son color morado TODO FALLOS AQUI
                Mat mat_point_center = result.submat(center_y, center_y+1, center_x, center_x+1);

                Mat rgb_center = new Mat();
                Mat hsv_center = new Mat();

                Imgproc.cvtColor(mat_point_center, rgb_center, Imgproc.COLOR_RGBA2RGB);
                Imgproc.cvtColor(rgb_center, hsv_center, Imgproc.COLOR_RGB2HSV);

                double hue_central_point = hsv_center.get(0,0)[0];
                if (hue_central_point > 133 && hue_central_point < 145){
                    //Si si es un circulo morado, rodea el circulo con una linea azul
                    Point center = new Point((int)vCircle[0], (int)vCircle[1]);
                    Imgproc.circle( result, center, (int)vCircle[2], new Scalar(0,0,255), 3, 8, 0 );
                    break;
                }
            }
        } else {
            result = inputFrame.rgba();
        }
        //Pausa el sistema por 500 ms
        SystemClock.sleep(500);
        return result;
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

    @Override
    public void onBackPressed(){
        //Cuando se pulse back, se destruye el historial de activities, al destruir la activitei
        //de MorviFinder, se llama el onDestroy que cierra el bt_driver y al iniciar el nuevo
        //intent se vuelve a crear de 0 para que vuelva a buscar dispositivos
        Intent intent = new Intent(ControllerActivity.this, MorviFinderActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
