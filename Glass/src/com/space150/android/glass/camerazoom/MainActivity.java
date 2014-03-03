package com.space150.android.glass.camerazoom;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import ktlab.lib.connection.ConnectionCallback;
import ktlab.lib.connection.ConnectionCommand;
import ktlab.lib.connection.bluetooth.ClientBluetoothConnection;
import ktlab.lib.connection.bluetooth.ServerBluetoothConnection;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements GestureDetector.OnGestureListener, Camera.OnZoomChangeListener {
	public static String TAG = "CameraZoom";

	private static final int DEVICE_SELECT_RESQUEST_CODE = 14123;

	public static float FULL_DISTANCE = 8000.0f;

	private static final byte TEST_TYPE = 3;
	private static final int TEST_ID = 234;

	private static final byte IMAGE_DATA = 42;
	private static final int IMAGE_DATA_COMMAND_ID = 10;

	private static final byte STRING_DATA = 43;
	private static final int STRING_DATA_COMMAND_ID = 11;

	private SurfaceView mPreview;
	private SurfaceHolder mPreviewHolder;
	private Camera mCamera;
	private boolean mInPreview = false;
	private boolean mCameraConfigured = false;
	private TextView mZoomLevelView;
	private TextView mStatusView;

	private GestureDetector mGestureDetector;

	private ClientBluetoothConnection mSenderConnection;
	private ServerBluetoothConnection mListenerConnection;

	private BluetoothDevice mDevice;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mPreview = (SurfaceView) findViewById(R.id.preview);
		mPreviewHolder = mPreview.getHolder();
		mPreviewHolder.addCallback(surfaceCallback);

		mZoomLevelView = (TextView) findViewById(R.id.zoomLevel);
		mStatusView = (TextView) findViewById(R.id.status);

		mGestureDetector = new GestureDetector(this, this);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// startBluetoothSender();
		// startBluetoothListener();
	}

	@Override
	public void onResume() {
		super.onResume();

		mCamera = Camera.open();
		configureCamera();
		startPreview();
	}

	@Override
	public void onPause() {
		if (mInPreview)
			mCamera.stopPreview();

		mCamera.release();
		mCamera = null;
		mCameraConfigured = false;
		mInPreview = false;

		super.onPause();
	}

	@Override
	protected void onStop() {
		if (mListenerConnection != null) {
			mListenerConnection.stopConnection();
		}
		if (mSenderConnection != null) {
			mSenderConnection.stopConnection();
		}

		super.onStop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == DEVICE_SELECT_RESQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK && data != null) {
				BluetoothDevice device = data.getParcelableExtra(DeviceSelectActivity.BLUETOOT_DEVICE);

				if (device != null) {
					mDevice = device;
					startBluetoothSender();
				}
			}
		}
	}

	private void initPreview(int width, int height) {
		if (mCamera != null && mPreviewHolder.getSurface() != null) {
			try {
				mCamera.setPreviewDisplay(mPreviewHolder);
			} catch (Throwable t) {
				Log.e(TAG, "Exception in initPreview()", t);
				Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
			}

			configureCamera();
		}
	}

	private void configureCamera() {
		if (!mCameraConfigured) {
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewSize(640, 360); // hard coded the largest
													// size for now
			parameters.setPreviewFpsRange(30000, 30000);
			mCamera.setParameters(parameters);
			mCamera.setZoomChangeListener(this);

			Size previewSize = mCamera.getParameters().getPreviewSize();
			int dataBufferSize = (int) (previewSize.height * previewSize.width * (ImageFormat.getBitsPerPixel(mCamera
					.getParameters().getPreviewFormat()) / 8.0));
			// int dataBufferSize = 262144;
			Log.i(TAG, "Size is: " + dataBufferSize);
			mCamera.addCallbackBuffer(new byte[dataBufferSize]);
			mCamera.addCallbackBuffer(new byte[dataBufferSize]);
			mCamera.addCallbackBuffer(new byte[dataBufferSize]);
			mCamera.addCallbackBuffer(new byte[dataBufferSize]);
			mCamera.addCallbackBuffer(new byte[dataBufferSize]);

			mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
				private long timestamp = 0;

				public synchronized void onPreviewFrame(byte[] data, Camera camera) {
					// Log.i(TAG, "Time Gap = " +
					// (System.currentTimeMillis() - timestamp));
					timestamp = System.currentTimeMillis();

					if (mSendImage) {
						Size previewSize = camera.getParameters().getPreviewSize();
						YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height,
								null);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, baos);

						Log.i(TAG, "Want to send image");
						mSendImage = false;
						sendImageData(baos.toByteArray());
					}

					camera.addCallbackBuffer(data);
					return;
				}
			});
			mCameraConfigured = true;
		}
	}

	private void startPreview() {
		if (mCameraConfigured && mCamera != null) {
			mCamera.startPreview();
			mInPreview = true;
		}
	}

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			// nothing
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			initPreview(width, height);
			startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// nothing
		}
	};

	private boolean mSendImage = false;

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		mGestureDetector.onTouchEvent(event);
		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		Camera.Parameters parameters = mCamera.getParameters();
		int zoom = parameters.getZoom();

		if (velocityX < 0.0f) {
			zoom -= 10;
			if (zoom < 0)
				zoom = 0;
		} else if (velocityX > 0.0f) {
			zoom += 10;
			if (zoom > parameters.getMaxZoom())
				zoom = parameters.getMaxZoom();
		}

		mCamera.startSmoothZoom(zoom);

		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		startActivityForResult(new Intent(this, DeviceSelectActivity.class), DEVICE_SELECT_RESQUEST_CODE);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// Log.d(TAG, "distanceX: " + distanceX + ", distanceY: " + distanceY);
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		Log.i(TAG, "Sending test packet");
		// mSenderConnection.sendData(TEST_TYPE, TEST_ID);
		// mSendImage = true;

		String stringToSend = "Hey there " + mDevice.getName();
		byte[] base64;
		try {
			base64 = Base64.encode(stringToSend.getBytes("UTF-8"), Base64.DEFAULT);
			mSenderConnection.sendData(STRING_DATA, base64, STRING_DATA_COMMAND_ID);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		return true;
	}

	@Override
	public void onZoomChange(int zoomValue, boolean stopped, Camera camera) {
		mZoomLevelView.setText("ZOOM: " + zoomValue);

	}

	// Bluetooth Helpers

	private void sendBitmap(Bitmap bitmap) {
		Log.i(TAG, "Sending bitmap data!!!");
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		sendImageData(stream.toByteArray());
	}

	private void sendImageData(byte[] data) {
		Log.i(TAG, "Sending image data!!!");
		mSenderConnection.sendData(IMAGE_DATA, data, IMAGE_DATA_COMMAND_ID);
	}

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
				Log.i(TAG, "Server#onDataSendComplete: " + id);
			}

			@Override
			public void onCommandReceived(ConnectionCommand command) {
				Log.i(TAG, "Server#onCommandReceived");

				switch (command.type) {
				case IMAGE_DATA:
					mBitmap = BitmapFactory.decodeByteArray(command.option, 0, command.optionLen);
					break;
				default:
					break;
				}
			}
		}, true);
	}

	private void startBluetoothSender() {
		if (mDevice == null) {
			mStatusView.setText("No Device found");
		} else {
			mSenderConnection = new ClientBluetoothConnection(new ConnectionCallback() {

				@Override
				public void onConnectComplete() {
					Log.i(TAG, "Client#onConnectComplete");
					mStatusView.setText("Connected to: '" + mDevice.getName() + "'");
				}

				@Override
				public void onConnectionFailed() {
					Log.i(TAG, "Client#onConnectionFailed");
					String name = mDevice.getName();
					mStatusView.setText("Failed to connected to: '" + name + "'");
				}

				@Override
				public void onDataSendComplete(int id) {
					Log.i(TAG, "Client#onDataSendComplete: " + id);
				}

				@Override
				public void onCommandReceived(ConnectionCommand command) {
					Log.i(TAG, "Client#onCommandReceived");
				}
			}, true, mDevice);
			mSenderConnection.startConnection();
		}
	}

	private BluetoothDevice getRemoteDevice() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

		for (BluetoothDevice bt : pairedDevices) {
			Log.i(TAG, "BT Device: " + bt.getName());
			if (bt.getName().toLowerCase().contains("nexus 5")) {
				return bt;
			}
		}
		return null;
	}
}