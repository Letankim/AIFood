package com.example.foodapp.models;

import java.io.Serializable;

public class Dish implements Serializable {
    private int id;
    private String name;
    private String description;
    private String ingredients;
    private String steps;
    private int calories;
    private String category;
    private String imageUrl;

    public Dish() {}

    public Dish(String name, String description, String ingredients, String steps, int calories, String category, String imageUrl) {
        this.name = name;
        this.description = description;
        this.ingredients = ingredients;
        this.steps = steps;
        this.calories = calories;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}