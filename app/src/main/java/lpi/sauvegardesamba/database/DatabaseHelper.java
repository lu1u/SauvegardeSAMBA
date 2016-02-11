package lpi.sauvegardesamba.database;

/**
 * Utilitaire de gestion de la base de donnees
 */
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;

public class DatabaseHelper extends SQLiteOpenHelper
{

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
public static final String COLUMN_DERNIERE_SAUVEGARDE = "DERNIERE" ;
public static final String COLUMN_INTEGRATION_SAUVEGARDE_AUTO = "SAUVEGARDEAUTO";
public static final String TABLE_HISTORIQUE = "HISTORIQUE";
public static final String COLONNE_HISTORIQUE_DATE = "DATE";
public static final String COLONNE_HISTORIQUE_LIGNE = "LIGNE";
public static final String COLONNE_HISTORIQUE_ID = "_id";
public static final String TABLE_TRACES = "TRACES";
public static final String COLONNE_TRACES_ID = "_id";
public static final String COLONNE_TRACES_DATE = "DATE";
public static final String COLONNE_TRACES_NIVEAU = "NIVEAU";
public static final String COLONNE_TRACES_LIGNE = "LIGNE";
private static final String DATABASE_NAME = "database.db";
private static final int DATABASE_VERSION = 12;
// Database creation sql statement
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

@Override
public void onCreate(SQLiteDatabase database)
{
	try
	{
		database.execSQL(DATABASE_PROFILS_CREATE);
		database.execSQL(DATABASE_HISTORIQUE_CREATE);
		database.execSQL(DATABASE_TRACES_CREATE);
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
		onCreate(db);
	} catch (SQLException e)
	{
		e.printStackTrace();
	}
}
}
