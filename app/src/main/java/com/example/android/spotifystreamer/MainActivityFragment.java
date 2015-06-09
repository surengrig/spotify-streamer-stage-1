package com.example.android.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String TAG = MainActivityFragment.class.getSimpleName();

    private ArtistAdapter mArtistsAdapter;

    public MainActivityFragment() {
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        mArtistsAdapter = new ArtistAdapter(getActivity(), R.id.artist_listview,
                new ArrayList<ArtistInfo>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView artistListView = (ListView) rootView.findViewById(R.id.artist_listview);
        artistListView.setAdapter(mArtistsAdapter);

        artistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArtistInfo artistsInfo = mArtistsAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), TopTracksActivity.class)
                        .putExtra(Constants.EXTRA_ARTIST_ID, artistsInfo.id)
                        .putExtra(Constants.EXTRA_ARTIST_NAME, artistsInfo.name);
                startActivity(intent);
            }
        });

        final EditText searchEditText = (EditText) rootView.findViewById(R.id.search_edit_text);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                SearchArtistTask searchTask = new SearchArtistTask();
                searchTask.execute(v.getText().toString());
                return false;
            }
        });
        return rootView;
    }

    private class SearchArtistTask extends AsyncTask<String, Void, List<ArtistInfo>> {

        @Override
        protected List<ArtistInfo> doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            List<ArtistInfo> artistsInfoList = new ArrayList<>();
            try {
                ArtistsPager results = spotify.searchArtists(params[0]);

                if (results != null && results.artists.total > 0) {
                    for (Artist artist : results.artists.items) {
                        String imgUrl = "";
                        if (!artist.images.isEmpty()) {
                            imgUrl = artist.images.get(0).url;
                        }
                        artistsInfoList.add(new ArtistInfo(imgUrl, artist.name, artist.id));
                    }

                }
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
            return artistsInfoList;
        }

        @Override
        protected void onPostExecute(List<ArtistInfo> artistsInfo) {

            if (artistsInfo.isEmpty()) {
                Toast.makeText(getActivity(), getString(R.string.empty_search_result),
                        Toast.LENGTH_SHORT).show();
            } else {
                mArtistsAdapter.clear();
                mArtistsAdapter.addAll(artistsInfo);
            }
        }
    }

    private class ArtistAdapter extends ArrayAdapter<ArtistInfo> {


        public ArtistAdapter(Context context, int resource, ArrayList<ArtistInfo> artistsInfo) {
            super(context, resource, artistsInfo);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final ViewHolder viewHolder;
            final ArtistInfo artistInfo = getItem(position);

            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.list_item_search, parent, false);

                viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.artist_imageview);
                viewHolder.artistName = (TextView) convertView.findViewById(R.id.artist_name_textview);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.artistName.setText(artistInfo.name);

            if (artistInfo.thumbnailUrl.isEmpty()) {
                viewHolder.thumbnail.setImageResource(R.drawable.ic_no_image);
            } else {
                Picasso.with(getActivity()).load(artistInfo.thumbnailUrl).into(viewHolder.thumbnail);
            }

            return convertView;
        }

        private class ViewHolder {
            ImageView thumbnail;
            TextView artistName;
        }
    }

    private class ArtistInfo {
        String id;
        String name;
        String thumbnailUrl;

        public ArtistInfo(String thumbnailUrl, String name, String id) {
            this.name = name;
            this.thumbnailUrl = thumbnailUrl;
            this.id = id;
        }
    }
}
