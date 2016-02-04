package lpi.sauvegardesamba.sauvegarde.SavedObject;

import android.content.Context;
import android.database.Cursor;

import lpi.sauvegardesamba.profils.Profil;
import lpi.sauvegardesamba.utils.Preferences;

/**
 * Created by lucien on 04/02/2016.
 */
public class VideosFactory extends SavedObjectFactory
{
@Override
protected boolean objetsActifs(Profil profil)
{
	return profil.Videos;
}

@Override
protected boolean regrouperObjets(Context context)
{
	Preferences pref = new Preferences(context);
	return pref.getRegrouperVideos();
}

@Override
protected String getMessage(SavedObjectFactory.MESSAGES message, Object... arguments)
{
	switch( message )
	{
		case LOG_SAUVEGARDE:
			return "Sauvegarde des vidéos" ;
		case IMPOSSIBLE_CREER_REPERTOIRE :
			return String.format("Impossible de créer le répertoire des vidéos %s", arguments );

		case PROGRESS:
			return "Vidéo %d/%d" ;

		case ERREUR_LORS_DE_LA_SAUVEGARDE:
			return String.format( "Erreur lors de la sauvegarde vidéo dans le répertoire %s", arguments) ;
		case INACTIF:
			return "Sauvegarde des vidéo non active" ;

		default:
			return "?!?" ;
	}
}

@Override
protected String getRepertoireObjets(Context context)
{
	Preferences pref = new Preferences(context);
	return pref.getPrefRepertoireVideos();
}

@Override
protected Cursor getList(Context context)
{
	return Video.getList(context);
}

@Override
protected SavedObject creerObjet(Cursor cursor, Context context)
{
	return new Video(cursor, context );
}
}
