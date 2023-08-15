package com.smallworld;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            TransactionDataFetcher transactionData = new TransactionDataFetcher("transactions.json");
            System.out.println("Total Transaction Amount: " + transactionData.getTotalTransactionAmount());
            System.out.println("Total Transaction Amount sent by Tom Shelby: " + transactionData.getTotalTransactionAmountSentBy("Tom Shelby"));
            System.out.println("Max Transaction Amount: " + transactionData.getMaxTransactionAmount());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
