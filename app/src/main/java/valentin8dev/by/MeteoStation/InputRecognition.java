package valentin8dev.by.MeteoStation;

import android.util.Log;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

class InputRecognition {

    private static final String TAG = "InputRecognition";

    static String tempr = "--", humid = "--", air = "--", tempr2 = "--",
            pirSensor = "--", pressure = "--", rgbLight = "--";

    // TODO: move to dif-t class, using Android Architecture Components
    static ArrayList<Float> fA = new ArrayList<>(); // fake array
    static ArrayList<String> valuesA = new ArrayList<>();   // array for current time

    private static int t = -1, z = -1, h = -1, y = -1, a = -1, x = -1, j = -1, q = -1,
            l = -1, w = -1, p = -1, u = -1, d = -1, s = -1;
    private static boolean date_came = false;
    private static float ii = 0;
    private static ArrayList<Float> t2Array = new ArrayList<>();
    private static int t2Size;

    /**
     * resolve input string 'in' to data (int) and parse it to the Activity (view elements)
     * <p>
     * ex: input t16.30zj17.01qh30.75ya166.41xd748.46sp0ul0w
     * tempr = 16.30
     * tempr2 = 17.01
     * hum = 30.75
     * air = 166.41
     * pres = 748.46
     * pir = 0
     * rgbLight = 0
     */
    static void inputSymbols(String input) {
        if (input != null) {
            //Log.d(TAG, "input " + input);
            setDate_came(true);
            int input_length = input.length();

            for (int i = 0; i < input_length; i++) {
                switch (input.charAt(i)) {
                    case 't': t = i + 1; break;
                    case 'z': z = i; break;
                    case 'h': h = i + 1; break;
                    case 'y': y = i; break;
                    case 'a': a = i + 1; break;
                    case 'x': x = i; break;
                    case 'j': j = i + 1; break;
                    case 'q': q = i; break;
                    case 'l': l = i + 1; break;
                    case 'w': w = i; break;
                    case 'p': p = i + 1; break;
                    case 'u': u = i; break;
                    case 'd': d = i + 1; break;
                    case 's': s = i; break;
                }
            }

            tempr = parameter(input, t, z);
            t = z = -1;

            humid = parameter(input, h, y);
            h = y = -1;

            air = parameter(input, a, x);
            a = x = -1;

            parameter_new(input, j, q, t2Array);
            setT2Size(t2Array.size());
            j = q = -1;

            rgbLight = parameter(input, l, w);
            l = w = -1;

            pirSensor = parameter(input, p, u);
            p = u = -1;

            pressure = parameter(input, d, s);
            d = s = -1;

            if (MyWidget.isWidgetOn()) {
                BluetoothFragment.mBluetoothService.stop();
                MyWidget.setWidgetOn(false);
            }
        }

        setDate_came(false);
    }

    /** Method build string from input values, convert them to float and store in array */
    // NEW version, need to implement to all elements.
    private static void parameter_new(String in, int from, int to, ArrayList<Float> arrayList) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(in);

        if (from != -1 && to != -1) {
            int stringSize = to - from;
            if (stringSize > 0) { // protection from outOfBoundEx-n
                String param = stringBuilder.substring(from, to);
                //Log.d(TAG, "param " + param);

                if (param != null && !param.equals(" NAN")) {
                    arrayList.add(Float.parseFloat(param)); // TODO: don't know why, but it works!
                    //Log.d(TAG, "arrayList " + arrayList.toString());
                }

                DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
                Date dateTime = new Date();
                String time = dateFormat.format(dateTime);

                fA.add(ii++);
                valuesA.add(time);
            }
        }
    }

    // Old version
    private static String parameter(String in, int from, int to) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(in);

        if (from != -1 && to != -1) {
            int stringSize = to - from;
            //Log.d(TAG, "from " + from + " to " + to + " s " + stringSize);
            if (stringSize > 0) { // protection from outOfBoundEx-n
                String param = stringBuilder.substring(from, to);
                //Log.d(TAG, "param " + param);

                return param;
            }
        }
        return "-1";
    }

    static ArrayList<Float> getT2Array() {
        return t2Array;
    }

    static int getT2Size() {
        return t2Size;
    }

    private static void setT2Size(int t2Size) {
        InputRecognition.t2Size = t2Size;
    }

    static boolean isDate_came() {
        //Log.d(TAG, "date_came " + date_came);
        return date_came;
    }

    static void setDate_came(boolean date_came) {
        InputRecognition.date_came = date_came;
        //Log.d(TAG, "date_came " + date_came);
    }
}
