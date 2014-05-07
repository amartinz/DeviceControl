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

}
