package com.example.cwash_pro.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cwash_pro.R;
import com.example.cwash_pro.myinterface.ItemClick;
import com.example.cwash_pro.models.Schedule;
import com.github.nikartm.button.FitButton;

import java.util.ArrayList;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_PENDING = 1;
    private static final int VIEW_TYPE_PROCESSING = 2;
    private static final int VIEW_TYPE_COMPLETED = 3;
    private static final int VIEW_TYPE_CANCELED = 4;
    private final List<Schedule> scheduleList;
    private final Context context;
    static ItemClick itemClick;

    public ScheduleAdapter(List<Schedule> scheduleList, Context context, ItemClick itemClick) {
        this.scheduleList = scheduleList;
        this.context = context;
        ScheduleAdapter.itemClick = itemClick;

    }

    @Override
    public int getItemViewType(int position) {
        if (scheduleList.get(position).getStatus().equals("Pending")) {
            return VIEW_TYPE_PENDING;
        } else if (scheduleList.get(position).getStatus().equals("Confirmed")) {
            return VIEW_TYPE_PROCESSING;
        } else if (scheduleList.get(position).getStatus().equals("Completed")) {
            return VIEW_TYPE_COMPLETED;
        } else {
            return VIEW_TYPE_CANCELED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_PROCESSING:
                return new ProcessingHolder(inflater.inflate(R.layout.item_processing_schedule, parent, false));
            case VIEW_TYPE_COMPLETED:
                return new CompletedHolder(inflater.inflate(R.layout.item_completed_schedule, parent, false));
            case VIEW_TYPE_CANCELED:
                return new CancelHolder(inflater.inflate(R.layout.item_cancel_schedule, parent, false));
            default:
                return new PendingHolder(inflater.inflate(R.layout.item_pending_schedule, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PendingHolder) {
            PendingHolder pendingHolder = (PendingHolder) holder;
            pendingHolder.tvTime.setText(scheduleList.get(position).getTimeBook());
            pendingHolder.tvName.setText(scheduleList.get(position).getUser().getFullName());
            pendingHolder.tvVehicle.setText(scheduleList.get(position).getVehicle().getBrand());
            List<String> stringList = new ArrayList<>();
            for (int i = 0; i < scheduleList.get(position).getServices().size(); i++) {
                stringList.add(scheduleList.get(position).getServices().get(i).getName());

            }
            pendingHolder.tvService.setText(String.valueOf(stringList));
        } else if (holder instanceof ProcessingHolder) {
            ProcessingHolder processingHolder = (ProcessingHolder) holder;
            processingHolder.tvTime.setText(scheduleList.get(position).getTimeBook());
            processingHolder.tvName.setText(scheduleList.get(position).getUser().getFullName());
            processingHolder.tvVehicle.setText(scheduleList.get(position).getVehicle().getBrand());
            if (scheduleList.get(position).getStatus().equals("Confirmed") && !scheduleList.get(position).getVehicleStatus()) {
                processingHolder.tvStatusVehicle.setText("Khách hàng chưa lấy xe");
                processingHolder.tvStatusVehicle.setTextColor(Color.RED);
            }else {
                processingHolder.tvStatusVehicle.setText("Khách hàng đã lấy xe");
                processingHolder.tvStatusVehicle.setTextColor(Color.GREEN);
            }
            List<String> stringList = new ArrayList<>();
            for (int i = 0; i < scheduleList.get(position).getServices().size(); i++) {
                stringList.add(scheduleList.get(position).getServices().get(i).getName());
            }
            processingHolder.tvService.setText(String.valueOf(stringList));
        } else if (holder instanceof CompletedHolder) {
            CompletedHolder completedHolder = (CompletedHolder) holder;
            completedHolder.tvTime.setText(scheduleList.get(position).getTimeBook());
            completedHolder.tvName.setText(scheduleList.get(position).getUser().getFullName());
            completedHolder.tvVehicle.setText(scheduleList.get(position).getVehicle().getBrand());
            List<String> stringList = new ArrayList<>();
            for (int i = 0; i < scheduleList.get(position).getServices().size(); i++) {
                stringList.add(scheduleList.get(position).getServices().get(i).getName());
            }
            completedHolder.tvService.setText(String.valueOf(stringList));
        } else {
            CancelHolder cancelHolder = (CancelHolder) holder;
            cancelHolder.tvTime.setText(scheduleList.get(position).getTimeBook());
            cancelHolder.tvName.setText(scheduleList.get(position).getUser().getFullName());
            cancelHolder.tvVehicle.setText(scheduleList.get(position).getVehicle().getBrand());
            List<String> stringList = new ArrayList<>();
            for (int i = 0; i < scheduleList.get(position).getServices().size(); i++) {
                stringList.add(scheduleList.get(position).getServices().get(i).getName());
            }
            cancelHolder.tvService.setText(String.valueOf(stringList));
        }
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }


    public static class PendingHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvTime;
        TextView tvService;
        TextView tvVehicle;
        Button btnConfirm, btnCancel;

        public PendingHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvService = itemView.findViewById(R.id.tvService);
            tvVehicle = itemView.findViewById(R.id.tvVehicle);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
            btnConfirm.setOnClickListener(v -> itemClick.setOnItemClick(v, getAdapterPosition()));
            btnCancel.setOnClickListener(v -> itemClick.setOnItemClick(v, getAdapterPosition()));
        }
    }

    public static class ProcessingHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvTime;
        TextView tvService;
        TextView tvVehicle;
        TextView tvStatusVehicle;
        FitButton btnComplete;

        public ProcessingHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvService = itemView.findViewById(R.id.tvService);
            tvVehicle = itemView.findViewById(R.id.tvVehicle);
            tvStatusVehicle = itemView.findViewById(R.id.tvStatusVehicles);
            btnComplete = itemView.findViewById(R.id.btnComplete);
            btnComplete.setOnClickListener(v -> itemClick.setOnItemClick(v, getAdapterPosition()));
        }
    }

    public static class CompletedHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvTime;
        TextView tvService;
        TextView tvVehicle;
        FitButton btnViewDetails;

        public CompletedHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvService = itemView.findViewById(R.id.tvService);
            tvVehicle = itemView.findViewById(R.id.tvVehicle);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnViewDetails.setOnClickListener(v -> itemClick.setOnItemClick(v, getAdapterPosition()));
        }
    }

    public static class CancelHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvTime;
        TextView tvService;
        TextView tvVehicle;
        FitButton btnViewDetails;

        public CancelHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvService = itemView.findViewById(R.id.tvService);
            tvVehicle = itemView.findViewById(R.id.tvVehicle);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnViewDetails.setOnClickListener(v -> itemClick.setOnItemClick(v, getAdapterPosition()));
        }
    }
}


