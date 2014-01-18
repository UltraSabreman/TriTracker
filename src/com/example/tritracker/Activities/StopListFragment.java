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
import com.example.tritracker.Stop;
import com.example.tritracker.Timer;
import com.example.tritracker.Util;
import com.example.tritracker.activities.MainService.LocalBinder;
import com.example.tritracker.arrayadaptors.StopArrayAdaptor;
import com.example.tritracker.json.JSONRequestManger;
import com.example.tritracker.json.JSONRequestManger.ResultCallback;
import com.example.tritracker.notmycode.UndoBarController;
import com.example.tritracker.notmycode.UndoBarController.UndoListener;

public class StopListFragment extends Fragment implements UndoListener {
	private UndoBarController mUndoBarController;
	private View ourView;
	private StopArrayAdaptor adaptor;
	private boolean isFavorites;
	
	private MainService theService;
	private boolean bound;
	
	@Override 
	public void onStart() {
		super.onStart();
		Intent intent = new Intent(getActivity(), MainService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        bound = true;
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
	
	public StopListFragment() {
		
	}
	
	public StopListFragment(boolean t) {
		isFavorites = t;
	}
	
	public StopListFragment(ArrayList<Stop> stops, boolean fav) {
		this.isFavorites = fav;
		
		theService.sub("Favorites", 
				new Timer.onUpdate() {
					public void run() {
						update();
					}
			});
		
		adaptor = new StopArrayAdaptor(getActivity().getApplicationContext(), stops, isFavorites);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//Util.parents.push(getClass());
		ourView = inflater.inflate(R.layout.activity_stop_list,
				container, false);

		mUndoBarController = new UndoBarController(ourView.findViewById(R.id.undobar), this);
		
		
		EditText edit = (EditText) ourView.findViewById(R.id.UIStopIDBox);

		edit.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE)) {
					EditText edit = (EditText) ourView.findViewById(R.id.UIStopIDBox);
					String text = edit.getText().toString();
					if (text != null && text.compareTo("") != 0) {
						((RelativeLayout) getActivity().findViewById(R.id.NoClickScreen)).setVisibility(View.VISIBLE);
						getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
						
						ResultCallback call = new JSONRequestManger.ResultCallback() { 
							public void run(Stop s, int e) {
								requsetCallback(s,e);
							}
						};
						
						new JSONRequestManger(theService, call, getActivity(), getActivity().getApplicationContext(), Integer.parseInt(text)).start();
					}

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

	public void function(int id) {
		Stop s = theService.getStop(id);
		Context c = getActivity().getApplicationContext();
		if (s != null) {
			showStop(s);
			return;
		}
		return;
	}
	
	public interface checkStops {
		public void doStops();
	}
	
	
	private void hanldeHTTPErrors(int error, final int id) {
		Activity a = getActivity();
		if (error == 1)
			Util.messageDiag(
					a,
					new checkStops() {
						public void doStops() {
							function(id);
						}
					},
					"Connection Timed-Out",
					"The connection timed-out (Trimet's servers might be busy, or you could have a poor connection)."
							+ "\n\nIf you've visited this stop before, and you want to see the cached times, click ok.");
		else if (error == 2)
			Util.messageDiag(
					a,
					new checkStops() {
						public void doStops() {
							function(id);
						}
					},
					"Malformed reponce",
					"Trimet didn't respond correctly (their servers may be under heavy load)"
							+ "\n\nIf you've visited this stop before, and you want to see the cached times, click ok.");
		else if (error == 3)
			Util.messageDiag(
					a,
					new checkStops() {
						public void doStops() {
							function(id);
						}
					},
					"Error Connecting",
					"It looks like Trimet changed their API. Please contact the developer ASAP and this will be fixed."
							+ "\n\nIf you've visited this stop before, and you want to see the cached times, click ok.");
		else if (error == 4)
			Util.messageDiag(
					a,
					new checkStops() {
						public void doStops() {
							function(id);
						}
					},
					"Are you connected?",
					"Can't reach the Trimet servers right now, are you connected to the internet?"
							+ "\n\nIf you've visited this stop before, and you want to see the cached times, click ok.");
		

	}
	
	public void requsetCallback(Stop s, int error) {
		if (s == null) return;
		Activity a = getActivity();
		Context c = a.getApplicationContext();
		
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

	public void update() {	
		adaptor.notifyDataSetChanged();
		
		if (adaptor == null || adaptor.getCount() == 0)
			((TextView) ourView.findViewById(R.id.NoMembers))
					.setVisibility(View.VISIBLE);
		else
			((TextView) ourView.findViewById(R.id.NoMembers))
					.setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onDestroy() {
		if (adaptor != null)
			adaptor.notifyDataSetInvalidated();
		
		if (bound)
			theService.unsub("Favorites");
		
        if (bound) {
            getActivity().unbindService(mConnection);
            bound = false;
        }
		super.onDestroy();
	}

	private void initList() {
		ListView view = (ListView) ourView.findViewById(R.id.UIStopList);
		view.setAdapter(adaptor);
		
		view.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Stop temp = adaptor.getItem(position);
				if (temp != null) {
					
					ResultCallback call = new JSONRequestManger.ResultCallback() { 
						public void run(Stop s, int e) {
							requsetCallback(s,e);
						}
					};
					
					new JSONRequestManger(theService, call, getActivity(), getActivity().getApplicationContext(), temp.StopID).start();
				}
			}
		});

		// Create a ListView-specific touch listener. ListViews are given
		// special treatment because
		// by default they handle touches for their list items... i.e. they're
		// in charge of drawing
		// the pressed state (the list selector), handling list item clicks,
		// etc.
		/*SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
				view, new SwipeDismissListViewTouchListener.DismissCallbacks() {
					@Override
					public boolean canDismiss(int position) {
						return true;
					}

					@Override
					public void onDismiss(ListView listView,
							int[] reverseSortedPositions) {
						for (int position : reverseSortedPositions) {
							Stop stop = adaptor.getItem(position);
							if (stop != null) {
								GlobalData.FUndos.add(stop);
								GlobalData.favAdaptor.remove(stop);

								mUndoBarController.showUndoBar(
										false,
										"Removed "
												+ GlobalData.FUndos.size()
												+ " Stop"
												+ (GlobalData.FUndos.size() > 1 ? "s"
														: ""), null);
							}
						}
						onActivityChange();
					}
				});
		view.setOnTouchListener(touchListener);
		// Setting this scroll listener is required to ensure that during
		// ListView scrolling,
		// we don't look for swipes.
		view.setOnScrollListener(touchListener.makeScrollListener());*/
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_action_bar, menu);
		return super.onCreateOptionsMenu(menu);
	}*/


	@Override
	public void onUndo(Parcelable token, boolean fail) {
		if (!fail) {
		/*	for (Stop s : GlobalData.FUndos) {
				if (!Util.favHasStop(s))
					GlobalData.favAdaptor.add(s);
			}
			GlobalData.FUndos.clear();
			onActivityChange();*/
		} else {
			//GlobalData.FUndos.clear();
		}
	}

}