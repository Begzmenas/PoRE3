package com.example.pore3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.client.mqttv3.*;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String IOT_HUB_HOSTNAME = "Begzmenas.azure-devices.net";
    private static final String DEVICE_ID = "device123";
    private static final String DEVICE_KEY = "randomGeneratedDeviceKey";
    private static final String TOPIC = "devices/" + DEVICE_ID + "/messages/events/";

    private MqttClient mqttClient;
    private SensorManager sensorManager;
    private TextView sensorDataTextView;
    private TextView statusTextView;
    private boolean isConnected = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorDataTextView = findViewById(R.id.sensorData);
        statusTextView = findViewById(R.id.status);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.e("SENSOR", "Accelerometer not found!");
            statusTextView.setText("Accelerometer not available.");
        }

        connectToAzureIoTHub();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            String sensorData = String.format(Locale.getDefault(), "Accelerometer: X=%.2f, Y=%.2f, Z=%.2f", x, y, z);
            sensorDataTextView.setText(sensorData);

            sendDataToAzureIoTHub(sensorData);
        }
    }

    private void sendDataToAzureIoTHub(String data) {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                MqttMessage message = new MqttMessage(data.getBytes());
                mqttClient.publish(TOPIC, message);
                Log.d("MQTT", "Message sent: " + data);
            }
        } catch (MqttException e) {
            Log.e("MQTT", "Failed to send message", e);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}