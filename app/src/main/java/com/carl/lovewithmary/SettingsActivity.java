package com.carl.lovewithmary;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    private static final int NOTIFICATION_PERMISSION_REQUEST = 52;

    private Spinner themeSpinner;
    private Spinner reminderTime;
    private EditText backgroundHex;
    private EditText textHex;
    private SeekBar transparencySeek;
    private TextView transparencyLabel;
    private TextView preview;

    private Switch masterNotifications;
    private Switch persistentCounter;
    private Switch anniversaryNotifications;
    private Switch monthsaryNotifications;
    private Switch milestoneNotifications;
    private Switch giftNotifications;

    private CheckBox anniv30;
    private CheckBox anniv14;
    private CheckBox anniv7;
    private CheckBox annivFinalWeek;
    private CheckBox anniv3;
    private CheckBox anniv1;
    private CheckBox anniv0;
    private CheckBox monthsary1;
    private CheckBox monthsary0;

    private boolean loading = true;

    private final String[] reminderLabels = new String[]{"9:00 AM", "12:00 PM", "6:00 PM", "8:00 PM"};
    private final int[] reminderHours = new int[]{9, 12, 18, 20};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppState.ensureMigration(this);
        setContentView(R.layout.activity_settings);
        bindViews();
        configureSpinners();
        loadState();
        wireActions();
        loading = false;
        updateNotificationControlsEnabled();
        updatePreviewFromFields(false);
    }

    private void bindViews() {
        themeSpinner = findViewById(R.id.theme_spinner);
        reminderTime = findViewById(R.id.reminder_time);
        backgroundHex = findViewById(R.id.background_hex);
        textHex = findViewById(R.id.text_hex);
        transparencySeek = findViewById(R.id.transparency_seek);
        transparencyLabel = findViewById(R.id.transparency_label);
        preview = findViewById(R.id.settings_widget_preview);

        masterNotifications = findViewById(R.id.master_notifications);
        persistentCounter = findViewById(R.id.persistent_counter);
        anniversaryNotifications = findViewById(R.id.anniversary_notifications);
        monthsaryNotifications = findViewById(R.id.monthsary_notifications);
        milestoneNotifications = findViewById(R.id.milestone_notifications);
        giftNotifications = findViewById(R.id.gift_notifications);

        anniv30 = findViewById(R.id.anniv_30);
        anniv14 = findViewById(R.id.anniv_14);
        anniv7 = findViewById(R.id.anniv_7);
        annivFinalWeek = findViewById(R.id.anniv_final_week);
        anniv3 = findViewById(R.id.anniv_3);
        anniv1 = findViewById(R.id.anniv_1);
        anniv0 = findViewById(R.id.anniv_0);
        monthsary1 = findViewById(R.id.monthsary_1);
        monthsary0 = findViewById(R.id.monthsary_0);
    }

    private void configureSpinners() {
        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                WidgetThemes.NAMES
        );
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(themeAdapter);

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                reminderLabels
        );
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reminderTime.setAdapter(timeAdapter);
    }

    private void loadState() {
        themeSpinner.setSelection(WidgetThemes.indexOf(AppState.widgetTheme(this)));
        backgroundHex.setText(AppState.colorToHex(AppState.widgetBackgroundColor(this)));
        textHex.setText(AppState.colorToHex(AppState.widgetTextColor(this)));
        transparencySeek.setProgress(AppState.widgetBackgroundStrength(this));
        updateTransparencyLabel();

        masterNotifications.setChecked(AppState.masterNotificationsEnabled(this));
        persistentCounter.setChecked(AppState.persistentCounterEnabled(this));
        anniversaryNotifications.setChecked(AppState.anniversaryNotificationsEnabled(this));
        monthsaryNotifications.setChecked(AppState.monthsaryNotificationsEnabled(this));
        milestoneNotifications.setChecked(AppState.milestoneNotificationsEnabled(this));
        giftNotifications.setChecked(AppState.giftNotificationsEnabled(this));

        anniv30.setChecked(AppState.anniversaryOffsetEnabled(this, 30));
        anniv14.setChecked(AppState.anniversaryOffsetEnabled(this, 14));
        anniv7.setChecked(AppState.anniversaryOffsetEnabled(this, 7));
        annivFinalWeek.setChecked(AppState.anniversaryFinalWeekDailyEnabled(this));
        anniv3.setChecked(AppState.anniversaryOffsetEnabled(this, 3));
        anniv1.setChecked(AppState.anniversaryOffsetEnabled(this, 1));
        anniv0.setChecked(AppState.anniversaryOffsetEnabled(this, 0));
        monthsary1.setChecked(AppState.monthsaryOffsetEnabled(this, 1));
        monthsary0.setChecked(AppState.monthsaryOffsetEnabled(this, 0));

        int hour = AppState.reminderHour(this);
        int index = 0;
        for (int i = 0; i < reminderHours.length; i++) if (reminderHours[i] == hour) index = i;
        reminderTime.setSelection(index);
    }

    private void wireActions() {
        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (loading) return;
                String theme = WidgetThemes.NAMES[position];
                backgroundHex.setText(AppState.colorToHex(WidgetThemes.backgroundFor(theme)));
                textHex.setText(AppState.colorToHex(WidgetThemes.textFor(theme)));
                updatePreviewFromFields(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        transparencySeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTransparencyLabel();
                if (!loading) updatePreviewFromFields(false);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        Button previewButton = findViewById(R.id.preview_custom_colors);
        previewButton.setOnClickListener(v -> updatePreviewFromFields(true));

        masterNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateNotificationControlsEnabled();
            if (isChecked && !LoveUpdater.canPostNotifications(this) && Build.VERSION.SDK_INT >= 33) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
            }
        });
        anniversaryNotifications.setOnCheckedChangeListener((button, checked) -> updateNotificationControlsEnabled());
        monthsaryNotifications.setOnCheckedChangeListener((button, checked) -> updateNotificationControlsEnabled());

        Button save = findViewById(R.id.save_settings);
        save.setOnClickListener(v -> saveSettings());

        Button reset = findViewById(R.id.reset_appearance);
        reset.setOnClickListener(v -> {
            AppState.resetAppearance(this);
            loading = true;
            themeSpinner.setSelection(0);
            backgroundHex.setText(AppState.colorToHex(WidgetThemes.DEFAULT_BG));
            textHex.setText("#FFFFFF");
            transparencySeek.setProgress(100);
            loading = false;
            updateTransparencyLabel();
            updatePreviewFromFields(false);
            LoveUpdater.updateAllWidgets(this);
            Toast.makeText(this, "Romantic Rose restored 💗", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateNotificationControlsEnabled() {
        boolean enabled = masterNotifications.isChecked();
        persistentCounter.setEnabled(enabled);
        anniversaryNotifications.setEnabled(enabled);
        monthsaryNotifications.setEnabled(enabled);
        milestoneNotifications.setEnabled(enabled);
        giftNotifications.setEnabled(enabled);
        anniv30.setEnabled(enabled && anniversaryNotifications.isChecked());
        anniv14.setEnabled(enabled && anniversaryNotifications.isChecked());
        anniv7.setEnabled(enabled && anniversaryNotifications.isChecked());
        annivFinalWeek.setEnabled(enabled && anniversaryNotifications.isChecked());
        anniv3.setEnabled(enabled && anniversaryNotifications.isChecked());
        anniv1.setEnabled(enabled && anniversaryNotifications.isChecked());
        anniv0.setEnabled(enabled && anniversaryNotifications.isChecked());
        monthsary1.setEnabled(enabled && monthsaryNotifications.isChecked());
        monthsary0.setEnabled(enabled && monthsaryNotifications.isChecked());
        reminderTime.setEnabled(enabled);

    }

    private void updateTransparencyLabel() {
        int strength = transparencySeek.getProgress();
        transparencyLabel.setText("Background strength: " + strength + "%");
    }

    private Integer parseColorField(EditText field) {
        String raw = field.getText().toString().trim();
        if (raw.isEmpty()) return null;
        if (!raw.startsWith("#")) raw = "#" + raw;
        if (raw.length() != 7) return null;
        try {
            return Color.parseColor(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private boolean updatePreviewFromFields(boolean showError) {
        Integer bg = parseColorField(backgroundHex);
        Integer text = parseColorField(textHex);
        if (bg == null || text == null) {
            if (showError) Toast.makeText(this, "Use colors like #D4145A and #FFFFFF", Toast.LENGTH_LONG).show();
            return false;
        }

        int strength = transparencySeek.getProgress();
        preview.setText(AppState.counterText(this));
        preview.setTextColor(text);
        preview.setBackground(WidgetThemes.previewDrawable(
                bg,
                strength,
                getResources().getDisplayMetrics().density
        ));
        return true;
    }

    private void saveSettings() {
        Integer bg = parseColorField(backgroundHex);
        Integer text = parseColorField(textHex);
        if (bg == null || text == null) {
            Toast.makeText(this, "Check your widget colors. Use six-digit hex colors like #D4145A.", Toast.LENGTH_LONG).show();
            return;
        }

        AppState.setWidgetTheme(this, WidgetThemes.NAMES[themeSpinner.getSelectedItemPosition()]);
        AppState.setWidgetBackgroundColor(this, bg);
        AppState.setWidgetTextColor(this, text);
        AppState.setWidgetBackgroundStrength(this, transparencySeek.getProgress());

        boolean master = masterNotifications.isChecked();
        if (master && Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
            Toast.makeText(this, "Allow notifications, then tap Save again.", Toast.LENGTH_LONG).show();
            return;
        }

        AppState.setMasterNotificationsEnabled(this, master);
        AppState.setPersistentCounterEnabled(this, persistentCounter.isChecked());
        AppState.setAnniversaryNotificationsEnabled(this, anniversaryNotifications.isChecked());
        AppState.setMonthsaryNotificationsEnabled(this, monthsaryNotifications.isChecked());
        AppState.setMilestoneNotificationsEnabled(this, milestoneNotifications.isChecked());
        AppState.setGiftNotificationsEnabled(this, giftNotifications.isChecked());

        AppState.setAnniversaryOffsetEnabled(this, 30, anniv30.isChecked());
        AppState.setAnniversaryOffsetEnabled(this, 14, anniv14.isChecked());
        AppState.setAnniversaryOffsetEnabled(this, 7, anniv7.isChecked());
        AppState.setAnniversaryFinalWeekDailyEnabled(this, annivFinalWeek.isChecked());
        AppState.setAnniversaryOffsetEnabled(this, 3, anniv3.isChecked());
        AppState.setAnniversaryOffsetEnabled(this, 1, anniv1.isChecked());
        AppState.setAnniversaryOffsetEnabled(this, 0, anniv0.isChecked());
        AppState.setMonthsaryOffsetEnabled(this, 1, monthsary1.isChecked());
        AppState.setMonthsaryOffsetEnabled(this, 0, monthsary0.isChecked());
        AppState.setReminderHour(this, reminderHours[reminderTime.getSelectedItemPosition()]);

        if (!master) {
            LoveUpdater.cancelAllNotifications(this);
        }
        LoveUpdater.refreshEverything(this);
        ReminderEngine.checkAndSend(this);

        Toast.makeText(this, "Settings saved ❤️", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (!granted) {
                masterNotifications.setChecked(false);
                AppState.setMasterNotificationsEnabled(this, false);
                LoveUpdater.cancelAllNotifications(this);
                Toast.makeText(this, "Notifications stay off until you allow permission.", Toast.LENGTH_LONG).show();
            } else {
                masterNotifications.setChecked(true);
                LoveUpdater.createChannels(this);
                Toast.makeText(this, "Permission granted. Tap Save my settings 💕", Toast.LENGTH_SHORT).show();
            }
            updateNotificationControlsEnabled();
        }
    }
}
