package fullplate.frugal.activities;

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
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.SortedMap;

import fullplate.frugal.R;
import fullplate.frugal.domain.Entry;
import fullplate.frugal.domain.PeriodSummary;
import fullplate.frugal.domain.PeriodicEntry;
import fullplate.frugal.domain.SingleEntry;
import fullplate.frugal.services.CalendarPeriod;
import fullplate.frugal.services.PeriodSummaryService;
import fullplate.frugal.utilities.PixelUtils;
import fullplate.frugal.view.ExpandableStreamAdapter;

/*
TODO
- view caching for expandablelistview
- persisting data using sharedpreferences or SQLite (ideal). Entries at first then possibly PeriodSummaries.
- statistics screen

Dialogs:
- a click on the transparent part closes the dialog but not the keyboard (http://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext)
- custom keyboard for currency input
- use AutoCompleteTextView with a dataadapter of previous descriptions
- since this code is duplicated 4+ times we could wrap it in a class

Misc:
- tidy code
- tidy font sizes/dimens/hardcoded strings etc.
- unify the repetitive stuff?
- different font types?

- since we're only persisting entries, may need to remove the ability to individually set targets
 */

public class StreamActivity extends Activity {

    private static ArrayList<Entry> generateTestEntries() {
        long day = 86400000;

        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < 40; i++) {
            long time = generateTestStartTime()+(i*day);
            if (time < System.currentTimeMillis()) {
                entries.add(new SingleEntry("Description "+Integer.toString(i), i, generateTestStartTime()+(i*day)));
            }
        }

        entries.add(new PeriodicEntry("P1", 10));
        entries.add(new PeriodicEntry("P2", 500));

        return entries;
    }

    private static long generateTestStartTime() {
        return 1396310400L * 1000; // 1st apr '14, midnight
    }

    public static void updateStreamView(Activity activity) {
        ArrayList<PeriodSummary> summaries = PeriodSummaryService.getService().getSummaries();
        SortedMap<PeriodSummary, ArrayList<Entry>> summaryMap = PeriodSummaryService.getService().getSummaryMap();

        ExpandableListView stream = (ExpandableListView) activity.findViewById(R.id.stream_explistview);
        ExpandableStreamAdapter streamAdapter = new ExpandableStreamAdapter(activity, R.layout.stream_entry, R.layout.stream_summary, summaries, summaryMap);

        stream.setAdapter(streamAdapter);
        stream.expandGroup(0);
    }

    private TextView getInputDialogLabel(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.entry_input_font_size));
        label.setPadding(PixelUtils.dpToPixels(this, 10), PixelUtils.dpToPixels(this, 10), 0, PixelUtils.dpToPixels(this, 10));
        label.setTextColor(getResources().getColor(R.color.orange_primary));
        label.setBackgroundColor(getResources().getColor(R.color.grey_light2));

        return label;
    }

    private void createClearDataDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);

        LinearLayout holder = new LinearLayout(this);
        holder.setOrientation(LinearLayout.VERTICAL);

        TextView label = getInputDialogLabel("Are you sure?");
        holder.addView(label);

        adb.setView(holder);

        adb.setPositiveButton("Clear Data", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                PeriodSummaryService.getService().clearData();
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
        holder.setOrientation(LinearLayout.VERTICAL);

        TextView descLabel = getInputDialogLabel("Description");
        TextView amountLabel = getInputDialogLabel("Amount");

        final EditText descInput = new EditText(this);
        descInput.setInputType(InputType.TYPE_CLASS_TEXT);
        descInput.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
        descInput.setHint("Enter short description");

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
                        Entry e = new SingleEntry(descInput.getText().toString(), Integer.parseInt(amountInput.getText().toString()), System.currentTimeMillis());
                        PeriodSummaryService.getService().addEntry(e);
                    }
                    else if (whichAction.equals("periodic")) {
                        Entry e = new PeriodicEntry(descInput.getText().toString(), Integer.parseInt(amountInput.getText().toString()));
                        PeriodSummaryService.getService().addEntry(e);
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
    }

    private void updatePeriodSummaryService() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // extract CalendarPeriod from preferences
        String periodPref = sharedPref.getString("periodPref", "");
        CalendarPeriod calendarPeriod;

        switch (Integer.parseInt(periodPref)) {
            case 0:
                calendarPeriod = new CalendarPeriod(Calendar.DAY_OF_YEAR, 7);
                break;
            case 1:
                calendarPeriod = new CalendarPeriod(Calendar.DAY_OF_YEAR, 14);
                break;
            case 2:
                calendarPeriod = new CalendarPeriod(Calendar.MONTH, 1);
                break;
            default:
                calendarPeriod = new CalendarPeriod(Calendar.DAY_OF_YEAR, 7);
        }

        // extract "default target amount" from preferences
        int defaultAmountPref = Integer.parseInt(sharedPref.getString("defaultAmountPref", "-1"));

        // if we are not using a default amount, have to manually set the amount to -1
        boolean useDefaultAmount = sharedPref.getBoolean("useDefaultPref", false);
        if (!useDefaultAmount) {
            defaultAmountPref = -1;
        }

        PeriodSummaryService service;

        // todo: this is dodgy, but will do until we use a datastore to initialize the service
        if (PeriodSummaryService.getService() == null) {
            service = new PeriodSummaryService(generateTestEntries(), generateTestStartTime(), calendarPeriod, defaultAmountPref);
        }
        else {
            service = PeriodSummaryService.getService();
            service.setPeriod(calendarPeriod);
            service.setDefaultTarget(defaultAmountPref);
            service.updateSummaries();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        updatePeriodSummaryService();

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
        return super.onOptionsItemSelected(item);
    }
}
