package lpi.sauvegardesamba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import lpi.sauvegardesamba.profils.ProfilsDatabase;
import lpi.sauvegardesamba.sauvegarde.ProgressDlg;
import lpi.sauvegardesamba.sauvegarde.Sauvegarde;
import lpi.sauvegardesamba.sauvegarde.SauvegardeManuelle;

public class SauvegardeEnCoursActivity extends AppCompatActivity    implements ProgressDlg
{
TextView _textViewProgress;
TextView _textviewProfil;
TextView _textViewPartage ;
ProgressBar _progressbarObjets;

boolean _annule;


public static final String PARAM_ID = "Id";
private BroadcastReceiver receiver = new BroadcastReceiver()
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();

		if ( Sauvegarde.ACTION_INFOS_FROM_SAUVEGARDE.equals(action))
			onSauvegardeInfo(intent);
	}
};

private void onSauvegardeInfo(Intent intent)
{
	if ( Sauvegarde.EXTRA_SAUVEGARDE_EN_COURS.equals(intent.getStringExtra(Sauvegarde.EXTRA_TYPE)))
	{
		boolean enCours = intent.getBooleanExtra(Sauvegarde.EXTRA_SAUVEGARDE_EN_COURS, true) ;
		if ( ! enCours )
		{
			setResult(RESULT_OK);
			finish();
		}
	}
}

@Override
protected void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_sauvegarde_en_cours);
	ProfilsDatabase database = ProfilsDatabase.getInstance(this);
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
			SauvegardeEnCoursActivity.this._annule = true;
			MainActivity.MessageNotification(findViewById(R.id.coordinatorlayout), "Annulation de la sauvegarde");
		}
	});

	IntentFilter filter = new IntentFilter();
	filter.addAction(Sauvegarde.ACTION_INFOS_FROM_SAUVEGARDE);
	registerReceiver(receiver, filter);

	int profilId = -1 ;
	Intent i = getIntent();
	if( i != null)
		profilId = i.getIntExtra(PARAM_ID, -1) ;

	SauvegardeManuelle s = new SauvegardeManuelle(this, this, profilId);
	s.execute();
}


public void setProgress(String format, int step, int Max)
{
	_progressbarObjets.setMax(Max);
	_progressbarObjets.setProgress(step);
	_textViewProgress.setText(String.format(format, step, Max));
}

public void setProfil(String profil)
{
	_textviewProfil.setText(profil);
}

@Override
public void setPartage(String partage)
{
_textViewPartage.setText(partage);
}

public void notification(String message)
{
	MainActivity.MessageNotification(findViewById(R.id.coordinatorlayout), message);
}

public boolean isCanceled()
{
	return _annule;
}
}
