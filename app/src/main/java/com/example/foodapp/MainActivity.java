package com.example.foodapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.PopupMenu;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.foodapp.R;
import com.example.foodapp.activities.LoginActivity;
import com.example.foodapp.fragments.DailySuggestionFragment;
import com.example.foodapp.fragments.DashboardFragment;
import com.example.foodapp.fragments.FavoritesFragment;
import com.example.foodapp.fragments.HistoryFragment;
import com.example.foodapp.fragments.HomeFragment;
import com.example.foodapp.fragments.IngredientSuggestionFragment;
import com.example.foodapp.fragments.ImageScanFragment;
import com.example.foodapp.fragments.ProfileFragment;
import com.example.foodapp.fragments.SuggestionHistoryFragment;
import com.example.foodapp.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        super.onCreate(savedInstanceState);

        setupStatusBar();

        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
            } else if (itemId == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else if (itemId == R.id.nav_more) {
                PopupMenu popup = new PopupMenu(MainActivity.this, bottomNavigationView.findViewById(R.id.nav_more));
                popup.getMenu().add(0, 1, 0, "Suggest guide");
                popup.getMenu().add(0, 2, 0, "Scan");
                popup.getMenu().add(0, 3, 0, "Suggestion History");
                popup.getMenu().add(0, 4, 0, "Suggestion for today");
                popup.getMenu().add(0, 5, 0, "Favorites");
                popup.setOnMenuItemClickListener(menuItem -> {
                    Fragment fragment = null;
                    if (menuItem.getItemId() == 1) {
                        fragment = new IngredientSuggestionFragment();
                    } else if (menuItem.getItemId() == 2) {
                        fragment = new ImageScanFragment();
                    } else if (menuItem.getItemId() == 3) {
                        fragment = new SuggestionHistoryFragment();
                    }
                    else if (menuItem.getItemId() == 4) {
                        fragment = new DailySuggestionFragment();
                    }
                    else if (menuItem.getItemId() == 5) {
                        fragment = new FavoritesFragment();
                    }
                    if (fragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    }
                    return true;
                });
                popup.show();
                return true;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );
        }
    }
}