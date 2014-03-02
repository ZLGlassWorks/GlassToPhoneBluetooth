package ktlab.lib.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Message;

public abstract class ConnectionThread extends Thread {

    protected InputStream mInput;
    protected OutputStream mOutput;

    protected Message mMessage;

    protected ConnectionThread(Message msg) {
        mMessage = msg;
    }

    public InputStream getInputStream() {
        return mInput;
    }

    public OutputStream getOutputStream() {
        return mOutput;
    }

    public boolean close() {

        boolean ret = true;

        try {
            if (mInput != null) {
                mInput.close();
            }
        } catch (IOException e) {
            ret = false;
        }

        try {
            if (mOutput != null) {
                mOutput.close();
            }
        } catch (IOException e) {
            ret = false;
        }

        mInput = null;
        mOutput = null;

        return ret;
    }
}
