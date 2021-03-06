package lpi.sauvegardesamba.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import lpi.sauvegardesamba.MainActivity;

/**
 * Gestionnaire de l'historique
 */
public class HistoriqueDatabase
{
private static HistoriqueDatabase INSTANCE = null;
/**
 * Instance unique non préinitialisée
 */
private SQLiteDatabase database;
private DatabaseHelper dbHelper;

private HistoriqueDatabase(Context context)
{
	dbHelper = new DatabaseHelper(context);
	database = dbHelper.getWritableDatabase();
}

/**
 * Point d'accès pour l'instance unique du singleton
 */
public static synchronized HistoriqueDatabase getInstance(Context context)
{
	if (INSTANCE == null)
	{
		INSTANCE = new HistoriqueDatabase(context);
	}
	return INSTANCE;
}

@Override
public void finalize()
{
	try
	{
		super.finalize();
	} catch (Throwable throwable)
	{
		throwable.printStackTrace();
	}
	dbHelper.close();
}


public void Ajoute(int Date, String ligne)
{
	ContentValues initialValues = new ContentValues();
	initialValues.put(DatabaseHelper.COLONNE_HISTORIQUE_DATE, Date);
	initialValues.put(DatabaseHelper.COLONNE_HISTORIQUE_LIGNE, ligne);
	try
	{
		database.insert(DatabaseHelper.TABLE_HISTORIQUE, null, initialValues);
	} catch (Exception e)
	{
		MainActivity.SignaleErreur("ajout d'une ligne d'historique", e);
	}
}

public Cursor getCursor()
{
	return database.query(DatabaseHelper.TABLE_HISTORIQUE, null, null, null, null, null, DatabaseHelper.COLONNE_HISTORIQUE_ID + " DESC");
}

public void Vide()
{
	database.delete(DatabaseHelper.TABLE_HISTORIQUE, null, null);
}
}
