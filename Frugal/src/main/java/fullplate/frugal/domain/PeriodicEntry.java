package fullplate.frugal.domain;

import java.util.Calendar;

import fullplate.frugal.services.CalendarPeriod;
import fullplate.frugal.services.DomainService;

public class PeriodicEntry extends Entry {

    public PeriodicEntry(int amount, String description) {
        super(-1L, amount, description);
    }

    @Override
    public String getTimestampString() {
        CalendarPeriod period = DomainService.getService().getPeriod();

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
