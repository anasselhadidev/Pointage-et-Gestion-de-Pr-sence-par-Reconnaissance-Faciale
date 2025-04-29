package com.example.projets4;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projets4.model.DailySchedule;

import java.util.ArrayList;
import java.util.List;

public class ConsulterEmploiActivity extends AppCompatActivity {

    private TableLayout table;
    private List<DailySchedule> scheduleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulter_emploi_temps);

        table = findViewById(R.id.tableSchedule);

        // Prépare l’emploi du temps fixe
        prepareFixedSchedule();

        // Ajoute chaque jour sous forme de ligne de tableau
        for (DailySchedule d : scheduleList) {
            TableRow row = (TableRow) LayoutInflater.from(this)
                    .inflate(R.layout.row_schedule, table, false);

            TextView tvDay     = row.findViewById(R.id.textDay);
            TextView tvMorning = row.findViewById(R.id.textMorning);
            TextView tvEvening = row.findViewById(R.id.textEvening);

            tvDay.setText(d.getDayName());
            tvMorning.setText(d.getMorningSubject());
            tvEvening.setText(d.getEveningSubject());

            table.addView(row);
        }
    }

    private void prepareFixedSchedule() {
        scheduleList.add(new DailySchedule("Lundi",    "Mathématiques", "Physique"));
        scheduleList.add(new DailySchedule("Mardi",    "Informatique",  "Anglais"));
        scheduleList.add(new DailySchedule("Mercredi", "Chimie",        "Français"));
        scheduleList.add(new DailySchedule("Jeudi",    "Biologie",      "Histoire"));
        scheduleList.add(new DailySchedule("Vendredi", "Géographie",    "EPS"));
    }
}
