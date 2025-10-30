package com.example.foodapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.foodapp.R;
import com.example.foodapp.database.FoodDatabaseHelper;
import com.example.foodapp.models.Dish;
import com.example.foodapp.models.MealLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DishDetailFragment extends Fragment {
    private static final String ARG_DISH = "dish";
    private Dish dish;
    private FoodDatabaseHelper dbHelper;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Button favoriteButton;

    public static DishDetailFragment newInstance(Dish dish) {
        DishDetailFragment fragment = new DishDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DISH, dish);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dish = (Dish) getArguments().getSerializable(ARG_DISH);
        }
        dbHelper = new FoodDatabaseHelper(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dish_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (dish != null && getContext() != null) {
            ImageView dishImage = view.findViewById(R.id.dish_image);
            TextView dishName = view.findViewById(R.id.dish_name);
            TextView dishCategory = view.findViewById(R.id.dish_category);
            TextView dishIngredients = view.findViewById(R.id.dish_ingredients);
            TextView dishSteps = view.findViewById(R.id.dish_steps);
            TextView dishDescription = view.findViewById(R.id.dish_description);
            TextView dishCalories = view.findViewById(R.id.dish_calories);

            Button logMealButton = view.findViewById(R.id.log_meal);
            favoriteButton = view.findViewById(R.id.add_favorite);

            Glide.with(getContext())
                    .load(dish.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(dishImage);

            dishName.setText(dish.getName());
            dishCategory.setText(dish.getCategory());
            dishDescription.setText(dish.getDescription());
            dishCalories.setText(dish.getCalories() + " kcal");

            formatAndSetText(dish.getIngredients(), dishIngredients, this::formatIngredients);
            formatAndSetText(dish.getSteps(), dishSteps, this::formatSteps);

            updateFavoriteButtonState();

            logMealButton.setOnClickListener(v -> logMeal());
            favoriteButton.setOnClickListener(v -> toggleFavorite());
        }
    }

    private void updateFavoriteButtonState() {
        executorService.execute(() -> {
            boolean isFavorite = dbHelper.isFavorite(dish.getName());
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (isFavorite) {
                        favoriteButton.setText(getString(R.string.remove_from_favorites_button_text));
                    } else {
                        favoriteButton.setText(getString(R.string.add_to_favorites_button_text));
                    }
                });
            }
        });
    }

    private void logMeal() {
        executorService.execute(() -> {
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            MealLog log = new MealLog(dish.getName(), dish.getCalories(), date);
            dbHelper.logMeal(log);
            if (getActivity() != null && getContext() != null) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), getString(R.string.meal_logged_success), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void toggleFavorite() {
        executorService.execute(() -> {
            if (dbHelper.isFavorite(dish.getName())) {
                dbHelper.removeFromFavorites(dish.getName());
                if (getActivity() != null && getContext() != null) {
                    getActivity().runOnUiThread(() -> {
                        favoriteButton.setText(getString(R.string.add_to_favorites_button_text));
                        Toast.makeText(getContext(), getString(R.string.removed_from_favorites_success), Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                dbHelper.addToFavorites(dish);
                if (getActivity() != null && getContext() != null) {
                    getActivity().runOnUiThread(() -> {
                        favoriteButton.setText(getString(R.string.remove_from_favorites_button_text));
                        Toast.makeText(getContext(), getString(R.string.added_to_favorites_success), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void formatAndSetText(String jsonString, TextView textView, JsonFormatter formatter) {
        try {
            textView.setText(formatter.format(jsonString));
        } catch (JSONException e) {
            textView.setText(jsonString);
        }
    }

    private String formatIngredients(String json) throws JSONException {
        JSONArray array = new JSONArray(json);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            builder.append("• ")
                    .append(obj.getString("name"))
                    .append(" (")
                    .append(obj.getString("quantity"))
                    .append(")\n");
        }
        return builder.toString().trim();
    }

    private String formatSteps(String json) throws JSONException {
        JSONArray array = new JSONArray(json);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length(); i++) {
            builder.append(String.format("%d. %s\n", i + 1, array.getString(i)));
        }
        return builder.toString().trim();
    }

    interface JsonFormatter {
        String format(String json) throws JSONException;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}