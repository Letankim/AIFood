package com.example.foodapp.fragments;

import android.os.Bundle;
import android.util.Log;
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
    private static final String TAG = "IngredientSuggestionFragment";

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
        private static final String DISHES = "dishes";  // Added for array handling
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
                                // Extract first dish from {"dishes": [...]}
                                JSONObject firstDish = extractFirstDish(result);
                                if (firstDish != null) {
                                    ApiFoodScanner.saveDishFromJson(getContext(), firstDish, "suggest");
                                    displayDish(firstDish);
                                    dishCard.setVisibility(View.VISIBLE);
                                    Log.d(TAG, "Displayed first suggested dish: " + firstDish.optString(JsonKeys.DISH_NAME));
                                } else {
                                    throw new JSONException("No dishes in response");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing response: " + e.getMessage(), e);
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), getString(R.string.error_parsing_dish) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "API Error: " + error);
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

    private JSONObject extractFirstDish(JSONObject result) throws JSONException {
        if (result.has(JsonKeys.DISHES)) {
            JSONArray dishes = result.getJSONArray(JsonKeys.DISHES);
            if (dishes.length() > 0) {
                return dishes.getJSONObject(0);
            }
        }
        return null;
    }

    private void displayDish(JSONObject json) {
        try {
            suggestedDish = new Dish();
            suggestedDish.setName(json.optString(JsonKeys.DISH_NAME, "Unknown Dish"));  // Fallback
            suggestedDish.setCategory(json.optString(JsonKeys.CATEGORY, "General"));  // Fallback + opt
            suggestedDish.setIngredients(json.optJSONArray(JsonKeys.INGREDIENTS) != null ?
                    json.getJSONArray(JsonKeys.INGREDIENTS).toString() : "[]");
            suggestedDish.setSteps(json.optJSONArray(JsonKeys.STEPS) != null ?
                    json.getJSONArray(JsonKeys.STEPS).toString() : "[]");

            JSONObject nutr = json.optJSONObject(JsonKeys.NUTRITION);
            if (nutr != null) {
                suggestedDish.setCalories(nutr.optInt(JsonKeys.TOTAL_CALORIES, 0));
            } else {
                suggestedDish.setCalories(0);
            }
            suggestedDish.setImageUrl(json.optString(JsonKeys.IMAGE_URL,
                    "https://img.freepik.com/free-photo/top-view-table-full-delicious-food-composition_23-2149141352.jpg"));

            dishNameTv.setText(suggestedDish.getName());
            categoryTv.setText(getString(R.string.dish_category_label) + " " + suggestedDish.getCategory());

            JSONArray ing = json.optJSONArray(JsonKeys.INGREDIENTS);
            StringBuilder ingStr = new StringBuilder();
            if (ing != null) {
                for (int i = 0; i < ing.length(); i++) {
                    JSONObject item = ing.optJSONObject(i);
                    if (item != null) {
                        ingStr.append("- ").append(item.optString(JsonKeys.NAME, "Unknown"))
                                .append(": ").append(item.optString(JsonKeys.QUANTITY, "?")).append("\n");
                    }
                }
            }
            ingredientsTv.setText(getString(R.string.dish_ingredients_label) + "\n" + ingStr.toString());

            JSONArray steps = json.optJSONArray(JsonKeys.STEPS);
            StringBuilder stepsStr = new StringBuilder();
            if (steps != null) {
                for (int i = 0; i < steps.length(); i++) {
                    stepsStr.append((i + 1) + ". ").append(steps.optString(i, "Step missing")).append("\n");
                }
            }
            stepsTv.setText(getString(R.string.dish_steps_label) + "\n" + stepsStr.toString());

            if (nutr != null) {
                nutritionTv.setText(getString(R.string.dish_nutrition_label) + "\n" +
                        getString(R.string.nutrition_format,
                                nutr.optInt(JsonKeys.TOTAL_CALORIES, 0),
                                nutr.optInt(JsonKeys.PROTEIN_G, 0),
                                nutr.optInt(JsonKeys.CARBOHYDRATES_G, 0),
                                nutr.optInt(JsonKeys.FAT_G, 0)
                        ));
            } else {
                nutritionTv.setText(getString(R.string.dish_nutrition_label) + "\nUnknown nutrition info");
            }

            // Update favorite button state
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

            Log.d(TAG, "Dish displayed successfully: " + suggestedDish.getName());

        } catch (JSONException e) {
            Log.e(TAG, "DisplayDish JSON error: " + e.getMessage(), e);
            if (getContext() != null) {
                Toast.makeText(getContext(), getString(R.string.error_parsing_dish), Toast.LENGTH_SHORT).show();
            }
        }
    }
}