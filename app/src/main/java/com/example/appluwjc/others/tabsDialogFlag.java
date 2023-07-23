package com.example.appluwjc.others;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.appluwjc.R;

public class tabsDialogFlag extends DialogFragment {
    public tabsDialogFlag() {
        super();
    }

    public static tabsDialogFlag newInstance(int myIndex) {
        tabsDialogFlag tabsDialogFlag = new tabsDialogFlag();

        //example of passing args
        Bundle args = new Bundle();
        args.putInt("anIntToSend", myIndex);
        tabsDialogFlag.setArguments(args);

        return tabsDialogFlag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        int myInt = getArguments().getInt("an")

        View view = inflater.inflate(R.layout.tab_dialog, null);

        return view;
    }
}
