package fullplate.frugal.activities;

import android.app.Activity;
import android.os.Bundle;

import fullplate.frugal.R;

public class StatisticsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_statistics);

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
