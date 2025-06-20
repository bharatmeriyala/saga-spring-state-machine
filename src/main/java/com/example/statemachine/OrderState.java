package com.example.statemachine;

public enum OrderState {
    ORDER_PLACED,
    PAYMENT_VERIFIED,
    RESTAURANT_CONFIRMED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    ROLLBACK
}