package com.barmej.apod.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.barmej.apod.R;

public class AboutFragment extends DialogFragment implements View.OnClickListener{

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        rootView.findViewById(R.id.cancel_about).setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.cancel_about:
                dismiss();
                break;
        }
    }
}
