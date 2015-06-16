package ghostysoft.bleuuidexplorer;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
//import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public static BluetoothGattCharacteristic mTargetCharacteristic; //Common variable for Read/Write operation
    public static BluetoothLeService mBluetoothLeService; //Common variable for Read/Write operation
    public static String mDeviceName;   //Common variable for Read/Write operation
    public static String mDeviceAddress; //Common variable for Read/Write operation
    public static boolean mConnected = false;  //Common variable for Read/Write operation

    private TextView mConnectionState;
    private TextView mRSSI;
    private TextView mDataField;
    private ExpandableListView mGattServicesList;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private final String LIST_PROPERTIES = "PROPERTIES";
    private final String LIST_PERMISSION = "PERMISSION";

    AlertDialog NordicSelectDialog;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothLeService.ACTION_GATT_CONNECTED)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (action.equals(BluetoothLeService.ACTION_GATT_DISCONNECTED)) {
                mConnected = false;
                mTargetCharacteristic = null;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (action.equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (action.equals(BluetoothLeService.ACTION_DATA_AVAILABLE)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            } else if (action.equals(BluetoothLeService.RSSI_DATA_AVAILABLE)) {
                displayRSSI(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
             Log.d(TAG, "BroadcastReceiver.onReceive():action="+action);
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {

                        Log.d(TAG, String.format("*** ExpandableListView.OnChildClickListener group=%d, child=%d, id=%d", groupPosition, childPosition, id));

                        mDataField.setText("not received"); //clear
                        mTargetCharacteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
                      //  final int charaProp = characteristic.getProperties();

                        Log.d(TAG, String.format("*** ExpandableListView.OnChildClickListener uuid=%s",mTargetCharacteristic.getUuid()));
                        UUID uuid=mTargetCharacteristic.getUuid();
                        //final Intent intent;


                        if (uuid.equals(GattAttributes.UUID_NORDIC_UART_RX_CHAR) ||
                           uuid.equals(GattAttributes.UUID_NORDIC_UART_TX_CHAR)) { //Nordic Uart Service
                            NordicSelectDialog.show();
                        } else {
                            final Intent intent = new Intent(DeviceControlActivity.this, CharacteristicReadWriteActivity.class); //default
                            startActivity(intent);
                        }
                        /*
                        // Notify first, then Read
                        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            if (mNotifyCharacteristic == null) {
                                // Enable Notify
                                Toast.makeText(DeviceControlActivity.this, "Notify Enabled", Toast.LENGTH_SHORT).show();
                                mNotifyCharacteristic = characteristic;
                                mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                                mBluetoothLeService.readCharacteristic(characteristic);
                            } else {
                                // Disable Notify
                                Toast.makeText(DeviceControlActivity.this, "Notify Disabled", Toast.LENGTH_SHORT).show();
                                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                        } else if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification( mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            Toast.makeText(DeviceControlActivity.this, "Read", Toast.LENGTH_SHORT).show();
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        */
                        return true;
                    }
                    else
                    {
                        mTargetCharacteristic = null;
                    }
                    return false;
                }
            };

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_control);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        mRSSI =  (TextView) findViewById(R.id.control_RSSI);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        //intent = new Intent(DeviceControlActivity.this, NordicUarActivityt.class);
        final String[] items = {"Generic IO","Nordic UART"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select I/O Control").setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) { //Generic IO
                    final Intent intent = new Intent(DeviceControlActivity.this, CharacteristicReadWriteActivity.class); //Generic
                    startActivity(intent);
                } else //
                {
                    final Intent intent = new Intent(DeviceControlActivity.this, NordicUartActivity.class); //Nordic
                    startActivity(intent);
                }
            }
        });
        NordicSelectDialog = builder.create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_control, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        } else {
            mDataField.setText(R.string.no_data);
        }
    }

    private void displayRSSI(String data) {
        if (data != null) {
            mRSSI.setText(data+" dBm");
        } else {
            mRSSI.setText(R.string.no_data);
        }
    }
    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, GattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                charas.add(gattCharacteristic);

                HashMap<String, String> currentCharaData = new HashMap<String, String>();

                uuid = gattCharacteristic.getUuid().toString();

                currentCharaData.put(
                        LIST_NAME, GattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);

                final int charaProp = gattCharacteristic.getProperties();
                String strProperties= String.format("[%04X]",charaProp);
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_BROADCAST) > 0) {
                    strProperties += " Broadcast";
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                    strProperties += " Indicate";
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS) > 0) {
                    strProperties += " ExtendedProps";
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    strProperties += " Read";
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                    strProperties += " Write";
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) > 0) {
                    strProperties += " SignedWrite";
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                    strProperties += " WriteNoResponse";
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    strProperties += " Notify";
                }
                currentCharaData.put(LIST_PROPERTIES, strProperties);

                final int charaPerm = gattCharacteristic.getPermissions();
                String strPermission = String.format("[%04X]",charaPerm);
                if ((charaPerm & BluetoothGattCharacteristic.PERMISSION_READ) > 0) {
                    strPermission += " Read";
                }
                if ((charaPerm & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED) > 0) {
                    strPermission += " ReadEncrypted";
                }
                if ((charaPerm & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM) > 0) {
                    strPermission += " ReadEncryptedMitm";
                }
                if ((charaPerm & BluetoothGattCharacteristic.PERMISSION_WRITE) > 0) {
                    strPermission += " Write";
                }
                if ((charaPerm & BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED) > 0) {
                    strPermission += " WriteEncrypted";
                }
                if ((charaPerm & BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM) > 0) {
                    strPermission += "WriteEncryptedMitm";
                }
                if ((charaPerm & BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED) > 0) {
                    strPermission += " WriteSigned";
                }
                if ((charaPerm & BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM) > 0) {
                    strPermission += " WriteSignedMitm";
                }
                currentCharaData.put(LIST_PERMISSION, strPermission);

                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] {android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                R.layout.characteristics_item_view,
                new String[] {LIST_NAME, LIST_UUID, LIST_PROPERTIES, LIST_PERMISSION},
                new int[] {R.id.characteristic_name, R.id.characteristic_uuid, R.id.characteristic_properties, R.id.characteristic_permission}
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
       // intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE); //removed by ghosty
        intentFilter.addAction(BluetoothLeService.RSSI_DATA_AVAILABLE); //ghosty
        return intentFilter;
    }
}
