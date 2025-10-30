package com.example.foodapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.foodapp.R;
import com.example.foodapp.database.FoodDatabaseHelper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView dailyCalTv, weeklyCalTv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        dailyCalTv = view.findViewById(R.id.daily_calories);
        weeklyCalTv = view.findViewById(R.id.weekly_calories);

        FoodDatabaseHelper dbHelper = new FoodDatabaseHelper(getContext());

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        int dailyCal = dbHelper.getDailyCalories(today);
        dailyCalTv.setText(dailyCal + " kcal");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        String startWeek = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
        int weeklyCal = dbHelper.getWeeklyCalories(startWeek, today);
        weeklyCalTv.setText(weeklyCal + " kcal");

        return view;
    }
}