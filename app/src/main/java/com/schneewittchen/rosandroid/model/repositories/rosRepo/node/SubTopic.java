package com.schneewittchen.rosandroid.model.repositories.rosRepo.node;

import com.schneewittchen.rosandroid.model.entities.widgets.BaseEntity;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.RosData;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.Topic;
import com.schneewittchen.rosandroid.utility.Utils;

import org.ros2.rcljava.interfaces.MessageDefinition;
import org.ros2.rcljava.subscription.Subscription;

import java.util.Timer;

public class SubTopic extends AbstractTopic {
    protected RosData lastRosData;

    protected Subscription<MessageDefinition> subscription;

    public SubTopic(BaseEntity widget) {
        this.widget = widget;
    }

    public RosData getLastRosData() {
        return lastRosData;
    }
}
