package com.example.projets4.utile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.R;
import com.example.projets4.model.Course;

import java.util.List;

public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.CourseViewHolder> {

    private List<Course> courseList;

    public CoursesAdapter(List<Course> courseList) {
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.title.setText(course.getTitle());
        holder.schedule.setText(course.getSchedule());
        holder.room.setText(course.getRoom());
        holder.progress.setProgress(course.getProgress());
        holder.progressText.setText(course.getProgress() + "%");
        holder.status.setText(course.getStatus());
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView title, schedule, room, progressText, status;
        ProgressBar progress;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.courseTitle);
            schedule = itemView.findViewById(R.id.courseSchedule);
            room = itemView.findViewById(R.id.courseRoom);
            progress = itemView.findViewById(R.id.courseProgress);
            progressText = itemView.findViewById(R.id.courseProgressText);
            status = itemView.findViewById(R.id.statusBadge);
        }
    }
}
