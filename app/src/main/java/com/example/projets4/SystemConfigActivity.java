package com.example.projets4;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemConfigActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private SeekBar seekBarRetard;
    private TextView tvRetardValue;
    private SeekBar seekBarAbsence;
    private TextView tvAbsenceValue;
    private CheckBox checkboxEnforceDouble;
    private CheckBox checkboxAllowJustification;
    private CheckBox checkboxAutoNotify;
    private CheckBox checkboxNotifyStudents;
    private CheckBox checkboxNotifyTeachers;
    private CheckBox checkboxNotifyAdmins;
    private CheckBox checkboxNotifyApp;
    private CheckBox checkboxNotifyEmail;
    private CheckBox checkboxNotifySMS;
    private Spinner spinnerPasswordPolicy;
    private SeekBar seekBarSessionDuration;
    private TextView tvSessionDurationValue;
    private CheckBox checkboxTwoFactor;
    private Spinner spinnerBackupFrequency;
    private TextView tvLastBackup;
    private Button btnBackupNow;
    private Button btnCleanDatabase;
    private Button btnRestoreBackup;
    private Button btnSaveConfig;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_config);

        // Initialisation Firebase
        db = FirebaseFirestore.getInstance();

        // Initialisation des vues
        initViews();

        // Configuration du toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Configuration Système");
        }

        // Configuration des spinners
        setupSpinners();

        // Configuration des seekbars
        setupSeekBars();

        // Chargement des paramètres actuels
        loadCurrentSettings();

        // Configuration des écouteurs
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        seekBarRetard = findViewById(R.id.seekBarRetard);
        tvRetardValue = findViewById(R.id.tvRetardValue);
        seekBarAbsence = findViewById(R.id.seekBarAbsence);
        tvAbsenceValue = findViewById(R.id.tvAbsenceValue);
        checkboxEnforceDouble = findViewById(R.id.checkboxEnforceDouble);
        checkboxAllowJustification = findViewById(R.id.checkboxAllowJustification);
        checkboxAutoNotify = findViewById(R.id.checkboxAutoNotify);
        checkboxNotifyStudents = findViewById(R.id.checkboxNotifyStudents);
        checkboxNotifyTeachers = findViewById(R.id.checkboxNotifyTeachers);
        checkboxNotifyAdmins = findViewById(R.id.checkboxNotifyAdmins);
        checkboxNotifyApp = findViewById(R.id.checkboxNotifyApp);
        checkboxNotifyEmail = findViewById(R.id.checkboxNotifyEmail);
        checkboxNotifySMS = findViewById(R.id.checkboxNotifySMS);
        spinnerPasswordPolicy = findViewById(R.id.spinnerPasswordPolicy);
        seekBarSessionDuration = findViewById(R.id.seekBarSessionDuration);
        tvSessionDurationValue = findViewById(R.id.tvSessionDurationValue);
        checkboxTwoFactor = findViewById(R.id.checkboxTwoFactor);
        spinnerBackupFrequency = findViewById(R.id.spinnerBackupFrequency);
        tvLastBackup = findViewById(R.id.tvLastBackup);
        btnBackupNow = findViewById(R.id.btnBackupNow);
        btnCleanDatabase = findViewById(R.id.btnCleanDatabase);
        btnRestoreBackup = findViewById(R.id.btnRestoreBackup);
        btnSaveConfig = findViewById(R.id.btnSaveConfig);
    }

    private void setupSpinners() {
        // Configuration du spinner de politique de mot de passe
        List<String> passwordPolicies = Arrays.asList(
                "Basique (6 caractères minimum)",
                "Standard (8 caractères, majuscules, chiffres)",
                "Renforcé (10 caractères, symboles, chiffres)",
                "Très sécurisé (12 caractères, symboles, chiffres, majuscules)"
        );
        ArrayAdapter<String> passwordAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, passwordPolicies);
        passwordAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPasswordPolicy.setAdapter(passwordAdapter);

        // Configuration du spinner de fréquence de sauvegarde
        List<String> backupFrequencies = Arrays.asList(
                "Quotidienne",
                "Hebdomadaire",
                "Bihebdomadaire",
                "Mensuelle"
        );
        ArrayAdapter<String> backupAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, backupFrequencies);
        backupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBackupFrequency.setAdapter(backupAdapter);
    }

    private void setupSeekBars() {
        // Configuration du SeekBar de seuil de retard
        seekBarRetard.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvRetardValue.setText(progress + " min");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Configuration du SeekBar d'absence automatique
        seekBarAbsence.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvAbsenceValue.setText(progress + " min");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Configuration du SeekBar de durée de session
        seekBarSessionDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvSessionDurationValue.setText(progress + " min");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void loadCurrentSettings() {
        // Charger les paramètres actuels depuis Firebase
        db.collection("system_settings").document("attendance_rules")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Paramètres des règles de présence
                        Integer delayThreshold = documentSnapshot.getLong("delayThreshold") != null ?
                                documentSnapshot.getLong("delayThreshold").intValue() : 15;
                        seekBarRetard.setProgress(delayThreshold);
                        tvRetardValue.setText(delayThreshold + " min");

                        Integer absenceThreshold = documentSnapshot.getLong("absenceThreshold") != null ?
                                documentSnapshot.getLong("absenceThreshold").intValue() : 30;
                        seekBarAbsence.setProgress(absenceThreshold);
                        tvAbsenceValue.setText(absenceThreshold + " min");

                        Boolean enforceDouble = documentSnapshot.getBoolean("enforceDouble");
                        if (enforceDouble != null) checkboxEnforceDouble.setChecked(enforceDouble);

                        Boolean allowJustification = documentSnapshot.getBoolean("allowJustification");
                        if (allowJustification != null) checkboxAllowJustification.setChecked(allowJustification);

                        Boolean autoNotify = documentSnapshot.getBoolean("autoNotify");
                        if (autoNotify != null) checkboxAutoNotify.setChecked(autoNotify);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors du chargement des paramètres: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Charger les paramètres de notification
        db.collection("system_settings").document("notification_settings")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean notifyStudents = documentSnapshot.getBoolean("notifyStudents");
                        if (notifyStudents != null) checkboxNotifyStudents.setChecked(notifyStudents);

                        Boolean notifyTeachers = documentSnapshot.getBoolean("notifyTeachers");
                        if (notifyTeachers != null) checkboxNotifyTeachers.setChecked(notifyTeachers);

                        Boolean notifyAdmins = documentSnapshot.getBoolean("notifyAdmins");
                        if (notifyAdmins != null) checkboxNotifyAdmins.setChecked(notifyAdmins);

                        Boolean notifyApp = documentSnapshot.getBoolean("notifyApp");
                        if (notifyApp != null) checkboxNotifyApp.setChecked(notifyApp);

                        Boolean notifyEmail = documentSnapshot.getBoolean("notifyEmail");
                        if (notifyEmail != null) checkboxNotifyEmail.setChecked(notifyEmail);

                        Boolean notifySMS = documentSnapshot.getBoolean("notifySMS");
                        if (notifySMS != null) checkboxNotifySMS.setChecked(notifySMS);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors du chargement des paramètres: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Charger les paramètres de sécurité
        db.collection("system_settings").document("security_settings")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String passwordPolicy = documentSnapshot.getString("passwordPolicy");
                        if (passwordPolicy != null) {
                            int policyIndex = 0;
                            switch (passwordPolicy) {
                                case "basic":
                                    policyIndex = 0;
                                    break;
                                case "standard":
                                    policyIndex = 1;
                                    break;
                                case "enhanced":
                                    policyIndex = 2;
                                    break;
                                case "very_secure":
                                    policyIndex = 3;
                                    break;
                            }
                            spinnerPasswordPolicy.setSelection(policyIndex);
                        }

                        Integer sessionDuration = documentSnapshot.getLong("sessionDuration") != null ?
                                documentSnapshot.getLong("sessionDuration").intValue() : 60;
                        seekBarSessionDuration.setProgress(sessionDuration);
                        tvSessionDurationValue.setText(sessionDuration + " min");

                        Boolean twoFactor = documentSnapshot.getBoolean("twoFactor");
                        if (twoFactor != null) checkboxTwoFactor.setChecked(twoFactor);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors du chargement des paramètres: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Charger les paramètres de sauvegarde
        db.collection("system_settings").document("backup_settings")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String backupFrequency = documentSnapshot.getString("backupFrequency");
                        if (backupFrequency != null) {
                            int frequencyIndex = 0;
                            switch (backupFrequency) {
                                case "daily":
                                    frequencyIndex = 0;
                                    break;
                                case "weekly":
                                    frequencyIndex = 1;
                                    break;
                                case "biweekly":
                                    frequencyIndex = 2;
                                    break;
                                case "monthly":
                                    frequencyIndex = 3;
                                    break;
                            }
                            spinnerBackupFrequency.setSelection(frequencyIndex);
                        }

                        String lastBackup = documentSnapshot.getString("lastBackup");
                        if (lastBackup != null) tvLastBackup.setText(lastBackup);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors du chargement des paramètres: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupListeners() {
        btnSaveConfig.setOnClickListener(v -> {
            saveSettings();
        });

        btnBackupNow.setOnClickListener(v -> {
            showBackupDialog();
        });

        btnCleanDatabase.setOnClickListener(v -> {
            showCleanDatabaseDialog();
        });

        btnRestoreBackup.setOnClickListener(v -> {
            showRestoreDialog();
        });
    }

    private void saveSettings() {
        // Enregistrer les paramètres des règles de présence
        Map<String, Object> attendanceRules = new HashMap<>();
        attendanceRules.put("delayThreshold", seekBarRetard.getProgress());
        attendanceRules.put("absenceThreshold", seekBarAbsence.getProgress());
        attendanceRules.put("enforceDouble", checkboxEnforceDouble.isChecked());
        attendanceRules.put("allowJustification", checkboxAllowJustification.isChecked());
        attendanceRules.put("autoNotify", checkboxAutoNotify.isChecked());

        db.collection("system_settings").document("attendance_rules")
                .set(attendanceRules)
                .addOnSuccessListener(aVoid -> {
                    // Sauvegarde réussie
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de l'enregistrement des paramètres: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Enregistrer les paramètres de notification
        Map<String, Object> notificationSettings = new HashMap<>();
        notificationSettings.put("notifyStudents", checkboxNotifyStudents.isChecked());
        notificationSettings.put("notifyTeachers", checkboxNotifyTeachers.isChecked());
        notificationSettings.put("notifyAdmins", checkboxNotifyAdmins.isChecked());
        notificationSettings.put("notifyApp", checkboxNotifyApp.isChecked());
        notificationSettings.put("notifyEmail", checkboxNotifyEmail.isChecked());
        notificationSettings.put("notifySMS", checkboxNotifySMS.isChecked());

        db.collection("system_settings").document("notification_settings")
                .set(notificationSettings)
                .addOnSuccessListener(aVoid -> {
                    // Sauvegarde réussie
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de l'enregistrement des paramètres: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Enregistrer les paramètres de sécurité
        Map<String, Object> securitySettings = new HashMap<>();
        String passwordPolicy;
        switch (spinnerPasswordPolicy.getSelectedItemPosition()) {
            case 0:
                passwordPolicy = "basic";
                break;
            case 1:
                passwordPolicy = "standard";
                break;
            case 2:
                passwordPolicy = "enhanced";
                break;
            case 3:
                passwordPolicy = "very_secure";
                break;
            default:
                passwordPolicy = "standard";
        }
        securitySettings.put("passwordPolicy", passwordPolicy);
        securitySettings.put("sessionDuration", seekBarSessionDuration.getProgress());
        securitySettings.put("twoFactor", checkboxTwoFactor.isChecked());

        db.collection("system_settings").document("security_settings")
                .set(securitySettings)
                .addOnSuccessListener(aVoid -> {
                    // Sauvegarde réussie
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de l'enregistrement des paramètres: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Enregistrer les paramètres de sauvegarde
        Map<String, Object> backupSettings = new HashMap<>();
        String backupFrequency;
        switch (spinnerBackupFrequency.getSelectedItemPosition()) {
            case 0:
                backupFrequency = "daily";
                break;
            case 1:
                backupFrequency = "weekly";
                break;
            case 2:
                backupFrequency = "biweekly";
                break;
            case 3:
                backupFrequency = "monthly";
                break;
            default:
                backupFrequency = "weekly";
        }
        backupSettings.put("backupFrequency", backupFrequency);
        // La date de dernière sauvegarde n'est pas mise à jour ici

        db.collection("system_settings").document("backup_settings")
                .set(backupSettings)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Paramètres enregistrés avec succès", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de l'enregistrement des paramètres: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showBackupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sauvegarde")
                .setMessage("Voulez-vous effectuer une sauvegarde complète du système maintenant ? Cette opération peut prendre plusieurs minutes.")
                .setPositiveButton("Sauvegarder", (dialog, which) -> {
                    performBackup();
                })
                .setNegativeButton("Annuler", null);
        builder.create().show();
    }

    private void performBackup() {
        Toast.makeText(this, "Sauvegarde en cours...", Toast.LENGTH_SHORT).show();

        // Simuler un temps de traitement
        btnBackupNow.setEnabled(false);
        btnBackupNow.setText("Sauvegarde en cours...");

        new android.os.Handler().postDelayed(() -> {
            btnBackupNow.setEnabled(true);
            btnBackupNow.setText("Effectuer une sauvegarde maintenant");

            // Mettre à jour la date de dernière sauvegarde
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            String currentDatetime = sdf.format(new java.util.Date());
            tvLastBackup.setText(currentDatetime);

            // Enregistrer la date dans Firebase
            db.collection("system_settings").document("backup_settings")
                    .update("lastBackup", currentDatetime)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Sauvegarde terminée avec succès", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erreur lors de la mise à jour de la date de sauvegarde", Toast.LENGTH_SHORT).show();
                    });
        }, 3000);
    }

    private void showCleanDatabaseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nettoyage de la base de données")
                .setMessage("Cette opération va supprimer les anciennes données (plus de 6 mois) et optimiser la base de données. Voulez-vous continuer ?")
                .setPositiveButton("Nettoyer", (dialog, which) -> {
                    Toast.makeText(this, "Nettoyage de la base de données en cours...", Toast.LENGTH_SHORT).show();

                    // Simuler un temps de traitement
                    btnCleanDatabase.setEnabled(false);

                    new android.os.Handler().postDelayed(() -> {
                        btnCleanDatabase.setEnabled(true);
                        Toast.makeText(this, "Nettoyage de la base de données terminé", Toast.LENGTH_SHORT).show();
                    }, 3000);
                })
                .setNegativeButton("Annuler", null);
        builder.create().show();
    }

    private void showRestoreDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Restauration d'une sauvegarde")
                .setMessage("ATTENTION : Cette opération va remplacer toutes les données actuelles par celles de la sauvegarde. Cette action est irréversible. Voulez-vous continuer ?")
                .setPositiveButton("Restaurer", (dialog, which) -> {
                    // Afficher les sauvegardes disponibles
                    showAvailableBackups();
                })
                .setNegativeButton("Annuler", null);
        builder.create().show();
    }

    private void showAvailableBackups() {
        // Simuler une liste de sauvegardes disponibles
        final String[] backups = {
                "Sauvegarde du 30/04/2025 02:15",
                "Sauvegarde du 29/04/2025 02:15",
                "Sauvegarde du 28/04/2025 02:15",
                "Sauvegarde du 27/04/2025 02:15",
                "Sauvegarde du 26/04/2025 02:15"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sélectionner une sauvegarde")
                .setItems(backups, (dialog, which) -> {
                    // Confirmation finale
                    confirmRestore(backups[which]);
                })
                .setNegativeButton("Annuler", null);
        builder.create().show();
    }

    private void confirmRestore(String backup) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation de restauration")
                .setMessage("Voulez-vous vraiment restaurer la " + backup + " ? Toutes les données actuelles seront remplacées.")
                .setPositiveButton("Restaurer", (dialog, which) -> {
                    Toast.makeText(this, "Restauration en cours...", Toast.LENGTH_SHORT).show();

                    // Simuler un temps de traitement
                    btnRestoreBackup.setEnabled(false);

                    new android.os.Handler().postDelayed(() -> {
                        btnRestoreBackup.setEnabled(true);
                        Toast.makeText(this, "Restauration terminée avec succès", Toast.LENGTH_SHORT).show();
                    }, 5000);
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