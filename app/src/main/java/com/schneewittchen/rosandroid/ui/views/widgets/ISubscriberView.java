package com.schneewittchen.rosandroid.ui.views.widgets;

import org.ros2.rcljava.interfaces.MessageDefinition;


/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.0.0
 * @created on 10.03.21
 */
public interface ISubscriberView {

    void onNewMessage(MessageDefinition message);
}
