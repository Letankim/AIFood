package com.example.foodapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.foodapp.R;
import com.example.foodapp.models.Dish;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    private List<Dish> dishes;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Dish dish);
    }

    public FavoritesAdapter(List<Dish> dishes, Context context, OnItemClickListener listener) {
        this.dishes = dishes;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dish, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Dish dish = dishes.get(position);
        holder.dishName.setText(dish.getName());
        holder.dishCategory.setText(dish.getCategory());
        try {
            JSONArray ingArray = new JSONArray(dish.getIngredients());
            StringBuilder ingBuilder = new StringBuilder();
            for (int i = 0; i < Math.min(3, ingArray.length()); i++) {
                JSONObject ing = ingArray.getJSONObject(i);
                ingBuilder.append(ing.getString("name")).append(", ");
            }
            String ingredients = ingBuilder.length() > 0 ? ingBuilder.substring(0, ingBuilder.length() - 2) : "";
            holder.dishIngredients.setText(ingredients);
        } catch (Exception e) {
            holder.dishIngredients.setText(dish.getIngredients());
        }
        String imageUrl = dish.getImageUrl() != null && !dish.getImageUrl().isEmpty() ?
                dish.getImageUrl() : "https://img.freepik.com/free-photo/top-view-table-full-delicious-food-composition_23-2149141352.jpg";
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.dishImage);
        holder.cardView.setOnClickListener(v -> listener.onItemClick(dish));
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView dishImage;
        TextView dishName, dishCategory, dishIngredients;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.dish_card);
            dishImage = itemView.findViewById(R.id.dish_image);
            dishName = itemView.findViewById(R.id.dish_name);
            dishCategory = itemView.findViewById(R.id.dish_category);
            dishIngredients = itemView.findViewById(R.id.dish_ingredients);
        }
    }
}