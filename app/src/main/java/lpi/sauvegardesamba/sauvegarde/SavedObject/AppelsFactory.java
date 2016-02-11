package lpi.sauvegardesamba.sauvegarde.SavedObject;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import lpi.sauvegardesamba.profils.Profil;
import lpi.sauvegardesamba.utils.Preferences;

/**
 * Created by lucien on 04/02/2016.
 */
public class AppelsFactory extends SavedObjectFactory
{
@Override
protected boolean objetsActifs(Profil profil)
{
	return profil.Appels;
}

@Override
protected boolean regrouperObjets(Context context)
{
	Preferences pref = Preferences.getInstance(context);
	return pref.getRegrouperAppels();
}

@Override
@NonNull
protected String getMessage(MESSAGES message, Object... arguments)
{
	switch( message )
	{
		case LOG_SAUVEGARDE:
			return "Sauvegarde des appels" ;
		case IMPOSSIBLE_CREER_REPERTOIRE :
			return String.format("Impossible de créer le répertoire des appels %s", arguments );

		case PROGRESS:
			return "Appel %d/%d" ;

		case ERREUR_LORS_DE_LA_SAUVEGARDE:
			return String.format( "Erreur lors de la sauvegarde appel dans le répertoire %s", arguments) ;
		case INACTIF:
			return "Sauvegarde des appels non active" ;

		default:
			return "?!?" ;
	}
}

@Override
protected String getRepertoireObjets(@NonNull Context context)
{
	Preferences pref = Preferences.getInstance(context);
	return pref.getPrefRepertoireAppels();
}

@Override
@Nullable
protected Cursor getList(Context context)
{
	return Appel.getList(context);
}

@Override
protected SavedObject creerObjet(@NonNull Cursor cursor, Context context)
{
	return new Appel(cursor, context );
}
}
