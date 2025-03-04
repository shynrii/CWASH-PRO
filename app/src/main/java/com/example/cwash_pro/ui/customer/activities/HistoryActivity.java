package com.example.cwash_pro.ui.customer.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.cwash_pro.R;
import com.example.cwash_pro.adapters.HistoryAdapter;
import com.example.cwash_pro.apis.ApiService;
import com.example.cwash_pro.apis.RetrofitClient;
import com.example.cwash_pro.models.Schedule;
import com.example.cwash_pro.models.ServerResponse;
import com.example.cwash_pro.ui.dialog.CustomDialogProgress;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView rvHistory;
    private ImageView imgBack;
    private List<Schedule> scheduleList = new ArrayList<>();
    HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initView();
        imgBack.setOnClickListener(v -> onBackPressed());
        final CustomDialogProgress dialog = new CustomDialogProgress(this);
        dialog.show();
        RetrofitClient.getInstance().create(ApiService.class).getSchedulesUser().enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response) {
                if (response.body() != null) {
                    scheduleList = response.body().schedules;
                }
                historyAdapter = new HistoryAdapter(HistoryActivity.this, scheduleList, (view, pos) -> {
                    if (view.getId() == R.id.btnCancelSchedule) {
                        AlertDialog builder = new AlertDialog.Builder(HistoryActivity.this).create();
                        View dialog1 = LayoutInflater.from(HistoryActivity.this).inflate(R.layout.dialog_cancel_schedule, null);
                        EditText edtNote = dialog1.findViewById(R.id.edtNote);
                        Button btnCancel = dialog1.findViewById(R.id.btnOKCancel);
                        Button btnNone = dialog1.findViewById(R.id.btnNone);
                        btnNone.setOnClickListener(view1 -> {
                            builder.dismiss();
                        });
                        btnCancel.setOnClickListener(v1 -> {
                            final CustomDialogProgress customDialogProgressCancel = new CustomDialogProgress(HistoryActivity.this);
                            customDialogProgressCancel.show();
                            RetrofitClient.getInstance().create(ApiService.class).cancel(scheduleList.get(pos).getId(), edtNote.getText().toString(), "Canceled").enqueue(new Callback<ServerResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<ServerResponse> call2, @NonNull Response<ServerResponse> response2) {
                                    if (response2.body() != null && response2.body().success) {
                                        Toast.makeText(HistoryActivity.this, "Hủy thành công", Toast.LENGTH_SHORT).show();
                                        scheduleList.remove(pos);
                                        builder.dismiss();
                                        finish();
                                        startActivity(getIntent());
                                        customDialogProgressCancel.dismiss();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<ServerResponse> call12, @NonNull Throwable t) {
                                    Log.d("onFailure: ", t.getMessage());
                                }
                            });
                        });
                        builder.setView(dialog1);
                        builder.show();
                    } else if (view.getId() == R.id.btnConfirmVehicle) {
                        final CustomDialogProgress customDialogProgressCancel = new CustomDialogProgress(HistoryActivity.this);
                        customDialogProgressCancel.show();
                        RetrofitClient.getInstance().create(ApiService.class).confirmVehicle(scheduleList.get(pos).getId(), true).enqueue(new Callback<ServerResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<ServerResponse> call1, @NonNull Response<ServerResponse> response1) {
                                if (response1.body() != null && response1.body().success) {
                                    finish();
                                    startActivity(getIntent());
                                    customDialogProgressCancel.dismiss();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ServerResponse> call1, @NonNull Throwable t) {
                                Log.d("onFailure: ", t.getMessage());

                            }
                        });
                    }

                });
                rvHistory.setLayoutManager(new LinearLayoutManager(HistoryActivity.this, LinearLayoutManager.VERTICAL, false));
                rvHistory.setAdapter(historyAdapter);
                dialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
            }
        });
    }

    private void initView() {
        rvHistory =  findViewById(R.id.rvHistory);
        imgBack = findViewById(R.id.imgBack);
    }
}
