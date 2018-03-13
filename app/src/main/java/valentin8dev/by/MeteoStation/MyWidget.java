package valentin8dev.by.MeteoStation;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.widget.RemoteViews;

import static valentin8dev.by.MeteoStation.BluetoothFragment.mBluetoothAdapter;
import static valentin8dev.by.MeteoStation.BluetoothFragment.mBluetoothService;
import static valentin8dev.by.MeteoStation.BluetoothFragment.meteoStationMAC;

public class MyWidget extends AppWidgetProvider {

    static boolean widgetOn = false;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        String t2 = InputRecognition.tempr2;
        String hum = InputRecognition.humid;
        String air = InputRecognition.air;
        String pres = InputRecognition.pressure;

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.meteo_station);

        views.setTextViewText(R.id.appwidget_t2, t2);
        views.setTextViewText(R.id.appwidget_hum, hum);
        views.setTextViewText(R.id.appwidget_air, air);
        views.setTextViewText(R.id.appwidget_pre, pres);

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

            if (mBluetoothAdapter != null && meteoStationMAC != null) {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(meteoStationMAC);
                // Attempt to connect to the device
                if (mBluetoothService != null) {
                    mBluetoothService.connect(device);
                    BluetoothFragment.setCONNECTED(true);
                    setWidgetOn(true);
                }
            }

            updateAppWidget(context, appWidgetManager, appWidgetId);
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

