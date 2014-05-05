package fullplate.frugal.services;

public class CalendarPeriod {
    private final int field;
    private final int amount;

    public CalendarPeriod(int field, int amount) {
        this.field = field;
        this.amount = amount;
    }

    public int getField() {
        return field;
    }

    public int getAmount() {
        return amount;
    }
}
