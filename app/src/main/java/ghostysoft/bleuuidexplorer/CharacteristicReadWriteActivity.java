package ghostysoft.bleuuidexplorer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.bluetooth.BluetoothGattCharacteristic;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

public class CharacteristicReadWriteActivity extends Activity {

    private final static String TAG = CharacteristicReadWriteActivity.class.getSimpleName();

    private Button mButtonRead, mButtonNotify, mButtonIndicate, mButtonWrite;
    private TextView mRxData, mWriteData, mRSSI;
    private boolean mNotify, mIndicate;
    private RadioGroup mReadFormatGroup, mWriteFormatGroup;

    private BluetoothGattCharacteristic mNotifyCharacteristic, mIndicateCharacteristic; //used for Notification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, String.format("*** onCreate()"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.characteristic_read_write);

        mRxData =  (TextView) findViewById(R.id.characteristic_RxData);
        mWriteData =  (TextView) findViewById(R.id.characteristic_WriteData);
        mRSSI =  (TextView) findViewById(R.id.characteristic_RSSI);
        mReadFormatGroup = (RadioGroup) findViewById(R.id.characteristic_ReadGorup);
        mWriteFormatGroup = (RadioGroup) findViewById(R.id.characteristic_WriteGorup);

        mButtonRead = (Button)findViewById(R.id.characteristic_btnRead);
        mButtonIndicate = (Button)findViewById(R.id.characteristic_btnIndicate);
        mButtonNotify = (Button)findViewById(R.id.characteristic_btnNotify);
        mButtonWrite = (Button)findViewById(R.id.characteristic_btnWrite);
        mNotify = false;

        mReadFormatGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
               if (!mNotify) {
                    if ( DeviceControlActivity.mConnected) { //Check connection state before READ
                        DeviceControlActivity.mBluetoothLeService.readCharacteristic(DeviceControlActivity.mTargetCharacteristic);
                    }
               }
            }
        });

        mWriteFormatGroup.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

            }
        });

        //mRxData.setText(DeviceControlActivity.mTargetCharacteristic.getUuid().toString()); //debug
        final int charaProp = DeviceControlActivity.mTargetCharacteristic.getProperties();

        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            mButtonRead.setEnabled(true);
            mButtonRead.setOnClickListener(new Button.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Toast.makeText(CharacteristicReadWriteActivity.this, "Read", Toast.LENGTH_SHORT).show();
                    if ( DeviceControlActivity.mConnected) { //Check connection state before READ
                        DeviceControlActivity.mBluetoothLeService.readCharacteristic(DeviceControlActivity.mTargetCharacteristic);
                    }
                }
            });
        } else {
            mButtonRead.setEnabled(false);
        } // READ

        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mButtonNotify.setEnabled(true);
            mButtonNotify.setOnClickListener(new Button.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (DeviceControlActivity.mTargetCharacteristic != null) {
                        // if ( DeviceControlActivity.mConnected) { //Check connection state before READ
                        if (mNotifyCharacteristic == null) {
                            // Enable Notify
                            Toast.makeText(CharacteristicReadWriteActivity.this, "Notify Enabled", Toast.LENGTH_SHORT).show();
                            mNotifyCharacteristic = DeviceControlActivity.mTargetCharacteristic;
                            DeviceControlActivity.mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
                            DeviceControlActivity.mBluetoothLeService.readCharacteristic(mNotifyCharacteristic); //read once to start notify?
                            mButtonNotify.setTextColor(Color.RED);
                            mNotify = true;
                            //mButtonRead.setEnabled(false);
                        } else {
                            // Disable Notify
                            Toast.makeText(CharacteristicReadWriteActivity.this, "Notify Disabled", Toast.LENGTH_SHORT).show();
                            DeviceControlActivity.mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;
                            mButtonNotify.setTextColor(Color.BLACK);
                            mNotify = false;
                            //mButtonRead.setEnabled(true);
                        }
                        //}
                    }
                }

            });
        } else {
            mButtonNotify.setEnabled(false);
        } //NOTIFY

        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
            mButtonIndicate.setEnabled(true);
            mButtonIndicate.setOnClickListener(new Button.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (DeviceControlActivity.mTargetCharacteristic != null) {
                        // if ( DeviceControlActivity.mConnected) { //Check connection state before READ
                        if (mIndicateCharacteristic == null) {
                            // Enable Notify
                            Toast.makeText(CharacteristicReadWriteActivity.this, "Indicate Enabled", Toast.LENGTH_SHORT).show();
                            mIndicateCharacteristic = DeviceControlActivity.mTargetCharacteristic;
                            DeviceControlActivity.mBluetoothLeService.setCharacteristicIndication(mIndicateCharacteristic, true);
                            DeviceControlActivity.mBluetoothLeService.readCharacteristic(mIndicateCharacteristic); //read once to start indicate?
                            mButtonIndicate.setTextColor(Color.RED);
                            mNotify = true;
                            //mButtonRead.setEnabled(false);
                        } else {
                            // Disable Notify
                            Toast.makeText(CharacteristicReadWriteActivity.this, "Indicate Disabled", Toast.LENGTH_SHORT).show();
                            DeviceControlActivity.mBluetoothLeService.setCharacteristicNotification(mIndicateCharacteristic, false);
                            mIndicateCharacteristic = null;
                            mButtonIndicate.setTextColor(Color.BLACK);
                            mIndicate = false;
                            //mButtonRead.setEnabled(true);
                        }
                        //}
                    }
                }

            });
        } else {
            mButtonIndicate.setEnabled(false);
        } //Indicate

        if (((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) ||
            (charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
            mButtonWrite.setEnabled(true);
            mButtonWrite.setOnClickListener(new Button.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Toast.makeText(CharacteristicReadWriteActivity.this, "Write", Toast.LENGTH_SHORT).show();
                    EditText editText = (EditText) findViewById(R.id.characteristic_WriteData);
                    String message = editText.getText().toString();
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        DeviceControlActivity.mBluetoothLeService.writeRXCharacteristic(value);
                       // editText.setText("");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        } else {
            mButtonWrite.setEnabled(false);
        } // WRITE
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (DeviceControlActivity.mBluetoothLeService != null) {
            final boolean result = DeviceControlActivity.mBluetoothLeService.connect(DeviceControlActivity.mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        Log.d(TAG, String.format("*** onCreateOptionMenu()"));
        getMenuInflater().inflate(R.menu.menu_characteristic_read_write, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.d(TAG, String.format("*** onOptionsItemSelected %d",id));

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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
            if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                // put some code here
                finish(); //close the activity
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayRxData(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.RSSI_DATA_AVAILABLE.equals(action)) {
                displayRSSI(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
            Log.d(TAG, "BroadcastReceiver.onReceive():action="+action);
        }
    };

    private void displayRxData(byte[] data) {
        if (data != null) {
            final StringBuilder strData = new StringBuilder();
            switch (mReadFormatGroup.getCheckedRadioButtonId()) {
                case R.id.characteristic_ReadFormatDEC:
                    Log.d(TAG, "RxData format DEC");
                    for(byte byteChar : data)
                        strData.append(String.format("%d ", byteChar));
                    break;
                case R.id.characteristic_ReadFormatHEX:
                    Log.d(TAG, "RxData format HEX");
                    for(byte byteChar : data)
                        strData.append(String.format("%02X ", byteChar));
                    break;
                case R.id.characteristic_ReadFormatSTRING:
                    Log.d(TAG, "RxData format STRING");
                    for(byte byteChar : data)
                        strData.append(String.format("%c", byteChar));
                    break;
                default:
                    Log.d(TAG, "RxData format ERROR");
            }
            mRxData.setText(strData);
        } else {
            mRxData.setText("");
        }
    }

    private void displayRSSI(String data) {
        if (data != null) {
            mRSSI.setText(data+" dBm");
        } else {
            mRSSI.setText(R.string.no_data);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        //intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.RSSI_DATA_AVAILABLE); //ghosty
        return intentFilter;
    }
}
