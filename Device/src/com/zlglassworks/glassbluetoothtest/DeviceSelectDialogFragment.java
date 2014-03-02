package com.zlglassworks.glassbluetoothtest;

import java.util.ArrayList;
import java.util.List;

import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class DeviceSelectDialogFragment extends DialogFragment implements OnItemClickListener {

	private ListView mDeviceListView;
	private List<BluetoothDevice> mDevices;

	public interface BluetoothDevcieSelectListener {
		public void onDeviceSelected(BluetoothDevice device);

		public void noBluetoothDevices();
	}

	public DeviceSelectDialogFragment() {
		// empty constructor required
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();

		if (defaultAdapter != null) {
			mDevices = new ArrayList<BluetoothDevice>(defaultAdapter.getBondedDevices());
		}

		if (mDevices.size() == 0) {
			BluetoothDevcieSelectListener activity = (BluetoothDevcieSelectListener) getActivity();
			activity.noBluetoothDevices();
			this.dismiss();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_dialog_device_list, container);
		mDeviceListView = (ListView) view.findViewById(R.id.device_list);

		List<String> deviceNames = new ArrayList<String>();
		for (BluetoothDevice device : mDevices) {
			deviceNames.add(device.getName());
		}

		ListAdapter adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
				android.R.id.text1, deviceNames);
		mDeviceListView.setAdapter(adapter);
		mDeviceListView.setOnItemClickListener(this);

		getDialog().setTitle("Select Bluetooth Device");

		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		BluetoothDevcieSelectListener activity = (BluetoothDevcieSelectListener) getActivity();
		activity.onDeviceSelected(mDevices.get(position));
		this.dismiss();
	}
}
