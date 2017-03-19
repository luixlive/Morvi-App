package umg.ingciberneticasistemas.morvi.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import umg.ingciberneticasistemas.morvi.R;

/**
 * Created by luchavez on 18/03/2017.
 * SettingsFragment: Fragment para abrir preferencias de la aplicacion.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
    }

}
