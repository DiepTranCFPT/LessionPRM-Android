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
import com.lessionprm.utils.PrefsHelper;
import com.lessionprm.utils.ValidationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private ImageView ivProfilePicture;
    private TextView tvUserEmail, tvUserRole, tvMemberSince;
    private TextInputLayout tilFullName, tilPhone;
    private TextInputEditText etFullName, etPhone;
    private MaterialButton btnUpdate, btnLogout;
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
        ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvUserRole = view.findViewById(R.id.tv_user_role);
        tvMemberSince = view.findViewById(R.id.tv_member_since);
        
        tilFullName = view.findViewById(R.id.til_full_name);
        tilPhone = view.findViewById(R.id.til_phone);
        etFullName = view.findViewById(R.id.et_full_name);
        etPhone = view.findViewById(R.id.et_phone);
        
        btnUpdate = view.findViewById(R.id.btn_update);
        btnLogout = view.findViewById(R.id.btn_logout);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        btnUpdate.setOnClickListener(v -> handleUpdateProfile());
        btnLogout.setOnClickListener(v -> handleLogout());
        ivProfilePicture.setOnClickListener(v -> handleProfilePictureClick());
    }

    private void loadUserProfile() {
        if (!ValidationUtils.isNetworkAvailable(requireContext())) {
            // Load from local storage if no network
            loadLocalUserData();
            return;
        }
        
        showLoading(true);
        
        RetrofitClient.getApiService().getProfile()
                .enqueue(new Callback<UserProfileResponse>() {
                    @Override
                    public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                        showLoading(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            UserProfileResponse profileResponse = response.body();
                            
                            if (profileResponse.isSuccess() && profileResponse.getUser() != null) {
                                currentUser = profileResponse.getUser();
                                updateUserInfo(currentUser);
                                // Update local storage
                                saveUserToLocal(currentUser);
                            } else {
                                showError(profileResponse.getMessage());
                                loadLocalUserData();
                            }
                        } else {
                            showError("Lỗi tải thông tin người dùng");
                            loadLocalUserData();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                        showLoading(false);
                        showError("Lỗi kết nối mạng");
                        loadLocalUserData();
                    }
                });
    }

    private void loadLocalUserData() {
        // Create user from local preferences
        currentUser = new User();
        currentUser.setId(PrefsHelper.getUserId(requireContext()));
        currentUser.setEmail(PrefsHelper.getUserEmail(requireContext()));
        currentUser.setFullName(PrefsHelper.getUserName(requireContext()));
        currentUser.setRole(PrefsHelper.getUserRole(requireContext()));
        
        updateUserInfo(currentUser);
    }

    private void saveUserToLocal(User user) {
        PrefsHelper.saveUserInfo(requireContext(), 
            user.getId(), 
            user.getEmail(), 
            user.getFullName(), 
            user.getRole());
    }

    private void updateUserInfo(User user) {
        if (user == null) return;
        
        // Set email (read-only)
        tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        
        // Set role with translation
        String roleText = translateRole(user.getRole());
        tvUserRole.setText(roleText);
        
        // Set member since
        if (user.getCreatedAt() != null) {
            tvMemberSince.setText("Thành viên từ: " + formatDate(user.getCreatedAt()));
        } else {
            tvMemberSince.setVisibility(View.GONE);
        }
        
        // Set editable fields
        etFullName.setText(user.getFullName() != null ? user.getFullName() : "");
        etPhone.setText(user.getPhone() != null ? user.getPhone() : "");
        
        // Load profile picture
        loadProfilePicture(user.getAvatarUrl());
        
        // Enable editing
        setEditingEnabled(true);
    }

    private String translateRole(String role) {
        if (role == null) return "Người dùng";
        
        switch (role.toUpperCase()) {
            case "ADMIN":
                return "Quản trị viên";
            case "INSTRUCTOR":
                return "Giảng viên";
            case "USER":
            default:
                return "Học viên";
        }
    }

    private String formatDate(String dateString) {
        // Basic date formatting - could be enhanced with proper date parsing
        if (dateString != null && dateString.length() >= 10) {
            return dateString.substring(0, 10);
        }
        return dateString;
    }

    private void loadProfilePicture(String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(ivProfilePicture);
        } else {
            ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }

    private void handleUpdateProfile() {
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
        setEditingEnabled(false);

        UpdateProfileRequest request = new UpdateProfileRequest(fullName, phone);
        
        RetrofitClient.getApiService().updateProfile(request)
                .enqueue(new Callback<UpdateProfileResponse>() {
                    @Override
                    public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                        showLoading(false);
                        setEditingEnabled(true);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            UpdateProfileResponse updateResponse = response.body();
                            
                            if (updateResponse.isSuccess()) {
                                // Update current user and local storage
                                if (updateResponse.getUser() != null) {
                                    currentUser = updateResponse.getUser();
                                    saveUserToLocal(currentUser);
                                } else {
                                    // Update local data with new values
                                    currentUser.setFullName(fullName);
                                    currentUser.setPhone(phone);
                                    saveUserToLocal(currentUser);
                                }
                                
                                Toast.makeText(getContext(), "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                            } else {
                                showError(updateResponse.getMessage());
                            }
                        } else {
                            showError("Lỗi cập nhật thông tin");
                        }
                    }

                    @Override
                    public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                        showLoading(false);
                        setEditingEnabled(true);
                        showError("Lỗi kết nối mạng");
                    }
                });
    }

    private void handleProfilePictureClick() {
        // TODO: Implement profile picture upload
        Toast.makeText(getContext(), "Tính năng upload ảnh sẽ được cập nhật sớm", Toast.LENGTH_SHORT).show();
    }

    private void handleLogout() {
        // Clear auth data
        PrefsHelper.clearAuthData(requireContext());
        
        // Navigate to login activity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void setEditingEnabled(boolean enabled) {
        etFullName.setEnabled(enabled);
        etPhone.setEnabled(enabled);
        btnUpdate.setEnabled(enabled);
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}