package com.adipso.udacity.spotifystreamer_p2.activity;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adipso.udacity.spotifystreamer_p2.R;
import com.adipso.udacity.spotifystreamer_p2.model.SpotifyStreamerPrefs;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * A list fragment representing a list of Artists. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link ArtistDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link ArtistListCallbacks}
 * interface.
 */
public class ArtistListFragment extends Fragment implements
        AdapterView.OnItemClickListener {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private static final String STATE_SEARCH = "search";

    private TextView txt_search;
    private ListView lv_artists;

    private String mLastSearch;
    private List<Artist> mLstArtists;
    private ArtistArrayAdapter mAA;
    private GetArtistsTask mGetArtistsTask;

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private ArtistListCallbacks mCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArtistListFragment() {
        txt_search = null;

        mLastSearch = null;
        mLstArtists = new ArrayList<>();
        mAA = null;
        mGetArtistsTask = null;

        mCallbacks = sDummyCallbacks;
        mActivatedPosition = ListView.INVALID_POSITION;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_artist_list, container, false);
        Context context = rootView.getContext();

        //init
        mAA = new ArtistArrayAdapter(context,
                R.layout.list_item_artist,
                mLstArtists);

        //view
        txt_search = (TextView)rootView.findViewById(R.id.txt_search);
        lv_artists = (ListView)rootView.findViewById(R.id.lv_artists);

        lv_artists.setAdapter(mAA);
        lv_artists.setOnItemClickListener(this);

        txt_search.addTextChangedListener(mSearchTextWatcher);

        //data
        //-if from previous state, restore it
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_SEARCH)) {
            mLastSearch = savedInstanceState.getString(STATE_SEARCH);
        }
        //-otherwise, restore from last search saved
        else {
            mLastSearch = SpotifyStreamerPrefs.getLastSearch(context);
        }
        //-set search and call doGetArtistsTask() via TextWatcher
        txt_search.setText(mLastSearch);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
        outState.putString(STATE_SEARCH, mLastSearch);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof ArtistListCallbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (ArtistListCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Artist item = (Artist)lv_artists.getItemAtPosition(position);
        if (item != null) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mCallbacks.onItemSelected(item.id, item.name);

            mActivatedPosition = position;
        }
    }


    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        lv_artists.setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            lv_artists.setItemChecked(mActivatedPosition, false);
        } else {
            lv_artists.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }



    private static class ArtistArrayAdapter extends ArrayAdapter<Artist> {
        private final LayoutInflater mInflater;
        private int mResource;
        private Picasso mPicasso;

        public ArtistArrayAdapter(Context context, int resource, List<Artist> objects) {
            super(context, resource, objects);

            mInflater = LayoutInflater.from(context);
            mResource = resource;
            mPicasso = Picasso.with(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder holder;

            //create or recycle view
            if (convertView == null) {
                view = mInflater.inflate(mResource, parent, false);
                holder = new ViewHolder();
                holder.img_artist = (ImageView)view.findViewById(R.id.img_artist);
                holder.txt_artist = (TextView)view.findViewById(R.id.txt_artist);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder)view.getTag();
            }

            //data
            Artist item = getItem(position);

            //fill
            //-name
            holder.txt_artist.setText(item.name);
            //-first image or no photo if empty
            if (item.images != null && !item.images.isEmpty()) {
                mPicasso.load(item.images.get(0).url)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.list_no_image)
                        .into(holder.img_artist);
            } else {
                mPicasso.load(R.drawable.list_no_image)
                        .into(holder.img_artist);
            }

            return view;
        }

        private class ViewHolder {
            ImageView img_artist;
            TextView txt_artist;
        }
    }

    private void doGetArtistsTask() {
        //cancel previous search
        if (mGetArtistsTask != null) {
            mGetArtistsTask.cancel(true);
            mGetArtistsTask = null;
        }
        //do new search
        String search = null;
        if (txt_search != null) {
            search = txt_search.getText().toString();
            mLastSearch = search;
        }
        mGetArtistsTask = new GetArtistsTask();
        mGetArtistsTask.execute(search);
    }
    private class GetArtistsTask extends AsyncTask<String, Void, List<Artist>> {
        private boolean mHasErrorWithData;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mHasErrorWithData = false;
        }

        @Override
        protected List<Artist> doInBackground(String[] params) {
            String search = (params.length > 0 ? params[0] : "");
            List<Artist> result = new ArrayList<>();

            if (!TextUtils.isEmpty(search)) {
                ArtistsPager results = null;
                try {
                    SpotifyApi api = new SpotifyApi();
                    SpotifyService spotify = api.getService();

                    //searchArtists() restricts query by including type=”artist”
                    //the * is to search "artist with name starts with ..."
                    results = spotify.searchArtists(search + "*");
                }
                catch (Exception e) {
                    e.printStackTrace();

                    mHasErrorWithData = true;
                }

                //if can continue (no cancel())
                if (!isCancelled() && results != null) {
                    //for (int pos=0, len=results.artists.total; pos<len; pos++) {
                    result.addAll(results.artists.items);
                    //}
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            mGetArtistsTask = null;
            Context context = getActivity();

            //isAdded() is to verify if Fragment is attached; otherwise, context may be null
            if (isAdded() && artists != null) {
                //save
                SpotifyStreamerPrefs.setLastSearch(context, mLastSearch);

                //show
                mLstArtists.clear();
                mLstArtists.addAll(artists);
                mAA.notifyDataSetChanged();

                if (mHasErrorWithData) {
                    Toast.makeText(context, context.getString(R.string.dialog_msg_err_data), Toast.LENGTH_SHORT).show();
                }
                else if (mLstArtists.isEmpty()) {
                    Toast.makeText(context, context.getString(R.string.dialog_msg_err_no_artist), Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    private TextWatcher mSearchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            doGetArtistsTask();
        }
    };

    /**
     * A dummy implementation of the {@link ArtistListCallbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static ArtistListCallbacks sDummyCallbacks = new ArtistListCallbacks() {
        @Override
        public void onItemSelected(String id, String subTitle) {
        }
    };

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface ArtistListCallbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id, String subTitle);
    }
}
