package com.schneewittchen.rosandroid.model.repositories.rosRepo.node;

import com.schneewittchen.rosandroid.model.entities.widgets.BaseEntity;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.Topic;

import org.ros2.rcljava.interfaces.MessageDefinition;
import org.ros2.rcljava.publisher.Publisher;


public abstract class BaseData {

    protected Topic topic;

    public Topic getTopic() {
        return this.topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public MessageDefinition toRosMessage(Publisher<MessageDefinition> publisher, BaseEntity widget) {
        return null;
    }
}
