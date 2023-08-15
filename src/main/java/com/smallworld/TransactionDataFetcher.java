package com.smallworld;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smallworld.data.Transaction;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionDataFetcher {
    List<Transaction> transactions;
    public TransactionDataFetcher(String jsonFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        this.transactions = objectMapper.readValue(new File(jsonFilePath), new TypeReference<List<Transaction>>(){});
    }

    /**
     * Returns the sum of the amounts of all transactions
     */
    public double getTotalTransactionAmount() {
        Set<Integer> uniqueTransactionIds = new HashSet<>();
        return transactions.stream()
                .filter(transaction -> uniqueTransactionIds.add(transaction.getMtn()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /**
     * Returns the sum of the amounts of all transactions sent by the specified client
     */
    public double getTotalTransactionAmountSentBy(String senderFullName) {
        Set<Integer> uniqueTransactionIds = new HashSet<>();
        return transactions.stream()
                .filter(transaction -> transaction.getSenderFullName().equals(senderFullName) && uniqueTransactionIds.add(transaction.getMtn()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /**
     * Returns the highest transaction amount
     */
    public double getMaxTransactionAmount() {
        return transactions.stream()
                .mapToDouble(Transaction::getAmount)
                .max()
                .orElse(0.0);
    }

    /**
     * Counts the number of unique clients that sent or received a transaction
     */
    public long countUniqueClients() {
        Set<String> uniqueClients = new HashSet<>();
        for (Transaction transaction : transactions) {
            uniqueClients.add(transaction.getSenderFullName());
            uniqueClients.add(transaction.getBeneficiaryFullName());
        }
        return uniqueClients.size();
    }

    /**
     * Returns whether a client (sender or beneficiary) has at least one transaction with a compliance
     * issue that has not been solved
     */
    public boolean hasOpenComplianceIssues(String clientFullName) {
        return transactions.stream()
                .filter(transaction -> transaction.getSenderFullName().equals(clientFullName) || transaction.getBeneficiaryFullName().equals(clientFullName))
                .anyMatch(transaction -> transaction.getIssueId() != null && !transaction.isIssueSolved());
    }

    /**
     * Returns all transactions indexed by beneficiary name
     */
    public Map<String, List<Transaction>> getTransactionsByBeneficiaryName() {
        return transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getBeneficiaryFullName));
    }

    /**
     * Returns the identifiers of all open compliance issues
     */
    public Set<Integer> getUnsolvedIssueIds() {
        return transactions.stream()
                .filter(transaction -> transaction.getIssueId() != null && !transaction.isIssueSolved())
                .map(Transaction::getIssueId)
                .collect(Collectors.toSet());
    }

    /**
     * Returns a list of all solved issue messages
     */
    public List<String> getAllSolvedIssueMessages() {
        return transactions.stream()
                .filter(transaction -> transaction.getIssueId() != null && transaction.isIssueSolved())
                .map(Transaction::getIssueMessage)
                .collect(Collectors.toList());
    }

    /**
     * Returns the 3 transactions with the highest amount sorted by amount descending
     */
    public List<Transaction> getTop3TransactionsByAmount() {
        Set<Integer> uniqueTransactionIds = new HashSet<>();
        return transactions.stream()
                .filter(transaction -> uniqueTransactionIds.add(transaction.getMtn()))
                .sorted(Comparator.comparingDouble(Transaction::getAmount).reversed())
                .limit(3)
                .collect(Collectors.toList());
    }

    /**
     * Returns the senderFullName of the sender with the most total sent amount
     */
    public Optional<String> getTopSender() {
        Set<Integer> processedTransactionIds = new HashSet<>(); // keep track of processed transactions, so it doesn't get counted twice
        Map<String, Double> senderTotalAmounts = new HashMap<>();
        for (Transaction transaction : transactions) {
            if (!processedTransactionIds.contains(transaction.getMtn())) {
                senderTotalAmounts.put(transaction.getSenderFullName(),
                        senderTotalAmounts.getOrDefault(transaction.getSenderFullName(), 0.0) + transaction.getAmount());
            }
            processedTransactionIds.add(transaction.getMtn());
        }

        return senderTotalAmounts.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey);
    }

}
