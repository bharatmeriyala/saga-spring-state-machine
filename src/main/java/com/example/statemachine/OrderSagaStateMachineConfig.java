package com.example.statemachine;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
public class OrderSagaStateMachineConfig extends StateMachineConfigurerAdapter<OrderState, OrderEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<OrderState, OrderEvent> states) throws Exception {
        states.withStates()
                .initial(OrderState.ORDER_PLACED)
                .states(EnumSet.allOf(OrderState.class))
                .end(OrderState.DELIVERED)
                .end(OrderState.ROLLBACK);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions) throws Exception {
        transitions
                .withExternal().source(OrderState.ORDER_PLACED)
                .target(OrderState.PAYMENT_VERIFIED).event(OrderEvent.NEXT).action(verifyPayment())

                .and()
                .withExternal().source(OrderState.PAYMENT_VERIFIED)
                .target(OrderState.RESTAURANT_CONFIRMED).event(OrderEvent.NEXT).action(confirmRestaurant())

                .and()
                .withExternal().source(OrderState.RESTAURANT_CONFIRMED)
                .target(OrderState.OUT_FOR_DELIVERY).event(OrderEvent.NEXT).action(startDelivery())

                .and()
                .withExternal().source(OrderState.OUT_FOR_DELIVERY)
                .target(OrderState.DELIVERED).event(OrderEvent.NEXT)

                .and()
                .withExternal().source(OrderState.PAYMENT_VERIFIED)
                .target(OrderState.ROLLBACK).event(OrderEvent.FAILURE).action(rollback("PAYMENT_VERIFIED"))

                .and()
                .withExternal().source(OrderState.RESTAURANT_CONFIRMED)
                .target(OrderState.ROLLBACK).event(OrderEvent.FAILURE).action(rollback("RESTAURANT_CONFIRMED"))

                .and()
                .withExternal().source(OrderState.OUT_FOR_DELIVERY)
                .target(OrderState.ROLLBACK).event(OrderEvent.FAILURE).action(rollback("OUT_FOR_DELIVERY"));
    }

    @Bean
    public Action<OrderState, OrderEvent> verifyPayment() {
        return context -> {
            String orderId = (String) context.getExtendedState().get("orderId", String.class);
            System.out.println("💳 Verifying payment for orderId: " + orderId);
            if (Math.random() < 0.8) {
                context.getStateMachine().sendEvent(OrderEvent.NEXT);
            } else {
                context.getStateMachine().sendEvent(OrderEvent.FAILURE);
            }
        };
    }

    @Bean
    public Action<OrderState, OrderEvent> confirmRestaurant() {
        return context -> {
            String orderId = (String) context.getExtendedState().get("orderId", String.class);
            System.out.println("🍽️ Confirming restaurant for orderId: " + orderId);
            if (Math.random() < 0.8) {
                context.getStateMachine().sendEvent(OrderEvent.NEXT);
            } else {
                context.getStateMachine().sendEvent(OrderEvent.FAILURE);
            }
        };
    }

    @Bean
    public Action<OrderState, OrderEvent> startDelivery() {
        return context -> {
            String orderId = (String) context.getExtendedState().get("orderId", String.class);
            System.out.println("🚚 Starting delivery for orderId: " + orderId);
            if (Math.random() < 0.8) {
                context.getStateMachine().sendEvent(OrderEvent.NEXT);
            } else {
                context.getStateMachine().sendEvent(OrderEvent.FAILURE);
            }
        };
    }

    public Action<OrderState, OrderEvent> rollback(String failedStep) {
        return context -> {
            String orderId = (String) context.getExtendedState().get("orderId", String.class);
            System.out.println("❌ Rolling back orderId: " + orderId + ", due to failure at: " + failedStep);
            switch (failedStep) {
                case "OUT_FOR_DELIVERY" -> System.out.println("🚚 Canceling delivery for orderId: " + orderId);
                case "RESTAURANT_CONFIRMED" -> System.out.println("🍽️ Canceling restaurant order for orderId: " + orderId);
                case "PAYMENT_VERIFIED" -> System.out.println("💳 Refunding payment for orderId: " + orderId);
            }
        };
    }
}
