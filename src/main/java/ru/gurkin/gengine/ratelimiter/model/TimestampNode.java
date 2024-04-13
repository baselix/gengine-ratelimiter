package ru.gurkin.gengine.ratelimiter.model;

import lombok.Data;

@Data
public class TimestampNode {
    private long timestamp;
    private long numberOfAllowed;
    private TimestampNode next;

    public TimestampNode(long timestamp){
        this.timestamp = timestamp;
        this.numberOfAllowed = 0;
        this.next = null;
    }
}
