package com.alibaba.dflow.config;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;

public class MockMessageChannel implements SubscribableChannel {
    private ArrayList<MessageHandler> handlers = new ArrayList<>();

    public ScheduledExecutorService executors = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build());


    public MockMessageChannel() {}

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

        ((ArrayList<MessageHandler>)handlers.clone()).forEach(x->executors.execute(()-> {
            try {
                Thread.sleep(new Random().nextInt(10));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x.handleMessage(message);
        }));
        return true;
    }

    @Override
    public boolean send(Message<?> message, long l) {
        return send(message);
    }
};