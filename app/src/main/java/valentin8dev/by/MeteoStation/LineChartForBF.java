package valentin8dev.by.MeteoStation;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class LineChartForBF extends Fragment {

    private static final String TAG = "LineChartForBF";

    private static LineChart chart;

    static void graphics(int id) {

        int size = 0;
        ArrayList<Float> floats = new ArrayList<>();
        ArrayList<String> strings = new ArrayList<>();
        String legend = "";

        switch (id) {
            case 1: // Temperature 1 from DHT-22
                floats = InputRecognition.getArrayT1();
                size = InputRecognition.getSizeT1();
                strings = InputRecognition.times_for_t1;
                legend = "Temperature 1";
                break;
            case 2: // Humidity from DHT-22
                floats = InputRecognition.getArrayH();
                size = InputRecognition.getSizeH();
                strings = InputRecognition.times_for_h;
                legend = "Humidity";
                break;
            case 3: // air quality from MQ-135
                floats = InputRecognition.getArrayA();
                size = InputRecognition.getSizeA();
                strings = InputRecognition.times_for_a;
                legend = "Air";
                break;
            case 4: // Temperature 2 from BMP280
                floats = InputRecognition.getArrayT2();
                size = InputRecognition.getSizeT2();
                strings = InputRecognition.times_for_t2;
                legend = "Temperature 2";
                break;
            case 5: // Pressure from BMP280
                floats = InputRecognition.getArrayP();
                size = InputRecognition.getSizeP();
                strings = InputRecognition.times_for_p;
                legend = "Pressure";
                break;
        }

        List<Entry> entries = new ArrayList<>();
        ArrayList<Float> fa = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            fa.add(i, (float) i);
            entries.add(new Entry(fa.get(i), floats.get(i)));
        }

        XAxis xAxis = chart.getXAxis(); // TODO: .NullPointerException when the screen rotates
        xAxis.setValueFormatter(new MyXAxisValueFormatter(strings));

        LineDataSet dataSet = new LineDataSet(entries, legend);
        dataSet.setColor(Color.BLACK);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        // allow 20 values to be displayed at once on the x-axis, not more
        chart.setVisibleXRangeMaximum(20);
        // Moves the left side (edge) of the current viewport to the specified x-value.
        chart.moveViewToX(size - 1);
        //chart.invalidate();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootLC = inflater.inflate(R.layout.bf_line_chart, container, false);

        chart = rootLC.findViewById(R.id.line_chart);

        return rootLC;
    }
}
