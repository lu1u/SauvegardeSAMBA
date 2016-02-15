package lpi.sauvegardesamba;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import lpi.sauvegardesamba.sauvegarde.AsyncSauvegarde;

public class StartupActivity extends AppCompatActivity
{

@Override
protected void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_startup);

	//Preferences.getInstance(this).setSauvegardeEnCours(false);
	if (AsyncSauvegarde.enCours(this))
	{
		// Aller directement sur l'activity "Sauvegarde en cours"
		Intent intent = new Intent(this, SauvegardeEnCoursActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}
	else
	{
		// Ouvrir l'activity principale
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}
}
}
