package com.schneewittchen.rosandroid.ui.fragments.master;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.schneewittchen.rosandroid.R;
import com.schneewittchen.rosandroid.databinding.FragmentMasterBinding;
import com.schneewittchen.rosandroid.viewmodel.MasterViewModel;

import java.util.ArrayList;


/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.3.0
 * @created on 10.01.2020
 * @updated on 05.10.2020
 * @modified by Nico Studt
 * @updated on 16.11.2020
 * @modified by Nils Rottmann
 * @updated on 13.05.2021
 * @modified by Nico Studt
 */
public class MasterFragment extends Fragment {

    private static final String TAG = MasterFragment.class.getSimpleName();
    private static final long MIN_HELP_TIMESPAM = 10 * 1000;
    protected AutoCompleteTextView ipAddressField;
    protected TextInputLayout ipAddressLayout;
    private MasterViewModel mViewModel;
    private FragmentMasterBinding binding;
    private ArrayList<String> ipItemList;
    private ArrayAdapter<String> ipArrayAdapter;

    public static MasterFragment newInstance() {
        Log.i(TAG, "New Master Fragment");
        return new MasterFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMasterBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = new ViewModelProvider(requireActivity()).get(MasterViewModel.class);

        // Define Views --------------------------------------------------------------
        ipAddressField = getView().findViewById(R.id.ipAddessTextView);
        ipAddressLayout = getView().findViewById(R.id.ipAddessLayout);

        ipItemList = new ArrayList<>();
        ipArrayAdapter = new ArrayAdapter<>(this.getContext(),
                R.layout.dropdown_menu_popup_item, ipItemList);
        ipAddressField.setAdapter(ipArrayAdapter);

        String firstDeviceIp = mViewModel.getIPAddress();
        if (firstDeviceIp != null) {
            ipAddressField.setText(firstDeviceIp, false);
        }

        ipAddressField.setOnClickListener(clickedView -> {
            updateIpSpinner();
            ipAddressField.showDropDown();
        });

        ipAddressLayout.setEndIconOnClickListener(v -> {
            ipAddressField.requestFocus();
            ipAddressField.callOnClick();
        });

        ipAddressField.setOnItemClickListener((parent, view, position, id) -> {
            ipAddressField.clearFocus();
        });

        mViewModel.getCurrentNetworkSSID().observe(getViewLifecycleOwner(),
                networkSSID -> binding.NetworkSSIDText.setText(networkSSID));

    }

    private void updateIpSpinner() {
        ipItemList = new ArrayList<>();
        ipItemList = mViewModel.getIPAddressList();
        ipArrayAdapter.clear();
        ipArrayAdapter.addAll(ipItemList);
    }
}
