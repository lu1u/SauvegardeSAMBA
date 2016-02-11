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
static final int LG_TITRE_MAX = 50;
static int COLONNE_DATE;
static int COLONNE_EXPEDITEUR;
static int COLONNE_TEXTE;
static int COLONNE_TYPE;
int _type;
long _date;
String _text;
String _expediteur;

public Message(Cursor cursor, Context context)
{
	_type = cursor.getInt(COLONNE_TYPE);
	_date = cursor.getLong(COLONNE_DATE);
	_text = cursor.getString(COLONNE_TEXTE);
	_expediteur = getContact(context, cursor.getString(COLONNE_EXPEDITEUR));
}

@Nullable
static Cursor getList(Context context)
{
	Uri u = Uri.parse("content://sms");
	Cursor cursor;
	try
	{
		cursor = context.getContentResolver().query(u, null, null, null, null);
		COLONNE_DATE = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE);
		COLONNE_EXPEDITEUR = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS);
		COLONNE_TEXTE = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY);
		COLONNE_TYPE = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE);
	} catch (Exception e)
	{
		return null;
	}
	return cursor;
}

public String getFileName(Context context)
{
	String res = "[" + sqliteDateHourToString(context, _date) + "] [";

	switch (_type)
	{
		case Telephony.Sms.MESSAGE_TYPE_INBOX:
			res += _expediteur + "] ";
			break;


		case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX:
		case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT:
			res += "moi] ";
			break;
		default:
	}
	if (_text.length() <= LG_TITRE_MAX)
		res += _text;
	else
		res += _text.substring(0, LG_TITRE_MAX - 1);

	return cleanFileName(res) + ".txt";
}

public SauvegardeReturnCode sauvegarde(@NonNull SmbFile smbRoot, @NonNull Context context, NtlmPasswordAuthentication authentification) throws IOException
{
	String path = smbRoot.getCanonicalPath();
	String messagePath = SavedObject.Combine(path, getFileName(context));
	Log.d("SAVE", "Message path:" + path);
	SmbFile messageSmbFile = new SmbFile(messagePath, authentification);
	SmbFileOutputStream sops;
	try
	{
		sops = new SmbFileOutputStream(messageSmbFile);

		sops.write((_text + "\n").getBytes());
		sops.write("*****************************************\n".getBytes());
		switch (_type)
		{
			case Telephony.Sms.MESSAGE_TYPE_INBOX:
				sops.write(("Reçu le " + sqliteDateHourToString(context, _date) + " de " + _expediteur).getBytes());
				break;


			case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX:
			case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT:
				sops.write(("Envoyé le " + sqliteDateHourToString(context, _date) + " à " + _expediteur).getBytes());

				break;
			default:
		}
		sops.close();
		messageSmbFile.setLastModified(_date);
		return SauvegardeReturnCode.OK;
	} catch (IOException e)
	{
		Report report = Report.getInstance(context);
		report.log(Report.NIVEAU.ERROR, "Erreur lors de la sauvegarde du message " + sqliteDateHourToString(context, _date) + " : " + _expediteur);
		report.log(Report.NIVEAU.ERROR, e);
		return SauvegardeReturnCode.ERREUR_CREATION_FICHIER;
	}
}


@Override
public String Nom(@NonNull Context context)
{
	String res = "[" + sqliteDateHourToString(context, _date) + "]";

	switch (_type)
	{
		case Telephony.Sms.MESSAGE_TYPE_INBOX:
			res += " de " + _expediteur;
			break;


		case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX:
		case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT:
			res += " à " + _expediteur;
			break;
		default:
	}

	return res;
}

@NonNull
@Override
public String getCategorie()
{
	return _expediteur;
}
}
