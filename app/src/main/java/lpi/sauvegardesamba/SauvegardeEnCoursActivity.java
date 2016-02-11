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

public class SauvegardeEnCoursActivity extends AppCompatActivity
{
public static final String PARAM_ID = "Id";
TextView _textViewProgress;
TextView _textviewProfil;
TextView _textViewPartage ;
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
boolean _annule = false;
AsyncSauvegardeManager _manager;

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
	//Utils.setTheme(this);
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_sauvegarde_en_cours);
	_progressbarObjets = (ProgressBar) findViewById(R.id.progressBarObjets);
	_textViewProgress = (TextView) findViewById(R.id.textviewMessage);
	_textviewProfil = (TextView) findViewById(R.id.textViewProfil);
	_textViewPartage = (TextView)findViewById(R.id.textViewPartage);

	_annule = false;
	findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			_manager.cancel();
			MainActivity.MessageNotification(findViewById(R.id.coordinatorlayout), "Annulation de la sauvegarde");
		}
	});

	IntentFilter filter = new IntentFilter();
	filter.addAction(AsyncSauvegarde.ACTION_ASYNCSAVE);
	registerReceiver(_receiver, filter);

	_manager = AsyncSauvegardeManager.getInstance(this);
	Intent i = getIntent();
	{
		int profilId = -1;
		if (i != null)
			profilId = i.getIntExtra(PARAM_ID, -1);

		_manager.startSauvegarde(profilId, AsyncSauvegardeManager.TYPE_LAUNCHED.MANUEL);
	}
}


public void setProgress(@NonNull String format, int step, int Max)
{
	_progressbarObjets.setMax(Max);
	_progressbarObjets.setProgress(step);
	_textViewProgress.setText(String.format(format, step, Max));
}


}
