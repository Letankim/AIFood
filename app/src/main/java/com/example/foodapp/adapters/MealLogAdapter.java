package com.example.foodapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.R;
import com.example.foodapp.models.MealLog;

import java.util.List;

public class MealLogAdapter extends RecyclerView.Adapter<MealLogAdapter.ViewHolder> {
    private List<MealLog> mealLogs;

    public MealLogAdapter(List<MealLog> mealLogs) {
        this.mealLogs = mealLogs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meal_log_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealLog log = mealLogs.get(position);
        holder.nameTv.setText(log.getName());
        holder.caloriesTv.setText(String.valueOf(log.getCalories()));
        holder.dateTv.setText(log.getDate());
    }

    @Override
    public int getItemCount() {
        return mealLogs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTv, caloriesTv, dateTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.log_name);
            caloriesTv = itemView.findViewById(R.id.log_calories);
            dateTv = itemView.findViewById(R.id.log_date);
        }
    }
}