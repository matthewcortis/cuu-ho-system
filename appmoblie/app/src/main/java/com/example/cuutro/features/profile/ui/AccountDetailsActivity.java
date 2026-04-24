package com.example.cuutro.features.profile.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cuutro.R;
import com.example.cuutro.app.MyApp;
import com.example.cuutro.core.location.DeviceLocationProvider;
import com.example.cuutro.core.location.LocationAddressResolver;
import com.example.cuutro.core.location.MapGestureCoordinator;
import com.example.cuutro.core.location.TrackAsiaMapController;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.auth.ui.LoginActivity;
import com.example.cuutro.features.profile.data.ProfileRepository;
import com.example.cuutro.features.profile.data.model.UserProfileData;
import com.example.cuutro.features.splash.ui.NotificationScreenActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.trackasia.android.annotations.Marker;
import com.trackasia.android.annotations.MarkerOptions;
import com.trackasia.android.camera.CameraUpdateFactory;
import com.trackasia.android.geometry.LatLng;
import com.trackasia.android.maps.MapView;
import com.trackasia.android.maps.TrackAsiaMap;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class AccountDetailsActivity extends AppCompatActivity {

    private static final String MAP_VIEW_STATE_KEY = "account_map_view_state";
    private static final String AVATAR_URI_KEY = "account_avatar_uri";
    private static final LatLng HANOI = new LatLng(21.0285, 105.8542);
    private static final double DEFAULT_ZOOM = 12.8;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");

    private MapView mapView;
    private TrackAsiaMapController mapController;
    private TrackAsiaMap trackAsiaMap;
    private Marker currentLocationMarker;
    private AppCompatImageView avatarImageView;
    private TextInputLayout nameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout phoneInputLayout;
    private TextInputLayout addressInputLayout;
    private TextInputEditText nameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText phoneEditText;
    private TextInputEditText addressEditText;
    private MaterialButton saveButton;
    private CharSequence defaultSaveButtonText;
    private MaterialButton deleteButton;
    private CharSequence defaultDeleteButtonText;
    private MaterialButton pickPreciseAddressButton;
    private final ExecutorService geocodeExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService avatarExecutor = Executors.newSingleThreadExecutor();
    private DeviceLocationProvider locationProvider;
    private LocationAddressResolver addressResolver;
    private AuthRepository authRepository;
    private ProfileRepository profileRepository;
    private ActivityResultLauncher<String[]> pickAvatarLauncher;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private Uri selectedAvatarUri;
    private boolean isFormattingPhoneInput;
    private LatLng currentUserLatLng;
    private String currentNguoiDungId;
    private int avatarLoadRequestToken;

    private enum ProfileAction {
        LOAD,
        SAVE,
        DELETE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        MyApp app = (MyApp) getApplication();
        authRepository = app.getAppContainer().getAuthRepository();
        profileRepository = app.getAppContainer().getProfileRepository();
        if (authRepository == null || !authRepository.hasActiveSession()) {
            startActivity(
                    NotificationScreenActivity.createUnauthorizedIntent(
                            this,
                            getString(R.string.auth_required_account_details_message)
                    )
            );
            finish();
            return;
        }

        locationProvider = new DeviceLocationProvider(this);
        addressResolver = new LocationAddressResolver(this, new Locale("vi", "VN"));
        setupAvatarPickerLauncher();
        setupLocationPermissionLauncher();

        avatarImageView = findViewById(R.id.img_account_avatar);
        nameInputLayout = findViewById(R.id.account_details_name_input_layout);
        emailInputLayout = findViewById(R.id.account_details_email_input_layout);
        phoneInputLayout = findViewById(R.id.account_details_phone_input_layout);
        addressInputLayout = findViewById(R.id.account_details_address_input_layout);
        nameEditText = findViewById(R.id.edt_account_details_name);
        emailEditText = findViewById(R.id.edt_account_details_email);
        phoneEditText = findViewById(R.id.edt_account_details_phone);
        mapView = findViewById(R.id.account_details_map_view);
        addressEditText = findViewById(R.id.edt_account_details_address);
        deleteButton = findViewById(R.id.btn_account_cancel);
        saveButton = findViewById(R.id.btn_account_save);
        defaultSaveButtonText = saveButton != null
                ? saveButton.getText()
                : getString(R.string.account_details_save);
        defaultDeleteButtonText = deleteButton != null
                ? deleteButton.getText()
                : getString(R.string.account_details_delete_action);
        pickPreciseAddressButton = findViewById(R.id.btn_pick_precise_address_map);
        if (mapView != null) {
            MapGestureCoordinator.install(mapView);
            Bundle mapViewBundle = null;
            if (savedInstanceState != null) {
                mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_STATE_KEY);
            }
            mapController = new TrackAsiaMapController(mapView);
            mapController.onCreate(mapViewBundle);
            LatLng initialTarget = currentUserLatLng != null ? currentUserLatLng : HANOI;
            double initialZoom = currentUserLatLng != null ? 15.5 : DEFAULT_ZOOM;
            mapController.loadStyle(
                    getString(R.string.trackasia_style_url),
                    initialTarget,
                    initialZoom,
                    map -> {
                        trackAsiaMap = map;
                        if (currentUserLatLng != null) {
                            placeOrMoveCurrentLocationMarker(currentUserLatLng, false);
                        }
                    }
            );
        }

        restoreAvatarState(savedInstanceState);
        setupFormValidationInteractions();

        View backButton = findViewById(R.id.btn_account_details_back);
        View avatarButton = findViewById(R.id.btn_account_avatar);
        if (avatarButton != null) {
            avatarButton.setOnClickListener(v -> {
                if (pickAvatarLauncher != null) {
                    pickAvatarLauncher.launch(new String[] {"image/*"});
                }
            });
            avatarButton.setOnLongClickListener(v -> {
                clearAvatarSelection();
                Toast.makeText(this, R.string.account_details_avatar_removed, Toast.LENGTH_SHORT).show();
                return true;
            });
        }
        if (pickPreciseAddressButton != null) {
            pickPreciseAddressButton.setOnClickListener(v -> pickAddressFromCurrentLocation());
        }
        if (saveButton != null) {
            saveButton.setOnClickListener(v -> {
                if (validateAccountDetailsInputs()) {
                    saveCurrentProfile();
                }
            });
        }
        if (deleteButton != null) {
            deleteButton.setOnClickListener(v -> confirmDeleteCurrentProfile());
        }

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        populateMinimalInfoFromSession();
        loadUserProfileData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapController != null) {
            mapController.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapController != null) {
            mapController.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (mapController != null) {
            mapController.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
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
    protected void onDestroy() {
        if (locationProvider != null) {
            locationProvider.cancel();
        }
        geocodeExecutor.shutdownNow();
        avatarExecutor.shutdownNow();
        if (mapController != null) {
            mapController.onDestroy();
            mapController = null;
        }
        mapView = null;
        trackAsiaMap = null;
        currentLocationMarker = null;
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedAvatarUri != null) {
            outState.putString(AVATAR_URI_KEY, selectedAvatarUri.toString());
        }
        if (mapController != null) {
            mapController.onSaveInstanceState(outState, MAP_VIEW_STATE_KEY);
        }
    }

    private void setupAvatarPickerLauncher() {
        pickAvatarLauncher =
                registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                    if (uri == null) {
                        return;
                    }
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (SecurityException ignored) {
                        // Some providers do not support persistable permissions.
                    }
                    selectedAvatarUri = uri;
                    applySelectedAvatar(uri);
                });
    }

    private void restoreAvatarState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        String avatarUriString = savedInstanceState.getString(AVATAR_URI_KEY);
        if (avatarUriString == null || avatarUriString.isBlank()) {
            return;
        }
        applyAvatarUrl(avatarUriString);
    }

    private void applySelectedAvatar(Uri uri) {
        if (avatarImageView == null || uri == null) {
            return;
        }
        avatarLoadRequestToken++;
        avatarImageView.setImageURI(uri);
        avatarImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatarImageView.setPadding(0, 0, 0, 0);
        avatarImageView.setImageTintList(null);
    }

    private void setupFormValidationInteractions() {
        if (nameEditText != null) {
            nameEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (nameInputLayout != null) {
                        nameInputLayout.setError(null);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        if (emailEditText != null) {
            emailEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (emailInputLayout != null) {
                        emailInputLayout.setError(null);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        if (phoneEditText != null) {
            phoneEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (phoneInputLayout != null) {
                        phoneInputLayout.setError(null);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable == null || isFormattingPhoneInput || phoneEditText == null) {
                        return;
                    }
                    String raw = editable.toString();
                    String digitsOnly = raw.replaceAll("\\D+", "");
                    if (digitsOnly.length() > 10) {
                        digitsOnly = digitsOnly.substring(0, 10);
                    }
                    if (raw.equals(digitsOnly)) {
                        return;
                    }
                    isFormattingPhoneInput = true;
                    phoneEditText.setText(digitsOnly);
                    phoneEditText.setSelection(digitsOnly.length());
                    isFormattingPhoneInput = false;
                }
            });
        }

        if (addressEditText != null) {
            addressEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (addressInputLayout != null) {
                        addressInputLayout.setError(null);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private boolean validateAccountDetailsInputs() {
        boolean isValid = true;

        String name = getTextValue(nameEditText);
        String email = getTextValue(emailEditText);
        String phone = getTextValue(phoneEditText);
        String address = getTextValue(addressEditText);

        if (nameInputLayout != null) {
            nameInputLayout.setError(null);
            if (name.isEmpty()) {
                nameInputLayout.setError(getString(R.string.account_details_name_required));
                isValid = false;
            }
        }

        if (emailInputLayout != null) {
            emailInputLayout.setError(null);
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInputLayout.setError(getString(R.string.account_details_email_error));
                isValid = false;
            }
        }

        if (phoneInputLayout != null) {
            phoneInputLayout.setError(null);
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                phoneInputLayout.setError(getString(R.string.account_details_phone_error));
                isValid = false;
            }
        }

        if (addressInputLayout != null) {
            addressInputLayout.setError(null);
            if (address.isEmpty()) {
                addressInputLayout.setError(getString(R.string.account_details_address_required));
                isValid = false;
            }
        }

        return isValid;
    }

    private String getTextValue(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private void setupLocationPermissionLauncher() {
        locationPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    boolean hasFine =
                            Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                    boolean hasCoarse =
                            Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                    if (hasFine || hasCoarse) {
                        requestCurrentLocationAndFillAddress();
                    } else {
                        Toast.makeText(
                                this,
                                R.string.account_details_location_permission_denied,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void pickAddressFromCurrentLocation() {
        if (!hasLocationPermission()) {
            if (locationPermissionLauncher != null) {
                locationPermissionLauncher.launch(new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
            return;
        }
        requestCurrentLocationAndFillAddress();
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void requestCurrentLocationAndFillAddress() {
        setPickButtonLoading(true);
        if (locationProvider == null) {
            onLocationRequestFailed(DeviceLocationProvider.Error.LOCATION_UNAVAILABLE);
            return;
        }
        locationProvider.requestCurrentLocation(new DeviceLocationProvider.Callback() {
            @Override
            public void onLocation(@androidx.annotation.NonNull Location location) {
                resolveAddressFromLocation(location);
            }

            @Override
            public void onError(@androidx.annotation.NonNull DeviceLocationProvider.Error error) {
                onLocationRequestFailed(error);
            }
        });
    }

    private void onLocationRequestFailed(DeviceLocationProvider.Error error) {
        setPickButtonLoading(false);
        int messageRes = error == DeviceLocationProvider.Error.LOCATION_DISABLED
                ? R.string.account_details_location_disabled
                : R.string.account_details_location_unavailable;
        Toast.makeText(
                this,
                messageRes,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void resolveAddressFromLocation(Location location) {
        LatLng target = new LatLng(location.getLatitude(), location.getLongitude());
        currentUserLatLng = target;
        placeOrMoveCurrentLocationMarker(target, true);

        geocodeExecutor.execute(() -> {
            String fallback = getString(
                    R.string.account_details_lat_lng_fallback,
                    target.getLatitude(),
                    target.getLongitude()
            );
            String resolvedAddress = addressResolver != null
                    ? addressResolver.reverseGeocode(target, fallback).getFullAddress()
                    : fallback;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setPickButtonLoading(false);

                if (addressEditText == null) {
                    return;
                }

                addressEditText.setText(resolvedAddress);
                if (addressEditText.getText() != null) {
                    addressEditText.setSelection(addressEditText.getText().length());
                }
            });
        });
    }

    private void placeOrMoveCurrentLocationMarker(@androidx.annotation.NonNull LatLng target, boolean animateCamera) {
        if (trackAsiaMap == null) {
            return;
        }

        if (currentLocationMarker != null) {
            trackAsiaMap.removeMarker(currentLocationMarker);
        }
        currentLocationMarker = trackAsiaMap.addMarker(new MarkerOptions().position(target));

        if (animateCamera) {
            trackAsiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 15.5));
        }
    }

    private void setPickButtonLoading(boolean isLoading) {
        if (pickPreciseAddressButton == null) {
            return;
        }
        pickPreciseAddressButton.setEnabled(!isLoading);
        pickPreciseAddressButton.setText(
                isLoading
                        ? R.string.account_details_pick_location_loading
                        : R.string.account_details_pick_precise_location
        );
    }

    private void saveCurrentProfile() {
        if (profileRepository == null) {
            return;
        }
        setProfileLoading(true, ProfileAction.SAVE);
        profileRepository.createOrUpdateCurrentUserProfile(
                buildCurrentProfileFromInputs(),
                new ResultCallback<UserProfileData>() {
                    @Override
                    public void onSuccess(UserProfileData data) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        setProfileLoading(false, null);
                        bindUserProfile(data);
                        Toast.makeText(
                                AccountDetailsActivity.this,
                                R.string.account_details_saved_success,
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(@androidx.annotation.NonNull NetworkError error) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        setProfileLoading(false, null);
                        Toast.makeText(
                                AccountDetailsActivity.this,
                                getString(R.string.account_details_profile_save_failed, error.getMessage()),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    @androidx.annotation.NonNull
    private UserProfileData buildCurrentProfileFromInputs() {
        Long taiKhoanId = authRepository != null ? authRepository.getCurrentTaiKhoanId() : null;
        String tenDangNhap = authRepository != null ? authRepository.getCurrentUsername() : null;
        String avatarUri = selectedAvatarUri != null ? selectedAvatarUri.toString() : null;
        String latitude = currentUserLatLng != null
                ? String.format(Locale.US, "%.6f", currentUserLatLng.getLatitude())
                : null;
        String longitude = currentUserLatLng != null
                ? String.format(Locale.US, "%.6f", currentUserLatLng.getLongitude())
                : null;

        return new UserProfileData(
                currentNguoiDungId,
                taiKhoanId,
                tenDangNhap,
                getTextValue(nameEditText),
                getTextValue(emailEditText),
                getTextValue(phoneEditText),
                getTextValue(addressEditText),
                latitude,
                longitude,
                avatarUri
        );
    }

    private void confirmDeleteCurrentProfile() {
        if (profileRepository == null) {
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.account_details_delete_title)
                .setMessage(R.string.account_details_delete_confirm_message)
                .setNegativeButton(R.string.account_details_delete_keep, null)
                .setPositiveButton(
                        R.string.account_details_delete_action,
                        (dialog, which) -> deleteCurrentProfile()
                )
                .show();
    }

    private void deleteCurrentProfile() {
        if (profileRepository == null) {
            return;
        }
        setProfileLoading(true, ProfileAction.DELETE);
        profileRepository.deleteCurrentUserProfile(new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setProfileLoading(false, null);
                clearAllProfileInputs();
                populateMinimalInfoFromSession();
                Toast.makeText(
                        AccountDetailsActivity.this,
                        R.string.account_details_delete_success,
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onError(@androidx.annotation.NonNull NetworkError error) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setProfileLoading(false, null);
                Toast.makeText(
                        AccountDetailsActivity.this,
                        getString(R.string.account_details_profile_delete_failed, error.getMessage()),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void clearAllProfileInputs() {
        currentNguoiDungId = null;
        if (nameEditText != null) {
            nameEditText.setText("");
        }
        if (emailEditText != null) {
            emailEditText.setText("");
        }
        if (phoneEditText != null) {
            phoneEditText.setText("");
        }
        if (addressEditText != null) {
            addressEditText.setText("");
        }
        if (nameInputLayout != null) {
            nameInputLayout.setError(null);
        }
        if (emailInputLayout != null) {
            emailInputLayout.setError(null);
        }
        if (phoneInputLayout != null) {
            phoneInputLayout.setError(null);
        }
        if (addressInputLayout != null) {
            addressInputLayout.setError(null);
        }
        clearAvatarSelection();
        clearCurrentLocationSelection();
    }

    private void clearAvatarSelection() {
        avatarLoadRequestToken++;
        selectedAvatarUri = null;
        applyDefaultAvatar();
    }

    private void applyDefaultAvatar() {
        if (avatarImageView == null) {
            return;
        }
        int paddingPx = Math.round(22f * getResources().getDisplayMetrics().density);
        avatarImageView.setImageResource(R.drawable.ic_nav_profile);
        avatarImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        avatarImageView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        avatarImageView.setImageTintList(ContextCompat.getColorStateList(this, R.color.color_primary));
    }

    private void clearCurrentLocationSelection() {
        currentUserLatLng = null;
        if (trackAsiaMap != null) {
            if (currentLocationMarker != null) {
                trackAsiaMap.removeMarker(currentLocationMarker);
                currentLocationMarker = null;
            }
            trackAsiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(HANOI, DEFAULT_ZOOM));
        }
    }

    private void loadUserProfileData() {
        if (profileRepository == null) {
            return;
        }
        setProfileLoading(true, ProfileAction.LOAD);
        profileRepository.getCurrentUserProfile(new ResultCallback<UserProfileData>() {
            @Override
            public void onSuccess(UserProfileData data) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setProfileLoading(false, null);
                bindUserProfile(data);
            }

            @Override
            public void onError(@androidx.annotation.NonNull NetworkError error) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setProfileLoading(false, null);
                if (error.isUnauthorized()) {
                    if (authRepository != null) {
                        authRepository.clearSession();
                    }
                    Toast.makeText(
                            AccountDetailsActivity.this,
                            R.string.auth_session_expired,
                            Toast.LENGTH_LONG
                    ).show();
                    navigateToLogin();
                    return;
                }
                if (error.getStatusCode() == 403) {
                    if (authRepository != null) {
                        authRepository.clearSession();
                    }
                    Toast.makeText(
                            AccountDetailsActivity.this,
                            R.string.auth_role_not_supported,
                            Toast.LENGTH_LONG
                    ).show();
                    navigateToLogin();
                    return;
                }
                Toast.makeText(
                        AccountDetailsActivity.this,
                        getString(R.string.account_details_profile_load_failed, error.getMessage()),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void bindUserProfile(UserProfileData profile) {
        if (profile == null) {
            return;
        }
        currentNguoiDungId = profile.getNguoiDungId();

        String fallbackUsername = authRepository != null ? authRepository.getCurrentUsername() : null;
        String hoTen = firstNonBlank(profile.getHoTen(), fallbackUsername);
        if (nameEditText != null) {
            nameEditText.setText(hoTen != null ? hoTen : "");
        }

        String email = profile.getEmail();
        if (emailEditText != null) {
            emailEditText.setText(email != null ? email : "");
        }

        String sdt = normalizePhoneDigits(profile.getSoDienThoai());
        if (phoneEditText != null) {
            phoneEditText.setText(sdt != null ? sdt : "");
        }

        String diaChi = profile.getDiaChi();
        if (addressEditText != null) {
            addressEditText.setText(diaChi != null ? diaChi : "");
        }

        if (profile.getAvatarUrl() != null) {
            applyAvatarUrl(profile.getAvatarUrl());
        } else {
            clearAvatarSelection();
        }

        LatLng profileLatLng = parseLatLng(profile.getViDo(), profile.getKinhDo());
        if (profileLatLng != null) {
            currentUserLatLng = profileLatLng;
            placeOrMoveCurrentLocationMarker(profileLatLng, true);
        } else {
            clearCurrentLocationSelection();
        }
    }

    private void setProfileLoading(boolean loading, ProfileAction action) {
        if (nameEditText != null) {
            nameEditText.setEnabled(!loading);
        }
        if (emailEditText != null) {
            emailEditText.setEnabled(!loading);
        }
        if (phoneEditText != null) {
            phoneEditText.setEnabled(!loading);
        }
        if (addressEditText != null) {
            addressEditText.setEnabled(!loading);
        }
        if (pickPreciseAddressButton != null) {
            pickPreciseAddressButton.setEnabled(!loading);
        }
        if (saveButton != null) {
            saveButton.setEnabled(!loading);
            int saveLabelRes = R.string.account_details_loading_data;
            if (action == ProfileAction.SAVE) {
                saveLabelRes = R.string.account_details_saving_data;
            }
            saveButton.setText(loading ? getString(saveLabelRes) : defaultSaveButtonText);
            saveButton.setAlpha(loading ? 0.85f : 1f);
        }
        if (deleteButton != null) {
            deleteButton.setEnabled(!loading);
            deleteButton.setText(
                    loading && action == ProfileAction.DELETE
                            ? getString(R.string.account_details_deleting_data)
                            : defaultDeleteButtonText
            );
            deleteButton.setAlpha(loading ? 0.85f : 1f);
        }
    }

    private void populateMinimalInfoFromSession() {
        if (authRepository == null) {
            return;
        }
        String username = authRepository.getCurrentUsername();
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        if (nameEditText != null && getTextValue(nameEditText).isEmpty()) {
            nameEditText.setText(username);
        }
        if (emailEditText != null
                && getTextValue(emailEditText).isEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            emailEditText.setText(username);
        }
    }

    private void applyAvatarUrl(String avatarUrl) {
        if (avatarImageView == null || avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return;
        }
        try {
            Uri uri = Uri.parse(avatarUrl.trim());
            String scheme = uri.getScheme();
            if (scheme == null) {
                return;
            }
            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                loadRemoteAvatar(avatarUrl.trim());
                return;
            }
            if (!"content".equalsIgnoreCase(scheme) && !"file".equalsIgnoreCase(scheme)) {
                return;
            }
            selectedAvatarUri = uri;
            applySelectedAvatar(uri);
        } catch (Exception ignored) {
            // Keep default avatar if URL cannot be parsed.
        }
    }

    private void loadRemoteAvatar(String avatarUrl) {
        final int requestToken = ++avatarLoadRequestToken;
        avatarExecutor.execute(() -> {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(avatarUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(6000);
                connection.setReadTimeout(8000);
                connection.setInstanceFollowRedirects(true);
                connection.connect();
                int statusCode = connection.getResponseCode();
                if (statusCode >= 200 && statusCode < 300) {
                    inputStream = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                }
            } catch (Exception ignored) {
                bitmap = null;
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception ignored) {
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }

            Bitmap loadedBitmap = bitmap;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (requestToken != avatarLoadRequestToken || avatarImageView == null) {
                    return;
                }
                if (loadedBitmap == null) {
                    return;
                }
                selectedAvatarUri = Uri.parse(avatarUrl);
                avatarImageView.setImageBitmap(loadedBitmap);
                avatarImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                avatarImageView.setPadding(0, 0, 0, 0);
                avatarImageView.setImageTintList(null);
            });
        });
    }

    private LatLng parseLatLng(String lat, String lng) {
        if (lat == null || lng == null) {
            return null;
        }
        try {
            return new LatLng(Double.parseDouble(lat.trim()), Double.parseDouble(lng.trim()));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String normalizePhoneDigits(String rawPhone) {
        if (rawPhone == null) {
            return null;
        }
        String digits = rawPhone.replaceAll("\\D+", "");
        if (digits.isEmpty()) {
            return null;
        }
        if (digits.length() > 10) {
            return digits.substring(0, 10);
        }
        return digits;
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) {
            return first.trim();
        }
        if (second != null && !second.trim().isEmpty()) {
            return second.trim();
        }
        return null;
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
