package com.example.projets4;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projets4.model.Course;
import com.example.projets4.utile.CoursesAdapter;
import java.util.ArrayList;
import java.util.List;

public class TeacherHomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CoursesAdapter adapter;
    private List<Course> courseList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.coursesList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize course list
        courseList = new ArrayList<>();

        // Add static courses
        courseList.add(new Course("Mathématiques Avancées", "📅 Mardi 29 Avril, 11:00 - 12:30", "📍 Salle 203", 75, "Terminé"));
        courseList.add(new Course("Physique Fondamentale", "📅 Mercredi 30 Avril, 09:00 - 10:30", "📍 Salle 101", 60, "En cours"));
        courseList.add(new Course("Chimie Organique", "📅 Jeudi 1 Mai, 14:00 - 15:30", "📍 Salle 304", 0, "À venir"));

        // Set adapter
        adapter = new CoursesAdapter(courseList);
        recyclerView.setAdapter(adapter);
    }
}
