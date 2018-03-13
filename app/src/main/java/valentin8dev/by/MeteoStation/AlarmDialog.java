package valentin8dev.by.MeteoStation;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

public class AlarmDialog extends Activity {

    public static String alarmMsg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        TextView tvAlarm = findViewById(R.id.tv_alarm);
        tvAlarm.setText(alarmMsg);
    }
}
