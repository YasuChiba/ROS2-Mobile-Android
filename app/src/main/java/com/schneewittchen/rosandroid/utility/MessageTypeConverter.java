package com.schneewittchen.rosandroid.utility;

import org.ros2.rcljava.interfaces.MessageDefinition;

public class MessageTypeConverter {

    public static Class<? extends MessageDefinition> toType(String typeName) {
        switch (typeName) {
            case "geometry_msgs.msg/Twist":
            case "geometry_msgs/Twist":
                return geometry_msgs.msg.Twist.class;
            case "geometry_msgs.msg.Transform":
                return geometry_msgs.msg.Transform.class;
        }
        return null;
    }
}
