package com.bigId.FileAnalysisService.servicebus;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ServiceBus<T> {

    private BlockingQueue<T> queue ;

    public ServiceBus(){
        this.queue = new LinkedBlockingQueue<>();
    }

    public void put(T m)  {
        try {
            queue.put(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public T poll(int timeoutMs) {
        try {
            return queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }

    public T take() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }


}