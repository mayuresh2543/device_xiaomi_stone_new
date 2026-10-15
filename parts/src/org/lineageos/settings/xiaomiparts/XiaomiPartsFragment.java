/*
 * Copyright (C) 2025 KamiKaonashi
 * Copyright (C) 2026 The LineageOS Project
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

package org.lineageos.settings.xiaomiparts;

import android.content.Intent;
import android.os.Bundle;
import androidx.preference.Preference;
import com.android.settingslib.widget.SettingsBasePreferenceFragment;
import org.lineageos.settings.R;
import org.lineageos.settings.dirac.DiracActivity;
import org.lineageos.settings.display.DcDimmingSettingsActivity;
import org.lineageos.settings.fastcharge.FastChargeActivity;
import org.lineageos.settings.hbm.HBMActivity;
import org.lineageos.settings.kernelmanager.KernelManagerActivity;
import org.lineageos.settings.refreshrate.RefreshActivity;
import org.lineageos.settings.resolution.ResolutionActivity;
import org.lineageos.settings.resolution.SystemResolutionActivity;
import org.lineageos.settings.speaker.ClearSpeakerActivity;
import org.lineageos.settings.thermal.ThermalActivity;

public class XiaomiPartsFragment extends SettingsBasePreferenceFragment {

    private static final String KEY_DC_DIMMING = "dc_dimming";
    private static final String KEY_HBM = "hbm";
    private static final String KEY_REFRESH_RATE = "refresh_rate";
    private static final String KEY_UPSCALE = "upscale";
    private static final String KEY_SYSTEM_RESOLUTION = "system_resolution";
    private static final String KEY_DIRAC = "dirac";
    private static final String KEY_CLEAR_SPEAKER = "clear_speaker";
    private static final String KEY_FAST_CHARGE = "fast_charge";
    private static final String KEY_THERMAL = "thermal";
    private static final String KEY_KERNEL_MANAGER = "kernel_manager";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.xiaomi_parts_settings, rootKey);

        setupPreference(KEY_DC_DIMMING, DcDimmingSettingsActivity.class);
        setupPreference(KEY_HBM, HBMActivity.class);
        setupPreference(KEY_REFRESH_RATE, RefreshActivity.class);
        setupPreference(KEY_UPSCALE, ResolutionActivity.class);
        setupPreference(KEY_SYSTEM_RESOLUTION, SystemResolutionActivity.class);
        setupPreference(KEY_DIRAC, DiracActivity.class);
        setupPreference(KEY_CLEAR_SPEAKER, ClearSpeakerActivity.class);
        setupPreference(KEY_FAST_CHARGE, FastChargeActivity.class);
        setupPreference(KEY_THERMAL, ThermalActivity.class);
        setupPreference(KEY_KERNEL_MANAGER, KernelManagerActivity.class);
    }

    private void setupPreference(String key, Class<?> targetClass) {
        Preference pref = findPreference(key);
        if (pref != null) {
            pref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getActivity(), targetClass);
                startActivity(intent);
                return true;
            });
        }
    }
}
