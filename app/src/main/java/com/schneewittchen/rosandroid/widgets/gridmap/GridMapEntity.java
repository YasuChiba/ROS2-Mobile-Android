package com.schneewittchen.rosandroid.widgets.gridmap;


import com.schneewittchen.rosandroid.model.entities.widgets.SubscriberLayerEntity;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.Topic;

import nav_msgs.msg.OccupancyGrid;


/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.0.0
 * @created on 08.03.21
 */
public class GridMapEntity extends SubscriberLayerEntity {

    public GridMapEntity() {
        this.topic = new Topic("/move_base/local_costmap/costmap", OccupancyGrid.class.getCanonicalName());
    }

}