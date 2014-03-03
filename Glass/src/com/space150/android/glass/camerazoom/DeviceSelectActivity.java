package com.space150.android.glass.camerazoom;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class DeviceSelectActivity extends Activity {

	public static final String BLUETOOT_DEVICE = "bluetoot_device";

	private List<BluetoothDevice> mDevices;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		List<String> deviceNames = new ArrayList<String>();
		BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();

		if (defaultAdapter != null) {
			mDevices = new ArrayList<BluetoothDevice>(defaultAdapter.getBondedDevices());
			for (BluetoothDevice device : mDevices) {
				deviceNames.add(device.getName());
			}
		}

		if (deviceNames.size() == 0) {
			Toast.makeText(this, "Unable to locate and Bluetooth Devices", Toast.LENGTH_SHORT).show();
			finish();
		} else {
			openOptionsMenu();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int position = 0;
		for (BluetoothDevice device : mDevices) {
			menu.add(0, position, position, device.getName());
			position++;
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent();
		intent.putExtra(BLUETOOT_DEVICE, mDevices.get(item.getItemId()));
		setResult(Activity.RESULT_OK, intent);
		finish();
		return true;
	}

	public void onOptionsMenuClosed(Menu menu) {
		super.onOptionsMenuClosed(menu);
		setResult(Activity.RESULT_OK, null);
		finish();
	}
}
