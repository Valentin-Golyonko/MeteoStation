package valentin8dev.by.MeteoStation;

import android.util.Log;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;


class MyXAxisValueFormatter implements IAxisValueFormatter {

    private ArrayList<String> valuesA = new ArrayList<>();

    MyXAxisValueFormatter(ArrayList<String> values) {
        this.valuesA = values;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // "value" represents the position of the label on the axis (x or y)
        int intValue = (int) value;
        if (valuesA.size() > intValue && intValue >= 0) {
            //Log.d("VF", "value " + valuesA.get(intValue));
            return valuesA.get(intValue);
        }

        return "";
        //return valuesA.get((int) value);
    }
}
