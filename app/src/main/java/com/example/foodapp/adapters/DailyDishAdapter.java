package com.example.foodapp.adapters;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodapp.R;
import com.example.foodapp.database.FoodDatabaseHelper;
import com.example.foodapp.models.Dish;
import com.example.foodapp.models.MealLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class DailyDishAdapter extends RecyclerView.Adapter<DailyDishAdapter.DishViewHolder> {

    private List<Dish> dishes;
    private Consumer<Dish> onDishClick;
    private FoodDatabaseHelper dbHelper;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public DailyDishAdapter(List<Dish> dishes, Consumer<Dish> onDishClick) {
        this.dishes = dishes;
        this.onDishClick = onDishClick;
    }

    @NonNull
    @Override
    public DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_dish, parent, false);
        return new DishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DishViewHolder holder, int position) {
        Dish dish = dishes.get(position);
        holder.bind(dish);
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    public void updateData(List<Dish> newDishes) {
        this.dishes = newDishes;
        notifyDataSetChanged();
    }

    class DishViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvCalories;
        Button btnLog;
        ImageButton btnFavorite;
        FoodDatabaseHelper dbHelper;
        boolean isLogged = false;

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_dish_name);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvCalories = itemView.findViewById(R.id.tv_calories);
            btnLog = itemView.findViewById(R.id.btn_log);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);

            dbHelper = new FoodDatabaseHelper(itemView.getContext());
        }

        void bind(Dish dish) {
            tvName.setText(dish.getName());
            tvCategory.setText(dish.getCategory());
            tvCalories.setText(dish.getCalories() + " kcal");

            // RESET BUTTON LOG STATE
            resetLogButton();

            // CHECK IF ALREADY LOGGED TODAY
            checkIfLoggedToday(dish.getName());

            // Favorite state
            boolean isFavorite = dbHelper.isFavorite(dish.getName());
            btnFavorite.setImageResource(isFavorite ?
                    R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

            // Click dish
            itemView.setOnClickListener(v -> onDishClick.accept(dish));

            // LOG MEAL BUTTON
            btnLog.setOnClickListener(v -> logMeal(dish));

            // FAVORITE BUTTON
            btnFavorite.setOnClickListener(v -> toggleFavorite(dish, isFavorite));
        }

        private void resetLogButton() {
            btnLog.setText("Ghi bữa ăn");
            btnLog.setEnabled(true);
            btnLog.setBackgroundTintList(itemView.getContext().getResources()
                    .getColorStateList(R.color.colorAccent));
            isLogged = false;
        }

        private void checkIfLoggedToday(String dishName) {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            // Kiểm tra trong DB nếu đã log hôm nay (optional)
            // Ở đây ta chỉ reset button cho đơn giản
        }

        private void logMeal(Dish dish) {
            if (isLogged) return;
            MealLog dishLog = new MealLog();
            dishLog.setName(dish.getName());
            dishLog.setCalories(dish.getCalories());
            dbHelper.logMeal(dishLog);

            isLogged = true;
            btnLog.setText("Đã ghi ✓");
            btnLog.setEnabled(false);
            btnLog.setBackgroundResource(android.R.color.darker_gray);

            // Toast success
            mainHandler.post(() ->
                    Toast.makeText(itemView.getContext(),
                            "Đã ghi: " + dish.getName(), Toast.LENGTH_SHORT).show()
            );

            Log.d("DailyDishAdapter", "Logged meal: " + dish.getName() + " - " + dish.getCalories() + " kcal");
        }

        private void toggleFavorite(Dish dish, boolean currentIsFavorite) {
            if (currentIsFavorite) {
                dbHelper.removeFromFavorites(dish.getName());
                btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                mainHandler.post(() ->
                        Toast.makeText(itemView.getContext(), "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show()
                );
            } else {
                dbHelper.addToFavorites(dish);
                btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
                mainHandler.post(() ->
                        Toast.makeText(itemView.getContext(), "Đã thêm yêu thích ✓", Toast.LENGTH_SHORT).show()
                );
            }
        }
    }
}