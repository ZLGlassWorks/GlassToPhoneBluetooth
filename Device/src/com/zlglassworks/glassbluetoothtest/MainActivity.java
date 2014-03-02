package com.zlglassworks.glassbluetoothtest;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import ktlab.lib.connection.ConnectionCallback;
import ktlab.lib.connection.ConnectionCommand;
import ktlab.lib.connection.bluetooth.BluetoothConnection;
import ktlab.lib.connection.bluetooth.ClientBluetoothConnection;
import ktlab.lib.connection.bluetooth.ServerBluetoothConnection;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = "GlassBluetoothRemoteDevice";

	private BluetoothDevice mDevice;
	private BluetoothConnection mSenderConnection;
	private BluetoothConnection mListenerConnection;

	private Button mButton;
	private TextView mTextView;
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
		mButton = (Button) findViewById(R.id.button);
		mImageView = (ImageView) findViewById(R.id.image_view);

		mButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(TAG, "sending data");
				mSenderConnection.sendData(COMMAND_TEST, COMMAND_ID_1);
			}
		});

	}

	@Override
	protected void onStart() {
		super.onStart();

//		startBluetoothSender();
		startBluetoothListener();
	}

	@Override
	protected void onStop() {
//		mSenderConnection.stopConnection();
		mListenerConnection.stopConnection();

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
		mDevice = getGlassDevice();

		if (mDevice == null) {
			mTextView.setText("No Glass found");
		} else {
			mSenderConnection = new ClientBluetoothConnection(new ConnectionCallback() {

				@Override
				public void onConnectComplete() {
					Log.i(TAG, "Client#onConnectComplete");
					mTextView.setText("Connected to: '" + mDevice.getName() + "'");
					mButton.setEnabled(true);
				}

				@Override
				public void onConnectionFailed() {
					Log.i(TAG, "Client#onConnectionFailed");
					String name = mDevice.getName();
					mTextView.setText("Failed to connect to: '" + name + "'");
					mButton.setEnabled(false);
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

	private BluetoothDevice getGlassDevice() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

		for (BluetoothDevice bt : pairedDevices) {
			if (bt.getName().toLowerCase().contains("glass")) {
				return bt;
			}
		}
		return null;
	}
}
