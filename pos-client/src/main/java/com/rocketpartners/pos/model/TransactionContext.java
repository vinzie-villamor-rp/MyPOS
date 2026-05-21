package com.rocketpartners.pos.model;

public class TransactionContext {

    private static Transaction current = Transaction.builder().build();

    private TransactionContext() {}

    public static Transaction get() {
        return current;
    }

    public static void reset() {
        current = Transaction.builder().build();
    }
}
