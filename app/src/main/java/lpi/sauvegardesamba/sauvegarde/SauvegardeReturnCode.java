package lpi.sauvegardesamba.sauvegarde;

/**
 * Codes de retour de la sauvegarde
 * Created by lucien on 03/02/2016.
 */
public enum SauvegardeReturnCode
{
OK,
EXISTE_DEJA,                        // L'objet sauvegardé existe déjà
IMPOSSIBLE_SUPPRIMER_TEMP,           // Le fichier temporaire existe déjà et est impossible à supprimer
	ERREUR_COPIE, INACTIF, IMPOSSIBLE_CREER_REPERTOIRE, ERREUR_CREATION_FICHIER, ERREUR_LISTE_PROFILS
}
