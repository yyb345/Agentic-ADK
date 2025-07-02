package com.alibaba.dflow.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;

public class LocalMessageChannel implements SubscribableChannel {
    private ArrayList<MessageHandler> handlers = new ArrayList<>();

    public LocalMessageChannel() {}

    @Override
    public boolean subscribe(MessageHandler messageHandler) {
        handlers.add(messageHandler);
        return true;
    }

    @Override
    public boolean unsubscribe(MessageHandler messageHandler) {
        return handlers.remove(messageHandler);
    }

    @Override
    public boolean send(Message<?> message) {
        ((ArrayList<MessageHandler>)handlers.clone()).forEach(x->x.handleMessage(message));
        return true;
    }

    @Override
    public boolean send(Message<?> message, long l) {
        ((ArrayList<MessageHandler>)handlers.clone()).forEach(x->x.handleMessage(message));
        return true;
    }
};