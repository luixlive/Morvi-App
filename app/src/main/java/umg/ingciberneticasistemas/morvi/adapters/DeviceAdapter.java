package umg.ingciberneticasistemas.morvi.adapters;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import umg.ingciberneticasistemas.morvi.R;

/**
 * Created by luchavez on 16/03/2017.
 * DeviceAdapter: Adaptador para la lista de dispositivos encontrados por bluetooth. Un adaptador
 * se encarga de poblar una lista y los elementos de cada item de la lista, en este caso, pone
 * el nombre (o mac address) del dispositivo bt encontrado, un icono de bt o de bt connected y
 * una barra de carga.
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceHolder> {

    /**
     * NO_DEVICE_CONNECTING: Valor que indica que actualmente la barra de progreso no esta activa
     * en ningun dispositivo
     */
    private final static int NO_PROGRESS_BAR = -1;


    /**
     * devices: Dispositivos agregados a la lista.
     */
    private final List<BluetoothDevice> devices;

    /**
     * show_pb: Indica si hay que mostrar la barra de progreso de cada item.
     */
    private final List<Boolean> show_pb;

    /**
     * devices_existence: HashMap con key: mac address; value: posicion en la lista. Sirve para
     * encontrar rapidamente si un dispositivo ya fue encontrado y en que posicion esta.
     */
    private final HashMap<String, Integer> devices_existence;

    /**
     * activity_context: Contexto padre donde se ubica la lista.
     */
    private final AppCompatActivity activity_context;

    /**
     * listener: Instancia de OnDeviceLickListener a la que se comunican los eventos de click en
     * los dispositivos de la lista.
     */
    private OnDeviceClickListener listener;

    /**
     * progress_bar_position: Indica a que dispositivo se esta tratando de conectar actualmente o -1 si
     * no se esta tratando de conectar.
     */
    private int progress_bar_position = NO_PROGRESS_BAR;

    /**
     * DeviceAdapter
     * @param devices dispositivos a agregar desde un comienzo.
     * @param activity_context activity padre donde se ubica la lista.
     */
    public DeviceAdapter(LinkedList<BluetoothDevice> devices, AppCompatActivity activity_context){
        this.devices = devices;
        this.show_pb = new LinkedList<>();
        devices_existence = new HashMap<>();
        this.activity_context = activity_context;
    }

    @Override
    public DeviceAdapter.DeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Infla la vista de cada item
        View layout_device = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.device_view, parent, false);
        return new DeviceHolder(layout_device, activity_context);
    }

    @Override
    public void onBindViewHolder(DeviceHolder holder, int position) {
        //Si no hay nombre de dispositivo, se usa la mac address
        String name = devices.get(position).getName();
        if (name == null || name.isEmpty()){
            name = devices.get(position).getAddress();
        }

        //Se configuran los valores de este item
        holder.setDeviceName(name);
        holder.setBTIcon(false);
        holder.showPB(show_pb.get(position));
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

        //Si no existia el dispositivo en la lista, se agrega
        if (!devices_existence.containsKey(address)) {
            devices.add(device);
            show_pb.add(false);
            devices_existence.put(device.getAddress(), devices.size() - 1);
            notifyItemInserted(devices.size() - 1);
        }

        //Si ya existia, se checa si se actualizo el nombre
        else {
            int index = devices_existence.get(address);
            String old_name = devices.get(index).getName();
            if (old_name == null && device.getName() != null){
                devices.remove(index);
                show_pb.remove(index);
                devices.add(device);
                show_pb.add(false);
            }
        }
    }

    /**
     * showProgressBar: Muestra la barra de progreso del dispositivo en la posicion indicada.
     * @param position posicion del dispositivo
     */
    public void showProgressBar(int position){
        //Si trata de colocar una barra de progreso, pero actualmente esta en otro dispositivo, no
        //se hace nada
        if (progress_bar_position == NO_PROGRESS_BAR) {
            show_pb.set(position, true);
            notifyItemChanged(position);
            progress_bar_position = position;
        }
    }

    /**
     * hideProgressBar: Esconde la barra de progreso si se esta mostrando actualmente.
     */
    public void hideProgressBar(){
        if (progress_bar_position != NO_PROGRESS_BAR) {
            show_pb.set(progress_bar_position, false);
            notifyItemChanged(progress_bar_position);
            progress_bar_position = -1;
        }
    }

    /**
     * clearList: Limpia la lista por completo.
     */
    public void clearList(){
        devices.clear();
        show_pb.clear();
        devices_existence.clear();
        progress_bar_position = NO_PROGRESS_BAR;
        notifyDataSetChanged();
    }

    /**
     * getDeviceInPosition: Regresa el dispositivo de la posicion indicada en la lista.
     * @param position posicion del dispositivo
     * @return dispositivo bt
     */
    public BluetoothDevice getDeviceInPosition(int position){
        return devices.get(position);
    }

    /**
     * setOnDeviceClickListener: Suscribe una instancia de OnDeviceClickListener para escuchar los
     * eventos de click sobre los dispositivos.
     * @param listener instancia de OnDeviceClickListener
     */
    public void setOnDeviceClickListener(OnDeviceClickListener listener){
        this.listener = listener;
    }

    /**
     * OnDeviceClickListener: Interface que comunica a sus instancias cuando se hace un click sobre
     * un dispositivo.
     */
     public interface OnDeviceClickListener {
        void onClick(View v, int position);
    }

    /**
     * DeviceHolder: Holder para cada item de la lista.
     */
      class DeviceHolder extends RecyclerView.ViewHolder{

        /**
         * device_name: Nombre de este dispositivo.
         */
        private final TextView device_name;

        /**
         * bt_icon: Existen dos iconos por default en la carpeta drawable para indicar si se esta
         * conectado con este dispositivo.
         */
        private ImageView bt_icon;

        /**
         * pb: ProgressBar para indicar al usuario cuando se esta tratando de establecer conexion.
         */
        private final ProgressBar pb;

        /**
         * DeviceHolder
         * @param itemView vista del item
         * @param activity_context contexto para poder colocar la animacion
         */
        DeviceHolder(View itemView, final AppCompatActivity activity_context) {
            super(itemView);

            //Se obtienen los valores del view
            device_name = (TextView)itemView.findViewById(R.id.device_name);
            bt_icon = (ImageView)itemView.findViewById(R.id.ic_bluetooth_status);
            pb = (ProgressBar)itemView.findViewById(R.id.progress_loader);

            //Se coloca una animacion simple al hacer click y se avisa al listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(AnimationUtils.loadAnimation(activity_context,
                            R.anim.on_click_item));
                    listener.onClick(v, getAdapterPosition());
                }
            });
        }

        /**
         * setDeviceName: Actualiza el nombre del dispositivo de este item.
         * @param name nombre de dispositivo
         */
        void setDeviceName(String name){
            device_name.setText(name);
        }

        /**
         * setBTIcon: Actualiza el icono de conexion a bt con este dispositivo.
         * @param connected conectado ?
         */
        void setBTIcon(boolean connected){
            if(connected){
                bt_icon.setImageResource(R.drawable.ic_bluetooth_connected);
            } else{
                bt_icon.setImageResource(R.drawable.ic_bluetooth);
            }
        }

        /**
         * showPB: Muestra/oculta la ProgressBar.
         * @param show mostrar ?
         */
        void showPB(boolean show){
            if (show){
                pb.setVisibility(View.VISIBLE);
            } else {
                pb.setVisibility(View.INVISIBLE);
            }
        }
    }

}