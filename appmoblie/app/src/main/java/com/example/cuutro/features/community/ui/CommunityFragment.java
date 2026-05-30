package com.example.cuutro.features.community.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuutro.R;
import com.example.cuutro.app.MyApp;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.community.data.CommunityRepository;
import com.example.cuutro.features.community.model.CommunityPostItem;
import com.example.cuutro.features.splash.ui.NotificationScreenActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {

    private final List<CommunityPostItem> postItems = new ArrayList<>();
    private CommunityPostAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyStateView;
    private AuthRepository authRepository;
    private CommunityRepository communityRepository;
    private final ActivityResultLauncher<Intent> createPostLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            loadPostsFromBackend();
                        }
                    }
            );

    public CommunityFragment() {
        super(R.layout.fragment_community);
    }

    @Override
    public void onDestroyView() {
        if (adapter != null) {
            adapter.release();
            adapter = null;
        }
        recyclerView = null;
        emptyStateView = null;
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MyApp app = (MyApp) requireActivity().getApplication();
        authRepository = app.getAppContainer().getAuthRepository();
        communityRepository = app.getAppContainer().getCommunityRepository();
        emptyStateView = view.findViewById(R.id.tvCommunityEmpty);
        setupRecyclerView(view);
        setupCreatePostAction(view);
        loadPostsFromBackend();
    }

    private void setupRecyclerView(@NonNull View root) {
        recyclerView = root.findViewById(R.id.rvCommunityPosts);
        adapter = new CommunityPostAdapter((item, position) ->
                Toast.makeText(requireContext(), R.string.community_post_action_toast, Toast.LENGTH_SHORT).show()
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupCreatePostAction(@NonNull View root) {
        FloatingActionButton createPostButton = root.findViewById(R.id.fabCreateCommunityPost);
        createPostButton.setOnClickListener(v -> {
            if (!isUserLoggedIn()) {
                startActivity(
                        NotificationScreenActivity.createUnauthorizedIntent(
                                requireContext(),
                                getString(R.string.auth_required_create_post_message)
                        )
                );
                return;
            }
            Intent intent = new Intent(requireContext(), AddNewCommunityActivity.class);
            createPostLauncher.launch(intent);
        });
    }

    private boolean isUserLoggedIn() {
        return authRepository != null && authRepository.hasActiveSession();
    }

    private void loadPostsFromBackend() {
        if (communityRepository == null) {
            showEmptyState(getString(R.string.community_empty_posts));
            return;
        }

        showLoadingState();
        communityRepository.getPublicPosts(new ResultCallback<List<CommunityPostItem>>() {
            @Override
            public void onSuccess(List<CommunityPostItem> data) {
                if (!isAdded()) {
                    return;
                }
                postItems.clear();
                if (data != null) {
                    postItems.addAll(data);
                }
                renderPosts();
            }

            @Override
            public void onError(@NonNull NetworkError error) {
                if (!isAdded()) {
                    return;
                }
                postItems.clear();
                renderPosts();
                Toast.makeText(
                        requireContext(),
                        getString(R.string.community_load_failed, error.getMessage()),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showLoadingState() {
        if (adapter != null) {
            adapter.submitList(new ArrayList<>());
        }
        showEmptyState(getString(R.string.community_loading_posts));
    }

    private void renderPosts() {
        if (adapter == null) {
            return;
        }
        adapter.submitList(postItems);
        if (postItems.isEmpty()) {
            showEmptyState(getString(R.string.community_empty_posts));
            return;
        }
        if (emptyStateView != null) {
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private void showEmptyState(@NonNull String message) {
        if (emptyStateView == null) {
            return;
        }
        emptyStateView.setText(message);
        emptyStateView.setVisibility(View.VISIBLE);
    }
}
