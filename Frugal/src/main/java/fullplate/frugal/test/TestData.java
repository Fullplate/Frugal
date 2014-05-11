package fullplate.frugal.test;

import java.util.ArrayList;
import java.util.HashMap;

import fullplate.frugal.domain.Entry;
import fullplate.frugal.domain.PeriodicEntry;
import fullplate.frugal.domain.SingleEntry;

public class TestData {

    public static ArrayList<Entry> generateTestEntries() {
        long day = 86400000;

        HashMap<String, Integer> testData = new HashMap<>();
        testData.put("Breakfast", 6);
        testData.put("Lunch", 10);
        testData.put("Dinner", 25);
        testData.put("Drinks", 15);
        testData.put("Clothes", 50);
        testData.put("Charity Donation", 20);
        testData.put("Morning Coffee", 3);
        testData.put("Electricity Bill", 200);

        String[] keys = testData.keySet().toArray(new String[testData.size()]);

        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < 500; i++) {
            long time = generateTestStartTime()+(i*day/2);
            if (time < System.currentTimeMillis()) {
                String item = keys[i % testData.size()];
                int value = testData.get(item);
                entries.add(new SingleEntry(time, value, item));
            }
        }

        entries.add(new PeriodicEntry(200, "Rent"));
        entries.add(new PeriodicEntry(50, "Groceries"));

        return entries;
    }

    public static long generateTestStartTime() {
        return 1396310400000L; // 1st apr '14, midnight
    }

}
