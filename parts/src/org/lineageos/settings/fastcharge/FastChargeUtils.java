/*
 * Copyright (C) 2025 KamiKaonashi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.fastcharge;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;
import androidx.preference.PreferenceManager;

import org.lineageos.settings.utils.FileUtils;

public class FastChargeUtils {

    private static final String TAG = "FastChargeUtils";
    public static final String NORMAL_CHARGE_NODE = "/sys/kernel/fastchgtoggle/mode";
    public static final String USB_CHARGE_NODE = "/sys/kernel/fast_charge/force_fast_charge";
    public static final String THERMAL_BOOST_NODE = "/sys/kernel/fastchgtoggle/thermals";
    public static final String BYPASS_CHARGE_NODE = "/sys/class/qcom-battery/input_suspend";
    
    private static final String PREF_NORMAL_CHARGE = "fastcharge_normal";
    private static final String PREF_USB_CHARGE = "fastcharge_usb";
    private static final String PREF_THERMAL_BOOST = "thermal_boost";
    private static final String PREF_BYPASS_CHARGE = "bypass_charge";

    // Charging modes
    public static final int MODE_30W = 2;
    public static final int MODE_15W = 1;
    public static final int MODE_8W = 0;

    // Bypass modes
    public static final int BYPASS_DISABLED = 0;
    public static final int BYPASS_ENABLED = 1;

    // Charging Control settings
    private static final String KEY_CHARGING_CONTROL_ENABLED = "charging_control_enabled";
    private static final String KEY_CHARGING_CONTROL_MODE = "charging_control_mode";
    private static final String KEY_CHARGING_CONTROL_LIMIT = "charging_control_charging_limit";

    // From Lineage HealthInterface
    private static final int MODE_AUTO = 1;
    private static final int MODE_LIMIT = 3;

    private static final int CC_LIMIT_MIN = 10;
    private static final int CC_LIMIT_MAX = 100;
    private static final int CC_LIMIT_DEF = 80;

    private SharedPreferences mSharedPrefs;
    private ContentResolver mContentResolver;

    public FastChargeUtils(Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mContentResolver = context.getContentResolver();
    }

    public int getNormalFastChargeMode() {
        try {
            String value = FileUtils.readOneLine(NORMAL_CHARGE_NODE);
            return value != null ? Integer.parseInt(value) : MODE_30W;
        } catch (Exception e) {
            Log.e(TAG, "Failed to read normal fast charge status", e);
            return MODE_30W;
        }
    }

    public boolean isUsbFastChargeEnabled() {
        try {
            String value = FileUtils.readOneLine(USB_CHARGE_NODE);
            return value != null && value.equals("1");
        } catch (Exception e) {
            Log.e(TAG, "Failed to read USB fast charge status", e);
            return false;
        }
    }

    public boolean isThermalBoostEnabled() {
        try {
            String value = FileUtils.readOneLine(THERMAL_BOOST_NODE);
            return value != null && value.equals("1");
        } catch (Exception e) {
            Log.e(TAG, "Failed to read thermal boost status", e);
            return false;
        }
    }

    public boolean isBypassChargeEnabled() {
        try {
            String value = FileUtils.readOneLine(BYPASS_CHARGE_NODE);
            return value != null && value.equals("1");
        } catch (Exception e) {
            Log.e(TAG, "Failed to read bypass charge status", e);
            return false;
        }
    }

    public void setNormalFastChargeMode(String mode) {
        try {
            FileUtils.writeLine(NORMAL_CHARGE_NODE, mode);
            mSharedPrefs.edit().putString(PREF_NORMAL_CHARGE, mode).apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to write normal fast charge status", e);
        }
    }

    public void enableUsbFastCharge(boolean enable) {
        try {
            FileUtils.writeLine(USB_CHARGE_NODE, enable ? "1" : "0");
            mSharedPrefs.edit().putBoolean(PREF_USB_CHARGE, enable).apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to write USB fast charge status", e);
        }
    }

    public void enableThermalBoost(boolean enable) {
        try {
            FileUtils.writeLine(THERMAL_BOOST_NODE, enable ? "1" : "0");
            mSharedPrefs.edit().putBoolean(PREF_THERMAL_BOOST, enable).apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to write thermal boost status", e);
        }
    }

    public void enableBypassCharge(boolean enable) {
        try {
            if (enable) {
                // Backup current Charging Control settings
                backupChargingControlSettings();
                
                // Configure Charging Control for bypass mode
                setChargingControlEnabled(true);
                setChargingControlMode(MODE_LIMIT);
                setChargingControlLimit(CC_LIMIT_MIN); // Set to minimum to enable bypass
                
                // Enable bypass charging
                FileUtils.writeLine(BYPASS_CHARGE_NODE, "1");
            } else {
                // Disable bypass charging first
                FileUtils.writeLine(BYPASS_CHARGE_NODE, "0");
                
                // Restore previous Charging Control settings
                restoreChargingControlSettings();
            }
            
            mSharedPrefs.edit().putBoolean(PREF_BYPASS_CHARGE, enable).apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to write bypass charge status", e);
        }
    }
    
    public boolean isNodeAccessible(String node) {
        try {
            String value = FileUtils.readOneLine(node);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Node " + node + " not accessible", e);
            return false;
        }
    }
    
    public boolean isThermalBoostSupported() {
        return isNodeAccessible(THERMAL_BOOST_NODE);
    }

    public boolean isBypassChargeSupported() {
        return isNodeAccessible(BYPASS_CHARGE_NODE);
    }

    // Charging Control helper methods
    private boolean isChargingControlEnabled() {
        return Settings.System.getInt(mContentResolver,
                KEY_CHARGING_CONTROL_ENABLED, 0) != 0;
    }

    private void setChargingControlEnabled(boolean enabled) {
        Settings.System.putInt(mContentResolver,
                KEY_CHARGING_CONTROL_ENABLED, enabled ? 1 : 0);
    }

    private int getChargingControlMode() {
        return Settings.System.getInt(mContentResolver,
                KEY_CHARGING_CONTROL_MODE, MODE_AUTO);
    }

    private void setChargingControlMode(int mode) {
        Settings.System.putInt(mContentResolver,
                KEY_CHARGING_CONTROL_MODE, mode);
    }

    private int getChargingControlLimit() {
        return Settings.System.getInt(mContentResolver,
                KEY_CHARGING_CONTROL_LIMIT, CC_LIMIT_DEF);
    }

    private void setChargingControlLimit(int limit) {
        if (limit < CC_LIMIT_MIN || limit > CC_LIMIT_MAX) {
            return;
        }
        Settings.System.putInt(mContentResolver,
                KEY_CHARGING_CONTROL_LIMIT, limit);
    }

    // Backup and restore methods for Charging Control settings
    private void backupChargingControlSettings() {
        mSharedPrefs.edit()
                .putInt("backup_" + KEY_CHARGING_CONTROL_MODE, getChargingControlMode())
                .putInt("backup_" + KEY_CHARGING_CONTROL_LIMIT, getChargingControlLimit())
                .putBoolean("backup_" + KEY_CHARGING_CONTROL_ENABLED, isChargingControlEnabled())
                .apply();
    }

    private void restoreChargingControlSettings() {
        setChargingControlLimit(mSharedPrefs.getInt(
                "backup_" + KEY_CHARGING_CONTROL_LIMIT, CC_LIMIT_DEF));
        setChargingControlMode(mSharedPrefs.getInt(
                "backup_" + KEY_CHARGING_CONTROL_MODE, MODE_AUTO));
        setChargingControlEnabled(mSharedPrefs.getBoolean(
                "backup_" + KEY_CHARGING_CONTROL_ENABLED, false));
    }
}
