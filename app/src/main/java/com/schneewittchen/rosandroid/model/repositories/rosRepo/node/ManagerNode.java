package com.schneewittchen.rosandroid.model.repositories.rosRepo.node;

import com.schneewittchen.rosandroid.model.entities.widgets.BaseEntity;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.RosData;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.Topic;
import com.schneewittchen.rosandroid.utility.Utils;

import org.ros2.rcljava.graph.NameAndTypes;
import org.ros2.rcljava.interfaces.MessageDefinition;
import org.ros2.rcljava.node.BaseComposableNode;
import org.ros2.rcljava.publisher.Publisher;
import org.ros2.rcljava.subscription.Subscription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ManagerNode extends BaseComposableNode {

    private NodeListener listener;

    public ManagerNode(NodeListener listener) {
        super("ros2MobileAndroidManager");
        this.listener = listener;
    }

    // get all topics in the network
    public List<Topic> getTopics() {
        List<Topic> topics = new ArrayList<>();

        Collection<NameAndTypes> namesAndTypes = this.node.getTopicNamesAndTypes();
        for (NameAndTypes n : namesAndTypes) {
            String type = n.types.iterator().next();
            Topic topic = new Topic(n.name, type);
            topics.add(topic);
        }
        return topics;
    }

    public PubTopic registerPubTopic(BaseEntity widget) {
        PubTopic pubTopic = new PubTopic(widget);

        Class<MessageDefinition> t = (Class<MessageDefinition>) Utils.getClassTypeFromAbsoluteClassName(widget.topic.type);
        pubTopic.publisher = this.node.createPublisher(t, widget.topic.name);
        return pubTopic;
    }

    public SubTopic registerSubNode(BaseEntity widget) {
        SubTopic subTopic = new SubTopic(widget);

        Class<MessageDefinition> t = (Class<MessageDefinition>)Utils.getClassTypeFromAbsoluteClassName(widget.topic.type);
        subTopic.subscription = this.node.createSubscription(t, widget.topic.name, data -> {
            subTopic.lastRosData = new RosData(widget.topic, data);
            this.listener.onNewMessage(new RosData(widget.topic, data));
        });
        return subTopic;
    }

    public void deregisterPubTopic(PubTopic pubTopic) {
        this.node.removePublisher(pubTopic.publisher);
    }

    public void deregisterSubTopic(SubTopic subTopic) {
        this.node.removeSubscription(subTopic.subscription);
    }

    public interface NodeListener {
        void onNewMessage(RosData message);
    }

}
