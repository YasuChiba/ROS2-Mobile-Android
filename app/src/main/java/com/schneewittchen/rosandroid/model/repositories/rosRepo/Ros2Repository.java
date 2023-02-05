package com.schneewittchen.rosandroid.model.repositories.rosRepo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.schneewittchen.rosandroid.model.entities.MasterEntity;
import com.schneewittchen.rosandroid.model.entities.widgets.BaseEntity;
import com.schneewittchen.rosandroid.model.entities.widgets.GroupEntity;
import com.schneewittchen.rosandroid.model.entities.widgets.IPublisherEntity;
import com.schneewittchen.rosandroid.model.entities.widgets.ISilentEntity;
import com.schneewittchen.rosandroid.model.entities.widgets.ISubscriberEntity;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.connection.ConnectionType;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.RosData;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.message.Topic;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.node.AbstractNode;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.node.BaseData;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.node.ManagerNode;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.node.PubNode;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.node.Ros2Service;
import com.schneewittchen.rosandroid.model.repositories.rosRepo.node.SubNode;

import org.ros2.rcljava.RCLJava;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import geometry_msgs.msg.Transform;

public class Ros2Repository implements SubNode.NodeListener{
    private static final String TAG = Ros2Repository.class.getSimpleName();

    private static Ros2Repository instance;

    private Ros2Service ros2Service;

    private final List<BaseEntity> currentWidgets;
    private final HashMap<Topic, AbstractNode> currentNodes;
    private final MutableLiveData<RosData> receivedData;

    private final WeakReference<Context> contextReference;

    private ManagerNode managerNode;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            ros2Service = ((Ros2Service.LocalBinder) binder).getService();
            initStaticNodes();
            registerAllNodes();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private Ros2Repository(Context context) {
        this.contextReference = new WeakReference<>(context);
        this.currentWidgets = new ArrayList<>();
        this.currentNodes = new HashMap<>();
        this.receivedData = new MutableLiveData<>();

        startRos2();
    }

    public static Ros2Repository getInstance(final Context context) {
        if (instance == null) {
            instance = new Ros2Repository(context);
        }

        return instance;
    }

    private void initStaticNodes() {
        Topic tfTopic = new Topic("tf", Transform.class.getCanonicalName());
        SubNode tfNode = new SubNode(this, tfTopic, null);
        currentNodes.put(tfTopic, tfNode);

        Topic tfStaticTopic = new Topic("tf_static", Transform.class.getCanonicalName());
        SubNode tfStaticNode = new SubNode(this, tfStaticTopic, null);
        currentNodes.put(tfStaticTopic, tfStaticNode);

        managerNode = new ManagerNode();
        currentNodes.put(managerNode.getTopic(), managerNode);
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
        //TODO TFMessageのときの条件わけ

        this.receivedData.postValue(message);
    }

    public void publishData(BaseData data) {
        AbstractNode node = currentNodes.get(data.getTopic());

        if (node instanceof PubNode) {
            ((PubNode) node).setData(data);
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
                updateNode(oldWidget, newWidget);

            } else {
                // Node not included in old list
                addNode(newWidget);
            }
        }

        // Delete unused widgets
        for (Long id : widgetCheckMap.keySet()) {
            if (!widgetCheckMap.get(id)) {
                // Node not included in new list
                removeNode(widgetEntryMap.get(id));
            }
        }

        this.currentWidgets.clear();
        this.currentWidgets.addAll(newEntities);
    }

    private AbstractNode addNode(BaseEntity widget) {
        if (widget instanceof ISilentEntity) return null;
        Log.i(TAG, "Add node: " + widget.name);

        // Create a new node from widget
        AbstractNode node;
        if (widget instanceof IPublisherEntity) {
            node = new PubNode(widget.topic, widget);

        } else if (widget instanceof ISubscriberEntity) {
            node = new SubNode(this, widget.topic, widget);

        } else {
            Log.i(TAG, "Widget is either publisher nor subscriber.");
            return null;
        }

        currentNodes.put(node.getTopic(), node);
        this.registerNode(node);

        return node;
    }


    /**
     * Update a widget and its associated Node by ID in the ROS graph.
     *
     * @param oldWidget Old version of the widget
     * @param widget    Widget to update
     */
    private void updateNode(BaseEntity oldWidget, BaseEntity widget) {
        if (widget instanceof ISilentEntity) return;
        Log.i(TAG, "Update Node: " + oldWidget.name);
        this.removeNode(oldWidget);
        this.addNode(widget);
    }

    /**
     * Remove a widget and its associated Node in the ROS graph.
     *
     * @param widget Widget to remove
     */
    private void removeNode(BaseEntity widget) {
        if (widget instanceof ISilentEntity) return;
        Log.i(TAG, "Remove Node: " + widget.name);

        AbstractNode node = this.currentNodes.remove(widget.topic);
        this.unregisterNode(node);
    }


    /**
     *
     * @param node Node to connect
     */
    private void registerNode(AbstractNode node) {
        Log.i(TAG, "Register Node: " + node.getTopic().name);
        ros2Service.registerNode(node);
    }

    private void registerAllNodes() {
        for (AbstractNode node : currentNodes.values()) {
            this.registerNode(node);
        }
    }

    /**
     * Disconnect the node from ROS node graph if a connection to the ROS master is running.
     *
     * @param node Node to disconnect
     */
    private void unregisterNode(AbstractNode node) {
        if (node == null) return;

        Log.i(TAG, "Unregister Node: " + node.getTopic().name);
        ros2Service.unregisterNode(node);
    }

    public LiveData<RosData> getData() {
        return receivedData;
    }

    public HashMap<Topic, AbstractNode> getLastRosData() {
        return currentNodes;
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

    public void updateMaster(MasterEntity master) {
        Log.i(TAG, "Update Master");

        if(master == null) {
            Log.i(TAG, "Master is null");
            return;
        }

        //this.master = master;

        // nodeConfiguration = NodeConfiguration.newPublic(master.deviceIp, getMasterURI());
    }

}
