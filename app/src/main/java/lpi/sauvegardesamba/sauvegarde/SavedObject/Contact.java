package lpi.sauvegardesamba.sauvegarde.SavedObject;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import lpi.sauvegardesamba.sauvegarde.SauvegardeReturnCode;
import lpi.sauvegardesamba.utils.Report;

/**
 * Objet representant un contact stocke sur le telephone
 * Created by lucien on 29/01/2016.
 */
public class Contact extends SavedObject
{
private static final String[] _colonneS_CONTACT = {
		ContactsContract.Contacts._ID,
		ContactsContract.Contacts.DISPLAY_NAME,
		ContactsContract.Contacts.HAS_PHONE_NUMBER,
		ContactsContract.Contacts.TIMES_CONTACTED,
		ContactsContract.Contacts.LAST_TIME_CONTACTED};

private static final String[] COLONNES_PHONENUMBER = {ContactsContract.CommonDataKinds.Phone.NUMBER};
private static final String[] COLONNES_EMAIL = {ContactsContract.CommonDataKinds.Email.DATA};
static int _colonneID;
static int _colonneDisplayName;
static int _colonneHasPhoneNumber;
static int _colonneTimesUpdated;
static int _colonneLastContacted;
public String _nom;
public long _lastContacted;
public long _timesContacted;
public String[] _numeros;
public String[] _eMails;

public Contact(Cursor cursor, Context context)
{
	if (cursor != null)
	{

		String contact_id = cursor.getString(_colonneID);
		_nom = cursor.getString(_colonneDisplayName);
		_lastContacted = cursor.getLong(_colonneLastContacted);
		_timesContacted = cursor.getLong(_colonneTimesUpdated);

		{
			// Numeros de telephone
			int hasPhoneNumber = Integer.parseInt(cursor.getString(_colonneHasPhoneNumber));
			if (hasPhoneNumber > 0)
			{
				Cursor phoneCursor = context.getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI, COLONNES_PHONENUMBER,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] //$NON-NLS-1$
								{contact_id}, null);
				if (phoneCursor != null)
				{
					_numeros = new String[phoneCursor.getCount()];
					final int _colonneIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
					int i = 0;
					while (phoneCursor.moveToNext())
					{
						_numeros[i] = phoneCursor.getString(_colonneIndex);
						i++;
					}
					phoneCursor.close();
				}
			}
		}

		{
			// Adresses mail
			Cursor emailCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
					COLONNES_EMAIL, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
					new String[]{contact_id}, null);

			if (emailCursor != null)
			{
				if (emailCursor.getCount() > 0)
				{
					final int _colonneIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
					_eMails = new String[emailCursor.getCount()];
					int i = 0;
					while (emailCursor.moveToNext())
					{
						_eMails[i] = emailCursor.getString(_colonneIndex);
						i++;
					}
				}
				emailCursor.close();
			}
		}
	}
}

@Nullable
public static Cursor getList(Context context)
{
	Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, _colonneS_CONTACT, null, null, null);
	try
	{
		_colonneID = cursor.getColumnIndex(BaseColumns._ID);
		_colonneDisplayName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
		_colonneHasPhoneNumber = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
		_colonneTimesUpdated = cursor.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED);
		_colonneLastContacted = cursor.getColumnIndex(ContactsContract.Contacts.LAST_TIME_CONTACTED);
	} catch (Exception e)
	{
		return null;
	}

	return cursor;
}

@Override
public String Nom(Context context)
{
	return _nom;
}

@NonNull
@Override
public String getCategorie()
{
	return "";
}

/***
 * Calcule un nom de fichier pour enregistrer ce ic_contact
 */
public String getFileName(Context context)
{
	return cleanFileName(_nom) + ".txt";
}

/**
 * Sauvegarde le ic_contact dans le fichier samba donne en parametre
 *
 * @throws IOException
 */
public SauvegardeReturnCode sauvegarde(SmbFile smbRoot, Context context, NtlmPasswordAuthentication authentification) throws IOException
{
	// Path de cet appel
	String path = smbRoot.getCanonicalPath();
	String contactPath = SavedObject.Combine(path, getFileName(context));
	Log.d("SAVE", "Contact path:" + path);
	SmbFile contactSmbFile = new SmbFile(contactPath, authentification);
	SmbFileOutputStream sops = null;

	try
	{
		sops = new SmbFileOutputStream(contactSmbFile);
		sops.write((_nom + "\n").getBytes());
		if (_timesContacted > 0)
		{
			sops.write(("Contacté " + _timesContacted + " fois\n").getBytes());

			try
			{
				sops.write(("Contacté la dernière fois: " + sqliteDateHourToString(context, _lastContacted) + "\n").getBytes());
			} catch (NumberFormatException e)
			{
			}
		}

		if (_numeros != null)
		{
			// Numeros de telephone
			for (String tel : _numeros)
				sops.write(("Téléphone: " + tel + "\n").getBytes());
		}

		if (_eMails != null)
		{
			// Adresses mail
			for (String mail : _eMails)
				sops.write(("E-mail: " + mail + "\n").getBytes());
		}
	} catch (IOException e)
	{
		Report.Log(Report.NIVEAU.ERROR, "Erreur lors de la sauvegarde du ic_contact " + _nom);
		Report.Log(Report.NIVEAU.ERROR, e);
		return SauvegardeReturnCode.ERREUR_CREATION_FICHIER;
	} finally
	{
		if (sops != null)
			sops.close();
	}

	return SauvegardeReturnCode.OK;
}

public boolean quelqueChoseASauvegarder()
{
	return (_timesContacted > 0) || (_numeros != null) || (_eMails != null);
}
}

