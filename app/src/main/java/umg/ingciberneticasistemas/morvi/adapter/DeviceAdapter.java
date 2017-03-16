package umg.ingciberneticasistemas.morvi.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by luchavez on 16/03/2017.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceHolder> {

    @Override
    public DeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(DeviceHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class DeviceHolder extends RecyclerView.ViewHolder{

        public DeviceHolder(View itemView) {
            super(itemView);
        }
    }

}
