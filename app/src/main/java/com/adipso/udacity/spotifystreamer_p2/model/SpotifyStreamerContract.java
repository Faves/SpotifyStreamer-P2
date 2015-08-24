package com.adipso.udacity.spotifystreamer_p2.model;

import android.provider.BaseColumns;

/**
 * Created by Fabien on 20/08/2015.
 */
public class SpotifyStreamerContract {
    public static final class AlbumEntry implements BaseColumns {
        public static final String TABLE_NAME = "album";

        // Column with the server key.
        public static final String COLUMN_SERVER_KEY = "server_key";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LARGE_IMAGE_URL = "large_image_url";
        public static final String COLUMN_SMALL_IMAGE_URL = "small_image_url";
    }

    public static final class TrackEntry implements BaseColumns {
        public static final String TABLE_NAME = "track";

        // Column with the server key.
        public static final String COLUMN_SERVER_KEY = "server_key";

        // Column with the foreign key into the album table.
        public static final String COLUMN_ALB_KEY = "album_id";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DURATION_MS = "duration_ms";
        public static final String COLUMN_PREVIEW_URL = "preview_url";
        public static final String COLUMN_ARTIST_NAME = "artist_name";
    }
}
