package com.example.foodapp.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.R;
import com.example.foodapp.adapters.DailyDishAdapter;
import com.example.foodapp.database.FoodDatabaseHelper;
import com.example.foodapp.models.Dish;
import com.example.foodapp.utils.ApiFoodScanner;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailySuggestionFragment extends Fragment {

    private RecyclerView rvDailyDishes;
    private ProgressBar loadingProgress;
    private TextInputEditText etSearch;
    private DailyDishAdapter adapter;
    private FoodDatabaseHelper dbHelper;
    private List<Dish> allDishes;
    private List<Dish> filteredDishes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_suggestion, container, false);

        initViews(view);
        setupRecyclerView();
        loadDailySuggestions();

        return view;
    }

    private void initViews(View view) {
        rvDailyDishes = view.findViewById(R.id.rv_daily_dishes);
        loadingProgress = view.findViewById(R.id.loading_progress);
        etSearch = view.findViewById(R.id.et_search);

        dbHelper = new FoodDatabaseHelper(requireContext());
        allDishes = new ArrayList<>();
        filteredDishes = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new DailyDishAdapter(filteredDishes, dish -> {
            // Navigate to dish detail
            DishDetailFragment detailFragment = new DishDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("dish", dish);
            detailFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack("dish_detail")
                    .commit();
        });
        rvDailyDishes.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDailyDishes.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                filterDishes(s.toString());
            }
        });
    }

    private void loadDailySuggestions() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        loadingProgress.setVisibility(View.VISIBLE);

        List<Dish> cachedDishes = dbHelper.getDailySuggestions(today);
//        if (!cachedDishes.isEmpty()) {
//            allDishes = cachedDishes;
//            filteredDishes = new ArrayList<>(allDishes);
//            adapter.updateData(filteredDishes);
//            loadingProgress.setVisibility(View.GONE);
//            Toast.makeText(requireContext(), "Loaded cached suggestions for today!", Toast.LENGTH_SHORT).show();
//            return;
//        }

        ApiFoodScanner.suggestDailyDishes(requireContext(), new ApiFoodScanner.ApiCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                requireActivity().runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    android.util.Log.d("DailySuggestion", "API SUCCESS!");
                    parseApiResponse(result, today);
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    android.util.Log.e("DailySuggestion", "API ERROR: " + error);
                    Toast.makeText(requireContext(), "API error - Loading sample data", Toast.LENGTH_SHORT).show();
                    loadFakeData(today);
                });
            }
        });
    }

    private void loadFakeData(String today) {
        allDishes.clear();

        String[] fakeDishes = {
                "Phở Bò", "Bánh Mì", "Bún Chả", "Cơm Tấm", "Chả Giò"
        };
        int[] calories = {600, 500, 550, 700, 400};
        String[] categories = {"Dinner", "Lunch", "Lunch", "Dinner", "Snacks"};

        for (int i = 0; i < 5; i++) {
            Dish dish = new Dish();
            dish.setName(fakeDishes[i]);
            dish.setDescription("A traditional Vietnamese dish that is delicious!");
            dish.setCategory(categories[i]);
            dish.setCalories(calories[i]);
            dish.setIngredients("[{\"name\":\"Beef\",\"quantity\":\"200g\"},{\"name\":\"Noodles\",\"quantity\":\"100g\"}]");
            dish.setSteps("[\"Step 1: Prepare ingredients\",\"Step 2: Cook\",\"Step 3: Enjoy\"]");
            dish.setImageUrl("https://placehold.co/600x400/FFA500/FFFFFF/png?text=" + fakeDishes[i]);

            dbHelper.addDailySuggestion(dish, today);
            allDishes.add(dish);
        }

        filteredDishes = new ArrayList<>(allDishes);
        adapter.updateData(filteredDishes);
        Toast.makeText(requireContext(), "Loaded five sample meals for today!", Toast.LENGTH_SHORT).show();
    }

    private void parseApiResponse(JSONObject result, String today) {
        try {
            JSONArray dishesArray = result.getJSONArray("dishes");
            allDishes.clear();

            for (int i = 0; i < Math.min(5, dishesArray.length()); i++) {
                JSONObject dishJson = dishesArray.getJSONObject(i);
                Dish dish = new Dish();
                dish.setName(dishJson.getString("dish_name"));
                dish.setDescription(dishJson.optString("description", ""));
                dish.setCategory(dishJson.optString("category", "Other"));
                dish.setCalories(dishJson.getJSONObject("nutrition").getInt("total_calories"));
                dish.setIngredients(dishJson.getJSONArray("ingredients").toString());
                dish.setSteps(dishJson.getJSONArray("steps").toString());
                dish.setImageUrl(dishJson.optString("image_url", "https://placehold.co/600x400/FFA500/FFFFFF/png?text=Dish"));

                dbHelper.addDailySuggestion(dish, today);
                allDishes.add(dish);
            }

            filteredDishes = new ArrayList<>(allDishes);
            adapter.updateData(filteredDishes);
            Toast.makeText(requireContext(), "Suggestions loaded from AI!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            android.util.Log.e("DailySuggestion", "Parse error: " + e.getMessage());
            Toast.makeText(requireContext(), "Error parsing API response - Loading sample data", Toast.LENGTH_SHORT).show();
            loadFakeData(today);
        }
    }

    private void filterDishes(String query) {
        filteredDishes.clear();
        if (query.trim().isEmpty()) {
            filteredDishes.addAll(allDishes);
        } else {
            for (Dish dish : allDishes) {
                if (dish.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredDishes.add(dish);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}