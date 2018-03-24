package valentin8dev.by.MeteoStation;

import android.util.Log;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

class InputRecognition {

    private static final String TAG = "InputRecognition";

    static String pirSensor = "--", rgbLight = "--";
    private static int t = -1, z = -1, h = -1, y = -1, a = -1, x = -1, j = -1, q = -1,
            l = -1, w = -1, p = -1, u = -1, d = -1, s = -1;

    private static boolean date_came = false;

    // TODO: move to dif-t class, using Android Architecture Components
    static ArrayList<String> times_for_t2 = new ArrayList<>(); // array of current time
    static ArrayList<String> times_for_t1 = new ArrayList<>(); // to plot graphic
    static ArrayList<String> times_for_h = new ArrayList<>();
    static ArrayList<String> times_for_a = new ArrayList<>();
    static ArrayList<String> times_for_p = new ArrayList<>();
    private static ArrayList<Float> arrayT2 = new ArrayList<>(); // array to store date
    private static ArrayList<Float> arrayT1 = new ArrayList<>();
    private static ArrayList<Float> arrayH = new ArrayList<>();
    private static ArrayList<Float> arrayA = new ArrayList<>();
    private static ArrayList<Float> arrayP = new ArrayList<>();
    private static int sizeT2;  // array size to plot graphic
    private static int sizeT1;
    private static int sizeH;
    private static int sizeA;
    private static int sizeP;

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

            parameter_new(input, t, z, arrayT1, times_for_t1);
            setSizeT1(arrayT1.size());
            t = z = -1;

            parameter_new(input, h, y, arrayH, times_for_h);
            setSizeH(arrayH.size());
            h = y = -1;

            parameter_new(input, a, x, arrayA, times_for_a);
            setSizeA(arrayA.size());
            a = x = -1;

            parameter_new(input, j, q, arrayT2, times_for_t2);
            setSizeT2(arrayT2.size());
            j = q = -1;

            rgbLight = parameter(input, l, w);
            l = w = -1;

            pirSensor = parameter(input, p, u);
            p = u = -1;

            parameter_new(input, d, s, arrayP, times_for_p);
            setSizeP(arrayP.size());
            d = s = -1;
        }
        setDate_came(false);
    }

    /**
     * Method build string from input values, convert them to float and store in array
     */
    private static void parameter_new(String in, int from, int to, ArrayList<Float> arrayList, ArrayList<String> time_value) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(in);

        // TODO: history !?
        if (arrayList.size() == 100) {
            arrayList.remove(0);
            arrayList.trimToSize();
        }
        if (time_value.size() == 100) {
            time_value.remove(0);
            time_value.trimToSize();
        }

        if (from != -1 && to != -1) {
            int stringSize = to - from;
            if (stringSize > 0) { // protection from outOfBoundEx-n
                String param = stringBuilder.substring(from, to);
                //Log.d(TAG, "param " + param);

                if (param != null && !param.equals(" NAN")) {
                    arrayList.add(Float.parseFloat(param));
                    //Log.d(TAG, arrayList.size() + " arrayList " + arrayList.toString());
                }

                DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
                Date dateTime = new Date();
                String time = dateFormat.format(dateTime);

                time_value.add(time);
                //Log.d(TAG, time_value.size() + " time_value " + time_value.toString());
            }
        }
        // don't know why, but it works as intended!
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

    static ArrayList<Float> getArrayT2() {
        return arrayT2;
    }

    static ArrayList<Float> getArrayT1() {
        return arrayT1;
    }

    static int getSizeT2() {
        return sizeT2;
    }

    private static void setSizeT2(int sizeT2) {
        InputRecognition.sizeT2 = sizeT2;
    }

    static ArrayList<Float> getArrayH() {
        return arrayH;
    }

    static int getSizeT1() {
        return sizeT1;
    }

    private static void setSizeT1(int sizeT1) {
        InputRecognition.sizeT1 = sizeT1;
    }

    static int getSizeH() {
        return sizeH;
    }

    private static void setSizeH(int sizeH) {
        InputRecognition.sizeH = sizeH;
    }

    static ArrayList<Float> getArrayA() {
        return arrayA;
    }

    static int getSizeA() {
        return sizeA;
    }

    private static void setSizeA(int sizeA) {
        InputRecognition.sizeA = sizeA;
    }

    static ArrayList<Float> getArrayP() {
        return arrayP;
    }

    static int getSizeP() {
        return sizeP;
    }

    private static void setSizeP(int sizeP) {
        InputRecognition.sizeP = sizeP;
    }

    static void setDate_came(boolean date_came) {
        InputRecognition.date_came = date_came;
    }

    public static boolean isDate_came() {
        return date_came;
    }
}
