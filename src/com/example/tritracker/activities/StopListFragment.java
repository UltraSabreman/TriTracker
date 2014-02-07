package com.example.tritracker.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.example.tritracker.R;
import com.example.tritracker.Sorter;
import com.example.tritracker.Stop;
import com.example.tritracker.Timer;
import com.example.tritracker.Util;
import com.example.tritracker.Util.ListType;
import com.example.tritracker.arrayadaptors.StopListArrayAdaptor;
import com.example.tritracker.json.ForgroundRequestManager;
import com.example.tritracker.json.ForgroundRequestManager.ResultCallback;
import com.example.tritracker.notmycode.SwipeDismissListViewTouchListener;
import com.example.tritracker.notmycode.UndoBarController;
import com.example.tritracker.notmycode.UndoBarController.UndoListener;

import java.util.ArrayList;

public class StopListFragment extends Fragment implements UndoListener {
	private UndoBarController mUndoBarController;
	private View ourView;
	private StopListArrayAdaptor adaptor;
	private ArrayList<Stop> undoList = new ArrayList<Stop>();
	private ArrayList<Stop> stopList = new ArrayList<Stop>();
	private boolean isFavorites;
	
	private MainService theService;	
	
	public StopListFragment(boolean t) {
		isFavorites = t;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ourView = inflater.inflate(R.layout.main_stop_list, container, false);
		mUndoBarController = new UndoBarController(ourView.findViewById(R.id.undobar), this);

		theService = MainService.getService();
		
		((TextView) ourView.findViewById(R.id.NoMembers)).setText("Your " + (isFavorites ? "Favorites List" : "History") + " is Empty.");

        stopList = isFavorites ? theService.getFavorties() : theService.getHistory();
		
		new Sorter<Stop>(Stop.class).sort(isFavorites ? ListType.Favorites : ListType.History, stopList, null);
		adaptor = new StopListArrayAdaptor(getActivity().getApplicationContext(), stopList, isFavorites);
		
		setListeners();
		update(true);
		
		return ourView;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_action_sort_list:
				final ArrayList<Stop> tempStops = isFavorites ? theService.getFavorties() : theService.getHistory();
				new Sorter<Stop>(Stop.class).sortUI(getActivity(), isFavorites ? ListType.Favorites : ListType.History,	tempStops,
						new Timer.onUpdate() {
							public void run() {
								update(true);
								theService.doUpdate(false);
							}
						});
				return true;
			case R.id.menu_action_clear_list:
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Are you sure you want to clear your " + (isFavorites ? "Favorites" : "History") + "?");

				builder.setPositiveButton("Ok", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						for (Stop s : stopList)						
							if (isFavorites)
								s.inFavorites = false;
							else
								s.inHistory = false;

						update(true);
						theService.doUpdate(false);
					}
				});
				
				builder.setNegativeButton("Cancel", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				});
				
				builder.create().show();
				
				return true;
			default:
				
		}
		theService.doUpdate(false);
		adaptor.notifyDataSetChanged();
		return super.onContextItemSelected(item);
	}
	
	
	@Override
	public void onStart() {
		theService.sub(isFavorites ? "Favorites" : "History", new Timer.onUpdate() {
			public void run() {
				update(true);
			}
		});
        update(false);
		super.onStart();
	}

    @Override
    public void onResume() {
        update(false);
        super.onResume();
    }
	
	@Override
	public void onStop() {
		theService.unsub(isFavorites ? "Favorites" : "History");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		if (adaptor != null)
			adaptor.notifyDataSetInvalidated();		
		super.onDestroy();
	}
	
	private void getJson(int stop) {
		Util.createSpinner(getActivity());
		ResultCallback call = new ForgroundRequestManager.ResultCallback() { 
			public void run(Stop s) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Util.hideSpinner();
					}
				});
				
				Context c = getActivity().getApplicationContext();
				
				Intent tempIntent = new Intent(c, StopDetailsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				tempIntent.putExtra("stop", s.StopID);
				
				c.startActivity(tempIntent);
				theService.doUpdate(false);
			}
		};
		
		new ForgroundRequestManager(call, getActivity(), getActivity().getApplicationContext(), stop).start();
		
	}
	
	private void setListeners() {
		EditText edit = (EditText)  getActivity().findViewById(R.id.UIStopIDBox);
		ListView view = (ListView) ourView.findViewById(R.id.UIStopList);
		RelativeLayout layout = (RelativeLayout) ourView.findViewById(R.id.longClickCatcher);
		
		registerForContextMenu(layout);
		view.setAdapter(adaptor);
		
		if (view.getFooterViewsCount() == 0)
			view.addFooterView(getActivity().getLayoutInflater().inflate(R.layout.misc_seperator, null), null, true);

		edit.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE)) {
					EditText edit = (EditText) getActivity().findViewById(R.id.UIStopIDBox);
					String text = edit.getText().toString();
					
					if (text != null && text.compareTo("") != 0) 
						getJson(Integer.parseInt(text));
		
					edit.getText().clear();

				}
				return false;
			}
		});	
						
		OnLongClickListener longClick = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				getActivity().openContextMenu(v);
				return true;
			}
		};
		
		layout.setOnLongClickListener(longClick);
		view.setOnLongClickListener(longClick);
				
		layout.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				menu.add(0, R.id.menu_action_sort_list, ContextMenu.NONE, "Sort " + (isFavorites ? "Favorites" : "History"));
				menu.add(0, R.id.menu_action_clear_list, ContextMenu.NONE, "Clear " + (isFavorites ? "Favorites" : "History"));	
			}
		});  
		
		view.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,	int position, long arg3) {
				Stop temp = adaptor.getItem(position);
				if (temp != null) 
					getJson(temp.StopID);
			}
		});

		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
				view, new SwipeDismissListViewTouchListener.DismissCallbacks() {
					@Override
					public boolean canDismiss(int position) {
						return true;
					}

					@Override
					public void onDismiss(ListView listView, int[] reverseSortedPositions) {
						for (int position : reverseSortedPositions) {
							Stop stop = adaptor.getItem(position);
							if (stop != null) {
								undoList.add(stop);
								stopList.remove(stop);
								if (isFavorites)
									stop.inFavorites = false;
								else
									stop.inHistory = false;

								mUndoBarController.showUndoBar(false,
										"Removed "
										+ undoList.size()
										+ " Stop"
										+ (undoList.size() > 1 ? "s": ""), null);
							}
						}
						update(true);
					}
				});
		view.setOnTouchListener(touchListener);
		view.setOnScrollListener(touchListener.makeScrollListener());
	}

	public void update(boolean get) {
        if (theService == null)
            theService = MainService.getService();

        if (get) {
            stopList.clear();
            stopList = isFavorites ? theService.getFavorties() : theService.getHistory();
        }

		if (getActivity() != null)
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {					
					if (stopList == null || stopList.size() == 0)
						((TextView) ourView.findViewById(R.id.NoMembers)).setVisibility(View.VISIBLE);
					else
						((TextView) ourView.findViewById(R.id.NoMembers)).setVisibility(View.INVISIBLE);


					if (adaptor != null) {
                        adaptor.clear();
                        adaptor.addAll(stopList);
						adaptor.notifyDataSetChanged();
                    }
                }
			});
	}


	@Override
	public void onUndo(Parcelable token, boolean fail) {
		if (!fail) {
			for (Stop s : undoList) {
				if (isFavorites)
					s.inFavorites = true;
				else
					s.inHistory = true;
				stopList.add(s);
			}
			undoList.clear();
		} else {
			for (Stop s : undoList) {
				if (!s.inFavorites && !s.inHistory)
					theService.removeStop(s);
			}
			undoList.clear();
		}
		update(true);
	}

}
