package ghostysoft.bleuuidexplorer;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends ListActivity {
    private final static String TAG = DeviceScanActivity.class.getSimpleName();

    // [ghosty
    class deviceInfo {
        public String Name;
        public String Address;
        public Integer RSSI;
        public byte[] scanRecord;
    }

    // ghosty]
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 5000;

    //ghosty
    private deviceInfo[] scanDevice=new deviceInfo[100]; //Support Max 100 Bluetooth devices
    private Integer scanIndex=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "*** onCreate()");
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.title_activity_device_scan);
        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "*** onCreateOptionsMenu()");
        getMenuInflater().inflate(R.menu.device_scan, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "*** onOptionsItemSelected()");
        switch (item.getItemId()) {
            case R.id.menu_scan:
                scanIndex = 0; //reset
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "*** onResume()");
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "*** onActivityResult()");
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "*** onPause()");
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(TAG, String.format("*** onListItemClick() position %d",position));
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        final Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
    }

    private void scanLeDevice(final boolean enable) {
        Log.d(TAG, "*** scanLeDevice()");
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    StringBuilder dumpHex(byte data[]) {
        Log.d(TAG, "dumpHex()");
        final StringBuilder strResult = new StringBuilder();
        for (int i=0; i<data.length; i++)
            strResult.append(String.format("%02X ", data[i]));
        return strResult;
    }

    StringBuilder dumpHex(byte data[], int length) {
        Log.d(TAG, String.format("dumpHex() length=%d",length));
        final StringBuilder strResult = new StringBuilder();
        for (int i=0; i<length; i++)
            strResult.append(String.format("%02X ", data[i]));
        return strResult;
    }

    StringBuilder dumpHex(byte data[], int start, int length) {
        Log.d(TAG, String.format("dumpHex() start=%d, length=%d",start,length));
        final StringBuilder strResult = new StringBuilder();
        for (int i=start; i<start+length; i++)
            strResult.append(String.format("%02X ", data[i]));
        return strResult;
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            Log.d(TAG, "*** LeDeviceListAdapter.LeDeviceListAdapter()");
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            Log.d(TAG, "*** LeDeviceListAdapter.addDevice()");
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            Log.d(TAG, "*** LeDeviceListAdapter.getDevice()");
            return mLeDevices.get(position);
        }

        public void clear() {
            Log.d(TAG, "*** LeDeviceListAdapter.clear()");
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            Log.d(TAG, "*** LeDeviceListAdapter.getCount()");
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            Log.d(TAG, "*** LeDeviceListAdapter.getItem()");
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            Log.d(TAG, "*** LeDeviceListAdapter.getItemId()");
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Log.d(TAG, String.format("*** LeDeviceListAdapter.getView() i=%d",i));
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.device_scan, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                viewHolder.deviceRSSI = (TextView) view.findViewById(R.id.scan_RSSI);
                viewHolder.deviceScanResult = (TextView) view.findViewById(R.id.scan_result);
                viewHolder.listEIRs = (TextView) view.findViewById(R.id.list_EIRs);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            /* [ghosty
            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();

            final String deviceName=scanDevice[i].Name;
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);

            viewHolder.deviceAddress.setText(device.getAddress());
            ghosty]*/
            viewHolder.deviceName.setText(scanDevice[i].Name);
            viewHolder.deviceAddress.setText(scanDevice[i].Address);
            viewHolder.deviceRSSI.setText(String.format("%d dBm",scanDevice[i].RSSI));

            // Show the scan result
            final StringBuilder strScanResult = new StringBuilder(dumpHex(scanDevice[i].scanRecord));
            Log.d(TAG, String.format("scan result %s", strScanResult.toString()));
            viewHolder.deviceScanResult.setText(strScanResult);

            // Explain the scan  result, see BLUETOOTH SPECIFICATION Version 4.0 [Vol 3]
            // Part C. Generic Access Profile. Section 18 -  APPENDIX C
            final StringBuilder strEIR = new StringBuilder();
            int p=0;

            while (p<scanDevice[i].scanRecord.length) {
                byte fieldLength = scanDevice[i].scanRecord[p];
                byte fieldType =  scanDevice[i].scanRecord[p+1];
                Log.d(TAG, String.format(" scan field ptr=%d, length=%d, type = 0x%02X", p, fieldLength, fieldType));
                switch (fieldType) {
                    case (byte)0x01: //Flags
                        byte flags = scanDevice[i].scanRecord[p+2];
                        strEIR.append(String.format("[0x01] Flags: 0x%02X\n",flags));
                        if ((flags & 0x01)>0) {
                            strEIR.append("    b0:LE Limited Discoverable Mode\n");
                        }
                        if ((flags & 0x02)>0) {
                            strEIR.append("     b1:LE General Discoverable Mode\n");
                        }
                        if ((flags & 0x04)>0) {
                            strEIR.append("     b2:BR/EDR Not Supported\n");
                        }
                        if ((flags & 0x08)>0) {
                            strEIR.append("     b3:Controller\n");
                        }
                        if ((flags & 0x10)>0) {
                            strEIR.append("     b4:Host\n");
                        }
                        if ((flags & 0x20)>0) {
                            strEIR.append("     b5:Reserved\n");
                        }
                        if ((flags & 0x40)>0) {
                            strEIR.append("     b6:Reserved\n");
                        }
                        if ((flags & 0x80)>0) {
                            strEIR.append("     b7:Reserved\n");
                        }
                        break;

                    case (byte)0x02:
                        strEIR.append("[0x02] More 16-bit UUIDs: 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-1)).append("\n");
                        break;

                    case (byte)0x03:
                        strEIR.append("[0x03] Complete 16-bit UUIDs: 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-1)).append("\n");
                        break;

                    case (byte)0x04:
                        strEIR.append("[0x04] More 32-bit UUIDs: 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-1)).append("\n");
                        break;

                    case (byte)0x05:
                        strEIR.append("[0x05] Complete 32-bit UUIDs: 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-1)).append("\n");
                        break;

                    case (byte)0x06:
                        strEIR.append("[0x06] More 128-bit UUIDs: 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-1)).append("\n");
                        break;

                    case (byte)0x07:
                        strEIR.append("[0x07] Complete 128-bit UUIDs: 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-1)).append("\n");
                        break;

                    case (byte)0x08:
                        strEIR.append("[0x08] Shortened local name: 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-1)).append("\n");
                        break;

                    case (byte)0x09:
                        strEIR.append("[0x09] Complete local name: 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-1)).append("\n");
                        break;

                    case (byte)0x0A:
                        byte txPower = scanDevice[i].scanRecord[p+2];
                        strEIR.append("[0x0A] Tx Power Level: 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-1)).append("\n");
                        break;

                    case (byte)0x0D:
                        strEIR.append("[0x0D] Device Class: 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-1)).append("\n");
                        break;

                    case (byte)0x0E:
                        strEIR.append("[0x0E] Simple Pairing Hash: 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-1)).append("\n");
                        break;

                    case (byte)0x0F:
                        strEIR.append("[0x0F] Simple Pairin Randomizer: 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-1));
                        break;

                    case (byte)0x10:
                        strEIR.append("[0x10] Security Manager TK Value: 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-1)).append("\n");
                        break;

                    case (byte)0x11: //Security Flags
                        byte secFlags = scanDevice[i].scanRecord[p+2];
                        strEIR.append(String.format("[0x11] Security Manager OOB Flags [0x%02X]\n",secFlags));
                        if ((secFlags & 0x01)>0) {
                            strEIR.append("     b0:OOB data present\n");
                        }
                        if ((secFlags & 0x02)>0) {
                            strEIR.append("     b1:LE supported\n");
                        }
                        if ((secFlags & 0x04)>0) {
                            strEIR.append("     b2:Host\n");
                        }
                        if ((secFlags & 0x08)>0) {
                            strEIR.append("     b3:Random address\n");
                        } else {
                            strEIR.append("     b3:Public address\n");
                        }
                        break;

                    case (byte)0x12:
                        strEIR.append("[0x12] Slave Connection Interval Range: 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-1)).append("\n");
                        break;

                    case(byte)0x14:
                        strEIR.append("[0x14] Service solication 16-bit UUIDs: 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-1)).append("\n");
                        break;

                    case (byte)0x15:
                        strEIR.append("[0x15] Service solication 128-bit UUIDs: 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-1)).append("\n");
                        break;

                    case (byte)0x16:
                        strEIR.append("[0x16] Service data : 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-2)).append("\n");
                        break;

                    case (byte)0xff:
                        strEIR.append("[0xFF] Manufacturer Specific Data: 0x").append(dumpHex(scanDevice[i].scanRecord,p+2,fieldLength-1)).append("\n");
                        int adCompanyID = (scanDevice[i].scanRecord[p+2]&0xFF) | ((scanDevice[i].scanRecord[p+3]&0xFF)<<8);
                        strEIR.append(String.format("     Company ID: 0x%04X (%s)\n", adCompanyID, CompanyIDs.lookup(adCompanyID,"Unknown")));
                        if (fieldLength>3) { //more data
                            byte adDataType =   scanDevice[i].scanRecord[p+4];
                            strEIR.append(String.format("     Data Type: 0x%02X\n", adDataType));
                            switch (adDataType) {
                                case 2: //iBeacon
                                    byte adDataLen = scanDevice[i].scanRecord[p+5];
                                    //strEIR.append(String.format("     Data Length: %02X\n", adDataLen));
                                    if (adDataLen==0x15) {
                                        switch (adCompanyID) {
                                            case 0x4C: strEIR.append("     [i-Beacon]\n "); break;
                                            case 0x59: strEIR.append("     [nRF-Beacon]\n "); break;
                                              default: strEIR.append("     [Beacon]\n "); break;
                                        }

                                        strEIR.append("          UUID: 0x").append(dumpHex(scanDevice[i].scanRecord, p + 6, 8)).append("\n"); //分2行列印
                                        strEIR.append("                        ").append(dumpHex(scanDevice[i].scanRecord, p + 6+8, 8)).append("\n");
                                        int adMajor = ((scanDevice[i].scanRecord[p + 22]&0xFF)<<8 | (scanDevice[i].scanRecord[p + 23]&0xFF));
                                        int adMinor = ((scanDevice[i].scanRecord[p + 24]&0xFF)<<8 | (scanDevice[i].scanRecord[p + 25]&0xFF));
                                        int adCalcPower = scanDevice[i].scanRecord[p + 26];
                                        strEIR.append(String.format("          Major: %d\n",adMajor));
                                        strEIR.append(String.format("          Minor: %d\n",adMinor));
                                        strEIR.append(String.format("          Calibration Power: %d dBm\n ", adCalcPower));
                                    } else {
                                        strEIR.append(String.format("     ? Invalid data length for iBeacon: 0x%02X\n", adDataLen));
                                    }
                                    break;
                                default :
                                    strEIR.append(String.format("     ? Unrecognized type: 0x%02X\n", adDataType));
                                    break;
                            }
                        }
                        break;

                    default:
                        strEIR.append(String.format("[0x%02X] Unrecognized EIR type: 0x",fieldType)).append(dumpHex(scanDevice[i].scanRecord,p,fieldLength-1)).append("\n");
                        break;
                } //switch (fieldType)
                p += (fieldLength+1);
            } //while
            Log.d(TAG, "scan field finished");
            viewHolder.listEIRs.setText(strEIR);
            return view;
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.d(TAG,  String.format("*** BluetoothAdapter.LeScanCallback.onLeScan(): *RSSI=%d",rssi));

                    // device.fetchUuidsWithSdp(); //ghosty, improve stability ?

                    final String deviceName = device.getName();
                    scanDevice[scanIndex]= new deviceInfo();
                    if (deviceName != null && deviceName.length() > 0) {
                        scanDevice[scanIndex].Name = deviceName;
                    }  else {
                        scanDevice[scanIndex].Name = "unknown device";
                    }
                    scanDevice[scanIndex].Address = device.getAddress();
                    scanDevice[scanIndex].RSSI = rssi;

                    // Search for actual packet length
                    int packetLength=0;
                    while (scanRecord[packetLength]>0 && packetLength<scanRecord.length) {
                        packetLength += scanRecord[packetLength]+1;
                    }
                    scanDevice[scanIndex].scanRecord = new byte[packetLength];
                    System.arraycopy (scanRecord,0,scanDevice[scanIndex].scanRecord,0,packetLength);
                    Log.d(TAG, String.format("*** Scan Index=%d, Name=%s, Address=%s, RSSI=%d, scan result length=%d",
                            scanIndex, scanDevice[scanIndex].Name,scanDevice[scanIndex].Address,scanDevice[scanIndex].RSSI, packetLength ));
                    scanIndex++;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "*** BluetoothAdapter.LeScanCallback.runOnUiThread()");
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }

            };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRSSI;
        TextView deviceScanResult;        
        TextView listEIRs;
    }
}