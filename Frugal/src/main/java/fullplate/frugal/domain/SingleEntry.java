package fullplate.frugal.domain;

public class SingleEntry extends Entry {

    public SingleEntry(String description, int amount, Long timestamp) {
        super(description, amount, timestamp);
    }

    @Override
    public int compareTo(Entry that) {
        if (this.getTimestamp().equals(that.getTimestamp())) return 0;

        return (this.getTimestamp() > that.getTimestamp()) ? -1 : 1;
    }
}
