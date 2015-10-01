package com.ultramegasoft.flavordex2.provider;

import android.app.backup.BackupManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * The ContentProvider backing the application.
 *
 * @author Steve Guidetti
 */
public class FlavordexProvider extends ContentProvider {
    /**
     * The Authority string for this ContentProvider
     */
    private static final String AUTHORITY = Tables.AUTHORITY;

    /**
     * URI identifier codes
     */
    private static final int ENTRIES = 1;
    private static final int ENTRIES_ID = 2;
    private static final int ENTRIES_FILTER = 3;
    private static final int ENTRIES_EXTRAS = 4;
    private static final int ENTRIES_FLAVOR = 5;
    private static final int ENTRIES_PHOTOS = 6;
    private static final int CATS = 7;
    private static final int CATS_ID = 8;
    private static final int CATS_EXTRAS = 9;
    private static final int CATS_FLAVOR = 10;
    private static final int EXTRAS = 11;
    private static final int EXTRAS_ID = 12;
    private static final int FLAVORS = 13;
    private static final int FLAVORS_ID = 14;
    private static final int PHOTOS = 15;
    private static final int PHOTOS_ID = 16;
    private static final int MAKERS = 17;
    private static final int MAKERS_ID = 18;
    private static final int MAKERS_FILTER = 19;
    private static final int LOCATIONS = 20;
    private static final int LOCATIONS_ID = 21;

    /**
     * The UriMatcher to use
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "entries", ENTRIES);
        sUriMatcher.addURI(AUTHORITY, "entries/#", ENTRIES_ID);
        sUriMatcher.addURI(AUTHORITY, "entries/filter/*", ENTRIES_FILTER);
        sUriMatcher.addURI(AUTHORITY, "entries/#/extras", ENTRIES_EXTRAS);
        sUriMatcher.addURI(AUTHORITY, "entries/#/flavor", ENTRIES_FLAVOR);
        sUriMatcher.addURI(AUTHORITY, "entries/#/photos", ENTRIES_PHOTOS);
        sUriMatcher.addURI(AUTHORITY, "cats", CATS);
        sUriMatcher.addURI(AUTHORITY, "cats/#", CATS_ID);
        sUriMatcher.addURI(AUTHORITY, "cats/#/extras", CATS_EXTRAS);
        sUriMatcher.addURI(AUTHORITY, "cats/#/flavor", CATS_FLAVOR);
        sUriMatcher.addURI(AUTHORITY, "extras", EXTRAS);
        sUriMatcher.addURI(AUTHORITY, "extras/#", EXTRAS_ID);
        sUriMatcher.addURI(AUTHORITY, "flavors", FLAVORS);
        sUriMatcher.addURI(AUTHORITY, "flavors/#", FLAVORS_ID);
        sUriMatcher.addURI(AUTHORITY, "photos", PHOTOS);
        sUriMatcher.addURI(AUTHORITY, "photos/#", PHOTOS_ID);
        sUriMatcher.addURI(AUTHORITY, "makers", MAKERS);
        sUriMatcher.addURI(AUTHORITY, "makers/#", MAKERS_ID);
        sUriMatcher.addURI(AUTHORITY, "makers/filter/*", MAKERS_FILTER);
        sUriMatcher.addURI(AUTHORITY, "locations", LOCATIONS);
        sUriMatcher.addURI(AUTHORITY, "locations/#", LOCATIONS_ID);
    }

    /**
     * The helper to access the database
     */
    private DatabaseHelper mDbHelper;

    /**
     * The BackupManager to notify of data changes
     */
    private BackupManager mBackupManager;

    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        mBackupManager = new BackupManager(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch(sUriMatcher.match(uri)) {
            case ENTRIES:
            case ENTRIES_FILTER:
                return Tables.Entries.DATA_TYPE;
            case ENTRIES_ID:
                return Tables.Entries.DATA_TYPE_ITEM;
            case CATS:
                return Tables.Cats.DATA_TYPE;
            case CATS_ID:
                return Tables.Cats.DATA_TYPE_ITEM;
            case EXTRAS:
            case ENTRIES_EXTRAS:
            case CATS_EXTRAS:
                return Tables.Extras.DATA_TYPE;
            case EXTRAS_ID:
                return Tables.Extras.DATA_TYPE_ITEM;
            case FLAVORS:
            case ENTRIES_FLAVOR:
            case CATS_FLAVOR:
                return Tables.Flavors.DATA_TYPE;
            case FLAVORS_ID:
                return Tables.Flavors.DATA_TYPE_ITEM;
            case PHOTOS:
            case ENTRIES_PHOTOS:
                return Tables.Photos.DATA_TYPE;
            case PHOTOS_ID:
                return Tables.Photos.DATA_TYPE_ITEM;
            case MAKERS:
            case MAKERS_FILTER:
                return Tables.Makers.DATA_TYPE;
            case MAKERS_ID:
                return Tables.Makers.DATA_TYPE_ITEM;
            case LOCATIONS:
                return Tables.Locations.DATA_TYPE;
            case LOCATIONS_ID:
                return Tables.Locations.DATA_TYPE_ITEM;
        }

        return null;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch(sUriMatcher.match(uri)) {
            case ENTRIES:
                queryBuilder.setTables(Tables.Entries.VIEW_NAME);
                break;
            case ENTRIES_ID:
                queryBuilder.setTables(Tables.Entries.VIEW_NAME);
                queryBuilder.appendWhere(Tables.Entries._ID + " = " + uri.getLastPathSegment());
                break;
            case ENTRIES_FILTER:
                queryBuilder.setTables(Tables.Entries.VIEW_NAME);
                queryBuilder.appendWhere(Tables.Entries.TITLE + " LIKE ");
                queryBuilder.appendWhereEscapeString("%" + uri.getLastPathSegment() + "%");
                break;
            case CATS:
                queryBuilder.setTables(Tables.Cats.VIEW_NAME);
                break;
            case CATS_ID:
                queryBuilder.setTables(Tables.Cats.VIEW_NAME);
                queryBuilder.appendWhere(Tables.Cats._ID + " = " + uri.getLastPathSegment());
                break;
            case EXTRAS:
                queryBuilder.setTables(Tables.Extras.TABLE_NAME);
                break;
            case EXTRAS_ID:
                queryBuilder.setTables(Tables.Extras.TABLE_NAME);
                queryBuilder.appendWhere(Tables.Extras._ID + " = " + uri.getLastPathSegment());
                break;
            case CATS_EXTRAS:
                queryBuilder.setTables(Tables.Extras.TABLE_NAME);
                queryBuilder.appendWhere(Tables.Extras.CAT + " = " + uri.getPathSegments().get(1));
                break;
            case ENTRIES_EXTRAS:
                queryBuilder.setTables(Tables.EntriesExtras.VIEW_NAME);
                queryBuilder.appendWhere(Tables.EntriesExtras.ENTRY + " = "
                        + uri.getPathSegments().get(1));
                break;
            case FLAVORS:
                queryBuilder.setTables(Tables.Flavors.TABLE_NAME);
                break;
            case FLAVORS_ID:
                queryBuilder.setTables(Tables.Flavors.TABLE_NAME);
                queryBuilder.appendWhere(Tables.Flavors._ID + " = " + uri.getLastPathSegment());
                break;
            case CATS_FLAVOR:
                queryBuilder.setTables(Tables.Flavors.TABLE_NAME);
                queryBuilder.appendWhere(Tables.Flavors.CAT + " = "
                        + uri.getPathSegments().get(1));
                break;
            case ENTRIES_FLAVOR:
                queryBuilder.setTables(Tables.EntriesFlavors.TABLE_NAME);
                queryBuilder.appendWhere(Tables.EntriesFlavors.ENTRY + " = "
                        + uri.getPathSegments().get(1));
                break;
            case PHOTOS:
                queryBuilder.setTables(Tables.Photos.TABLE_NAME);
                break;
            case PHOTOS_ID:
                queryBuilder.setTables(Tables.Photos.TABLE_NAME);
                queryBuilder.appendWhere(Tables.Photos._ID + " = " + uri.getLastPathSegment());
                break;
            case ENTRIES_PHOTOS:
                queryBuilder.setTables(Tables.Photos.TABLE_NAME);
                queryBuilder.appendWhere(Tables.Photos.ENTRY + " = "
                        + uri.getPathSegments().get(1));
                break;
            case MAKERS:
                queryBuilder.setTables(Tables.Makers.TABLE_NAME);
                break;
            case MAKERS_ID:
                queryBuilder.setTables(Tables.Makers.TABLE_NAME);
                queryBuilder.appendWhere(Tables.Makers._ID + " = " + uri.getLastPathSegment());
                break;
            case MAKERS_FILTER:
                queryBuilder.setTables(Tables.Makers.TABLE_NAME);
                queryBuilder.appendWhere(Tables.Makers.NAME + " LIKE ");
                queryBuilder.appendWhereEscapeString("%" + uri.getLastPathSegment() + "%");
                break;
            case LOCATIONS:
                queryBuilder.setTables(Tables.Locations.TABLE_NAME);
                break;
            case LOCATIONS_ID:
                queryBuilder.setTables(Tables.Locations.TABLE_NAME);
                queryBuilder.appendWhere(Tables.Locations._ID + " = " + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri.toString());
        }

        final Cursor cursor = queryBuilder.query(mDbHelper.getReadableDatabase(), projection,
                selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        String table;
        values = new ContentValues(values);

        switch(sUriMatcher.match(uri)) {
            case ENTRIES:
                table = Tables.Entries.TABLE_NAME;
                if(values.containsKey(Tables.Entries.MAKER)
                        || values.containsKey(Tables.Entries.ORIGIN)) {
                    processMaker(values);
                }
                break;
            case CATS:
                table = Tables.Cats.TABLE_NAME;
                values.remove(Tables.Cats.PRESET);
                filterPreset(values.getAsString(Tables.Cats.NAME));
                break;
            case EXTRAS:
                table = Tables.Extras.TABLE_NAME;
                values.remove(Tables.Extras.PRESET);
                filterPreset(values.getAsString(Tables.Extras.NAME));
                break;
            case CATS_EXTRAS:
                table = Tables.Extras.TABLE_NAME;
                values.remove(Tables.Extras.PRESET);
                filterPreset(values.getAsString(Tables.Extras.NAME));
                values.put(Tables.Extras.CAT, uri.getPathSegments().get(1));
                break;
            case FLAVORS:
                table = Tables.Flavors.TABLE_NAME;
                break;
            case CATS_FLAVOR:
                table = Tables.Flavors.TABLE_NAME;
                values.put(Tables.Flavors.CAT, uri.getPathSegments().get(1));
                break;
            case ENTRIES_EXTRAS:
                table = Tables.EntriesExtras.TABLE_NAME;
                values.put(Tables.EntriesExtras.ENTRY, uri.getPathSegments().get(1));
                break;
            case ENTRIES_FLAVOR:
                table = Tables.EntriesFlavors.TABLE_NAME;
                values.put(Tables.EntriesFlavors.ENTRY, uri.getPathSegments().get(1));
                break;
            case ENTRIES_PHOTOS:
                table = Tables.Photos.TABLE_NAME;
                values.put(Tables.Photos.ENTRY, uri.getPathSegments().get(1));
                break;
            case PHOTOS:
                table = Tables.Photos.TABLE_NAME;
                break;
            case LOCATIONS:
                table = Tables.Locations.TABLE_NAME;
                break;
            case ENTRIES_ID:
            case CATS_ID:
            case EXTRAS_ID:
            case FLAVORS_ID:
            case PHOTOS_ID:
            case LOCATIONS_ID:
                throw new IllegalArgumentException("Insert not permitted on: " + uri.toString());
            case ENTRIES_FILTER:
            case MAKERS:
            case MAKERS_ID:
            case MAKERS_FILTER:
                throw new IllegalArgumentException("URI is read-only: " + uri.toString());
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri.toString());
        }

        synchronized(FlavordexProvider.class) {
            final long id = mDbHelper.getWritableDatabase().insert(table, null, values);

            if(id > 0) {
                mBackupManager.dataChanged();

                final Uri rowUri = ContentUris.withAppendedId(uri, id);
                getContext().getContentResolver().notifyChange(rowUri, null);
                return rowUri;
            }
        }

        throw new SQLiteException("Failed to insert row into " + uri.toString());
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        String table;
        values = new ContentValues(values);

        switch(sUriMatcher.match(uri)) {
            case ENTRIES:
                table = Tables.Entries.TABLE_NAME;
                if(values.containsKey(Tables.Entries.MAKER)
                        || values.containsKey(Tables.Entries.ORIGIN)) {
                    processMaker(values);
                }
                break;
            case ENTRIES_ID:
                table = Tables.Entries.TABLE_NAME;
                if(values.containsKey(Tables.Entries.MAKER)
                        || values.containsKey(Tables.Entries.ORIGIN)) {
                    processMaker(values);
                }
                selection = appendWhere(selection,
                        Tables.Entries._ID + " = " + uri.getLastPathSegment());
                break;
            case CATS:
                table = Tables.Cats.TABLE_NAME;
                values.remove(Tables.Cats.PRESET);
                filterPreset(values.getAsString(Tables.Cats.NAME));
                selection = appendWhere(selection, Tables.Cats.PRESET + " = 0");
                break;
            case CATS_ID:
                table = Tables.Cats.TABLE_NAME;
                values.remove(Tables.Cats.PRESET);
                filterPreset(values.getAsString(Tables.Cats.NAME));
                selection = appendWhere(selection, Tables.Cats.PRESET + " = 0");
                selection = appendWhere(selection,
                        Tables.Cats._ID + " = " + uri.getLastPathSegment());
                break;
            case EXTRAS:
                table = Tables.Extras.TABLE_NAME;
                values.remove(Tables.Extras.PRESET);
                filterPreset(values.getAsString(Tables.Extras.NAME));
                selection = appendWhere(selection, Tables.Extras.PRESET + " = 0");
                break;
            case EXTRAS_ID:
                table = Tables.Extras.TABLE_NAME;
                values.remove(Tables.Extras.PRESET);
                filterPreset(values.getAsString(Tables.Extras.NAME));
                selection = appendWhere(selection, Tables.Extras.PRESET + " = 0");
                selection = appendWhere(selection,
                        Tables.Extras._ID + " = " + uri.getLastPathSegment());
                break;
            case CATS_EXTRAS:
                table = Tables.Extras.TABLE_NAME;
                values.remove(Tables.Extras.PRESET);
                filterPreset(values.getAsString(Tables.Extras.NAME));
                selection = appendWhere(selection, Tables.Extras.PRESET + " = 0");
                selection = appendWhere(selection,
                        Tables.Extras.CAT + " = " + uri.getPathSegments().get(1));
                break;
            case ENTRIES_EXTRAS:
                table = Tables.EntriesExtras.TABLE_NAME;
                selection = appendWhere(selection,
                        Tables.EntriesExtras.ENTRY + " = " + uri.getPathSegments().get(1));
                break;
            case FLAVORS:
                table = Tables.Flavors.TABLE_NAME;
                break;
            case FLAVORS_ID:
                table = Tables.Flavors.TABLE_NAME;
                selection = appendWhere(selection,
                        Tables.Flavors._ID + " = " + uri.getLastPathSegment());
                break;
            case CATS_FLAVOR:
                table = Tables.Flavors.TABLE_NAME;
                selection = appendWhere(selection,
                        Tables.Flavors.CAT + " = " + uri.getPathSegments().get(1));
                break;
            case ENTRIES_FLAVOR:
                table = Tables.EntriesFlavors.TABLE_NAME;
                selection = appendWhere(selection,
                        Tables.EntriesFlavors.ENTRY + " = " + uri.getPathSegments().get(1));
                break;
            case PHOTOS:
                table = Tables.Photos.TABLE_NAME;
                break;
            case PHOTOS_ID:
                table = Tables.Photos.TABLE_NAME;
                selection = appendWhere(selection,
                        Tables.Photos._ID + " = " + uri.getLastPathSegment());
                break;
            case ENTRIES_PHOTOS:
                table = Tables.Photos.TABLE_NAME;
                selection = appendWhere(selection,
                        Tables.Photos.ENTRY + " = " + uri.getLastPathSegment());
                break;
            case LOCATIONS:
            case LOCATIONS_ID:
                throw new IllegalArgumentException("Update not permitted on: " + uri.toString());
            case ENTRIES_FILTER:
            case MAKERS:
            case MAKERS_ID:
            case MAKERS_FILTER:
                throw new IllegalArgumentException("URI is read-only: " + uri.toString());
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri.toString());
        }

        synchronized(FlavordexProvider.class) {
            final int count = mDbHelper.getWritableDatabase().update(table, values, selection,
                    selectionArgs);

            if(count > 0) {
                mBackupManager.dataChanged();
                getContext().getContentResolver().notifyChange(uri, null);
            }

            return count;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        String table;

        switch(sUriMatcher.match(uri)) {
            case ENTRIES:
                table = Tables.Entries.TABLE_NAME;
                break;
            case ENTRIES_ID:
                table = Tables.Entries.TABLE_NAME;
                selection = appendWhere(selection,
                        Tables.Entries._ID + " = " + uri.getLastPathSegment());
                break;
            case CATS:
                table = Tables.Cats.TABLE_NAME;
                selection = appendWhere(selection, Tables.Cats.PRESET + " = 0");
                break;
            case CATS_ID:
                table = Tables.Cats.TABLE_NAME;
                selection = appendWhere(selection, Tables.Cats.PRESET + " = 0");
                selection = appendWhere(selection,
                        Tables.Cats._ID + " = " + uri.getLastPathSegment());
                break;
            case EXTRAS:
                table = Tables.Extras.TABLE_NAME;
                selection = appendWhere(selection, Tables.Extras.PRESET + " = 0");
                break;
            case EXTRAS_ID:
                table = Tables.Extras.TABLE_NAME;
                selection = appendWhere(selection, Tables.Extras.PRESET + " = 0");
                selection = appendWhere(selection,
                        Tables.Extras._ID + " = " + uri.getLastPathSegment());
                break;
            case CATS_EXTRAS:
                table = Tables.Extras.TABLE_NAME;
                selection = appendWhere(selection, Tables.Extras.PRESET + " = 0");
                selection = appendWhere(selection,
                        Tables.Extras.CAT + " = " + uri.getPathSegments().get(1));
                break;
            case ENTRIES_EXTRAS:
                table = Tables.EntriesExtras.TABLE_NAME;
                selection = appendWhere(selection,
                        Tables.EntriesExtras.ENTRY + " = " + uri.getPathSegments().get(1));
                break;
            case FLAVORS:
                table = Tables.Flavors.TABLE_NAME;
                break;
            case FLAVORS_ID:
                table = Tables.Flavors.TABLE_NAME;
                selection = appendWhere(selection,
                        Tables.Flavors._ID + " = " + uri.getLastPathSegment());
                break;
            case CATS_FLAVOR:
                table = Tables.Flavors.TABLE_NAME;
                selection = appendWhere(selection,
                        Tables.Flavors.CAT + " = " + uri.getPathSegments().get(1));
                break;
            case ENTRIES_FLAVOR:
                table = Tables.EntriesFlavors.TABLE_NAME;
                selection = appendWhere(selection,
                        Tables.EntriesFlavors.ENTRY + " = " + uri.getPathSegments().get(1));
                break;
            case PHOTOS:
                table = Tables.Photos.TABLE_NAME;
                break;
            case PHOTOS_ID:
                table = Tables.Photos.TABLE_NAME;
                selection = appendWhere(selection,
                        Tables.Photos._ID + " = " + uri.getLastPathSegment());
                break;
            case ENTRIES_PHOTOS:
                table = Tables.Photos.TABLE_NAME;
                selection = appendWhere(selection,
                        Tables.Photos.ENTRY + " = " + uri.getLastPathSegment());
                break;
            case LOCATIONS:
            case LOCATIONS_ID:
                throw new IllegalArgumentException("Delete not permitted on: " + uri.toString());
            case ENTRIES_FILTER:
            case MAKERS:
            case MAKERS_ID:
            case MAKERS_FILTER:
                throw new IllegalArgumentException("URI is read-only: " + uri.toString());
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri.toString());
        }

        synchronized(FlavordexProvider.class) {
            final int count =
                    mDbHelper.getWritableDatabase().delete(table, selection, selectionArgs);

            if(count > 0) {
                mBackupManager.dataChanged();
                getContext().getContentResolver().notifyChange(uri, null);
            }

            return count;
        }
    }


    /**
     * Find or create a maker based on data in values, replacing relevant values with the maker's
     * ID. Used while inserting or updating an entry.
     *
     * @param values ContentValues containing the name and/or origin of the maker
     */
    private void processMaker(ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String maker = values.getAsString(Tables.Entries.MAKER);
        if(maker == null) {
            maker = "";
        }

        String origin = values.getAsString(Tables.Entries.ORIGIN);
        if(origin == null) {
            origin = "";
        }

        final Cursor cursor = db.query(Tables.Makers.TABLE_NAME, new String[] {Tables.Makers._ID},
                Tables.Makers.NAME + " = ? AND " + Tables.Makers.LOCATION + " = ?",
                new String[] {maker, origin}, null, null, null, "1");
        try {
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                values.put(Tables.Entries.MAKER, cursor.getLong(0));
            } else {
                final ContentValues makerValues = new ContentValues();
                makerValues.put(Tables.Makers.NAME, maker);
                makerValues.put(Tables.Makers.LOCATION, origin);

                synchronized(FlavordexProvider.class) {
                    final long makerId = db.insert(Tables.Makers.TABLE_NAME, null, makerValues);
                    values.put(Tables.Entries.MAKER, makerId);
                }
            }
        } finally {
            cursor.close();
        }

        values.remove(Tables.Entries.ORIGIN);
        values.remove(Tables.Entries.MAKER_ID);
    }

    /**
     * Appends a fragment to a where clause.
     *
     * @param selection The original where clause
     * @param fragment  The fragment to add
     * @return The original where clause ANDed to the new fragment
     */
    private static String appendWhere(String selection, String fragment) {
        if(TextUtils.isEmpty(selection)) {
            return fragment;
        } else {
            return fragment + " AND " + selection;
        }
    }

    /**
     * Prevent reserved names from being inserted by the user.
     *
     * @param name The name of the item to check
     */
    private static void filterPreset(String name) {
        if(!TextUtils.isEmpty(name) && name.charAt(0) == '_') {
            throw new SQLiteException("Illegal name: " + name);
        }
    }
}
