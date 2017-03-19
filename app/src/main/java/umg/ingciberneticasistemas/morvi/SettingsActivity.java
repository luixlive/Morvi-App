package umg.ingciberneticasistemas.morvi;

import android.app.Activity;
import android.os.Bundle;

import umg.ingciberneticasistemas.morvi.preferences.SettingsFragment;

/**
 * Created by luchavez on 18/03/2017.
 * SettingsActivity: Activity que abre el fragment de preferencias.
 */
public class SettingsActivity extends Activity {

    /**
     * KEY_PREF_SEARCH_TIME: Llave para identificar la preferencia de tiempo de busqueda.
     */
    public final static String KEY_PREF_SEARCH_TIME = "key_plpbst";

    /**
     * KEY_PREF_SEARCH_TIME_DEFAULT: Tiempo de busqueda por default.
     */
    public final static String KEY_PREF_SEARCH_TIME_DEFAULT = "10";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }
}