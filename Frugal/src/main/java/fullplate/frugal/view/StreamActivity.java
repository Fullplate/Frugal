package fullplate.frugal.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.SortedMap;

import fullplate.frugal.R;
import fullplate.frugal.domain.Entry;
import fullplate.frugal.domain.PeriodSummary;
import fullplate.frugal.domain.PeriodicEntry;
import fullplate.frugal.domain.SingleEntry;
import fullplate.frugal.services.DomainService;
import fullplate.frugal.utilities.ArrayUtils;
import fullplate.frugal.utilities.PixelUtils;

/*
TODO
- ensure proguard is working (requires app signing first)
- remove unused imports
- remember to change .gitconfig
 */

public class StreamActivity extends Activity {

    public static void updateStreamView(Activity activity) {
        ArrayList<PeriodSummary> summaries = DomainService.getService().getSummaries();
        SortedMap<PeriodSummary, ArrayList<Entry>> summaryMap = DomainService.getService().getSummaryMap();

        ExpandableListView stream = (ExpandableListView) activity.findViewById(R.id.stream_explistview);
        ExpandableStreamAdapter streamAdapter = new ExpandableStreamAdapter(activity, R.layout.stream_entry, R.layout.stream_summary, summaries, summaryMap);

        stream.setAdapter(streamAdapter);
        stream.expandGroup(0);
    }

    public static TextView getInputDialogLabel(Context context, String text) {
        TextView label = new TextView(context);
        label.setText(text);
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.font_size_xxl));
        label.setPadding(PixelUtils.dpToPixels(context, 10), PixelUtils.dpToPixels(context, 10), 0, PixelUtils.dpToPixels(context, 10));
        label.setTextColor(context.getResources().getColor(R.color.orange_primary));
        label.setBackgroundColor(context.getResources().getColor(R.color.grey_light2));

        return label;
    }

    private void createClearDataDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);

        LinearLayout holder = new LinearLayout(this);
        holder.setOrientation(LinearLayout.VERTICAL);

        TextView label = getInputDialogLabel(this, "Are you sure?");
        holder.addView(label);

        adb.setView(holder);

        adb.setPositiveButton("Clear Data", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DomainService.getService().clearData();
                StreamActivity.updateStreamView(StreamActivity.this);
                Toast.makeText(StreamActivity.this, "Data cleared!", Toast.LENGTH_SHORT).show();
            }
        });
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        adb.show();
    }

    private void createNewEntryDialog(final String whichAction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout holder = new LinearLayout(this);
        holder.setId(12345);
        holder.setOrientation(LinearLayout.VERTICAL);

        TextView descLabel = getInputDialogLabel(this, "Description");
        TextView amountLabel = getInputDialogLabel(this, "Amount");

        final AutoCompleteTextView descInput = new AutoCompleteTextView(this);
        descInput.setInputType(InputType.TYPE_CLASS_TEXT);
        descInput.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
        descInput.setHint("Enter short description");

        String[] defaultDescs = getResources().getStringArray(R.array.defaultDescriptions);
        String[] userDescs = DomainService.getService().getDescriptions();
        String[] allDescs = ArrayUtils.concatStringArrays(defaultDescs, userDescs);
        String[] uniqueDescs = ArrayUtils.filterUniqueStrings(allDescs);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.simple_dropdown_item, uniqueDescs);
        descInput.setAdapter(adapter);
        descInput.setThreshold(1);

        final EditText amountInput = new EditText(this);
        amountInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        amountInput.setRawInputType(Configuration.KEYBOARD_12KEY);
        amountInput.setFilters(new InputFilter[] { new InputFilter.LengthFilter(5) });
        amountInput.setHint("Enter whole dollar amount");

        holder.addView(descLabel);
        holder.addView(descInput);
        holder.addView(amountLabel);
        holder.addView(amountInput);

        builder.setView(holder);

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    if (whichAction.equals("single")) {
                        Entry e = new SingleEntry(System.currentTimeMillis(), Integer.parseInt(amountInput.getText().toString()), descInput.getText().toString());
                        DomainService.getService().addEntry(e);
                    }
                    else if (whichAction.equals("periodic")) {
                        Entry e = new PeriodicEntry(Integer.parseInt(amountInput.getText().toString()), descInput.getText().toString());
                        DomainService.getService().addEntry(e);
                    }

                    updateStreamView(StreamActivity.this);
                }
                catch (NumberFormatException e) {
                    Toast.makeText(StreamActivity.this, "Invalid input!", Toast.LENGTH_SHORT).show();
                    createNewEntryDialog(whichAction);
                }

                imm.hideSoftInputFromWindow(descInput.getWindowToken(), 0);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                imm.hideSoftInputFromWindow(descInput.getWindowToken(), 0);
                dialog.cancel();
            }
        });

        builder.show();

        // display keyboard automatically
        descInput.requestFocus();
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        // fix for area underneath keyboard being redrawn when keyboard is closed
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    private void setClickListeners() {
        TextView addSingleButton = (TextView) findViewById(R.id.stream_actions_add_single);

        addSingleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewEntryDialog("single");
            }
        });

        TextView addPeriodicButton = (TextView) findViewById(R.id.stream_actions_add_periodic);

        addPeriodicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewEntryDialog("periodic");
            }
        });

        View.OnTouchListener buttonDimmer = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    view.setBackgroundColor(StreamActivity.this.getResources().getColor(R.color.orange_primary_selected));
                }
                else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    view.setBackgroundColor(StreamActivity.this.getResources().getColor(R.color.orange_primary));
                }
                return false;
            }
        };

        addSingleButton.setOnTouchListener(buttonDimmer);
        addPeriodicButton.setOnTouchListener(buttonDimmer);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        DomainService.getService().updatePreferences(sharedPref);

        updateStreamView(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stream);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setClickListeners();

        // force overflow menu on devices with a physical menu button
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        }
        catch (Exception e) {
        }

        // important - initialize the DomainService with entries from database and user preferences
        DomainService.startService(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_statistics) {
            startActivity(new Intent(this, StatisticsActivity.class));
            return true;
        }
        else if (id == R.id.action_preferences) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        else if (id == R.id.action_clear_data) {
            createClearDataDialog();
            return true;
        }
        else if (id == R.id.action_help) {
            Intent intent = new Intent(this, HelpActivity.class);
            intent.putExtra("caller", getIntent().getComponent().getClassName());
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
