package uk.co.asepstrath.bank;

public class Transaction {

    private String withdrawAccount;
    private String depositAccount;
    private String timestamp;
    private String id;
    private float amount;
    private String currency;

    public Transaction(String withdrawAccount, String depositAccount, String timestamp, String id, float amount, String currency) {
        this.withdrawAccount = withdrawAccount;
        this.depositAccount = depositAccount;
        this.timestamp = timestamp;
        this.id = id;
        this.amount = amount;
        this.currency = currency;
    }

    public String getWithdrawAccount() {
        return withdrawAccount;
    }

    public String getDepositAccount() {
        return depositAccount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getId() {
        return id;
    }

    public float getAmount() { return amount; }

    public String getCurrency() {
        return currency;
    }

    @Override
    public String toString(){
        return this.withdrawAccount + " " + this.depositAccount + " " + this.timestamp + " " + this.id + " " + this.amount + " " + this.currency;
    }

}
