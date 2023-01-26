package com.schneewittchen.rosandroid.ui.views.details;

import android.view.View;

import com.schneewittchen.rosandroid.model.entities.widgets.BaseEntity;
import com.schneewittchen.rosandroid.viewmodel.DetailsViewModel;

import org.ros2.rcljava.interfaces.MessageDefinition;

import java.util.List;


/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.0.0
 * @created on 26.05.2021
 */
public abstract class PublisherLayerViewHolder extends DetailViewHolder {

    private final LayerViewHolder layerViewHolder;
    private final PublisherViewHolder publisherViewHolder;


    public PublisherLayerViewHolder() {
        this.layerViewHolder = new LayerViewHolder(this);
        this.publisherViewHolder = new PublisherViewHolder(this);
        this.publisherViewHolder.topicTypes = this.getTopicTypes();
    }


    public abstract List<Class<? extends MessageDefinition>> getTopicTypes();


    @Override
    public void setViewModel(DetailsViewModel viewModel) {
        super.setViewModel(viewModel);
        publisherViewHolder.viewModel = viewModel;
    }

    public void baseInitView(View view) {
        layerViewHolder.baseInitView(view);
        publisherViewHolder.baseInitView(view);
    }

    public void baseBindEntity(BaseEntity entity) {
        layerViewHolder.baseBindEntity(entity);
        publisherViewHolder.baseBindEntity(entity);
    }

    public void baseUpdateEntity(BaseEntity entity) {
        layerViewHolder.baseUpdateEntity(entity);
        publisherViewHolder.baseUpdateEntity(entity);
    }
}
