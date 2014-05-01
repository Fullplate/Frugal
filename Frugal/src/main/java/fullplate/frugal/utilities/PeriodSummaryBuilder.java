package fullplate.frugal.utilities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.SortedMap;
import java.util.TreeMap;

import fullplate.frugal.domain.Entry;
import fullplate.frugal.domain.PeriodSummary;

public class PeriodSummaryBuilder {

    private SortedMap<PeriodSummary, ArrayList<Entry>> summaries;
    private ArrayList<Entry> entries;

    public PeriodSummaryBuilder(ArrayList<Entry> entries) {
        summaries = new TreeMap<>();
        this.entries = entries;

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

    public void addSingleEntry(Entry e) {
        entries.add(e);
        updateSummaries();
    }

    public void addPeriodicEntry() {
        // todo: complete
    }

    public void removeLastEntry() {
        if (entries.size() > 0) {
            entries.remove(entries.size() - 1);
            updateSummaries();
        }
    }

    private void printSummaries() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");

        for (PeriodSummary summary : getSummaries()) {

            String displayDate = dateFormat.format(summary.getStartTimestamp());
            System.out.println("Summary: "+displayDate+", $"+summary.getCurrentAmount()+"\n");

            for (Entry entry : summaries.get(summary)) {
                System.out.println("entry: "+entry.getDescription());
            }
        }
    }

    // todo: if these are always called together, they could be combined into a single function
    // todo: rebuilding summaries every time is obviously inefficient, but will do for now
    // todo: write to database on update?
    private void updateSummaries() {
        summaries = new TreeMap<>();

        buildSummariesSkeleton();
        fillSummaries();
        calculateAmounts();
    }

    private void buildSummariesSkeleton() {
        int totalAmount = 100; // todo: constructor arg

        long now = System.currentTimeMillis();
        Calendar startMarker = CalendarDateOnly.getInstance();
        startMarker.set(2014, Calendar.JANUARY, 1); // todo: constructor arg, this should be user-set or the first day the user uses the app

        Calendar endMarker = CalendarDateOnly.getInstanceFromTimeInMillis(startMarker.getTimeInMillis());

        while (startMarker.getTimeInMillis() < now) {
            endMarker.add(Calendar.DAY_OF_YEAR, 7); // todo: construct arg, this is the user-set time period
            PeriodSummary newSummary = new PeriodSummary(startMarker.getTimeInMillis(), endMarker.getTimeInMillis(), totalAmount);

            summaries.put(newSummary, new ArrayList<Entry>());

            startMarker.setTimeInMillis(endMarker.getTimeInMillis());
        }
    }

    private void fillSummaries() {
        for (Entry e : entries) {
            for (PeriodSummary s : getSummaries()) {
                if (e.getTimestamp() >= s.getStartTimestamp()) {
                    summaries.get(s).add(e);
                    break;
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

    @Deprecated
    private void buildSummaries() {
        // can probably do helper methods for creating+adding a new PeriodSummary, and for incrementing amount+adding entry
        // we are working in milliseconds now

        int totalAmount = 1000; // todo: constructor arg

        long now = System.currentTimeMillis();

        Calendar startMarker = Calendar.getInstance();

        startMarker.set(2014, Calendar.JANUARY, 1); // todo: constructor arg, allow user to set this

        Calendar endMarker = Calendar.getInstance();
        endMarker.set(2014, Calendar.JANUARY, 1);
        endMarker.add(Calendar.DAY_OF_YEAR, 7); // todo: constructor arg

        PeriodSummary periodSummary = new PeriodSummary(startMarker.getTimeInMillis(), endMarker.getTimeInMillis(), totalAmount);
        summaries.put(periodSummary, new ArrayList<Entry>());

        for (Entry entry : entries) { // assume entries are ordered from oldest to newest, if not then we can easily do this
            if (startMarker.getTimeInMillis() < now) {
                if (entry.getTimestamp() < endMarker.getTimeInMillis()) {
                    summaries.get(periodSummary).add(entry);
                }
                else {
                    startMarker = endMarker; // add 1 ms or something
                    endMarker.add(Calendar.WEEK_OF_MONTH, 1);

                    periodSummary = new PeriodSummary(startMarker.getTimeInMillis(), endMarker.getTimeInMillis(), totalAmount);
                    summaries.put(periodSummary, new ArrayList<Entry>());

                    summaries.get(periodSummary).add(entry);
                }
            }
            else {
                break;
            }
        }

        // calculate currentAmount for each PeriodSummary
        for (PeriodSummary summary : summaries.keySet()) {
            for (Entry entry : summaries.get(summary)) {
                summary.increaseCurrentAmount(entry.getAmount());
            }
        }

        printSummaries();
    }
}
