package com.gjk.chassip;

import java.util.Map;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gjk.chassip.model.User;
import com.gjk.chassip.model.Chat;
import com.google.common.collect.Maps;

/**
 *
 * @author gpl
 */
public class ChatsDrawerFragment extends ListFragment {

	private Map<Integer, Chat> mPositionToChatMap;
	private ChatAdapter mAdapter;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.chats_drawer_list, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPositionToChatMap = Maps.newHashMap();
		mAdapter = new ChatAdapter(getActivity());
		setListAdapter(mAdapter);
	}
	
	public void addChat(Chat chat) {
		mAdapter.add(chat);
	}
	
	public void updateView() {
		mAdapter.notifyDataSetChanged();
	}
	
	public class ChatAdapter extends ArrayAdapter<Chat> {

		public ChatAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			
			Integer iPosition = Integer.valueOf(position);
			
			if (!mPositionToChatMap.containsKey(iPosition)) {
				mPositionToChatMap.put(iPosition, getItem(position));
			}
			
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.chats_drawer_row, null);
			}
			TextView chatLabel = (TextView) convertView.findViewById(R.id.chatLabel);
			chatLabel.setText(getLabel(position));
			TextView members = (TextView) convertView.findViewById(R.id.chatMembers);
			members.setText(getMembers(position));

			return convertView;
		}
		
		private String getLabel(int position) {

			return String.format("Chat #%d", position);
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
