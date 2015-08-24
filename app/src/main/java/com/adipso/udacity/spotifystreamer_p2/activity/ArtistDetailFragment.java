package com.adipso.udacity.spotifystreamer_p2.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adipso.udacity.spotifystreamer_p2.R;
import com.adipso.udacity.spotifystreamer_p2.model.CustomTrack;
import com.adipso.udacity.spotifystreamer_p2.model.CustomTrackHelper;
import com.adipso.udacity.spotifystreamer_p2.model.TrackDataSource;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * A fragment representing a single Artist detail screen.
 * This fragment is either contained in a {@link ArtistListActivity}
 * in two-pane mode (on tablets) or a {@link ArtistDetailActivity}
 * on handsets.
 */
public class ArtistDetailFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Pair<ArtistDetailFragment.EResultStateTask, List<CustomTrack>>> {
    public static final String STATE_LST_TRACKS = "lst_tracks";
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_ARTIST_NAME = "artistName";

    public static Fragment createFragment(String idArt, String artistName) {
        Fragment fragment = new ArtistDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ITEM_ID, idArt);
        args.putString(ARG_ARTIST_NAME, artistName);
        fragment.setArguments(args);
        return fragment;
    }

    private static final int LOADER_A = 1;


    private String mIdArt;
    private String mArtistName;

    private List<CustomTrack> mLstTracks;
    private TrackArrayAdapter mAA;
    private boolean mIsTabletLayout;

    public ArtistDetailFragment() {
        mIdArt = null;
        mArtistName = null;

        mLstTracks = new ArrayList<>();
        mAA = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //init
        mIsTabletLayout = getResources().getBoolean(R.bool.tablet_layout);

        //args
        if (null != getArguments()) {
            mIdArt = getArguments().getString(ARG_ITEM_ID);
            mArtistName = getArguments().getString(ARG_ARTIST_NAME);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = view.getContext();

        //init
        mAA = new TrackArrayAdapter(context,
                R.layout.list_item_track,
                mLstTracks);
        setListAdapter(mAA);

        //data
        //-if data from previous state
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_LST_TRACKS)) {
            List<CustomTrack> savedLstTracks = savedInstanceState.getParcelableArrayList(STATE_LST_TRACKS);
            mLstTracks.addAll(savedLstTracks);

            //if data is empty (from previous state)
            if (mLstTracks.isEmpty()) {
                getLoaderManager().restartLoader(LOADER_A, null, this);
            }
        }
        //-if new state
        else {
            setListShownNoAnimation(false);

            getLoaderManager().initLoader(LOADER_A, null, this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(STATE_LST_TRACKS, (ArrayList<CustomTrack>)mLstTracks);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        CustomTrack item = (CustomTrack)getListView().getItemAtPosition(position);
        if (null != item) {
            Long idMin = mLstTracks.get(0).get_ID();
            Long idMax = mLstTracks.get(mLstTracks.size()-1).get_ID();

            if (mIsTabletLayout) {
                PlayerDialogFragment.showDialog(getFragmentManager(), item.get_ID(), item, idMin, idMax);
            }
            else {
                PlayerActivity.startActivity(getActivity(), item.get_ID(), item, idMin, idMax);
            }
        }
    }


    @Override
    public Loader<Pair<EResultStateTask, List<CustomTrack>>> onCreateLoader(int id, Bundle args) {
        if (LOADER_A == id) {
            return new GetTracksTask(getActivity(), mIdArt, mArtistName);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Pair<EResultStateTask, List<CustomTrack>>> loader, Pair<EResultStateTask, List<CustomTrack>> data) {
        Context context = getActivity();

        if (LOADER_A == loader.getId()) {
            setListShown(true);

            if (null != data) {
                mLstTracks.clear();
                mLstTracks.addAll(data.second);
                mAA.notifyDataSetChanged();

                if (EResultStateTask.error.equals(data.first)) {
                    Toast.makeText(context, context.getString(R.string.dialog_msg_err_data), Toast.LENGTH_SHORT).show();
                }
                else if (mLstTracks.isEmpty()) {
                    Toast.makeText(context, context.getString(R.string.dialog_msg_err_no_tracks), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Pair<EResultStateTask, List<CustomTrack>>> loader) {
    }


    private static class TrackArrayAdapter extends ArrayAdapter<CustomTrack> {
        private final LayoutInflater mInflater;
        private int mResource;
        private Picasso mPicasso;

        public TrackArrayAdapter(Context context, int resource, List<CustomTrack> objects) {
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
            if (null == convertView) {
                view = mInflater.inflate(mResource, parent, false);
                holder = new ViewHolder();
                holder.img_track = (ImageView)view.findViewById(R.id.img_track);
                holder.txt_album = (TextView)view.findViewById(R.id.txt_album);
                holder.txt_title = (TextView)view.findViewById(R.id.txt_title);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder)view.getTag();
            }

            //data
            CustomTrack item = getItem(position);

            //fill
            //-album
            if (item.getAlbum() != null) {
                holder.txt_album.setText(item.getAlbum().getName());
            }
            else {
                holder.txt_album.setText("");
            }
            //-name
            holder.txt_title.setText(item.getName());
            //-first image or no photo if empty
            if (item.getAlbum() != null && !TextUtils.isEmpty(item.getAlbum().getSmallImageUrl())) {
                mPicasso.load(item.getAlbum().getSmallImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.list_no_image)
                        .into(holder.img_track);
            } else {
                mPicasso.load(R.drawable.list_no_image)
                        .into(holder.img_track);
            }

            return view;
        }

        private class ViewHolder {
            ImageView img_track;
            TextView txt_album, txt_title;
        }
    }


    private static class GetTracksTask extends AsyncTaskLoader<Pair<EResultStateTask, List<CustomTrack>>> {
        private TrackDataSource trackDataSource;
        private String idArt;
        private String artistName;

        public GetTracksTask(Context context, String idArt, String artistName) {
            super(context);

            this.idArt = idArt;
            this.artistName = artistName;
            trackDataSource = new TrackDataSource(context);
        }

        @Override
        public Pair<EResultStateTask, List<CustomTrack>> loadInBackground() {
            EResultStateTask state = EResultStateTask.error;
            List<CustomTrack> result = new ArrayList<>();

            if (!TextUtils.isEmpty(idArt)) {
                try {
                    trackDataSource.open();

                    SpotifyApi api = new SpotifyApi();
                    SpotifyService spotify = api.getService();

                    Hashtable<String, Object> args = new Hashtable<>();
                    args.put("country", "FR");
                    Tracks results = spotify.getArtistTopTrack(idArt, args);

                    trackDataSource.removeAllTrack();
                    for (Track track : results.tracks) {
                        CustomTrack customTrack = CustomTrackHelper.convertTrackToCustomTrack(track, artistName);
                        trackDataSource.createTrack(customTrack);
                    }
                    result = trackDataSource.getAllCustomTracks();

                    state = EResultStateTask.ok;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    trackDataSource.close();
                }
            }
            return new Pair<>(state, result);
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }


    public static enum EResultStateTask {
        error,
        ok
    }
}
