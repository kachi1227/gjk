package com.gjk;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gjk.database.objects.GroupMember;
import com.gjk.helper.GeneralHelper;
import com.gjk.utils.media2.ImageManager;
import com.gjk.views.CacheImageView;
import com.gjk.views.RecyclingImageView;
import com.google.common.collect.Lists;

import java.util.List;

import static com.gjk.Constants.BASE_URL;
import static com.gjk.Constants.CONVO_CONTEXT_MENU_ID;
import static com.gjk.Constants.CONVO_DRAWER_ADD_CHAT_MEMBERS;
import static com.gjk.Constants.CONVO_DRAWER_ADD_SIDE_CONVO_MEMBERS;
import static com.gjk.Constants.CONVO_DRAWER_ADD_WHISPER_MEMBERS;
import static com.gjk.Constants.CONVO_DRAWER_DELETE_CHAT;
import static com.gjk.Constants.CONVO_DRAWER_DELETE_SIDE_CONVO;
import static com.gjk.Constants.CONVO_DRAWER_DELETE_WHISPER;
import static com.gjk.Constants.CONVO_DRAWER_REMOVE_CHAT_MEMBERS;
import static com.gjk.Constants.CONVO_DRAWER_REMOVE_SIDE_CONVO_MEMBERS;
import static com.gjk.Constants.CONVO_DRAWER_REMOVE_WHISPER_MEMBERS;

/**
 * @author gpl
 */
public class ConvosDrawerFragment extends Fragment {

    private ConvoDrawerAdapter mAdapter;
    private ListView mConvosList;
    private Button mCreateConvo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.convos_drawer, null);
        mConvosList = (ListView) view.findViewById(R.id.convos_list);
        mCreateConvo = (Button) view.findViewById(R.id.createConvo);
        mCreateConvo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CreateConvoDialog().show(getActivity().getSupportFragmentManager(), "CreateConvoDialog");
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final MainActivity activity = (MainActivity) getActivity();
        if (mAdapter == null) {
            mAdapter = new ConvoDrawerAdapter(activity);
        }
        mConvosList.setItemsCanFocus(false);
        mConvosList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activity.toggleConvo(mAdapter.getItem(position).getConvoId());
            }
        });
        mConvosList.setAdapter(mAdapter);
        registerForContextMenu(mConvosList);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ConvoFragment frag = mAdapter.getItem(info.position);
        menu.setHeaderTitle(frag.getName());
        switch (frag.getConvoType()) {
            case MAIN_CHAT:
                menu.add(CONVO_CONTEXT_MENU_ID, v.getId(), 0, CONVO_DRAWER_ADD_CHAT_MEMBERS);
                menu.add(CONVO_CONTEXT_MENU_ID, v.getId(), 1, CONVO_DRAWER_REMOVE_CHAT_MEMBERS);
                menu.add(CONVO_CONTEXT_MENU_ID, v.getId(), 2, CONVO_DRAWER_DELETE_CHAT);
                break;
            case SIDE_CONVO:
                menu.add(CONVO_CONTEXT_MENU_ID, v.getId(), 0, CONVO_DRAWER_ADD_SIDE_CONVO_MEMBERS);
                menu.add(CONVO_CONTEXT_MENU_ID, v.getId(), 1, CONVO_DRAWER_REMOVE_SIDE_CONVO_MEMBERS);
                menu.add(CONVO_CONTEXT_MENU_ID, v.getId(), 2, CONVO_DRAWER_DELETE_SIDE_CONVO);
                break;
            case WHISPER:
                menu.add(CONVO_CONTEXT_MENU_ID, v.getId(), 0, CONVO_DRAWER_ADD_WHISPER_MEMBERS);
                menu.add(CONVO_CONTEXT_MENU_ID, v.getId(), 1, CONVO_DRAWER_REMOVE_WHISPER_MEMBERS);
                menu.add(CONVO_CONTEXT_MENU_ID, v.getId(), 2, CONVO_DRAWER_DELETE_WHISPER);
                break;
            default:
                break;
        }
    }

    public void updateView() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void clear() {
        if (mAdapter != null) {
            mAdapter.clear();
        }
    }

    public void addAll(List<ConvoFragment> convos) {
        if (mAdapter == null) {
            mAdapter = new ConvoDrawerAdapter((MainActivity) getActivity());
        }
        mAdapter.addAll(convos);
    }

    public ConvoFragment getItem(int i) {
        return mAdapter.getItem(i);
    }

    private class ConvoDrawerAdapter extends ArrayAdapter<ConvoFragment> {

        MainActivity mMa;

        public ConvoDrawerAdapter(MainActivity ma) {
            super(ma, 0);
            mMa = ma;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.convos_drawer_row, null);
            }

            TextView convoLabel = (TextView) convertView.findViewById(R.id.convoLabel);
            convoLabel.setText(getItem(position).getName());
            LinearLayout gallery = (LinearLayout) convertView.findViewById(R.id.gallery);
            gallery.removeAllViews();
            mMa.getDrawerLayout().unregisterViews(getItem(position).getConvoId());
            if (getItem(position) != null && getItem(position).getMembers() != null) {
                for (GroupMember gm : Lists.newArrayList(getItem(position).getMembers())) {
                    View view = insertPhoto(gm);
                    gallery.addView(view);
                }
            }
            mMa.getDrawerLayout().registerView(getItem(position).getConvoId(), gallery);
            gallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMa.toggleConvo(position);
                    mMa.getDrawerLayout().closeDrawer(Gravity.RIGHT);
                }
            });

            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        private View insertPhoto(GroupMember gm) {
            if (GeneralHelper.getKachisCachePref()) {
                CacheImageView imageView = new CacheImageView(getActivity().getApplicationContext());
                imageView.setLayoutParams(new ViewGroup.LayoutParams(300, 300));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(10, 10, 10, 10);
                imageView.configure(BASE_URL + gm.getImageUrl(), 0, false);
                return imageView;
            }
            RecyclingImageView imageView = new RecyclingImageView(getActivity().getApplicationContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(300, 300));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(10, 10, 10, 10);
            ImageManager.getInstance(getActivity().getSupportFragmentManager()).loadUncirclizedImage(gm.getImageUrl(),
                    imageView);
            return imageView;
        }
    }
}
