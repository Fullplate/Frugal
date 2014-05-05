package fullplate.frugal.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.SortedMap;

import fullplate.frugal.R;
import fullplate.frugal.domain.Entry;
import fullplate.frugal.domain.PeriodSummary;
import fullplate.frugal.domain.SingleEntry;

public class ExpandableStreamAdapter extends BaseExpandableListAdapter {

    Context context;
    int itemResourceId;
    int headerResourceId;
    ArrayList<PeriodSummary> headers;
    SortedMap<PeriodSummary, ArrayList<Entry>> data;

    public ExpandableStreamAdapter(Context context, int itemResourceId, int headerResourceId, ArrayList<PeriodSummary> headers, SortedMap<PeriodSummary, ArrayList<Entry>> data) {
        this.itemResourceId = itemResourceId;
        this.headerResourceId = headerResourceId;
        this.context = context;
        this.headers = headers;
        this.data = data;
    }

    public Entry getChild(int groupPosition, int childPosition) {
        return data.get(headers.get(groupPosition)).get(childPosition); // yuck
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View row = convertView;
        Entry entry = getChild(groupPosition, childPosition);

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(itemResourceId, parent, false);

        if (entry instanceof SingleEntry) {
            TextView timestampView = (TextView) row.findViewById(R.id.stream_entry_timestamp);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");
            String displayDate = dateFormat.format(entry.getTimestamp());
            timestampView.setText(displayDate);
        }

        TextView descriptionView = (TextView) row.findViewById(R.id.stream_entry_description);
        descriptionView.setText(entry.getDescription());

        TextView amountView = (TextView) row.findViewById(R.id.stream_entry_amount);
        amountView.setText("$"+Integer.toString(entry.getAmount()));

        return row;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return data.get(headers.get(groupPosition)).size();
    }

    public PeriodSummary getGroup(int groupPosition) {
        return headers.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return headers.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View row = convertView;
        PeriodSummary periodSummary = getGroup(groupPosition);

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(headerResourceId, null);

        TextView dateTextView = (TextView) row.findViewById(R.id.stream_header_date);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
        String displayDate = dateFormat.format(periodSummary.getStartTimestamp());
        dateTextView.setText(displayDate);

        TextView amountTextView = (TextView) row.findViewById(R.id.stream_header_amount);

        boolean useDefaultAmount = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("useDefaultPref", false);
        if (useDefaultAmount) {
            amountTextView.setText("$" + Integer.toString(periodSummary.getCurrentAmount()) + " / $" + Integer.toString(periodSummary.getMaximumAmount()));

            // maximum amount exceeded
            if (periodSummary.getCurrentAmount() > periodSummary.getMaximumAmount()) {
                amountTextView.setTextColor(context.getResources().getColor(R.color.stream_header_amount_exceeded));
            }
        }
        else {
            amountTextView.setText("$"+Integer.toString(periodSummary.getCurrentAmount()));
        }

        // alternate styling for the active (first) header
        if (groupPosition == 0) {
            row.setBackgroundColor(context.getResources().getColor(R.color.orange_primary));
            dateTextView.setTypeface(null, Typeface.BOLD);
            amountTextView.setTypeface(null, Typeface.BOLD);
        }
        else {
            row.setBackgroundColor(context.getResources().getColor(R.color.orange_secondary));
        }

        return row;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
