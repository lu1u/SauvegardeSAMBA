package lpi.sauvegardesamba;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import lpi.sauvegardesamba.sauvegarde.AsyncSauvegarde;
import lpi.sauvegardesamba.sauvegarde.Plannificateur;
import lpi.sauvegardesamba.utils.Preferences;
import lpi.sauvegardesamba.utils.Utils;

public class PlannificationActivity extends AppCompatActivity implements TimePicker.OnTimeChangedListener
{
public static final String ACTION_PLANNIFICATION_FINISHED = "lpi.plannification_finished";
private static final String SAUVEGARDE_AUTO = "lpi.SauveAuto";
private static final String HOUR = "lpi.Heure";
private static final String MINUTE = "lpi.Minute";
private static final java.lang.String DETECTE_WIFI = "lpi.DetecteWIFI";

private CheckBox _sauvegardeAutoActivee;
private CheckBox _detectionWIFI;
private TextView _textProchaineSauvegarde;
private Preferences _pref;
private int _Heure, _Minute;
@Override
protected void onCreate(Bundle savedInstanceState)
{
	Utils.setTheme(this);
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_plannification);
	Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	setSupportActionBar(toolbar);
	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	getSupportActionBar().setDisplayShowTitleEnabled(true);

	_pref = Preferences.getInstance(this);
	// Inflate the layout for this fragment
	TimePicker p = (TimePicker) findViewById(R.id.timePicker);
	p.setIs24HourView(DateFormat.is24HourFormat(this));

	_textProchaineSauvegarde = (TextView) findViewById(R.id.textViewProchaineSauvegarde);
	_sauvegardeAutoActivee = (CheckBox) findViewById(R.id.checkBoxSauvegardePlannifiee);
	_detectionWIFI = (CheckBox) findViewById(R.id.checkBoxConnexionWifi);
	boolean bSauvegardeAutoActivee;
	boolean bDetecteWIFI;

	if (savedInstanceState != null)
	{
		bSauvegardeAutoActivee = savedInstanceState.getBoolean(SAUVEGARDE_AUTO);
		bDetecteWIFI = savedInstanceState.getBoolean(DETECTE_WIFI);
		_Heure = savedInstanceState.getInt(HOUR);
		_Minute = savedInstanceState.getInt(MINUTE);
	}
	else
	{
		bSauvegardeAutoActivee = _pref.getSauvegarderAuto();
		bDetecteWIFI = _pref.getDetectionWIFI();
		_Heure = _pref.getSauvegardeAutoHeure();
		_Minute = _pref.getSauvegardeAutoMinute();
	}

	_sauvegardeAutoActivee.setChecked(bSauvegardeAutoActivee);
	_detectionWIFI.setChecked(bDetecteWIFI);
	_sauvegardeAutoActivee.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			animeBouton(R.id.timePicker, isChecked);
			UpdateText();
		}
	});

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
	{
		p.setHour(_Heure);
		p.setMinute(_Minute);
	}
	else
	{
		p.setCurrentHour(_Heure);
		p.setCurrentMinute(_Minute);
	}

// !!! Initialiser les valeurs de temps AVANT de configurer le listener!
	p.setOnTimeChangedListener(this);
	p.setVisibility(_pref.getSauvegarderAuto() ? View.VISIBLE : View.GONE);

	UpdateText();
}


@Override
public void onSaveInstanceState(Bundle savedInstanceState)
{
	savedInstanceState.putBoolean(SAUVEGARDE_AUTO, ((CheckBox) findViewById(R.id.checkBoxSauvegardePlannifiee)).isChecked());
	TimePicker p = (TimePicker) findViewById(R.id.timePicker);
	int Heure, Minute;
	if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
	{
		Heure = p.getHour();
		Minute = p.getMinute();
	}
	else
	{
		Heure = p.getCurrentHour();
		Minute = p.getCurrentMinute();
	}

	savedInstanceState.putInt(HOUR, Heure);
	savedInstanceState.putInt(MINUTE, Minute);
	super.onSaveInstanceState(savedInstanceState);
}

/**
 * Called when the fragment is visible to the user and actively running.
 * This is generally
 * tied to {@link Activity#onResume() Activity.onResume} of the containing
 * Activity's lifecycle.
 */

private void UpdateText()
{
	if (_pref.getSauvegarderAuto())
	{
		_textProchaineSauvegarde.setText(
				String.format(getString(R.string.sauvegarde_auto_programmee), AsyncSauvegarde.getLocalizedTimeAndDate(this, Calendar.getInstance())));

	}
	else
	{
		_textProchaineSauvegarde.setText(R.string.sauvegarde_automatique_desactivee);
	}
}

private void animeBouton(int buttonId, boolean isChecked)
{
	View v = findViewById(buttonId);

	if (v == null)
		return;

	Animation anim = AnimationUtils.loadAnimation(this, isChecked ? R.anim.pop_enter : R.anim.pop_exit);
	v.setAnimation(anim);
	v.setVisibility(isChecked ? View.VISIBLE : View.GONE);
}


/**
 * @param view      The view associated with this listener.
 * @param hourOfDay The current hour.
 * @param minute    The current minute.
 */
public void onTimeChanged(TimePicker view, int hourOfDay, int minute)
{
	_Heure = hourOfDay;
	_Minute = minute;
	UpdateText();
}

public boolean onCreateOptionsMenu(Menu menu)
{
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.menu_dialog_box, menu);
	return true;
}

@Override
public boolean onOptionsItemSelected(MenuItem item)
{
	switch (item.getItemId())
	{
		case R.id.buttonOK:
			onOK();
			return true;

		case R.id.buttonCancel:
			onAnnuler();
			return true;

		case android.R.id.home:
			onAnnuler();
			return true;
		default:
			// If we got here, the user's action was not recognized.
			// Invoke the superclass to handle it.
			return super.onOptionsItemSelected(item);

	}
}

/**
 * OK: fermer l'ecran et renvoyer les donnees
 */
public void onOK()
{
	_pref.setSauvegardeAuto(_sauvegardeAutoActivee.isChecked());
	_pref.setDetectionWIFI(_detectionWIFI.isChecked());
	_pref.setPrefSauvegardeAutoHeure(_Heure);
	_pref.setPrefSauvegardeAutoMinute(_Minute);
	Plannificateur p = new Plannificateur(this);
	p.plannifieSauvegarde();

	Intent returnIntent = new Intent();
	returnIntent.setAction(ACTION_PLANNIFICATION_FINISHED);
	setResult(Activity.RESULT_OK, returnIntent);
	sendBroadcast(returnIntent);
	finish();
}

public void onAnnuler()
{
	Intent returnIntent = new Intent();
	setResult(Activity.RESULT_CANCELED, returnIntent);
	finish();
}

}
