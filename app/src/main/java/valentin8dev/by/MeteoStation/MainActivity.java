package valentin8dev.by.MeteoStation;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    static boolean oGraphicsMenu = false;
    private static int bfId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            FragmentTransaction ftBF = getSupportFragmentManager().beginTransaction();
            BluetoothFragment bf = new BluetoothFragment();
            bfId = bf.getId();
            ftBF.add(R.id.fragment_main, bf, String.valueOf(bfId))
                    .commit();
        }

        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothFragment bf = new BluetoothFragment();
                bf.startSpeechToText();
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    // TODO: reduce multiple opening on clicks (all fragments in BackStack)!!!
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_graphics: {
                openGraphicsMenu();
                return true;
            }
            case R.id.menu_about: {
                openAboutMenu();
                return true;
            }
            case R.id.options_menu_graphics_update: {
                Graphics.graphicGF();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void openGraphicsMenu() {
        FragmentManager fmG = this.getSupportFragmentManager();
        FragmentTransaction ftG = fmG.beginTransaction();
        ftG.replace(R.id.fragment_main, new Graphics(), "Graphics")
                .addToBackStack(null)
                .commit();
    }

    private void openAboutMenu() {
        FragmentManager fmA = this.getSupportFragmentManager();
        FragmentTransaction ftA = fmA.beginTransaction();
        ftA.replace(R.id.fragment_main, new AboutMenu(), "AboutMenu")
                .addToBackStack(null)
                .commit();
    }
}
