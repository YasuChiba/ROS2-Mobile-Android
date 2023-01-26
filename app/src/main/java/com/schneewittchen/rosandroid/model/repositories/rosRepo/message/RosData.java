package com.schneewittchen.rosandroid.model.repositories.rosRepo.message;

import org.ros2.rcljava.interfaces.MessageDefinition;

/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.0.0
 * @created on 21.09.20
 * @updated on
 * @modified by
 */
public class RosData {

    private final Topic topic;
    private final MessageDefinition message;


    public RosData(Topic topic, MessageDefinition message) {
        this.topic = topic;
        this.message = message;
    }


    public Topic getTopic() {
        return this.topic;
    }

    public MessageDefinition getMessage() {
        return this.message;
    }
}
