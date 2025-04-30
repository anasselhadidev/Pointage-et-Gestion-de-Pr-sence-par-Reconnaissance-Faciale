package com.example.projets4;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projets4.adapters.TerminalAdapter;
import com.example.projets4.model.Terminal;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemMonitoringActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button btnRefreshMap;
    private ImageView ivCampusMap;
    private TextView tvOverallStatus;
    private RecyclerView recyclerTerminals;
    private TextView tvAlertsList;
    private Button btnRunDiagnostic;
    private Button btnSystemLogs;
    private FloatingActionButton fabAddTerminal;

    private FirebaseFirestore db;
    private List<Terminal> terminalList = new ArrayList<>();
    private TerminalAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_monitoring);

        // Initialisation Firebase
        db = FirebaseFirestore.getInstance();

        // Initialisation des vues
        initViews();

        // Configuration du toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Surveillance Système");
        }

        // Configuration du RecyclerView
        recyclerTerminals.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TerminalAdapter(terminalList, this::showTerminalActionDialog);
        recyclerTerminals.setAdapter(adapter);

        // Chargement des terminaux
        loadTerminals();

        // Configuration des écouteurs
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        btnRefreshMap = findViewById(R.id.btnRefreshMap);
        ivCampusMap = findViewById(R.id.ivCampusMap);
        tvOverallStatus = findViewById(R.id.tvOverallStatus);
        recyclerTerminals = findViewById(R.id.recyclerTerminals);
        tvAlertsList = findViewById(R.id.tvAlertsList);
        btnRunDiagnostic = findViewById(R.id.btnRunDiagnostic);
        btnSystemLogs = findViewById(R.id.btnSystemLogs);
        fabAddTerminal = findViewById(R.id.fabAddTerminal);
    }

    private void loadTerminals() {
        db.collection("terminals")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    terminalList.clear();
                    int online = 0;
                    int offline = 0;
                    int maintenance = 0;
                    StringBuilder alertsBuilder = new StringBuilder();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Terminal terminal = doc.toObject(Terminal.class);
                        terminal.setId(doc.getId());
                        terminalList.add(terminal);

                        // Comptage pour l'état global
                        if (terminal.isOnline()) {
                            online++;
                        } else {
                            offline++;

                            // Ajouter une alerte pour les terminaux hors ligne
                            alertsBuilder.append("• Terminal ").append(terminal.getName())
                                    .append(" hors ligne");

                            if (terminal.getLastSeen() != null && !terminal.getLastSeen().isEmpty()) {
                                alertsBuilder.append(" depuis ").append(terminal.getLastSeen());
                            }

                            alertsBuilder.append("\n");
                        }

                        if (terminal.isInMaintenance()) {
                            maintenance++;
                        }

                        // Vérification de la batterie
                        if (terminal.getBatteryLevel() < 20) {
                            alertsBuilder.append("• Terminal ").append(terminal.getName())
                                    .append(" batterie faible (").append(terminal.getBatteryLevel()).append("%)\n");
                        }
                    }

                    // Mise à jour de l'état global
                    tvOverallStatus.setText(online + " en ligne, " + offline + " hors ligne, " +
                            maintenance + " en maintenance");

                    // Mise à jour de la liste d'alertes
                    if (alertsBuilder.length() > 0) {
                        tvAlertsList.setText(alertsBuilder.toString());
                    } else {
                        tvAlertsList.setText("Aucune alerte active");
                        tvAlertsList.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors du chargement des terminaux: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupListeners() {
        btnRefreshMap.setOnClickListener(v -> {
            Toast.makeText(this, "Actualisation de la carte...", Toast.LENGTH_SHORT).show();
            loadTerminals();
        });

        btnRunDiagnostic.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Diagnostic système")
                    .setMessage("Lancer un diagnostic complet de tous les terminaux ? Cette opération peut prendre plusieurs minutes.")
                    .setPositiveButton("Lancer", (dialog, which) -> {
                        Toast.makeText(this, "Diagnostic en cours...", Toast.LENGTH_SHORT).show();
                        // Simuler un temps de traitement
                        btnRunDiagnostic.setEnabled(false);
                        btnRunDiagnostic.setText("Diagnostic en cours...");

                        new android.os.Handler().postDelayed(() -> {
                            btnRunDiagnostic.setEnabled(true);
                            btnRunDiagnostic.setText("Lancer un diagnostic complet");
                            showDiagnosticResults();
                        }, 3000);
                    })
                    .setNegativeButton("Annuler", null);
            builder.create().show();
        });

        btnSystemLogs.setOnClickListener(v -> {
            Toast.makeText(this, "Chargement des logs système...", Toast.LENGTH_SHORT).show();
            // Afficher les logs système
            showSystemLogs();
        });

        fabAddTerminal.setOnClickListener(v -> {
            showAddTerminalDialog();
        });
    }

    private void showDiagnosticResults() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Résultats du diagnostic")
                .setMessage("Diagnostic terminé avec succès.\n\n" +
                        "Résultats:\n" +
                        "- 19 terminaux fonctionnent correctement\n" +
                        "- 2 terminaux présentent des problèmes de connectivité\n" +
                        "- 1 terminal nécessite une mise à jour du firmware\n" +
                        "- 1 terminal signale une batterie défectueuse")
                .setPositiveButton("Générer rapport", (dialog, which) -> {
                    Toast.makeText(this, "Génération du rapport de diagnostic...", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Fermer", null);
        builder.create().show();
    }

    private void showSystemLogs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Vue défilante pour les logs
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_system_logs, null);
        TextView tvLogs = view.findViewById(R.id.tvSystemLogs);

        // Simuler des logs système
        StringBuilder logs = new StringBuilder();
        logs.append("[2025-04-30 08:15:23] Terminal A101 - Démarrage réussi\n");
        logs.append("[2025-04-30 08:16:05] Terminal B203 - Connexion au réseau établie\n");
        logs.append("[2025-04-30 08:30:12] Terminal C305 - Premier pointage de la journée\n");
        logs.append("[2025-04-30 09:01:45] Terminal A204 - Perte de connexion\n");
        logs.append("[2025-04-30 09:10:12] Terminal A204 - Tentative de reconnexion échouée\n");
        logs.append("[2025-04-30 09:15:45] Terminal D107 - Batterie faible (15%)\n");
        logs.append("[2025-04-30 10:05:33] Terminal B203 - 25 pointages enregistrés\n");
        logs.append("[2025-04-30 10:30:19] Terminal C305 - Synchronisation des données\n");
        logs.append("[2025-04-30 11:45:23] Serveur - Sauvegarde automatique des données\n");
        logs.append("[2025-04-30 12:15:45] Terminal B102 - Mise à jour du firmware lancée\n");
        logs.append("[2025-04-30 12:20:12] Terminal B102 - Mise à jour terminée avec succès\n");

        tvLogs.setText(logs.toString());

        builder.setView(view)
                .setTitle("Logs Système")
                .setPositiveButton("Exporter", (dialog, which) -> {
                    Toast.makeText(this, "Export des logs système...", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Fermer", null);
        builder.create().show();
    }

    private void showAddTerminalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_terminal, null);

        EditText etTerminalName = view.findViewById(R.id.etTerminalName);
        EditText etTerminalLocation = view.findViewById(R.id.etTerminalLocation);
        Spinner spinnerTerminalType = view.findViewById(R.id.spinnerTerminalType);

        builder.setView(view)
                .setTitle("Ajouter un terminal")
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String name = etTerminalName.getText().toString().trim();
                    String location = etTerminalLocation.getText().toString().trim();
                    String type = spinnerTerminalType.getSelectedItem().toString();

                    if (name.isEmpty() || location.isEmpty()) {
                        Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Créer un nouveau terminal
                    Map<String, Object> terminal = new HashMap<>();
                    terminal.put("name", name);
                    terminal.put("location", location);
                    terminal.put("type", type);
                    terminal.put("online", true);
                    terminal.put("batteryLevel", 100);
                    terminal.put("inMaintenance", false);
                    terminal.put("lastSeen", "maintenant");

                    db.collection("terminals")
                            .add(terminal)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "Terminal ajouté avec succès", Toast.LENGTH_SHORT).show();
                                loadTerminals();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erreur lors de l'ajout du terminal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Annuler", null);
        builder.create().show();
    }

    private void showTerminalActionDialog(Terminal terminal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Terminal " + terminal.getName())
                .setItems(new String[]{"Voir les détails", "Diagnostiquer", "Mettre à jour le firmware", "Redémarrer", "Placer en maintenance"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Voir les détails
                            showTerminalDetails(terminal);
                            break;
                        case 1: // Diagnostiquer
                            runTerminalDiagnostic(terminal);
                            break;
                        case 2: // Mettre à jour le firmware
                            updateTerminalFirmware(terminal);
                            break;
                        case 3: // Redémarrer
                            restartTerminal(terminal);
                            break;
                        case 4: // Placer en maintenance
                            toggleMaintenanceMode(terminal);
                            break;
                    }
                });
        builder.create().show();
    }

    private void showTerminalDetails(Terminal terminal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_terminal_details, null);
        TextView tvTerminalName = view.findViewById(R.id.tvTerminalName);
        TextView tvTerminalLocation = view.findViewById(R.id.tvTerminalLocation);
        TextView tvTerminalType = view.findViewById(R.id.tvTerminalType);
        TextView tvTerminalStatus = view.findViewById(R.id.tvTerminalStatus);
        TextView tvBatteryLevel = view.findViewById(R.id.tvBatteryLevel);
        TextView tvFirmwareVersion = view.findViewById(R.id.tvFirmwareVersion);
        TextView tvLastSeen = view.findViewById(R.id.tvLastSeen);

        tvTerminalName.setText("Nom: " + terminal.getName());
        tvTerminalLocation.setText("Emplacement: " + terminal.getLocation());
        tvTerminalType.setText("Type: " + terminal.getType());
        tvTerminalStatus.setText("Statut: " + (terminal.isOnline() ? "En ligne" : "Hors ligne"));
        tvBatteryLevel.setText("Niveau de batterie: " + terminal.getBatteryLevel() + "%");
        tvFirmwareVersion.setText("Version du firmware: " + (terminal.getFirmwareVersion() != null ? terminal.getFirmwareVersion() : "1.0.0"));
        tvLastSeen.setText("Dernière activité: " + (terminal.getLastSeen() != null ? terminal.getLastSeen() : "inconnue"));

        builder.setView(view)
                .setTitle("Détails du terminal")
                .setPositiveButton("Fermer", null);
        builder.create().show();
    }

    private void runTerminalDiagnostic(Terminal terminal) {
        Toast.makeText(this, "Diagnostic du terminal " + terminal.getName() + " en cours...", Toast.LENGTH_SHORT).show();

        // Simuler un diagnostic
        new android.os.Handler().postDelayed(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Résultat du diagnostic")
                    .setMessage("Le terminal " + terminal.getName() + " fonctionne correctement.\n\n" +
                            "- Connexion réseau: OK\n" +
                            "- Niveau de batterie: " + terminal.getBatteryLevel() + "%\n" +
                            "- Stockage: OK\n" +
                            "- Capteur NFC: OK\n")
                    .setPositiveButton("OK", null);
            builder.create().show();
        }, 2000);
    }

    private void updateTerminalFirmware(Terminal terminal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mise à jour du firmware")
                .setMessage("Voulez-vous mettre à jour le firmware du terminal " + terminal.getName() + " ? Cette opération peut prendre plusieurs minutes.")
                .setPositiveButton("Mettre à jour", (dialog, which) -> {
                    Toast.makeText(this, "Mise à jour du firmware en cours...", Toast.LENGTH_SHORT).show();

                    // Simuler une mise à jour
                    new android.os.Handler().postDelayed(() -> {
                        Toast.makeText(this, "Mise à jour terminée avec succès", Toast.LENGTH_SHORT).show();

                        // Mettre à jour la version du firmware
                        db.collection("terminals").document(terminal.getId())
                                .update("firmwareVersion", "2.1.0")
                                .addOnSuccessListener(aVoid -> {
                                    // Recharger les terminaux
                                    loadTerminals();
                                });
                    }, 3000);
                })
                .setNegativeButton("Annuler", null);
        builder.create().show();
    }

    private void restartTerminal(Terminal terminal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Redémarrer le terminal")
                .setMessage("Voulez-vous redémarrer le terminal " + terminal.getName() + " ?")
                .setPositiveButton("Redémarrer", (dialog, which) -> {
                    Toast.makeText(this, "Redémarrage du terminal en cours...", Toast.LENGTH_SHORT).show();

                    // Simuler un redémarrage
                    db.collection("terminals").document(terminal.getId())
                            .update("online", false)
                            .addOnSuccessListener(aVoid -> {
                                loadTerminals();

                                // Simuler le retour en ligne après le redémarrage
                                new android.os.Handler().postDelayed(() -> {
                                    db.collection("terminals").document(terminal.getId())
                                            .update("online", true)
                                            .addOnSuccessListener(aVoid2 -> {
                                                loadTerminals();
                                                Toast.makeText(this, "Redémarrage terminé", Toast.LENGTH_SHORT).show();
                                            });
                                }, 5000);
                            });
                })
                .setNegativeButton("Annuler", null);
        builder.create().show();
    }

    private void toggleMaintenanceMode(Terminal terminal) {
        boolean newMode = !terminal.isInMaintenance();
        String message = newMode ?
                "Placer le terminal " + terminal.getName() + " en mode maintenance ?" :
                "Sortir le terminal " + terminal.getName() + " du mode maintenance ?";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mode maintenance")
                .setMessage(message)
                .setPositiveButton("Confirmer", (dialog, which) -> {
                    db.collection("terminals").document(terminal.getId())
                            .update("inMaintenance", newMode)
                            .addOnSuccessListener(aVoid -> {
                                String toast = newMode ?
                                        "Terminal placé en mode maintenance" :
                                        "Terminal sorti du mode maintenance";
                                Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
                                loadTerminals();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Annuler", null);
        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}