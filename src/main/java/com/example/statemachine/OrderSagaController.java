package com.example.statemachine;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderSagaController {

    private final StateMachineFactory<OrderState, OrderEvent> factory;
    private final Map<String, StateMachine<OrderState, OrderEvent>> orderMachines = new ConcurrentHashMap<>();

    @PostMapping("/start")
    public ResponseEntity<String> startSaga() {
        String orderId = UUID.randomUUID().toString();
        StateMachine<OrderState, OrderEvent> sm = factory.getStateMachine(orderId);
        sm.getExtendedState().getVariables().put("orderId", orderId);
        sm.start();
        orderMachines.put(orderId, sm);
        sm.sendEvent(MessageBuilder.withPayload(OrderEvent.NEXT).setHeader("orderId", orderId).build());
        return ResponseEntity.ok("Order saga started for orderId: " + orderId);
    }

    @PostMapping("/{orderId}/next")
    public ResponseEntity<String> sendNext(@PathVariable String orderId) {
        StateMachine<OrderState, OrderEvent> sm = orderMachines.get(orderId);
        if (sm != null) {
            sm.sendEvent(MessageBuilder.withPayload(OrderEvent.NEXT).setHeader("orderId", orderId).build());
            return ResponseEntity.ok("NEXT event sent for orderId: " + orderId);
        }
        return ResponseEntity.badRequest().body("OrderId not found");
    }

    @PostMapping("/{orderId}/fail")
    public ResponseEntity<String> sendFail(@PathVariable String orderId) {
        StateMachine<OrderState, OrderEvent> sm = orderMachines.get(orderId);
        if (sm != null) {
            sm.sendEvent(MessageBuilder.withPayload(OrderEvent.FAILURE).setHeader("orderId", orderId).build());
            return ResponseEntity.ok("FAILURE event sent for orderId: " + orderId);
        }
        return ResponseEntity.badRequest().body("OrderId not found");
    }

    @GetMapping("/{orderId}/state")
    public ResponseEntity<String> getState(@PathVariable String orderId) {
        StateMachine<OrderState, OrderEvent> sm = orderMachines.get(orderId);
        if (sm != null) {
            return ResponseEntity.ok("Current state for orderId " + orderId + ": " + sm.getState().getId());
        }
        return ResponseEntity.badRequest().body("OrderId not found");
    }
}