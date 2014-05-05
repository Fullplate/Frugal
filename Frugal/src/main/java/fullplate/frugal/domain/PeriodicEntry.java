package fullplate.frugal.domain;

public class PeriodicEntry extends Entry {

    public PeriodicEntry(String description, int amount) {
        super(description, amount, -1L);
    }

    @Override
    public int compareTo(Entry that) {
        return 1; // ie, should always be at the end of the list
    }
}
