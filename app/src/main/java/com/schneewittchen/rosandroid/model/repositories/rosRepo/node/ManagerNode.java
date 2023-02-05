package com.schneewittchen.rosandroid.model.repositories.rosRepo.node;

import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.Topic;

import org.ros2.rcljava.graph.NameAndTypes;
import org.ros2.rcljava.node.BaseComposableNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ManagerNode extends AbstractNode {
    public ManagerNode() {
        super(new Topic("ros2MobileAndroidManager", std_msgs.msg.String.class.getCanonicalName()));
        this.topic = new Topic("ros2MobileAndroidManager", std_msgs.msg.String.class.getCanonicalName());
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
}
