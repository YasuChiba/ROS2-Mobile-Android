package com.schneewittchen.rosandroid.widgets.button;

import com.schneewittchen.rosandroid.model.entities.widgets.BaseEntity;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.node.BaseData;


import org.ros2.rcljava.interfaces.MessageDefinition;
import org.ros2.rcljava.publisher.Publisher;

/**
 * TODO: Description
 *
 * @author Dragos Circa
 * @version 1.0.0
 * @created on 02.11.2020
 * @updated on 18.11.2020
 * @modified by Nils Rottmann
 */

public class ButtonData extends BaseData {

    public boolean pressed;

    public ButtonData(boolean pressed) {
        this.pressed = pressed;
    }

    @Override
    public MessageDefinition toRosMessage(Publisher<MessageDefinition> publisher, BaseEntity widget) {
        std_msgs.msg.Bool message = new std_msgs.msg.Bool();
        message.setData(pressed);
        return message;
    }
}