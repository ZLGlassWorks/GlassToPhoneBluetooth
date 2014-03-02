package ktlab.lib.connection;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;

import android.os.Message;
import android.util.Log;

public class CommandSendThread extends Thread {

    private OutputStream mOut;
    private Message mMessage;
    private ConnectionCommand mCommand;
    private ByteOrder mOrder;

    public CommandSendThread(OutputStream out, ConnectionCommand command,
            Message msg, ByteOrder order) {
        mOut = out;
        mCommand = command;
        mMessage = msg;
        mOrder = order;
    }

    @Override
    public void run() {
        try {
        	Log.i("KTLAB", "CommandSendThread#run command type: " + mCommand.type);
            mOut.write(ConnectionCommand.toByteArray(mCommand, mOrder));
        } catch (IOException e) {
        	Log.e("KTLAB", "CommandSendThread#run fail: " + e.getMessage());
            mMessage.what = Connection.EVENT_CONNECTION_FAIL;
        }

        mMessage.sendToTarget();
    }
}
