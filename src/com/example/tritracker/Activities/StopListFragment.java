package com.example.tritracker.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.example.tritracker.arrayadaptors.StopArrayAdaptor;
import com.example.tritracker.json.JSONRequestManger;
import com.example.tritracker.json.JSONRequestManger.ResultCallback;
import com.example.tritracker.notmycode.SwipeDismissListViewTouchListener;
import com.example.tritracker.notmycode.UndoBarController;
import com.example.tritracker.notmycode.UndoBarController.UndoListener;

public class StopListFragment extends Fragment implements UndoListener {
	private UndoBarController mUndoBarController;
	private View ourView;
	private StopArrayAdaptor adaptor;
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

		adaptor = new StopArrayAdaptor(theService, getActivity().getApplicationContext(), stopList, isFavorites);
		initList();
		
		update(null);
		
		theService.sub(isFavorites ? "Favorites" : "History", new Timer.onUpdate() {
			public void run() {
				update(null);
			}
		});

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
	
	public StopListFragment(MainService service, boolean t) {
		isFavorites = t;
		theService = service;
	}
	
	private void getJson(int stop) {
		((RelativeLayout) getActivity().findViewById(R.id.NoClickScreen)).setVisibility(View.VISIBLE);
		((RelativeLayout) getActivity().findViewById(R.id.NoClickScreen2)).setVisibility(View.VISIBLE);
		getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
		
		ResultCallback call = new JSONRequestManger.ResultCallback() { 
			public void run(Stop s, int e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						((RelativeLayout) getActivity().findViewById(R.id.NoClickScreen)).setVisibility(View.INVISIBLE);
						((RelativeLayout) getActivity().findViewById(R.id.NoClickScreen2)).setVisibility(View.INVISIBLE);
						getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
					}
				});
				requsetCallback(s,e);
				theService.doUpdate(false);
			}
		};
		
		new JSONRequestManger(theService, call, getActivity().getApplicationContext(), stop).start();
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ourView = inflater.inflate(R.layout.activity_stop_list,
				container, false);

		mUndoBarController = new UndoBarController(ourView.findViewById(R.id.undobar), this);
		EditText edit = (EditText)  getActivity().findViewById(R.id.UIStopIDBox);
		
		((TextView) ourView.findViewById(R.id.NoMembers)).setText("Your " + (isFavorites ? "Favorites List" : "History") + " is Empty.");

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
	
		initList();
		return ourView;
	}

	public void showStop(Stop s) {
		Context c = getActivity().getApplicationContext();
		
		Intent tempIntent = new Intent(c, BussListActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		tempIntent.putExtra("stop", s);
		
		c.startActivity(tempIntent);
	}
	
	public interface checkStops {
		public void doStops();
	}
	
	
	private void hanldeHTTPErrors(int error, final int id) {
		Activity a = getActivity();
		checkStops newCallback = new checkStops() {
									public void doStops() {
										Stop s = theService.getStop(id);
										if (s != null)
											showStop(s);
										return;
									}
								};
		
		if (error == 1)
			Util.messageDiag(a,	newCallback,
					"Connection Timed-Out",
					"The connection timed-out (Trimet's servers might be busy, or you could have a poor connection)."
							+ "\n\nIf you've visited this stop before, and you want to see the cached times, click ok.");
		else if (error == 2)
			Util.messageDiag(a,	newCallback,
					"Malformed reponce",
					"Trimet didn't respond correctly (their servers may be under heavy load)"
							+ "\n\nIf you've visited this stop before, and you want to see the cached times, click ok.");
		else if (error == 3)
			Util.messageDiag(a,	newCallback,
					"Error Connecting",
					"It looks like Trimet changed their API. Please contact the developer ASAP and this will be fixed."
							+ "\n\nIf you've visited this stop before, and you want to see the cached times, click ok.");
		else if (error == 4)
			Util.messageDiag(a,	newCallback,
					"Are you connected?",
					"Can't reach the Trimet servers right now, are you connected to the internet?"
							+ "\n\nIf you've visited this stop before, and you want to see the cached times, click ok.");
		

	}
	
	public void requsetCallback(Stop s, int error) {		
		if (s == null) return;
		Activity a = getActivity();
		//Context c = a.getApplicationContext();
		
		if (error > 0) {
			hanldeHTTPErrors(error, s.StopID);
		} else if (error == -1) {
			AlertDialog.Builder builder = new AlertDialog.Builder(a);

			builder.setMessage(
					"A stop with the ID \"" + s.StopID + "\" doesn't exist.")
					.setTitle(R.string.no_stop);

			builder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int id) {
						}
					});

			builder.create().show();
		} else {
			showStop(s);
		}
		
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
	
	private void initList() {
		ListView view = (ListView) ourView.findViewById(R.id.UIStopList);
		view.setAdapter(adaptor);
		
		view.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,	int position, long arg3) {
				Stop temp = adaptor.getItem(position);
				if (temp != null) 
					getJson(temp.StopID);
			}
		});

		// Create a ListView-specific touch listener. ListViews are given
		// special treatment because
		// by default they handle touches for their list items... i.e. they're
		// in charge of drawing
		// the pressed state (the list selector), handling list item clicks,
		// etc.
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
		// Setting this scroll listener is required to ensure that during
		// ListView scrolling,
		// we don't look for swipes.
		view.setOnScrollListener(touchListener.makeScrollListener());
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
