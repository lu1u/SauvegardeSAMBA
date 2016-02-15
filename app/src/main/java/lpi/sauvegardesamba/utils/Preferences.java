package lpi.sauvegardesamba.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import lpi.sauvegardesamba.database.DatabaseHelper;


/**
 * Gestionnaire des preferences de l'application
 */
public class Preferences
{
private static final String PREF_SAUVEGARDE_EN_COURS = "SauvegardeEnCours"; //$NON-NLS-1$
private static final String PREF_SAUVEGARDE_AUTO_HEURE = "AutoHeure"; //$NON-NLS-1$
private static final String PREF_SAUVEGARDE_AUTO_MINUTE = "AutoMinute"; //$NON-NLS-1$
private static final String PREF_SAUVEGARDE_AUTO_ACTIVEE = "AutoActivee"; //$NON-NLS-1$
private static final String PREF_DETECTE_CONNEXION_WIFI = "ConnxionWIFI";
private static final String PREF_REGROUPER_APPELS = "RegroupeAppels";
private static final String PREF_REGROUPER_MESSAGES = "RegroupeMessages";
private static final String PREF_REGROUPER_PHOTOS = "RegroupePhotos";
private static final String PREF_REGROUPER_VIDEOS = "RegroupeVideos";
private static final String PREF_THEME = "Theme";
private static final String PREF_REPERTOIRE_SAUVEGARDE = "RepSauvegarde"; //$NON-NLS-1$
private static final String PREF_REPERTOIRE_CONTACTS = "RepContacts";
private static final String PREF_REPERTOIRE_APPELS = "RepAppels";
private static final String PREF_REPERTOIRE_MESSAGES = "RepMessages";
private static final String PREF_REPERTOIRE_PHOTOS = "RepPhotos";
private static final String PREF_REPERTOIRE_VIDEOS = "RepVideos";


private static Preferences INSTANCE = null;
private SQLiteDatabase database;
private DatabaseHelper dbHelper;

private Preferences(Context context)
{
	dbHelper = new DatabaseHelper(context);
	database = dbHelper.getWritableDatabase();
}

/**
 * Point d'accès pour l'instance unique du singleton
 */
@NonNull
public static synchronized Preferences getInstance(@NonNull Context context)
{
	if (INSTANCE == null)
	{
		INSTANCE = new Preferences(context);
	}
	return INSTANCE;
}


public void putString(String name, String s)
{
	ContentValues values = new ContentValues();
	values.put(DatabaseHelper.COLONNE_PREF_STRING_NAME, name);
	values.put(DatabaseHelper.COLONNE_PREF_STRING_VALEUR, s);

	database.beginTransaction();
	boolean present = trouveId(DatabaseHelper.TABLE_PREFERENCES_STRING, DatabaseHelper.COLONNE_PREF_STRING_NAME, name);
	try
	{
		if (present)
			database.update(DatabaseHelper.TABLE_PREFERENCES_STRING, values, DatabaseHelper.COLONNE_PREF_STRING_NAME + "=?", new String[]{name});
		else
			database.insert(DatabaseHelper.TABLE_PREFERENCES_STRING, null, values);
		database.setTransactionSuccessful();
	} catch (Exception e)
	{
		Log.e("SAMBA", e.getMessage());
	} finally
	{
		database.endTransaction();
	}
}

private boolean trouveId(String tableName, String colonneID, String name)
{
	Cursor c = database.query(tableName, new String[]{colonneID}, colonneID + " =?", new String[]{name}, null, null, null, null);
	boolean result = false;

	if (c != null)
	{
		if (c.moveToFirst()) //if the row exist then return the id
			result = true;
		c.close();
	}
	return result;
}

public String getString(String name, String defaut)
{
	String result = defaut;
	try
	{
		String where = DatabaseHelper.COLONNE_PREF_STRING_NAME + " = \"" + name + "\"";
		Cursor cursor = database.query(DatabaseHelper.TABLE_PREFERENCES_STRING, null, where, null, null, null, null);
		if (cursor != null)
		{
			if (cursor.moveToFirst())
				result = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLONNE_PREF_STRING_VALEUR));
			cursor.close();
		}
	} catch (SQLException e)
	{
		e.printStackTrace();
	}
	return result;
}

public void putInt(String name, int i)
{
	ContentValues values = new ContentValues();
	values.put(DatabaseHelper.COLONNE_PREF_INT_NAME, name);
	values.put(DatabaseHelper.COLONNE_PREF_INT_VALEUR, i);

	database.beginTransaction();
	boolean present = trouveId(DatabaseHelper.TABLE_PREFERENCES_INT, DatabaseHelper.COLONNE_PREF_INT_NAME, name);
	try
	{
		if (present)
			database.update(DatabaseHelper.TABLE_PREFERENCES_INT, values, DatabaseHelper.COLONNE_PREF_INT_NAME + "=?", new String[]{name});
		else
			database.insert(DatabaseHelper.TABLE_PREFERENCES_INT, null, values);
		database.setTransactionSuccessful();
	} catch (Exception e)
	{
		Log.e("SAMBA", e.getMessage());
	} finally
	{
		database.endTransaction();
	}
}

public int getInt(String name, int defaut)
{
	int result = defaut;
	try
	{
		String where = DatabaseHelper.COLONNE_PREF_INT_NAME + " = \"" + name + "\"";
		Cursor cursor = database.query(DatabaseHelper.TABLE_PREFERENCES_INT, null, where, null, null, null, null);
		if (cursor != null)
		{
			if (cursor.moveToFirst())
				result = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLONNE_PREF_INT_VALEUR));
			cursor.close();
		}
	} catch (SQLException e)
	{
		e.printStackTrace();
	}
	return result;
}

public void putBool(String name, boolean b)
{
	putInt(name, b ? 1 : 0);
}

public boolean getBool(String name, boolean defaut)
{
	int res = getInt(name, defaut ? 1 : 0);
	return (res == 0) ? false : true;
}

public boolean getSauvegardeEnCours()
{
	return getBool(PREF_SAUVEGARDE_EN_COURS, false);
}

public void setSauvegardeEnCours(boolean b)
{
	putBool(PREF_SAUVEGARDE_EN_COURS, b);
}


public int getSauvegardeAutoHeure()
{
	return getInt(PREF_SAUVEGARDE_AUTO_HEURE, 0);
}

public void setPrefSauvegardeAutoHeure(int p)
{
	putInt(PREF_SAUVEGARDE_AUTO_HEURE, p);
}

public int getSauvegardeAutoMinute()
{
	return getInt(PREF_SAUVEGARDE_AUTO_MINUTE, 0);
}

public void setPrefSauvegardeAutoMinute(int p)
{
	putInt(PREF_SAUVEGARDE_AUTO_MINUTE, p);
}

public boolean getSauvegarderAuto()
{
	return getBool(PREF_SAUVEGARDE_AUTO_ACTIVEE, false);
}

public void setSauvegardeAuto(boolean b)
{
	putBool(PREF_SAUVEGARDE_AUTO_ACTIVEE, b);
}

public String getPrefRepertoireSauvegarde()
{
	return getString(PREF_REPERTOIRE_SAUVEGARDE, "SauvegardeSAMBA");
}

public void setPrefRepertoireSauvegarde(String rep)
{
	putString(PREF_REPERTOIRE_SAUVEGARDE, rep);
}

public String getPrefRepertoireContacts()
{
	return getString(PREF_REPERTOIRE_CONTACTS, "Contacts");
}

public void setPrefRepertoireContacts(String rep)
{
	putString(PREF_REPERTOIRE_CONTACTS, rep);
}

public String getPrefRepertoireAppels()
{
	return getString(PREF_REPERTOIRE_APPELS, "Appels");
}

public void setPrefRepertoireAppels(String rep)
{
	putString(PREF_REPERTOIRE_APPELS, rep);
}

public String getPrefRepertoireMessages()
{
	return getString(PREF_REPERTOIRE_MESSAGES, "Messages");
}

public void setPrefRepertoireMessages(String rep)
{
	putString(PREF_REPERTOIRE_MESSAGES, rep);
}

public String getPrefRepertoirePhotos()
{
	return getString(PREF_REPERTOIRE_PHOTOS, "Photos");
}

public void setPrefRepertoirePhotos(String rep)
{
	putString(PREF_REPERTOIRE_PHOTOS, rep);
}

public String getPrefRepertoireVideos()
{
	return getString(PREF_REPERTOIRE_VIDEOS, "Videos");
}

public void setPrefRepertoireVideos(String rep)
{
	putString(PREF_REPERTOIRE_VIDEOS, rep);
}

public boolean getRegrouperAppels()
{
	return getBool(PREF_REGROUPER_APPELS, true);
}

public void setPrefRegrouperAppels(boolean regrouper)
{
	putBool(PREF_REGROUPER_APPELS, regrouper);
}

public boolean getRegrouperMessages()
{
	return getBool(PREF_REGROUPER_MESSAGES, true);
}

public void setPrefRegrouperMessages(boolean regrouper)
{
	putBool(PREF_REGROUPER_MESSAGES, regrouper);
}

public boolean getRegrouperPhotos()
{
	return getBool(PREF_REGROUPER_PHOTOS, true);
}

public void setPrefRegrouperPhotos(boolean regrouper)
{
	putBool(PREF_REGROUPER_PHOTOS, regrouper);
}

public boolean getRegrouperVideos()
{
	return getBool(PREF_REGROUPER_VIDEOS, true);
}

public void setPrefRegrouperVideos(boolean regrouper)
{
	putBool(PREF_REGROUPER_VIDEOS, regrouper);
}


public boolean getDetectionWIFI()
{
	return getBool(PREF_DETECTE_CONNEXION_WIFI, true);
}

public void setDetectionWIFI(boolean regrouper)
{
	putBool(PREF_DETECTE_CONNEXION_WIFI, regrouper);
}

public int getTheme()
{
	return getInt(PREF_THEME, 0);
}

public void setTheme(int p)
{
	putInt(PREF_THEME, p);
}
}
