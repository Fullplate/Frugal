package fullplate.frugal.domain;

import fullplate.frugal.services.DomainService;

public class PeriodicEntry extends Entry {

    public PeriodicEntry(int amount, String description) {
        super(-1L, amount, description);
    }

    @Override
    public String getTimestampString() {
        return DomainService.getService().getPeriodString();
    }

    @Override
    public int compareTo(Entry that) {
        return 1; // ie, should always be at the end of the list
    }
}
