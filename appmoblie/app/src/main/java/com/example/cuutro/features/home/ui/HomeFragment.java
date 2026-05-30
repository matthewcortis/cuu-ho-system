package com.example.cuutro.features.home.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.trackasia.android.plugins.annotation.Line;
import com.trackasia.android.plugins.annotation.LineManager;
import com.trackasia.android.plugins.annotation.LineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
    private static final float CAPTAIN_MARKER_RADIUS = 5.2f;
    private static final String CAPTAIN_MARKER_COLOR_HEX = "#1E88E5";
    private static final String CAPTAIN_ROUTE_COLOR_HEX = "#1E88E5";
    private static final float CAPTAIN_ROUTE_WIDTH = 4.2f;
    private static final String CAPTAIN_ROUTE_PROFILE = "moto";
    private static final String CAPTAIN_ROUTE_FALLBACK_PROFILE = "driving";
    private static final String TRACK_ASIA_ROUTE_BASE_FALLBACK = "https://maps.track-asia.com/route/v1/";
    private static final String OPEN_WEATHER_CURRENT_BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String OPEN_WEATHER_UNITS_METRIC = "metric";
    private static final String OPEN_WEATHER_LANG_VI = "vi";

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
    private final List<Circle> captainLocationCircles = new ArrayList<>();
    private final List<Line> captainRouteLines = new ArrayList<>();
    private final List<Call> captainRouteCalls = new ArrayList<>();
    private final List<ValueAnimator> reportPulseAnimators = new ArrayList<>();
    private final OkHttpClient directionsHttpClient = new OkHttpClient();
    private volatile int captainRouteRenderGeneration = 0;
    @Nullable
    private View weatherWidgetCard;
    @Nullable
    private TextView weatherToggleText;
    @Nullable
    private ImageView weatherToggleArrow;
    @Nullable
    private View weatherLoadingIndicator;
    @Nullable
    private TextView weatherStatusText;
    @Nullable
    private View weatherContentContainer;
    @Nullable
    private TextView weatherLocationText;
    @Nullable
    private TextView weatherTemperatureText;
    @Nullable
    private TextView weatherDescriptionText;
    @Nullable
    private TextView weatherDetailsText;
    @Nullable
    private volatile Call weatherCall;
    @Nullable
    private CircleManager reportCircleManager;
    @Nullable
    private LineManager captainRouteLineManager;

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
                    } else if (isWeatherWidgetExpanded()) {
                        showWeatherError(getString(R.string.home_weather_permission_required));
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
                        initializeCaptainRouteLineManager();
                        loadRescueReportNodes();
                        if (currentUserLatLng != null) {
                            placeOrMoveCurrentLocationMarker(currentUserLatLng, false);
                        }
                    }
            );
        }

        initSosPulse(view);
        setupSosAction(view);
        setupWeatherWidget(view);
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
        if (captainRouteLineManager != null) {
            captainRouteLineManager.onDestroy();
            captainRouteLineManager = null;
        }
        if (mapController != null) {
            mapController.onDestroy();
            mapController = null;
        }
        if (locationProvider != null) {
            locationProvider.cancel();
        }
        cancelWeatherCall();
        weatherWidgetCard = null;
        weatherToggleText = null;
        weatherToggleArrow = null;
        weatherLoadingIndicator = null;
        weatherStatusText = null;
        weatherContentContainer = null;
        weatherLocationText = null;
        weatherTemperatureText = null;
        weatherDescriptionText = null;
        weatherDetailsText = null;
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

    private void setupWeatherWidget(@NonNull View root) {
        View weatherDropdownButton = root.findViewById(R.id.btn_home_weather_dropdown);
        weatherWidgetCard = root.findViewById(R.id.card_home_weather_widget);
        weatherToggleText = root.findViewById(R.id.tv_home_weather_toggle);
        weatherToggleArrow = root.findViewById(R.id.iv_home_weather_arrow);
        weatherLoadingIndicator = root.findViewById(R.id.pb_home_weather_loading);
        weatherStatusText = root.findViewById(R.id.tv_home_weather_status);
        weatherContentContainer = root.findViewById(R.id.layout_home_weather_content);
        weatherLocationText = root.findViewById(R.id.tv_home_weather_location);
        weatherTemperatureText = root.findViewById(R.id.tv_home_weather_temperature);
        weatherDescriptionText = root.findViewById(R.id.tv_home_weather_description);
        weatherDetailsText = root.findViewById(R.id.tv_home_weather_details);

        setWeatherWidgetExpanded(false, false);
        if (weatherDropdownButton == null) {
            return;
        }
        weatherDropdownButton.setOnClickListener(v -> {
            boolean shouldExpand = !isWeatherWidgetExpanded();
            setWeatherWidgetExpanded(shouldExpand, true);
            if (shouldExpand) {
                fetchWeatherForCurrentLocation();
            }
        });
    }

    private boolean isWeatherWidgetExpanded() {
        return weatherWidgetCard != null && weatherWidgetCard.getVisibility() == View.VISIBLE;
    }

    private void setWeatherWidgetExpanded(boolean expanded, boolean animateArrow) {
        if (weatherWidgetCard != null) {
            weatherWidgetCard.setVisibility(expanded ? View.VISIBLE : View.GONE);
        }
        if (weatherToggleText != null) {
            weatherToggleText.setText(expanded
                    ? R.string.home_weather_toggle_hide
                    : R.string.home_weather_toggle_show);
        }
        if (weatherToggleArrow != null) {
            weatherToggleArrow.animate().cancel();
            if (animateArrow) {
                weatherToggleArrow.animate()
                        .rotation(expanded ? 180f : 0f)
                        .setDuration(140L)
                        .start();
            } else {
                weatherToggleArrow.setRotation(expanded ? 180f : 0f);
            }
        }
        if (!expanded) {
            cancelWeatherCall();
        }
    }

    private void fetchWeatherForCurrentLocation() {
        if (!isAdded() || !isWeatherWidgetExpanded()) {
            return;
        }
        if (!hasLocationPermission()) {
            showWeatherError(getString(R.string.home_weather_permission_required));
            requestCurrentLocationWithPermission();
            return;
        }
        if (currentUserLatLng == null) {
            showWeatherLoading(getString(R.string.home_weather_loading_location));
            requestCurrentLocation();
            return;
        }
        requestWeatherForLocation(currentUserLatLng);
    }

    private void requestWeatherForLocation(@NonNull LatLng targetLocation) {
        if (!isWeatherWidgetExpanded()) {
            return;
        }
        String apiKey = getString(R.string.open_weather_api_key);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            showWeatherError(getString(R.string.home_weather_config_missing));
            return;
        }

        String weatherUrl = Uri.parse(OPEN_WEATHER_CURRENT_BASE_URL).buildUpon()
                .appendQueryParameter("lat", String.format(Locale.US, "%.6f", targetLocation.getLatitude()))
                .appendQueryParameter("lon", String.format(Locale.US, "%.6f", targetLocation.getLongitude()))
                .appendQueryParameter("appid", apiKey.trim())
                .appendQueryParameter("units", OPEN_WEATHER_UNITS_METRIC)
                .appendQueryParameter("lang", OPEN_WEATHER_LANG_VI)
                .build()
                .toString();
        Request request = new Request.Builder()
                .url(weatherUrl)
                .get()
                .build();

        cancelWeatherCall();
        showWeatherLoading(getString(R.string.home_weather_loading_data));
        Call currentRequestCall = directionsHttpClient.newCall(request);
        weatherCall = currentRequestCall;
        currentRequestCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (call.isCanceled() || call != weatherCall) {
                    return;
                }
                weatherCall = null;
                runOnUiThreadIfAlive(() -> {
                    if (!isWeatherWidgetExpanded()) {
                        return;
                    }
                    showWeatherError(getString(R.string.home_weather_load_failed));
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (call != weatherCall) {
                    response.close();
                    return;
                }
                WeatherDisplayData displayData = null;
                try {
                    if (response.isSuccessful()) {
                        okhttp3.ResponseBody responseBody = response.body();
                        if (responseBody != null) {
                            displayData = parseWeatherDisplayData(responseBody.string());
                        }
                    }
                } finally {
                    response.close();
                    if (call == weatherCall) {
                        weatherCall = null;
                    }
                }
                if (displayData == null) {
                    runOnUiThreadIfAlive(() -> {
                        if (!isWeatherWidgetExpanded()) {
                            return;
                        }
                        showWeatherError(getString(R.string.home_weather_load_failed));
                    });
                    return;
                }
                WeatherDisplayData finalDisplayData = displayData;
                runOnUiThreadIfAlive(() -> {
                    if (!isWeatherWidgetExpanded()) {
                        return;
                    }
                    showWeatherData(finalDisplayData);
                });
            }
        });
    }

    private void showWeatherLoading(@NonNull String statusText) {
        if (weatherLoadingIndicator != null) {
            weatherLoadingIndicator.setVisibility(View.VISIBLE);
        }
        if (weatherStatusText != null) {
            weatherStatusText.setVisibility(View.VISIBLE);
            weatherStatusText.setText(statusText);
        }
        if (weatherContentContainer != null) {
            weatherContentContainer.setVisibility(View.GONE);
        }
    }

    private void showWeatherError(@NonNull String statusText) {
        if (weatherLoadingIndicator != null) {
            weatherLoadingIndicator.setVisibility(View.GONE);
        }
        if (weatherStatusText != null) {
            weatherStatusText.setVisibility(View.VISIBLE);
            weatherStatusText.setText(statusText);
        }
        if (weatherContentContainer != null) {
            weatherContentContainer.setVisibility(View.GONE);
        }
    }

    private void showWeatherData(@NonNull WeatherDisplayData displayData) {
        if (weatherLoadingIndicator != null) {
            weatherLoadingIndicator.setVisibility(View.GONE);
        }
        if (weatherStatusText != null) {
            weatherStatusText.setVisibility(View.VISIBLE);
            weatherStatusText.setText(R.string.home_weather_updated_from_location);
        }
        if (weatherContentContainer != null) {
            weatherContentContainer.setVisibility(View.VISIBLE);
        }

        String placeName = displayData.locationName;
        if (placeName == null || placeName.trim().isEmpty()) {
            placeName = getString(R.string.home_weather_location_unknown);
        }
        if (weatherLocationText != null) {
            weatherLocationText.setText(getString(R.string.home_weather_location_format, placeName));
        }
        if (weatherTemperatureText != null) {
            weatherTemperatureText.setText(getString(R.string.home_weather_temperature_format, displayData.temperatureCelsius));
        }
        if (weatherDescriptionText != null) {
            weatherDescriptionText.setText(capitalizeWeatherDescription(displayData.description));
        }

        String feelsLikeText = Double.isNaN(displayData.feelsLikeCelsius)
                ? "--"
                : getString(R.string.home_weather_value_temp, displayData.feelsLikeCelsius);
        String humidityText = displayData.humidityPercent < 0
                ? "--"
                : getString(R.string.home_weather_value_humidity, displayData.humidityPercent);
        String windText = Double.isNaN(displayData.windSpeedMetersPerSecond)
                ? "--"
                : getString(R.string.home_weather_value_wind, displayData.windSpeedMetersPerSecond);
        if (weatherDetailsText != null) {
            weatherDetailsText.setText(getString(
                    R.string.home_weather_details_template,
                    feelsLikeText,
                    humidityText,
                    windText
            ));
        }
    }

    @Nullable
    private WeatherDisplayData parseWeatherDisplayData(@Nullable String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            return null;
        }
        try {
            JSONObject root = new JSONObject(payload);
            JSONObject main = root.optJSONObject("main");
            if (main == null) {
                return null;
            }

            double temperature = main.optDouble("temp", Double.NaN);
            if (Double.isNaN(temperature)) {
                return null;
            }
            double feelsLike = main.optDouble("feels_like", Double.NaN);
            int humidity = main.optInt("humidity", -1);
            String locationName = root.optString("name", null);

            String description = null;
            JSONArray weatherArray = root.optJSONArray("weather");
            if (weatherArray != null && weatherArray.length() > 0) {
                JSONObject firstWeather = weatherArray.optJSONObject(0);
                if (firstWeather != null) {
                    description = firstWeather.optString("description", null);
                }
            }

            double windSpeed = Double.NaN;
            JSONObject wind = root.optJSONObject("wind");
            if (wind != null) {
                windSpeed = wind.optDouble("speed", Double.NaN);
            }

            return new WeatherDisplayData(
                    locationName,
                    description,
                    temperature,
                    feelsLike,
                    humidity,
                    windSpeed
            );
        } catch (JSONException ignored) {
            return null;
        }
    }

    @NonNull
    private String capitalizeWeatherDescription(@Nullable String description) {
        if (description == null || description.trim().isEmpty()) {
            return getString(R.string.home_weather_description_unknown);
        }
        String normalized = description.trim();
        if (normalized.length() == 1) {
            return normalized.toUpperCase(Locale.getDefault());
        }
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private void runOnUiThreadIfAlive(@NonNull Runnable action) {
        if (!isAdded()) {
            return;
        }
        androidx.fragment.app.FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(action);
    }

    private void cancelWeatherCall() {
        Call activeCall = weatherCall;
        weatherCall = null;
        if (activeCall != null) {
            activeCall.cancel();
        }
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

    private void initializeCaptainRouteLineManager() {
        if (mapView == null || trackAsiaMap == null) {
            return;
        }
        if (captainRouteLineManager != null) {
            return;
        }
        Style style = trackAsiaMap.getStyle();
        if (style == null) {
            return;
        }
        captainRouteLineManager = new LineManager(mapView, trackAsiaMap, style);
    }

    private void loadRescueReportNodes() {
        if (!isAdded() || sosRepository == null) {
            return;
        }
        initializeReportCircleManager();
        initializeCaptainRouteLineManager();
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
            String normalizedStatus = normalizeStatus(node.getStatus());
            NodeStyle nodeStyle = resolveNodeStyle(normalizedStatus);
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

            if (SosRepository.TRANG_THAI_DANG_TREN_DUONG_TOI.equals(normalizedStatus)) {
                renderCaptainRoute(node, nodePosition);
            }
        }

        startReportPulseAnimationsIfNeeded();
    }

    private void clearRescueReportNodes() {
        stopReportPulseAnimations();
        captainRouteRenderGeneration++;
        cancelCaptainRouteCalls();
        if (reportCircleManager != null) {
            if (!reportPulseCircles.isEmpty()) {
                reportCircleManager.delete(reportPulseCircles);
            }
            if (!reportCoreCircles.isEmpty()) {
                reportCircleManager.delete(reportCoreCircles);
            }
            if (!captainLocationCircles.isEmpty()) {
                reportCircleManager.delete(captainLocationCircles);
            }
        }
        if (captainRouteLineManager != null && !captainRouteLines.isEmpty()) {
            captainRouteLineManager.delete(captainRouteLines);
        }
        reportPulseCircles.clear();
        reportCoreCircles.clear();
        captainLocationCircles.clear();
        captainRouteLines.clear();
    }

    private void renderCaptainRoute(
            @NonNull EmergencyReportMapNode node,
            @NonNull LatLng reportPosition
    ) {
        if (reportCircleManager == null) {
            return;
        }
        Double captainLatitude = node.getCaptainLatitude();
        Double captainLongitude = node.getCaptainLongitude();
        if (captainLatitude == null || captainLongitude == null) {
            return;
        }

        LatLng captainPosition = new LatLng(captainLatitude, captainLongitude);
        Circle captainCoreCircle = reportCircleManager.create(new CircleOptions()
                .withLatLng(captainPosition)
                .withCircleColor(CAPTAIN_MARKER_COLOR_HEX)
                .withCircleOpacity(0.96f)
                .withCircleBlur(0f)
                .withCircleRadius(CAPTAIN_MARKER_RADIUS)
                .withCircleStrokeWidth(1.6f)
                .withCircleStrokeColor("#FFFFFF")
                .withCircleStrokeOpacity(0.92f));
        captainLocationCircles.add(captainCoreCircle);

        requestCaptainRoute(
                captainPosition,
                reportPosition,
                captainRouteRenderGeneration,
                CAPTAIN_ROUTE_PROFILE,
                true
        );
    }

    private void requestCaptainRoute(
            @NonNull LatLng captainPosition,
            @NonNull LatLng reportPosition,
            int renderGeneration,
            @NonNull String profile,
            boolean allowFallback
    ) {
        String routeUrl = buildCaptainRouteUrl(captainPosition, reportPosition, profile);
        if (routeUrl == null || routeUrl.isEmpty()) {
            return;
        }
        Request request = new Request.Builder()
                .url(routeUrl)
                .get()
                .build();
        Call routeCall = directionsHttpClient.newCall(request);
        trackCaptainRouteCall(routeCall);
        routeCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                untrackCaptainRouteCall(call);
                if (call.isCanceled() || isCaptainRouteStale(renderGeneration)) {
                    return;
                }
                if (allowFallback && !CAPTAIN_ROUTE_FALLBACK_PROFILE.equals(profile)) {
                    requestCaptainRoute(
                            captainPosition,
                            reportPosition,
                            renderGeneration,
                            CAPTAIN_ROUTE_FALLBACK_PROFILE,
                            false
                    );
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                untrackCaptainRouteCall(call);
                String responseBody = null;
                try {
                    if (!response.isSuccessful()) {
                        maybeFallbackRouteRequest(captainPosition, reportPosition, renderGeneration, profile, allowFallback);
                        return;
                    }
                    okhttp3.ResponseBody body = response.body();
                    if (body == null) {
                        maybeFallbackRouteRequest(captainPosition, reportPosition, renderGeneration, profile, allowFallback);
                        return;
                    }
                    responseBody = body.string();
                } catch (IOException ioException) {
                    maybeFallbackRouteRequest(captainPosition, reportPosition, renderGeneration, profile, allowFallback);
                    return;
                } finally {
                    response.close();
                }

                List<LatLng> routePoints = parseCaptainRoute(responseBody);
                if (routePoints.size() < 2) {
                    maybeFallbackRouteRequest(captainPosition, reportPosition, renderGeneration, profile, allowFallback);
                    return;
                }
                if (isCaptainRouteStale(renderGeneration) || getActivity() == null) {
                    return;
                }
                androidx.fragment.app.FragmentActivity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(() -> drawCaptainRouteLine(routePoints, renderGeneration));
            }
        });
    }

    private void maybeFallbackRouteRequest(
            @NonNull LatLng captainPosition,
            @NonNull LatLng reportPosition,
            int renderGeneration,
            @NonNull String profile,
            boolean allowFallback
    ) {
        if (isCaptainRouteStale(renderGeneration)) {
            return;
        }
        if (!allowFallback || CAPTAIN_ROUTE_FALLBACK_PROFILE.equals(profile)) {
            return;
        }
        requestCaptainRoute(
                captainPosition,
                reportPosition,
                renderGeneration,
                CAPTAIN_ROUTE_FALLBACK_PROFILE,
                false
        );
    }

    private void drawCaptainRouteLine(@NonNull List<LatLng> routePoints, int renderGeneration) {
        if (!isAdded()
                || captainRouteLineManager == null
                || isCaptainRouteStale(renderGeneration)) {
            return;
        }
        Line routeLine = captainRouteLineManager.create(new LineOptions()
                .withLatLngs(routePoints)
                .withLineColor(CAPTAIN_ROUTE_COLOR_HEX)
                .withLineWidth(CAPTAIN_ROUTE_WIDTH)
                .withLineOpacity(0.84f));
        captainRouteLines.add(routeLine);
    }

    @Nullable
    private String buildCaptainRouteUrl(
            @NonNull LatLng captainPosition,
            @NonNull LatLng reportPosition,
            @NonNull String profile
    ) {
        String baseRouteUrl = resolveTrackAsiaRouteBaseUrl();
        if (baseRouteUrl.isEmpty()) {
            return null;
        }
        String coordinates = String.format(
                Locale.US,
                "%.7f,%.7f;%.7f,%.7f",
                captainPosition.getLongitude(),
                captainPosition.getLatitude(),
                reportPosition.getLongitude(),
                reportPosition.getLatitude()
        );
        Uri.Builder uriBuilder = Uri.parse(baseRouteUrl + profile + "/" + coordinates + ".json").buildUpon()
                .appendQueryParameter("overview", "full")
                .appendQueryParameter("geometries", "geojson");
        String apiKey = getString(R.string.trackasia_api_key);
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            uriBuilder.appendQueryParameter("key", apiKey.trim());
        }
        return uriBuilder.build().toString();
    }

    @NonNull
    private String resolveTrackAsiaRouteBaseUrl() {
        String styleUrl = getString(R.string.trackasia_style_url);
        if (styleUrl == null || styleUrl.trim().isEmpty()) {
            return TRACK_ASIA_ROUTE_BASE_FALLBACK;
        }
        Uri styleUri = Uri.parse(styleUrl);
        String scheme = styleUri.getScheme();
        String host = styleUri.getHost();
        if (scheme == null || host == null || scheme.isEmpty() || host.isEmpty()) {
            return TRACK_ASIA_ROUTE_BASE_FALLBACK;
        }
        int port = styleUri.getPort();
        if (port > 0) {
            return scheme + "://" + host + ":" + port + "/route/v1/";
        }
        return scheme + "://" + host + "/route/v1/";
    }

    @NonNull
    private List<LatLng> parseCaptainRoute(@Nullable String routeBody) {
        List<LatLng> routePoints = new ArrayList<>();
        if (routeBody == null || routeBody.trim().isEmpty()) {
            return routePoints;
        }
        try {
            JSONObject root = new JSONObject(routeBody);
            JSONArray routes = root.optJSONArray("routes");
            if (routes == null || routes.length() == 0) {
                return routePoints;
            }
            JSONObject firstRoute = routes.optJSONObject(0);
            if (firstRoute == null) {
                return routePoints;
            }
            JSONObject geometry = firstRoute.optJSONObject("geometry");
            if (geometry == null) {
                return routePoints;
            }
            JSONArray coordinates = geometry.optJSONArray("coordinates");
            if (coordinates == null || coordinates.length() == 0) {
                return routePoints;
            }
            for (int i = 0; i < coordinates.length(); i++) {
                JSONArray coordinate = coordinates.optJSONArray(i);
                if (coordinate == null || coordinate.length() < 2) {
                    continue;
                }
                double longitude = coordinate.optDouble(0, Double.NaN);
                double latitude = coordinate.optDouble(1, Double.NaN);
                if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
                    continue;
                }
                routePoints.add(new LatLng(latitude, longitude));
            }
        } catch (JSONException ignored) {
            // Ignore malformed routing payloads and keep map state stable.
        }
        return routePoints;
    }

    private boolean isCaptainRouteStale(int renderGeneration) {
        return !isAdded() || captainRouteRenderGeneration != renderGeneration;
    }

    private void trackCaptainRouteCall(@NonNull Call call) {
        synchronized (captainRouteCalls) {
            captainRouteCalls.add(call);
        }
    }

    private void untrackCaptainRouteCall(@NonNull Call call) {
        synchronized (captainRouteCalls) {
            captainRouteCalls.remove(call);
        }
    }

    private void cancelCaptainRouteCalls() {
        List<Call> activeCalls;
        synchronized (captainRouteCalls) {
            if (captainRouteCalls.isEmpty()) {
                return;
            }
            activeCalls = new ArrayList<>(captainRouteCalls);
            captainRouteCalls.clear();
        }
        for (Call call : activeCalls) {
            call.cancel();
        }
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
    private NodeStyle resolveNodeStyle(@Nullable String status) {
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

    private static final class WeatherDisplayData {
        @Nullable
        private final String locationName;
        @Nullable
        private final String description;
        private final double temperatureCelsius;
        private final double feelsLikeCelsius;
        private final int humidityPercent;
        private final double windSpeedMetersPerSecond;

        private WeatherDisplayData(
                @Nullable String locationName,
                @Nullable String description,
                double temperatureCelsius,
                double feelsLikeCelsius,
                int humidityPercent,
                double windSpeedMetersPerSecond
        ) {
            this.locationName = locationName;
            this.description = description;
            this.temperatureCelsius = temperatureCelsius;
            this.feelsLikeCelsius = feelsLikeCelsius;
            this.humidityPercent = humidityPercent;
            this.windSpeedMetersPerSecond = windSpeedMetersPerSecond;
        }
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
                if (isWeatherWidgetExpanded()) {
                    requestWeatherForLocation(currentUserLatLng);
                }
            }

            @Override
            public void onError(@NonNull DeviceLocationProvider.Error error) {
                if (isWeatherWidgetExpanded()) {
                    showWeatherError(getString(R.string.home_weather_location_unavailable));
                }
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
