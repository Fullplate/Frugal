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

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View row = convertView;
        final Entry entry = getChild(groupPosition, childPosition);

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(itemResourceId, parent, false);

        TextView timestampView = (TextView) row.findViewById(R.id.stream_entry_timestamp);
        timestampView.setText(entry.getTimestampString());

        TextView descriptionView = (TextView) row.findViewById(R.id.stream_entry_description);
        descriptionView.setText(entry.getDescription());

        TextView amountView = (TextView) row.findViewById(R.id.stream_entry_amount);
        amountView.setText("$"+Integer.toString(entry.getAmount()));

        // setup the 'set target' action
        ImageView deleteButton = (ImageView) row.findViewById(R.id.stream_entry_action_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
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

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View row = convertView;
        final PeriodSummary periodSummary = getGroup(groupPosition);

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(headerResourceId, null);

        TextView dateTextView = (TextView) row.findViewById(R.id.stream_header_date);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
        String displayDate = dateFormat.format(periodSummary.getStartTimestamp());
        dateTextView.setText(displayDate);

        TextView amountTextView = (TextView) row.findViewById(R.id.stream_header_amount);

        String currencySymbol = context.getResources().getString(R.string.currency_symbol);

        // conditionally display target if useDefaultTarget preference is true and the user set a target
        boolean useDefaultTarget = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("useDefaultPref", false);

        if (useDefaultTarget || periodSummary.getTarget() != -1) {
            amountTextView.setText(currencySymbol + Integer.toString(periodSummary.getCurrentAmount())
                    + " / " + currencySymbol + Integer.toString(periodSummary.getTarget()));

            // maximum amount exceeded
            if (periodSummary.getCurrentAmount() > periodSummary.getTarget()) {
                amountTextView.setTextColor(context.getResources().getColor(R.color.stream_header_amount_exceeded));
            }
        }
        else {
            amountTextView.setText(currencySymbol + Integer.toString(periodSummary.getCurrentAmount()));
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
