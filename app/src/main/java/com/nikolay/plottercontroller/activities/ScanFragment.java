package com.nikolay.plottercontroller.activities;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nikolay.plottercontroller.R;

import java.util.Scanner;

public class ScanFragment extends Fragment {

    private static final String TAG = "Lisko";

    private ProgressBar mProgressBar;
    private TextView mTextView;

    public ScanFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mTextView = getView().findViewById(R.id.label_scan);
        mProgressBar = getView().findViewById(R.id.progressBar);
        super.onViewCreated(view, savedInstanceState);
    }

    public void startScanning() {
        mTextView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void stopScanning() {
        mTextView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    public void setTextConnecting() {
        mTextView.setText("Connecting...");
    }
}


