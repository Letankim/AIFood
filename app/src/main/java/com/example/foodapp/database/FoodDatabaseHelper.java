package com.example.foodapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.foodapp.models.Dish;
import com.example.foodapp.models.MealLog;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FoodDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "food_app.db";
    private static final int DATABASE_VERSION = 3;

    // Tables
    private static final String TABLE_DISHES = "dishes";
    private static final String TABLE_MEAL_LOGS = "meal_logs";
    private static final String TABLE_FAVORITES = "favorites";
    private static final String TABLE_SUGGESTIONS = "suggestions";

    // Columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_INGREDIENTS = "ingredients";
    private static final String COLUMN_STEPS = "steps";
    private static final String COLUMN_CALORIES = "calories";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_LOG_DATE = "log_date";
    private static final String COLUMN_SUGGESTION_DATE = "suggestion_date";
    private static final String COLUMN_IMAGE_URL = "image_url";

    public FoodDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createDishesTable = "CREATE TABLE " + TABLE_DISHES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT UNIQUE, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_INGREDIENTS + " TEXT, " +
                COLUMN_STEPS + " TEXT, " +
                COLUMN_CALORIES + " INTEGER, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_IMAGE_URL + " TEXT)";
        db.execSQL(createDishesTable);

        String createMealLogsTable = "CREATE TABLE " + TABLE_MEAL_LOGS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_CALORIES + " INTEGER, " +
                COLUMN_LOG_DATE + " TEXT)";
        db.execSQL(createMealLogsTable);

        String createFavoritesTable = "CREATE TABLE " + TABLE_FAVORITES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_INGREDIENTS + " TEXT, " +
                COLUMN_STEPS + " TEXT, " +
                COLUMN_CALORIES + " INTEGER, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_IMAGE_URL + " TEXT)";
        db.execSQL(createFavoritesTable);

        String createSuggestionsTable = "CREATE TABLE " + TABLE_SUGGESTIONS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_INGREDIENTS + " TEXT, " +
                COLUMN_STEPS + " TEXT, " +
                COLUMN_CALORIES + " INTEGER, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_IMAGE_URL + " TEXT, " +
                COLUMN_SUGGESTION_DATE + " TEXT)";
        db.execSQL(createSuggestionsTable);

        insertSampleDishes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISHES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEAL_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUGGESTIONS);
        onCreate(db);
    }

    private void insertSampleDishes(SQLiteDatabase db) {
        String[] dishes = {
                "{\"name\":\"Phở Bò\",\"description\":\"A traditional Vietnamese beef noodle soup\",\"ingredients\":\"[{\\\"name\\\":\\\"Beef\\\",\\\"quantity\\\":\\\"500g\\\"},{\\\"name\\\":\\\"Rice noodles\\\",\\\"quantity\\\":\\\"400g\\\"},{\\\"name\\\":\\\"Onion\\\",\\\"quantity\\\":\\\"1\\\"},{\\\"name\\\":\\\"Ginger\\\",\\\"quantity\\\":\\\"50g\\\"},{\\\"name\\\":\\\"Star anise\\\",\\\"quantity\\\":\\\"2\\\"}]\",\"steps\":\"[\\\"Boil beef bones for broth\\\",\\\"Add spices and simmer for 3 hours\\\",\\\"Cook noodles\\\",\\\"Serve with fresh herbs\\\"]\",\"calories\":600,\"category\":\"Dinner\",\"image_url\":\"https://file.hstatic.net/200000700229/article/pho-bo-ha-noi-thumb_980349ef2bcf40c9b736a672e5a944d3.jpg\"}",
                "{\"name\":\"Bánh Mì\",\"description\":\"Vietnamese baguette with various fillings\",\"ingredients\":\"[{\\\"name\\\":\\\"Baguette\\\",\\\"quantity\\\":\\\"1\\\"},{\\\"name\\\":\\\"Pork\\\",\\\"quantity\\\":\\\"200g\\\"},{\\\"name\\\":\\\"Cucumber\\\",\\\"quantity\\\":\\\"1\\\"},{\\\"name\\\":\\\"Carrot\\\",\\\"quantity\\\":\\\"100g\\\"},{\\\"name\\\":\\\"Mayonnaise\\\",\\\"quantity\\\":\\\"2 tbsp\\\"}]\",\"steps\":\"[\\\"Toast baguette\\\",\\\"Add fillings\\\",\\\"Serve with chili sauce\\\"]\",\"calories\":500,\"category\":\"Lunch\",\"image_url\":\"https://www.andy-cooks.com/cdn/shop/articles/20230813061131-andy-20cooks-20-20roast-20pork-20banh-20mi.jpg?v=1691997210\"}",
                "{\"name\":\"Bún Chả\",\"description\":\"Grilled pork with rice noodles and fish sauce\",\"ingredients\":\"[{\\\"name\\\":\\\"Pork\\\",\\\"quantity\\\":\\\"400g\\\"},{\\\"name\\\":\\\"Rice noodles\\\",\\\"quantity\\\":\\\"300g\\\"},{\\\"name\\\":\\\"Fish sauce\\\",\\\"quantity\\\":\\\"4 tbsp\\\"},{\\\"name\\\":\\\"Garlic\\\",\\\"quantity\\\":\\\"2 cloves\\\"},{\\\"name\\\":\\\"Fresh herbs\\\",\\\"quantity\\\":\\\"50g\\\"}]\",\"steps\":\"[\\\"Marinate pork with spices\\\",\\\"Grill pork until charred\\\",\\\"Cook noodles\\\",\\\"Serve with fish sauce and herbs\\\"]\",\"calories\":550,\"category\":\"Dinner\",\"image_url\":\"https://i-giadinh.vnecdn.net/2023/04/16/Buoc-11-Thanh-pham-11-7068-1681636164.jpg\"}",
                "{\"name\":\"Cơm Tấm\",\"description\":\"Broken rice with grilled pork chop\",\"ingredients\":\"[{\\\"name\\\":\\\"Broken rice\\\",\\\"quantity\\\":\\\"200g\\\"},{\\\"name\\\":\\\"Pork chop\\\",\\\"quantity\\\":\\\"1 piece\\\"},{\\\"name\\\":\\\"Fish sauce\\\",\\\"quantity\\\":\\\"2 tbsp\\\"},{\\\"name\\\":\\\"Cucumber\\\",\\\"quantity\\\":\\\"1\\\"},{\\\"name\\\":\\\"Pickled carrots\\\",\\\"quantity\\\":\\\"50g\\\"}]\",\"steps\":\"[\\\"Cook broken rice\\\",\\\"Marinate and grill pork chop\\\",\\\"Prepare fish sauce dipping\\\",\\\"Serve with cucumber and pickles\\\"]\",\"calories\":650,\"category\":\"Lunch\",\"image_url\":\"https://i-giadinh.vnecdn.net/2024/03/07/7Honthinthnhphm1-1709800144-8583-1709800424.jpg\"}",
                "{\"name\":\"Bánh Xèo\",\"description\":\"Crispy Vietnamese pancake with shrimp and bean sprouts\",\"ingredients\":\"[{\\\"name\\\":\\\"Rice flour\\\",\\\"quantity\\\":\\\"200g\\\"},{\\\"name\\\":\\\"Shrimp\\\",\\\"quantity\\\":\\\"200g\\\"},{\\\"name\\\":\\\"Bean sprouts\\\",\\\"quantity\\\":\\\"100g\\\"},{\\\"name\\\":\\\"Turmeric\\\",\\\"quantity\\\":\\\"1 tsp\\\"},{\\\"name\\\":\\\"Coconut milk\\\",\\\"quantity\\\":\\\"100ml\\\"}]\",\"steps\":\"[\\\"Mix batter with rice flour and turmeric\\\",\\\"Fry pancake with shrimp and sprouts\\\",\\\"Serve with fish sauce and herbs\\\"]\",\"calories\":450,\"category\":\"Dinner\",\"image_url\":\"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSuSquASeahdJodJDPITs8LxvOATtEjlPcpeg&s\"}",
                "{\"name\":\"Chả Giò\",\"description\":\"Vietnamese fried spring rolls\",\"ingredients\":\"[{\\\"name\\\":\\\"Ground pork\\\",\\\"quantity\\\":\\\"300g\\\"},{\\\"name\\\":\\\"Rice paper\\\",\\\"quantity\\\":\\\"10 sheets\\\"},{\\\"name\\\":\\\"Carrot\\\",\\\"quantity\\\":\\\"100g\\\"},{\\\"name\\\":\\\"Wood ear mushrooms\\\",\\\"quantity\\\":\\\"50g\\\"},{\\\"name\\\":\\\"Fish sauce\\\",\\\"quantity\\\":\\\"1 tbsp\\\"}]\",\"steps\":\"[\\\"Mix filling ingredients\\\",\\\"Wrap in rice paper\\\",\\\"Deep fry until golden\\\",\\\"Serve with dipping sauce\\\"]\",\"calories\":300,\"category\":\"Snacks\",\"image_url\":\"https://cdn2.fptshop.com.vn/unsafe/1920x0/filters:format(webp):quality(75)/2024_2_28_638447510905455385_cach-lam-cha-gio-khoai-mon-thit-0.jpg\"}",
                "{\"name\":\"Gỏi Cuốn\",\"description\":\"Fresh spring rolls with shrimp and pork\",\"ingredients\":\"[{\\\"name\\\":\\\"Rice paper\\\",\\\"quantity\\\":\\\"10 sheets\\\"},{\\\"name\\\":\\\"Shrimp\\\",\\\"quantity\\\":\\\"200g\\\"},{\\\"name\\\":\\\"Pork\\\",\\\"quantity\\\":\\\"150g\\\"},{\\\"name\\\":\\\"Lettuce\\\",\\\"quantity\\\":\\\"50g\\\"},{\\\"name\\\":\\\"Rice noodles\\\",\\\"quantity\\\":\\\"100g\\\"}]\",\"steps\":\"[\\\"Boil shrimp and pork\\\",\\\"Soften rice paper in water\\\",\\\"Wrap ingredients in rice paper\\\",\\\"Serve with peanut sauce\\\"]\",\"calories\":200,\"category\":\"Snacks\",\"image_url\":\"https://cdn.tgdd.vn/2021/08/CookProduct/BeFunky-collage(28)-1200x676-1.jpg\"}",
                "{\"name\":\"Bún Bò Huế\",\"description\":\"Spicy beef noodle soup from Hue\",\"ingredients\":\"[{\\\"name\\\":\\\"Beef shank\\\",\\\"quantity\\\":\\\"500g\\\"},{\\\"name\\\":\\\"Rice noodles\\\",\\\"quantity\\\":\\\"400g\\\"},{\\\"name\\\":\\\"Lemongrass\\\",\\\"quantity\\\":\\\"2 stalks\\\"},{\\\"name\\\":\\\"Chili paste\\\",\\\"quantity\\\":\\\"2 tbsp\\\"},{\\\"name\\\":\\\"Banana blossom\\\",\\\"quantity\\\":\\\"100g\\\"}]\",\"steps\":\"[\\\"Boil beef for broth\\\",\\\"Add lemongrass and chili\\\",\\\"Cook noodles\\\",\\\"Serve with herbs and banana blossom\\\"]\",\"calories\":700,\"category\":\"Dinner\",\"image_url\":\"https://file.hstatic.net/200000700229/article/bun-bo-hue-1_da318989e7c2493f9e2c3e010e722466.jpg\"}",
                "{\"name\":\"Canh Chua\",\"description\":\"Sour soup with fish and tamarind\",\"ingredients\":\"[{\\\"name\\\":\\\"Fish fillet\\\",\\\"quantity\\\":\\\"300g\\\"},{\\\"name\\\":\\\"Tamarind paste\\\",\\\"quantity\\\":\\\"2 tbsp\\\"},{\\\"name\\\":\\\"Pineapple\\\",\\\"quantity\\\":\\\"100g\\\"},{\\\"name\\\":\\\"Tomato\\\",\\\"quantity\\\":\\\"2\\\"},{\\\"name\\\":\\\"Okra\\\",\\\"quantity\\\":\\\"100g\\\"}]\",\"steps\":\"[\\\"Boil water with tamarind\\\",\\\"Add fish and vegetables\\\",\\\"Simmer until cooked\\\",\\\"Serve with rice\\\"]\",\"calories\":350,\"category\":\"Dinner\",\"image_url\":\"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQldivONiT8mdxnMuWY5m3dyCYjMPToTjWKDg&s\"}",
                "{\"name\":\"Bánh Chưng\",\"description\":\"Sticky rice cake with pork and mung bean\",\"ingredients\":\"[{\\\"name\\\":\\\"Glutinous rice\\\",\\\"quantity\\\":\\\"500g\\\"},{\\\"name\\\":\\\"Pork\\\",\\\"quantity\\\":\\\"200g\\\"},{\\\"name\\\":\\\"Mung beans\\\",\\\"quantity\\\":\\\"200g\\\"},{\\\"name\\\":\\\"Banana leaves\\\",\\\"quantity\\\":\\\"10 leaves\\\"},{\\\"name\\\":\\\"Salt\\\",\\\"quantity\\\":\\\"1 tsp\\\"}]\",\"steps\":\"[\\\"Soak rice overnight\\\",\\\"Wrap ingredients in banana leaves\\\",\\\"Boil for 8 hours\\\",\\\"Serve sliced\\\"]\",\"calories\":600,\"category\":\"Breakfast\",\"image_url\":\"https://vcdn1-vnexpress.vnecdn.net/2023/01/15/Tet-BanhchungLoKhe-ThanhHue-Vn-1947-3492-1673796669.jpg?w=460&h=0&q=100&dpr=2&fit=crop&s=VVJS7LNDsVDjLw3NrbyeWw\"}",
                "{\"name\":\"Xôi Gà\",\"description\":\"Sticky rice topped with shredded chicken\",\"ingredients\":\"[{\\\"name\\\":\\\"Glutinous rice\\\",\\\"quantity\\\":\\\"300g\\\"},{\\\"name\\\":\\\"Chicken breast\\\",\\\"quantity\\\":\\\"200g\\\"},{\\\"name\\\":\\\"Fish sauce\\\",\\\"quantity\\\":\\\"1 tbsp\\\"},{\\\"name\\\":\\\"Scallion oil\\\",\\\"quantity\\\":\\\"2 tbsp\\\"},{\\\"name\\\":\\\"Peanuts\\\",\\\"quantity\\\":\\\"50g\\\"}]\",\"steps\":\"[\\\"Cook sticky rice\\\",\\\"Boil and shred chicken\\\",\\\"Top rice with chicken and scallion oil\\\",\\\"Garnish with peanuts\\\"]\",\"calories\":450,\"category\":\"Breakfast\",\"image_url\":\"https://i.ytimg.com/vi/gZt9CsZCpDM/maxresdefault.jpg\"}",
                "{\"name\":\"Mì Quảng\",\"description\":\"Turmeric rice noodles with pork and peanuts\",\"ingredients\":\"[{\\\"name\\\":\\\"Rice noodles\\\",\\\"quantity\\\":\\\"300g\\\"},{\\\"name\\\":\\\"Pork\\\",\\\"quantity\\\":\\\"200g\\\"},{\\\"name\\\":\\\"Peanuts\\\",\\\"quantity\\\":\\\"50g\\\"},{\\\"name\\\":\\\"Turmeric\\\",\\\"quantity\\\":\\\"1 tsp\\\"},{\\\"name\\\":\\\"Fish sauce\\\",\\\"quantity\\\":\\\"2 tbsp\\\"}]\",\"steps\":\"[\\\"Cook noodles with turmeric\\\",\\\"Stir-fry pork\\\",\\\"Serve with peanuts and herbs\\\"]\",\"calories\":500,\"category\":\"Lunch\",\"image_url\":\"https://daivietourist.vn/wp-content/uploads/2025/06/mi-quang-hoi-an-2.jpg\"}",
                "{\"name\":\"Cao Lầu\",\"description\":\"Hoi An-style noodles with pork and greens\",\"ingredients\":\"[{\\\"name\\\":\\\"Rice noodles\\\",\\\"quantity\\\":\\\"300g\\\"},{\\\"name\\\":\\\"Pork loin\\\",\\\"quantity\\\":\\\"200g\\\"},{\\\"name\\\":\\\"Soy sauce\\\",\\\"quantity\\\":\\\"2 tbsp\\\"},{\\\"name\\\":\\\"Fresh greens\\\",\\\"quantity\\\":\\\"100g\\\"},{\\\"name\\\":\\\"Crisp rice crackers\\\",\\\"quantity\\\":\\\"50g\\\"}]\",\"steps\":\"[\\\"Cook noodles\\\",\\\"Marinate and grill pork\\\",\\\"Top with greens and crackers\\\",\\\"Serve with soy sauce\\\"]\",\"calories\":450,\"category\":\"Lunch\",\"image_url\":\"https://drt.danang.vn/content/images/2024/07/quan-cao-lau-ngon-o-hoi-an-1.jpg\"}",
                "{\"name\":\"Chè Ba Màu\",\"description\":\"Three-color dessert with beans and jelly\",\"ingredients\":\"[{\\\"name\\\":\\\"Red beans\\\",\\\"quantity\\\":\\\"100g\\\"},{\\\"name\\\":\\\"Mung beans\\\",\\\"quantity\\\":\\\"100g\\\"},{\\\"name\\\":\\\"Pandan jelly\\\",\\\"quantity\\\":\\\"100g\\\"},{\\\"name\\\":\\\"Coconut milk\\\",\\\"quantity\\\":\\\"200ml\\\"},{\\\"name\\\":\\\"Sugar\\\",\\\"quantity\\\":\\\"100g\\\"}]\",\"steps\":\"[\\\"Cook beans separately\\\",\\\"Prepare pandan jelly\\\",\\\"Layer ingredients in a glass\\\",\\\"Top with coconut milk\\\"]\",\"calories\":300,\"category\":\"Snacks\",\"image_url\":\"https://cdn.tgdd.vn/2021/11/CookDish/cach-nau-che-ba-mau-ngot-thom-thanh-mat-giai-nhiet-mua-he-avt-1200x676.jpg\"}",
                "{\"name\":\"Hủ Tiếu\",\"description\":\"Rice noodle soup with pork and shrimp\",\"ingredients\":\"[{\\\"name\\\":\\\"Rice noodles\\\",\\\"quantity\\\":\\\"300g\\\"},{\\\"name\\\":\\\"Pork\\\",\\\"quantity\\\":\\\"200g\\\"},{\\\"name\\\":\\\"Shrimp\\\",\\\"quantity\\\":\\\"150g\\\"},{\\\"name\\\":\\\"Bean sprouts\\\",\\\"quantity\\\":\\\"100g\\\"},{\\\"name\\\":\\\"Fish sauce\\\",\\\"quantity\\\":\\\"2 tbsp\\\"}]\",\"steps\":\"[\\\"Boil pork for broth\\\",\\\"Cook noodles\\\",\\\"Add shrimp and sprouts\\\",\\\"Serve with herbs\\\"]\",\"calories\":550,\"category\":\"Dinner\",\"image_url\":\"https://i-giadinh.vnecdn.net/2023/05/15/Bc8Thnhphm18-1684125639-9811-1684125654.jpg\"}",
                "{\"name\":\"Bánh Cuốn\",\"description\":\"Steamed rice rolls with minced pork\",\"ingredients\":\"[{\\\"name\\\":\\\"Rice flour\\\",\\\"quantity\\\":\\\"200g\\\"},{\\\"name\\\":\\\"Ground pork\\\",\\\"quantity\\\":\\\"150g\\\"},{\\\"name\\\":\\\"Wood ear mushrooms\\\",\\\"quantity\\\":\\\"50g\\\"},{\\\"name\\\":\\\"Fish sauce\\\",\\\"quantity\\\":\\\"2 tbsp\\\"},{\\\"name\\\":\\\"Fried shallots\\\",\\\"quantity\\\":\\\"2 tbsp\\\"}]\",\"steps\":\"[\\\"Prepare rice batter\\\",\\\"Steam thin rice layers\\\",\\\"Add pork and mushroom filling\\\",\\\"Serve with fish sauce and shallots\\\"]\",\"calories\":350,\"category\":\"Breakfast\",\"image_url\":\"https://cdn.tgdd.vn/2021/08/CookRecipe/Avatar/banh-cuon-nong-thit-bam-thumbnail.jpg\"}",
                "{\"name\":\"Nem Chua\",\"description\":\"Fermented pork roll\",\"ingredients\":\"[{\\\"name\\\":\\\"Ground pork\\\",\\\"quantity\\\":\\\"300g\\\"},{\\\"name\\\":\\\"Pork skin\\\",\\\"quantity\\\":\\\"100g\\\"},{\\\"name\\\":\\\"Garlic\\\",\\\"quantity\\\":\\\"2 cloves\\\"},{\\\"name\\\":\\\"Chili\\\",\\\"quantity\\\":\\\"1\\\"},{\\\"name\\\":\\\"Sugar\\\",\\\"quantity\\\":\\\"1 tbsp\\\"}]\",\"steps\":\"[\\\"Mix pork with spices\\\",\\\"Wrap in banana leaves\\\",\\\"Ferment for 2-3 days\\\",\\\"Serve sliced\\\"]\",\"calories\":250,\"category\":\"Snacks\",\"image_url\":\"https://cdn-i2.congthuong.vn/stores/news_dataimages/2023/122023/31/08/in_article/nc220231231084604.jpg?rt=20231231084607\"}",
                "{\"name\":\"Gà Kho Gừng\",\"description\":\"Braised chicken with ginger\",\"ingredients\":\"[{\\\"name\\\":\\\"Chicken thighs\\\",\\\"quantity\\\":\\\"500g\\\"},{\\\"name\\\":\\\"Ginger\\\",\\\"quantity\\\":\\\"50g\\\"},{\\\"name\\\":\\\"Fish sauce\\\",\\\"quantity\\\":\\\"2 tbsp\\\"},{\\\"name\\\":\\\"Sugar\\\",\\\"quantity\\\":\\\"1 tbsp\\\"},{\\\"name\\\":\\\"Garlic\\\",\\\"quantity\\\":\\\"2 cloves\\\"}]\",\"steps\":\"[\\\"Marinate chicken with spices\\\",\\\"Sauté ginger and garlic\\\",\\\"Braise chicken until tender\\\",\\\"Serve with rice\\\"]\",\"calories\":500,\"category\":\"Dinner\",\"image_url\":\"https://cdn.tgdd.vn/2022/03/CookRecipe/Avatar/canh-ga-kho-gung-thumbnail.jpg\"}",
                "{\"name\":\"Cháo Gà\",\"description\":\"Chicken rice porridge\",\"ingredients\":\"[{\\\"name\\\":\\\"Rice\\\",\\\"quantity\\\":\\\"100g\\\"},{\\\"name\\\":\\\"Chicken breast\\\",\\\"quantity\\\":\\\"200g\\\"},{\\\"name\\\":\\\"Ginger\\\",\\\"quantity\\\":\\\"20g\\\"},{\\\"name\\\":\\\"Scallions\\\",\\\"quantity\\\":\\\"2\\\"},{\\\"name\\\":\\\"Fish sauce\\\",\\\"quantity\\\":\\\"1 tbsp\\\"}]\",\"steps\":\"[\\\"Boil rice with water for porridge\\\",\\\"Shred cooked chicken\\\",\\\"Add ginger and scallions\\\",\\\"Serve hot\\\"]\",\"calories\":300,\"category\":\"Breakfast\",\"image_url\":\"https://static.hawonkoo.vn/hwk02/images/2023/10/cach-nau-chao-ga-1.jpg\"}"
        };

        for (String dishJson : dishes) {
            try {
                JSONObject json = new JSONObject(dishJson);
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME, json.getString("name"));
                values.put(COLUMN_DESCRIPTION, json.getString("description"));
                values.put(COLUMN_INGREDIENTS, json.getString("ingredients"));
                values.put(COLUMN_STEPS, json.getString("steps"));
                values.put(COLUMN_CALORIES, json.getInt("calories"));
                values.put(COLUMN_CATEGORY, json.getString("category"));
                values.put(COLUMN_IMAGE_URL, json.getString("image_url"));
                db.insert(TABLE_DISHES, null, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<Dish> getAllDishes() {
        List<Dish> dishes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DISHES, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Dish dish = new Dish();
            dish.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            dish.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
            dish.setIngredients(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS)));
            dish.setSteps(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STEPS)));
            dish.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIES)));
            dish.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
            dish.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)));
            dishes.add(dish);
        }
        cursor.close();
        return dishes;
    }

    public List<Dish> getFavorites() {
        List<Dish> favorites = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, null, null, null, null, null, COLUMN_NAME + " ASC");
        while (cursor.moveToNext()) {
            Dish dish = new Dish();
            dish.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            dish.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
            dish.setIngredients(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS)));
            dish.setSteps(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STEPS)));
            dish.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIES)));
            dish.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
            dish.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)));
            favorites.add(dish);
        }
        cursor.close();
        return favorites;
    }

    public List<Dish> getSuggestions() {
        List<Dish> suggestions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SUGGESTIONS, null, null, null, null, null, COLUMN_SUGGESTION_DATE + " DESC");
        while (cursor.moveToNext()) {
            Dish dish = new Dish();
            dish.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            dish.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
            dish.setIngredients(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS)));
            dish.setSteps(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STEPS)));
            dish.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIES)));
            dish.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
            dish.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)));
            suggestions.add(dish);
        }
        cursor.close();
        return suggestions;
    }

    public long addDish(Dish dish) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, dish.getName());
        values.put(COLUMN_DESCRIPTION, dish.getDescription());
        values.put(COLUMN_INGREDIENTS, dish.getIngredients());
        values.put(COLUMN_STEPS, dish.getSteps());
        values.put(COLUMN_CALORIES, dish.getCalories());
        values.put(COLUMN_CATEGORY, dish.getCategory());
        values.put(COLUMN_IMAGE_URL, dish.getImageUrl());
        long id = db.insertWithOnConflict(TABLE_DISHES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        return id;
    }

    public void addToFavorites(Dish dish) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, dish.getName());
        values.put(COLUMN_DESCRIPTION, dish.getDescription());
        values.put(COLUMN_INGREDIENTS, dish.getIngredients());
        values.put(COLUMN_STEPS, dish.getSteps());
        values.put(COLUMN_CALORIES, dish.getCalories());
        values.put(COLUMN_CATEGORY, dish.getCategory());
        values.put(COLUMN_IMAGE_URL, dish.getImageUrl());
        db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public boolean isFavorite(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{COLUMN_ID}, COLUMN_NAME + " = ?", new String[]{name}, null, null, null);
        boolean isFav = cursor.moveToFirst();
        cursor.close();
        return isFav;
    }

    public void removeFromFavorites(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, COLUMN_NAME + " = ?", new String[]{name});
    }

    public void addToSuggestions(Dish dish, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, dish.getName());
        values.put(COLUMN_DESCRIPTION, dish.getDescription());
        values.put(COLUMN_INGREDIENTS, dish.getIngredients());
        values.put(COLUMN_STEPS, dish.getSteps());
        values.put(COLUMN_CALORIES, dish.getCalories());
        values.put(COLUMN_CATEGORY, dish.getCategory());
        values.put(COLUMN_IMAGE_URL, dish.getImageUrl());
        values.put(COLUMN_SUGGESTION_DATE, date);
        db.insert(TABLE_SUGGESTIONS, null, values);
    }

    public void logMeal(MealLog log) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, log.getName());
        values.put(COLUMN_CALORIES, log.getCalories());
        values.put(COLUMN_LOG_DATE, log.getDate());
        db.insert(TABLE_MEAL_LOGS, null, values);
    }

    public List<MealLog> getMealLogs(String date) {
        List<MealLog> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MEAL_LOGS, null, COLUMN_LOG_DATE + " = ?", new String[]{date}, null, null, null);
        while (cursor.moveToNext()) {
            MealLog log = new MealLog(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIES)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_DATE))
            );
            logs.add(log);
        }
        cursor.close();
        return logs;
    }

    public boolean dishExists(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DISHES, new String[]{COLUMN_ID}, COLUMN_NAME + " = ?", new String[]{name}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public int getDailyCalories(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        int totalCalories = 0;
        Cursor cursor = db.query(TABLE_MEAL_LOGS, new String[]{COLUMN_CALORIES}, COLUMN_LOG_DATE + " = ?", new String[]{date}, null, null, null);
        while (cursor.moveToNext()) {
            totalCalories += cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIES));
        }
        cursor.close();
        return totalCalories;
    }

    // FIX: THÊM METHOD NÀY CHO DASHBOARD
    public int getWeeklyCalories(String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        int totalCalories = 0;
        Cursor cursor = db.query(TABLE_MEAL_LOGS, new String[]{COLUMN_CALORIES},
                COLUMN_LOG_DATE + " BETWEEN ? AND ?", new String[]{startDate, endDate}, null, null, null);
        while (cursor.moveToNext()) {
            totalCalories += cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIES));
        }
        cursor.close();
        return totalCalories;
    }

    // FIX: THÊM METHOD NÀY CHO HISTORY
    public List<MealLog> getAllMealLogs() {
        List<MealLog> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MEAL_LOGS, null, null, null, null, null, COLUMN_LOG_DATE + " DESC");
        while (cursor.moveToNext()) {
            MealLog log = new MealLog(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIES)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_DATE))
            );
            logs.add(log);
        }
        cursor.close();
        return logs;
    }

    // THÊM METHOD CHO DAILY SUGGESTION (nếu cần)
    public void addDailySuggestion(Dish dish, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, dish.getName());
        values.put(COLUMN_DESCRIPTION, dish.getDescription());
        values.put(COLUMN_INGREDIENTS, dish.getIngredients());
        values.put(COLUMN_STEPS, dish.getSteps());
        values.put(COLUMN_CALORIES, dish.getCalories());
        values.put(COLUMN_CATEGORY, dish.getCategory());
        values.put(COLUMN_IMAGE_URL, dish.getImageUrl());
        values.put(COLUMN_SUGGESTION_DATE, date);
        db.insert(TABLE_SUGGESTIONS, null, values);
    }

    public List<Dish> getDailySuggestions(String date) {
        List<Dish> dishes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SUGGESTIONS, null, COLUMN_SUGGESTION_DATE + " = ?", new String[]{date}, null, null, null);
        while (cursor.moveToNext()) {
            Dish dish = new Dish();
            dish.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            dish.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
            dish.setIngredients(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS)));
            dish.setSteps(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STEPS)));
            dish.setCalories(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIES)));
            dish.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
            dish.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)));
            dishes.add(dish);
        }
        cursor.close();
        return dishes;
    }

    // THÊM METHOD CHO API (nếu cần)
    public void saveDishFromJson(Context context, JSONObject json, String type) {
        try {
            Dish dish = new Dish();
            dish.setName(json.getString("name"));
            dish.setDescription(json.optString("description", ""));
            dish.setIngredients(json.getJSONArray("ingredients").toString());
            dish.setSteps(json.getJSONArray("steps").toString());
            dish.setCalories(json.getInt("calories"));
            dish.setCategory(json.optString("category", "Other"));
            dish.setImageUrl(json.optString("image_url", ""));
            addDish(dish);
            if ("suggestion".equals(type)) {
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                addToSuggestions(dish, date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}