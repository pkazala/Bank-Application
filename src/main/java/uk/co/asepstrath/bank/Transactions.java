package uk.co.asepstrath.bank;

public class Transactions {

    private float balanceBefore;
    private float balanceAfter;
    private int noOfTransactions;
    private int noOfFailedTransactions;

    public Transactions (float balanceBefore, float balanceAfter, int noOfTransactions, int noOfFailedTransactions) {

        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.noOfTransactions = noOfTransactions;
        this.noOfFailedTransactions = noOfFailedTransactions;

    }

    public float getBalanceBefore() {
        return this.balanceBefore;
    }

    public float getBalanceAfter() {
        return this.balanceAfter;
    }

    public int getNoOfTransactions() {
        return this.noOfTransactions;
    }

    public int getNoOfFailedTransactions() {
        return this.noOfFailedTransactions;
    }

}
