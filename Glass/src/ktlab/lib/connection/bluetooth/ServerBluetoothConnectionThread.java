package ktlab.lib.connection.bluetooth;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.os.Message;
import android.util.Log;

public class ServerBluetoothConnectionThread extends BluetoothConnectionThread {

    private static final String TAG = "BluetoothConnection";

    private static final String SERVICE_NAME = "KTLab_Library";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket mServerSocket;

    public ServerBluetoothConnectionThread(Message msg) {
        super(msg);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    protected void getSocket() {

        // create server socket
        try {
            mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME,
                    BluetoothConnection.SERVICE_UUID);
        } catch (IOException e) {
            Log.e(TAG, "failed to get server socekt", e);
            return;
        }

        // get socket
        try {
            mSocket = mServerSocket.accept();
        } catch (IOException e) {
            Log.e(TAG, "failed to get Bluetooth socket");
            mSocket = null;
        }

        // close server socket
        try {
            mServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "failed to close ServerSocket");
        }
    }

    @Override
    public boolean close(){
        if (mServerSocket != null){
            try {
                mServerSocket.close();
            } catch (IOException e) {
            }
        }
        return super.close();
    }
}
