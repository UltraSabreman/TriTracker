package com.example.tritracker.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import com.example.tritracker.activities.MainService.LocalBinder;
import com.example.tritracker.arrayadaptors.StopListArrayAdaptor;
import com.example.tritracker.json.ForgroundRequestManager;
import com.example.tritracker.json.ForgroundRequestManager.ResultCallback;
import com.example.tritracker.notmycode.SwipeDismissListViewTouchListener;
import com.example.tritracker.notmycode.UndoBarController;
import com.example.tritracker.notmycode.UndoBarController.UndoListener;

public class StopListActivity extends Fragment implements UndoListener {
	private UndoBarController mUndoBarController;
	private View ourView;
	private StopListArrayAdaptor adaptor;
	private ArrayList<Stop> undoList = new ArrayList<Stop>();
	private ArrayList<Stop> stopList = new ArrayList<Stop>();
	private boolean isFavorites;
	
	private MainService theService;
	private boolean bound;
	
	@Override 
	public void onStart() {
		super.onStart();
		Intent intent = new Intent(getActivity(), MainService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);    
        
        stopList = isFavorites ? theService.getFavorties() : theService.getHistory();
		
		new Sorter<Stop>(Stop.class, theService).sort(isFavorites ? ListType.Favorites : ListType.History, stopList, null);
		adaptor = new StopListArrayAdaptor(theService, getActivity().getApplicationContext(), stopList, isFavorites);
		
		setListeners();
		update(null);
		
		theService.sub(isFavorites ? "Favorites" : "History", new Timer.onUpdate() {
			public void run() {
				update(null);
			}
		});

	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_sort_list:
				final ArrayList<Stop> tempStops = isFavorites ? theService.getFavorties() : theService.getHistory();
				new Sorter<Stop>(Stop.class, theService).sortUI(getActivity(), isFavorites ? ListType.Favorites : ListType.History,	tempStops,
						new Timer.onUpdate() {
							public void run() {
								update(tempStops);
								theService.doUpdate(false);
							}
						});
				return true;
			case R.id.action_clear_list:
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
						
						if (isFavorites)
							update(theService.getFavorties());
						else
							update(theService.getHistory());
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
	public void onResume() {
		super.onResume();
		
	}
	
	@Override
	public void onStop() {
		theService.unsub(isFavorites ? "Favorites" : "History");
		if (bound)
			getActivity().unbindService(mConnection);
		super.onStop();
	}

	@Override
	public void onDestroy() {
		if (adaptor != null)
			adaptor.notifyDataSetInvalidated();		
		super.onDestroy();
	}
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            theService = binder.getService();
            bound = true;
        }

        
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
	
	public StopListActivity(MainService service, boolean t) {
		isFavorites = t;
		theService = service;
	}
	
	private void getJson(int stop) {
		Util.creatDiag(getActivity());
		ResultCallback call = new ForgroundRequestManager.ResultCallback() { 
			public void run(Stop s) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Util.hideDiag();
					}
				});
				
				Context c = getActivity().getApplicationContext();
				
				Intent tempIntent = new Intent(c, StopDetailsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				tempIntent.putExtra("stop", s);
				
				c.startActivity(tempIntent);
				theService.doUpdate(false);
			}
		};
		
		new ForgroundRequestManager(theService, call, getActivity(), getActivity().getApplicationContext(), stop).start();
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ourView = inflater.inflate(R.layout.stop_list_layout, container, false);
		mUndoBarController = new UndoBarController(ourView.findViewById(R.id.undobar), this);
		
		((TextView) ourView.findViewById(R.id.NoMembers)).setText("Your " + (isFavorites ? "Favorites List" : "History") + " is Empty.");

		setListeners();
		return ourView;
	}
	
	private void setListeners() {
		EditText edit = (EditText)  getActivity().findViewById(R.id.UIStopIDBox);
		ListView view = (ListView) ourView.findViewById(R.id.UIStopList);
		RelativeLayout layout = (RelativeLayout) ourView.findViewById(R.id.mainView);
		
		registerForContextMenu(view);
		registerForContextMenu(layout);
		view.setAdapter(adaptor);

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
				
		OnLongClickListener lc = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				getActivity().openContextMenu(v);
				return true;
			}
		};
		
		view.setOnLongClickListener(lc);
		layout.setOnLongClickListener(lc);
		
		OnCreateContextMenuListener cl = new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				MenuInflater inflater = getActivity().getMenuInflater();
				inflater.inflate(R.menu.stop_list_context_menu, menu);
				
				((MenuItem) menu.findItem(R.id.action_sort_list)).setTitle("Sort " + (isFavorites ? "Favorites" : "History"));
				((MenuItem) menu.findItem(R.id.action_clear_list)).setTitle("Clear " + (isFavorites ? "Favorites" : "History"));
			}
		};
		
		view.setOnCreateContextMenuListener(cl);  
		layout.setOnCreateContextMenuListener(cl);  
		
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
						update(null);
					}
				});
		view.setOnTouchListener(touchListener);
		view.setOnScrollListener(touchListener.makeScrollListener());
	}

	public void update(final ArrayList<Stop> newStops) {
		if (newStops != null) {
			stopList = newStops;
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
						if (newStops != null) {
							adaptor.clear();
							adaptor.addAll(stopList);
						}
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
		update(null);
	}

}
