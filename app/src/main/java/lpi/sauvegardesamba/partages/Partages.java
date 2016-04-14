package lpi.sauvegardesamba.partages;

import android.app.Activity;

/**
 * Created by lucien on 26/01/2016.
 */
public class Partages
{
     public static final String ACTION_RESULT_RECHERCHE_PARTAGE = "lpi.sauvegardesamba.recherchepartage" ;
    public static final String RESULT_RECHERCHE = "result" ;
    public static final String RESULT_MESSAGE = "message" ;
    public static final int RESULT_OK = 0 ;
    public static final int RESULT_ERREUR= 1;

    public static final String LISTE_RESULT = "liste" ;


public static void LanceRecherchePartage(Activity a, String utilisateur, String motDePasse)
    {
        RecherchePartage r = new RecherchePartage(a, utilisateur, motDePasse);
        r.execute() ;
    }
}
