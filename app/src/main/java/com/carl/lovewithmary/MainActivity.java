package com.carl.lovewithmary;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;

public class MainActivity extends Activity {
    private TextView daysCount;
    private TextView loveLine;
    private TextView sinceDate;
    private TextView nextAnniversary;
    private TextView nextMonthsary;
    private TextView nextMilestone;
    private TextView dateValue;
    private TextView widgetPreview;
    private TextView reminderSummary;
    private EditText partnerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppState.ensureMigration(this);
        setContentView(R.layout.activity_main);
        bindViews();
        wireActions();
        refreshUi();
        LoveUpdater.refreshEverything(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppState.ensureMigration(this);
        refreshUi();
        LoveUpdater.refreshEverything(this);
    }

    private void bindViews() {
        daysCount = findViewById(R.id.days_count);
        loveLine = findViewById(R.id.love_line);
        sinceDate = findViewById(R.id.since_date);
        nextAnniversary = findViewById(R.id.next_anniversary);
        nextMonthsary = findViewById(R.id.next_monthsary);
        nextMilestone = findViewById(R.id.next_milestone);
        dateValue = findViewById(R.id.date_value);
        widgetPreview = findViewById(R.id.widget_preview);
        reminderSummary = findViewById(R.id.reminder_summary);
        partnerName = findViewById(R.id.partner_name);
    }

    private void wireActions() {
        Button chooseDate = findViewById(R.id.choose_date);
        Button saveStory = findViewById(R.id.save_story);
        Button addWidget = findViewById(R.id.add_widget);
        Button personalize = findViewById(R.id.personalize_widget);
        Button reminderSettings = findViewById(R.id.reminder_settings);
        Button shareCount = findViewById(R.id.share_count);

        chooseDate.setOnClickListener(v -> showDatePicker());
        saveStory.setOnClickListener(v -> saveStory());
        addWidget.setOnClickListener(v -> requestWidgetPin());
        personalize.setOnClickListener(v -> openSettings());
        reminderSettings.setOnClickListener(v -> openSettings());
        shareCount.setOnClickListener(v -> shareCount());
    }

    private void refreshUi() {
        String savedName = AppState.getPartnerName(this);
        if (!partnerName.hasFocus() && !partnerName.getText().toString().equals(savedName)) {
            partnerName.setText(savedName);
            partnerName.setSelection(partnerName.getText().length());
        }

        if (AppState.hasStartDate(this)) {
            long days = AppState.daysOfLove(this);
            daysCount.setText(String.valueOf(days));
            loveLine.setText(AppState.loveLine(this));
            sinceDate.setText(AppState.sinceText(this));
            dateValue.setText(AppState.formattedDate(this));
            nextAnniversary.setText(AppState.nextAnniversarySummary(this));
            nextMonthsary.setText(AppState.nextMonthsarySummary(this));
            nextMilestone.setText(AppState.nextMilestoneSummary(this));
        } else {
            daysCount.setText("♡");
            loveLine.setText("Your story starts here");
            sinceDate.setText("Choose a relationship date below.");
            dateValue.setText("Choose the day your story began");
            nextAnniversary.setText("Next anniversary\nSet your date");
            nextMonthsary.setText("Next monthsary\nSet your date");
            nextMilestone.setText("Next milestone\nSet your date");
        }

        widgetPreview.setText(AppState.counterText(this));
        widgetPreview.setTextColor(AppState.widgetTextColor(this));
        widgetPreview.setBackground(WidgetThemes.previewDrawable(
                AppState.widgetBackgroundColor(this),
                AppState.widgetBackgroundStrength(this),
                getResources().getDisplayMetrics().density
        ));

        reminderSummary.setText(buildReminderSummary());
    }

    private String buildReminderSummary() {
        if (!AppState.masterNotificationsEnabled(this)) {
            return "Reminders are currently off. Turn them on when you want Days of Love to watch the calendar for you.";
        }
        StringBuilder enabled = new StringBuilder("On: ");
        boolean any = false;
        if (AppState.anniversaryNotificationsEnabled(this)) {
            enabled.append("anniversary"); any = true;
        }
        if (AppState.monthsaryNotificationsEnabled(this)) {
            if (any) enabled.append(" • ");
            enabled.append("monthsary"); any = true;
        }
        if (AppState.milestoneNotificationsEnabled(this)) {
            if (any) enabled.append(" • ");
            enabled.append("milestones"); any = true;
        }
        if (AppState.giftNotificationsEnabled(this)) {
            if (any) enabled.append(" • ");
            enabled.append("gift-plan"); any = true;
        }
        if (AppState.persistentCounterEnabled(this)) {
            if (any) enabled.append(" • ");
            enabled.append("daily counter"); any = true;
        }
        if (!any) return "Notifications are enabled, but every reminder type is switched off.";
        return enabled.toString();
    }

    private void showDatePicker() {
        LocalDate current = AppState.getStartDate(this);
        if (current == null) current = LocalDate.now();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    LocalDate selected = LocalDate.of(year, month + 1, dayOfMonth);
                    AppState.setStartDate(this, selected);
                    refreshUi();
                    LoveUpdater.refreshEverything(this);
                },
                current.getYear(),
                current.getMonthValue() - 1,
                current.getDayOfMonth()
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void saveStory() {
        String name = partnerName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Add your partner's name first 💗", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!AppState.hasStartDate(this)) {
            Toast.makeText(this, "Choose the day your story began first 💗", Toast.LENGTH_SHORT).show();
            return;
        }

        AppState.setPartnerName(this, name);
        partnerName.clearFocus();
        refreshUi();
        LoveUpdater.refreshEverything(this);
        Toast.makeText(this, "Your love story is saved ❤️", Toast.LENGTH_SHORT).show();
    }

    private void requestWidgetPin() {
        if (!AppState.hasStartDate(this)) {
            Toast.makeText(this, "Choose your relationship date first ❤️", Toast.LENGTH_SHORT).show();
            return;
        }
        AppWidgetManager manager = getSystemService(AppWidgetManager.class);
        ComponentName provider = new ComponentName(this, LoveWidgetProvider.class);
        if (manager != null && manager.isRequestPinAppWidgetSupported()) {
            manager.requestPinAppWidget(provider, null, null);
        } else {
            Toast.makeText(this, "Open your Home screen widget picker and choose Days of Love.", Toast.LENGTH_LONG).show();
        }
    }

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void shareCount() {
        if (!AppState.hasStartDate(this)) {
            Toast.makeText(this, "Set your love story date first ❤️", Toast.LENGTH_SHORT).show();
            return;
        }
        String text = AppState.counterText(this) + "\n" + AppState.sinceText(this) + "\n\nMade with Days of Love 💗";
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(send, "Share your love story"));
    }
}
