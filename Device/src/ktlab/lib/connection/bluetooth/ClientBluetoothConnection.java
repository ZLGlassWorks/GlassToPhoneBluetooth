package ktlab.lib.connection.bluetooth;

import java.nio.ByteOrder;

import ktlab.lib.connection.ConnectionCallback;
import android.bluetooth.BluetoothDevice;
import android.os.Message;

public class ClientBluetoothConnection extends BluetoothConnection {

    private final BluetoothDevice mDevice;

    /**
     * create Bluetooth socket
     */
    public ClientBluetoothConnection(ConnectionCallback callback,
            boolean canQueueing, BluetoothDevice device) {
        super(callback, canQueueing);
        mDevice = device;
    }

    public ClientBluetoothConnection(ConnectionCallback callback,
            boolean canQueueing, ByteOrder order, BluetoothDevice device) {
        super(callback, canQueueing, order);
        mDevice = device;
    }

    public void startConnection() {
        Message msg = obtainMessage(EVENT_CONNECT_COMPLETE);
        mConnectionThread = new ClientBluetoothConnectionThread(mDevice, msg);
        mConnectionThread.start();
    }
}
