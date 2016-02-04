package lpi.sauvegardesamba.sauvegarde.SavedObject;

import android.content.Context;
import android.database.Cursor;

import lpi.sauvegardesamba.profils.Profil;
import lpi.sauvegardesamba.utils.Preferences;

/**
 * Created by lucien on 04/02/2016.
 */
public class PhotosFactory extends SavedObjectFactory
{
@Override
protected boolean objetsActifs(Profil profil)
{
	return profil.Photos;
}

@Override
protected boolean regrouperObjets(Context context)
{
	Preferences pref = new Preferences(context);
	return pref.getRegrouperPhotos();
}

@Override
protected String getMessage(SavedObjectFactory.MESSAGES message, Object... arguments)
{
	switch (message)
	{
		case LOG_SAUVEGARDE:
			return "Sauvegarde des photos";
		case IMPOSSIBLE_CREER_REPERTOIRE:
			return String.format("Impossible de créer le répertoire des photos %s", arguments);

		case PROGRESS:
			return "Photos %d/%d";

		case ERREUR_LORS_DE_LA_SAUVEGARDE:
			return String.format("Erreur lors de la sauvegarde photo dans le répertoire %s", arguments);
		case INACTIF:
			return "Sauvegarde des photos non active";

		default:
			return "?!?";
	}
}

@Override
protected String getRepertoireObjets(Context context)
{
	Preferences pref = new Preferences(context);
	return pref.getPrefRepertoirePhotos();
}

@Override
protected Cursor getList(Context context)
{
	return Photo.getList(context);
}

@Override
protected SavedObject creerObjet(Cursor cursor, Context context)
{
	return new Photo(cursor, context);
}
}