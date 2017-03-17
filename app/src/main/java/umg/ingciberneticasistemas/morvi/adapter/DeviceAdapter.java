package umg.ingciberneticasistemas.morvi.adapter;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import umg.ingciberneticasistemas.morvi.R;

/**
 * DeviceAdapter: Adaptador para la lista de dispositivos encontrados por bluetooth.
 * Created by luchavez on 16/03/2017.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceHolder> {

    private List<BluetoothDevice> devices;
    private HashMap<String, Integer> devices_existence;

    public DeviceAdapter(ArrayList<BluetoothDevice> devices){
        this.devices = devices;
        devices_existence = new HashMap<>();
    }

    @Override
    public DeviceAdapter.DeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout_device = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.device_view, parent, false);
        return new DeviceHolder(layout_device);
    }

    @Override
    public void onBindViewHolder(DeviceHolder holder, int position) {
        String name = devices.get(position).getName();
        if (name == null || name.isEmpty()){
            name = devices.get(position).getAddress();
        }
        holder.setDeviceName(name);
        holder.setBTIcon(false);
        holder.showPB(false);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    /**
     * addDevice: Agregar un dispositivo a la lista. Si ya se encuentra en ella, se checa si
     * el anterior tiene nombre nulo, y el nuevo tiene un nombre valido, entonces se saca el
     * anterior y se introduce el nuevo a la lista.
     * @param device dispositivo a agregar.
     */
    public void addDevice(BluetoothDevice device){
        String address = device.getAddress();
        if (!devices_existence.containsKey(address)) {
            devices.add(device);
            devices_existence.put(device.getAddress(), devices.size() - 1);
            Log.i("Adapter", "added: " + device);
            notifyItemInserted(devices.size() - 1);
        } else {
            int index = devices_existence.get(address);
            String old_name = devices.get(index).getName();
            if (old_name == null && device.getName() != null){
                devices.remove(index);
                devices.add(device);
            }
        }
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
