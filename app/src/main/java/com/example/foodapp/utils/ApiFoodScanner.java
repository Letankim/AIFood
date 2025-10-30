package com.example.foodapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.example.foodapp.database.FoodDatabaseHelper;
import com.example.foodapp.models.Dish;

public class ApiFoodScanner {
    private static final String TAG = "ApiFoodScanner";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private static final List<ApiKey> GEMINI_KEYS = new ArrayList<>();
    private static final int MAX_IMAGE_SIZE_BYTES = 20 * 1024 * 1024;

    static {
        GEMINI_KEYS.add(new ApiKey("AIzaSyBR4Q2qgvZBrmT14S1agp6HYaiQwJBuGfA", 100, 1000));
        GEMINI_KEYS.add(new ApiKey("AIzaSyDZiIX2dLKwrA7nRvg1UTzFsDG_NA5kqBY", 100, 1000));
    }

    private static class ApiKey {
        String key;
        int weight;
        int remaining;

        ApiKey(String key, int weight, int remaining) {
            this.key = key;
            this.weight = weight;
            this.remaining = remaining;
        }
    }

    private static ApiKey getRandomKey() {
        List<ApiKey> available = new ArrayList<>();
        for (ApiKey k : GEMINI_KEYS) {
            if (k.remaining > 0) available.add(k);
        }
        if (available.isEmpty()) return null;

        int totalWeight = 0;
        for (ApiKey k : available) totalWeight += k.weight;
        int rand = new Random().nextInt(totalWeight + 1);
        int cumulative = 0;
        for (ApiKey k : available) {
            cumulative += k.weight;
            if (rand <= cumulative) return k;
        }
        return available.get(0);
    }

    public static void analyzeFoodByGemini(Bitmap image, String localTime, Context context, final ApiCallback callback) {
        if (!validateBitmap(image, callback)) return;

        String base64Image = bitmapToBase64(image);
        if (base64Image == null) {
            callback.onError("Failed to encode image to Base64");
            return;
        }

        String prompt = getAnalyzePrompt(localTime);
        JSONObject requestBody = createRequestBody(prompt, base64Image);
        sendRequest(requestBody, context, callback, 3);
    }

    public static void describeRecipeFromImage(Bitmap image, String localTime, Context context, final ApiCallback callback) {
        if (!validateBitmap(image, callback)) return;

        String base64Image = bitmapToBase64(image);
        if (base64Image == null) {
            callback.onError("Failed to encode image to Base64");
            return;
        }

        String prompt = getRecipePrompt(localTime);
        JSONObject requestBody = createRequestBody(prompt, base64Image);
        sendRequest(requestBody, context, callback, 3);
    }

    public static void suggestDishesFromIngredients(String ingredients, String localTime, Context context, final ApiCallback callback) {
        if (ingredients == null || ingredients.trim().isEmpty()) {
            callback.onError("Ingredients list cannot be empty");
            return;
        }

        String prompt = "Suggest Vietnamese dishes from these ingredients: " + ingredients + ". Return in pure JSON format with {\"dishes\": [array of objects, each with dish_name, description, ingredients (array of {name, quantity}), steps (array of strings), estimated_time_minutes, difficulty_level, nutrition {total_calories, protein_g, carbohydrates_g, fat_g}, confidence_score]}. Only suggest safe, edible dishes. Category based on time " + localTime + ": 05:00–10:59 → Breakfast, 11:00–14:59 → Lunch, 15:00–22:59 → Dinner, 23:00–04:59 → Snacks.";

        JSONObject requestBody = createTextRequestBody(prompt);
        sendRequest(requestBody, context, callback, 3);
    }

    public static void suggestDailyDishes(Context context, final ApiCallback callback) {
        try {
            String prompt = "Suggest 5 random Vietnamese dishes. Return in pure JSON format with {\"dishes\": [array of objects, each with dish_name, description, ingredients (array of {name, quantity}), steps (array of strings), nutrition {total_calories, protein_g, carbohydrates_g, fat_g}, category (Breakfast, Lunch, Dinner, or Snacks based on time)}]}. Only safe, edible dishes.";

            JSONObject requestBody = createTextRequestBody(prompt);
            sendRequest(requestBody, context, callback, 3);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in suggestDailyDishes", e);
            callback.onError("Unexpected error: " + e.getMessage());
        }
    }

    public static void saveDishFromJson(Context context, JSONObject json, String type) {
        try {
            FoodDatabaseHelper dbHelper = new FoodDatabaseHelper(context);
            Dish dish = new Dish();
            dish.setName(json.getString("dish_name"));
            dish.setDescription(json.optString("description", ""));
            dish.setIngredients(json.getJSONArray("ingredients").toString());
            dish.setSteps(json.getJSONArray("steps").toString());
            dish.setCalories(json.getJSONObject("nutrition").getInt("total_calories"));
            dish.setCategory(json.getString("category"));

            long id = dbHelper.addDish(dish);
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            dbHelper.addToSuggestions(dish, date);
        } catch (JSONException e) {
            Log.e(TAG, "Error saving dish from JSON", e);
        }
    }

    private static boolean validateBitmap(Bitmap image, ApiCallback callback) {
        if (image == null || image.isRecycled()) {
            callback.onError("Invalid or recycled bitmap");
            return false;
        }
        if (image.getWidth() == 0 || image.getHeight() == 0) {
            callback.onError("Bitmap has invalid dimensions");
            return false;
        }
        return true;
    }

    private static String bitmapToBase64(Bitmap image) {
        try {
            // Resize image if too large
            int maxDimension = 1024; // Limit to reduce size
            int width = image.getWidth();
            int height = image.getHeight();
            if (width > maxDimension || height > maxDimension) {
                float scale = Math.min((float) maxDimension / width, (float) maxDimension / height);
                int newWidth = Math.round(width * scale);
                int newHeight = Math.round(height * scale);
                image = Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 85, baos); // Reduced quality to 85
            byte[] imageBytes = baos.toByteArray();
            if (imageBytes.length > MAX_IMAGE_SIZE_BYTES) {
                Log.e(TAG, "Image size exceeds 20MB limit: " + imageBytes.length);
                return null;
            }
            // Use NO_WRAP to avoid line breaks in Base64 string
            return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Error encoding bitmap to Base64", e);
            return null;
        }
    }

    private static JSONObject createRequestBody(String prompt, String base64Image) {
        JSONObject requestBody = new JSONObject();
        try {
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            parts.put(new JSONObject().put("text", prompt));
            if (base64Image != null) {
                JSONObject inlineData = new JSONObject();
                inlineData.put("mime_type", "image/jpeg");
                inlineData.put("data", base64Image);
                parts.put(new JSONObject().put("inline_data", inlineData));
            }
            content.put("parts", parts);
            contents.put(content);
            requestBody.put("contents", contents);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating request body", e);
        }
        return requestBody;
    }

    private static JSONObject createTextRequestBody(String prompt) {
        return createRequestBody(prompt, null);
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static void sendRequest(final JSONObject requestBody, final Context context, final ApiCallback callback, final int retry) {
        ApiKey keyInfo = getRandomKey();
        if (keyInfo == null) {
            callback.onError("No available API keys");
            return;
        }

        if (!isNetworkAvailable(context)) {
            callback.onError("No network connection available");
            return;
        }

        String url = BASE_URL + "?key=" + keyInfo.key;
        Log.d(TAG, "Request URL: " + url);
        Log.d(TAG, "Request Body: " + requestBody.toString());
        Log.d(TAG, "Selected API key: " + keyInfo.key + ", Remaining: " + keyInfo.remaining);
        Log.d(TAG, "Sending request with retry count: " + retry);

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String rawText = response.getJSONArray("candidates").getJSONObject(0)
                                    .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
                            String jsonString = extractJsonFromText(rawText);
                            if (jsonString != null) {
                                JSONObject json = new JSONObject(jsonString);
                                callback.onSuccess(json);
                                keyInfo.remaining--;
                            } else {
                                callback.onError("Cannot parse JSON from response");
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parse error: " + e.getMessage());
                            callback.onError("JSON parse error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int status = error.networkResponse != null ? error.networkResponse.statusCode : 0;
                        String responseData = error.networkResponse != null && error.networkResponse.data != null
                                ? new String(error.networkResponse.data) : "No response data";
                        String errorMessage = error.getMessage() != null ? error.getMessage() : "Unknown error";

                        // Handle specific status codes
                        if (status == 400) {
                            errorMessage = "Invalid request: Check image encoding or API parameters. " + responseData;
                        } else if (status == 403 || status == 429) {
                            keyInfo.remaining = 0;
                            errorMessage = "API quota exceeded or invalid key: " + responseData;
                        } else if (status == 0) {
                            errorMessage = "Network error: No response from server. Check connectivity or server status.";
                        }

                        Log.e(TAG, "Volley error: Status=" + status + ", Message=" + errorMessage + ", Data=" + responseData);

                        if (retry > 1) {
                            Log.d(TAG, "Retrying request, attempts left: " + (retry - 1));
                            sendRequest(requestBody, context, callback, retry - 1);
                        } else {
                            callback.onError(errorMessage);
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        // Increased timeout and retries for robustness
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 3, 1.5f));
        queue.add(request);
    }

    private static String extractJsonFromText(String rawText) {
        if (rawText == null) return null;
        int start = rawText.indexOf("{");
        int end = rawText.lastIndexOf("}") + 1;
        if (start != -1 && end != -1 && end > start) {
            return rawText.substring(start, end);
        }
        Log.e(TAG, "Failed to extract JSON from response: " + rawText);
        return null;
    }

    private static String getAnalyzePrompt(String localTime) {
        return "This is a photo of food. Please analyze the image and return the result in pure JSON format with the following structure **only if** the food appears safe and edible for humans.\n" +
                "Reject any image that:\n" +
                "- Does not clearly depict food,\n" +
                "- Contains spoiled, moldy, rotten, or burnt food,\n" +
                "- Contains ingredients or dishes that are toxic, dangerous, or potentially fatal (e.g., poisonous mushrooms, aconite, raw pufferfish, cây lá ngón, etc.).\n" +
                "You will receive:\n" +
                "- An image of food.\n" +
                "- The local time (in 24-hour format) when the user submitted the image: " + localTime + ".\n" +
                "Rules for determining \"category\" based on submission time:\n" +
                "- 05:00–10:59 → \"Breakfast\"\n" +
                "- 11:00–14:59 → \"Lunch\"\n" +
                "- 15:00–22:59 → \"Dinner\"\n" +
                "- 23:00–04:59 → \"Snacks\"\n" +
                "If valid and edible, return this JSON structure:\n" +
                "{\n" +
                "  \"food_name\": \"<name of the dish>\",\n" +
                "  \"quantity\": 1,\n" +
                "  \"category\": \"<Breakfast | Lunch | Dinner | Snacks>\",\n" +
                "  \"predictions\": [\n" +
                "    {\n" +
                "      \"total_weight\": <weight in grams>,\n" +
                "      \"calories\": <calorie value>,\n" +
                "      \"fat\": <grams of fat>,\n" +
                "      \"carbs\": <grams of carbs>,\n" +
                "      \"protein\": <grams of protein>\n" +
                "    }\n" +
                "  ],\n" +
                "  \"quality\": \"<fresh | spoiled | moldy | burnt | unknown>\",\n" +
                "  \"metrics\": {},\n" +
                "  \"is_food_image\": true,\n" +
                "  \"food_confidence\": <confidence score from 0 to 1>\n" +
                "}\n" +
                "If the image is not valid or the food is unsafe, return:\n" +
                "{\n" +
                "  \"error_message\": \"The image does not show a recognizable and safe-to-eat dish. Please try again with a clear, edible, and safe food image.\"\n" +
                "}\n" +
                "Only return pure JSON. No explanation, markdown, or additional text.";
    }

    private static String getRecipePrompt(String localTime) {
        return "You are a professional AI chef capable of recognizing any dish from an image and providing detailed cooking instructions.\n" +
                "Please analyze the provided image and return the result in **pure JSON format** with one of the following two options:\n" +
                "You will receive:\n" +
                "- An image of food.\n" +
                "- The local time (in 24-hour format) when the user submitted the image: " + localTime + ".\n" +
                "Rules for determining \"category\" based on submission time:\n" +
                "- 05:00–10:59 → \"Breakfast\"\n" +
                "- 11:00–14:59 → \"Lunch\"\n" +
                "- 15:00–22:59 → \"Dinner\"\n" +
                "- 23:00–04:59 → \"Snacks\"\n" +
                "1. If the image clearly contains a recognizable food or dish **that appears fresh, safe for human consumption, and contains no toxic or hazardous ingredients** (e.g., spoiled food, moldy items, poisonous plants or animals such as aconite, death cap mushrooms, raw pufferfish, etc.):\n" +
                "{\n" +
                "  \"dish_name\": \"<Name of the dish>\",\n" +
                "  \"description\": \"<Description of the dish>\",\n" +
                "  \"category\": \"<Breakfast | Lunch | Dinner | Snacks>\",\n" +
                "  \"ingredients\": [\n" +
                "    { \"name\": \"<ingredient name>\", \"quantity\": \"<amount and unit>\" }\n" +
                "  ],\n" +
                "  \"steps\": [\n" +
                "    \"<step 1 with detailed instructions>\",\n" +
                "    \"<step 2 with detailed instructions>\",\n" +
                "    \"...\"\n" +
                "  ],\n" +
                "  \"estimated_time_minutes\": <estimated cooking time in minutes>,\n" +
                "  \"difficulty_level\": \"<easy | medium | hard>\",\n" +
                "  \"nutrition\": {\n" +
                "    \"total_calories\": <total estimated calories>,\n" +
                "    \"protein_g\": <estimated protein in grams>,\n" +
                "    \"carbohydrates_g\": <estimated carbohydrates in grams>,\n" +
                "    \"fat_g\": <estimated fat in grams>\n" +
                "  },\n" +
                "  \"confidence_score\": <a value from 0 to 1 representing model confidence>\n" +
                "}\n" +
                "2. If the image does NOT appear to contain a recognizable dish, **or** the food appears spoiled, unsafe, toxic, or potentially harmful to health:\n" +
                "{\n" +
                "  \"error_message\": \"The image does not appear to contain a recognizable or safe-to-eat dish. Please try again with a valid, edible, and safe food image.\"\n" +
                "}\n" +
                "Return **only** one of the two JSON formats above. Do NOT provide any explanation, markdown, or additional text outside the JSON.";
    }

    public interface ApiCallback {
        void onSuccess(JSONObject result);
        void onError(String error);
    }
}