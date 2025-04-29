package com.example.projets4.utile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.R;
import com.example.projets4.model.DailySchedule;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.VH> {
    private final List<DailySchedule> data;

    public ScheduleAdapter(List<DailySchedule> data) {
        this.data = data;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        DailySchedule d = data.get(position);
        holder.textDay.setText(d.getDayName());
        holder.textMorning.setText(d.getMorningSubject());
        holder.textEvening.setText(d.getEveningSubject());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView textDay, textMorning, textEvening;
        VH(@NonNull View v) {
            super(v);
            textDay     = v.findViewById(R.id.textDay);
            textMorning = v.findViewById(R.id.textMorning);
            textEvening = v.findViewById(R.id.textEvening);
        }
    }
}
