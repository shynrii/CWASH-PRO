package com.example.cwash_pro.ui.staff.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cwash_pro.R;
import com.example.cwash_pro.adapters.ScheduleAdapter;
import com.example.cwash_pro.apis.ApiService;
import com.example.cwash_pro.apis.RetrofitClient;
import com.example.cwash_pro.models.Schedule;
import com.example.cwash_pro.models.ServerResponse;
import com.example.cwash_pro.ui.dialog.CustomDialogProgress;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PendingScheduleFragment extends Fragment {
    private RecyclerView rvPendingSchedule;
    private List<Schedule> scheduleList;
    ScheduleAdapter scheduleAdapter;
    Button btnConfirm, btnCancel;
    FragmentManager fragmentManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_schedule, container, false);
        initView(view);
        scheduleList = new ArrayList<>();
        fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        final CustomDialogProgress dialog = new CustomDialogProgress(getContext());
        dialog.show();
        RetrofitClient.getInstance().create(ApiService.class).getAllSchedule().enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response) {
                List<Schedule> schedules = null;
                if (response.body() != null) {
                    schedules = response.body().schedules;
                }
                if (response.body() != null) {
                    if (response.body().success) {
                        for (int i = 0; i < schedules.size(); i++) {
                            if (schedules.get(i).getStatus().equals("Pending")) {
                                scheduleList.add(schedules.get(i));
                            }
                        }
                        scheduleAdapter = new ScheduleAdapter(scheduleList, getActivity(), (v, pos) -> {
                            if (v.getId() == R.id.btnConfirm) {
                                final CustomDialogProgress dialogLoadBook = new CustomDialogProgress(getContext());
                                dialogLoadBook.show();
                                RetrofitClient.getInstance().create(ApiService.class).confirm(scheduleList.get(pos).getId(), "Confirmed").enqueue(new Callback<ServerResponse>() {
                                    @Override
                                    public void onResponse(@NonNull Call<ServerResponse> call1, @NonNull Response<ServerResponse> response1) {
                                        if (response1.body() != null && response1.body().success) {
                                            new Handler().postDelayed(() -> {
                                                scheduleList.remove(pos);
                                                scheduleAdapter.notifyDataSetChanged();
                                                dialog.dismiss();
                                            }, 2000);
                                            Toast.makeText(getActivity(), " Xác nhận thành công", Toast.LENGTH_SHORT).show();
                                            dialogLoadBook.dismiss();
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<ServerResponse> call13, @NonNull Throwable t) {
                                        Log.d("onFailure: ", t.getMessage());
                                    }
                                });
                            } else if (v.getId() == R.id.btnCancel) {
                                AlertDialog builder = new AlertDialog.Builder(getContext()).create();
                                View dialog = LayoutInflater.from(getContext()).inflate(R.layout.dialog_cancel_schedule, null);
                                EditText edtNote = dialog.findViewById(R.id.edtNote);
                                Button btnCancel = dialog.findViewById(R.id.btnOKCancel);
                                Button btnNone = dialog.findViewById(R.id.btnNone);
                                btnNone.setOnClickListener(view1 -> {
                                    builder.dismiss();
                                });
                                btnCancel.setOnClickListener(v1 -> {
                                    final CustomDialogProgress dialogCancel = new CustomDialogProgress(getContext());
                                    dialogCancel.show();
                                    RetrofitClient.getInstance().create(ApiService.class).cancel(scheduleList.get(pos).getId(), edtNote.getText().toString(), "Canceled").enqueue(new Callback<ServerResponse>() {
                                        @Override
                                        public void onResponse(@NonNull Call<ServerResponse> call2, @NonNull Response<ServerResponse> response2) {
                                            if (response2.body() != null && response2.body().success) {
                                                final CustomDialogProgress dialog = new CustomDialogProgress(getContext());
                                                dialog.show();
                                                scheduleList.remove(pos);
                                                scheduleAdapter.notifyDataSetChanged();
                                                dialog.dismiss();
                                                Toast.makeText(getActivity(), "Hủy thành công", Toast.LENGTH_SHORT).show();
                                                setCurrentFragment(new CancelledScheduleFragment());
                                                builder.dismiss();
                                            }
                                            dialogCancel.dismiss();
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<ServerResponse> call12, @NonNull Throwable t) {
                                            Log.d("onFailure: ", t.getMessage());
                                        }
                                    });
                                });
                                builder.setView(dialog);
                                builder.show();
                            }
                        });
                        rvPendingSchedule.setLayoutManager(new LinearLayoutManager(getActivity()));
                        rvPendingSchedule.setAdapter(scheduleAdapter);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getActivity(), "Lỗi " + response.body().message, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
                Log.e("onFailureStaffMain: ", t.getMessage());
            }
        });
        return view;
    }

    private void initView(View view) {
        rvPendingSchedule = view.findViewById(R.id.rvPendingSchedule);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        btnCancel = view.findViewById(R.id.btnCancel);
    }

    private void setCurrentFragment(Fragment fragment) {
        fragmentManager.beginTransaction().replace(R.id.flFragment2, fragment).commit();
    }
}