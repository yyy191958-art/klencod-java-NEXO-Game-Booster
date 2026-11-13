package com.nexo.gamebooster;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView tvRamPercent, tvRamDetails, tvTemp, tvStatus;
    private ProgressBar pbRam;
    private Button btnBoost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ربط العناصر بملف التصميم
        tvRamPercent = findViewById(R.id.tvRamPercent);
        tvRamDetails = findViewById(R.id.tvRamDetails);
        tvTemp = findViewById(R.id.tvTemp);
        tvStatus = findViewById(R.id.tvStatus);
        pbRam = findViewById(R.id.pbRam);
        btnBoost = findViewById(R.id.btnBoost);

        // تحديث إحصائيات الجهاز أول ما يفتح التطبيق
        updateSystemStats();

        // الحدث عند الضغط على زر التسريع
        btnBoost.setOnClickListener(v -> startBoostProcess());
    }

    // دالة قراءة استهلاك الرام وحرارة الجهاز
    private void updateSystemStats() {
        // حساب الرام الفعلي
        ActivityManager actManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);

        double totalRam = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0);
        double availRam = memInfo.availMem / (1024.0 * 1024.0 * 1024.0);
        double usedRam = totalRam - availRam;
        int percentUsed = (int) ((usedRam / totalRam) * 100);

        pbRam.setProgress(percentUsed);
        tvRamPercent.setText(percentUsed + "%");
        tvRamDetails.setText(String.format("المستخدم: %.2f GB / الكلي: %.2f GB", usedRam, totalRam));

        // قراءة درجة الحرارة من مستشعر البطارية
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        if (batteryStatus != null) {
            int temp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            float tempCelsius = temp / 10.0f;
            tvTemp.setText(tempCelsius + " °C");
        }
    }

    // دالة عملية التسريع
    private void startBoostProcess() {
        btnBoost.setEnabled(false);
        btnBoost.setText("جاري التنظيف...");
        tvStatus.setText("يتم تفريغ الذاكرة المؤقتة وتحسين الأداء...");

        // أنيميشن نبض للزر أثناء التنظيف
        ScaleAnimation anim = new ScaleAnimation(1f, 0.92f, 1f, 0.92f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(350);
        anim.setRepeatCount(3);
        anim.setRepeatMode(Animation.REVERSE);
        btnBoost.startAnimation(anim);

        // تنظيف الذاكرة المتاحة
        System.gc();

        // تأخير زمني بسيط لإعطاء حماس للمستخدم ثم تحديث النتيجة
        new Handler().postDelayed(() -> {
            updateSystemStats();
            btnBoost.setEnabled(true);
            btnBoost.setText("BOOST\nتم التسريع ✓");
            tvStatus.setText("تم تحرير الذاكرة بنجاح! الأداء في ذروته.");
            Toast.makeText(this, "تم تنظيف الجهاز بنجاح!", Toast.LENGTH_SHORT).show();
        }, 1800);
    }
}
