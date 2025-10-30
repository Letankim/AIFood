package com.example.foodapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
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

public class DishDetailActivity extends AppCompatActivity {
    private ImageView dishImageTv;
    private TextView nameTv, descTv, ingredientsTv, stepsTv, caloriesTv, categoryTv;
    private Button logButton, favoriteButton;
    private FoodDatabaseHelper dbHelper;
    private Dish dish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_detail);

        dishImageTv = findViewById(R.id.dish_image);
        nameTv = findViewById(R.id.dish_name);
        descTv = findViewById(R.id.dish_description);
        ingredientsTv = findViewById(R.id.dish_ingredients);
        stepsTv = findViewById(R.id.dish_steps);
        caloriesTv = findViewById(R.id.dish_calories);
        categoryTv = findViewById(R.id.dish_category);
        logButton = findViewById(R.id.log_meal);
        favoriteButton = findViewById(R.id.add_favorite);

        dbHelper = new FoodDatabaseHelper(this);

        // Retrieve dish from intent
        dish = new Dish();
        dish.setName(getIntent().getStringExtra("dish_name"));
        dish.setDescription(getIntent().getStringExtra("dish_description"));
        dish.setIngredients(getIntent().getStringExtra("dish_ingredients"));
        dish.setSteps(getIntent().getStringExtra("dish_steps"));
        dish.setCalories(getIntent().getIntExtra("dish_calories", 0));
        dish.setCategory(getIntent().getStringExtra("dish_category"));
        dish.setImageUrl(getIntent().getStringExtra("dish_image_url"));

        // Display data
        nameTv.setText(dish.getName());
        descTv.setText(dish.getDescription());
        categoryTv.setText(dish.getCategory());
        caloriesTv.setText(String.valueOf(dish.getCalories()));

        // Parse and display ingredients
        try {
            JSONArray ingArray = new JSONArray(dish.getIngredients());
            StringBuilder ingBuilder = new StringBuilder();
            for (int i = 0; i < ingArray.length(); i++) {
                JSONObject ing = ingArray.getJSONObject(i);
                ingBuilder.append("- ").append(ing.getString("name")).append(": ").append(ing.getString("quantity")).append("\n");
            }
            ingredientsTv.setText(ingBuilder.toString());
        } catch (JSONException e) {
            ingredientsTv.setText(dish.getIngredients());
        }

        // Parse and display steps
        try {
            JSONArray stepsArray = new JSONArray(dish.getSteps());
            StringBuilder stepsBuilder = new StringBuilder();
            for (int i = 0; i < stepsArray.length(); i++) {
                stepsBuilder.append((i + 1) + ". ").append(stepsArray.getString(i)).append("\n");
            }
            stepsTv.setText(stepsBuilder.toString());
        } catch (JSONException e) {
            stepsTv.setText(dish.getSteps());
        }

        String imageUrl = dish.getImageUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = "https://img.freepik.com/free-photo/top-view-table-full-delicious-food-composition_23-2149141352.jpg";
        }
        Glide.with(this).load(imageUrl).into(dishImageTv);

        if (dbHelper.isFavorite(dish.getName())) {
            favoriteButton.setText("Remove from Favorites");
            favoriteButton.setOnClickListener(v -> {
                dbHelper.removeFromFavorites(dish.getName());
                favoriteButton.setText("Add to Favorites");
            });
        } else {
            favoriteButton.setText("Add to Favorites");
            favoriteButton.setOnClickListener(v -> {
                dbHelper.addToFavorites(dish);
                favoriteButton.setText("Remove from Favorites");
            });
        }

        logButton.setOnClickListener(v -> {
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            MealLog log = new MealLog(dish.getName(), dish.getCalories(), date);
            dbHelper.logMeal(log);
        });
    }
}