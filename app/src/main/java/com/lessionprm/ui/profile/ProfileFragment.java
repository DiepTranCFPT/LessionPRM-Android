package com.lessionprm.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.lessionprm.R;
import com.lessionprm.data.api.RetrofitClient;
import com.lessionprm.data.model.UpdateProfileRequest;
import com.lessionprm.data.model.UpdateProfileResponse;
import com.lessionprm.data.model.User;
import com.lessionprm.data.model.UserProfileResponse;
import com.lessionprm.ui.auth.LoginActivity;
import com.lessionprm.utils.NetworkUtils;
import com.lessionprm.utils.PrefsHelper;
import com.lessionprm.utils.ValidationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvEmail, tvRole, tvMemberSince;
    private TextInputLayout tilFullName, tilPhone;
    private TextInputEditText etFullName, etPhone;
    private MaterialButton btnSave, btnLogout;
    private ProgressBar progressBar;

    private User currentUser;
    private boolean isEditing = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupClickListeners();
        loadUserProfile();
    }

    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvEmail = view.findViewById(R.id.tv_email);
        tvRole = view.findViewById(R.id.tv_role);
        tvMemberSince = view.findViewById(R.id.tv_member_since);
        
        tilFullName = view.findViewById(R.id.til_full_name);
        tilPhone = view.findViewById(R.id.til_phone);
        etFullName = view.findViewById(R.id.et_full_name);
        etPhone = view.findViewById(R.id.et_phone);
        
        btnSave = view.findViewById(R.id.btn_save);
        btnLogout = view.findViewById(R.id.btn_logout);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> {
            if (isEditing) {
                saveProfile();
            } else {
                enableEditing();
            }
        });
        
        btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void loadUserProfile() {
        // First display cached user info
        displayCachedUserInfo();
        
        // Then fetch updated profile from server
        if (!ValidationUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        RetrofitClient.getApiService().getProfile()
                .enqueue(new Callback<UserProfileResponse>() {
                    @Override
                    public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                        showLoading(false);

                        if (NetworkUtils.isResponseSuccessful(response)) {
                            UserProfileResponse profileResponse = response.body();

                            if (profileResponse.isSuccess() && profileResponse.getUser() != null) {
                                currentUser = profileResponse.getUser();
                                displayUserProfile();
                                updateCachedUserInfo();
                            } else {
                                showError(profileResponse.getMessage());
                            }
                        } else {
                            if (NetworkUtils.isAuthError(response.code())) {
                                handleAuthError();
                            } else {
                                showError(NetworkUtils.getErrorMessage(response.code()));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                        showLoading(false);
                        showError(NetworkUtils.getNetworkErrorMessage(t));
                    }
                });
    }

    private void displayCachedUserInfo() {
        if (getContext() != null) {
            tvEmail.setText(PrefsHelper.getUserEmail(getContext()));
            tvRole.setText(PrefsHelper.getUserRole(getContext()));
            etFullName.setText(PrefsHelper.getUserName(getContext()));
        }
    }

    private void displayUserProfile() {
        if (currentUser == null) return;

        // Display basic info
        tvEmail.setText(currentUser.getEmail());
        tvRole.setText(currentUser.getRole());
        
        if (currentUser.getCreatedAt() != null) {
            tvMemberSince.setText("Thành viên từ: " + currentUser.getCreatedAt());
        }

        // Display editable fields
        etFullName.setText(currentUser.getFullName());
        etPhone.setText(currentUser.getPhone());

        // Load avatar
        if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentUser.getAvatarUrl())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .circleCrop()
                    .into(ivAvatar);
        }
    }

    private void updateCachedUserInfo() {
        if (getContext() != null && currentUser != null) {
            PrefsHelper.saveUserInfo(getContext(),
                    currentUser.getId(),
                    currentUser.getEmail(),
                    currentUser.getFullName(),
                    currentUser.getRole());
        }
    }

    private void enableEditing() {
        isEditing = true;
        etFullName.setEnabled(true);
        etPhone.setEnabled(true);
        btnSave.setText("Lưu");
    }

    private void disableEditing() {
        isEditing = false;
        etFullName.setEnabled(false);
        etPhone.setEnabled(false);
        btnSave.setText("Chỉnh sửa");
    }

    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Clear previous errors
        tilFullName.setError(null);
        tilPhone.setError(null);

        // Validate inputs
        boolean isValid = true;

        String nameError = ValidationUtils.getNameError(fullName);
        if (nameError != null) {
            tilFullName.setError(nameError);
            isValid = false;
        }

        if (!phone.isEmpty()) {
            String phoneError = ValidationUtils.getPhoneError(phone);
            if (phoneError != null) {
                tilPhone.setError(phoneError);
                isValid = false;
            }
        }

        if (!isValid) return;

        if (!ValidationUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        btnSave.setEnabled(false);

        UpdateProfileRequest request = new UpdateProfileRequest(fullName, phone);

        RetrofitClient.getApiService().updateProfile(request)
                .enqueue(new Callback<UpdateProfileResponse>() {
                    @Override
                    public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                        showLoading(false);
                        btnSave.setEnabled(true);

                        if (NetworkUtils.isResponseSuccessful(response)) {
                            UpdateProfileResponse updateResponse = response.body();

                            if (updateResponse.isSuccess()) {
                                currentUser = updateResponse.getUser();
                                updateCachedUserInfo();
                                disableEditing();
                                Toast.makeText(getContext(), "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                            } else {
                                showError(updateResponse.getMessage());
                            }
                        } else {
                            showError(NetworkUtils.getErrorMessage(response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                        showLoading(false);
                        btnSave.setEnabled(true);
                        showError(NetworkUtils.getNetworkErrorMessage(t));
                    }
                });
    }

    private void handleLogout() {
        if (getContext() != null) {
            PrefsHelper.clearAuthData(getContext());
            
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }

    private void handleAuthError() {
        if (getContext() != null) {
            PrefsHelper.clearAuthData(getContext());
            Toast.makeText(getContext(), "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}