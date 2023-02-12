package com.schneewittchen.rosandroid.model.repositories.rosRepo.node;

import com.schneewittchen.rosandroid.model.entities.widgets.BaseEntity;
import com.schneewittchen.rosandroid.model.entities.widgets.PublisherLayerEntity;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.RosData;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.Topic;

import org.ros2.rcljava.interfaces.MessageDefinition;
import org.ros2.rcljava.publisher.Publisher;

import java.util.Timer;
import java.util.TimerTask;

public class PubTopic extends AbstractTopic {

    protected Publisher<MessageDefinition> publisher;
    private BaseData lastData;
    private Timer pubTimer;
    private long pubPeriod = 100L;
    private boolean immediatePublish = true;

    public PubTopic(BaseEntity widget) {
        this.widget = widget;

        if (!(widget instanceof PublisherLayerEntity)) {
            return;
        }
        PublisherLayerEntity pubEntity = (PublisherLayerEntity) widget;
        this.setImmediatePublish(pubEntity.immediatePublish);
        this.setFrequency(pubEntity.publishRate);
    }


    /**
     * Call this method to publish a ROS message.
     *
     * @param data Data to publish
     */
    public void setData(BaseData data) {
        this.lastData = data;

        if (immediatePublish) {
            publish();
        }
    }

    /**
     * Set publishing frequency.
     * E.g. With a value of 10 the node will publish 10 times per second.
     *
     * @param hz Frequency in hertz
     */
    public void setFrequency(float hz) {
        this.pubPeriod = (long) (1000 / hz);
    }

    /**
     * Enable or disable immediate publishing.
     * In the enabled state the node will create und send a ros message as soon as
     *
     * @param flag Enable immediate publishing
     * @link #setData(Object) is called.
     */
    public void setImmediatePublish(boolean flag) {
        this.immediatePublish = flag;
    }

    public void createAndStartSchedule() {
        if (pubTimer != null) {
            pubTimer.cancel();
        }

        if (immediatePublish) {
            return;
        }

        pubTimer = new Timer();
        pubTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                publish();
            }
        }, pubPeriod, pubPeriod);
    }

    private void publish() {
        if (publisher == null) {
            return;
        }
        if (lastData == null) {
            return;
        }

        MessageDefinition message = lastData.toRosMessage(publisher, widget);
        publisher.publish(message);
    }

}
