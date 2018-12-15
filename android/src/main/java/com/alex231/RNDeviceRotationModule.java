
package com.alex231;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.modules.appstate;

import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RNDeviceRotationModule extends ReactContextBaseJavaModule implements SensorEventListener {

  private final ReactApplicationContext reactContext;

  public RNDeviceRotationModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNDeviceRotation";
  }

  boolean inactive = true;
  float azimut, pitch, roll;

  @ReactMethod
  public float[] getDeviceRotation(Callback success, Callback error) {
    return rotationMap();
  }

  public WritableMap getRotationMap() {
    WritableMap rotationMap = Arguments.createMap();
    rotationMap.putString("azimut", azimut);
    rotationMap.putString("pitch", pitch);
    rotationMap.putString("roll", roll);
    return rotationMap;
  }

  private void sendAppStateChangeEvent() {
    getReactApplicationContext().getJSModule(RCTDeviceEventEmitter.class)
            .emit("deviceRotationDidChange", getRotationMap());
  }

  @Override
  public void initialize() {
    inactive = false;
    getReactApplicationContext().addLifecycleEventListener(this);
  }

  @Override
  public void onHostResume() {
    inactive = false;
    mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
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
      }
    }
  }

  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }

  public void onSensorChanged(SensorEvent event) {
  }
}