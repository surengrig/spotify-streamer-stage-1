package com.example.android.spotifystreamer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {

    private static final String TAG = TopTracksActivityFragment.class.getSimpleName();
    private TopTracksAdapter mTopTracksAdapter;
    private String mCountry = "US";

    public TopTracksActivityFragment() {
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        mTopTracksAdapter = new TopTracksAdapter(getActivity(), R.id.artist_listview, new ArrayList<TrackInfo>());
        FetchTopTracksTask topTracksTask = new FetchTopTracksTask();
        topTracksTask.execute(getActivity().getIntent().getStringExtra(Constants.EXTRA_ARTIST_ID));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

//        setRetainInstance(true);
        ListView topTracksListView = (ListView) rootView.findViewById(R.id.top_tracks_listview);
        topTracksListView.setAdapter(mTopTracksAdapter);

        topTracksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

        return rootView;
    }

    private class FetchTopTracksTask extends AsyncTask<String, Void, List<TrackInfo>> {

        @Override
        protected List<TrackInfo> doInBackground(String... params) {

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            HashMap<String, Object> map = new HashMap<>();
            map.put(SpotifyService.COUNTRY, mCountry);
            List<TrackInfo> topTrackInfoList = new ArrayList<>();

            try {
                Tracks topTracks = spotify.getArtistTopTrack(params[0], map);

                if (topTracks != null && !topTracks.tracks.isEmpty()) {
                    for (Track track : topTracks.tracks) {

                        List<Image> images = track.album.images;

                        String largeImageUrl = "";
                        String smallImageUrl = "";

                        if (images != null && !images.isEmpty()) {
                            for (Image image : images) {
                                if (image.width == 640) {
                                    largeImageUrl = image.url;
                                } else if (image.width == 200) {
                                    smallImageUrl = image.url;
                                }
                            }
                            if (largeImageUrl.isEmpty() && smallImageUrl.isEmpty()) {
                                largeImageUrl = smallImageUrl = images.get(0).url;
                            } else if (largeImageUrl.isEmpty()) {
                                largeImageUrl = smallImageUrl;
                            } else {
                                smallImageUrl = largeImageUrl;
                            }
                        }

                        topTrackInfoList.add(new TrackInfo(track.album.name, track.name,
                                largeImageUrl, smallImageUrl, track.preview_url));
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }

            return topTrackInfoList;
        }

        @Override
        protected void onPostExecute(List<TrackInfo> trackInfoList) {
            if (trackInfoList.isEmpty()) {
                Toast.makeText(getActivity(), getString(R.string.empty_top_ten),
                        Toast.LENGTH_SHORT).show();
            } else {
                mTopTracksAdapter.addAll(trackInfoList);
            }
        }
    }

    private class TopTracksAdapter extends ArrayAdapter<TrackInfo> {


        public TopTracksAdapter(Context context, int resource, ArrayList<TrackInfo> trackInfo) {
            super(context, resource, trackInfo);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            TrackInfo trackInfo = getItem(position);

            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.list_item_track, parent, false);

                viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.album_imageview);
                viewHolder.album = (TextView) convertView.findViewById(R.id.album_textview);
                viewHolder.track = (TextView) convertView.findViewById(R.id.track_textview);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.album.setText(trackInfo.album);
            viewHolder.track.setText(trackInfo.track);
            if (trackInfo.smallImage.isEmpty()) {
                viewHolder.thumbnail.setImageResource(R.drawable.ic_no_image);
            } else {
                Picasso.with(getActivity()).load(trackInfo.smallImage).into(viewHolder.thumbnail);
            }

            return convertView;
        }


        private class ViewHolder {
            ImageView thumbnail;
            TextView album;
            TextView track;
        }
    }

    private class TrackInfo {
        String largeImage;
        String smallImage;
        String track;
        String album;
        String previewUrl;

        public TrackInfo(String album, String track, String largeImage, String smallImage, String previewUrl) {
            this.largeImage = largeImage;
            this.smallImage = smallImage;
            this.track = track;
            this.album = album;
            this.previewUrl = previewUrl;
        }
    }
}
