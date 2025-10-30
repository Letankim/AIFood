package com.example.foodapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.R;
import com.example.foodapp.adapters.MealLogAdapter;
import com.example.foodapp.database.FoodDatabaseHelper;
import com.example.foodapp.models.MealLog;

import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.meal_log_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FoodDatabaseHelper dbHelper = new FoodDatabaseHelper(getContext());
        List<MealLog> logs = dbHelper.getAllMealLogs();

        MealLogAdapter adapter = new MealLogAdapter(logs);
        recyclerView.setAdapter(adapter);

        return view;
    }
}