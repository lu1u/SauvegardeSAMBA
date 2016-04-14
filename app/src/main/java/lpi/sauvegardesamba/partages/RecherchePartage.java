package lpi.sauvegardesamba.partages;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.ArrayList;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import lpi.sauvegardesamba.utils.Report;

/**
 * Created by lucien on 26/01/2016.
 */
public class RecherchePartage extends AsyncTask<Void, Void, Void>
{
final Activity _activity;
final String _utilisateur, _motDePasse;
ProgressDialog progress;

public RecherchePartage(Activity a, String Utilisateur, String MotDePasse)
{
	_activity = a;
	_utilisateur = Utilisateur;
	_motDePasse = MotDePasse;
}

/**
 * Override this method to perform a computation on a background thread. The
 * specified parameters are the parameters passed to {@link #execute}
 * by the caller of this task.
 * <p/>
 * This method can call {@link #publishProgress} to publish updates
 * on the UI thread.
 *
 * @param params The parameters of the task.
 * @return A result, defined by the subclass of this task.
 * @see #onPreExecute()
 * @see #onPostExecute
 * @see #publishProgress
 */
@Override
protected Void doInBackground(Void... params)
{
	Report report = Report.getInstance(_activity);
	Intent intent = new Intent(Partages.ACTION_RESULT_RECHERCHE_PARTAGE);
	try
	{
		report.log(Report.NIVEAU.DEBUG, "Recherche des domaines du réseau");
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, _utilisateur, _motDePasse);
		SmbFile domaines = new SmbFile("smb://", auth);
		SmbFile[] domains = domaines.listFiles();
		ArrayList<String> liste = new ArrayList<>();

		for (SmbFile domaine : domains)
		{
			report.log(Report.NIVEAU.DEBUG, "Domaine : " + domaine.getName());
			scanDomain(domaine, liste);
		}

		intent.putStringArrayListExtra(Partages.LISTE_RESULT, liste);
		intent.putExtra(Partages.RESULT_RECHERCHE, Partages.RESULT_OK);
	} catch (Exception e)
	{
		intent.putExtra(Partages.RESULT_RECHERCHE, Partages.RESULT_ERREUR);
		intent.putExtra(Partages.RESULT_MESSAGE, e.getMessage());

		report.log(Report.NIVEAU.ERROR, "Erreur lors de la recherche des domaines réseau");
		report.log(Report.NIVEAU.ERROR, e.getLocalizedMessage());
	} finally
	{
		_activity.sendBroadcast(intent);
	}

	return null;
}

/***
 * Parcourir les ordinateurs du domaine
 *
 * @param domaine
 * @param liste
 */
private void scanDomain(SmbFile domaine, ArrayList<String> liste)
{
	Report report = Report.getInstance(_activity);
	try
	{
		// Recherche des ordinateurs dans chaque domaine
		SmbFile[] ordinateurs = domaine.listFiles();
		for (SmbFile ordinateur : ordinateurs)
		{
			report.log(Report.NIVEAU.DEBUG, "Ordinateur " + ordinateur.getName());

			scanOrdinateur(ordinateur, liste);
		}

	} catch (SmbException e)
	{
		report.log(Report.NIVEAU.ERROR, "Erreur lors du parcours du domaine: " + domaine.getPath());
		report.log(Report.NIVEAU.ERROR, e);
	}
}

private void scanOrdinateur(SmbFile ordinateur, ArrayList<String> liste)
{
	Report report = Report.getInstance(_activity);
	try
	{
		SmbFile[] partages = ordinateur.listFiles();

		for (SmbFile partage : partages)
		{
			report.log(Report.NIVEAU.DEBUG, "Partage " + partage.getName());
			String name = partage.getPath();
			ajouteName(liste, name);
		}
	} catch (SmbException e)
	{
		report.log(Report.NIVEAU.ERROR, "Scan d'ordinateur: " + ordinateur.getPath());
		report.log(Report.NIVEAU.ERROR, e);
	}
}

/***
 * Ajoute un nom de partage a la liste, a condition que ça ne soit pas un partage administratif
 *
 * @param liste
 * @param name
 */
private void ajouteName(ArrayList<String> liste, String name)
{
	while (name.endsWith("/"))
		name = name.substring(0, name.length() - 1);

	if (!name.endsWith("$"))   // Partage administratif
	{
		if (name.startsWith("smb://"))
			// Supprimer le prefixe "smb"
			name = name.substring("smb://".length());

		liste.add(name);
	}
}


protected void onPreExecute()
{
	progress = new ProgressDialog(_activity);
	progress.setMessage("Veuillez patienter pendant la recherche dossiers partagés sur le réseau");
	progress.show();
}

protected void onPostExecute(Void v)
{
	//quand c'est fini, on supprime le ProgressDialog
	if (progress.isShowing())
	{
		progress.dismiss();
	}

}
}
