package com.example.foodapp.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.example.foodapp.R;
import com.example.foodapp.adapters.FavoritesAdapter;
import com.example.foodapp.database.FoodDatabaseHelper;
import com.example.foodapp.models.Dish;
import com.example.foodapp.models.MealLog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private ViewPager2 bannerViewPager;
    private TextInputEditText searchInput;
    private RecyclerView searchResultsRecycler;
    private ProgressBar loadingProgress;
    private ImageView bannerImage;
    private FloatingActionButton addMealFab;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        bannerViewPager = view.findViewById(R.id.banner_viewpager);
        searchInput = view.findViewById(R.id.search_input);
        searchResultsRecycler = view.findViewById(R.id.search_results_recycler);
        loadingProgress = view.findViewById(R.id.loading_progress);
        bannerImage = view.findViewById(R.id.banner_image);
        addMealFab = view.findViewById(R.id.add_meal_fab);
        searchResultsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (getContext() != null) {
            Glide.with(requireContext())
                    .load("https://www.ucsfhealth.org/-/media/project/ucsf/ucsf-health/education/hero/top-ten-foods-for-health-2x.jpg")
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(bannerImage);
        }

        loadDishesFromDatabase();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDishes(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        addMealFab.setOnClickListener(v -> showAddMealDialog());

        return view;
    }

    private void showAddMealDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_meal, null);
        builder.setView(dialogView);

        final EditText editDishName = dialogView.findViewById(R.id.edit_dish_name);
        final EditText editCalories = dialogView.findViewById(R.id.edit_calories);
        final EditText editProtein = dialogView.findViewById(R.id.edit_protein);
        final EditText editCarbs = dialogView.findViewById(R.id.edit_carbs);
        final EditText editFat = dialogView.findViewById(R.id.edit_fat);

        builder.setPositiveButton("Lưu", null); // Đặt listener là null để ghi đè sau
        builder.setNegativeButton("Hủy", (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String dishName = editDishName.getText().toString().trim();
                String caloriesStr = editCalories.getText().toString().trim();

                if (TextUtils.isEmpty(dishName) || TextUtils.isEmpty(caloriesStr)) {
                    Toast.makeText(getContext(), "Tên món ăn và calories là bắt buộc", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int calories = Integer.parseInt(caloriesStr);
                    // Lấy các giá trị khác, mặc định là 0 nếu trống
                    double protein = TextUtils.isEmpty(editProtein.getText()) ? 0 : Double.parseDouble(editProtein.getText().toString());
                    double carbs = TextUtils.isEmpty(editCarbs.getText()) ? 0 : Double.parseDouble(editCarbs.getText().toString());
                    double fat = TextUtils.isEmpty(editFat.getText()) ? 0 : Double.parseDouble(editFat.getText().toString());

                    // Tạo các chuỗi JSON mô phỏng
                    JSONArray ingredients = new JSONArray().put(new JSONObject().put("name", "User-added").put("quantity", "N/A"));
                    JSONArray steps = new JSONArray().put("User-added dish");
                    
                    Dish dish = new Dish(dishName, "Món ăn do người dùng thêm", ingredients.toString(), steps.toString(), calories, "User Added", "");
                    
                    executorService.execute(() -> {
                        FoodDatabaseHelper dbHelper = new FoodDatabaseHelper(getContext());
                        dbHelper.addDish(dish);
                        
                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        MealLog log = new MealLog(dishName, calories, date);
                        dbHelper.logMeal(log);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Đã thêm và lưu món ăn!", Toast.LENGTH_SHORT).show();
                                loadDishesFromDatabase(); // Tải lại danh sách
                                dialog.dismiss();
                            });
                        }
                    });

                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Vui lòng nhập số hợp lệ cho các giá trị dinh dưỡng", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                     Toast.makeText(getContext(), "Lỗi khi tạo dữ liệu món ăn", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }


    private void loadDishesFromDatabase() {
        loadingProgress.setVisibility(View.VISIBLE);
        executorService.execute(() -> {
            if (getContext() != null) {
                FoodDatabaseHelper dbHelper = new FoodDatabaseHelper(getContext());
                List<Dish> dishes = dbHelper.getAllDishes();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        loadingProgress.setVisibility(View.GONE);
                        FavoritesAdapter adapter = new FavoritesAdapter(dishes, requireContext(), this::navigateToDetail);
                        searchResultsRecycler.setAdapter(adapter);
                    });
                }
            }
        });
    }

    private void filterDishes(String query) {
        loadingProgress.setVisibility(View.VISIBLE);
        executorService.execute(() -> {
            if (getContext() != null) {
                FoodDatabaseHelper dbHelper = new FoodDatabaseHelper(getContext());
                List<Dish> allDishes = dbHelper.getAllDishes();
                List<Dish> filtered = new ArrayList<>();
                for (Dish dish : allDishes) {
                    if (dish.getName().toLowerCase().contains(query.toLowerCase())) {
                        filtered.add(dish);
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        loadingProgress.setVisibility(View.GONE);
                        FavoritesAdapter adapter = new FavoritesAdapter(filtered, requireContext(), this::navigateToDetail);
                        searchResultsRecycler.setAdapter(adapter);
                    });
                }
            }
        });
    }

    private void navigateToDetail(Dish dish) {
        if (getActivity() != null) {
            DishDetailFragment detailFragment = DishDetailFragment.newInstance(dish);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, detailFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
}