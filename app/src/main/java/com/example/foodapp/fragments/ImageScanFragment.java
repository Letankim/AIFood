package com.example.foodapp.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.foodapp.R;
import com.example.foodapp.database.FoodDatabaseHelper;
import com.example.foodapp.models.MealLog;
import com.example.foodapp.utils.ApiFoodScanner;
import com.google.android.material.card.MaterialCardView;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageScanFragment extends Fragment {

    private static final String TAG = "ImageScanFragment";
    private PreviewView previewView;
    private Button captureButton, uploadButton, logMealButton;
    private TextView dishNameTv, categoryTv, ingredientsTv, stepsTv, nutritionTv;
    private ProgressBar loadingProgress;
    private MaterialCardView resultsCard;
    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    private boolean isCameraBound = false;
    private FoodDatabaseHelper dbHelper;
    private JSONObject lastScanResult;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri == null) {
                        showError("Không thể truy cập hình ảnh từ thư viện");
                        return;
                    }
                    new DecodeBitmapTask().execute(uri);
                } else {
                    showError("Không chọn được hình ảnh từ thư viện");
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new FoodDatabaseHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_scan, container, false);

        previewView = view.findViewById(R.id.preview_view);
        captureButton = view.findViewById(R.id.capture_button);
        uploadButton = view.findViewById(R.id.upload_button);
        logMealButton = view.findViewById(R.id.log_meal_button);
        dishNameTv = view.findViewById(R.id.dish_name);
        categoryTv = view.findViewById(R.id.dish_category);
        ingredientsTv = view.findViewById(R.id.dish_ingredients);
        stepsTv = view.findViewById(R.id.dish_steps);
        nutritionTv = view.findViewById(R.id.dish_nutrition);
        loadingProgress = view.findViewById(R.id.loading_progress);
        resultsCard = view.findViewById(R.id.results_card);

        checkCameraPermission();

        captureButton.setOnClickListener(v -> takePhoto());
        uploadButton.setOnClickListener(v -> openGallery());
        logMealButton.setOnClickListener(v -> logMeal());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isCameraBound) {
            startCamera();
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        }
    }

    private void checkStoragePermission() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
        } else {
            requestPermissions(permissions, 101);
        }
    }

    private void openGallery() {
        checkStoragePermission();
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            galleryLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi mở thư viện", e);
            showError("Không thể mở thư viện ảnh: " + e.getMessage());
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setTargetResolution(new android.util.Size(1024, 1024))
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                isCameraBound = true;
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Lỗi khi khởi động camera", e);
                showError("Lỗi khi khởi động camera: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            showError("Camera chưa được khởi tạo");
            return;
        }

        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull androidx.camera.core.ImageProxy image) {
                try {
                    Bitmap bitmap = image.toBitmap();
                    if (bitmap == null) {
                        showError("Không thể chụp ảnh");
                        return;
                    }
                    processImage(bitmap);
                } finally {
                    image.close();
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Lỗi khi chụp ảnh", exception);
                showError("Lỗi khi chụp ảnh: " + exception.getMessage());
            }
        });
    }

    private void processImage(Bitmap bitmap) {
        loadingProgress.setVisibility(View.VISIBLE);
        resultsCard.setVisibility(View.GONE);
        nutritionTv.setText("Đang xử lý hình ảnh...");
        String localTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        ApiFoodScanner.describeRecipeFromImage(bitmap, localTime, getContext(), new ApiFoodScanner.ApiCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                requireActivity().runOnUiThread(() -> {
                    try {
                        lastScanResult = result; // Store result for logging
                        ApiFoodScanner.saveDishFromJson(getContext(), result, "scan");
                        displayDish(result);
                        resultsCard.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi lưu hoặc hiển thị món ăn", e);
                        showError("Lỗi khi xử lý kết quả: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    Log.e(TAG, "Lỗi API: " + error);
                    showError("Lỗi: " + error);
                });
            }
        });
    }

    private void displayDish(JSONObject json) {
        try {
            dishNameTv.setText(json.getString("dish_name"));
            categoryTv.setText(json.getString("category"));
            JSONArray ing = json.getJSONArray("ingredients");
            StringBuilder ingStr = new StringBuilder("Nguyên liệu:\n");
            for (int i = 0; i < ing.length(); i++) {
                JSONObject item = ing.getJSONObject(i);
                ingStr.append(item.getString("name")).append(": ").append(item.getString("quantity")).append("\n");
            }
            ingredientsTv.setText(ingStr.toString());

            JSONArray steps = json.getJSONArray("steps");
            StringBuilder stepsStr = new StringBuilder("Các bước thực hiện:\n");
            for (int i = 0; i < steps.length(); i++) {
                stepsStr.append((i + 1)).append(". ").append(steps.getString(i)).append("\n");
            }
            stepsTv.setText(stepsStr.toString());

            JSONObject nutr = json.getJSONObject("nutrition");
            nutritionTv.setText("Dinh dưỡng:\nCalories: " + nutr.getInt("total_calories") + "\nProtein: " + nutr.getInt("protein_g") + "g\nCarbs: " + nutr.getInt("carbohydrates_g") + "g\nFat: " + nutr.getInt("fat_g") + "g");
        } catch (JSONException e) {
            Log.e(TAG, "Lỗi khi hiển thị món ăn", e);
            showError("Lỗi khi hiển thị món ăn: " + e.getMessage());
        }
    }

    private void logMeal() {
        if (lastScanResult == null) {
            showError("Không có dữ liệu món ăn để ghi log");
            return;
        }
        executorService.execute(() -> {
            try {
                String dishName = lastScanResult.getString("dish_name");
                JSONObject nutrition = lastScanResult.getJSONObject("nutrition");
                int calories = nutrition.getInt("total_calories");
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                MealLog log = new MealLog(dishName, calories, date);
                dbHelper.logMeal(log);
                if (getActivity() != null && getContext() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Đã ghi món ăn vào nhật ký", Toast.LENGTH_SHORT).show());
                }
            } catch (JSONException e) {
                Log.e(TAG, "Lỗi khi ghi log món ăn", e);
                if (getActivity() != null && getContext() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Lỗi khi ghi log: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void showError(String message) {
        loadingProgress.setVisibility(View.GONE);
        resultsCard.setVisibility(View.GONE);
        nutritionTv.setText(message);
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Storage permission granted
        } else {
            showError("Cần quyền truy cập để sử dụng camera hoặc thư viện ảnh");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            isCameraBound = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    private class DecodeBitmapTask extends AsyncTask<Uri, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            loadingProgress.setVisibility(View.VISIBLE);
            resultsCard.setVisibility(View.GONE);
            nutritionTv.setText("Đang xử lý hình ảnh...");
        }

        @Override
        protected Bitmap doInBackground(Uri... uris) {
            try {
                InputStream is = getContext().getContentResolver().openInputStream(uris[0]);
                if (is == null) {
                    return null;
                }
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
                is.close();
                return bitmap;
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi giải mã bitmap", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                showError("Không thể tải hình ảnh từ thư viện");
            } else {
                processImage(bitmap);
            }
        }
    }
}