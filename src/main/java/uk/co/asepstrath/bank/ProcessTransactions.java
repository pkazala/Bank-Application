package uk.co.asepstrath.bank;

import java.util.ArrayList;
import java.util.HashMap;

public class ProcessTransactions {

    private static int noOfTransactions;

    public static int getNoOfTransactions() { return noOfTransactions; }

    public static Account findWithdrawAccount(ArrayList<Account> accounts, String id) {

        for(Account account: accounts) {

            if(account.getId().equals(id)) {

                return account;

            }

        }

        return null;
    }

    public static Account findDepositAccount(ArrayList<Account> accounts, String id) {

        for(Account account: accounts) {

            if(account.getId().equals(id)) {

                return account;

            }

        }

        return null;
    }

    public static HashMap<Transaction, Account> processTransactions(ArrayList<Account> accounts, ArrayList<Transaction> transactions) {

        HashMap<Transaction, Account> completedTransactions = new HashMap<Transaction, Account>();

        for(Transaction transaction: transactions) {

            Account withdrawalAccount = findWithdrawAccount(accounts, transaction.getWithdrawAccount());
            Account depositAccount = findDepositAccount(accounts, transaction.getDepositAccount());

            if(withdrawalAccount == null || depositAccount == null || transaction.getAmount() < 0) {

                continue;

            }

            withdrawalAccount.withdraw(transaction.getAmount());
            withdrawalAccount.setNoOfTransactions(withdrawalAccount.getNoOfTransactions() + 1);

            depositAccount.deposit(transaction.getAmount());
            depositAccount.setNoOfTransactions(depositAccount.getNoOfTransactions() + 1);

            completedTransactions.put(transaction, withdrawalAccount);
            completedTransactions.put(transaction, depositAccount);

            noOfTransactions++;

        }

        return completedTransactions;

    }

}
