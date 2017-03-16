package umg.ingciberneticasistemas.morvi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by luchavez on 16/03/2017.
 * SplashScreenActivity: Splash Activity para mostrar logotipo de morvi y permitir la carga
 * correcta de la activity MorviFinderActivity.
 */
public class SplashScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MorviFinderActivity.class);
        startActivity(intent);
        finish();
    }

}
