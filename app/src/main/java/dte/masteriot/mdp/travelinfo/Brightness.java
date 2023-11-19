package dte.masteriot.mdp.travelinfo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.view.WindowManager;
import android.content.ContentResolver;
import android.os.Bundle;


public class Brightness implements SensorEventListener {
    private Context mContext;

    private SensorManager sensorManager;
    private Sensor lightSensor;
    boolean lightSensorIsActive;

    Brightness(Context context) {
        this.mContext = context;
        lightSensorIsActive = false;
        // Get the reference to the sensor manager and the sensor:
        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.unregisterListener(Brightness.this, lightSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT){
            float lightValue = sensorEvent.values[0];
            if (lightValue < 10) {
                // Ambiente muy oscuro, ajustar a un valor bajo
                setBrightness(0.1f);
            } else if (lightValue < 100) {
                // Ambiente con poca luz, ajustar a un valor moderado
                setBrightness(0.5f);
            } else {
                // Ambiente bien iluminado, ajustar a un valor alto
                setBrightness(1.0f);
            }

        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void setBrightness(float brightness) {
        // Verificar si el ajuste automático de brillo está habilitado y desactivarlo si es necesario
        try {
            int mode = Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        // Ajustar el brillo de la pantalla
        android.view.WindowManager.LayoutParams layoutParams = new android.view.WindowManager.LayoutParams();
        layoutParams.copyFrom(((android.app.Activity) mContext).getWindow().getAttributes());
        layoutParams.screenBrightness = brightness;
        ((android.app.Activity) mContext).getWindow().setAttributes(layoutParams);
    }





}
