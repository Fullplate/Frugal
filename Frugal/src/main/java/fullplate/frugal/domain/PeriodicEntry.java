package fullplate.frugal.domain;

import java.util.Calendar;

import fullplate.frugal.services.CalendarPeriod;
import fullplate.frugal.services.PeriodSummaryService;

public class PeriodicEntry extends Entry {

    public PeriodicEntry(String description, int amount) {
        super(description, amount, -1L);
    }

    @Override
    public String getTimestampString() {
        CalendarPeriod period = PeriodSummaryService.getService().getPeriod();

        switch (period.getField()) {
            case Calendar.DAY_OF_YEAR:
                switch (period.getAmount()) {
                    case 7:
                        return "Weekly";
                    case 14:
                        return "Biweekly";
                }
                break;
            case Calendar.MONTH:
                return "Monthly";
        }

        return "";
    }

    @Override
    public int compareTo(Entry that) {
        return 1; // ie, should always be at the end of the list
    }
}
