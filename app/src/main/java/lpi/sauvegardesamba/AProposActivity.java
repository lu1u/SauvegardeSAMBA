package lpi.sauvegardesamba;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import lpi.sauvegardesamba.utils.Utils;

public class AProposActivity extends AppCompatActivity
{

@Override
protected void onCreate(Bundle savedInstanceState)
{
	Utils.setTheme(this);
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_apropos);
	Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	setSupportActionBar(toolbar);

	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
}

}
