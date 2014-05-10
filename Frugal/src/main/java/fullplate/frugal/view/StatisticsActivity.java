package fullplate.frugal.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import fullplate.frugal.R;
import fullplate.frugal.domain.Entry;
import fullplate.frugal.domain.PeriodSummary;
import fullplate.frugal.services.DomainService;

public class StatisticsActivity extends Activity {

    private LinkedHashMap<String, String> statistics; // ordered map of label:value
    private String period;

    private void generateStatistics() {
        /*
            Ugly procedural code, but it does the job.
         */

        DomainService service = DomainService.getService();

        period = service.getPeriodString();

        statistics = new LinkedHashMap<>();
        String symbol = this.getResources().getString(R.string.currency_symbol);
        ArrayList<PeriodSummary> summaries = service.getSummaries();
        ArrayList<Entry> entries = service.getEntries();

        // calculate average spent on food-related categories
        Set<String> foodCategories = new HashSet<String>() {{
            add("Food");
            add("Groceries");
            add("Breakfast");
            add("Lunch");
            add("Dinner");
        }};
        int totalFood = 0;
        for (Entry e : entries) {
            if (foodCategories.contains(e.getDescription())) {
                totalFood += e.getAmount();
            }
        }
        int averageFood = totalFood / summaries.size();

        // calculate total and average spent on savings category
        Set<String> savingsCategories = new HashSet<String>() {{
                add("Savings");
        }};
        int totalSavings = 0;
        for (Entry e : entries) {
            if (savingsCategories.contains(e.getDescription())) {
                totalSavings += e.getAmount();
            }
        }
        int averageSavings = totalSavings / summaries.size();

        // calculate average expenses per period
        int totalAmount = 0;
        for (PeriodSummary s : summaries) {
            totalAmount += s.getCurrentAmount();
        }
        int averageAmount = (totalAmount - totalSavings) / summaries.size();

        // calculate average expenses in relation to target
        int targetDifference = service.getDefaultTarget() - averageAmount;
        String targetDifferenceSuffix = "";

        if (targetDifference > 0) {
            targetDifferenceSuffix = " under";
        }
        else if (targetDifference < 0) {
            targetDifference *= -1;
            targetDifferenceSuffix = " over";
        }

        String targetDifferenceString;
        boolean useDefaultTarget = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("useDefaultPref", false);
        if (useDefaultTarget && service.getDefaultTarget() != 0) {
            targetDifferenceString = symbol + Integer.toString(targetDifference) + targetDifferenceSuffix;
        }
        else {
            targetDifferenceString = "-";
        }

        statistics.put("Average expenses, minus savings", symbol + Integer.toString(averageAmount));
        statistics.put("Compared to your target", targetDifferenceString);
        statistics.put("Average spent on food", symbol + Integer.toString(averageFood));
        statistics.put("Average savings", symbol + Integer.toString(averageSavings));
        statistics.put("Savings to date", symbol + Integer.toString(totalSavings));
    }

    private View buildStatisticsView() {
        /*
            Statistics entries (label+value) are added programmatically in an attempt to follow
            DRY principles. We use layout templates as a replacement for using styles, since
            apparently programmatically adding style is not a thing.
         */

        LayoutInflater inflater = getLayoutInflater();
        FrameLayout container = (FrameLayout) inflater.inflate(R.layout.activity_statistics, null);

        TextView header = (TextView) container.findViewById(R.id.activity_statistics_header);
        header.setText(period+" Statistics");

        LinearLayout linearLayout = (LinearLayout) container.findViewById(R.id.activity_statistics_layout);

        for (String label : statistics.keySet()) {
            TextView labelView = (TextView) inflater.inflate(R.layout.statistics_label_template, linearLayout, false);
            TextView valueView = (TextView) inflater.inflate(R.layout.statistics_value_template, linearLayout, false);

            labelView.setText(label);
            valueView.setText(statistics.get(label));

            linearLayout.addView(labelView);
            linearLayout.addView(valueView);
        }

        return container;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.secondary, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_help:
                Intent intent = new Intent(this, HelpActivity.class);
                intent.putExtra("caller", getIntent().getComponent().getClassName());
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        generateStatistics();
        setContentView(buildStatisticsView());

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
