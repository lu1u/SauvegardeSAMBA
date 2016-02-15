package lpi.sauvegardesamba;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import lpi.sauvegardesamba.database.DatabaseHelper;
import lpi.sauvegardesamba.database.GenericDatabaseAdapter;
import lpi.sauvegardesamba.database.WithIdCursorWrapper;

public class DisplayDatabaseActivity extends AppCompatActivity
{
private SQLiteDatabase database;
private DatabaseHelper dbHelper;
private String[] _tables;

@Override
protected void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_display_database);
	Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	setSupportActionBar(toolbar);
	getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
	if (fab != null)
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		});

	initSpinner(this);
}

private void initSpinner(final Context context)
{
	final Spinner spin = (Spinner) findViewById(R.id.spinner);
	if (spin == null)
		return;
	dbHelper = new DatabaseHelper(context);
	database = dbHelper.getWritableDatabase();

	_tables = getTables(context, database);
	spin.setAdapter(new MyAdapter(context, _tables));

	spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
		{
			String table = _tables[position];

			Cursor cursor = database.query(table, null, null, null, null, null, null);
			changeColonnes(cursor.getColumnNames());
			WithIdCursorWrapper c = new WithIdCursorWrapper(cursor, WithIdCursorWrapper.ID_FIRST);

			GenericDatabaseAdapter adapter = new GenericDatabaseAdapter(context, c);
			((ListView) findViewById(R.id.listView)).setAdapter(adapter);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent)
		{

		}
	});
}

private void changeColonnes(String[] colonnes)
{
	LinearLayout l = (LinearLayout) findViewById(R.id.layoutColonnes);
	if (l == null)
		return;

	l.removeAllViews();
	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	lp.weight = 1;
	lp.gravity = Gravity.LEFT | Gravity.TOP;

	for (int i = 0; i < colonnes.length; i++)
	{
		TextView v = new TextView(this);
		if (i % 2 == 0)
			v.setTextColor(Color.parseColor("#000000"));
		else
			v.setTextColor(Color.parseColor("#000088"));
		v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		v.setId(i);
		v.setPadding(0, 0, 10, 0);
		v.setText(colonnes[i]);
		l.addView(v);
	}

}

@NonNull
private String[] getTables(Context context, SQLiteDatabase database)
{
	Cursor c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
	String[] tables = new String[c.getCount()];
	int i = 0;
	while (c.moveToNext())
	{
		tables[i] = c.getString(c.getColumnIndex("name"));
		i++;
	}

	return tables;
}

private static class MyAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter
{
	private final ThemedSpinnerAdapter.Helper mDropDownHelper;

	public MyAdapter(Context context, String[] objects)
	{
		super(context, android.R.layout.simple_list_item_1, objects);
		mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent)
	{
		View view;

		if (convertView == null)
		{
			// Inflate the drop down using the helper's LayoutInflater
			LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
			view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
		}
		else
		{
			view = convertView;
		}

		TextView textView = (TextView) view.findViewById(android.R.id.text1);
		textView.setText(getItem(position));

		return view;
	}

	@Override
	public Resources.Theme getDropDownViewTheme()
	{
		return mDropDownHelper.getDropDownViewTheme();
	}

	@Override
	public void setDropDownViewTheme(Resources.Theme theme)
	{
		mDropDownHelper.setDropDownViewTheme(theme);
	}
}
}
