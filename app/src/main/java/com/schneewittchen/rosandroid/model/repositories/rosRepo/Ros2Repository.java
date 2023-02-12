package com.schneewittchen.rosandroid.model.repositories.rosRepo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.schneewittchen.rosandroid.model.entities.widgets.BaseEntity;
import com.schneewittchen.rosandroid.model.entities.widgets.GroupEntity;
import com.schneewittchen.rosandroid.model.entities.widgets.IPublisherEntity;
import com.schneewittchen.rosandroid.model.entities.widgets.ISilentEntity;
import com.schneewittchen.rosandroid.model.entities.widgets.ISubscriberEntity;
import com.schneewittchen.rosandroid.model.entities.widgets.SubscriberLayerEntity;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.RosData;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.Topic;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.node.AbstractTopic;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.node.BaseData;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.node.ManagerNode;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.node.PubTopic;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.node.Ros2Service;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.node.SubTopic;
import com.schneewittchen.rosandroid.utility.ros.geometry.FrameTransformTree;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import geometry_msgs.msg.TransformStamped;
import tf2_msgs.msg.TFMessage;

public class Ros2Repository implements ManagerNode.NodeListener{
    private static final String TAG = Ros2Repository.class.getSimpleName();

    private static Ros2Repository instance;

    private Ros2Service ros2Service;
    private final FrameTransformTree frameTransformTree;

    private final List<BaseEntity> currentWidgets;

    private final HashMap<Topic, AbstractTopic> currentTopics;

    private final MutableLiveData<RosData> receivedData;

    private final WeakReference<Context> contextReference;

    private ManagerNode managerNode;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            ros2Service = ((Ros2Service.LocalBinder) binder).getService();
            initManagerNode();
            initStaticTopics();
            registerAllTopics();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private Ros2Repository(Context context) {
        this.contextReference = new WeakReference<>(context);
        this.currentWidgets = new ArrayList<>();
        this.currentTopics = new HashMap<>();
        this.receivedData = new MutableLiveData<>();
        this.frameTransformTree = TransformProvider.getInstance().getTree();

        startRos2();
    }

    public static Ros2Repository getInstance(final Context context) {
        if (instance == null) {
            instance = new Ros2Repository(context);
        }

        return instance;
    }

    private void initManagerNode() {
        this.managerNode = new ManagerNode(this);
        ros2Service.registerNode(this.managerNode);
    }

    private void initStaticTopics() {
        BaseEntity tfWidget = new SubscriberLayerEntity() {
            @Override
            public boolean equalRosState(BaseEntity other) {
                return super.equalRosState(other);
            }
        };
        tfWidget.topic = new Topic("tf", TFMessage.class.getCanonicalName());
        registerTopic(tfWidget);

        BaseEntity tfStaticWidget = new SubscriberLayerEntity() {
            @Override
            public boolean equalRosState(BaseEntity other) {
                return super.equalRosState(other);
            }
        };
        tfStaticWidget.topic = new Topic("tf_static", TFMessage.class.getCanonicalName());
        registerTopic(tfStaticWidget);
    }

    private void startRos2() {
        Context context = contextReference.get();
        if (context == null) {
            return;
        }

        // Create service intent
        Intent serviceIntent = new Intent(context, Ros2Service.class);

        // Start service and check state
        context.startService(serviceIntent);
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void onNewMessage(RosData message) {
        // Save transforms from tf messages
        if (message.getMessage() instanceof TFMessage) {
            TFMessage tf = (TFMessage) message.getMessage();

            for (TransformStamped transform: tf.getTransforms()) {
                frameTransformTree.update(transform);
            }
        }

        this.receivedData.postValue(message);
    }

    public void publishData(BaseData data) {
        AbstractTopic topic = currentTopics.get(data.getTopic());
        if(topic instanceof PubTopic) {
            ((PubTopic) topic).setData(data);
        }
    }

    /**
     * React on a widget change. If at least one widget is added, deleted or changed this method
     * should be called.
     *
     * @param newWidgets Current list of widgets
     */
    public void updateWidgets(List<BaseEntity> newWidgets) {
        Log.i(TAG, "Update widgets");

        // Unpack widgets as a widget can contain child widgets
        List<BaseEntity> newEntities = new ArrayList<>();
        for (BaseEntity baseEntity : newWidgets) {
            if (baseEntity instanceof GroupEntity) {
                newEntities.addAll(baseEntity.childEntities);
            } else {
                newEntities.add(baseEntity);
            }
        }

        for (BaseEntity baseEntity : newEntities) {
            Log.i(TAG, "Entity: " + baseEntity.name);
        }

        // Compare old and new widget lists
        // Create widget check with ids
        HashMap<Long, Boolean> widgetCheckMap = new HashMap<>();
        HashMap<Long, BaseEntity> widgetEntryMap = new HashMap<>();

        for (BaseEntity oldWidget : currentWidgets) {
            widgetCheckMap.put(oldWidget.id, false);
            widgetEntryMap.put(oldWidget.id, oldWidget);
        }

        for (BaseEntity newWidget : newEntities) {
            if (widgetCheckMap.containsKey(newWidget.id)) {
                // Node included in old and new list

                widgetCheckMap.put(newWidget.id, true);

                // Check if widget has changed
                BaseEntity oldWidget = widgetEntryMap.get(newWidget.id);
                updateTopic(oldWidget, newWidget);

            } else {
                // Node not included in old list
                registerTopic(newWidget);
            }
        }

        // Delete unused widgets
        for (Long id : widgetCheckMap.keySet()) {
            if (!widgetCheckMap.get(id)) {
                // Node not included in new list
                deregisterTopic(widgetEntryMap.get(id));
            }
        }

        this.currentWidgets.clear();
        this.currentWidgets.addAll(newEntities);
    }

    private AbstractTopic registerTopic(BaseEntity widget) {
        if (widget instanceof ISilentEntity) return null;
        Log.i(TAG, "Add topic: " + widget.name);

        // Create a new topic from widget
        AbstractTopic topic;
        if (widget instanceof IPublisherEntity) {
            topic = managerNode.registerPubTopic(widget);

        } else if (widget instanceof ISubscriberEntity) {
            topic = managerNode.registerSubNode(widget);

        } else {
            Log.i(TAG, "Widget is either publisher nor subscriber.");
            return null;
        }

        currentTopics.put(widget.topic, topic);
        return topic;
    }

    private void updateTopic(BaseEntity oldWidget, BaseEntity widget) {
        if (widget instanceof ISilentEntity) return;
        Log.i(TAG, "Update topic: " + oldWidget.name);

        this.deregisterTopic(oldWidget);
        this.registerTopic(widget);
    }


    private void deregisterTopic(BaseEntity widget) {
        if (widget instanceof ISilentEntity) return;
        Log.i(TAG, "Remove topic: " + widget.name);

        AbstractTopic topic = this.currentTopics.remove(widget.topic);
        if(topic instanceof PubTopic) {
            this.managerNode.deregisterPubTopic((PubTopic) topic);
        } else if(topic instanceof  SubTopic) {
            this.managerNode.deregisterSubTopic((SubTopic) topic);
        }
    }

    private void registerAllTopics() {
        for (AbstractTopic topic : currentTopics.values()) {
            this.registerTopic(topic.widget);
        }
    }


    public LiveData<RosData> getData() {
        return receivedData;
    }

    public HashMap<Topic, AbstractTopic> getLastRosData() {
        return currentTopics;
    }

    /**
     * Get a list from the ROS Master with all available topics.
     *
     * @return Topic list
     */
    public List<Topic> getTopicList() {
        List<Topic> topicList = managerNode.getTopics();
        return topicList;
    }
}
