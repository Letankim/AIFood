package com.example.foodapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.foodapp.R;
import com.example.foodapp.database.FoodDatabaseHelper;
import com.example.foodapp.models.Dish;
import com.example.foodapp.models.MealLog;
import com.example.foodapp.utils.ApiFoodScanner;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IngredientSuggestionFragment extends Fragment {

    private TextInputEditText ingredientsEt;
    private Button suggestBtn;
    private ProgressBar loadingProgress;
    private MaterialCardView dishCard;
    private TextView dishNameTv, categoryTv, ingredientsTv, stepsTv, nutritionTv;
    private Button logButton, favoriteButton;
    private Dish suggestedDish;
    private FoodDatabaseHelper dbHelper;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final class JsonKeys {
        private static final String DISH_NAME = "dish_name";
        private static final String CATEGORY = "category";
        private static final String INGREDIENTS = "ingredients";
        private static final String STEPS = "steps";
        private static final String NUTRITION = "nutrition";
        private static final String TOTAL_CALORIES = "total_calories";
        private static final String IMAGE_URL = "image_url";
        private static final String NAME = "name";
        private static final String QUANTITY = "quantity";
        private static final String PROTEIN_G = "protein_g";
        private static final String CARBOHYDRATES_G = "carbohydrates_g";
        private static final String FAT_G = "fat_g";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ingredient_suggestion, container, false);

        ingredientsEt = view.findViewById(R.id.ingredients_input);
        suggestBtn = view.findViewById(R.id.suggest_button);
        loadingProgress = view.findViewById(R.id.loading_progress);
        dishCard = view.findViewById(R.id.dish_card);
        dishNameTv = view.findViewById(R.id.dish_name);
        categoryTv = view.findViewById(R.id.dish_category);
        ingredientsTv = view.findViewById(R.id.dish_ingredients);
        stepsTv = view.findViewById(R.id.dish_steps);
        nutritionTv = view.findViewById(R.id.dish_nutrition);
        logButton = view.findViewById(R.id.log_meal);
        favoriteButton = view.findViewById(R.id.add_favorite);

        dbHelper = new FoodDatabaseHelper(getContext());

        suggestBtn.setOnClickListener(v -> {
            String ingredients = ingredientsEt.getText().toString().trim();
            if (ingredients.isEmpty()) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), getString(R.string.enter_ingredients_hint), Toast.LENGTH_SHORT).show();
                }
                return;
            }
            loadingProgress.setVisibility(View.VISIBLE);
            dishCard.setVisibility(View.GONE);
            String localTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            ApiFoodScanner.suggestDishesFromIngredients(ingredients, localTime, getContext(), new ApiFoodScanner.ApiCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            loadingProgress.setVisibility(View.GONE);
                            try {
                                ApiFoodScanner.saveDishFromJson(getContext(), result, "suggest");
                                displayDish(result);
                                dishCard.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), getString(R.string.error_parsing_dish), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            loadingProgress.setVisibility(View.GONE);
                            if (getContext() != null) {
                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        });

        logButton.setOnClickListener(v -> executorService.execute(() -> {
            if (suggestedDish != null) {
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                MealLog log = new MealLog(suggestedDish.getName(), suggestedDish.getCalories(), date);
                dbHelper.logMeal(log);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), getString(R.string.meal_logged_success), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), getString(R.string.no_dish_to_log), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }));

        favoriteButton.setOnClickListener(v -> executorService.execute(() -> {
            if (suggestedDish != null) {
                if (dbHelper.isFavorite(suggestedDish.getName())) {
                    dbHelper.removeFromFavorites(suggestedDish.getName());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            favoriteButton.setText(getString(R.string.add_to_favorites_button_text));
                            if (getContext() != null) {
                                Toast.makeText(getContext(), getString(R.string.removed_from_favorites_success), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    dbHelper.addToFavorites(suggestedDish);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            favoriteButton.setText(getString(R.string.remove_from_favorites_button_text));
                            if (getContext() != null) {
                                Toast.makeText(getContext(), getString(R.string.added_to_favorites_success), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } else {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), getString(R.string.no_dish_to_favorite), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }));

        return view;
    }

    private void displayDish(JSONObject json) {
        try {
            suggestedDish = new Dish();
            suggestedDish.setName(json.getString(JsonKeys.DISH_NAME));
            suggestedDish.setCategory(json.getString(JsonKeys.CATEGORY));
            suggestedDish.setIngredients(json.getJSONArray(JsonKeys.INGREDIENTS).toString());
            suggestedDish.setSteps(json.getJSONArray(JsonKeys.STEPS).toString());
            JSONObject nutr = json.getJSONObject(JsonKeys.NUTRITION);
            suggestedDish.setCalories(nutr.getInt(JsonKeys.TOTAL_CALORIES));
            suggestedDish.setImageUrl(json.optString(JsonKeys.IMAGE_URL, "https://img.freepik.com/free-photo/top-view-table-full-delicious-food-composition_23-2149141352.jpg"));

            dishNameTv.setText(suggestedDish.getName());
            categoryTv.setText(getString(R.string.dish_category_label) + " " + suggestedDish.getCategory());

            JSONArray ing = json.getJSONArray(JsonKeys.INGREDIENTS);
            StringBuilder ingStr = new StringBuilder();
            for (int i = 0; i < ing.length(); i++) {
                JSONObject item = ing.getJSONObject(i);
                ingStr.append("- ").append(item.getString(JsonKeys.NAME)).append(": ").append(item.getString(JsonKeys.QUANTITY)).append("\n");
            }
            ingredientsTv.setText(getString(R.string.dish_ingredients_label) + "\n" + ingStr.toString());

            JSONArray steps = json.getJSONArray(JsonKeys.STEPS);
            StringBuilder stepsStr = new StringBuilder();
            for (int i = 0; i < steps.length(); i++) {
                stepsStr.append((i + 1) + ". ").append(steps.getString(i)).append("\n");
            }
            stepsTv.setText(getString(R.string.dish_steps_label) + "\n" + stepsStr.toString());

            nutritionTv.setText(getString(R.string.dish_nutrition_label) + "\n" +
                    getString(R.string.nutrition_format,
                            nutr.getInt(JsonKeys.TOTAL_CALORIES),
                            nutr.getInt(JsonKeys.PROTEIN_G),
                            nutr.getInt(JsonKeys.CARBOHYDRATES_G),
                            nutr.getInt(JsonKeys.FAT_G)
                    ));

            // Cập nhật trạng thái nút yêu thích
            executorService.execute(() -> {
                boolean isFavorite = dbHelper.isFavorite(suggestedDish.getName());
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

        } catch (JSONException e) {
            if (getContext() != null) {
                Toast.makeText(getContext(), getString(R.string.error_parsing_dish), Toast.LENGTH_SHORT).show();
            }
        }
    }
}