/*
 *  Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.namelessrom.devicecontrol.api;

/**
 * Include this file to your app, together with the permission in the manifest,
 * to make use of DeviceControl's API.
 */
interface IRemoteService {

//--------------------------------------------------------------------------------------------------
// CPU
//--------------------------------------------------------------------------------------------------
    void prepareCpuFreq();
    boolean isCpuFreqAvailable();
    List<String> getAvailableCpuFrequencies();
    String getMaxFrequency();
    String getMinFrequency();
    int getAvailableCores();
    void setMaxFrequency(in String value);
    void setMinFrequency(in String value);
//--------------------------------------------------------------------------------------------------
    void prepareGovernor();
    boolean isGovernorAvailable();
    List<String> getAvailableGovernors();
    String getCurrentGovernor();
    void setGovernor(in String value);
//--------------------------------------------------------------------------------------------------
// GPU
//--------------------------------------------------------------------------------------------------
    void prepareGpu();
    boolean isGpuAvailable();
    List<String> getAvailableGpuFrequencies();
    String getMaxGpuFrequency();
    String getCurrentGpuGovernor();
    void setMaxGpuFrequency(in String value);
    void setGpuGovernor(in String value);
//--------------------------------------------------------------------------------------------------
// Memory
//--------------------------------------------------------------------------------------------------
    long[] readMemory();
}
