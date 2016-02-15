package lpi.sauvegardesamba.sauvegarde.SavedObject;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

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
	Preferences pref = Preferences.getInstance(context);
	return pref.getRegrouperPhotos();
}

@Override
@NonNull
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
@NonNull
protected String getRepertoireObjets(@NonNull Context context)
{
	Preferences pref = Preferences.getInstance(context);
	return pref.getPrefRepertoirePhotos();
}

@Override
protected Cursor getList(@NonNull Context context)
{
	return Photo.getList(context);
}

@Override
@NonNull
protected SavedObject creerObjet(@NonNull Cursor cursor, @NonNull Context context)
{
	return new Photo(cursor, context);
}
}