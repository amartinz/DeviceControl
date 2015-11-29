package org.namelessrom.devicecontrol.tv;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v4.content.ContextCompat;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.tv.presenter.CardPresenter;

import alexander.martinz.libs.hardware.device.Device;
import alexander.martinz.libs.hardware.device.EmmcInfo;
import alexander.martinz.libs.hardware.device.KernelInfo;
import alexander.martinz.libs.hardware.device.MemoryInfo;

/**
 * Created by amartinz on 29.11.15.
 */
public class MainFragment extends BrowseFragment {
    private ArrayObjectAdapter mRowsAdapter;

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupUiElements();
        setupNavigation();
    }

    private void setupUiElements() {
        setTitle(getString(R.string.app_name_device_control));
        setBrandColor(ContextCompat.getColor(getActivity(), R.color.primary));
    }

    private void setupNavigation() {
        final Device device = Device.get(getActivity());
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        // start the async stuff before
        setupMemory();
        setupKernel();
        setupEmmc();

        setupPlatform(device);
        setupRuntime(device);
        setupDevice(device);

        setAdapter(mRowsAdapter);
    }

    private void setupPlatform(@NonNull final Device device) {
        HeaderItem cardHeader = new HeaderItem(0, getString(R.string.platform_information));

        CardPresenter cardPresenter = new CardPresenter();
        ArrayObjectAdapter cardRowAdapter = new ArrayObjectAdapter(cardPresenter);
        cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.version), device.platformVersion));
        cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.build_id), device.platformId));
        cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.type), device.platformType));
        cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.tags), device.platformTags));
        cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.build_date), device.platformBuildType));
        mRowsAdapter.add(new ListRow(cardHeader, cardRowAdapter));
    }

    private void setupRuntime(@NonNull final Device device) {
        HeaderItem cardHeader = new HeaderItem(0, getString(R.string.runtime));

        CardPresenter cardPresenter = new CardPresenter();
        ArrayObjectAdapter cardRowAdapter = new ArrayObjectAdapter(cardPresenter);
        cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.type), device.vmLibrary));
        cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.version), device.vmVersion));
        mRowsAdapter.add(new ListRow(cardHeader, cardRowAdapter));
    }

    private void setupDevice(@NonNull final Device device) {
        HeaderItem cardHeader = new HeaderItem(0, getString(R.string.device_information));

        CardPresenter cardPresenter = new CardPresenter();
        ArrayObjectAdapter cardRowAdapter = new ArrayObjectAdapter(cardPresenter);
        cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.android_id), device.androidId));
        cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.manufacturer), device.manufacturer));
        cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.device), device.device));
        cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.model), device.model));
        mRowsAdapter.add(new ListRow(cardHeader, cardRowAdapter));

        cardRowAdapter = new ArrayObjectAdapter(cardPresenter);
        cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.product), device.product));
        cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.board), device.board));
        cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.bootloader), device.bootloader));
        cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.radio_version), device.radio));
        cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.selinux), device.isSELinuxEnforcing
                ? getString(R.string.selinux_enforcing) : getString(R.string.selinux_permissive)));
        mRowsAdapter.add(new ListRow(cardRowAdapter));
    }

    private void setupMemory() {
        MemoryInfo.feedWithInformation(getActivity(), MemoryInfo.TYPE_MB, memoryInfo -> getActivity().runOnUiThread(() -> {
            HeaderItem cardHeader = new HeaderItem(0, getString(R.string.memory));

            CardPresenter cardPresenter = new CardPresenter();
            ArrayObjectAdapter cardRowAdapter = new ArrayObjectAdapter(cardPresenter);
            cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.total), MemoryInfo.getAsMb(memoryInfo.total)));
            cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.cached), MemoryInfo.getAsMb(memoryInfo.cached)));
            cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.free), MemoryInfo.getAsMb(memoryInfo.free)));
            mRowsAdapter.add(new ListRow(cardHeader, cardRowAdapter));
        }));
    }

    private void setupKernel() {
        KernelInfo.feedWithInformation(getActivity(), kernelInfo -> getActivity().runOnUiThread(() -> {
            HeaderItem cardHeader = new HeaderItem(0, getString(R.string.kernel));

            CardPresenter cardPresenter = new CardPresenter();
            ArrayObjectAdapter cardRowAdapter = new ArrayObjectAdapter(cardPresenter);
            cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.version),
                    String.format("%s %s", kernelInfo.version, kernelInfo.revision)));
            cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.extras), kernelInfo.extras));
            cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.toolchain), kernelInfo.toolchain));
            cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.build_date), kernelInfo.date));
            cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.host), kernelInfo.host));
            mRowsAdapter.add(new ListRow(cardHeader, cardRowAdapter));
        }));
    }

    private void setupEmmc() {
        EmmcInfo.feedWithInformation(getActivity(), emmcInfo -> getActivity().runOnUiThread(() -> {
            HeaderItem cardHeader = new HeaderItem(0, getString(R.string.emmc));

            CardPresenter cardPresenter = new CardPresenter();
            ArrayObjectAdapter cardRowAdapter = new ArrayObjectAdapter(cardPresenter);
            cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.name), emmcInfo.name));
            cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.emmc_cid), emmcInfo.cid));
            cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.emmc_mid), emmcInfo.mid));
            cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.emmc_rev), emmcInfo.rev));
            cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.emmc_date), emmcInfo.date));
            cardRowAdapter.add(new CardPresenter.CardDetail(getString(R.string.emmc_can_brick), emmcInfo.canBrick()
                    ? getString(R.string.emmc_can_brick_true) : getString(R.string.emmc_can_brick_false)));
            mRowsAdapter.add(new ListRow(cardHeader, cardRowAdapter));
        }));
    }
}
