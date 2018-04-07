package valentin8dev.by.MeteoStation;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class Graphics extends Fragment {

    private static LineChart chart;
    private final String TAG = "GraphicsFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootGraphics = inflater.inflate(R.layout.graphics, container, false);

        chart = rootGraphics.findViewById(R.id.chart_menu_graphic);
        chart.setTouchEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDragEnabled(true);

        if (InputRecognition.getSizeT2() != 0) {
            graphicGF();
        }

        return rootGraphics;
    }

    static void graphicGF() {

        int size = InputRecognition.getSizeT2();
        ArrayList<Float> fa = new ArrayList<>();
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            fa.add(i, (float)i);
            entries.add(new Entry(fa.get(i), InputRecognition.getArrayT2().get(i)));

            XAxis xAxis = chart.getXAxis(); // TODO: .NullPointerException when the screen rotates
            xAxis.setValueFormatter(new MyXAxisValueFormatter(InputRecognition.times_for_t2));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Temperature");
        dataSet.setColor(Color.BLACK);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        chart.setVisibleXRangeMaximum(20);
/*
        // get overall orientation of the screen.
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == 1) { // ORIENTATION_PORTRAIT
            // allow 20 values to be displayed at once on the x-axis, not more
            chart.setVisibleXRangeMaximum(20);
        } else if (orientation == 2) {  // ORIENTATION_LANDSCAPE
            chart.setVisibleXRangeMaximum(50);
        }
*/

        // Moves the left side (edge) of the current viewport to the specified x-value.
        chart.moveViewToX(size - 1);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.options_menu_graphics, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }
}
