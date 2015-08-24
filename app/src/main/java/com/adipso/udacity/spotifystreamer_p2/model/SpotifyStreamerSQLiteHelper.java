package com.adipso.udacity.spotifystreamer_p2.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.adipso.udacity.spotifystreamer_p2.model.SpotifyStreamerContract.AlbumEntry;
import com.adipso.udacity.spotifystreamer_p2.model.SpotifyStreamerContract.TrackEntry;

/**
 * Created by Fabien on 20/08/2015
 *
 * v1 - 20150820 : init
 */
public class SpotifyStreamerSQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "spotifystreamer.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE_ALBUM =
            "CREATE TABLE IF NOT EXISTS "+AlbumEntry.TABLE_NAME+" (\n" +
                    "  "+AlbumEntry._ID+" INTEGER primary key autoincrement,\n" +
                    "  "+AlbumEntry.COLUMN_SERVER_KEY+" TEXT DEFAULT NULL,\n" +
                    "  "+AlbumEntry.COLUMN_NAME+" TEXT DEFAULT NULL,\n" +
                    "  "+AlbumEntry.COLUMN_LARGE_IMAGE_URL+" TEXT DEFAULT NULL,\n" +
                    "  "+AlbumEntry.COLUMN_SMALL_IMAGE_URL+" TEXT DEFAULT NULL,\n" +

                    // To assure the application have just one album entry per server_key,
                    // it's created a UNIQUE constraint with REPLACE strategy
                    " UNIQUE (" + AlbumEntry.COLUMN_SERVER_KEY + ") ON CONFLICT REPLACE" +
                    ");\n";
    private static final String DATABASE_CREATE_TRACK =
            "CREATE TABLE IF NOT EXISTS "+TrackEntry.TABLE_NAME+" (\n" +
                    "  "+TrackEntry._ID+" INTEGER primary key autoincrement,\n" +
                    "  "+TrackEntry.COLUMN_SERVER_KEY+" TEXT DEFAULT NULL,\n" +
                    "  "+TrackEntry.COLUMN_NAME+" TEXT DEFAULT NULL,\n" +
                    "  "+TrackEntry.COLUMN_ALB_KEY+" INTEGER DEFAULT NULL,\n" +
                    "  "+TrackEntry.COLUMN_DURATION_MS+" INTEGER DEFAULT NULL,\n" +
                    "  "+TrackEntry.COLUMN_PREVIEW_URL+" TEXT DEFAULT NULL,\n" +
                    "  "+TrackEntry.COLUMN_ARTIST_NAME+" TEXT DEFAULT NULL,\n" +

                    // Set up the location column as a foreign key to location table.
                    " FOREIGN KEY (" + TrackEntry.COLUMN_ALB_KEY + ") REFERENCES " +
                    AlbumEntry.TABLE_NAME + " (" + AlbumEntry._ID + "), " +

                    // To assure the application have just one track entry per server_key,
                    // it's created a UNIQUE constraint with REPLACE strategy
                    " UNIQUE (" + AlbumEntry.COLUMN_SERVER_KEY + ") ON CONFLICT REPLACE" +
                    ");\n";


    public SpotifyStreamerSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_ALBUM);//v1
        database.execSQL(DATABASE_CREATE_TRACK);//v1
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SpotifyStreamerSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS " + TrackEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AlbumEntry.TABLE_NAME);


        onCreate(db);
    }

}