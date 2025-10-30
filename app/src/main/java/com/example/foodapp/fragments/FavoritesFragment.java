package com.example.foodapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodapp.R;
import com.example.foodapp.activities.DishDetailActivity;
import com.example.foodapp.adapters.FavoritesAdapter;
import com.example.foodapp.database.FoodDatabaseHelper;
import com.example.foodapp.models.Dish;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerView = view.findViewById(R.id.favorites_recycler);
        emptyView = view.findViewById(R.id.empty_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FoodDatabaseHelper dbHelper = new FoodDatabaseHelper(getContext());
        List<Dish> favorites = dbHelper.getFavorites();

        if (favorites.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            FavoritesAdapter adapter = new FavoritesAdapter(favorites, getContext(), dish -> {
                Intent intent = new Intent(getContext(), DishDetailActivity.class);
                intent.putExtra("dish_name", dish.getName());
                intent.putExtra("dish_description", dish.getDescription());
                intent.putExtra("dish_ingredients", dish.getIngredients());
                intent.putExtra("dish_steps", dish.getSteps());
                intent.putExtra("dish_calories", dish.getCalories());
                intent.putExtra("dish_category", dish.getCategory());
                intent.putExtra("dish_image_url", dish.getImageUrl());
                startActivity(intent);
            });
            recyclerView.setAdapter(adapter);
        }

        return view;
    }
}