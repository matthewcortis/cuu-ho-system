package com.example.cuutro.app;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.cuutro.core.auth.AuthSessionManager;
import com.example.cuutro.core.network.NetworkCallExecutor;
import com.example.cuutro.core.network.NetworkClientFactory;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.auth.data.remote.AuthApiService;
import com.example.cuutro.features.chat.data.ChatRepository;
import com.example.cuutro.features.chat.data.remote.ChatApiService;
import com.example.cuutro.features.profile.data.ProfileRepository;
import com.example.cuutro.features.profile.data.remote.ProfileApiService;
import com.example.cuutro.features.report.data.ReportRepository;
import com.example.cuutro.features.report.data.remote.ReportApiService;
import com.example.cuutro.features.sos.data.SosRepository;
import com.example.cuutro.features.sos.data.remote.SosApiService;

import retrofit2.Retrofit;

public class AppContainer {

    private final AuthRepository authRepository;
    private final ProfileRepository profileRepository;
    private final SosRepository sosRepository;
    private final ReportRepository reportRepository;
    private final ChatRepository chatRepository;

    public AppContainer(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        AuthSessionManager authSessionManager = new AuthSessionManager(appContext);
        NetworkCallExecutor networkCallExecutor = new NetworkCallExecutor();

        Retrofit retrofit = NetworkClientFactory.createRetrofit(authSessionManager);
        AuthApiService authApiService = retrofit.create(AuthApiService.class);
        ProfileApiService profileApiService = retrofit.create(ProfileApiService.class);
        SosApiService sosApiService = retrofit.create(SosApiService.class);
        ReportApiService reportApiService = retrofit.create(ReportApiService.class);
        ChatApiService chatApiService = retrofit.create(ChatApiService.class);

        authRepository = new AuthRepository(authApiService, authSessionManager, networkCallExecutor);
        profileRepository = new ProfileRepository(
                appContext,
                profileApiService,
                authRepository,
                networkCallExecutor
        );
        sosRepository = new SosRepository(sosApiService, authRepository, networkCallExecutor);
        reportRepository = new ReportRepository(
                appContext,
                reportApiService,
                authRepository,
                networkCallExecutor
        );
        chatRepository = new ChatRepository(
                appContext,
                chatApiService,
                authRepository,
                networkCallExecutor
        );
    }

    @NonNull
    public AuthRepository getAuthRepository() {
        return authRepository;
    }

    @NonNull
    public ProfileRepository getProfileRepository() {
        return profileRepository;
    }

    @NonNull
    public SosRepository getSosRepository() {
        return sosRepository;
    }

    @NonNull
    public ReportRepository getReportRepository() {
        return reportRepository;
    }

    @NonNull
    public ChatRepository getChatRepository() {
        return chatRepository;
    }
}
