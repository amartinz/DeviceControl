package org.namelessrom.devicecontrol.widgets.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.objects.FlashItem;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import java.util.ArrayList;
import java.util.List;

import static butterknife.ButterKnife.findById;

/**
 * Created by alex on 22.06.14.
 */
public class FlashListAdapter extends BaseAdapter {

    private List<FlashItem> flashItemList;

    public FlashListAdapter() { flashItemList = new ArrayList<FlashItem>(); }

    public FlashListAdapter(final List<FlashItem> flashItems) { this.flashItemList = flashItems; }

    @Override public int getCount() { return flashItemList.size(); }

    @Override public Object getItem(final int position) { return flashItemList.get(position); }

    @Override public long getItemId(final int position) { return 0; }


    private static final class ViewHolder {
        private final View     rootView;
        private final TextView filePath;
        private final TextView fileName;

        private ViewHolder(final View rootView) {
            this.rootView = rootView;
            this.filePath = findById(rootView, R.id.flash_path);
            this.fileName = findById(rootView, R.id.flash_name);
        }
    }

    @Override public View getView(final int position, View v, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (v == null) {
            v = Application.getLayoutInflater().inflate(R.layout.list_item_flasher, parent, false);
            assert (v != null);
            viewHolder = new ViewHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        final FlashItem item = flashItemList.get(position);

        viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BusProvider.getBus().post(item);
            }
        });
        viewHolder.fileName.setText(item.getName());
        viewHolder.filePath.setText(item.getPath());

        return v;
    }
}
