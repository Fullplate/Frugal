package fullplate.frugal.domain;

public class PeriodSummary implements Comparable<PeriodSummary> {
    private final Long startTimestamp;
    private final Long endTimestamp;
    private int target;
    private int currentAmount;

    public PeriodSummary(Long startTimestamp, Long endTimestamp, int target) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.target = target;

        this.currentAmount = 0;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    public int getTarget() {
        return target;
    }

    public int getCurrentAmount() {
        return currentAmount;
    }

    public void setTarget(int target) {
        this.target = target;
    }

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
