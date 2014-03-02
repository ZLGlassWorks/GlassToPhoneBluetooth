package ktlab.lib.connection.bluetooth;

import java.nio.ByteOrder;

import ktlab.lib.connection.ConnectionCallback;
import android.os.Message;

public class ServerBluetoothConnection extends BluetoothConnection {

    public ServerBluetoothConnection(ConnectionCallback cb, boolean canQueueing) {
        super(cb, canQueueing);
    }

    public ServerBluetoothConnection(ConnectionCallback cb, boolean canQueueing, ByteOrder order) {
        super(cb, canQueueing, order);
    }

    @Override
    public void startConnection() {
        Message msg = obtainMessage(EVENT_CONNECT_COMPLETE);
        mConnectionThread = new ServerBluetoothConnectionThread(msg);
        mConnectionThread.start();
    }
}
