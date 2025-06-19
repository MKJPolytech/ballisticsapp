package kr.ac.kopo.ballisticsapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerWeaponType, spinnerAmmo;
    EditText editVelocity, editAngle, editDistance, editCustomMass;
    TextView textResult, textMassInfo, editCustomMassUnit;
    TextView labelDistanceUnit, labelAngleUnit, labelVelocityUnit;
    Button btnCalculate, btnToggleUnit, btnAdvanced;
    boolean isMps = true;
    double gravity = 9.8;

    List<Bullet> allBullets;
    List<Bullet> filteredBullets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerWeaponType = findViewById(R.id.spinner_weapon_type);
        spinnerAmmo = findViewById(R.id.spinner_ammo);
        editVelocity = findViewById(R.id.edit_velocity);
        editAngle = findViewById(R.id.edit_angle);
        editDistance = findViewById(R.id.edit_distance);
        editCustomMass = findViewById(R.id.edit_custom_mass);
        editCustomMassUnit = findViewById(R.id.edit_custom_mass_unit);
        labelDistanceUnit = findViewById(R.id.label_distance_unit);
        labelAngleUnit = findViewById(R.id.label_angle_unit);
        labelVelocityUnit = findViewById(R.id.label_velocity_unit);
        textResult = findViewById(R.id.text_result);
        textMassInfo = findViewById(R.id.text_mass_info);
        btnCalculate = findViewById(R.id.btn_calculate);
        btnToggleUnit = findViewById(R.id.btn_unit_toggle);
        btnAdvanced = findViewById(R.id.btn_advanced);

        ArrayAdapter<String> weaponAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"1. Choose weapon type", "Rifle", "Shotgun", "Handgun"});
        weaponAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeaponType.setAdapter(weaponAdapter);

        allBullets = Arrays.asList(
                new Bullet("5.56 NATO M193 FMJ", 3.56, 940, "Rifle"),
                new Bullet("5.56 NATO CUSTOM", 0, 0, "Rifle"),
                new Bullet("7.62 NATO M80 FMJ", 9.33, 840, "Rifle"),
                new Bullet("7.62 NATO CUSTOM", 0, 0, "Rifle"),
                new Bullet("12 Gauge Slug", 28.0, 450, "Shotgun"),
                new Bullet("9mm Parabellum FMJ", 7.45, 360, "Handgun"),
                new Bullet("9mm CUSTOM", 0, 0, "Handgun")
        );

        spinnerWeaponType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.startsWith("1.")) {
                    filteredBullets = new ArrayList<>();
                    spinnerAmmo.setAdapter(null);
                    textMassInfo.setText("");
                    editVelocity.setText("");
                    return;
                }

                filteredBullets = new ArrayList<>();
                for (Bullet b : allBullets) {
                    if (b.getCategory().equals(selected)) {
                        filteredBullets.add(b);
                    }
                }

                ArrayAdapter<Bullet> ammoAdapter = new ArrayAdapter<>(
                        MainActivity.this,
                        android.R.layout.simple_spinner_item,
                        filteredBullets
                );
                ammoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerAmmo.setAdapter(ammoAdapter);
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerAmmo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Bullet bullet = (Bullet) parent.getItemAtPosition(position);
                editVelocity.setText(String.format(Locale.US, "%.2f", bullet.getVelocityMps()));

                if (bullet.isCustom()) {
                    editVelocity.setText("");
                    editVelocity.setHint(isMps ? "속도 (m/s)" : "Velocity (fps)");
                    editCustomMass.setVisibility(View.VISIBLE);
                    editCustomMassUnit.setVisibility(View.VISIBLE);
                    editCustomMassUnit.setText(isMps ? "g" : "gr");
                    textMassInfo.setText("커스텀 질량 입력 필요");
                } else {
                    double velocityVal = bullet.getVelocityMps();
                    if (!isMps) velocityVal = mpsToFps(velocityVal);
                    editVelocity.setText(String.format("%.2f", velocityVal));
                    editCustomMass.setVisibility(View.GONE);
                    editCustomMassUnit.setVisibility(View.GONE);
                    double grain = gramToGrain(bullet.getMassGram());
                    if (isMps) {
                        textMassInfo.setText(String.format(Locale.US, "질량: %.2fg", bullet.getMassGram()));
                    } else {
                        textMassInfo.setText(String.format(Locale.US, "질량: %.0fgr", grain));
                    }
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnToggleUnit.setOnClickListener(v -> {
            isMps = !isMps;
            btnToggleUnit.setText("단위 전환: " + (isMps ? "m/s ↔ fps" : "fps ↔ m/s"));
            editVelocity.setHint(isMps ? "속도 (m/s)" : "Velocity (fps)");
            editDistance.setHint(isMps ? "거리 (m)" : "Distance (ft)");
            labelDistanceUnit.setText(isMps ? "m" : "ft");
            labelAngleUnit.setText("°");
            labelVelocityUnit.setText(isMps ? "m/s" : "fps");
            Bullet b = (Bullet) spinnerAmmo.getSelectedItem();

            if (b != null) {
                if (b.isCustom()) {
                    editCustomMassUnit.setText(isMps ? "g" : "gr");
                } else {
                    double grain = gramToGrain(b.getMassGram());
                    if (isMps) {
                        textMassInfo.setText(String.format(Locale.US, "질량: %.2fg", b.getMassGram()));
                    } else {
                        textMassInfo.setText(String.format(Locale.US, "질량: %.0fgr", grain));
                    }
                }
            }
            try {
                double velocityVal = Double.parseDouble(editVelocity.getText().toString());
                velocityVal = isMps ? fpsToMps(velocityVal) : mpsToFps(velocityVal);
                editVelocity.setText(String.format("%.2f", velocityVal));
            } catch (Exception e) {
                Toast.makeText(this, "입력값 확인", Toast.LENGTH_SHORT).show();
            }
        });

        btnAdvanced.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_advanced, null);
            EditText editGravity = dialogView.findViewById(R.id.edit_gravity);
            editGravity.setText(String.valueOf(gravity));

            new AlertDialog.Builder(this)
                    .setTitle("⚙ 고급 설정")
                    .setView(dialogView)
                    .setPositiveButton("저장", (dialog, which) -> {
                        try {
                            gravity = Double.parseDouble(editGravity.getText().toString());
                        } catch (Exception e) {
                            Toast.makeText(this, "중력값 오류", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        btnCalculate.setOnClickListener(v -> {
            Bullet b = (Bullet) spinnerAmmo.getSelectedItem();
            if (b == null) return;

            try {
                String velocityInput = editVelocity.getText().toString();
                if (velocityInput.isEmpty()) {
                    Toast.makeText(this, "속도를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                double velocity = Double.parseDouble(velocityInput);
                if (!isMps) velocity = fpsToMps(velocity);

                double angle = Double.parseDouble(editAngle.getText().toString());
                double angleRad = Math.toRadians(angle);

                double mass = b.getMassGram();
                if (b.isCustom()) {
                    mass = Double.parseDouble(editCustomMass.getText().toString());
                    if (!isMps) mass /= 15.4324;
                }

                double horizontalSpeed = velocity * Math.cos(angleRad);
                double distance = 0.0;
                String distanceStr = editDistance.getText().toString();
                if (!distanceStr.isEmpty()) {
                    distance = Double.parseDouble(distanceStr);
                    if (!isMps) distance *= 0.3048;
                }

                double time = (2 * velocity * Math.sin(angleRad)) / gravity;
                double range = Math.pow(velocity, 2) * Math.sin(2 * angleRad) / gravity;
                double maxHeight = Math.pow(velocity * Math.sin(angleRad), 2) / (2 * gravity);

                String kmRange = String.format(Locale.US, " (%.1f Km)", range / 1000.0);
                String additional = "";
                if (distance > 0) {
                    double tToTarget = distance / horizontalSpeed;
                    additional = String.format(Locale.US, "\n🎯 %.0f m 도달 시간: %.2f 초", distance, tToTarget);
                }

                String result = String.format(Locale.US,
                        "⏱ 비행 시간: %.2f 초\n📏 최대 사거리: %.2f m%s\n📈 최고 높이: %.2f m%s",
                        time, range, kmRange, maxHeight, additional);

                textResult.setText(result);
                textResult.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                Toast.makeText(this, "입력값 확인", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double gramToGrain(double gram) {
        return gram * 15.4324;
    }

    private double fpsToMps(double fps) {
        return fps * 0.3048;
    }

    private double mpsToFps(double mps) {
        return mps / 0.3048;
    }
}

