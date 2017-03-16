package umg.ingciberneticasistemas.morvi.adapter;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import umg.ingciberneticasistemas.morvi.R;

/**
 * Created by luchavez on 16/03/2017.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceHolder> {

    private ArrayList<BluetoothDevice> devices;

    public DeviceAdapter(ArrayList<BluetoothDevice> devices){
        this.devices = devices;
    }

    @Override
    public DeviceAdapter.DeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout_device = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.device_view, parent, false);
        return new DeviceHolder(layout_device);
    }

    @Override
    public void onBindViewHolder(DeviceHolder holder, int position) {
        holder.setDeviceName(devices.get(position).getName());
        holder.setBTIcon(false);
        holder.showPB(true);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void addDevice(BluetoothDevice device){
        devices.add(device);
        notifyItemInserted(devices.size() - 1);
    }

    public static class DeviceHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        private TextView device_name;
        private ImageView bt_icon;
        private ProgressBar pb;

        public DeviceHolder(View itemView) {
            super(itemView);

            device_name = (TextView)itemView.findViewById(R.id.device_name);
            bt_icon = (ImageView)itemView.findViewById(R.id.ic_bluetooth_status);
            pb = (ProgressBar)itemView.findViewById(R.id.progress_loader);

            itemView.setOnClickListener(this);
        }

        public void setDeviceName(String name){
            device_name.setText(name);
        }

        public void setBTIcon(boolean connected){
            if(connected){
                bt_icon.setImageResource(R.drawable.ic_bluetooth_connected);
            } else{
                bt_icon.setImageResource(R.drawable.ic_bluetooth);
            }
        }

        public void showPB(boolean show){
            if (show){
                pb.setVisibility(View.VISIBLE);
            } else {
                pb.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            //CONECTAR A DISPOSITIVO
        }
    }

}
