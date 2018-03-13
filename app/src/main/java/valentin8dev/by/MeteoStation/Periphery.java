package valentin8dev.by.MeteoStation;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

class Periphery {

    private static final String TAG = "Periphery";
    /**
     * all Periphery in one 'switch' :
     * 1 = bltStatus (bluetooth status)
     * 2 = switchRgbLight (rgb strip switcher status)
     */

    private static int updatePeriod = 3;    // period of sending a message from the station = 3 sec

    static void periphery(int i, boolean bool) {
        try {
            //Get the output stream for data transfer
            if (BluetoothService.getmSocket() != null) {
                OutputStream outputStream = BluetoothService.getmSocket().getOutputStream();
                int value = -1;

                switch (i) {
                    case 1: // bltStatus
                        // Depending on which button was pressed, we change the data to send
                        if (bool) {
                            value = 1000 + updatePeriod;    // BLT ON, ready to receive data, period = 5sec
                        } else {
                            value = 2000;    // BLT OFF, stop transmit from arduino
                            InputRecognition.setDate_came(false);
                        }
                        break;
                    case 2: // switchRgbLight
                        if (bool) {
                            value = 3002;    // RGBStrip ON always
                            //Log.d(TAG, "rgb ON");
                        } else {
                            value = 3000;
                            //Log.d(TAG, "rgb OFF");
                        }
                        break;
                }

                //Log.d(TAG, String.valueOf(value));

                // Write the data to the output stream
                byte[] bytes = ByteBuffer.allocate(4).putInt(value).array();
                //Log.d(TAG, Arrays.toString(bytes));

                for (byte b : bytes) {
                    outputStream.write(b);
                }

            }
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    static void setUpdatePeriod(int updatePeriod) {
        Periphery.updatePeriod = updatePeriod;
    }
}
