package fullplate.frugal.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.SortedMap;

import fullplate.frugal.R;
import fullplate.frugal.domain.Entry;
import fullplate.frugal.domain.PeriodSummary;
import fullplate.frugal.utilities.PeriodSummaryBuilder;
import fullplate.frugal.utilities.PixelUtils;
import fullplate.frugal.view.ExpandableStreamAdapter;

/*
TODO
- view caching for expandablelistview
- the blue highlight, blue for inputview etc. should be changed to orange
- better styling for buttons
- better styling for headers
- better styling for entries -- see also http://stackoverflow.com/questions/5132699/android-how-to-change-the-position-of-expandablelistview-indicator
- header remains at top of screen and contents scroll (might require different listview)
- persisting data using sharedpreferences or SQLite (ideal). Entries at first then possibly PeriodSummaries.
- preferences & statistics screen...
- icon click for back won't work on 4.0.3

thurs:
- description entry box
- require both an amount and a description to click ok
- suggestions for descriptions (previous input) would be ideal
 */

public class StreamActivity extends Activity {

    private PeriodSummaryBuilder summaryBuilder;

    private static ArrayList<Entry> generateTestEntries() {
        long start = 1388534400L * 1000;
        long day = 86400000;

        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < 200; i++) {
            entries.add(new Entry("Description "+Integer.toString(i), i, start+(i*day)));
        }

        return entries;
    }

    private void updateStreamView() {
        ArrayList<PeriodSummary> summaries = summaryBuilder.getSummaries();
        SortedMap<PeriodSummary, ArrayList<Entry>> summaryMap = summaryBuilder.getSummaryMap();

        ExpandableListView stream = (ExpandableListView) findViewById(R.id.stream_explistview);
        ExpandableStreamAdapter streamAdapter = new ExpandableStreamAdapter(this, R.layout.stream_entry, R.layout.stream_summary, summaries, summaryMap);

        stream.setAdapter(streamAdapter);
        stream.expandGroup(0);
    }

    private void addSingleEntry(String description, int amount) {
        Entry e = new Entry(description, amount, System.currentTimeMillis());

        summaryBuilder.addSingleEntry(e);

        updateStreamView();
    }

    private void addPeriodEntry() {
        // todo
    }

    private void removeLastEntry() {
        summaryBuilder.removeLastEntry();

        updateStreamView();
    }

    private TextView getInputDialogLabel(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.entry_input_font_size));
        label.setPadding(PixelUtils.dpToPixels(this, 10), PixelUtils.dpToPixels(this, 5), 0, PixelUtils.dpToPixels(this, 5));
        label.setTextColor(getResources().getColor(R.color.orange_primary));
        label.setBackgroundColor(getResources().getColor(R.color.entry_background));

        return label;
    }

    private void createInputDialog(final String whichAction) {
        // todo
        // a click on the transparent part closes the dialog but not the keyboard (http://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext)
        // custom keyboard for currency input
        // use AutoCompleteTextView with a dataadapter of previous descriptions

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
                        StreamActivity.this.addSingleEntry(descInput.getText().toString(), Integer.parseInt(amountInput.getText().toString()));
                    }
                    else if (whichAction.equals("periodic")) {
                        // todo
                    }
                }
                catch (NumberFormatException e) {
                    Toast.makeText(StreamActivity.this, "Invalid input!", Toast.LENGTH_SHORT).show();
                    createInputDialog(whichAction);
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
        Button btnSingle = (Button) findViewById(R.id.stream_actions_add_single);

        btnSingle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createInputDialog("single");
            }
        });

        Button btnPeriodic = (Button) findViewById(R.id.stream_actions_add_periodic);

        btnPeriodic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //createInputDialog("periodic");
            }
        });

        Button btnRemoveLast = (Button) findViewById(R.id.stream_actions_remove_last);

        btnRemoveLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeLastEntry();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stream);

        summaryBuilder = new PeriodSummaryBuilder(generateTestEntries());
        updateStreamView();

        setClickListeners();
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
            // todo: send PeriodSummaryBuilder through, or make it globally accessible.
            startActivity(new Intent(this, StatisticsActivity.class));
            return true;
        }
        else if (id == R.id.action_preferences) {
            startActivity(new Intent(this, PreferencesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
