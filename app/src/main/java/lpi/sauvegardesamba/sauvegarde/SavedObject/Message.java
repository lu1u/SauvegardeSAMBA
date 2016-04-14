package lpi.sauvegardesamba.sauvegarde.SavedObject;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
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
 * Objet representant un message SMS du telephone
 * Created by lucien on 29/01/2016.
 */
public class Message extends SavedObject
{
static final int LG_TITRE_MAX = 100;
static int COLONNE_DATE;
static int COLONNE_ADDRESS;
static int COLONNE_DATE_SENT;
static int COLONNE_BODY;
static int COLONNE_LOCKED;
static int COLONNE_PERSON;
static int COLONNE_PROTOCOL;
static int COLONNE_READ;
static int COLONNE_REPLY_PATH_PRESENT;
static int COLONNE_SEEN;
static int COLONNE_SERVICE_CENTER;
static int COLONNE_STATUS;
static int COLONNE_SUBJECT;
static int COLONNE_THREAD_ID;
static int COLONNE_TYPE;

final long _date;
final String _address;
final Long _dateSent;
final String _body;
final int _locked;
final int _personn;
final int _protocol;
final int _read;
final int _replyPathPresent;
final int _seen;
final String _serviceCenter;
final int _status;
final String _subject;
final int _threadId;
final int _type;
final String _contact;

public Message(Cursor cursor, Context context)
{
	_date = cursor.getLong(COLONNE_DATE);
	_address = cursor.getString(COLONNE_ADDRESS);
	_dateSent = cursor.getLong(COLONNE_DATE_SENT);
	_body = cursor.getString(COLONNE_BODY);
	_locked = cursor.getInt(COLONNE_LOCKED);
	_personn = cursor.getInt(COLONNE_PERSON);
	_protocol = cursor.getInt(COLONNE_PROTOCOL);
	_read = cursor.getInt(COLONNE_READ);
	_replyPathPresent = cursor.getInt(COLONNE_REPLY_PATH_PRESENT);
	_seen = cursor.getInt(COLONNE_SEEN);
	_serviceCenter = cursor.getString(COLONNE_SERVICE_CENTER);
	_status = cursor.getInt(COLONNE_STATUS);
	_subject = cursor.getString(COLONNE_SUBJECT);
	_threadId = cursor.getInt(COLONNE_THREAD_ID);
	_type = cursor.getInt(COLONNE_TYPE);
	_contact = getContact(context, _address);
}

@Nullable
static Cursor getList(Context context)
{
	Uri u = Uri.parse("content://sms");
	try
	{
		Cursor cursor = context.getContentResolver().query(u, null, null, null, null);
		if (cursor != null)
		{
			COLONNE_DATE = cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.DATE);
			COLONNE_ADDRESS = cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.ADDRESS);
			COLONNE_DATE_SENT = cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.DATE_SENT);
			COLONNE_BODY = cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.BODY);
			COLONNE_LOCKED = cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.LOCKED);
			COLONNE_PERSON = cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.PERSON);
			COLONNE_PROTOCOL = cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.PROTOCOL);
			COLONNE_READ = cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.READ);
			COLONNE_REPLY_PATH_PRESENT = cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.REPLY_PATH_PRESENT);
			COLONNE_SEEN = cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.SEEN);
			COLONNE_SERVICE_CENTER = cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.SERVICE_CENTER);
			COLONNE_STATUS = cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.STATUS);
			COLONNE_SUBJECT = cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.SUBJECT);
			COLONNE_THREAD_ID = cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.THREAD_ID);
			COLONNE_TYPE = cursor.getColumnIndexOrThrow(Telephony.TextBasedSmsColumns.TYPE);
		}
		return cursor;
	} catch (Exception e)
	{
		return null;
	}
}

public String getFileName(Context context)
{
	String res = "[" + sqliteDateHourToString(context, _date) + "] [";

	switch (_type)
	{
		case Telephony.Sms.MESSAGE_TYPE_INBOX:
			res += _contact + "] ";
			break;


		case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX:
		case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT:
			res += "moi] ";
			break;
		default:
	}

	String titre;
	if (_subject != null)
		titre = _subject;
	else
		titre = _body;

	if (titre.length() <= LG_TITRE_MAX)
		res += titre;
	else
		res += titre.substring(0, LG_TITRE_MAX - 1);

	return cleanFileName(res) + ".txt";
}

public SauvegardeReturnCode sauvegarde(@NonNull SmbFile smbRoot, @NonNull Context context, NtlmPasswordAuthentication authentification) throws IOException
{
	String path = smbRoot.getCanonicalPath();
	String messagePath = SavedObject.Combine(path, getFileName(context));
	SmbFile messageSmbFile = new SmbFile(messagePath, authentification);
	if (messageSmbFile.exists())
		return SauvegardeReturnCode.EXISTE_DEJA;
	Log.d("SAVE", "Message path:" + path);
	SmbFileOutputStream sops;
	try
	{
		sops = new SmbFileOutputStream(messageSmbFile);

		sops.write((_body + "\n").getBytes());
		sops.write("*****************************************\n".getBytes());
		switch (_type)
		{
			case Telephony.Sms.MESSAGE_TYPE_INBOX:
				sops.write(("Reçu le " + sqliteDateHourToString(context, _date) + " de " + _contact).getBytes());
				break;


			case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX:
			case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT:
				sops.write(("Envoyé le " + sqliteDateHourToString(context, _date) + " à " + _contact).getBytes());

				break;
			default:
		}

		sops.write(BEGIN_DATA.getBytes());
		sops.write(("\nDATE " + _date).getBytes());
		sops.write(("\nADDRESS " + _address).getBytes());
		sops.write(("\nDATESENT " + _dateSent).getBytes());
		sops.write(("\nBODY " + _body).getBytes());
		sops.write(("\nLOCKED " + _locked).getBytes());
		sops.write(("\nPERSON " + _personn).getBytes());
		sops.write(("\nPROTOCOL " + _protocol).getBytes());
		sops.write(("\nREAD " + _read).getBytes());
		sops.write(("\nREPLYPATHPRESENT " + _replyPathPresent).getBytes());
		sops.write(("\nSEEN " + _seen).getBytes());
		sops.write(("\nSERVICECENTER " + _serviceCenter).getBytes());
		sops.write(("\nSTATUS " + _status).getBytes());
		sops.write(("\nSUBJECT " + _subject).getBytes());
		sops.write(("\nTHREADID " + _threadId).getBytes());
		sops.write(("\nTYPE " + _type).getBytes());
		sops.write(ENDDATA.getBytes());

		sops.close();
		messageSmbFile.setLastModified(_date);
		return SauvegardeReturnCode.OK;
	} catch (IOException e)
	{
		Report report = Report.getInstance(context);
		report.log(Report.NIVEAU.ERROR, "Erreur lors de la sauvegarde du message " + sqliteDateHourToString(context, _date) + " : " + _address);
		report.log(Report.NIVEAU.ERROR, e);
		return SauvegardeReturnCode.ERREUR_CREATION_FICHIER;
	}
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
			res += " de " + _contact;
			break;


		case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX:
		case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT:
			res += " à " + _contact;
			break;
		default:
	}

	return res;
}

@NonNull
@Override
public String getCategorie()
{
	return _contact;
}
}
