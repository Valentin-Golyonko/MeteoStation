package valentin8dev.by.MeteoStation;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skydoves.colorpickerview.ColorListener;
import com.skydoves.colorpickerview.ColorPickerView;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class RGBLightSelect extends Fragment {

    private static final String TAG = "RGBLightSelect";

    public static int[] rgbColor;
    public static int rgbColor_i;
    private ColorPickerView mColorPickerView;
    private LinearLayout llColorPicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set result CANCELED in case the user backs out
        getActivity().setResult(Activity.RESULT_CANCELED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.rgb_light_select, container, false);

        mColorPickerView = v.findViewById(R.id.colorPickerView);
        mColorPickerView.setColorListener(new ColorListener() {
            @Override
            public void onColorSelected(int color) {
                setRGBColor(color);
            }
        });

        AppCompatButton btn_ok = v.findViewById(R.id.rgb_btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFragment();
            }
        });

        CardView cv_ok = v.findViewById(R.id.cv_btn_ok);
        cv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFragment();
            }
        });

        llColorPicker = v.findViewById(R.id.colorPickerLl);

        return v;
    }

    // TODO: last piked or saved position (color)
    private void setRGBColor(int color) {

        // TODO: add to setings: selector - "add color index/code"
        TextView textView = getActivity().findViewById(R.id.colorPickerTv);
        String rgbColorHtml = mColorPickerView.getColorHtml();
        textView.setText("#" + rgbColorHtml);

        // TODO: better color representation
        llColorPicker.setBackgroundColor(color);

        rgbColor = mColorPickerView.getColorRGB();
        rgbColor_i = color;
        //Log.d(TAG, "color " + Arrays.toString(rgbColor));

        setRGBLight(rgbColor);
    }

    private void setRGBLight(int[] color) {

        // R = 10 255 - 10 000  color[0]
        // G = 11 255 - 11 000  color[1]
        // B = 13 255 - 13 000  color[2],
        // 13 = prefix code for blue pin 9, coz 90255 does't get in to 2 bytes :(

        if (BluetoothService.getmSocket() != null) {
            try {
                OutputStream outputStream = BluetoothService.getmSocket().getOutputStream();

                int[] value = new int[3];
                value[0] = 10000 + color[0];
                value[1] = 11000 + color[1];
                value[2] = 13000 + color[2];
                Log.d(TAG, Arrays.toString(value));

                // set rgb strip color
                for (int i = 0; i < 3; i++) {
                    byte[] bytes_2 = ByteBuffer.allocate(4).putInt(value[i]).array();
                    //Log.d(TAG, Arrays.toString(bytes_2));

                    for (byte b : bytes_2) {
                        outputStream.write(b);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeFragment() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().onBackPressed();

        // TODO: save last peaked color !
    }
}
