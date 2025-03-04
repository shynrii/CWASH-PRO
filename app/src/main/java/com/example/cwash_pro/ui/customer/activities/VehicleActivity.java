package com.example.cwash_pro.ui.customer.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cwash_pro.R;
import com.example.cwash_pro.adapters.VehicleAdapter;
import com.example.cwash_pro.apis.ApiService;
import com.example.cwash_pro.apis.RetrofitClient;
import com.example.cwash_pro.models.ServerResponse;
import com.example.cwash_pro.models.Vehicle;
import com.example.cwash_pro.myinterface.ItemClick;
import com.example.cwash_pro.ui.dialog.CustomDialogProgress;
import com.reginald.editspinner.EditSpinner;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleActivity extends AppCompatActivity {
    RecyclerView rvVehicle;
    Button imgAddVehicle;
    ImageView imgBack;
    VehicleAdapter adapter;
    List<Vehicle> vehicles = new ArrayList<>();
    String type = "";
    String brand = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle);

        imgBack = findViewById(R.id.imgBack);
        imgBack.setOnClickListener(v -> onBackPressed());

        rvVehicle = findViewById(R.id.rvVehicle);
        imgAddVehicle = findViewById(R.id.imgAddVehicle);
        final CustomDialogProgress dialogLoad = new CustomDialogProgress(this);
        dialogLoad.show();
        RetrofitClient.getInstance().create(ApiService.class).getVehicle().enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response) {
                if (response.code() == 200) {
                    vehicles = response.body().vehicles;
                    adapter = new VehicleAdapter(VehicleActivity.this, vehicles, new ItemClick() {
                        @Override
                        public void setOnItemClick(View v, int pos) {
                            if (v.getId() == R.id.imgDelete) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(VehicleActivity.this);
                                builder.setTitle("Bạn có chắc chắn muốn xoá phương tiên này không? ");
                                builder.setPositiveButton("Xoá", (dialog, which) -> {
                                    final CustomDialogProgress dialogLoad = new CustomDialogProgress(VehicleActivity.this);
                                    dialogLoad.show();
                                    RetrofitClient.getInstance().create(ApiService.class).deleteVehicle(vehicles.get(pos).getId()).enqueue(new Callback<ServerResponse>() {
                                        @Override
                                        public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response) {
                                            if (response.body().success) {
                                                Toast.makeText(VehicleActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
                                                Intent intent = getIntent();
                                                finish();
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(VehicleActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
                                            }
                                            dialogLoad.dismiss();
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
                                        }
                                    });
                                });
                                builder.setNegativeButton("Không", (dialog, which) -> {
                                    dialog.dismiss();
                                });
                                AlertDialog dialog = builder.create();
                                dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog);
                                dialog.show();
                            } else if (v.getId() == R.id.imgUpdate) {
                                AlertDialog builder = new AlertDialog.Builder(VehicleActivity.this).create();
                                View dialog = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_update_vehicle, null);
                                Button btnUpdate = dialog.findViewById(R.id.btnUpdateVehicle);
                                EditText edtName = dialog.findViewById(R.id.edtNameOfVehicle);
                                EditSpinner edtBrand = dialog.findViewById(R.id.edtBrand);
                                EditText edtColor = dialog.findViewById(R.id.edtColorOfVehicle);
                                EditText edtLicense = dialog.findViewById(R.id.edtLicense);
                                Spinner spnType = dialog.findViewById(R.id.spnType);
                                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(VehicleActivity.this,
                                        R.array.type_of_vehicle, android.R.layout.simple_spinner_dropdown_item);
                                ArrayAdapter<String> adapterBrandMoto = new ArrayAdapter<>(VehicleActivity.this, android.R.layout.simple_spinner_dropdown_item,
                                        getResources().getStringArray(R.array.brand_of_vehicle_moto));
                                ArrayAdapter<String> adapterBrandCar = new ArrayAdapter<>(VehicleActivity.this, android.R.layout.simple_spinner_dropdown_item,
                                        getResources().getStringArray(R.array.brand_of_vehicle_car));
                                edtBrand.setAdapter(adapterBrandMoto);
                                adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
                                spnType.setAdapter(adapter);
                                spnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        if (spnType.getSelectedItem().toString().equals("Xe máy")) {
                                            type = "Motorcycle";
                                            edtBrand.setAdapter(adapterBrandMoto);

                                        } else if (spnType.getSelectedItem().toString().equals("Ô tô")) {
                                            type = "Car";
                                            edtBrand.setAdapter(adapterBrandCar);

                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });
                                edtName.setText(vehicles.get(pos).getName());
                                edtBrand.setText(vehicles.get(pos).getBrand());
                                edtColor.setText(vehicles.get(pos).getColor());
                                edtLicense.setText(vehicles.get(pos).getLicense());
                                Button btnCancel = dialog.findViewById(R.id.btnCancel);
                                btnCancel.setOnClickListener(v1 -> builder.dismiss());
                                btnUpdate.setOnClickListener(v1 -> {
                                    final CustomDialogProgress dialogLoadAdd = new CustomDialogProgress(VehicleActivity.this);
                                    dialogLoadAdd.show();
                                    RetrofitClient.getInstance().create(ApiService.class).updateVehicle(vehicles.get(pos).getId(),
                                            edtName.getText().toString().trim(),
                                            type,
                                            edtLicense.getText().toString().trim(),
                                            edtColor.getText().toString().trim(),
                                            edtBrand.getText().toString().trim())
                                            .enqueue(new Callback<ServerResponse>() {
                                                @Override
                                                public void onResponse(@NonNull Call<ServerResponse> call1, @NonNull Response<ServerResponse> response1) {
                                                    if (response1.body().success) {
                                                        Toast.makeText(VehicleActivity.this, response1.body().message, Toast.LENGTH_SHORT).show();
                                                        finish();
                                                        startActivity(getIntent());
                                                    } else {
                                                        Toast.makeText(VehicleActivity.this, response1.body().message, Toast.LENGTH_SHORT).show();
                                                    }
                                                    dialogLoad.dismiss();
                                                }

                                                @Override
                                                public void onFailure(@NonNull Call<ServerResponse> call1, @NonNull Throwable t) {
                                                }
                                            });
                                });

                                builder.setView(dialog);
                                builder.getWindow().setBackgroundDrawableResource(R.drawable.dialog);
                                builder.show();
                            }
                        }
                    });
                    rvVehicle.setLayoutManager(new LinearLayoutManager(VehicleActivity.this, LinearLayoutManager.VERTICAL, false));
                    rvVehicle.setAdapter(adapter);
                    dialogLoad.dismiss();
                } else {
                    Toast.makeText(VehicleActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
                Log.d("huhu", t.getMessage());
            }
        });
        imgAddVehicle.setOnClickListener(v -> {
            AlertDialog builder = new AlertDialog.Builder(this).create();
            View inflate = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_add_vehicle, null);
            EditText edtNameOfVehicle = inflate.findViewById(R.id.edtNameOfVehicle);
            EditText edtColorOfVehicle = inflate.findViewById(R.id.edtColorOfVehicle);
            EditText edtLicense = inflate.findViewById(R.id.edtLicense);
            EditSpinner spnBrand = inflate.findViewById(R.id.editSpinnerBrand);
            Spinner spnType = inflate.findViewById(R.id.spnType);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.type_of_vehicle, android.R.layout.simple_spinner_dropdown_item);
            ArrayAdapter<String> adapterBrandMoto = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                    getResources().getStringArray(R.array.brand_of_vehicle_moto));
            ArrayAdapter<String> adapterBrandCar = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                    getResources().getStringArray(R.array.brand_of_vehicle_car));

            adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
            spnBrand.setAdapter(adapterBrandMoto);
            spnType.setAdapter(adapter);
            Button btnAddVehicle = inflate.findViewById(R.id.btnAddVehicle);
            Button btnCancel = inflate.findViewById(R.id.btnCancel);
            spnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (spnType.getSelectedItem().toString().equals("Xe máy")) {
                        type = "Motorcycle";
                        spnBrand.setAdapter(adapterBrandMoto);
                    } else if (spnType.getSelectedItem().toString().equals("Ô tô")) {
                        type = "Car";
                        spnBrand.setAdapter(adapterBrandCar);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            btnAddVehicle.setOnClickListener(view -> {

                if (!edtNameOfVehicle.getText().toString().isEmpty() && !edtLicense.getText().toString().isEmpty() && !edtColorOfVehicle.getText().toString().isEmpty() && !spnBrand.getText().toString().isEmpty()) {
                    final CustomDialogProgress dialogLoadAdd = new CustomDialogProgress(this);
                    dialogLoadAdd.show();
                    RetrofitClient.getInstance().create(ApiService.class).addVehicle(edtNameOfVehicle.getText().toString(), type
                            , edtLicense.getText().toString(), edtColorOfVehicle.getText().toString(), spnBrand.getText().toString()).enqueue(new Callback<ServerResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response) {
                            if (response.code() == 200) {
                                if (response.body() != null) {
                                    Toast.makeText(VehicleActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
                                }
                                finish();
                                overridePendingTransition(0, 0);
                                startActivity(getIntent());
                                overridePendingTransition(0, 0);
                            }
                            dialogLoadAdd.dismiss();
                            builder.dismiss();
                        }

                        @Override
                        public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
                            Log.d("Error to add vehicle", t.getMessage());
                            dialogLoadAdd.dismiss();
                        }
                    });
                } else {
                    Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                }
            });
            btnCancel.setOnClickListener(view -> builder.dismiss());
            builder.setView(inflate);
            builder.getWindow().setBackgroundDrawableResource(R.drawable.dialog);
            builder.show();
        });
    }
}