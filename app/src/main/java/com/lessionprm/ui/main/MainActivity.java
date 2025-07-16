package com.lessionprm.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.lessionprm.R;
import com.lessionprm.ui.auth.LoginActivity;
import com.lessionprm.utils.PrefsHelper;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigation;
    private MaterialToolbar toolbar;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupNavigation();
        setupDrawer();
        setupBottomNavigation();
        updateNavigationForUser();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        toolbar = findViewById(R.id.toolbar);
        
        setSupportActionBar(toolbar);
    }

    private void setupNavigation() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        
        // Define top-level destinations
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_courses, R.id.nav_my_courses, R.id.nav_profile)
                .setOpenableLayout(drawerLayout)
                .build();
        
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, 
                R.string.navigation_drawer_open, 
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupBottomNavigation() {
        NavigationUI.setupWithNavController(bottomNavigation, navController);
    }

    private void updateNavigationForUser() {
        Menu navMenu = navigationView.getMenu();
        Menu bottomMenu = bottomNavigation.getMenu();
        
        // Show/hide admin items based on user role
        boolean isAdmin = PrefsHelper.isAdmin(this);
        navMenu.findItem(R.id.nav_admin).setVisible(isAdmin);
        
        if (bottomMenu.findItem(R.id.nav_admin) != null) {
            bottomMenu.findItem(R.id.nav_admin).setVisible(isAdmin);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_search) {
            // TODO: Implement search functionality
            Toast.makeText(this, "Tìm kiếm", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_notifications) {
            // TODO: Implement notifications
            Toast.makeText(this, "Thông báo", Toast.LENGTH_SHORT).show();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_courses) {
            navController.navigate(R.id.nav_courses);
        } else if (id == R.id.nav_my_courses) {
            navController.navigate(R.id.nav_my_courses);
        } else if (id == R.id.nav_profile) {
            navController.navigate(R.id.nav_profile);
        } else if (id == R.id.nav_admin) {
            if (PrefsHelper.isAdmin(this)) {
                navController.navigate(R.id.nav_admin);
            } else {
                Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_settings) {
            // TODO: Implement settings
            Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            logout();
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        PrefsHelper.clearAuthData(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}