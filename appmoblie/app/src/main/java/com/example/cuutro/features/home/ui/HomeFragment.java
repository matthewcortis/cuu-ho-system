package com.example.cuutro.features.home.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.cuutro.R;
import com.example.cuutro.app.AppContainer;
import com.example.cuutro.app.MyApp;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.core.location.DeviceLocationProvider;
import com.example.cuutro.core.location.TrackAsiaMapController;
import com.example.cuutro.features.report.ui.ReportActivity;
import com.example.cuutro.features.sos.data.SosRepository;
import com.example.cuutro.features.sos.model.EmergencyReportMapNode;
import com.trackasia.android.annotations.Marker;
import com.trackasia.android.annotations.MarkerOptions;
import com.trackasia.android.camera.CameraUpdateFactory;
import com.trackasia.android.geometry.LatLng;
import com.trackasia.android.maps.MapView;
import com.trackasia.android.maps.Style;
import com.trackasia.android.maps.TrackAsiaMap;
import com.trackasia.android.plugins.annotation.Circle;
import com.trackasia.android.plugins.annotation.CircleManager;
import com.trackasia.android.plugins.annotation.CircleOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String MAP_VIEW_STATE_KEY = "home_map_view_state";
    private static final LatLng HANOI = new LatLng(21.0285, 105.8542);
    private static final double DEFAULT_ZOOM_VIETNAM = 9.4;
    private static final double CURRENT_LOCATION_ZOOM = 15.5;
    private static final long SOS_PULSE_DURATION_MS = 1800L;
    private static final long REPORT_PULSE_DURATION_MS = 1700L;
    private static final float REPORT_PULSE_MIN_RADIUS = 8f;
    private static final float REPORT_PULSE_MAX_RADIUS = 24f;
    private static final float REPORT_CORE_RADIUS = 4.5f;

    private MapView mapView;
    private TrackAsiaMapController mapController;
    private TrackAsiaMap trackAsiaMap;
    private Marker currentLocationMarker;
    private SosRepository sosRepository;
    private DeviceLocationProvider locationProvider;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private LatLng currentUserLatLng;
    private final List<ObjectAnimator> sosPulseAnimators = new ArrayList<>();
    private final List<Circle> reportPulseCircles = new ArrayList<>();
    private final List<Circle> reportCoreCircles = new ArrayList<>();
    private final List<ValueAnimator> reportPulseAnimators = new ArrayList<>();
    @Nullable
    private CircleManager reportCircleManager;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationProvider = new DeviceLocationProvider(requireContext());
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean hasFine = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                    boolean hasCoarse = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                    if (hasFine || hasCoarse) {
                        requestCurrentLocation();
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MyApp app = (MyApp) requireActivity().getApplication();
        AppContainer appContainer = app.getAppContainer();
        if (appContainer != null) {
            sosRepository = appContainer.getSosRepository();
        }

        mapView = view.findViewById(R.id.home_map_view);
        if (mapView != null) {
            mapController = new TrackAsiaMapController(mapView);
        }

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_STATE_KEY);
        }
        if (mapController != null) {
            mapController.onCreate(mapViewBundle);
            mapController.loadStyle(
                    getString(R.string.trackasia_style_url),
                    HANOI,
                    DEFAULT_ZOOM_VIETNAM,
                    map -> {
                        trackAsiaMap = map;
                        initializeReportCircleManager();
                        loadRescueReportNodes();
                        if (currentUserLatLng != null) {
                            placeOrMoveCurrentLocationMarker(currentUserLatLng, false);
                        }
                    }
            );
        }

        initSosPulse(view);
        setupSosAction(view);
        requestCurrentLocationWithPermission();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapController != null) {
            mapController.onStart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapController != null) {
            mapController.onResume();
        }
        if (reportPulseCircles.isEmpty()) {
            loadRescueReportNodes();
        } else {
            startReportPulseAnimationsIfNeeded();
        }
    }

    @Override
    public void onPause() {
        stopReportPulseAnimations();
        if (mapController != null) {
            mapController.onPause();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mapController != null) {
            mapController.onStop();
        }
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapController != null) {
            mapController.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapController != null) {
            mapController.onSaveInstanceState(outState, MAP_VIEW_STATE_KEY);
        }
    }

    @Override
    public void onDestroyView() {
        stopSosPulse();
        stopReportPulseAnimations();
        clearRescueReportNodes();
        if (reportCircleManager != null) {
            reportCircleManager.onDestroy();
            reportCircleManager = null;
        }
        if (mapController != null) {
            mapController.onDestroy();
            mapController = null;
        }
        if (locationProvider != null) {
            locationProvider.cancel();
        }
        trackAsiaMap = null;
        currentLocationMarker = null;
        sosRepository = null;
        mapView = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (locationProvider != null) {
            locationProvider.cancel();
            locationProvider = null;
        }
        super.onDestroy();
    }

    private void initSosPulse(@NonNull View root) {
        View pulse1 = root.findViewById(R.id.sos_pulse_1);
        View pulse2 = root.findViewById(R.id.sos_pulse_2);
        if (pulse1 == null || pulse2 == null) {
            return;
        }
        stopSosPulse();
        startPulseAnimator(pulse1, 0L);
        startPulseAnimator(pulse2, SOS_PULSE_DURATION_MS / 2);
    }

    private void setupSosAction(@NonNull View root) {
        View sosButton = root.findViewById(R.id.btn_home_sos);
        if (sosButton == null) {
            return;
        }
        sosButton.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ReportActivity.class))
        );
    }

    private void startPulseAnimator(@NonNull View pulseView, long startDelay) {
        pulseView.setScaleX(1f);
        pulseView.setScaleY(1f);
        pulseView.setAlpha(0f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(pulseView, View.SCALE_X, 1f, 1.9f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(pulseView, View.SCALE_Y, 1f, 1.9f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(pulseView, View.ALPHA, 0.58f, 0f);

        configurePulseAnimator(scaleX, startDelay);
        configurePulseAnimator(scaleY, startDelay);
        configurePulseAnimator(alpha, startDelay);

        sosPulseAnimators.add(scaleX);
        sosPulseAnimators.add(scaleY);
        sosPulseAnimators.add(alpha);

        scaleX.start();
        scaleY.start();
        alpha.start();
    }

    private void configurePulseAnimator(@NonNull ObjectAnimator animator, long startDelay) {
        animator.setDuration(SOS_PULSE_DURATION_MS);
        animator.setStartDelay(startDelay);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
    }

    private void stopSosPulse() {
        for (ObjectAnimator animator : sosPulseAnimators) {
            animator.cancel();
        }
        sosPulseAnimators.clear();
    }

    private void initializeReportCircleManager() {
        if (mapView == null || trackAsiaMap == null) {
            return;
        }
        if (reportCircleManager != null) {
            return;
        }
        Style style = trackAsiaMap.getStyle();
        if (style == null) {
            return;
        }
        reportCircleManager = new CircleManager(mapView, trackAsiaMap, style);
    }

    private void loadRescueReportNodes() {
        if (!isAdded() || sosRepository == null) {
            return;
        }
        initializeReportCircleManager();
        if (reportCircleManager == null) {
            return;
        }

        sosRepository.getEmergencyReportMapNodes(new ResultCallback<List<EmergencyReportMapNode>>() {
            @Override
            public void onSuccess(List<EmergencyReportMapNode> data) {
                if (!isAdded()) {
                    return;
                }
                renderRescueReportNodes(data);
            }

            @Override
            public void onError(@NonNull NetworkError error) {
                if (!isAdded()) {
                    return;
                }
                clearRescueReportNodes();
            }
        });
    }

    private void renderRescueReportNodes(@Nullable List<EmergencyReportMapNode> nodes) {
        if (reportCircleManager == null) {
            return;
        }
        clearRescueReportNodes();
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        for (EmergencyReportMapNode node : nodes) {
            if (node == null) {
                continue;
            }
            NodeStyle nodeStyle = resolveNodeStyle(node.getStatus());
            if (nodeStyle == null) {
                continue;
            }

            LatLng nodePosition = new LatLng(node.getLatitude(), node.getLongitude());
            Circle pulseCircle = reportCircleManager.create(new CircleOptions()
                    .withLatLng(nodePosition)
                    .withCircleColor(nodeStyle.colorHex)
                    .withCircleOpacity(0.56f)
                    .withCircleBlur(0.72f)
                    .withCircleRadius(REPORT_PULSE_MIN_RADIUS)
                    .withCircleStrokeWidth(0f)
                    .withCircleStrokeOpacity(0f));
            Circle coreCircle = reportCircleManager.create(new CircleOptions()
                    .withLatLng(nodePosition)
                    .withCircleColor(nodeStyle.colorHex)
                    .withCircleOpacity(1f)
                    .withCircleBlur(0f)
                    .withCircleRadius(REPORT_CORE_RADIUS)
                    .withCircleStrokeWidth(1.4f)
                    .withCircleStrokeColor("#FFFFFF")
                    .withCircleStrokeOpacity(0.9f));

            reportPulseCircles.add(pulseCircle);
            reportCoreCircles.add(coreCircle);
        }

        startReportPulseAnimationsIfNeeded();
    }

    private void clearRescueReportNodes() {
        stopReportPulseAnimations();
        if (reportCircleManager != null) {
            if (!reportPulseCircles.isEmpty()) {
                reportCircleManager.delete(reportPulseCircles);
            }
            if (!reportCoreCircles.isEmpty()) {
                reportCircleManager.delete(reportCoreCircles);
            }
        }
        reportPulseCircles.clear();
        reportCoreCircles.clear();
    }

    private void startReportPulseAnimationsIfNeeded() {
        if (reportCircleManager == null || reportPulseCircles.isEmpty() || !reportPulseAnimators.isEmpty()) {
            return;
        }

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(REPORT_PULSE_DURATION_MS);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.addUpdateListener(valueAnimator -> {
            if (reportCircleManager == null || reportPulseCircles.isEmpty()) {
                return;
            }
            float progress = (float) valueAnimator.getAnimatedValue();
            float radius = REPORT_PULSE_MIN_RADIUS
                    + ((REPORT_PULSE_MAX_RADIUS - REPORT_PULSE_MIN_RADIUS) * progress);
            float opacity = 0.62f * (1f - progress);
            for (Circle pulseCircle : reportPulseCircles) {
                pulseCircle.setCircleRadius(radius);
                pulseCircle.setCircleOpacity(opacity);
            }
            reportCircleManager.update(reportPulseCircles);
        });
        reportPulseAnimators.add(animator);
        animator.start();
    }

    private void stopReportPulseAnimations() {
        for (ValueAnimator animator : reportPulseAnimators) {
            animator.cancel();
        }
        reportPulseAnimators.clear();
    }

    @Nullable
    private NodeStyle resolveNodeStyle(@Nullable String rawStatus) {
        String status = normalizeStatus(rawStatus);
        if (status == null) {
            return null;
        }
        if (SosRepository.TRANG_THAI_DANG_TREN_DUONG_TOI.equals(status)) {
            return NodeStyle.RED;
        }
        if (SosRepository.TRANG_THAI_DANG_XU_LY.equals(status)) {
            return NodeStyle.YELLOW;
        }
        if (SosRepository.TRANG_THAI_HOAN_THANH.equals(status)) {
            return NodeStyle.GREEN;
        }
        if (SosRepository.TRANG_THAI_DA_NHAN.equals(status) || SosRepository.TRANG_THAI_HUY.equals(status)) {
            return null;
        }
        return null;
    }

    @Nullable
    private String normalizeStatus(@Nullable String rawStatus) {
        if (rawStatus == null) {
            return null;
        }
        String normalized = rawStatus.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private enum NodeStyle {
        RED("#E53935"),
        YELLOW("#FBC02D"),
        GREEN("#2E7D32");

        @NonNull
        private final String colorHex;

        NodeStyle(@NonNull String colorHex) {
            this.colorHex = colorHex;
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCurrentLocationWithPermission() {
        if (hasLocationPermission()) {
            requestCurrentLocation();
            return;
        }
        if (locationPermissionLauncher != null) {
            locationPermissionLauncher.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void requestCurrentLocation() {
        if (locationProvider == null) {
            return;
        }
        locationProvider.requestCurrentLocation(new DeviceLocationProvider.Callback() {
            @Override
            public void onLocation(@NonNull Location location) {
                if (!isAdded()) {
                    return;
                }
                currentUserLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                placeOrMoveCurrentLocationMarker(currentUserLatLng, true);
            }

            @Override
            public void onError(@NonNull DeviceLocationProvider.Error error) {
                // Keep default camera target (Ha Noi) when current location is unavailable.
            }
        });
    }

    private void placeOrMoveCurrentLocationMarker(@NonNull LatLng target, boolean animateCamera) {
        if (trackAsiaMap == null) {
            return;
        }

        if (currentLocationMarker != null) {
            trackAsiaMap.removeMarker(currentLocationMarker);
        }
        currentLocationMarker = trackAsiaMap.addMarker(new MarkerOptions().position(target));

        if (animateCamera) {
            trackAsiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, CURRENT_LOCATION_ZOOM));
        }
    }
}
