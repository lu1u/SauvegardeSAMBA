package lpi.sauvegardesamba.sauvegarde.SavedObject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
static final private String COLONNES[] = new String[]
		{
				CallLog.Calls.NUMBER,
				CallLog.Calls.DATE,
				CallLog.Calls.DURATION,
				CallLog.Calls.TYPE,
				CallLog.Calls.GEOCODED_LOCATION,
				CallLog.Calls.IS_READ,
		};


private static int _colNumber;
private static int _colDate;
private static int _colDuration;
private static int _colType;
private static int _colGeocodedLocation;
private static int _colIsRead;


public final String _number;
public final long _date;
public final long _duration;
public final int _type;
public final String _address;
public final String _geoCodedLocation;
public final int _isRead;


public Appel(@NonNull Cursor cursor, @NonNull Context context)
{
	_date = cursor.getLong(_colDate);
	_duration = cursor.getLong(_colDuration);
	_number = cursor.getString(_colNumber);
	_type = cursor.getInt(_colType);
	_address = getContact(context, _number);

	_geoCodedLocation = cursor.getString(_colGeocodedLocation);
	_isRead = cursor.getInt(_colIsRead);
}

@Nullable
public static Cursor getCursor(@NonNull Context context)
{
	if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
	{
		return null;
	}
	Cursor cursor = context.getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI, COLONNES, null, null, null);
	if (cursor != null)
		try
		{
			_colNumber = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER);
			_colDate = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE);
			_colDuration = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION);
			_colType = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE);
			_colGeocodedLocation = cursor.getColumnIndexOrThrow(CallLog.Calls.GEOCODED_LOCATION);
			_colIsRead = cursor.getColumnIndexOrThrow(CallLog.Calls.IS_READ);
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
	return _address;
}

@NonNull
public String getFileName(@NonNull Context context)
{
	return cleanFileName(Nom(context)) + ".txt";
}

@Override
public SauvegardeReturnCode sauvegarde(@NonNull SmbFile smbRoot, @NonNull Context context, NtlmPasswordAuthentication authentification) throws IOException
{
	// Path de cet appel
	String path = smbRoot.getCanonicalPath();
	String appelPath = SavedObject.Combine(path, getFileName(context));
	SmbFile appelSmbFile = new SmbFile(appelPath, authentification);
	if (appelSmbFile.exists())
		return SauvegardeReturnCode.EXISTE_DEJA;
	Log.d("SAVE", "Message path:" + appelPath);

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
		sops.write(BEGIN_DATA.getBytes());
		sops.write(("\nADDRESS " + _address).getBytes());
		sops.write(("\nTYPE " + _type).getBytes());
		sops.write(("\nNUMBER " + _number).getBytes());
		sops.write(("\nDATE " + _date).getBytes());
		sops.write(("\nDURATION " + _duration).getBytes());
		sops.write(("\nTYPE " + _type).getBytes());
		sops.write(("\nADDRESS " + _address).getBytes());
		sops.write(("\nGEOLOCATION " + _geoCodedLocation).getBytes());
		sops.write(("\nIS_READ " + _isRead).getBytes());
		sops.write(ENDDATA.getBytes());

	} catch (IOException e)
	{
		Report report = Report.getInstance(context);
		report.log(Report.NIVEAU.ERROR, "Erreur lors de la sauvegarde de l'appel " + _address + sqliteDateHourToString(context, _date));
		report.log(Report.NIVEAU.ERROR, e);
		return SauvegardeReturnCode.ERREUR_CREATION_FICHIER;
	} finally
	{
		if (sops != null)
			sops.close();
	}

	appelSmbFile.setLastModified(_date);
	return SauvegardeReturnCode.OK;
}


}
