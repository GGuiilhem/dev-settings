package com.gguilhem.devsettings;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {
    private static final String KEY_DEV_SETTINGS = "development_settings_enabled";
    private static final String KEY_ADB = "adb_enabled";
    private static final String KEY_ADB_WIFI = "adb_wifi_enabled";
    private static final String KEY_WINDOW_ANIM = "window_animation_scale";
    private static final String KEY_TRANSITION_ANIM = "transition_animation_scale";
    private static final String KEY_ANIMATOR_DURATION = "animator_duration_scale";

    private int colorBg;
    private int colorCard;
    private int colorCardAlt;
    private int colorText;
    private int colorMuted;
    private int colorDivider;
    private int colorAccent;
    private int colorOnAccent;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable statusTicker = new Runnable() {
        @Override
        public void run() {
            refreshStatus();
            handler.postDelayed(this, 1000);
        }
    };

    private Switch devSwitch;
    private Switch usbSwitch;
    private Switch wifiSwitch;
    private TextView permissionWarningText;
    private TextView disabledHintText;
    private TextView animationStatusText;
    private TextView chip0x;
    private TextView chip05x;
    private TextView chip1x;
    private View usbDivider;
    private View usbRow;
    private View wifiDivider;
    private View wifiRow;
    private View mockLocationDivider;
    private View mockLocationRow;
    private TextView animationSectionLabel;
    private LinearLayout animationCard;
    private boolean refreshing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(isSystemDark() ? R.style.AppTheme_Dark : R.style.AppTheme_Light);
        super.onCreate(savedInstanceState);
        loadColors();
        configureSystemBars();
        setContentView(buildContent());
        refreshStatus();
    }

    private void loadColors() {
        colorBg = color(R.color.app_bg);
        colorCard = color(R.color.app_card);
        colorCardAlt = color(R.color.app_card_alt);
        colorText = color(R.color.app_text);
        colorMuted = color(R.color.app_muted);
        colorDivider = color(R.color.app_divider);
        colorAccent = color(R.color.app_accent);
        colorOnAccent = color(R.color.app_on_accent);
    }

    private void configureSystemBars() {
        getWindow().setStatusBarColor(colorBg);
        getWindow().setNavigationBarColor(colorBg);

        View decor = getWindow().getDecorView();
        int flags = decor.getSystemUiVisibility();
        int lightFlags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        if (isSystemDark()) {
            flags &= ~lightFlags;
        } else {
            flags |= lightFlags;
        }
        decor.setSystemUiVisibility(flags);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(statusTicker);
        handler.post(statusTicker);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(statusTicker);
        super.onPause();
    }

    private View buildContent() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(colorBg);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(36), dp(22), dp(26));
        scrollView.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        root.addView(headerCard(), fullWidthWithBottom(dp(18)));

        permissionWarningText = text("Permissao do sistema ausente. Conceda WRITE_SECURE_SETTINGS via ADB.", 13, colorMuted, false);
        permissionWarningText.setPadding(dp(14), dp(10), dp(14), dp(10));
        permissionWarningText.setBackground(rounded(colorCardAlt, dp(18)));
        root.addView(permissionWarningText, fullWidthWithBottom(dp(18)));

        root.addView(sectionLabel("Controles"));
        LinearLayout settingsCard = card();
        devSwitch = addSwitchRow(settingsCard, "Modo desenvolvedor", "Chave principal das opcoes avancadas.", KEY_DEV_SETTINGS);

        disabledHintText = text("Depuracao e animacoes aparecem quando o modo desenvolvedor esta ligado.", 13, colorMuted, false);
        disabledHintText.setPadding(dp(16), 0, dp(16), dp(16));
        settingsCard.addView(disabledHintText);

        usbDivider = addDivider(settingsCard);
        usbSwitch = addSwitchRow(settingsCard, "Depuracao USB", "ADB via cabo.", KEY_ADB);
        usbRow = (View) usbSwitch.getParent();
        wifiDivider = addDivider(settingsCard);
        wifiSwitch = addSwitchRow(settingsCard, "Depuracao Wi-Fi", "ADB sem fio.", KEY_ADB_WIFI);
        wifiRow = (View) wifiSwitch.getParent();
        mockLocationDivider = addDivider(settingsCard);
        mockLocationRow = addActionRow(settingsCard, "App de local ficticio", "Selecionar no sistema.", this::openMockLocationSelector);
        root.addView(settingsCard, fullWidthWithBottom(dp(18)));

        animationSectionLabel = sectionLabel("Animacoes");
        root.addView(animationSectionLabel);
        animationCard = card();
        animationStatusText = text("", 14, colorMuted, false);
        animationStatusText.setPadding(dp(16), dp(14), dp(16), dp(10));
        animationCard.addView(animationStatusText);

        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setPadding(dp(12), 0, dp(12), dp(12));
        chip0x = animationChip("0x", 0f);
        chip05x = animationChip("0.5x", 0.5f);
        chip1x = animationChip("1x", 1f);
        chips.addView(chip0x, weightParams());
        chips.addView(chip05x, weightParams());
        chips.addView(chip1x, weightParams());
        animationCard.addView(chips);
        root.addView(animationCard, fullWidthWithBottom(dp(18)));

        root.addView(actionButton("Abrir opcoes do desenvolvedor", this::openDeveloperOptions), fullWidthWithBottom(0));

        return scrollView;
    }

    private View headerCard() {
        LinearLayout header = card();
        header.setPadding(dp(18), dp(16), dp(18), dp(16));

        TextView label = text("DEV SETTINGS", 12, colorAccent, true);
        label.setPadding(0, 0, 0, dp(6));
        header.addView(label);

        TextView title = text("Controles de desenvolvedor", 24, colorText, true);
        header.addView(title);

        TextView subtitle = text("Atalhos para ADB, local ficticio e animacoes.", 14, colorMuted, false);
        subtitle.setPadding(0, dp(6), 0, dp(14));
        header.addView(subtitle);

        return header;
    }

    private Switch addSwitchRow(LinearLayout parent, String title, String subtitle, String key) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(12), dp(12), dp(12));
        row.setMinimumHeight(dp(74));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleView = text(title, 16, colorText, false);
        TextView subtitleView = text(subtitle, 12, colorMuted, false);
        subtitleView.setPadding(0, dp(3), dp(12), 0);
        copy.addView(titleView);
        copy.addView(subtitleView);

        Switch settingSwitch = new Switch(this);
        settingSwitch.setShowText(false);
        settingSwitch.setMinWidth(dp(54));
        settingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (refreshing) {
                return;
            }
            runSettingAction(title + (isChecked ? " ligado" : " desligado"), () -> writeSwitchValue(key, isChecked));
        });

        row.setOnClickListener(v -> settingSwitch.setChecked(!settingSwitch.isChecked()));
        row.addView(copy, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(settingSwitch);
        parent.addView(row);
        return settingSwitch;
    }

    private View addActionRow(LinearLayout parent, String title, String subtitle, Runnable action) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(12), dp(16), dp(12));
        row.setMinimumHeight(dp(74));
        row.setOnClickListener(v -> action.run());

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleView = text(title, 16, colorText, false);
        TextView subtitleView = text(subtitle, 12, colorMuted, false);
        subtitleView.setPadding(0, dp(3), dp(12), 0);
        copy.addView(titleView);
        copy.addView(subtitleView);

        TextView arrow = text(">", 24, colorMuted, false);
        arrow.setGravity(Gravity.CENTER);

        row.addView(copy, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(arrow, new LinearLayout.LayoutParams(dp(28), LinearLayout.LayoutParams.WRAP_CONTENT));
        parent.addView(row);
        return row;
    }

    private void writeSwitchValue(String key, boolean enabled) {
        if (KEY_DEV_SETTINGS.equals(key)) {
            if (enabled) {
                putInt(KEY_DEV_SETTINGS, 1);
            } else {
                putInt(KEY_ADB, 0);
                putInt(KEY_ADB_WIFI, 0);
                putInt(KEY_DEV_SETTINGS, 0);
            }
            return;
        }

        putInt(key, enabled ? 1 : 0);
    }

    private TextView animationChip(String label, float value) {
        TextView chip = text(label, 15, colorText, true);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(0, dp(10), 0, dp(10));
        chip.setOnClickListener(v -> runSettingAction("Animacoes em " + label, () -> setAnimations(value)));
        return chip;
    }

    private TextView actionButton(String label, Runnable action) {
        TextView button = text(label, 15, colorOnAccent, true);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(16), dp(14), dp(16), dp(14));
        button.setBackground(rounded(colorAccent, dp(22)));
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private void openMockLocationSelector() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        intent.putExtra(":settings:fragment_args_key", "mock_location_app");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Tela de desenvolvedor nao encontrada", Toast.LENGTH_LONG).show();
        }
    }

    private void openDeveloperOptions() {
        try {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Tela de desenvolvedor nao encontrada", Toast.LENGTH_LONG).show();
        }
    }

    private void runSettingAction(String successMessage, SettingAction action) {
        if (!hasWriteSecureSettings()) {
            Toast.makeText(this, "Permissao WRITE_SECURE_SETTINGS nao concedida", Toast.LENGTH_LONG).show();
            refreshStatus();
            return;
        }

        try {
            action.run();
            Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Falha: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        refreshStatus();
    }

    private void refreshStatus() {
        boolean devEnabled = isEnabled(KEY_DEV_SETTINGS);

        refreshing = true;
        setSwitchChecked(devSwitch, devEnabled);
        setSwitchChecked(usbSwitch, devEnabled && isEnabled(KEY_ADB));
        setSwitchChecked(wifiSwitch, devEnabled && isEnabled(KEY_ADB_WIFI));
        refreshing = false;

        setDeveloperOnlyVisible(devEnabled);

        if (permissionWarningText != null) {
            permissionWarningText.setVisibility(hasWriteSecureSettings() ? View.GONE : View.VISIBLE);
        }

        if (devEnabled) {
            String window = readString(KEY_WINDOW_ANIM);
            String transition = readString(KEY_TRANSITION_ANIM);
            String animator = readString(KEY_ANIMATOR_DURATION);
            animationStatusText.setText("Atual: janela " + window + " | transicao " + transition + " | duracao " + animator);
            updateAnimationChips(window, transition, animator);
        }
    }

    private void setDeveloperOnlyVisible(boolean visible) {
        int state = visible ? View.VISIBLE : View.GONE;
        int hintState = visible ? View.GONE : View.VISIBLE;
        if (disabledHintText != null) {
            disabledHintText.setVisibility(hintState);
        }
        if (usbDivider != null) {
            usbDivider.setVisibility(state);
        }
        if (usbRow != null) {
            usbRow.setVisibility(state);
        }
        if (wifiDivider != null) {
            wifiDivider.setVisibility(state);
        }
        if (wifiRow != null) {
            wifiRow.setVisibility(state);
        }
        if (mockLocationDivider != null) {
            mockLocationDivider.setVisibility(state);
        }
        if (mockLocationRow != null) {
            mockLocationRow.setVisibility(state);
        }
        if (animationSectionLabel != null) {
            animationSectionLabel.setVisibility(state);
        }
        if (animationCard != null) {
            animationCard.setVisibility(state);
        }
    }

    private void updateAnimationChips(String window, String transition, String animator) {
        boolean all0 = sameAnimation(window, transition, animator, "0");
        boolean all05 = sameAnimation(window, transition, animator, "0.5");
        boolean all1 = sameAnimation(window, transition, animator, "1");
        styleChip(chip0x, all0);
        styleChip(chip05x, all05);
        styleChip(chip1x, all1);
    }

    private boolean sameAnimation(String window, String transition, String animator, String expected) {
        return expected.equals(normalizeFloat(window))
                && expected.equals(normalizeFloat(transition))
                && expected.equals(normalizeFloat(animator));
    }

    private String normalizeFloat(String value) {
        try {
            float parsed = Float.parseFloat(value);
            if (parsed == 0f) {
                return "0";
            }
            if (parsed == 0.5f) {
                return "0.5";
            }
            if (parsed == 1f) {
                return "1";
            }
            return String.format(Locale.US, "%.2f", parsed);
        } catch (Exception e) {
            return value;
        }
    }

    private void styleChip(TextView chip, boolean selected) {
        int bg = selected ? colorAccent : colorCardAlt;
        int fg = selected ? colorOnAccent : colorText;
        chip.setTextColor(fg);
        chip.setBackground(rounded(bg, dp(18)));
    }

    private boolean isEnabled(String key) {
        return "1".equals(readString(key));
    }

    private void setSwitchChecked(CompoundButton button, boolean checked) {
        if (button != null) {
            button.setChecked(checked);
        }
    }

    private boolean hasWriteSecureSettings() {
        return getPackageManager().checkPermission(
                Manifest.permission.WRITE_SECURE_SETTINGS,
                getPackageName()
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void setAnimations(float value) {
        putFloat(KEY_WINDOW_ANIM, value);
        putFloat(KEY_TRANSITION_ANIM, value);
        putFloat(KEY_ANIMATOR_DURATION, value);
    }

    private void putInt(String key, int value) {
        Settings.Global.putInt(resolver(), key, value);
    }

    private void putFloat(String key, float value) {
        Settings.Global.putFloat(resolver(), key, value);
    }

    private String readString(String key) {
        String value = Settings.Global.getString(resolver(), key);
        return value == null ? "null" : value;
    }

    private ContentResolver resolver() {
        return getContentResolver();
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(rounded(colorCard, dp(24)));
        return card;
    }

    private TextView sectionLabel(String text) {
        TextView view = text(text, 13, colorMuted, true);
        view.setPadding(dp(4), 0, 0, dp(8));
        return view;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        if (bold) {
            view.setTypeface(view.getTypeface(), Typeface.BOLD);
        }
        return view;
    }

    private View addDivider(LinearLayout parent) {
        View divider = new View(this);
        divider.setBackgroundColor(colorDivider);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        );
        params.setMargins(dp(16), 0, dp(16), 0);
        parent.addView(divider, params);
        return divider;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private LinearLayout.LayoutParams weightParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(dp(4), 0, dp(4), 0);
        return params;
    }

    private LinearLayout.LayoutParams fullWidthWithBottom(int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, bottom);
        return params;
    }

    private boolean isSystemDark() {
        int mode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    private int color(int resId) {
        return getResources().getColor(resId, getTheme());
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private interface SettingAction {
        void run();
    }
}
