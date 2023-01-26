package com.schneewittchen.rosandroid.model.repositories.rosRepo.node;

import android.util.Log;

import com.schneewittchen.rosandroid.model.entities.widgets.BaseEntity;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.RosData;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.Topic;

import org.ros2.rcljava.node.BaseComposableNode;

/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.0.0
 * @created on 15.09.20
 */
public abstract class AbstractNode extends BaseComposableNode {

    public static final String TAG = AbstractNode.class.getSimpleName();

    protected Topic topic;
    protected BaseEntity widget;
    protected RosData lastRosData;

    public AbstractNode(Topic topic) {
        // set topic name as node name
        super(topic.name);
    }

    /*

    @Override
    public void onStart(ConnectedNode parentNode) {
        Log.i(TAG, "On Start:  " + topic.name);
    }

    @Override
    public void onShutdown(Node node) {
        Log.i(TAG, "On Shutdown:  " + topic.name);
    }

    @Override
    public void onShutdownComplete(Node node) {
        Log.i(TAG, "On Shutdown Complete: " + topic.name);
    }

    @Override
    public void onError(Node node, Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of(topic.name);
    }

     */

    public Topic getTopic() {
        return this.topic;
    }

    public RosData getLastRosData() {
        return lastRosData;
    }

}
