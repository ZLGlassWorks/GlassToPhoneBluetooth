package ktlab.lib.connection.bluetooth;

import java.io.IOException;

import android.bluetooth.BluetoothDevice;
import android.os.Message;

public class ClientBluetoothConnectionThread extends BluetoothConnectionThread {

    protected final BluetoothDevice mDevice;

    public ClientBluetoothConnectionThread(BluetoothDevice device, Message msg) {
        super(msg);
        mDevice = device;
    }

    @Override
    protected void getSocket() {

        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(BluetoothConnection.SERVICE_UUID);
        } catch (IOException e) {
            mSocket = null;
            return;
        }

        int count = 0;
        do {
            try {
                if (mSocket != null) {
                    mSocket.connect();
                }
                break;
            } catch (IOException e) {
                // DO NOTHING
            }
            // retry
        } while (count++ < 5);
    }
}
