package com.example.draganddrop;


import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onItemClick(View view) {
        if (view instanceof TextView) {
            Toast.makeText(this, ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
        }
    }
}
