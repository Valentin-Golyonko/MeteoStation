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
import android.support.annotation.NonNull;
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
import java.util.Objects;

/**
 * This fragment controls Bluetooth to communicate with your Meteo Station
 */
public class BluetoothFragment extends Fragment {

    private static final String TAG = "BluetoothFragment";

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 3;
    static BluetoothAdapter mBluetoothAdapter = null;
    static BluetoothService mBluetoothService = null;
    static String meteoStationMAC = null;
    private final int SPEECH_RECOGNITION_CODE = 4;
    private CardView cv_t1;
    private CardView cv_t2;
    private CardView cv_h;
    private CardView cv_a;
    private CardView cv_p;
    private CardView cv_pir;
    private StringBuilder sb = new StringBuilder();
    private TextView voiceToTxtOutput; // speech to text output
    private ImageButton btnMicrophone; // google speech to text
    private Switch switch_blt; // on/off bluetooth and select device to connect
    private TextView tvTemp; // Temperature 1 from DHT-22
    private TextView tvHum; // Humidity from DHT-22
    private TextView tvAirq; // air quality from MQ-135
    private AppCompatButton btnRgbLight; // select RGB color and on/off RGB strip
    private Switch switch_rgbLight; // on/off RGB strip
    private TextView tvTempr2; // Temperature 2 from BMP280
    private TextView tvPressure; // Pressure from BMP280
    private TextView tvPir; // infrared PIR motion sensor
    private ProgressBar pBar; // bluetooth connection progress
    private int active_chart = 1;   // t1 = 1, h = 2, a = 3, t2 = 4, p = 5

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

    /**
     * Establish connection with other device
     */
    public static void connectDevice(Intent data) {
        // Get the device MAC address
        String address = Objects.requireNonNull(data.getExtras())
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
            if (activity != null) {
                activity.finish();
            }
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
            mBluetoothService = new BluetoothService(mHandler);
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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootBF = inflater.inflate(R.layout.fragment_bluetooth, container, false);

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

        FragmentManager chart_fm = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        FragmentTransaction chart_ft = chart_fm.beginTransaction();
        chart_ft.replace(R.id.fragment_for_line_chart, new LineChartForBF())
                .commit();

        cv_t1 = rootBF.findViewById(R.id.cardView_t1);
        cv_t2 = rootBF.findViewById(R.id.cardView_t2);
        cv_h = rootBF.findViewById(R.id.cardView_h);
        cv_a = rootBF.findViewById(R.id.cardView_a);
        cv_p = rootBF.findViewById(R.id.cardView_p);
        cv_pir = rootBF.findViewById(R.id.cardView_pir);

        updateUI();
        cardViewsBackColor(active_chart);

        return rootBF;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

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
                    active_chart = 1;
                    LineChartForBF.graphics(active_chart);
                    cardViewsBackColor(active_chart);
                }
            }
        });

        cv_t2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InputRecognition.getSizeT2() > 0) {
                    active_chart = 4;
                    LineChartForBF.graphics(active_chart);
                    cardViewsBackColor(active_chart);
                }
            }
        });

        cv_h.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InputRecognition.getSizeH() > 0) {
                    active_chart = 2;
                    LineChartForBF.graphics(active_chart);
                    cardViewsBackColor(active_chart);
                }
            }
        });

        cv_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InputRecognition.getSizeA() > 0) {
                    active_chart = 3;
                    LineChartForBF.graphics(active_chart);
                    cardViewsBackColor(active_chart);
                }
            }
        });

        cv_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InputRecognition.getSizeP() > 0) {
                    active_chart = 5;
                    LineChartForBF.graphics(active_chart);
                    cardViewsBackColor(active_chart);
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
                    mBluetoothService = new BluetoothService(mHandler);
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    Objects.requireNonNull(getActivity()).finish();
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
        FragmentManager fmL = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
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

        int t1Size = InputRecognition.getSizeT1();
        if (t1Size > 0) {
            float t1 = InputRecognition.getArrayT1().get(t1Size - 1);
            tvTemp.setText(String.format("%s °C", t1));
            if (active_chart == 1) {
                LineChartForBF.graphics(1);
            }
            // with this implementation i do't have '-1' value from InputRecognition
        }

        int hSize = InputRecognition.getSizeH();
        if (hSize > 0) {
            float h = InputRecognition.getArrayH().get(hSize - 1);
            tvHum.setText(MessageFormat.format("{0} %", h));
            if (active_chart == 2) {
                LineChartForBF.graphics(2);
            }
        }

        int aSize = InputRecognition.getSizeA();
        if (aSize > 0) {
            float a = InputRecognition.getArrayA().get(aSize - 1);
            tvAirq.setText(String.format("%s AirQ", a));
            if (active_chart == 3) {
                LineChartForBF.graphics(3);
            }
        }

        String rgbLight = InputRecognition.rgbLight;
        if (rgbLight.equals("1")) {
            switch_rgbLight.setChecked(true);
        } else {
            switch_rgbLight.setChecked(false);
        }

        String pirSensor = InputRecognition.pirSensor;
        if (pirSensor.equals("1")) {
            tvPir.setText(R.string.pir_on);
            tvPir.setTextColor(Color.RED);
            cv_pir.setCardBackgroundColor(Color.YELLOW);
        } else {
            tvPir.setText(R.string.pir_off);
            tvPir.setTextColor(Color.BLACK);
            cv_pir.setCardBackgroundColor(Color.WHITE);
        }

        int pSize = InputRecognition.getSizeP();
        if (pSize > 0) {
            float p = InputRecognition.getArrayP().get(pSize - 1);
            tvPressure.setText(String.format("%s mm.rs", p));
            if (active_chart == 5) {
                LineChartForBF.graphics(5);
            }
        }

        int t2Size = InputRecognition.getSizeT2();
        if (t2Size > 0) {
            float t2 = InputRecognition.getArrayT2().get(t2Size - 1);
            tvTempr2.setText(String.format("%s °C", t2));
            if (active_chart == 4) {
                LineChartForBF.graphics(4);
            }
        }
    }

    private void cardViewsBackColor(int chart) {
        switch (chart) {
            case 1:
                cv_t1.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                cv_h.setCardBackgroundColor(Color.WHITE);
                cv_a.setCardBackgroundColor(Color.WHITE);
                cv_t2.setCardBackgroundColor(Color.WHITE);
                cv_p.setCardBackgroundColor(Color.WHITE);
                break;
            case 2:
                cv_t1.setCardBackgroundColor(Color.WHITE);
                cv_h.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                cv_a.setCardBackgroundColor(Color.WHITE);
                cv_t2.setCardBackgroundColor(Color.WHITE);
                cv_p.setCardBackgroundColor(Color.WHITE);
                break;
            case 3:
                cv_t1.setCardBackgroundColor(Color.WHITE);
                cv_h.setCardBackgroundColor(Color.WHITE);
                cv_a.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                cv_t2.setCardBackgroundColor(Color.WHITE);
                cv_p.setCardBackgroundColor(Color.WHITE);
                break;
            case 4:
                cv_t1.setCardBackgroundColor(Color.WHITE);
                cv_h.setCardBackgroundColor(Color.WHITE);
                cv_a.setCardBackgroundColor(Color.WHITE);
                cv_t2.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                cv_p.setCardBackgroundColor(Color.WHITE);
                break;
            case 5:
                cv_t1.setCardBackgroundColor(Color.WHITE);
                cv_h.setCardBackgroundColor(Color.WHITE);
                cv_a.setCardBackgroundColor(Color.WHITE);
                cv_t2.setCardBackgroundColor(Color.WHITE);
                cv_p.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
        }
    }
}