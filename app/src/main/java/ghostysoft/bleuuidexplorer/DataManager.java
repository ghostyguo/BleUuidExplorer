package ghostysoft.bleuuidexplorer;

import android.util.Log;

/**
 * Created by ghosty on 2015/7/4.
 */
public class DataManager {
    private final static String TAG = DataManager.class.getSimpleName();

    static final String pattern20="A123456789B123456789";
    static final String pattern50="A123456789B123456789C123456789D123456789E123456789";
    static final String pattern100="A123456789B123456789C123456789D123456789E123456789F123456789G123456789H123456789I123456789J123456789";
    static final String pattern200=pattern100+pattern100;
    static final String pattern500=pattern100+pattern100+pattern100+pattern100+pattern100;
    static final String pattern1K = pattern100+pattern100+pattern100+pattern100+pattern100+pattern100+pattern100+pattern100+pattern100+pattern100;
    static final String pattern2K = pattern1K+pattern1K;
    static final String pattern5K = pattern1K+pattern1K+pattern1K+pattern1K+pattern1K;
    static final String pattern10K = pattern1K+pattern1K+pattern1K+pattern1K+pattern1K+pattern1K+pattern1K+pattern1K+pattern1K+pattern1K;


    public StringBuilder byteArrayToHex(byte data[]) {
        Log.d(TAG, "dumpHex()");
        final StringBuilder strResult = new StringBuilder();
        for (int i=0; i<data.length; i++)
            strResult.append(String.format("%02X ", data[i]));
        return strResult;
    }

    public StringBuilder byteArrayToHex(byte data[], int length) {
        Log.d(TAG, String.format("dumpHex() length=%d",length));
        final StringBuilder strResult = new StringBuilder();
        for (int i=0; i<length; i++)
            strResult.append(String.format("%02X ", data[i]));
        return strResult;
    }

    public StringBuilder byteArrayToHex(byte data[], int start, int length) {
        Log.d(TAG, String.format("dumpHex() start=%d, length=%d",start,length));
        final StringBuilder strResult = new StringBuilder();
        for (int i=start; i<start+length; i++)
            strResult.append(String.format("%02X ", data[i]));
        return strResult;
    }

    public byte[] hexStringToByteArray(String s) {
        /*
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
        */
        String[] items = s.split(" ");
        byte[] data = new byte[items.length];
        for (int i = 0; i < items.length; i++) {
            try {
                data[i] = Byte.parseByte (items[i],16);
            } catch (NumberFormatException nfe) {};
        }
        return data;
    }

    public byte[] decStringToByteArray(String s) {
        String[] items =s.split(" ");
        byte[] data = new byte[items.length];
        for (int i = 0; i < items.length; i++) {
            try {
                data[i] = Byte.parseByte (items[i],10);
            } catch (NumberFormatException nfe) {};
        }
        return data;
    }

}
