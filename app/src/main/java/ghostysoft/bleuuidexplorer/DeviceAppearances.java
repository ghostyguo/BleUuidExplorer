package ghostysoft.bleuuidexplorer;

import java.util.HashMap;

/**
 * Created by ghosty on 2015/4/14.
 */
public class DeviceAppearances {
    private static HashMap<Integer, String> appearances = new HashMap();

    public static String lookup(Integer id, String defaultName) {
        String name =  appearances.get(id);
        return name == null ? defaultName : name;
    }

    static {
        appearances.put(0,"Unknown");
        appearances.put(64,"Generic Phone");
        appearances.put(128,"Generic Computer");
        appearances.put(192,"Generic Watch");
        appearances.put(193,"Watch: Sports Watch");
        appearances.put(256,"Generic Clock");
        appearances.put(320,"Generic Display");
        appearances.put(384,"Generic Remote Control");
        appearances.put(448,"Generic Eye-glasses");
        appearances.put(512,"Generic Tag");
        appearances.put(576,"Generic Keyring");
        appearances.put(640,"Generic Media Player");
        appearances.put(704,"Generic Barcode Scanner");
        appearances.put(768,"Generic Thermometer");
        appearances.put(769,"Thermometer: Ear");
        appearances.put(832,"Generic Heart rate Sensor");
        appearances.put(833,"Heart Rate Sensor: Heart Rate Belt");
        appearances.put(896,"Generic Blood Pressure");
        appearances.put(897,"Blood Pressure: Arm");
        appearances.put( 898,"Blood Pressure: Wrist");
        appearances.put(960,"Human Interface Device (HID)");
        appearances.put(961,"Keyboard (HID)");
        appearances.put(962,"Mouse (HID)");
        appearances.put(963,"Joystiq (HID)");
        appearances.put( 964,"Gamepad (HID)");
        appearances.put(965,"Digitizer Tablet (HID)");
        appearances.put(966,"Card Reader (HID)");
        appearances.put( 967,"Digital Pen (HID)");
        appearances.put(968,"Barcode Scanner (HID )");
        appearances.put(1024,"Generic Glucose Meter");
        appearances.put(1088,"Generic Running Walking Sensor");
        appearances.put(1089,"Running Walking Sensor: In-Shoe");
        appearances.put(1090,"Running Walking Sensor: On-Shoe");
        appearances.put(1091,"Running Walking Sensor: On-Hip");
        appearances.put(1152,"Generic Cycling");
        appearances.put(1153,"Cycling: Cycling Computer");
        appearances.put(1154,"Cycling: Speed Sensor");
        appearances.put( 1155,"Cycling: Cadence Sensor");
        appearances.put(1156,"Cycling: Power Sensor");
        appearances.put(1157,"Cycling: Speed and Cadence Sensor");
        appearances.put(3136,"Generic Pulse Oximeter");
        appearances.put(3137,"Fingertip (Pulse Oximeter)");
        appearances.put(3138,"Wrist Worn(Pulse Oximeter)");
        appearances.put(3200,"Generic Weight Scale");
        appearances.put(5184,"Generic Outdoor Sports Activity");
        appearances.put(5185,"Location Display Device (Outdoor Sports Activity)");
        appearances.put(5186,"Location and Navigation Display Device (Outdoor Sports Activitye)");
        appearances.put(5187,"Location Pod (Outdoor Sports Activity)");
        appearances.put(5188,"Location and Navigation Pod (Outdoor Sports Activity)");
        appearances.put(0x5566,"iCareBox");
    }
}
