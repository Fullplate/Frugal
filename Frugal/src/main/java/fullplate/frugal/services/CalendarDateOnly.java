package fullplate.frugal.services;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CalendarDateOnly extends GregorianCalendar {

    public static Calendar getInstance() {
        return resetTimeComponent(Calendar.getInstance());
    }

    public static Calendar getInstanceFromTimeInMillis(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return resetTimeComponent(c);
    }

    public static Calendar addCalendarPeriod(Calendar c, CalendarPeriod cPeriod) {
        c.set(cPeriod.getField(), cPeriod.getAmount());
        return c;
    }

    private static Calendar resetTimeComponent(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, c.getActualMinimum(Calendar.HOUR_OF_DAY));
        c.set(Calendar.MINUTE, c.getActualMinimum(Calendar.MINUTE));
        c.set(Calendar.SECOND, c.getActualMinimum(Calendar.SECOND));
        c.set(Calendar.MILLISECOND, c.getActualMinimum(Calendar.MILLISECOND));
        return c;
    }
}
