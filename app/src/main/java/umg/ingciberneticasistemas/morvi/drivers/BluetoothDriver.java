package umg.ingciberneticasistemas.morvi.drivers;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by luchavez on 18/03/2017.
 */

public class BluetoothDriver implements Parcelable {

    protected BluetoothDriver(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BluetoothDriver> CREATOR = new Creator<BluetoothDriver>() {
        @Override
        public BluetoothDriver createFromParcel(Parcel in) {
            return new BluetoothDriver(in);
        }

        @Override
        public BluetoothDriver[] newArray(int size) {
            return new BluetoothDriver[size];
        }
    };
}
