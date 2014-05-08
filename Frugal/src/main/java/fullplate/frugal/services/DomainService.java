package fullplate.frugal.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import fullplate.frugal.database.EntriesTableHandler;
import fullplate.frugal.domain.Entry;
import fullplate.frugal.domain.PeriodSummary;

/*
    This class provides an abstraction over the persistence layer.
    It builds our domain objects into a structure suitable for the view layer, which includes
    handling user preferences.
 */
public class DomainService {

    private static DomainService summaryService = null;

    // ideally this logic would be in getService, but passing Context each time isn't ideal
    // as such, this should be called once only before subsequent calls to getService()
    public static void startService(Context context) {
        EntriesTableHandler entriesTableHandler = new EntriesTableHandler(context);

        try {
            entriesTableHandler.open();
        }
        catch (SQLException e) {
            Log.e(DomainService.class.getCanonicalName(), "Failed to open SQLite database.");
        }

        ArrayList<Entry> entries = entriesTableHandler.getAllEntries();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        long startTime = readStartTimePref(sharedPref, false);
        CalendarPeriod period = readPeriodPref(sharedPref);
        int defaultAmount = readDefaultTargetPref(sharedPref);

        summaryService = new DomainService(entries, startTime, period, defaultAmount);

        summaryService.entriesTableHandler = entriesTableHandler;
        summaryService.sharedPref = sharedPref;
    }

    public static DomainService getService() {
        return summaryService;
    }

    private static CalendarPeriod readPeriodPref(SharedPreferences sharedPref) {
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

        return calendarPeriod;
    }

    private static int readDefaultTargetPref(SharedPreferences sharedPref) {
        // extract "default target amount" from preferences
        int defaultAmountPref = Integer.parseInt(sharedPref.getString("defaultAmountPref", "-1"));

        // if we are not using a default amount, have to manually set the amount to -1
        boolean useDefaultAmount = sharedPref.getBoolean("useDefaultPref", false);
        if (!useDefaultAmount) {
            defaultAmountPref = -1;
        }

        return defaultAmountPref;
    }

    private static long readStartTimePref(SharedPreferences sharedPref, boolean reset) {
        long startTime = sharedPref.getLong("startTimestamp", -1);

        // first-time run or timestamp reset, so we record the current time as our start timestamp
        if (startTime == -1 || reset) {
            SharedPreferences.Editor editor = sharedPref.edit();
            startTime = System.currentTimeMillis();
            editor.putLong("startTimestamp", startTime);
            editor.commit();
        }

        return startTime;
    }


    protected EntriesTableHandler entriesTableHandler;
    protected SharedPreferences sharedPref;

    private SortedMap<PeriodSummary, ArrayList<Entry>> summaries;
    private ArrayList<Entry> entries;
    private long startTime;
    private CalendarPeriod period;
    private int defaultTarget;

    public DomainService(ArrayList<Entry> entries, long startTime, CalendarPeriod period, int defaultTarget) {
        if (summaryService == null) {
            summaryService = this;
        }

        summaries = new TreeMap<>();

        this.entries = entries;
        this.startTime = startTime;
        this.period = period;
        this.defaultTarget = defaultTarget;

        updateSummaries();
    }

    public SortedMap<PeriodSummary, ArrayList<Entry>> getSummaryMap() {
        return summaries;
    }

    public ArrayList<PeriodSummary> getSummaries() {
        return new ArrayList<>(summaries.keySet());
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    public CalendarPeriod getPeriod() {
        return period;
    }

    public void updatePreferences(SharedPreferences sharedPref) {
        this.period = readPeriodPref(sharedPref);
        this.defaultTarget = readDefaultTargetPref(sharedPref);
    }

    public void addEntry(Entry e) {
        entries.add(e);                     // update in-memory entries
        entriesTableHandler.createEntry(e); // update entries table in database

        updateSummaries();
    }

    public void removeEntry(Entry entry) {
        for (int i = 0; i < entries.size(); i++) {
            if (entry.getTimestamp().equals(entries.get(i).getTimestamp())) {
                entriesTableHandler.deleteEntry(entries.get(i));
                entries.remove(i);
                break;
            }
        }

        updateSummaries();
    }

    public void clearData() {
        entries = new ArrayList<>();
        entriesTableHandler.wipeTable();

        // reset start time and record it to preferences
        startTime = readStartTimePref(sharedPref, true);

        updateSummaries();
    }

    public void printSummaries() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");

        for (PeriodSummary summary : getSummaries()) {

            String displayDate = dateFormat.format(summary.getStartTimestamp());
            System.out.println("Summary: "+displayDate+", $"+summary.getCurrentAmount()+"\n");

            for (Entry entry : summaries.get(summary)) {
                System.out.println("entry: "+entry.getDescription()+", timestamp: "+entry.getTimestamp());
            }
        }
    }

    public void updateSummaries() {
        summaries = new TreeMap<>();

        Collections.sort(entries); // entries are sorted from oldest -> newest

        buildSummariesSkeleton();
        fillSummaries();
        calculateAmounts();
    }

    private void buildSummariesSkeleton() {
        long now = System.currentTimeMillis();
        Calendar startMarker = CalendarDateOnly.getInstanceFromTimeInMillis(startTime);
        Calendar endMarker = CalendarDateOnly.getInstanceFromTimeInMillis(startMarker.getTimeInMillis());

        while (startMarker.getTimeInMillis() < now) {
            endMarker.add(period.getField(), period.getAmount());

            PeriodSummary newSummary = new PeriodSummary(startMarker.getTimeInMillis(), endMarker.getTimeInMillis(), defaultTarget);
            summaries.put(newSummary, new ArrayList<Entry>());

            startMarker.setTimeInMillis(endMarker.getTimeInMillis());
        }
    }

    private void fillSummaries() {
        for (Entry e : entries) {
            for (PeriodSummary s : getSummaries()) {
                if (e.getTimestamp() >= s.getStartTimestamp() && e.getTimestamp() <= s.getEndTimestamp()) {
                    summaries.get(s).add(e);
                    break;
                }
                else if (e.getTimestamp() == -1) {
                    summaries.get(s).add(e);
                }
            }
        }
    }

    private void calculateAmounts() {
        for (PeriodSummary summary : summaries.keySet()) {
            for (Entry entry : summaries.get(summary)) {
                summary.increaseCurrentAmount(entry.getAmount());
            }
        }
    }
}
