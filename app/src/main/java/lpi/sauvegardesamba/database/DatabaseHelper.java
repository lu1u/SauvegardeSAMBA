package lpi.sauvegardesamba.database;

/**
 * Utilitaire de gestion de la base de donnees
 */

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;

public class DatabaseHelper extends SQLiteOpenHelper
{
public static final int DATABASE_VERSION = 19;
public static final String DATABASE_NAME = "database.db";
////////////////////////////////////////////////////////////////////////////////////////////////////
// Table des profils
public static final String TABLE_PROFILS = "PROFILS";
public static final String COLUMN_ID = "_id";
public static final String COLUMN_NOM = "NOM";
public static final String COLUMN_UTILISATEUR = "UTILISATEUR";
public static final String COLUMN_MOTDEPASSE = "MOTDEPASSE";
public static final String COLUMN_PARTAGE = "PARTAGE";
public static final String COLUMN_CONTACTS = "CONTACTS";
public static final String COLUMN_MESSAGES = "MESSAGES";
public static final String COLUMN_APPELS = "APPELS";
public static final String COLUMN_PHOTOS = "PHOTOS";
public static final String COLUMN_VIDEOS = "VIDEOS";
public static final String COLUMN_DERNIERE_SAUVEGARDE = "DERNIERE";
public static final String COLUMN_INTEGRATION_SAUVEGARDE_AUTO = "SAUVEGARDEAUTO";
////////////////////////////////////////////////////////////////////////////////////////////////////
// Table historique
public static final String TABLE_HISTORIQUE = "HISTORIQUE";
public static final String COLONNE_HISTORIQUE_DATE = "DATE";
public static final String COLONNE_HISTORIQUE_LIGNE = "LIGNE";
public static final String COLONNE_HISTORIQUE_ID = "_id";
public static final String TABLE_TRACES = "TRACES";
////////////////////////////////////////////////////////////////////////////////////////////////////
// Table traces
public static final String COLONNE_TRACES_ID = "_id";
public static final String COLONNE_TRACES_DATE = "DATE";
public static final String COLONNE_TRACES_NIVEAU = "NIVEAU";
public static final String COLONNE_TRACES_LIGNE = "LIGNE";
// Table preferences bool et int
public static final String TABLE_PREFERENCES_INT = "PREFERENCES_INT";
public static final String COLONNE_PREF_INT_NAME = "NAME";
public static final String COLONNE_PREF_INT_VALEUR = "VALEUR";
// Table preferences string
public static final String TABLE_PREFERENCES_STRING = "PREFERENCES_STRING";
public static final String COLONNE_PREF_STRING_NAME = "NAME";
public static final String COLONNE_PREF_STRING_VALEUR = "VALEUR";
private static final String DATABASE_PROFILS_CREATE = "create table "
		+ TABLE_PROFILS + "("
		+ COLUMN_ID + " integer primary key autoincrement, "
		+ COLUMN_NOM + " text not null,"
		+ COLUMN_UTILISATEUR + " text not null, "
		+ COLUMN_MOTDEPASSE + " text not null, "
		+ COLUMN_INTEGRATION_SAUVEGARDE_AUTO + " integer, "
		+ COLUMN_PARTAGE + " text not null, "
		+ COLUMN_CONTACTS + " integer, "
		+ COLUMN_APPELS + " integer, "
		+ COLUMN_MESSAGES + " integer, "
		+ COLUMN_PHOTOS + " integer ,"
		+ COLUMN_VIDEOS + " integer,"
		+ COLUMN_DERNIERE_SAUVEGARDE + " integer"
		+ ");";
private static final String DATABASE_HISTORIQUE_CREATE = "create table "
		+ TABLE_HISTORIQUE + "("
		+ COLUMN_ID + " integer primary key autoincrement, "
		+ COLONNE_HISTORIQUE_DATE + " integer,"
		+ COLONNE_HISTORIQUE_LIGNE + " text not null"
		+ ");";
private static final String DATABASE_TRACES_CREATE = "create table "
		+ TABLE_TRACES + "("
		+ COLONNE_HISTORIQUE_ID + " integer primary key autoincrement, "
		+ COLONNE_TRACES_DATE + " integer,"
		+ COLONNE_TRACES_NIVEAU + " integer,"
		+ COLONNE_TRACES_LIGNE + " text not null"
		+ ");";
private static final String DATABASE_PREF_INT_CREATE = "create table "
		+ TABLE_PREFERENCES_INT + "("
		+ COLONNE_PREF_INT_NAME + " text primary key not null, "
		+ COLONNE_PREF_INT_VALEUR + " integer "
		+ ");";
private static final String DATABASE_PREF_STRING_CREATE = "create table "
		+ TABLE_PREFERENCES_STRING + "("
		+ COLONNE_PREF_INT_NAME + " text primary key not null, "
		+ COLONNE_PREF_INT_VALEUR + " text "
		+ ");";

public DatabaseHelper(Context context)
{
	super(context, DATABASE_NAME, null, DATABASE_VERSION);
}

static public int CalendarToSQLiteDate(@Nullable Calendar cal)
{
	if (cal == null)
		cal = Calendar.getInstance();
	return (int) (cal.getTimeInMillis() / 1000L);
}

@NonNull
static public Calendar SQLiteDateToCalendar(int date)
{
	Calendar cal = Calendar.getInstance();
	cal.setTimeInMillis((long) date * 1000L);
	return cal;
}

@NonNull
public static String getStringFromAnyColumn(@NonNull Cursor cursor, int colonne)
{
	Object o = getObjectFromAnyColumn(cursor, colonne);
	if (o != null)
		return o.toString();
	else
		return "Impossible de lire la colonne " + cursor.getColumnName(colonne);
}

@Nullable
public static Object getObjectFromAnyColumn(@NonNull Cursor cursor, int colonne)
{
	try
	{
		return cursor.getInt(colonne);
	} catch (Exception e)
	{
		try
		{
			return cursor.getShort(colonne);
		} catch (Exception e1)
		{
			try
			{
				return cursor.getLong(colonne);
			} catch (Exception e2)
			{
				try
				{
					return cursor.getDouble(colonne);
				} catch (Exception e3)
				{
					try
					{
						return cursor.getFloat(colonne);
					} catch (Exception e4)
					{
						try
						{
							return cursor.getString(colonne);
						} catch (Exception e5)
						{
							Log.e("Dabase", "impossible de lire la colonne " + colonne);
						}
					}
				}
			}
		}
	}

	return null;
}

@Override
public void onCreate(SQLiteDatabase database)
{
	try
	{
		database.execSQL(DATABASE_PROFILS_CREATE);
		database.execSQL(DATABASE_HISTORIQUE_CREATE);
		database.execSQL(DATABASE_TRACES_CREATE);
		database.execSQL(DATABASE_PREF_INT_CREATE);
		database.execSQL(DATABASE_PREF_STRING_CREATE);
	} catch (SQLException e)
	{
		e.printStackTrace();
	}
}

@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
{
	try
	{
		Log.w(DatabaseHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORIQUE);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREFERENCES_INT);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREFERENCES_STRING);
		onCreate(db);
	} catch (SQLException e)
	{
		e.printStackTrace();
	}
}
}
