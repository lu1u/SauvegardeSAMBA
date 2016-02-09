package lpi.sauvegardesamba.sauvegarde.SavedObject;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import lpi.sauvegardesamba.profils.Profil;
import lpi.sauvegardesamba.sauvegarde.AsyncSauvegarde;
import lpi.sauvegardesamba.sauvegarde.AsyncSauvegardeManager;
import lpi.sauvegardesamba.sauvegarde.SauvegardeReturnCode;
import lpi.sauvegardesamba.utils.Report;

/**
 * Created by lucien on 04/02/2016.
 */
public abstract class SavedObjectFactory
{
protected abstract boolean objetsActifs(Profil profil);

protected abstract boolean regrouperObjets(Context context);

@NonNull
protected abstract String getMessage(MESSAGES message, Object... arguments);

@NonNull
protected abstract String getRepertoireObjets(Context context);

@Nullable
protected abstract Cursor getList(Context context);

@NonNull
protected abstract SavedObject creerObjet(Cursor cursor, Context context);

public SauvegardeReturnCode sauvegarde(Context context, Profil profil, String rootPath, NtlmPasswordAuthentication authentification, AsyncSauvegardeManager dlg)
{
	if (!objetsActifs(profil))
	{
		Report.Log(Report.NIVEAU.DEBUG, getMessage(MESSAGES.INACTIF));
		return SauvegardeReturnCode.INACTIF;
	}

	Report.Log(Report.NIVEAU.DEBUG, getMessage(MESSAGES.LOG_SAUVEGARDE));
	rootPath = SavedObject.Combine(rootPath, getRepertoireObjets(context));
	try
	{
		SmbFile smbRoot = new SmbFile(rootPath, authentification);
		if (!smbRoot.exists())
		{
			try
			{
				smbRoot.mkdir();
			} catch (SmbException e)
			{
				Report.Log(Report.NIVEAU.ERROR, getMessage(MESSAGES.IMPOSSIBLE_CREER_REPERTOIRE, rootPath));
				return SauvegardeReturnCode.IMPOSSIBLE_CREER_REPERTOIRE;
			}
		}

		Cursor cursor = getList(context);
		int max = cursor.getCount();
		int current = 0;
		while (cursor.moveToNext() && !dlg.isCanceled())
		{
			current++;
			AsyncSauvegarde.signaleProgress(context, getMessage(MESSAGES.PROGRESS), current, max);

			SavedObject objet = creerObjet(cursor, context);
			if (objet.quelqueChoseASauvegarder())
			{
				SmbFile objectRoot = getPath(rootPath, objet, context, authentification);
				if (objectRoot != null)
					switch (objet.sauvegarde(objectRoot, context, authentification))
					{
						case ERREUR_COPIE:
						case EXISTE_DEJA:
						case OK:
						case IMPOSSIBLE_CREER_REPERTOIRE:
						case IMPOSSIBLE_SUPPRIMER_TEMP:
						case INACTIF:
							// TODO: traiter les retours d'erreur
							break;
					}
			}
		}
		cursor.close();

	} catch (Exception e)
	{
		Report.Log(Report.NIVEAU.ERROR, getMessage(MESSAGES.ERREUR_LORS_DE_LA_SAUVEGARDE, rootPath));
		Report.Log(Report.NIVEAU.DEBUG, e);
	}

	Report.Log(Report.NIVEAU.DEBUG, "Tous les objets traités");
	return SauvegardeReturnCode.OK;
}

@Nullable
private SmbFile getPath(String rootPath, SavedObject objet, Context context, NtlmPasswordAuthentication authentification)
{
	// Sous repertoire pour cet objet ?
	SmbFile objectRoot = null;
	try
	{
		if (regrouperObjets(context))
		{
			String categorie = objet.getCategorie();
			rootPath = SavedObject.Combine(rootPath, categorie);
			objectRoot = new SmbFile(rootPath, authentification);
			if (!objectRoot.exists())
			{
				try
				{
					objectRoot.mkdir();
				} catch (SmbException e)
				{
					Report.Log(Report.NIVEAU.ERROR, getMessage(MESSAGES.IMPOSSIBLE_CREER_REPERTOIRE, rootPath));
					return null;
				}
			}
		}
		else
			objectRoot = new SmbFile(rootPath);

	} catch (Exception e)
	{
		Report.Log(Report.NIVEAU.ERROR, "Erreur lors de la creation du repertoire " + rootPath);
		Report.Log(Report.NIVEAU.DEBUG, e);
	}

	return objectRoot;
}


protected enum MESSAGES
{
	LOG_SAUVEGARDE, IMPOSSIBLE_CREER_REPERTOIRE, PROGRESS, ERREUR_LORS_DE_LA_SAUVEGARDE, INACTIF
}

}
