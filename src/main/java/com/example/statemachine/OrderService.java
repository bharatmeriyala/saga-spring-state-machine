package com.example.statemachine;

import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final StateMachineFactory<OrderState, OrderEvent> factory;
    private final Map<String, StateMachine<OrderState, OrderEvent>> orderMachines = new ConcurrentHashMap<>();

    public String createOrder() {
        String orderId = UUID.randomUUID().toString();
        StateMachine<OrderState, OrderEvent> sm = factory.getStateMachine(orderId);
        sm.start();
        orderMachines.put(orderId, sm);
        return orderId;
    }

    public boolean sendEvent(String orderId, OrderEvent event) {
        StateMachine<OrderState, OrderEvent> sm = orderMachines.get(orderId);
        if (sm != null) {
            return sm.sendEvent(event);
        }
        return false;
    }

    public OrderState getOrderState(String orderId) {
        StateMachine<OrderState, OrderEvent> sm = orderMachines.get(orderId);
        return sm != null ? sm.getState().getId() : null;
    }
}
