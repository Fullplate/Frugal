package fullplate.frugal.domain;

abstract public class Entry implements Comparable<Entry>{
    private String description;
    private int amount;
    private final Long timestamp;

    public Entry(String description, int amount, Long timestamp) {
        this.description = description;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public int getAmount() {
        return amount;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    abstract public int compareTo(Entry that);
}
