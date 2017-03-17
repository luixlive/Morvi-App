package umg.ingciberneticasistemas.morvi.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import umg.ingciberneticasistemas.morvi.R;

/**
 * SimpleDialog: Fragment de Android para mostrar dialogos con un titulo y un mensaje, y la
 * posibilidad de aceptar/cancelar.
 * Created by luchavez on 16/03/2017.
 */

public class SimpleDialog extends DialogFragment {

    private static final String MESSAGE_KEY = "m";
    private static final String TITLE_KEY = "t";

    private UserSelectionCallback callback;

    public static SimpleDialog newDialog(String title, String message) {
        SimpleDialog dialog = new SimpleDialog();
        Bundle args = new Bundle();

        args.putString(TITLE_KEY, title);
        args.putString(MESSAGE_KEY, message);

        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(TITLE_KEY);
        String message = getArguments().getString(MESSAGE_KEY);

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.simple_dialog_accept),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (callback != null) {
                                    callback.accepted(getTag());
                                }
                            }
                        })
                .setNegativeButton(getString(R.string.simple_dialog_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (callback != null) {
                                    callback.canceled(getTag());
                                }
                            }
                        })
                .create();
    }

    public void setCallback(UserSelectionCallback callback){
        this.callback = callback;
    }

    public interface UserSelectionCallback {
        void accepted(String tag);
        void canceled(String tag);
    }

}
