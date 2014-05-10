package fullplate.frugal.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import fullplate.frugal.R;

public class HelpActivity extends Activity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                String className = getIntent().getStringExtra("caller");

                try {
                    Intent intent = new Intent(this, Class.forName(className));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
                catch (ClassNotFoundException e) {
                    NavUtils.navigateUpFromSameTask(this);
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help);

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
