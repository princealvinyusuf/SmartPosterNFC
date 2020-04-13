package com.polibatam.smartposternfc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener{
    private Button btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        if (v.getId() == R.id.btn_close){
            finish();
        }
    }
}
