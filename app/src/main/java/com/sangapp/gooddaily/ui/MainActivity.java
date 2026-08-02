package com.sangapp.gooddaily.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.color.DynamicColors;
import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.databinding.ActivityMainBinding;
import com.sangapp.gooddaily.ui.auth.PinLockActivity;
import com.sangapp.gooddaily.util.SecuritySession;
import com.sangapp.gooddaily.util.ThemeUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final Set<Integer> TOP_LEVEL = new HashSet<>(Arrays.asList(
            R.id.dashboardFragment, R.id.financeFragment, R.id.healthFragment,
            R.id.plannerFragment, R.id.profileFragment));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalUserStore startupStore = new LocalUserStore(this);
        if (startupStore.isDynamicColorsEnabled()) DynamicColors.applyToActivityIfAvailable(this);
        if (startupStore.isSecureScreenEnabled()) getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        LocalUserStore userStore = new LocalUserStore(this);
        ThemeUtils.applyBottomNavigation(binding.bottomNavigation, this, userStore.getThemeKey());

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        if (host == null) return;
        NavController navController = host.getNavController();
        int startDestination = navController.getGraph().getStartDestinationId();

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int destination = item.getItemId();
            if (!TOP_LEVEL.contains(destination)) return false;
            if (navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId() == destination) return true;

            if (destination == startDestination) {
                navController.popBackStack(startDestination, false);
                return true;
            }

            NavOptions options = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setRestoreState(true)
                    .setPopUpTo(startDestination, false, true)
                    .build();
            try {
                navController.navigate(destination, null, options);
                return true;
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean topLevel = TOP_LEVEL.contains(destination.getId());
            binding.bottomNavigationCard.setVisibility(topLevel ? View.VISIBLE : View.GONE);
            if (topLevel && binding.bottomNavigation.getSelectedItemId() != destination.getId()) {
                binding.bottomNavigation.getMenu().findItem(destination.getId()).setChecked(true);
            }
        });

        String requested = getIntent().getStringExtra("open_destination");
        if ("planner".equals(requested)) {
            binding.bottomNavigation.setSelectedItemId(R.id.plannerFragment);
        }
    }

    @Override protected void onStart() {
        super.onStart();
        if (SecuritySession.shouldLock(this)) startActivity(new Intent(this, PinLockActivity.class));
    }

    @Override protected void onStop() {
        super.onStop();
        new LocalUserStore(this).setLastBackgroundAt(System.currentTimeMillis());
    }

}
