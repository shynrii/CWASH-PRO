package com.example.cwash_pro.ui.customer.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.cwash_pro.R;
import com.example.cwash_pro.adapters.ChooseTimeAdapter;
import com.example.cwash_pro.adapters.ChooseVehicleAdapter;
import com.example.cwash_pro.adapters.ServiceAdapter;
import com.example.cwash_pro.apis.ApiService;
import com.example.cwash_pro.apis.RetrofitClient;
import com.example.cwash_pro.models.Schedule;
import com.example.cwash_pro.models.ScheduleBody;
import com.example.cwash_pro.models.ServerResponse;
import com.example.cwash_pro.models.Service;
import com.example.cwash_pro.models.Time;
import com.example.cwash_pro.models.User;
import com.example.cwash_pro.models.Vehicle;
import com.example.cwash_pro.services.RemindService;
import com.example.cwash_pro.ui.dialog.CustomDialogProgress;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarWashServiceActivity extends AppCompatActivity {
    List<Vehicle> vehicleList;
    List<Service> serviceList;
    private Spinner spnVehicleCar;
    private TextView tvDate;
    private ImageView imgBack;
    LinearLayout tvChooseDate;
    private RecyclerView rvTime, rvService;
    private Button btnBook;
    private List<Time> timeListOfCar;
    private List<Time> timeListOfMoto;
    private List<Time> timeList;
    private String dateBook = "", timeBook, vehicle;
    ServiceAdapter serviceAdapter;
    List<Schedule> schedules;
    List<Schedule> schedulesPending;
    List<User> staffList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_wash_service);
        initView();
        vehicleList = new ArrayList<>();
        serviceList = new ArrayList<>();
        timeList = new ArrayList<>();
        timeListOfCar = new ArrayList<>();
        timeListOfMoto = new ArrayList<>();
        schedules = new ArrayList<>();
        schedulesPending = new ArrayList<>();
        staffList = new ArrayList<>();
        getVehicle();
        getServices();
        setTimeServiceMoto();
        setTimeServiceCar();
        timeList = timeListOfMoto;
        getStatusSchedulePending();
        rvTime.setLayoutManager(new GridLayoutManager(this, 3, RecyclerView.HORIZONTAL, false));
        imgBack.setOnClickListener(v -> onBackPressed());
        tvChooseDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(CarWashServiceActivity.this, (view, year1, monthOfYear, dayOfMonth) -> {
                tvDate.setText("Ngày " + dayOfMonth + " tháng " + (monthOfYear + 1) + " năm " + year1);
                dateBook = dayOfMonth + "/" + (monthOfYear + 1);
                rvTime.setAdapter(new ChooseTimeAdapter(this, timeList, dateBook, staffList, schedulesPending, (v1, pos) -> timeBook = timeList.get(pos).getTime())
                );
            }, year, month, day);
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            datePickerDialog.show();
        });
        spnVehicleCar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vehicle = vehicleList.get(position).getId();
                if (vehicleList.get(position).getType().equals("Motorcycle")) {
                    timeList = timeListOfMoto;
                    rvTime.setAdapter(chooseTimeAdapter());
                } else if (vehicleList.get(position).getType().equals("Car")) {
                    timeList = timeListOfCar;
                    rvTime.setAdapter(chooseTimeAdapter());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        rvTime.setAdapter(chooseTimeAdapter());
        btnBook.setOnClickListener(v ->

        {
            List<Service> serviceSelect = new ArrayList<>();
            for (int i = 0; i < serviceList.size(); i++) {
                if (serviceList.get(i).isChecked()) {
                    serviceSelect.add(serviceList.get(i));
                }
            }
            if (vehicle == null) {
                Toast.makeText(CarWashServiceActivity.this, "Bạn chưa chọn xe", Toast.LENGTH_SHORT).show();
            } else if (dateBook == null) {
                Toast.makeText(CarWashServiceActivity.this, "Bạn chưa chọn thời gian", Toast.LENGTH_SHORT).show();
            } else if (timeBook == null) {
                Toast.makeText(CarWashServiceActivity.this, "Bạn chưa chọn thời gian", Toast.LENGTH_SHORT).show();
            } else if (serviceSelect.size() == 0) {
                Toast.makeText(CarWashServiceActivity.this, "Bạn chưa chọn dịch vụ", Toast.LENGTH_SHORT).show();
            } else {
                ScheduleBody scheduleBody = new ScheduleBody();
                scheduleBody.setTimeBook(timeBook + " @ " + dateBook);
                scheduleBody.setVehicle(vehicle);
                scheduleBody.setServices(serviceSelect);
                final CustomDialogProgress dialogLoadBook = new CustomDialogProgress(this);
                dialogLoadBook.show();
                RetrofitClient.getInstance().create(ApiService.class).book(scheduleBody).enqueue(new Callback<ServerResponse>() {
                    @Override
                    public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                        RetrofitClient.getInstance().create(ApiService.class).getSchedulesUser().enqueue(new Callback<ServerResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response2) {
                                if (response2.body() != null) {


                                    String time = response2.body().schedules.get(0).getTimeBook() + "/2022";
//                                        time = "9:44 @ 18/1/2022";
//
                                    Log.e("AAAAAAAAAAAAaa", "onResponse: " + time);
                                    Log.e("AAAAAAAAAAAAaa", "onResponse: " +  response2.body().schedules.get(0).getTimeBook() + "/2022");
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm @ dd/MM/yyyy");
                                    try {
                                        setNotificationTime(simpleDateFormat.parse(time).getTime());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }

                            @Override
                            public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
                            }
                        });
                        AlertDialog builder = new AlertDialog.Builder(CarWashServiceActivity.this).create();
                        View dialog;
                        if (response.body() != null) {
                            if (response.body().success) {
                                dialog = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_success, null);
                                Button btnClose = dialog.findViewById(R.id.btnClose);
                                LinearLayout rootView = dialog.findViewById(R.id.rootView);
                                LottieAnimationView lottieAnimationView = rootView.findViewById(R.id.lottieAnimation);
                                lottieAnimationView.setAnimation("done-animation.json");
                                lottieAnimationView.playAnimation();
                                builder.getWindow().setBackgroundDrawableResource(R.drawable.dialog);
                                btnClose.setOnClickListener(v -> {
                                    builder.dismiss();
                                    finish();
                                });
                            } else {
                                dialog = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_failed, null);
                                Button btnClose = dialog.findViewById(R.id.btnClose);
                                TextView tvMessage = dialog.findViewById(R.id.tvMessage);
                                tvMessage.setText(response.body().message);
                                LinearLayout rootView = dialog.findViewById(R.id.rootView);
                                LottieAnimationView lottieAnimationView = rootView.findViewById(R.id.lottieAnimation);
                                lottieAnimationView.setAnimation("fail-animation.json");
                                lottieAnimationView.playAnimation();
                                builder.getWindow().setBackgroundDrawableResource(R.drawable.dialog);
                                btnClose.setOnClickListener(v -> {
                                    builder.dismiss();
                                    finish();
                                });
                            }
                            builder.setView(dialog);
                        }
                        builder.show();
                        dialogLoadBook.dismiss();
                    }

                    @Override
                    public void onFailure(Call<ServerResponse> call, Throwable t) {dialogLoadBook.dismiss();
                        Log.e("onFailure: ", t.getMessage());
//                        String time = scheduleBody.getTimeBook() + "/2022";
////                                        time = "23:35 & 17/1/2021";
////                                        String hour = time.substring(0, 5);
//                        Log.e("AAAAAAAAAAAAaa", "onResponse: " + time);
//                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm & dd/MM/yyyy");
//                        try {
//                            setNotificationTime(simpleDateFormat.parse(time).getTime());
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
                    }
                });
            }
        });
    }

    private void getStatusSchedulePending() {
        final CustomDialogProgress dialogLoadBook = new CustomDialogProgress(this);
        dialogLoadBook.show();
        RetrofitClient.getInstance().create(ApiService.class).getSchedulePending().enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response) {
                if (response.body() != null) {
                    if (response.body().success) {
                        schedulesPending = response.body().schedules;
                        staffList = response.body().users;
                        Log.e("TAG", "onResponse: " + schedulesPending.size());
                    } else {
                        Toast.makeText(CarWashServiceActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
                    }
                    dialogLoadBook.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
                Log.e("onFailureGetNumber: ", t.getMessage());
            }
        });
    }


    private void initView() {
        spnVehicleCar = findViewById(R.id.spnVehicleCar);
        tvChooseDate = findViewById(R.id.tvChooseDate);
        imgBack = findViewById(R.id.imgBack);
        tvDate = findViewById(R.id.tvDate);
        rvTime = findViewById(R.id.rvTime);
        rvService = findViewById(R.id.rvService);
        btnBook = findViewById(R.id.btnBook);
    }


    private void getServices() {
        final CustomDialogProgress dialogLoadBook = new CustomDialogProgress(this);
        dialogLoadBook.show();
        RetrofitClient.getInstance().create(ApiService.class).getServices().enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response) {
                if (response.body() != null) {
                    if (response.body().success) {
                        serviceList = response.body().services;
                        Log.e("onResponse: ", String.valueOf(serviceList.size()));
                        serviceAdapter = new ServiceAdapter(CarWashServiceActivity.this, serviceList);
                        Log.e("onCreate: ", String.valueOf(serviceList.size()));
                        rvService.setLayoutManager(new LinearLayoutManager(CarWashServiceActivity.this));
                        rvService.setAdapter(serviceAdapter);
                    } else {
                        Toast.makeText(CarWashServiceActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
                    }
                    dialogLoadBook.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
                Log.e("onFailure: ", t.getMessage());
            }
        });
    }

    private void getVehicle() {
        final CustomDialogProgress dialogLoadBook = new CustomDialogProgress(this);
        dialogLoadBook.show();
        RetrofitClient.getInstance().create(ApiService.class).getVehicle().enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response) {
                if (response.body() != null) {
                    if (response.body().success) {
                        vehicleList = response.body().vehicles;
                        ChooseVehicleAdapter chooseVehicleAdapter = new ChooseVehicleAdapter(CarWashServiceActivity.this, vehicleList);
                        spnVehicleCar.setAdapter(chooseVehicleAdapter);
                    } else {
                        Toast.makeText(CarWashServiceActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
                    }
                    dialogLoadBook.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
                Log.e("getVehicles: ", t.getMessage());
            }
        });
    }

    public void setTimeServiceMoto() {
        for (int i = 8; i <= 19; i++) {
            for (int j = 0; j < 60; j = j + 15) {
                String timeMoto;
                if (j < 10) {
                    timeMoto = i + ":" + j + "0";
                } else {
                    timeMoto = i + ":" + j;
                }
                timeListOfMoto.add(new Time(timeMoto, false, true));
            }
        }
    }

    public void setTimeServiceCar() {
        for (int i = 8; i <= 19; i++) {
            for (int j = 0; j < 60; j = j + 30) {
                String timeCar;
                if (j < 10) {
                    timeCar = i + ":" + j + "0";
                }else {
                    timeCar = i + ":" + j;
                }
                timeListOfCar.add(new Time(timeCar, false, true));
            }
        }
    }


    private ChooseTimeAdapter chooseTimeAdapter() {
        return new ChooseTimeAdapter(this, timeList, dateBook, staffList, schedulesPending, (v, pos) -> timeBook = timeList.get(pos).getTime());
    }

    public void setNotificationTime(Long mScheduleBody) {
        Long timeDelay = mScheduleBody - TimeUnit.MILLISECONDS.convert(20, TimeUnit.MINUTES);
        Intent myIntent = new Intent(CarWashServiceActivity.this, RemindService.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(CarWashServiceActivity.this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeDelay, pendingIntent);
    }
}