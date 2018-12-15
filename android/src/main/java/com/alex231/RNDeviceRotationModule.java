
package com.alex231;

import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.LifecycleEventListener;

import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.Sensor;

public class RNDeviceRotationModule extends ReactContextBaseJavaModule implements SensorEventListener, LifecycleEventListener {

  private final ReactApplicationContext reactContext;
  private final SensorManager mSensorManager;
  private final Sensor mAccelerometer;
  private final Sensor mGeomagneticSensor;
  private float[] mGravity, mGeomagnetic;

  public RNDeviceRotationModule(ReactApplicationContext reactContext) {
    super(reactContext);
    mSensorManager = (SensorManager)reactContext.getSystemService(reactContext.SENSOR_SERVICE);
    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    mGeomagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    this.reactContext = reactContext;
    inactive = false;
    reactContext.addLifecycleEventListener(this);
    mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.registerListener(this, mGeomagneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
  }

  @Override
  public String getName() {
    return "RNDeviceRotation";
  }

  boolean inactive;
  float azimut, pitch, roll;

  @ReactMethod
  public WritableMap getDeviceRotation(Callback success, Callback error) {
    return getRotationMap();
  }

  public WritableMap getRotationMap() {
    WritableMap rotationMap = Arguments.createMap();
    rotationMap.putDouble("azimut", azimut);
    rotationMap.putDouble("pitch", pitch);
    rotationMap.putDouble("roll", roll);
    return rotationMap;
  }

  private void sendRotatioChangedEvent() {
    reactContext.getJSModule(RCTDeviceEventEmitter.class)
            .emit("deviceRotationDidChange", getRotationMap());
  }

  @Override
  public void onHostResume() {
    inactive = false;
    mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.registerListener(this, mGeomagneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
  }

  @Override
  public void onHostPause() {
    inactive = true;
    mSensorManager.unregisterListener(this);
  }

  @Override
  public void onHostDestroy() {
    mSensorManager.unregisterListener(this);
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
      mGravity = event.values;

    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
      mGeomagnetic = event.values;

    if (mGravity != null && mGeomagnetic != null) {
      float R[] = new float[9];
      float I[] = new float[9];

      boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
      if (success) {
          float orientation[] = new float[3];
          SensorManager.getOrientation(R, orientation);
          azimut = orientation[0]; // orientation contains: azimut, pitch and roll
          pitch = orientation[1];
          roll = orientation[2];
          sendRotatioChangedEvent();
      }
    }
  }

  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }
}