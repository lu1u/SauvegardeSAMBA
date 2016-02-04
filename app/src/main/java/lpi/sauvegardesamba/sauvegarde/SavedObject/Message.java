package lpi.sauvegardesamba.sauvegarde.SavedObject;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import lpi.sauvegardesamba.sauvegarde.SauvegardeReturnCode;
import lpi.sauvegardesamba.utils.Report;

/**
 * Created by lucien on 29/01/2016.
 */
public class Message extends SavedObject
{
    static int COLONNE_DATE;
    static int COLONNE_EXPEDITEUR;
    static int COLONNE_TEXTE;
    static int COLONNE_TYPE;
    static final int LG_TITRE = 50 ;

    int _type ;
    long _date ;
    String _text;
    String _expediteur ;

    public Message(Cursor cursor, Context context)
    {
        _type = cursor.getInt(COLONNE_TYPE);
        _date = cursor.getLong(COLONNE_DATE) ;
        _text = cursor.getString(COLONNE_TEXTE);
        _expediteur = getContact(context, cursor.getString(COLONNE_EXPEDITEUR));
    }

    public static Cursor getList(Context context)
    {
        Uri u = Uri.parse("content://sms");
        Cursor cursor = context.getContentResolver().query(u, null, null, null, null);
        COLONNE_DATE = cursor.getColumnIndex(Telephony.Sms.DATE);
        COLONNE_EXPEDITEUR = cursor.getColumnIndex(Telephony.Sms.ADDRESS);
        COLONNE_TEXTE = cursor.getColumnIndex(Telephony.Sms.BODY);
        COLONNE_TYPE = cursor.getColumnIndex(Telephony.Sms.TYPE);
        return cursor;
    }

    public String getFileName(Context context)
    {
        String res = "[" + sqliteDateHourToString( context, _date ) + "] [" ;

        switch( _type)
        {
            case Telephony.Sms.MESSAGE_TYPE_INBOX :
                res += _expediteur + "] " ;
                break ;


            case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX:
            case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT:
                res += "moi] ";
                break ;
            default:
        }
        if ( _text.length()<=LG_TITRE)
            res += _text;
        else
            res += _text.substring(0, LG_TITRE-1) ;

        return cleanFileName(res) + ".txt" ;
    }

public SauvegardeReturnCode sauvegarde(SmbFile smbRoot, Report report, Context context, NtlmPasswordAuthentication authentification) throws IOException
    {
	    String path = smbRoot.getCanonicalPath();
	    String messagePath = SavedObject.Combine(path, getFileName(context));
	    Log.d("SAVE", "Message path:" + path);
	    SmbFile messageSmbFile = new SmbFile(messagePath, authentification);
        SmbFileOutputStream sops =null;
        try
        {
            sops = new SmbFileOutputStream(messageSmbFile);

            sops.write((_text + "\n").getBytes());
            sops.write("*****************************************\n".getBytes());
            switch( _type)
            {
                case Telephony.Sms.MESSAGE_TYPE_INBOX :
                    sops.write(("Reçu le " + sqliteDateHourToString(context, _date) + " de " + _expediteur).getBytes());
                    break ;


                case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX:
                case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT:
                    sops.write(("Envoyé le " + sqliteDateHourToString(context,_date) + " à " + _expediteur).getBytes());

                    break ;
                default:
            }
	        sops.close();
	        messageSmbFile.setLastModified(_date);
	        return SauvegardeReturnCode.OK;
        } catch (IOException e)
        {
            report.Log("Erreur lors de la sauvegarde du message " + sqliteDateHourToString(context, _date) + " : " + _expediteur);
            report.Log(e);
	        return SauvegardeReturnCode.ERREUR_CREATION_FICHIER ;
        }
    }


    @Override
    public String Nom(Context context)
    {
        String res = "[" + sqliteDateHourToString( context, _date ) + "]" ;

        switch( _type)
        {
            case Telephony.Sms.MESSAGE_TYPE_INBOX :
                res += " de " + _expediteur  ;
                break ;


            case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX:
            case Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT:
                res += " à " + _expediteur ;
                break ;
            default:
        }

        return res ;
    }

@NonNull
@Override
public String getCategorie()
{
    return _expediteur;
}
}
