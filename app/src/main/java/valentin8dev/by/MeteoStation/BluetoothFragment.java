package valentin8dev.by.MeteoStation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * This fragment controls Bluetooth to communicate with your Meteo Station
 */
public class BluetoothFragment extends Fragment {

    private static final String TAG = "BluetoothFragment";

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 3;
    static BluetoothAdapter mBluetoothAdapter = null;
    static BluetoothService mBluetoothService = null;
    static boolean CONNECTED = false;   // 'flag' to start painting (from app or widget)
    static String meteoStationMAC = null;
    private final int SPEECH_RECOGNITION_CODE = 4;
    private CardView cv_t1;
    private CardView cv_t2;
    private CardView cv_h;
    private StringBuilder sb = new StringBuilder();
    private TextView voiceToTxtOutput;
    private ImageButton btnMicrophone;
    private Switch switch_blt;
    private TextView tvTemp;
    private TextView tvHum;
    private TextView tvAirq;
    private AppCompatButton btnRgbLight;
    private Switch switch_rgbLight;
    private TextView tvTempr2;
    private TextView tvPressure;
    private TextView tvPir;
    private ProgressBar pBar;
    private boolean running = false;    // alarm dialog on/off  TODO: do i need it ?
    private int active_chart = 1;   // 1 = temperature_1

    // The Handler that gets information back from the BluetoothService
    // TODO: HandlerLeak ???
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            pBar.setVisibility(View.INVISIBLE);
                            pBar.animate().cancel();
                            CONNECTED = true;
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            pBar.setVisibility(View.VISIBLE);
                            pBar.animate().start();
                            break;
                        case BluetoothService.STATE_LISTEN:
                            break;
                        case BluetoothService.STATE_NONE:
                            pBar.setVisibility(View.INVISIBLE);
                            pBar.animate().cancel();
                            break;
                    }
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;  // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    sb.append(readMessage);

                    Log.d(TAG, "sb " + sb.toString() + " " + sb.length());
                    /* example: sb t16.30zj17.01qh30.75ya166.41xd748.46sp0ul0wE 46 */

                    int endOfLineIndex = sb.indexOf("E");   // end of the receiving line index
                    if (endOfLineIndex > 0) {
                        String sbPrint = sb.substring(0, endOfLineIndex);
                        sb.delete(0, sb.length());
                        sb.trimToSize();

                        InputRecognition.inputSymbols(sbPrint); // resolve input string to data (int)
                        updateUI();                             // and than update layout
                    }
                    if (sb.length() > 64) {
                        sb.delete(0, sb.length());
                        Log.w(TAG, "sb too large");
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    String connectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + connectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();

                        // check connection state and switcher
                        if (BluetoothService.mState == 0 || BluetoothService.mState == 1) {
                            switch_blt.setChecked(false);
                        }
                    }
                    break;
            }
        }
    };

    public static boolean isCONNECTED() {
        return CONNECTED;
    }

    public static void setCONNECTED(boolean c) {
        BluetoothFragment.CONNECTED = c;
        //Log.d(TAG, "CONNECTED " + CONNECTED);
    }

    /**
     * Establish connection with other device
     */
    public static void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        if (address != null) {
            meteoStationMAC = address;
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            // Attempt to connect to the device
            if (mBluetoothService != null) {
                mBluetoothService.connect(device);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // If BT is not on, request that it be enabled and do it onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mBluetoothService == null) {
            // Initialize the BluetoothService to perform bluetooth connections
            mBluetoothService = new BluetoothService(getActivity(), mHandler);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateUI();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth services
                mBluetoothService.start();
            }

            /*if (meteoStationMAC != null) {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(meteoStationMAC);
                // Attempt to connect to the device
                if (mBluetoothService != null) {
                    mBluetoothService.connect(device);
                }
            }*/
        }

        // set button color background
        if (RGBLightSelect.rgbColor != null) {
            btnRgbLight.setBackgroundColor(RGBLightSelect.rgbColor_i);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // TODO: stop transmission in background (add timer?)
        // TODO: RGBLightSelector - Save and display the last selected ??
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBluetoothService != null) { // TODO: triggered when the screen is rotated
            mBluetoothService.stop();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootBF = inflater.inflate(R.layout.test_main, container, false);  // fragment_bluetooth / test_main

        switch_blt = rootBF.findViewById(R.id.switch_blt);
        voiceToTxtOutput = rootBF.findViewById(R.id.voice_to_text_output);
        btnMicrophone = rootBF.findViewById(R.id.btn_mic);
        tvTemp = rootBF.findViewById(R.id.tv_temperature_1);
        tvHum = rootBF.findViewById(R.id.tv_humidity);
        tvAirq = rootBF.findViewById(R.id.tv_air);
        tvTempr2 = rootBF.findViewById(R.id.tv_temperature_2);
        tvPressure = rootBF.findViewById(R.id.tv_pressure);
        tvPir = rootBF.findViewById(R.id.tv_pir);
        btnRgbLight = rootBF.findViewById(R.id.btn_rgbLight);
        switch_rgbLight = rootBF.findViewById(R.id.switch_rgbLight);
        pBar = rootBF.findViewById(R.id.pb_main_fragment);

        FragmentManager chart_fm = getActivity().getSupportFragmentManager();
        FragmentTransaction chart_ft = chart_fm.beginTransaction();
        chart_ft.replace(R.id.fragment_for_line_chart, new LineChartForBF())
                .commit();

        cv_t1 = rootBF.findViewById(R.id.cardView_t1);
        cv_t2 = rootBF.findViewById(R.id.cardView_t2);
        cv_h = rootBF.findViewById(R.id.cardView_h);

        updateUI();

        return rootBF;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        switch_blt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switch_blt.isChecked()) {
                    // Launch the DeviceListActivity to see devices and do scan
                    Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                } else {
                    if (mBluetoothService != null) {
                        mBluetoothService.stop();
                    }
                }
            }
        });

        btnMicrophone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechToText();
            }
        });

        btnRgbLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRgbLight();
                Periphery.periphery(2, true);
            }
        });

        switch_rgbLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Periphery.periphery(2, switch_rgbLight.isChecked());
            }
        });

        cv_t1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InputRecognition.getSizeT1() > 0) {
                    //LineChartForBF.graphicBF_t1();
                    LineChartForBF.graphics(1);
                    active_chart = 1;
                }
            }
        });

        cv_t2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InputRecognition.getSizeT2() > 0) {
                    //LineChartForBF.graphicBF_t2();
                    LineChartForBF.graphics(4);
                    active_chart = 4;
                    //updateUI();
                }
            }
        });

        cv_h.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InputRecognition.getSizeH() > 0) {
                    //LineChartForBF.graphicBF_h();
                    LineChartForBF.graphics(2);
                    active_chart = 2;
                    //updateUI();
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data);
                } else {
                    switch_blt.setChecked(false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a session
                    // Initialize the BluetoothService to perform bluetooth connections
                    mBluetoothService = new BluetoothService(getActivity(), mHandler);
                    //Log.d(TAG, "BT enabled");
                } else {
                    // User did not enable Bluetooth or an error occurred
                    //Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                break;
            case SPEECH_RECOGNITION_CODE:
                // Callback for speech updateUI activity
                if (resultCode == MainActivity.RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String speech_recognition_text = result.get(0);
                    voiceToTxtOutput.setText(speech_recognition_text);
                    //Log.d(TAG, speech_recognition_text);

                    TextToInt.TextRecognition(speech_recognition_text);
                }
                break;
        }
    }

    private void changeRgbLight() {
        // Launch the RGBLightSelect Fragment to select RGB-color
        FragmentManager fmL = getActivity().getSupportFragmentManager();
        FragmentTransaction ftL = fmL.beginTransaction();
        ftL.replace(R.id.fragment_main, new RGBLightSelect())
                .addToBackStack(null)
                .commit();
    }

    /**
     * Start speech to text intent.
     * This opens up Google Speech Recognition API dialog box to listen the speech input.
     */
    public void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());   //
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speak something...");
        try {
            startActivityForResult(intent, SPEECH_RECOGNITION_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getActivity(),
                    "Sorry! Speech updateUI is not supported in this device.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {

        //tvTemp.setText(String.format("%s °C", InputRecognition.tempr));
        int t1Size = InputRecognition.getSizeT1();
        if (t1Size > 0) {
            float t1 = InputRecognition.getArrayT1().get(t1Size - 1);
            tvTemp.setText(String.format("%s °C", t1));
            if (active_chart == 1) {
                //LineChartForBF.graphicBF_t1();
                LineChartForBF.graphics(1);
            }
            // with this implementation i do't have '-1' value from InputRecognition
        }

        //tvHum.setText(MessageFormat.format("{0} %", InputRecognition.humid));
        int hSize = InputRecognition.getSizeH();
        if (hSize > 0) {
            float h = InputRecognition.getArrayH().get(hSize - 1);
            tvHum.setText(MessageFormat.format("{0} %", h));
            if (active_chart == 2) {
                //LineChartForBF.graphicBF_h();
                LineChartForBF.graphics(2);
            }
        }

        tvAirq.setText(String.format("%s AirQ", InputRecognition.air));

        String rgbLight = InputRecognition.rgbLight;
        if (rgbLight.equals("1")) {
            switch_rgbLight.setChecked(true);
            //Log.d(TAG, "RGBlight is On");
        } else {
            switch_rgbLight.setChecked(false);
            //Log.d(TAG, "RGBlight is Off");
        }

        String pirSensor = InputRecognition.pirSensor;
        if (pirSensor.equals("1")) {
            //Log.d(TAG, "PIRSensor is On");
            tvPir.setText(R.string.pir_on);
            tvPir.setTextColor(Color.RED);
            tvPir.setBackgroundColor(Color.YELLOW);
        } else {
            running = false;
            //Log.d(TAG, "PIRSensor is Off");
            tvPir.setText(R.string.pir_off);
            tvPir.setTextColor(Color.BLACK);
            tvPir.setBackgroundColor(Color.WHITE);
        }

        tvPressure.setText(String.format("%s mm.rs", InputRecognition.pressure));

        int t2Size = InputRecognition.getSizeT2();
        if (t2Size > 0) {
            float t2 = InputRecognition.getArrayT2().get(t2Size - 1);
            tvTempr2.setText(String.format("%s °C", t2));
            if (active_chart == 4) {
                //LineChartForBF.graphicBF_t2();
                LineChartForBF.graphics(4);
            }
        }
    }
}