package fullplate.frugal.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import fullplate.frugal.domain.Entry;
import fullplate.frugal.domain.PeriodSummary;
import fullplate.frugal.domain.SingleEntry;

public class PeriodSummaryService {

    // todo: this class should probably be instantiated at static time from a database, and have a private constructor

    private static PeriodSummaryService summaryService = null;
    public static PeriodSummaryService getService()
    {
        return summaryService;
    }

    private SortedMap<PeriodSummary, ArrayList<Entry>> summaries;
    private ArrayList<Entry> entries;
    private long startTime;
    private CalendarPeriod period;
    private int defaultTarget;

    public PeriodSummaryService(ArrayList<Entry> entries, long startTime, CalendarPeriod period, int defaultTarget) {
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

    public void setPeriod(CalendarPeriod period) {
        this.period = period;
    }

    public void setDefaultTarget(int defaultTarget) {
        this.defaultTarget = defaultTarget;
    }

    public void setSpecificTarget(PeriodSummary summary, int target) {
        // linear search is fine since summaries are ordered from newest to oldest
        for (PeriodSummary s : summaries.keySet()) {
            if (s.equals(summary)) {
                s.setTarget(target);
            }
        }
    }

    public void addEntry(Entry e) {
        entries.add(e);
        updateSummaries();
    }

    public void removeEntry(Entry entry) {
        for (int i = 0; i < entries.size(); i++) {
            if (entry.getTimestamp().equals(entries.get(i).getTimestamp())) {
                entries.remove(i);
                break;
            }
        }

        updateSummaries();
    }

    public void removeLatestEntry() {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i) instanceof SingleEntry) {
                entries.remove(i);
                break;
            }
        }

        updateSummaries();
    }

    public void clearData() {
        entries = new ArrayList<>();
        startTime = System.currentTimeMillis();
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

    // todo: write to database on update?
    // we assume entries are ordered from oldest->newest
    public void updateSummaries() {
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
