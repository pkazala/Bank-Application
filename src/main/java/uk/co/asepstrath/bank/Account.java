package uk.co.asepstrath.bank;

public class Account {

    private float balance;
    private String name;

    public Account() {
        this.balance = 0;
        this.name="";
    }

    public Account(float b) {
        this.balance = b;
        this.name="";
    }
    public Account(String name,float b) {
        this.name=name;
        this.balance = b;
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

    public float getBalance() {
        return this.balance;
    }

    @Override
    public String toString(){
        return  this.name + " &pound;" + this.balance;
    }


}