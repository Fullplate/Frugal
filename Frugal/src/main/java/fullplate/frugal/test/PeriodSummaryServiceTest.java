package fullplate.frugal.test;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.SortedMap;

import fullplate.frugal.domain.Entry;
import fullplate.frugal.domain.PeriodSummary;
import fullplate.frugal.domain.SingleEntry;
import fullplate.frugal.services.PeriodSummaryService;

// todo: this is probably failing because Calendar does not set time fields to 0

@Deprecated
public class PeriodSummaryServiceTest extends TestCase {

    private ArrayList<Entry> entries;
    private ArrayList<PeriodSummary> summaries;
    private SortedMap<PeriodSummary, ArrayList<Entry>> summaryMap;
    Long start;
    Long day;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        entries = new ArrayList<>();

        Calendar c = Calendar.getInstance();
        c.set(2014, Calendar.JANUARY, 1);
        start = c.getTimeInMillis();
        day = 86400000L;

        for (int i = 0; i < 50; i++) {
            entries.add(new SingleEntry(start+(i*day), i, "Description "+Integer.toString(i)));
        }

        PeriodSummaryService summaryBuilder = null; //new PeriodSummaryService(entries);
        summaries = summaryBuilder.getSummaries();
        summaryMap = summaryBuilder.getSummaryMap();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPeriodSummaryNotNull() {
        PeriodSummary actualSummary = null;

        for (PeriodSummary summary : summaries) {
            if (summary.getStartTimestamp().equals(start)) {
                actualSummary = summary;
                break;
            }
        }

        assertNotNull(actualSummary);
    }

    public void testFirstPeriodSummaryCorrectAmount() {
        PeriodSummary expectedSummary = new PeriodSummary(start, start+(day*7), 28);
        PeriodSummary actualSummary = null;

        for (PeriodSummary summary : summaries) {
            if (summary.getStartTimestamp().equals(start)) {
                actualSummary = summary;
                break;
            }
        }

        assertEquals(actualSummary.getCurrentAmount(), expectedSummary.getCurrentAmount());
    }

}
