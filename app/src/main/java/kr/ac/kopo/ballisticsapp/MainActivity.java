package kr.ac.kopo.ballisticsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerWeaponType, spinnerAmmo;
    EditText editVelocity, editAngle, editDistance, editCustomMass;
    TextView textResult, textMassInfo, editCustomMassUnit;
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
        textResult = findViewById(R.id.text_result);
        textMassInfo = findViewById(R.id.text_mass_info);
        btnCalculate = findViewById(R.id.btn_calculate);
        btnToggleUnit = findViewById(R.id.btn_unit_toggle);
        btnAdvanced = findViewById(R.id.btn_advanced);

        // 1. Choose weapon type 옵션 포함
        ArrayAdapter<String> weaponAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"1. Choose weapon type", "Rifle", "Shotgun", "Handgun"});
        weaponAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeaponType.setAdapter(weaponAdapter);
        spinnerWeaponType.setSelection(0);

        // 전체 탄 목록
        allBullets = Arrays.asList(
                new Bullet("5.56 NATO M193 FMJ", 3.56, 940, "Rifle"),
                new Bullet("5.56 NATO CUSTOM", 0, 940, "Rifle"),
                new Bullet("7.62 NATO M80 FMJ", 9.33, 840, "Rifle"),
                new Bullet("7.62 NATO CUSTOM", 0, 840, "Rifle"),
                new Bullet("12 Gauge Slug", 28.0, 450, "Shotgun"),
                new Bullet("9mm Parabellum FMJ", 7.45, 360, "Handgun"),
                new Bullet("9mm CUSTOM", 0, 360, "Handgun")
        );

        // 무기 타입 선택 시 탄 종류 필터링
        spinnerWeaponType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                if (selectedCategory.startsWith("1.")) {
                    filteredBullets = new ArrayList<>();
                    spinnerAmmo.setAdapter(null);
                    textMassInfo.setText("");
                    editVelocity.setText("");
                    return;
                }

                filteredBullets = new ArrayList<>();
                for (Bullet bullet : allBullets) {
                    if (bullet.getCategory().equals(selectedCategory)) {
                        filteredBullets.add(bullet);
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

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 탄 선택 시 속도/질량 표시
        spinnerAmmo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Bullet selectedBullet = (Bullet) parent.getItemAtPosition(position);
                editVelocity.setText(String.format(Locale.US, "%.2f", selectedBullet.getVelocityMps()));

                if (selectedBullet.isCustom()) {
                    editCustomMass.setVisibility(View.VISIBLE);
                    editCustomMassUnit.setVisibility(View.VISIBLE);
                    textMassInfo.setText("커스텀 질량 입력 필요");
                } else {
                    editCustomMass.setVisibility(View.GONE);
                    editCustomMassUnit.setVisibility(View.GONE);
                    double massGrain = gramToGrain(selectedBullet.getMassGram());
                    textMassInfo.setText(String.format(Locale.US, "질량: %.2fg / %.0fgr",
                            selectedBullet.getMassGram(), massGrain));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 단위 전환
        btnToggleUnit.setOnClickListener(v -> {
            isMps = !isMps;
            btnToggleUnit.setText("단위 전환: " + (isMps ? "m/s ↔ fps" : "fps ↔ m/s"));
            editVelocity.setHint(isMps ? "속도 (m/s)" : "Velocity (fps)");
            editDistance.setHint(isMps ? "거리 (m)" : "Distance (ft)");
        });

        // 고급 설정 (중력)
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

        // 계산
        btnCalculate.setOnClickListener(v -> {
            Bullet selectedBullet = (Bullet) spinnerAmmo.getSelectedItem();
            if (selectedBullet == null) return;

            try {
                double velocity = Double.parseDouble(editVelocity.getText().toString());
                double angle = Double.parseDouble(editAngle.getText().toString());
                double angleRad = Math.toRadians(angle);

                double massGram = selectedBullet.getMassGram();
                if (selectedBullet.isCustom()) {
                    massGram = Double.parseDouble(editCustomMass.getText().toString());
                }

                if (!isMps) velocity = fpsToMps(velocity);

                // 거리 기반 도달 시간 계산용
                double horizontalSpeed = velocity * Math.cos(angleRad);
                double distance = 0.0;
                String distStr = editDistance.getText().toString();
                if (!distStr.isEmpty()) {
                    distance = Double.parseDouble(distStr);
                    if (!isMps) distance = fpsToMps(distance);
                }

                double time = (2 * velocity * Math.sin(angleRad)) / gravity;
                double range = Math.pow(velocity, 2) * Math.sin(2 * angleRad) / gravity;
                double maxHeight = Math.pow(velocity * Math.sin(angleRad), 2) / (2 * gravity);

                String additional = "";
                if (distance > 0) {
                    double timeToTarget = distance / horizontalSpeed;
                    additional = String.format(Locale.US, "\n🎯 %.0f m 도달 시간: %.2f 초", distance, timeToTarget);
                }

                String result = String.format(Locale.US,
                        "⏱ 비행 시간: %.2f 초\n📏 사거리: %.2f m\n📈 최고 높이: %.2f m%s",
                        time, range, maxHeight, additional);

                textResult.setText(result);
                textResult.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                Toast.makeText(this, "입력을 확인해주세요", Toast.LENGTH_SHORT).show();
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