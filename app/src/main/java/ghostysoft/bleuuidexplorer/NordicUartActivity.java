package ghostysoft.bleuuidexplorer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;


public class NordicUartActivity extends Activity {
    private final static String TAG = CharacteristicReadWriteActivity.class.getSimpleName();

    TextView mRxData;
    EditText mTxData;
    private Button btnSend, btnClear;
    RadioGroup mTxModeGroup, mRxModeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.nordic_uart);
        btnSend = (Button) findViewById(R.id.btnNordicUartSend);
        btnClear = (Button) findViewById(R.id.btnNordicUartClear);
        mRxData = (TextView) findViewById(R.id.NordicUartRxData);
        mTxData = (EditText) findViewById(R.id.NordicUartTxData);
        mRxModeGroup = (RadioGroup) findViewById(R.id.NordicUartRxModeGroup);
        mTxModeGroup = (RadioGroup) findViewById(R.id.NordicUartTxFormatGroup);

        // Handler Send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText mTxData = (EditText) findViewById(R.id.NordicUartTxData);
                String message = mTxData.getText().toString();
                byte[] value; //used for UTF8
                try {
                    //send data to service
                    value = message.getBytes("UTF-8"); //Used for UTF8
                    DeviceControlActivity.mBluetoothLeService.writeRXCharacteristic(value);

                    //Update the log with time stamp
                    //--String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    //--listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    //--messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    //mTxData.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

        // Handler Clear button
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRxData.setText("");
            }
        });

        DeviceControlActivity.mBluetoothLeService.enableTXNotification();
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nordic_uart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        String pattern100="A123456789B123456789C123456789D123456789E123456789F123456789G123456789H123456789I123456789J123456789";
        String pattern1K = pattern100+pattern100+pattern100+pattern100+pattern100+pattern100+pattern100+pattern100+pattern100+pattern100;
        String pattern10K = pattern1K+pattern1K+pattern1K+pattern1K+pattern1K+pattern1K+pattern1K+pattern1K+pattern1K+pattern1K;

        switch (item.getItemId()) {
            case R.id.menu_hot_keys:
                Log.d(TAG, "menu hot keys selected");
                mTxData.setText(item.getTitle());
                //sendMessage.setSelection(sendMessage.getText().length());
                return true;
            case R.id.menu_throughput_test100:
                Log.d(TAG, "menu_throughput_test selected");
                mTxData.setText(pattern100);
                //sendMessage.setSelection(sendMessage.getText().length());
                return true;
            case R.id.menu_throughput_test1k:
                Log.d(TAG, "menu_throughput_test selected");
                mTxData.setText(pattern1K);
                //sendMessage.setSelection(sendMessage.getText().length());
                return true;
            case R.id.menu_throughput_test10k:
                Log.d(TAG, "menu_throughput_test selected");
                mTxData.setText(pattern10K);
                //sendMessage.setSelection(sendMessage.getText().length());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                final byte[] txValue = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String rxText = new String(txValue, "UTF-8");
                            switch (mRxModeGroup.getCheckedRadioButtonId()) {
                                case R.id.NordicUartMode_Debug: //debug mode
                                    mRxData.setText(mRxData.getText().toString()+rxText);
                                    break;
                                case R.id.NordicUartMode_Nordic: //Nordic mode
                                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                                    mRxData.setText(mRxData.getText().toString()+String.format("[%s] RX:%s",currentDateTimeString,rxText));
                            }
                            final ScrollView scrollview = ((ScrollView) findViewById(R.id.NordicUartRxDataScrollView));
                            scrollview.post(new Runnable() {
                                @Override
                                public void run() {
                                    scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                                }
                            });
                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                        }
                    }
                });
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                //cannot happened here
                Log.d(TAG,"ACTION_GATT_CONNECTED ???");
                //DeviceControlActivity.mBluetoothLeService.disconnect();
               // finish();
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG,"ACTION_GATT_DISCONNECTED");
                //DeviceControlActivity.mBluetoothLeService.disconnect();
                finish(); //close the activity
            }
            else if  (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                //cannot happened here
                Log.d(TAG,"ACTION_GATT_SERVICES_DISCOVERED ???");
                //DeviceControlActivity.mBluetoothLeService.disconnect();
               // finish(); //close the activity
            }
            else if (BluetoothLeService.DEVICE_DOES_NOT_SUPPORT_UART.equals(action)) {
                //cannot happened here
                Log.d(TAG,"DEVICE_DOES_NOT_SUPPORT_UART ???");
                //DeviceControlActivity.mBluetoothLeService.disconnect();
                finish(); //close the activity
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        //intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        //intentFilter.addAction(BluetoothLeService.RSSI_DATA_AVAILABLE); //ghosty
        return intentFilter;
    }

}
