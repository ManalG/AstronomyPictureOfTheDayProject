package com.barmej.apod.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.barmej.apod.Constants;
import com.barmej.apod.R;


public class DatePickerFragment extends DialogFragment implements View.OnClickListener {

    private DatePicker datePicker;
    private Button okButton;
    private Button cancelButton;

    private static String date;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.date_picker_layout, container, false);

        datePicker = (DatePicker) rootView.findViewById(R.id.date_picker);
        okButton = (Button) rootView.findViewById(R.id.ok_action);
        cancelButton = (Button) rootView.findViewById(R.id.cancel_action);

        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.ok_action:
                int year = datePicker.getYear();
                int month = datePicker.getMonth() + 1;
                int day = datePicker.getDayOfMonth();
                date = year + "-" + month + "-" + day;
                saveDate();
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().onBackPressed();
                break;
            case R.id.cancel_action:
                getActivity().onBackPressed();
                break;
        }
    }

    private void saveDate() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Constants.APP_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.DATE, date);
        editor.apply();
    }

}


