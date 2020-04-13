package com.polibatam.smartposternfc.record;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface ParsedNdefRecord {


    public View getView(Activity activity, LayoutInflater inflater, ViewGroup parent,
                        int offset);

}
