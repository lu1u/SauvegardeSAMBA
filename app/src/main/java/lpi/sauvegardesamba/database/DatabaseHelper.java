package lpi.sauvegardesamba.database;

/**
 * Created by lucien on 26/01/2016.
 */


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper
{

public static final String TABLE_PROFILS = "PROFILS";
public static final String COLUMN_ID = "_id";
public static final String COLUMN_NOM = "NOM";
public static final String COLUMN_UTILISATEUR = "UTILISATEUR";
public static final String COLUMN_MOTDEPASSE = "MOTDEPASSE";
public static final String COLUMN_SAUVEGARDE_MANUELLE = "MANUELLE";
public static final String COLUMN_PARTAGE = "PARTAGE";
public static final String COLUMN_CONTACTS = "CONTACTS";
public static final String COLUMN_MESSAGES = "MESSAGES";
public static final String COLUMN_APPELS = "APPELS";
public static final String COLUMN_PHOTOS = "PHOTOS";
public static final String COLUMN_VIDEOS = "VIDEOS";
public static final String COLUMN_WIFI = "WIFI";
public static final String COLUMN_DERNIERE_SAUVEGARDE = "DERNIERE" ;
public static final String COLUMN_SAUVEGARDE_PLANNIFIEE = "PLANIFIEE";


private static final String DATABASE_NAME = "profils.db";
private static final int DATABASE_VERSION = 7;

// Database creation sql statement
private static final String DATABASE_CREATE = "create table "
		+ TABLE_PROFILS + "("
		+ COLUMN_ID + " integer primary key autoincrement, "
		+ COLUMN_NOM + " text not null,"
		+ COLUMN_WIFI + " integer, "
		+ COLUMN_UTILISATEUR + " text not null, "
		+ COLUMN_MOTDEPASSE + " text not null, "
		+ COLUMN_PARTAGE + " text not null, "
		+ COLUMN_CONTACTS + " integer, "
		+ COLUMN_APPELS + " integer, "
		+ COLUMN_MESSAGES + " integer, "
		+ COLUMN_PHOTOS + " integer ,"
		+ COLUMN_VIDEOS + " integer,"
		+ COLUMN_SAUVEGARDE_MANUELLE + " integer, "
		+ COLUMN_SAUVEGARDE_PLANNIFIEE + " integer, "
		+ COLUMN_DERNIERE_SAUVEGARDE + " integer"
		+ ");";

public DatabaseHelper(Context context)
{
	super(context, DATABASE_NAME, null, DATABASE_VERSION);
}

@Override
public void onCreate(SQLiteDatabase database)
{
	try
	{
		database.execSQL(DATABASE_CREATE);
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
		onCreate(db);
	} catch (SQLException e)
	{
		e.printStackTrace();
	}
}

}
