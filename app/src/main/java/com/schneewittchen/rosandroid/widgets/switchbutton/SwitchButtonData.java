package com.schneewittchen.rosandroid.widgets.switchbutton;

import com.schneewittchen.rosandroid.model.entities.widgets.BaseEntity;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.node.BaseData;

import org.ros2.rcljava.interfaces.MessageDefinition;
import org.ros2.rcljava.publisher.Publisher;


/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.0.0
 * @created on 10.05.2022
 */
public class SwitchButtonData extends BaseData {

    public boolean pressed;

    public SwitchButtonData(boolean pressed) {
        this.pressed = pressed;
    }

    @Override
    public MessageDefinition toRosMessage(Publisher<MessageDefinition> publisher, BaseEntity widget) {
        std_msgs.msg.Bool message = new std_msgs.msg.Bool();
        message.setData(pressed);
        return message;
    }
}