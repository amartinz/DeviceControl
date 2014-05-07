package org.namelessrom.devicecontrol.api;

/**
 * Created by alex on 07.05.14.
 */
interface IRemoteService {

    boolean isCpuFreqAvailable();
    void prepareCpuFreq();
    List<String> getAvailableCpuFrequencies();

}
