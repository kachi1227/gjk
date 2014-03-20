package com.gjk.chassip;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gjk.chassip.model.User;

/**
 *
 * @author gpl
 */
public class ThreadsDrawerFragment extends ListFragment {

	private ThreadAdapter mAdapter;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.threads_drawer_list, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new ThreadAdapter(getActivity());
		setListAdapter(mAdapter);
	}
	
	public void addThread(ThreadFragment frag) {
		mAdapter.add(frag);
	}
	
	public void removeAllThreads() {
		mAdapter.clear();
	}
	
	public void removeThread(ThreadFragment frag) {
		mAdapter.remove(frag);
	}
	
	public void updateView() {
		mAdapter.notifyDataSetChanged();
	}
	
	public class ThreadAdapter extends ArrayAdapter<ThreadFragment> {

		public ThreadAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.threads_drawer_row, null);
			}
			TextView threadLabel = (TextView) convertView.findViewById(R.id.threadLabel);
			threadLabel.setText(getLabel(position));
			TextView members = (TextView) convertView.findViewById(R.id.threadMembers);
			members.setText(getMembers(position));

			return convertView;
		}
		
		private String getLabel(int position) {
			switch (getItem(position).getThreadType()) {
			case MAIN_CHAT:
				return "MAIN";
			case SIDE_CONVO:
				return "SIDE CONVO";
			case WHISPER:
				return "WHISPER";
			default:
				return "WTF...";
			}
		}
		
		private String getMembers(int position) {
			String tempMembers = "";
			for (User member : getItem(position).getMembers()) {
				tempMembers = tempMembers.concat(member.getName()).concat(" ");
			}
			return tempMembers;
		}

	}
}
