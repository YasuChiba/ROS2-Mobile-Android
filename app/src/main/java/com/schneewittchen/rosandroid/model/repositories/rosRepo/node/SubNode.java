package com.schneewittchen.rosandroid.model.repositories.rosRepo.node;

import com.schneewittchen.rosandroid.model.entities.widgets.BaseEntity;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.RosData;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.Topic;
import com.schneewittchen.rosandroid.utility.Utils;

import org.ros2.rcljava.interfaces.MessageDefinition;
import org.ros2.rcljava.subscription.Subscription;


/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.0.0
 * @created on 16.09.20
 */
public class SubNode extends AbstractNode {

    private final NodeListener listener;
    private Subscription<MessageDefinition> subscription;

    public SubNode(NodeListener listener, Topic topic, BaseEntity widget) {
        super(topic);
        this.topic = topic;
        this.widget = widget;

        this.listener = listener;

        Class<MessageDefinition> t = (Class<MessageDefinition>)Utils.getClassTypeFromAbsoluteClassName(topic.type);
        this.subscription = this.node.createSubscription(t, topic.name, data -> {
            this.listener.onNewMessage(new RosData(topic, data));
        });
    }

    public interface NodeListener {
        void onNewMessage(RosData message);
    }
}
