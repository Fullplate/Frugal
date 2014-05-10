package fullplate.frugal.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.SortedMap;

import fullplate.frugal.R;
import fullplate.frugal.domain.Entry;
import fullplate.frugal.domain.PeriodSummary;
import fullplate.frugal.services.DomainService;

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
        return data.get(headers.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    private void createDeleteEntryDialog(final Entry entry) {
        AlertDialog.Builder adb = new AlertDialog.Builder(context);

        LinearLayout holder = new LinearLayout(context);
        holder.setOrientation(LinearLayout.VERTICAL);

        TextView label = StreamActivity.getInputDialogLabel(context, "Are you sure?");
        holder.addView(label);

        adb.setView(holder);

        adb.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DomainService.getService().removeEntry(entry);
                StreamActivity.updateStreamView((Activity) context);
                Toast.makeText(context, "Entry deleted!", Toast.LENGTH_SHORT).show();
            }
        });
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        adb.show();
    }

    private static class ChildViewHolder {
        TextView timestampView;
        TextView descriptionView;
        TextView amountView;
        ImageView deleteButton;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View row = convertView;
        ChildViewHolder viewHolder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(itemResourceId, parent, false);

            viewHolder = new ChildViewHolder();
            viewHolder.timestampView = (TextView) row.findViewById(R.id.stream_entry_timestamp);
            viewHolder.descriptionView = (TextView) row.findViewById(R.id.stream_entry_description);
            viewHolder.amountView = (TextView) row.findViewById(R.id.stream_entry_amount);
            viewHolder.deleteButton = (ImageView) row.findViewById(R.id.stream_entry_action_delete);

            row.setTag(viewHolder);
        }
        else {
            viewHolder = (ChildViewHolder) row.getTag();
        }

        final Entry entry = getChild(groupPosition, childPosition);

        viewHolder.timestampView.setText(entry.getTimestampString());
        viewHolder.descriptionView.setText(entry.getDescription());
        viewHolder.amountView.setText(context.getResources().getString(R.string.currency_symbol) + Integer.toString(entry.getAmount()));

        // setup the 'delete entry' action
        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDeleteEntryDialog(entry);
            }
        });

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

    private static class GroupViewHolder {
        TextView dateView;
        TextView amountView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View row = convertView;
        GroupViewHolder viewHolder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(headerResourceId, null);

            viewHolder = new GroupViewHolder();

            viewHolder.dateView = (TextView) row.findViewById(R.id.stream_header_date);
            viewHolder.amountView = (TextView) row.findViewById(R.id.stream_header_amount);

            row.setTag(viewHolder);
        }
        else {
            viewHolder = (GroupViewHolder) row.getTag();
        }

        final PeriodSummary periodSummary = getGroup(groupPosition);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
        String displayDate = dateFormat.format(periodSummary.getStartTimestamp());
        viewHolder.dateView.setText(displayDate);

        String currencySymbol = context.getResources().getString(R.string.currency_symbol);

        // conditionally display target if useDefaultTarget preference is true and the user set a target
        boolean useDefaultTarget = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("useDefaultPref", false);

        if (useDefaultTarget && periodSummary.getTarget() != 0) {
            viewHolder.amountView.setText(currencySymbol + Integer.toString(periodSummary.getCurrentAmount())
                    + " / " + currencySymbol + Integer.toString(periodSummary.getTarget()));

            // maximum amount exceeded
            if (periodSummary.getCurrentAmount() > periodSummary.getTarget()) {
                viewHolder.amountView.setTextColor(context.getResources().getColor(R.color.stream_header_amount_exceeded));
            }
        }
        else {
            viewHolder.amountView.setText(currencySymbol + Integer.toString(periodSummary.getCurrentAmount()));
        }

        // alternate styling for the active (first) header
        if (groupPosition == 0) {
            row.setBackgroundColor(context.getResources().getColor(R.color.orange_primary));
            viewHolder.dateView.setTypeface(null, Typeface.BOLD);
            viewHolder.amountView.setTypeface(null, Typeface.BOLD);
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
