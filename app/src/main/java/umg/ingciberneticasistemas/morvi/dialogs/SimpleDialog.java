package umg.ingciberneticasistemas.morvi.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import umg.ingciberneticasistemas.morvi.R;

/**
 * Created by luchavez on 16/03/2017.
 * SimpleDialog: Fragment de Android para mostrar dialogos con un titulo y un mensaje, y la
 * posibilidad de aceptar/cancelar.
 */

public class SimpleDialog extends DialogFragment {

    /**
     * MESSAGE_KEY: Llave para identificar el parametro de mensaje.
     */
    private static final String MESSAGE_KEY = "m";

    /**
     * TITLE_KEY: Llave para identificar el parametro de titulo.
     */
    private static final String TITLE_KEY = "t";

    /**
     * callback: instancia de la interface que comunica los eventos del dialogo.
     */
    private UserSelectionCallback callback;

    /**
     * newDialog: Regresa una instancia de SimpleDialog con los parametros indicados.
     * @param title titulo del dialogo.
     * @param message mensaje del dialogo.
     * @return instancia del dialogo.
     */
    public static SimpleDialog newDialog(String title, String message) {
        SimpleDialog dialog = new SimpleDialog();

        //Configura los parametros a enviar al fragment del dialogo
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putString(MESSAGE_KEY, message);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Captura los parametros por su llave
        String title = getArguments().getString(TITLE_KEY);
        String message = getArguments().getString(MESSAGE_KEY);

        //Crea un dialogo AlertDialog para aprovechar su simpleza
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.simple_dialog_accept),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (callback != null) {
                                    //El usuario acepto, se envia el evento
                                    callback.accepted(getTag());
                                }
                            }
                        })
                .setNegativeButton(getString(R.string.simple_dialog_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (callback != null) {
                                    //El usuario rechazo, se envia el evento
                                    callback.canceled(getTag());
                                }
                            }
                        })
                .create();
    }

    /**
     * setCallback: Suscribe una instancia de la interface UserSelectionCallback para escuchar
     * los eventos del dialogo.
     * @param callback instancia de UserSelectionCallback.
     */
    public void setCallback(UserSelectionCallback callback){
        this.callback = callback;
    }

    /**
     * UserSelectionCallback: Interface para comunicar los eventos del dialogo
     */
    public interface UserSelectionCallback {
        void accepted(String tag);
        void canceled(String tag);
    }

}
