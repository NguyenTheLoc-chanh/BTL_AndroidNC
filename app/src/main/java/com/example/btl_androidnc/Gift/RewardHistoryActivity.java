package com.example.btl_androidnc.Gift;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.btl_androidnc.R;

public class RewardHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_history);

        ImageView imgNextLeft = findViewById(R.id.imgNextLeft);
        imgNextLeft.setOnClickListener(v -> finish());

        // Tải RewardHistoryFragment mặc định
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new RewardHistoryFragment())
                .commit();
    }
}