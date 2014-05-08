package fullplate.frugal.domain;

import java.text.SimpleDateFormat;

public class SingleEntry extends Entry {

    public SingleEntry(Long timestamp, int amount, String description) {
        super(timestamp, amount, description);
    }

    @Override
    public String getTimestampString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");
        return dateFormat.format(timestamp);
    }

    @Override
    public int compareTo(Entry that) {
        if (this.getTimestamp().equals(that.getTimestamp())) return 0;

        return (this.getTimestamp() > that.getTimestamp()) ? -1 : 1;
    }
}
