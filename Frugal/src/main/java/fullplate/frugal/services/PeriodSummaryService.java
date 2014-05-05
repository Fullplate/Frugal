package fullplate.frugal.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import fullplate.frugal.domain.Entry;
import fullplate.frugal.domain.PeriodSummary;

public class PeriodSummaryService {

    private SortedMap<PeriodSummary, ArrayList<Entry>> summaries;
    private ArrayList<Entry> entries;
    private long startTime;
    private CalendarPeriod period;
    private int defaultAmount;

    public PeriodSummaryService(ArrayList<Entry> entries, long startTime, CalendarPeriod period, int defaultAmount) {
        summaries = new TreeMap<>();

        this.entries = entries;
        this.startTime = startTime;
        this.period = period;
        this.defaultAmount = defaultAmount;

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

    public void addEntry(Entry e) {
        entries.add(e);
        updateSummaries();
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
    // we assume entries are ordered from oldest->newest
    private void updateSummaries() {
        summaries = new TreeMap<>();

        Collections.sort(entries);

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

            PeriodSummary newSummary = new PeriodSummary(startMarker.getTimeInMillis(), endMarker.getTimeInMillis(), defaultAmount);
            summaries.put(newSummary, new ArrayList<Entry>());

            startMarker.setTimeInMillis(endMarker.getTimeInMillis());
        }
    }

    private void fillSummaries() {
        for (Entry e : entries) {
            for (PeriodSummary s : getSummaries()) {
                if (e.getTimestamp() >= s.getStartTimestamp() || e.getTimestamp() == -1) {
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
}
