package fullplate.frugal.test;

import java.util.ArrayList;

import fullplate.frugal.domain.Entry;
import fullplate.frugal.domain.PeriodicEntry;
import fullplate.frugal.domain.SingleEntry;

public class TestData {

    private static ArrayList<Entry> generateTestEntries() {
        long day = 86400000;

        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < 40; i++) {
            long time = generateTestStartTime()+(i*day);
            if (time < System.currentTimeMillis()) {
                entries.add(new SingleEntry(generateTestStartTime()+(i*day), i, "Description "+Integer.toString(i)));
            }
        }

        entries.add(new PeriodicEntry(10, "P1"));
        entries.add(new PeriodicEntry(500, "P2"));

        return entries;
    }

    private static long generateTestStartTime() {
        return 1396310400000L; // 1st apr '14, midnight
    }

}
