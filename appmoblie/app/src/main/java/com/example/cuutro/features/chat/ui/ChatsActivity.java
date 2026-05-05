package com.example.cuutro.features.chat.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cuutro.R;
import com.example.cuutro.app.MyApp;
import com.example.cuutro.features.chat.data.ChatRepository;
import com.example.cuutro.features.chat.ui.controller.ChatsController;

public class ChatsActivity extends AppCompatActivity {

    public static final String EXTRA_REPORT_ID = "extra_chat_report_id";

    @NonNull
    public static Intent createIntent(@NonNull Context context, long reportId) {
        Intent intent = new Intent(context, ChatsActivity.class);
        intent.putExtra(EXTRA_REPORT_ID, reportId);
        return intent;
    }

    private ChatsController chatsController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chats);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MyApp app = (MyApp) getApplication();
        if (app.getAppContainer() == null) {
            Toast.makeText(this, R.string.chat_missing_dependency, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        long reportId = getIntent() == null ? -1L : getIntent().getLongExtra(EXTRA_REPORT_ID, -1L);
        if (reportId <= 0L) {
            Toast.makeText(this, R.string.chat_invalid_report_id, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ChatRepository chatRepository = app.getAppContainer().getChatRepository();
        chatsController = new ChatsController(
                this,
                reportId,
                chatRepository,
                app.getAppContainer().getAuthRepository()
        );
    }

    @Override
    protected void onPause() {
        if (chatsController != null) {
            chatsController.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (chatsController != null) {
            chatsController.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        if (chatsController != null) {
            chatsController.onDestroy();
            chatsController = null;
        }
        super.onDestroy();
    }
}
