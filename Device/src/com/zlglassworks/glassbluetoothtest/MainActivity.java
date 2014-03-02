package com.zlglassworks.glassbluetoothtest;

import java.io.UnsupportedEncodingException;

import ktlab.lib.connection.ConnectionCallback;
import ktlab.lib.connection.ConnectionCommand;
import ktlab.lib.connection.bluetooth.BluetoothConnection;
import ktlab.lib.connection.bluetooth.ClientBluetoothConnection;
import ktlab.lib.connection.bluetooth.ServerBluetoothConnection;
import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zlglassworks.glassbluetoothtest.DeviceSelectDialogFragment.BluetoothDevcieSelectListener;

public class MainActivity extends FragmentActivity implements BluetoothDevcieSelectListener {

	private static final String TAG = "GlassBluetoothRemoteDevice";
	
	private static final String DEVICE_SAVED_STATE = "device";

	private BluetoothDevice mDevice;
	private BluetoothConnection mSenderConnection;
	private BluetoothConnection mListenerConnection;

	private TextView mTextView;
	private Button mSendTestButton;
	private Button mConnectButton;
	private ImageView mImageView;


	public static final byte COMMAND_TEST = 1;

	public static final int COMMAND_ID_1 = 1;
	public static final int COMMAND_ID_2 = 1;

	private static final byte IMAGE_DATA = 42;
	private static final byte STRING_DATA = 43;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		mTextView = (TextView) findViewById(R.id.text_view);
		mSendTestButton = (Button) findViewById(R.id.send_test_button);
		mConnectButton = (Button) findViewById(R.id.connect_to_bluetooth_button);
		mImageView = (ImageView) findViewById(R.id.image_view);

		mSendTestButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(TAG, "sending data");
				mSenderConnection.sendData(COMMAND_TEST, COMMAND_ID_1);
			}
		});
		
		mConnectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(TAG, "showing selection dialog");
				showSelectionDialog();
			}
		});

		if (savedInstanceState != null) {
			mDevice = savedInstanceState.getParcelable(DEVICE_SAVED_STATE);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		startBluetoothListener();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if (mDevice != null) {
			outState.putParcelable(DEVICE_SAVED_STATE, mDevice);
		}
	}
	
	@Override
	protected void onStop() {
		if (mSenderConnection != null) {
			mSenderConnection.stopConnection();
		}
		if (mListenerConnection != null) {
			mListenerConnection.stopConnection();
		}

		super.onStop();
	}

	// Helpers

	private void startBluetoothListener() {
		mListenerConnection = new ServerBluetoothConnection(new ConnectionCallback() {

			private Bitmap mBitmap;

			@Override
			public void onConnectComplete() {
				Log.i(TAG, "Server#onConnectComplete");
			}

			@Override
			public void onConnectionFailed() {
				Log.i(TAG, "Server#onConnectionFailed");
			}

			@Override
			public void onDataSendComplete(int id) {
				Log.i(TAG, "Server#onDataSendComplete");
			}

			@Override
			public void onCommandReceived(ConnectionCommand command) {
				Log.i(TAG, "Server#onCommandReceived");

				switch (command.type) {
				case IMAGE_DATA:
					mBitmap = BitmapFactory.decodeByteArray(command.option, 0, command.optionLen);
					mTextView.setText("IMAGE_DATA (" + command.type + ") :: " + command.optionLen + " :: " + command.option.length);
					mImageView.setImageBitmap(mBitmap);
					break;
				case STRING_DATA:
					try {
						String stringSent = new String(Base64.decode(command.option, Base64.DEFAULT), "UTF-8");
						mTextView.setText("STRING_DATA : " + stringSent);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					break;
				default:
					mTextView.setText("Received data of type: " + command.type);
					break;
				}
			}
		}, true);
		mListenerConnection.startConnection();
	}

	private void startBluetoothSender() {
		if (mDevice == null) {
			mTextView.setText("No Device");
		} else {
			mSenderConnection = new ClientBluetoothConnection(new ConnectionCallback() {

				@Override
				public void onConnectComplete() {
					Log.i(TAG, "Client#onConnectComplete");
					mTextView.setText("Connected to: '" + mDevice.getName() + "'");
					mSendTestButton.setEnabled(true);
				}

				@Override
				public void onConnectionFailed() {
					Log.i(TAG, "Client#onConnectionFailed");
					String name = mDevice.getName();
					mTextView.setText("Failed to connect to: '" + name + "'");
					mSendTestButton.setEnabled(false);
					mDevice = null;
				}

				@Override
				public void onDataSendComplete(int id) {
					Log.i(TAG, "Client#onDataSendComplete");
				}

				@Override
				public void onCommandReceived(ConnectionCommand command) {
					Log.i(TAG, "Client#onCommandReceived");
				}
			}, true, mDevice);
			mSenderConnection.startConnection();
		}
	}

	// Device Selection
	
	private void showSelectionDialog() {
		DeviceSelectDialogFragment newFragment = new DeviceSelectDialogFragment();
        newFragment.show(getFragmentManager(), "dialog");
    }
	
	//
	
	@Override
	public void onDeviceSelected(BluetoothDevice device) {
		mDevice = device;
		if (mSenderConnection != null) {
			mSenderConnection.stopConnection();
		}
		startBluetoothSender();
	}

	@Override
	public void noBluetoothDevices() {
		mDevice = null;
		Toast.makeText(this, "No Bluetooth Devices", Toast.LENGTH_SHORT).show();
	}
}
