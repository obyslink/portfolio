package tech.dappworld.webpagetoappconverter.drawer;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import tech.dappworld.webpagetoappconverter.Config;
import tech.dappworld.webpagetoappconverter.R;

import java.util.ArrayList;

public class DrawerAdapter extends ArrayAdapter<String> {

    //The current position
    private int selectedPos = 0;

    public DrawerAdapter(Context context, ArrayList<String> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        String item = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.drawer_row, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.title);
        ImageView imgViewIcon = (ImageView) convertView.findViewById(R.id.iconView);

        //If this is the current position, highlight it
        if (selectedPos == position) {
            convertView.setBackgroundResource(R.color.drawerSelectedItem);
        } else {
            TypedValue outValue = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            convertView.setBackgroundResource(outValue.resourceId);
        }

        //If a drawable is provided, set it as compound drawable
        if (Config.ICONS.length > position && Config.ICONS[position] != 0) {
            //tvName.setCompoundDrawablesWithIntrinsicBounds(
              //      ContextCompat.getDrawable(this.getContext(), Config.ICONS[position]), null, null, null);
            imgViewIcon.setImageResource(Config.ICONS[position]);
            imgViewIcon.setVisibility(View.VISIBLE);
        } else {
            //tvName.setCompoundDrawables(
            //        null, null, null, null);
            imgViewIcon.setImageResource(0);
            imgViewIcon.setVisibility(View.GONE);
        }

        // Populate the data into the template view using the data object
        tvName.setText(item);

        // Return the completed view to render on screen
        return convertView;
    }

    //Set the current position
    public void setSelectedPosition(int pos) {
        selectedPos = pos;
        notifyDataSetChanged();
    }
}
