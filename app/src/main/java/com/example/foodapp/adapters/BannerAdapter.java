package com.example.foodapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.foodapp.R;
import com.example.foodapp.activities.DishDetailActivity;
import com.example.foodapp.models.Dish;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {
    private List<Dish> dishes;
    private Context context;

    public BannerAdapter(List<Dish> dishes, Context context) {
        this.dishes = dishes;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.banner_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Dish dish = dishes.get(position);
        holder.nameTextView.setText(dish.getName());
        String tempUrl = dish.getImageUrl();
        if (tempUrl == null || tempUrl.isEmpty()) {
            tempUrl = "https://img.freepik.com/free-photo/top-view-table-full-delicious-food-composition_23-2149141352.jpg";
        }
        final String imageUrl = tempUrl;
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DishDetailActivity.class);
            intent.putExtra("dish_name", dish.getName());
            intent.putExtra("dish_description", dish.getDescription());
            intent.putExtra("dish_ingredients", dish.getIngredients());
            intent.putExtra("dish_steps", dish.getSteps());
            intent.putExtra("dish_calories", dish.getCalories());
            intent.putExtra("dish_category", dish.getCategory());
            intent.putExtra("dish_image_url", imageUrl);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.dish_name);
            imageView = itemView.findViewById(R.id.dish_image);
        }
    }
}