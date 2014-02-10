package com.example.tritracker.arrayadaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tritracker.R;
import com.example.tritracker.activities.MainService;
import com.example.tritracker.json.AllRoutesJSONResult.ResultSet.Route.Dir;

import java.util.ArrayList;

public class RouteDirListArrayAdaptor extends ArrayAdapter<Dir> {
	// private final Context context;
	private MainService theService;

	public RouteDirListArrayAdaptor(Context context, ArrayList<Dir> l) {
		super(context, R.layout.search_dir, l);
		this.theService = MainService.getService();
	}


	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.search_dir, null);
		}

		Dir curDir = getItem(position);

		if (curDir != null) {
			TextView Direction = (TextView) v.findViewById(R.id.Dirname);
			TextView LineName = (TextView) v.findViewById(R.id.LineName);

			LineName.setText(curDir.desc);
			LineName.setSelected(true);

			Direction.setText(curDir.dir == 1 ? "Inbound" : "Outbound");
		}

		return v;
	}
}
