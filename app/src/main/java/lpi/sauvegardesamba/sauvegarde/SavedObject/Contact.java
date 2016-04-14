package lpi.sauvegardesamba.sauvegarde.SavedObject;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
private static final String[] COLONNES = {
		ContactsContract.Contacts._ID,
		ContactsContract.Contacts.LOOKUP_KEY,
		ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
		ContactsContract.Contacts.PHOTO_ID,
		ContactsContract.Contacts.PHOTO_URI,

		ContactsContract.Contacts.DISPLAY_NAME,
		ContactsContract.Contacts.HAS_PHONE_NUMBER,
		ContactsContract.Contacts.IN_VISIBLE_GROUP,
		ContactsContract.Contacts.IS_USER_PROFILE,

		ContactsContract.Contacts.TIMES_CONTACTED,
		ContactsContract.Contacts.LAST_TIME_CONTACTED};

private static final String[] COLONNES_PHONENUMBER = {ContactsContract.CommonDataKinds.Phone.NUMBER};
private static final String[] COLONNES_EMAIL = {ContactsContract.CommonDataKinds.Email.DATA};
static int _colonneID;
static int _colonneDisplayName;
static int _colonneHasPhoneNumber;
static int _colonneTimesContacted;
static int _colonneLastContacted;
static int _colonneLookupKey;
static int _colonneDisplayNamePrimary;
static int _colonnePhotoId;
static int _colonnePhotoUri;
static int _colonneInVisibleGroup;
static int _colonneIsUserProfile;

public String _nom;
public long _lastContacted;
public long _timesContacted;
public String[] _numeros;
public String[] _eMails;
public long _LastContacted;
public String _LookupKey;
public String _DisplayNamePrimary;
public String _PhotoId;
public String _PhotoUri;
public String _InDefaultDirectory;
public int _InVisibleGroup;
public String _IsUserProfile;


public Contact(@NonNull Cursor cursor, @NonNull Context context)
{
	String contact_id = cursor.getString(_colonneID);
	_nom = cursor.getString(_colonneDisplayName);
	_lastContacted = cursor.getLong(_colonneLastContacted);
	_timesContacted = cursor.getLong(_colonneTimesContacted);
	_LastContacted = cursor.getLong(_colonneLastContacted);
	_LookupKey = cursor.getString(_colonneLookupKey);
	_DisplayNamePrimary = cursor.getString(_colonneDisplayNamePrimary);
	_PhotoId = cursor.getString(_colonnePhotoId);
	_PhotoUri = cursor.getString(_colonnePhotoUri);
	_InVisibleGroup = cursor.getInt(_colonneInVisibleGroup);
	_IsUserProfile = cursor.getString(_colonneIsUserProfile);


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

@Nullable
public static Cursor getCursor(Context context)
{
	Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, COLONNES, null, null, null);
	if (cursor != null)
		try
		{
			_colonneID = cursor.getColumnIndexOrThrow(BaseColumns._ID);
			_colonneDisplayName = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME);
			_colonneHasPhoneNumber = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER);
			_colonneTimesContacted = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.TIMES_CONTACTED);
			_colonneLastContacted = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LAST_TIME_CONTACTED);
			_colonneLookupKey = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY);
			_colonneDisplayNamePrimary = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
			_colonnePhotoId = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_ID);
			_colonnePhotoUri = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI);
			_colonneInVisibleGroup = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.IN_VISIBLE_GROUP);
			_colonneIsUserProfile = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.IS_USER_PROFILE);

		} catch (Exception e)
		{
			return null;
		}

	return cursor;
}

@Override
public
@NonNull
String Nom(@NonNull Context context)
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
public SauvegardeReturnCode sauvegarde(@NonNull SmbFile smbRoot, @NonNull Context context, NtlmPasswordAuthentication authentification) throws IOException
{
	Report report = Report.getInstance(context);
	// Path de cet appel
	String path = smbRoot.getCanonicalPath();
	String contactPath = SavedObject.Combine(path, getFileName(context));
	SmbFile contactSmbFile = new SmbFile(contactPath, authentification);
	if (contactSmbFile.exists())
		return SauvegardeReturnCode.EXISTE_DEJA;
	report.log(Report.NIVEAU.DEBUG, "Contact path:" + contactPath);

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
				report.log(Report.NIVEAU.ERROR, "Erreur lors de l'ecriture du contact" + _nom);
				report.log(Report.NIVEAU.ERROR, e);
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

		sops.write(BEGIN_DATA.getBytes());
		sops.write(("\nNAME " + _nom).getBytes());
		sops.write(("\nTIMESCONTACTED " + _timesContacted).getBytes());
		sops.write(("\nLASTCONTACTED " + _LastContacted).getBytes());
		sops.write(("\nLOOKUPKEY " + _LookupKey).getBytes());
		sops.write(("\nDISPLAYNAMEPRIMARY " + _DisplayNamePrimary).getBytes());
		sops.write(("\nPHOTOID " + _PhotoId).getBytes());
		sops.write(("\nPHOTOURI " + _PhotoUri).getBytes());
		sops.write(("\nISVISIBLEGROUP " + _InVisibleGroup).getBytes());
		sops.write(("\nISUSERPROFILE " + _IsUserProfile).getBytes());
		if (_numeros != null)
		{
			// Numeros de telephone
			for (String tel : _numeros)
				sops.write(("\nPHONE " + tel).getBytes());
		}

		if (_eMails != null)
		{
			// Adresses mail
			for (String mail : _eMails)
				sops.write(("\nEMAIL " + mail + "\n").getBytes());
		}
		sops.write(ENDDATA.getBytes());
	} catch (IOException e)
	{
		report.log(Report.NIVEAU.ERROR, "Erreur lors de la sauvegarde du ic_contact " + _nom);
		report.log(Report.NIVEAU.ERROR, e);
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

