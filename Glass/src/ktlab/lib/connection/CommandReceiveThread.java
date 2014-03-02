package ktlab.lib.connection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.os.Message;

public class CommandReceiveThread extends Thread {

    private boolean forceStop = false;

    private final InputStream mInput;
    private Message mMessage;
    private ByteOrder mOrder;

    public CommandReceiveThread(InputStream in, Message msg, ByteOrder order) {
        mInput = in;
        mMessage = msg;
        mOrder = order;
    }

    public void run() {
        byte[] rawHeader = new byte[ConnectionCommand.HEADER_LENGTH];

        int receivedSize = 0;

        // receive header
        while (!forceStop && (receivedSize < ConnectionCommand.HEADER_LENGTH)) {
            int length = 0;
            try {
                length = mInput.read(rawHeader, receivedSize,
                        ConnectionCommand.HEADER_LENGTH - receivedSize);
            } catch (IOException e) {
                mMessage.what = Connection.EVENT_CONNECTION_FAIL;
                mMessage.sendToTarget();
                return;
            }
            if (length != -1) {
                receivedSize += length;
            }

            if (length == 0) {
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    // DO NOTHING
                }
            }
        }

        int optionLen = ByteBuffer.wrap(rawHeader).order(mOrder).getInt(1);

        byte[] rawOption = new byte[optionLen];
        receivedSize = 0;

        // receive option
        while (!forceStop && (receivedSize < optionLen)) {

            int length = 0;
            try {
                length = mInput.read(rawOption, receivedSize, optionLen
                        - receivedSize);
            } catch (IOException e) {
                mMessage.what = Connection.EVENT_CONNECTION_FAIL;
                mMessage.sendToTarget();
                return;
            }
            if (length != -1) {
                receivedSize += length;
            }
        }

        byte[] orderedOption = new byte[rawOption.length];
        ByteBuffer.wrap(rawOption).order(mOrder).get(orderedOption);

        ConnectionCommand command = ConnectionCommand.fromHeaderAndOption(
                rawHeader, rawOption, mOrder);
        mMessage.obj = command;
        mMessage.sendToTarget();
    }

    protected void forceStop() {
        forceStop = true;
    }
}
