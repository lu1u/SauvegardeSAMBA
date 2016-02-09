package lpi.sauvegardesamba.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import lpi.sauvegardesamba.MainActivity;
import lpi.sauvegardesamba.profils.Profil;

/**
 * Created by lucien on 26/01/2016.
 */
@SuppressWarnings("ALL")
public class ProfilsDatabase
{
public static final int INVALID_ID = -1;

public static final int S_JAMAIS = 0;              // Ne jamais integrer ce profil dans une sauvegarde automatique
public static final int S_AUTO_WIFI = 1;           // Integration dans les sauvegardes automatiques uniquement si on est connecte en WIFI
public static final int S_AUTO_TOUJOURS = 2;       // Integration dans les sauvegardes automatiques qu'on soit connecte en donnees mobiles ou WIFI
/**
 * Instance unique non préinitialisée
 */
private static ProfilsDatabase INSTANCE = null;
private SQLiteDatabase database;
private DatabaseHelper dbHelper;

private ProfilsDatabase(Context context)
{
	dbHelper = new DatabaseHelper(context);
	database = dbHelper.getReadableDatabase();
}

/**
 * Point d'accès pour l'instance unique du singleton
 */
public static synchronized ProfilsDatabase getInstance(Context context)
{
	if (INSTANCE == null)
	{
		INSTANCE = new ProfilsDatabase(context);
	}
	return INSTANCE;
}

@Override
public void finalize()
{
	dbHelper.close();
}

/***
 * Ajoute le profil
 *
 * @param profil
 */
public void Ajoute(Profil profil)
{
	ContentValues initialValues = new ContentValues();
	profil.toContentValues(initialValues, false);

	try
	{
		int id = (int) database.insert(DatabaseHelper.TABLE_PROFILS, null, initialValues);
	} catch (Exception e)
	{
		MainActivity.SignaleErreur("ajout du profil", e);
	}
}

/***
 * Retourne un profil cree a partir de la base de donnnees
 *
 * @param Id
 * @return profil
 */
public Profil getProfil(int Id)
{
	Profil profil = null;
	Cursor cursor = null;
	try
	{
		String[] colonnes = null;
		String where = DatabaseHelper.COLUMN_ID + " = " + Id;
		cursor = database.query(DatabaseHelper.TABLE_PROFILS, colonnes, where, null, null, null, null);
		cursor.moveToFirst();
		profil = new Profil(cursor);
		return profil;
	} catch (SQLException e)
	{
		e.printStackTrace();
	} finally
	{
		if (cursor != null)
			cursor.close();
	}

	return profil;

}

public void ModifieProfil(Profil profil)
{
	try
	{
		ContentValues valeurs = new ContentValues();
		profil.toContentValues(valeurs, true);
		database.update(DatabaseHelper.TABLE_PROFILS, valeurs, DatabaseHelper.COLUMN_ID + " = " + profil.Id, null);
	} catch (Exception e)
	{
		MainActivity.SignaleErreur("modification du profil", e);
	}
}

public void SupprimeProfil(Profil profil)
{
	try
	{
		database.delete(DatabaseHelper.TABLE_PROFILS, DatabaseHelper.COLUMN_ID + " = " + profil.Id, null);
	} catch (Exception e)
	{
		MainActivity.SignaleErreur("suppression profil", e);
	}
}


public Cursor getCursor()
{
	return database.query(DatabaseHelper.TABLE_PROFILS, null, null, null, null, null, null);
}

public long nbProfils()
{
	Cursor cursor = database.rawQuery("SELECT COUNT (*) FROM " + DatabaseHelper.TABLE_PROFILS, null);
	int count = 0;
	if (null != cursor)
		if (cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			count = cursor.getInt(0);
		}
	cursor.close();

	return count;
}

public void ChangeDate(int Id, int derniereSauvegarde)
{
	try
	{
		ContentValues valeurs = new ContentValues();
		valeurs.put(DatabaseHelper.COLUMN_DERNIERE_SAUVEGARDE, derniereSauvegarde);

		database.update(DatabaseHelper.TABLE_PROFILS, valeurs, DatabaseHelper.COLUMN_ID + " = " + Id, null);
	} catch (Exception e)
	{
		MainActivity.SignaleErreur("modification du profil", e);
	}
}
}
