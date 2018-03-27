package valentin8dev.by.MeteoStation;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.MessageFormat;

import static valentin8dev.by.MeteoStation.BluetoothFragment.mBluetoothAdapter;
import static valentin8dev.by.MeteoStation.BluetoothFragment.mBluetoothService;
import static valentin8dev.by.MeteoStation.BluetoothFragment.meteoStationMAC;

public class MyWidget extends AppWidgetProvider {

    private static boolean widgetOn = false;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.meteo_station);

        int t2Size = InputRecognition.getSizeT2();
        if (t2Size > 0) {
            float t2 = InputRecognition.getArrayT2().get(t2Size - 1);
            views.setTextViewText(R.id.appwidget_t2, String.format("%s °C", t2));
        }

        int hSize = InputRecognition.getSizeH();
        if (hSize > 0) {
            float h = InputRecognition.getArrayH().get(hSize - 1);
            views.setTextViewText(R.id.appwidget_hum, MessageFormat.format("{0} %", h));
        }

        int aSize = InputRecognition.getSizeA();
        if (aSize > 0) {
            float a = InputRecognition.getArrayA().get(aSize - 1);
            views.setTextViewText(R.id.appwidget_air, String.format("%s AirQ", a));
        }

        int pSize = InputRecognition.getSizeP();
        if (pSize > 0) {
            float p = InputRecognition.getArrayP().get(pSize - 1);
            views.setTextViewText(R.id.appwidget_pre, String.format("%s mm.rs", p));
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static boolean isWidgetOn() {
        return widgetOn;
    }

    public static void setWidgetOn(boolean widgetOn) {
        MyWidget.widgetOn = widgetOn;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {

            if (BluetoothFragment.mBluetoothAdapter != null && BluetoothFragment.meteoStationMAC != null) {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(meteoStationMAC);
                // Attempt to connect to the device
                if (mBluetoothService != null) {
                    setWidgetOn(true);
                    BluetoothFragment.mBluetoothService.connect(device);
                    //Log.d("widget ", "connect " + device);
                    updateAppWidget(context, appWidgetManager, appWidgetId);
                    //Log.d("widget ", "update");
                }
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        if (mBluetoothService != null) {
            mBluetoothService.stop();
        }
    }
}