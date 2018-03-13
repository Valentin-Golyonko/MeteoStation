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
    static ArrayList<Float> fA = new ArrayList<>();
    static ArrayList<String> valuesA = new ArrayList<>();

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
                    case 't':
                        t = i + 1;
                        break;
                    case 'z':
                        z = i - 1;
                        break;
                    case 'h':
                        h = i + 1;
                        break;
                    case 'y':
                        y = i - 1;
                        break;
                    case 'a':
                        a = i + 1;
                        break;
                    case 'x':
                        x = i - 1;
                        break;
                    case 'j':
                        j = i + 1;
                        break;
                    case 'q':
                        q = i;
                        break;
                    case 'l':
                        l = i + 1;
                        break;
                    case 'w':
                        w = i;
                        break;
                    case 'p':
                        p = i + 1;
                        break;
                    case 'u':
                        u = i;
                        break;
                    case 'd':
                        d = i + 1;
                        break;
                    case 's':
                        s = i;
                        break;
                }
            }

            buildString(input);

            if (MyWidget.isWidgetOn()) {
                BluetoothFragment.mBluetoothService.stop();
                MyWidget.setWidgetOn(false);
            }
        }

        setDate_came(false);
    }

    private static void buildString(String in) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(in);

        if (t != -1 && z != -1) {
            tempr = stringBuilder.substring(t, z);
            //Log.d(TAG, "tempr " + tempr);

            t = -1;
            z = -1;
        } else {    // if date did not came
            tempr = "";
        }
        if (h != -1 && y != -1) {
            humid = stringBuilder.substring(h, y);
            //Log.d(TAG, "humid " + humid);
            h = -1;
            y = -1;
        } else {
            humid = "";
        }
        if (a != -1 && x != -1) {
            air = stringBuilder.substring(a, x);

            //Log.d(TAG, "air " + air);
            a = -1;
            x = -1;
        } else {
            air = "";
        }
        if (j != -1 && q != -1) {
            tempr2 = stringBuilder.substring(j, q);
            //Log.d(TAG, "tempr2 " + tempr2);

            buildT2Array();

            j = -1;
            q = -1;
        } else {
            tempr2 = "";
        }
        if (l != -1 && w != -1) {
            rgbLight = stringBuilder.substring(l, w);
            l = -1;
            w = -1;
        } else {
            rgbLight = "";
        }
        if (p != -1 && u != -1) {
            pirSensor = stringBuilder.substring(p, u);
            p = -1;
            u = -1;
        } else {
            pirSensor = "";
        }
        if (d != -1 && s != -1) {
            pressure = stringBuilder.substring(d, s);
            //Log.d(TAG, "pressure " + pressure);
            d = -1;
            s = -1;
        } else {
            pressure = "";
        }
    }

    private static void buildT2Array() {
        if (tempr2 != null && !tempr2.equals(" NAN")) {
            t2Array.add(Float.parseFloat(tempr2));
            setT2Size(t2Array.size());
            //Log.d(TAG, "t2Array " + t2Array.toString());
        }

        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        Date dateTime = new Date();
        String time = dateFormat.format(dateTime);

        fA.add(ii++);
        //Log.d(TAG, "ii: " + ii + " fA: " + fA);
        valuesA.add(time);
        //Log.d(TAG, "values " + valuesA);
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
