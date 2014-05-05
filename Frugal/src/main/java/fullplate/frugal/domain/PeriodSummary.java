package fullplate.frugal.domain;

public class PeriodSummary implements Comparable<PeriodSummary> {
    private final Long startTimestamp;
    private final Long endTimestamp;
    private int maximumAmount;
    private int currentAmount;

    public PeriodSummary(Long startTimestamp, Long endTimestamp, int maximumAmount) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.maximumAmount = maximumAmount;

        this.currentAmount = 0;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    public int getMaximumAmount() {
        return maximumAmount;
    }

    public int getCurrentAmount() {
        return currentAmount;
    }

    // total amount can be set independently for each period
    public void increaseCurrentAmount(int amount) {
        this.currentAmount += amount;
    }

    @Override
    public int hashCode() {
        return startTimestamp.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PeriodSummary) {
            if (startTimestamp.equals(((PeriodSummary) o).getStartTimestamp())) { // maybe just use our hashCode method defined above
                return true;
            }
        }

        return false;
    }

    @Override
    public int compareTo(PeriodSummary that) {
        if (this.getStartTimestamp().equals(that.getStartTimestamp())) return 0;

        return (this.getStartTimestamp() > that.getStartTimestamp()) ? -1 : 1;
    }
}
