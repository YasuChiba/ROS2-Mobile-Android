package com.schneewittchen.rosandroid.widgets.switchbutton;


import com.schneewittchen.rosandroid.model.entities.widgets.PublisherWidgetEntity;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.Topic;

import std_msgs.msg.Bool;


/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.0.0
 * @created on 10.05.2022
 */
public class SwitchButtonEntity extends PublisherWidgetEntity {

    public String text;


    public SwitchButtonEntity() {
        this.width = 2;
        this.height = 1;
        this.topic = new Topic("switch_state", Bool.class.getCanonicalName());
        this.immediatePublish = true;
        this.text = "Switch";
    }

}