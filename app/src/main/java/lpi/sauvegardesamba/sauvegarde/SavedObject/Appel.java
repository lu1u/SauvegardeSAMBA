package lpi.sauvegardesamba.sauvegarde.SavedObject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.CallLog;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import lpi.sauvegardesamba.sauvegarde.SauvegardeReturnCode;
import lpi.sauvegardesamba.utils.Report;

/**
 * Created by lucien on 30/01/2016.
 */
public class Appel extends SavedObject
{
static private String COLONNES[] = new String[]
		{CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.DURATION, CallLog.Calls.TYPE};

private static int _colID;
private static int _colNumber;
private static int _colDate;
private static int _colDuration;
private static int _colType;

public String _ID;
public String _number;
public long _date;
public long _duration;
public int _type;
public String _address;

public Appel(Cursor cursor, Context context)
{
	_ID = cursor.getString(_colID);
	_date = cursor.getLong(_colDate);
	_duration = cursor.getLong(_colDuration);
	_number = cursor.getString(_colNumber);
	_type = cursor.getInt(_colType);
	_address = getContact(context, _number);
}

@Nullable
public static Cursor getList(Context context)
{
	if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
	{
		return null;
	}
	Cursor cursor = context.getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI, COLONNES, null, null, null);

	try
	{
		_colID = cursor.getColumnIndexOrThrow(BaseColumns._ID);
		_colNumber = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER);
		_colDate = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE);
		_colDuration = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION);
		_colType = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE);
	} catch (IllegalArgumentException e)
	{
		return null;
	}

	return cursor;
}

@Override
public String Nom(Context context)
{
	String res = "[" + sqliteDateHourToString(context, _date) + "]";

	switch (_type)
	{
		case Telephony.Sms.MESSAGE_TYPE_INBOX:
			res += " de " + _address;
			break;


		case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX:
		case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT:
			res += " à " + _address;
			break;
		default:
	}

	res += " " + sqliteDurationToString(context, _duration);

	return res;
}

@NonNull
@Override
public String getCategorie()
{
	return _address ;
}

public String getFileName(Context context)
{
	return cleanFileName(Nom(context)) + ".txt";
}

@Override
public SauvegardeReturnCode sauvegarde(SmbFile smbRoot, Context context, NtlmPasswordAuthentication authentification) throws IOException
{
	// Path de cet appel
 	String path = smbRoot.getCanonicalPath();
	String appelPath = SavedObject.Combine(path, getFileName(context));
	Log.d("SAVE", "Message path:" + path);
	SmbFile appelSmbFile = new SmbFile(appelPath, authentification);
	SmbFileOutputStream sops = null;

	try
	{
		sops = new SmbFileOutputStream(appelSmbFile);
		switch (_type)
		{
			case Telephony.Sms.MESSAGE_TYPE_INBOX:
				sops.write(("Appel le " + sqliteDateHourToString(context, _date) + " de " + _address).getBytes());
				break;


			case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX:
			case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT:
				sops.write(("Appel le " + sqliteDateHourToString(context, _date) + " à " + _address).getBytes());
				break;
			default:
		}

		sops.write(("\nDurée: " + sqliteDurationToString(context, _duration)).getBytes());
	} catch (IOException e)
	{
		Report.Log(Report.NIVEAU.ERROR, "Erreur lors de la sauvegarde de l'appel " + _address + sqliteDateHourToString(context, _date));
		Report.Log(Report.NIVEAU.ERROR, e);
		return SauvegardeReturnCode.ERREUR_CREATION_FICHIER;
	} finally
	{
		sops.close();
	}

	appelSmbFile.setLastModified(_date);
	return SauvegardeReturnCode.OK;
}


}
