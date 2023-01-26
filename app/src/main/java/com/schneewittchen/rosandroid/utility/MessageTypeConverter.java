package com.schneewittchen.rosandroid.utility;

import org.ros2.rcljava.interfaces.MessageDefinition;

public class MessageTypeConverter {

    public static Class<? extends MessageDefinition> toType(String typeName) {
        switch (typeName) {
            case "geometry_msgs.msg.Twist":
                return geometry_msgs.msg.Twist.class;
        }
        return null;
    }
}
