package com.bigId.FileAnalysisService.servicebus;

import lombok.Data;
import lombok.Getter;

@Data
public abstract class Message {

    protected long timestamp ;

    public Message(){
        timestamp = System.currentTimeMillis();
    }



}
