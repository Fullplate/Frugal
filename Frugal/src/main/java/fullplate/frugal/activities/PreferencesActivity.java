package fullplate.frugal.activities;

import android.app.Activity;
import android.os.Bundle;

import fullplate.frugal.R;

public class PreferencesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preferences);

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
