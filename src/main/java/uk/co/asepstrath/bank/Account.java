package uk.co.asepstrath.bank;

public class Account {
    private String id;
    private String name;
    private float balance;
    private String currency;
    private String accountType;

    public Account(String id, String name, float balance, String currency, String accountType) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.currency = currency;
        this.accountType = accountType;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getBalance() {
        return this.balance;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAccountType() {
        return this.accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    public void withdraw(double amount) {
        if (this.balance - amount >= 0) {
            this.balance -= amount;
        } else {
            throw new ArithmeticException("Amount to withdraw exceeds balance.");
        }
    }

    @Override
    public String toString(){
        return this.id + " " + this.name + " " + this.currency + " " + this.balance + " " + this.accountType;
    }

}