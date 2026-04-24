package com.example.cuutro.features.report.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cuutro.R;
import com.example.cuutro.app.MyApp;
import com.example.cuutro.core.location.DeviceLocationProvider;
import com.example.cuutro.core.location.LocationAddressResolver;
import com.example.cuutro.core.location.ReportLocationBottomSheet;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.profile.data.ProfileRepository;
import com.example.cuutro.features.profile.data.model.UserProfileData;
import com.example.cuutro.features.report.data.ReportRepository;
import com.example.cuutro.features.report.data.model.ReportEmergencyType;
import com.example.cuutro.features.report.data.model.ReportSupplyItem;
import com.example.cuutro.features.report.ui.controller.EmergencyTypeSectionController;
import com.example.cuutro.features.report.ui.controller.ReportAttachmentSectionController;
import com.example.cuutro.features.report.ui.controller.ReportBitmapLoader;
import com.example.cuutro.features.report.ui.controller.SupplySectionController;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.trackasia.android.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class ReportActivity extends AppCompatActivity {

    private static final LatLng DEFAULT_LOCATION = new LatLng(21.0285, 105.8542);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+?\\d{9,15}|0\\d{9,10})$");

    private final List<ReportEmergencyType> emergencyTypes = new ArrayList<>();

    private TextView reportLocationValueText;
    private String selectedLocationText;
    private LatLng selectedLocationLatLng = DEFAULT_LOCATION;
    private MaterialButton submitButton;
    private TextInputLayout reporterNameLayout;
    private TextInputLayout reporterPhoneLayout;
    private TextInputEditText reporterNameEditText;
    private TextInputEditText reporterPhoneEditText;
    private TextInputEditText emergencyBriefEditText;

    private ReportLocationBottomSheet reportLocationBottomSheet;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private AuthRepository authRepository;
    private ProfileRepository profileRepository;
    private ReportRepository reportRepository;
    private DeviceLocationProvider locationProvider;
    private LocationAddressResolver addressResolver;
    private final ExecutorService geocodeExecutor = Executors.newSingleThreadExecutor();
    private boolean waitingInitialLocationPermissionResult;

    private ReportBitmapLoader bitmapLoader;
    private EmergencyTypeSectionController emergencyTypeSectionController;
    private SupplySectionController supplySectionController;
    private ReportAttachmentSectionController reportAttachmentSectionController;
    private ReportEmergencyType selectedEmergencyType;

    private boolean submitEnabledByData = true;
    private boolean submitInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        reportLocationValueText = findViewById(R.id.tvReportLocationValue);
        reporterNameLayout = findViewById(R.id.layoutReporterName);
        reporterPhoneLayout = findViewById(R.id.layoutReporterPhone);
        reporterNameEditText = findViewById(R.id.edtReporterName);
        reporterPhoneEditText = findViewById(R.id.edtReporterPhone);
        emergencyBriefEditText = findViewById(R.id.edtEmergencyBrief);
        selectedLocationText = reportLocationValueText != null
                ? reportLocationValueText.getText().toString().trim()
                : getString(R.string.report_location_value);

        initDependencies();
        initSectionControllers();
        initAttachmentSectionController();
        setupLocationPermissionLauncher();
        setupLocationBottomSheet();
        setupActions();
        prefillReporterInfo();
        loadInitialUserLocationAddress();
        loadEmergencyTypesFromBackend();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (reportLocationBottomSheet != null) {
            reportLocationBottomSheet.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (reportLocationBottomSheet != null) {
            reportLocationBottomSheet.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (reportAttachmentSectionController != null) {
            reportAttachmentSectionController.onPause();
        }
        if (reportLocationBottomSheet != null) {
            reportLocationBottomSheet.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (reportLocationBottomSheet != null) {
            reportLocationBottomSheet.onStop();
        }
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (reportLocationBottomSheet != null) {
            reportLocationBottomSheet.onLowMemory();
        }
    }

    @Override
    protected void onDestroy() {
        if (emergencyTypeSectionController != null) {
            emergencyTypeSectionController.release();
            emergencyTypeSectionController = null;
        }
        if (supplySectionController != null) {
            supplySectionController.release();
            supplySectionController = null;
        }
        if (reportAttachmentSectionController != null) {
            reportAttachmentSectionController.onDestroy();
            reportAttachmentSectionController = null;
        }
        if (bitmapLoader != null) {
            bitmapLoader.shutdown();
            bitmapLoader = null;
        }
        if (reportLocationBottomSheet != null) {
            reportLocationBottomSheet.onDestroy();
            reportLocationBottomSheet = null;
        }
        if (locationProvider != null) {
            locationProvider.cancel();
            locationProvider = null;
        }
        geocodeExecutor.shutdownNow();
        super.onDestroy();
    }

    private void initDependencies() {
        if (!(getApplication() instanceof MyApp)) {
            return;
        }
        MyApp app = (MyApp) getApplication();
        if (app.getAppContainer() == null) {
            return;
        }
        authRepository = app.getAppContainer().getAuthRepository();
        profileRepository = app.getAppContainer().getProfileRepository();
        reportRepository = app.getAppContainer().getReportRepository();
        locationProvider = new DeviceLocationProvider(this);
        addressResolver = new LocationAddressResolver(this, new Locale("vi", "VN"));
    }

    private void prefillReporterInfo() {
        if (authRepository == null || !authRepository.hasActiveSession()) {
            clearReporterInfo();
            return;
        }
        if (profileRepository == null) {
            clearReporterInfo();
            return;
        }

        profileRepository.getCurrentUserProfile(new ResultCallback<UserProfileData>() {
            @Override
            public void onSuccess(UserProfileData data) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                String hoTen = data != null ? trimToNull(data.getHoTen()) : null;
                String soDienThoai = normalizePhoneDigits(data != null ? data.getSoDienThoai() : null);
                applyReporterInfoIfEmpty(hoTen, soDienThoai);
            }

            @Override
            public void onError(@androidx.annotation.NonNull NetworkError error) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (error.isUnauthorized() && authRepository != null) {
                    authRepository.clearSession();
                }
            }
        });
    }

    private void clearReporterInfo() {
        if (reporterNameEditText != null) {
            reporterNameEditText.setText("");
        }
        if (reporterPhoneEditText != null) {
            reporterPhoneEditText.setText("");
        }
    }

    private void applyReporterInfoIfEmpty(String hoTen, String soDienThoai) {
        if (reporterNameEditText != null && getNormalizedInput(reporterNameEditText).isEmpty()) {
            reporterNameEditText.setText(hoTen != null ? hoTen : "");
        }
        if (reporterPhoneEditText != null && getNormalizedInput(reporterPhoneEditText).isEmpty()) {
            reporterPhoneEditText.setText(soDienThoai != null ? soDienThoai : "");
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void initSectionControllers() {
        GridLayout emergencyGrid = findViewById(R.id.gridEmergencyTypes);
        LinearLayout supplyListContainer = findViewById(R.id.layoutReportSupplyList);
        TextView supplyStateText = findViewById(R.id.tvReportSupplyState);

        bitmapLoader = new ReportBitmapLoader();

        if (emergencyGrid != null) {
            emergencyTypeSectionController = new EmergencyTypeSectionController(
                    this,
                    emergencyGrid,
                    bitmapLoader,
                    (emergencyType, position) -> {
                        selectedEmergencyType = emergencyType;
                        loadSuppliesForSelectedEmergency();
                    }
            );
        }

        if (supplyListContainer != null && supplyStateText != null) {
            supplySectionController = new SupplySectionController(
                    this,
                    supplyListContainer,
                    supplyStateText,
                    bitmapLoader
            );
            supplySectionController.showState(getString(R.string.report_supply_select_incident_hint));
            supplySectionController.clear();
        }
    }

    private void initAttachmentSectionController() {
        reportAttachmentSectionController = new ReportAttachmentSectionController(this);
    }

    private void setupActions() {
        ImageButton backButton = findViewById(R.id.btnBack);
        submitButton = findViewById(R.id.btnSubmitReport);
        TextView changeLocationText = findViewById(R.id.tvChangeLocation);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        if (changeLocationText != null) {
            changeLocationText.setOnClickListener(v -> {
                if (reportLocationBottomSheet != null) {
                    reportLocationBottomSheet.show(selectedLocationText, selectedLocationLatLng);
                }
            });
        }
        if (submitButton != null) {
            submitButton.setOnClickListener(v -> {
                if (submitInProgress) {
                    return;
                }
                if (selectedEmergencyType == null) {
                    Toast.makeText(this, R.string.report_select_type_first, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isSelectedEmergencyTypeValid()) {
                    Toast.makeText(this, R.string.report_invalid_selected_type, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!validateReporterInfo()) {
                    return;
                }
                if (!validateLocation()) {
                    return;
                }
                if (!validateSelectedSupplies()) {
                    return;
                }
                if (!validateEmergencyBrief()) {
                    return;
                }
                if (!validateAttachmentSelection()) {
                    return;
                }
                submitRescueTicket();
            });
        }
        applySubmitButtonState();
    }

    private void setupLocationBottomSheet() {
        reportLocationBottomSheet = new ReportLocationBottomSheet(
                this,
                getString(R.string.trackasia_style_url),
                new ReportLocationBottomSheet.Listener() {
                    @Override
                    public boolean hasLocationPermission() {
                        return ReportActivity.this.hasLocationPermission();
                    }

                    @Override
                    public void requestLocationPermission() {
                        if (locationPermissionLauncher != null) {
                            locationPermissionLauncher.launch(new String[] {
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            });
                        }
                    }

                    @Override
                    public void onLocationConfirmed(String locationText, LatLng latLng) {
                        selectedLocationText = locationText;
                        if (latLng != null) {
                            selectedLocationLatLng = latLng;
                        }
                        if (reportLocationValueText != null) {
                            reportLocationValueText.setText(locationText);
                        }
                    }
                }
        );
    }

    private void setupLocationPermissionLauncher() {
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean hasFine = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                    boolean hasCoarse = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                    boolean granted = hasFine || hasCoarse;

                    boolean handleInitialLocation = waitingInitialLocationPermissionResult;
                    waitingInitialLocationPermissionResult = false;

                    if (handleInitialLocation && granted) {
                        requestCurrentLocationAndFillAddress();
                    }

                    if (reportLocationBottomSheet != null) {
                        reportLocationBottomSheet.onLocationPermissionResult(granted);
                    }
                }
        );
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void loadInitialUserLocationAddress() {
        if (hasLocationPermission()) {
            requestCurrentLocationAndFillAddress();
            return;
        }

        if (locationPermissionLauncher == null) {
            return;
        }

        waitingInitialLocationPermissionResult = true;
        locationPermissionLauncher.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    @SuppressLint("MissingPermission")
    private void requestCurrentLocationAndFillAddress() {
        if (locationProvider == null) {
            return;
        }
        if (reportLocationValueText != null) {
            reportLocationValueText.setText(getString(R.string.report_location_loading));
        }

        locationProvider.requestCurrentLocation(new DeviceLocationProvider.Callback() {
            @Override
            public void onLocation(@androidx.annotation.NonNull Location location) {
                resolveAddressFromLocation(location);
            }

            @Override
            public void onError(@androidx.annotation.NonNull DeviceLocationProvider.Error error) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (reportLocationValueText != null) {
                    reportLocationValueText.setText(selectedLocationText);
                }
            }
        });
    }

    private void resolveAddressFromLocation(@androidx.annotation.NonNull Location location) {
        LatLng target = new LatLng(location.getLatitude(), location.getLongitude());

        geocodeExecutor.execute(() -> {
            String fallback = getString(
                    R.string.report_lat_lng_fallback,
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

                selectedLocationLatLng = target;
                selectedLocationText = resolvedAddress;
                if (reportLocationValueText != null) {
                    reportLocationValueText.setText(resolvedAddress);
                }
            });
        });
    }

    private void loadEmergencyTypesFromBackend() {
        if (reportRepository == null) {
            Toast.makeText(this, R.string.report_load_types_missing_dependency, Toast.LENGTH_LONG).show();
            setSubmitEnabled(false);
            return;
        }

        setSubmitEnabled(false);
        reportRepository.getActiveEmergencyTypes(new ResultCallback<List<ReportEmergencyType>>() {
            @Override
            public void onSuccess(List<ReportEmergencyType> data) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                emergencyTypes.clear();
                if (data != null) {
                    emergencyTypes.addAll(data);
                }

                if (emergencyTypes.isEmpty()) {
                    selectedEmergencyType = null;
                    if (emergencyTypeSectionController != null) {
                        emergencyTypeSectionController.clear();
                    }
                    if (supplySectionController != null) {
                        supplySectionController.clear();
                        supplySectionController.showState(getString(R.string.report_supply_select_incident_hint));
                    }
                    Toast.makeText(
                            ReportActivity.this,
                            R.string.report_no_active_types,
                            Toast.LENGTH_LONG
                    ).show();
                    setSubmitEnabled(false);
                    return;
                }

                if (emergencyTypeSectionController != null) {
                    emergencyTypeSectionController.setItems(emergencyTypes);
                } else {
                    selectedEmergencyType = emergencyTypes.get(0);
                    loadSuppliesForSelectedEmergency();
                }
                setSubmitEnabled(true);
            }

            @Override
            public void onError(NetworkError error) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                selectedEmergencyType = null;
                emergencyTypes.clear();

                if (emergencyTypeSectionController != null) {
                    emergencyTypeSectionController.clear();
                }
                if (supplySectionController != null) {
                    supplySectionController.clear();
                    supplySectionController.showState(getString(R.string.report_supply_select_incident_hint));
                }

                String message = error == null ? "" : error.getMessage();
                Toast.makeText(
                        ReportActivity.this,
                        getString(R.string.report_load_types_failed, message),
                        Toast.LENGTH_LONG
                ).show();
                setSubmitEnabled(false);
            }
        });
    }

    private void loadSuppliesForSelectedEmergency() {
        if (supplySectionController == null) {
            return;
        }

        if (selectedEmergencyType == null) {
            supplySectionController.clear();
            supplySectionController.showState(getString(R.string.report_supply_select_incident_hint));
            return;
        }

        if (reportRepository == null) {
            supplySectionController.clear();
            supplySectionController.showState(getString(R.string.report_load_types_missing_dependency));
            return;
        }

        Long loaiSuCoId = selectedEmergencyType.getId();
        if (loaiSuCoId == null || loaiSuCoId <= 0) {
            supplySectionController.clear();
            supplySectionController.showState(getString(R.string.report_supply_empty));
            return;
        }

        supplySectionController.showState(getString(R.string.report_supply_loading));
        reportRepository.getSuppliesByEmergencyType(loaiSuCoId, new ResultCallback<List<ReportSupplyItem>>() {
            @Override
            public void onSuccess(List<ReportSupplyItem> data) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                supplySectionController.setItems(data);
                if (supplySectionController.isEmpty()) {
                    supplySectionController.showState(getString(R.string.report_supply_empty));
                    return;
                }
                supplySectionController.hideState();
            }

            @Override
            public void onError(NetworkError error) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                supplySectionController.clear();
                String message = error == null ? "" : error.getMessage();
                supplySectionController.showState(getString(R.string.report_supply_load_failed, message));
            }
        });
    }

    private void setSubmitEnabled(boolean enabled) {
        submitEnabledByData = enabled;
        applySubmitButtonState();
    }

    private void applySubmitButtonState() {
        if (submitButton == null) {
            return;
        }
        boolean enabled = submitEnabledByData && !submitInProgress;
        submitButton.setEnabled(enabled);
        submitButton.setAlpha(enabled ? 1f : 0.5f);
    }

    private void setSubmitting(boolean submitting) {
        submitInProgress = submitting;
        if (submitButton != null) {
            submitButton.setText(submitting ? R.string.report_submit_loading : R.string.report_submit);
        }
        applySubmitButtonState();
    }

    private boolean isSelectedEmergencyTypeValid() {
        if (selectedEmergencyType == null) {
            return false;
        }
        Long selectedId = selectedEmergencyType.getId();
        if (selectedId == null || selectedId <= 0) {
            return false;
        }
        for (ReportEmergencyType emergencyType : emergencyTypes) {
            if (emergencyType == null || emergencyType.getId() == null) {
                continue;
            }
            if (selectedId.equals(emergencyType.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean validateReporterInfo() {
        if (reporterNameLayout != null) {
            reporterNameLayout.setError(null);
        }
        if (reporterPhoneLayout != null) {
            reporterPhoneLayout.setError(null);
        }

        String reporterName = getNormalizedInput(reporterNameEditText);
        String reporterPhone = getNormalizedInput(reporterPhoneEditText).replaceAll("\\s+", "");

        if (reporterName.isEmpty()) {
            if (reporterNameLayout != null) {
                reporterNameLayout.setError(getString(R.string.report_reporter_name_required));
            }
            if (reporterNameEditText != null) {
                reporterNameEditText.requestFocus();
            }
            return false;
        }

        if (reporterPhone.isEmpty()) {
            if (reporterPhoneLayout != null) {
                reporterPhoneLayout.setError(getString(R.string.report_reporter_phone_required));
            }
            if (reporterPhoneEditText != null) {
                reporterPhoneEditText.requestFocus();
            }
            return false;
        }

        if (!PHONE_PATTERN.matcher(reporterPhone).matches()) {
            if (reporterPhoneLayout != null) {
                reporterPhoneLayout.setError(getString(R.string.report_reporter_phone_invalid));
            }
            if (reporterPhoneEditText != null) {
                reporterPhoneEditText.requestFocus();
            }
            return false;
        }

        return true;
    }

    private boolean validateLocation() {
        String address = normalizeLocationAddressForSubmission();
        if (address == null || address.trim().isEmpty()) {
            Toast.makeText(this, R.string.report_location_required, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateSelectedSupplies() {
        if (supplySectionController == null) {
            return false;
        }
        List<SupplySectionController.SelectedSupply> selectedSupplies = supplySectionController.getSelectedSupplies();
        if (selectedSupplies.isEmpty()) {
            Toast.makeText(this, R.string.report_supply_select_at_least_one, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateEmergencyBrief() {
        String emergencyBrief = getNormalizedInput(emergencyBriefEditText);
        if (emergencyBrief.isEmpty()) {
            Toast.makeText(this, R.string.report_brief_required, Toast.LENGTH_SHORT).show();
            if (emergencyBriefEditText != null) {
                emergencyBriefEditText.requestFocus();
            }
            return false;
        }
        return true;
    }

    private boolean validateAttachmentSelection() {
        if (reportAttachmentSectionController == null || !reportAttachmentSectionController.hasAnyAttachment()) {
            Toast.makeText(this, R.string.report_attachment_required, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void submitRescueTicket() {
        if (reportRepository == null) {
            Toast.makeText(this, R.string.report_submit_missing_dependency, Toast.LENGTH_LONG).show();
            return;
        }

        ReportRepository.CreateRescueTicketInput input = buildCreateRescueTicketInput();
        setSubmitting(true);
        reportRepository.createRescueTicket(input, new ResultCallback<Long>() {
            @Override
            public void onSuccess(Long data) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setSubmitting(false);
                if (data != null && data > 0) {
                    Toast.makeText(
                            ReportActivity.this,
                            getString(R.string.report_submit_success_with_id, data),
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    Toast.makeText(ReportActivity.this, R.string.report_submit_success, Toast.LENGTH_LONG).show();
                }
                finish();
            }

            @Override
            public void onError(@androidx.annotation.NonNull NetworkError error) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setSubmitting(false);
                String message = error.getMessage();
                if (message == null || message.trim().isEmpty()) {
                    Toast.makeText(
                            ReportActivity.this,
                            R.string.report_submit_failed_generic,
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }
                Toast.makeText(
                        ReportActivity.this,
                        getString(R.string.report_submit_failed, message),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private ReportRepository.CreateRescueTicketInput buildCreateRescueTicketInput() {
        Long loaiSuCoId = selectedEmergencyType != null ? selectedEmergencyType.getId() : null;
        String reporterName = getNormalizedInput(reporterNameEditText);
        String reporterPhone = getNormalizedInput(reporterPhoneEditText).replaceAll("\\s+", "");
        String address = normalizeLocationAddressForSubmission();
        LatLng selectedLatLng = selectedLocationLatLng != null ? selectedLocationLatLng : DEFAULT_LOCATION;
        String lat = formatCoordinate(selectedLatLng.getLatitude());
        String lng = formatCoordinate(selectedLatLng.getLongitude());
        String emergencyBrief = getNormalizedInput(emergencyBriefEditText);

        return new ReportRepository.CreateRescueTicketInput(
                loaiSuCoId,
                reporterName,
                reporterPhone,
                address,
                lat,
                lng,
                emergencyBrief,
                mapSelectedSupplyInputs(),
                collectAttachmentInputs()
        );
    }

    private List<ReportRepository.SelectedSupplyInput> mapSelectedSupplyInputs() {
        List<ReportRepository.SelectedSupplyInput> mapped = new ArrayList<>();
        if (supplySectionController == null) {
            return mapped;
        }

        List<SupplySectionController.SelectedSupply> selectedSupplies = supplySectionController.getSelectedSupplies();
        for (SupplySectionController.SelectedSupply selectedSupply : selectedSupplies) {
            if (selectedSupply == null || selectedSupply.getItem() == null) {
                continue;
            }
            mapped.add(new ReportRepository.SelectedSupplyInput(
                    selectedSupply.getItem().getId(),
                    selectedSupply.getQuantity()
            ));
        }
        return mapped;
    }

    private List<ReportRepository.AttachmentInput> collectAttachmentInputs() {
        List<ReportRepository.AttachmentInput> attachments = new ArrayList<>();
        if (reportAttachmentSectionController == null) {
            return attachments;
        }

        for (Uri imageUri : reportAttachmentSectionController.getSelectedImageUris()) {
            attachments.add(ReportRepository.AttachmentInput.image(imageUri));
        }

        Uri selectedVideoUri = reportAttachmentSectionController.getSelectedVideoUri();
        if (selectedVideoUri != null) {
            attachments.add(ReportRepository.AttachmentInput.video(selectedVideoUri));
        }

        Uri selectedAudioUri = reportAttachmentSectionController.getSelectedAudioUri();
        if (selectedAudioUri != null) {
            attachments.add(ReportRepository.AttachmentInput.audio(selectedAudioUri));
        }

        return attachments;
    }

    private String normalizeLocationAddressForSubmission() {
        String locationText = trimToNull(selectedLocationText);
        String loadingLabel = trimToNull(getString(R.string.report_location_loading));
        String defaultLabel = trimToNull(getString(R.string.report_location_value));
        if (locationText != null
                && !locationText.equalsIgnoreCase(loadingLabel)
                && !locationText.equalsIgnoreCase(defaultLabel)) {
            return locationText;
        }

        LatLng target = selectedLocationLatLng != null ? selectedLocationLatLng : DEFAULT_LOCATION;
        return getString(
                R.string.report_lat_lng_fallback,
                target.getLatitude(),
                target.getLongitude()
        );
    }

    private String formatCoordinate(double value) {
        return String.format(Locale.US, "%.6f", value);
    }

    private String getNormalizedInput(TextInputEditText input) {
        if (input == null || input.getText() == null) {
            return "";
        }
        return input.getText().toString().trim();
    }
}
