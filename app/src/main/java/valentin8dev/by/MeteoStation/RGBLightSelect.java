package valentin8dev.by.MeteoStation;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.skydoves.colorpickerview.ColorListener;
import com.skydoves.colorpickerview.ColorPickerView;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;

public class RGBLightSelect extends Fragment {

    private static final String TAG = "RGBLightSelect";

    public static int[] rgbColor;
    public static int rgbColor_i;
    private ColorPickerView mColorPickerView;
    private LinearLayout llColorPicker;
    public SeekBar sSeekBar;
    static float brightness = 70f;
    private TextView tv_sb;

    public static float getBrightness() {
        return brightness;
    }

    public static void setBrightness(float brightness) {
        RGBLightSelect.brightness = brightness;
        Log.d(TAG, "progress: " + String.valueOf(brightness));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set result CANCELED in case the user backs out
        Objects.requireNonNull(getActivity()).setResult(Activity.RESULT_CANCELED);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        llColorPicker = v.findViewById(R.id.colorPickerLl);

        tv_sb = v.findViewById(R.id.seek_bar_progress);

        sSeekBar = v.findViewById(R.id.seekBar_rgb);
        sSeekBar.setProgress((int) getBrightness());
        sSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sSeekBar = seekBar;
                setBrightness(progress);

                tv_sb.setText(String.valueOf(progress));
                setRGBLight(rgbColor, getBrightness());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                sSeekBar = seekBar;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sSeekBar = seekBar;
            }
        });

        return v;
    }

    // TODO: last piked or saved position (color)
    private void setRGBColor(int color) {

        // TODO: add to settings: selector - "add color index/code"
        TextView textView = Objects.requireNonNull(getActivity()).findViewById(R.id.colorPickerTv);
        String rgbColorHtml = mColorPickerView.getColorHtml();
        textView.setText(MessageFormat.format("#{0}", rgbColorHtml));

        // TODO: better color representation
        llColorPicker.setBackgroundColor(color);

        rgbColor = mColorPickerView.getColorRGB();
        rgbColor_i = color;
        //Log.d(TAG, "color " + Arrays.toString(rgbColor));

        setRGBLight(rgbColor, getBrightness());
    }

    private void setRGBLight(int[] color, float progress) {

        // R = 10 255 - 10 000  color[0]
        // G = 11 255 - 11 000  color[1]
        // B = 13 255 - 13 000  color[2],
        // 13 = prefix code for blue pin 9, coz 90255 does't get in to 2 bytes :(

        if (BluetoothService.getmSocket() != null) {
            try {
                OutputStream outputStream = BluetoothService.getmSocket().getOutputStream();

                int[] value = new int[3];
                value[0] = 10000 + (int) (color[0] * progress/100);
                value[1] = 11000 + (int) (color[1] * progress/100);
                value[2] = 13000 + (int) (color[2] * progress/100);

                Log.d(TAG, "RGB: " + Arrays.toString(value));

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
        Objects.requireNonNull(getActivity()).setResult(Activity.RESULT_OK);
        getActivity().onBackPressed();

        // TODO: save last peaked color !
    }
}
