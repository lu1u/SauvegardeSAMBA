package lpi.sauvegardesamba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import lpi.sauvegardesamba.profils.Profil;
import lpi.sauvegardesamba.sauvegarde.AsyncSauvegarde;
import lpi.sauvegardesamba.sauvegarde.AsyncSauvegardeManager;
import lpi.sauvegardesamba.utils.Preferences;
import lpi.sauvegardesamba.utils.Report;
import lpi.sauvegardesamba.utils.Utils;

public class SauvegardeEnCoursActivity extends AppCompatActivity implements Utils.ConfirmListener
{
public static final String PARAM_ID = "Id";
TextView _textViewProgress;
TextView _textviewProfil;
TextView _textViewPartage;
ProgressBar _progressbarObjets;
final private BroadcastReceiver _receiver = new BroadcastReceiver()
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();

		if (AsyncSauvegarde.ACTION_ASYNCSAVE.equals(action))
			onSauvegardeInfo(intent);
	}
};
int _nbTentativesAnnule;
boolean _annule = false;


private void onSauvegardeInfo(Intent intent)
{
	String commande = intent.getStringExtra(AsyncSauvegarde.COMMAND);

	if (AsyncSauvegarde.COMMAND_STARTED.equals(commande))
	{
		findViewById(R.id.progressBarEnCours).setVisibility(View.VISIBLE);
	}
	else if (AsyncSauvegarde.COMMAND_FINISHED.equals(commande))
	{
		setResult(RESULT_OK);
		finish();
	}
	else if (AsyncSauvegarde.COMMAND_PROFIL.equals(commande))
	{
		Profil profil = new Profil(intent.getExtras());
		int current = intent.getIntExtra(AsyncSauvegarde.PARAM_CURRENT, 1);
		int max = intent.getIntExtra(AsyncSauvegarde.PARAM_MAX, 1);
		_textviewProfil.setText(String.format("%s %d/%d", profil.Nom, current, max));
		_textViewPartage.setText(profil.Partage);
	}
	else if (AsyncSauvegarde.COMMAND_PROGRESS.equals(commande))
	{
		String format = intent.getStringExtra(AsyncSauvegarde.PARAM_FORMAT);
		int current = intent.getIntExtra(AsyncSauvegarde.PARAM_CURRENT, 1);
		int max = intent.getIntExtra(AsyncSauvegarde.PARAM_MAX, 1);
		_textViewProgress.setText(String.format(format, current, max));
		_progressbarObjets.setMax(max);
		_progressbarObjets.setProgress(current);
	}


}

@Override
protected void onDestroy()
{
	super.onDestroy();
	unregisterReceiver(_receiver);
}

@Override
protected void onCreate(Bundle savedInstanceState)
{
//	Utils.setTheme(this);
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_sauvegarde_en_cours);
	_progressbarObjets = (ProgressBar) findViewById(R.id.progressBarObjets);
	_textViewProgress = (TextView) findViewById(R.id.textviewMessage);
	_textviewProfil = (TextView) findViewById(R.id.textViewProfil);
	_textViewPartage = (TextView) findViewById(R.id.textViewPartage);

	_annule = false;
	_nbTentativesAnnule++;
	final AsyncSauvegardeManager manager = AsyncSauvegardeManager.getInstance(this);

	findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if (_nbTentativesAnnule > 5)
			{
				Utils.confirmDialog(SauvegardeEnCoursActivity.this, "Forcer l'annulation de la sauvegarde", "La sauvegarde semble bloquée, voulez-vous forcer l'annulation?", 0,
						SauvegardeEnCoursActivity.this);
			}
			else
			{
				_nbTentativesAnnule++;
				Report.getInstance(SauvegardeEnCoursActivity.this).historique("Sauvegarde annulée par application");
				manager.cancel();
				MainActivity.MessageNotification(findViewById(R.id.coordinatorlayout), "Annulation de la sauvegarde");
			}
		}
	});

	IntentFilter filter = new IntentFilter();
	filter.addAction(AsyncSauvegarde.ACTION_ASYNCSAVE);
	registerReceiver(_receiver, filter);

	// Déterminer si une sauvegarde est deja en cours ou s'il faut en lancer une
	if (Preferences.getInstance(this).getSauvegardeEnCours())
	{
		// Une sauvegarde est deja en cours, on se sert de cette activity pour suivre son
		// deroulement et éventuellement l'annuler
	}
	else
	{
		// Pas de sauvegarde en cours, c'est donc qu'il faut en lancer une
		Intent i = getIntent();
		{
			int profilId = -1;
			if (i != null)
				profilId = i.getIntExtra(PARAM_ID, -1);
			Report.getInstance(this).historique("Sauvegarde lancée par l'application");
			manager.startSauvegarde(profilId, AsyncSauvegardeManager.TYPE_LAUNCHED.MANUEL);
		}
	}
}


public void setProgress(@NonNull String format, int step, int Max)
{
	_progressbarObjets.setMax(Max);
	_progressbarObjets.setProgress(step);
	_textViewProgress.setText(String.format(format, step, Max));
}


@Override
public void onConfirmOK(int requestCode)
{
	if (requestCode == 0)
	{
		// Forcer l'annulation de la sauvegarde
		Report.getInstance(this).historique("Annulation forcée de la sauvegarde depuis l'annulation");
		AsyncSauvegardeManager.getInstance(this).forceCancel(this);
	}
}

@Override
public void onConfirmCancel(int requestCode)
{
	// Rien a faire
}
}
