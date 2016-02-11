package lpi.sauvegardesamba.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import lpi.sauvegardesamba.R;

/**
 * Fonctions utilitaires diverses
 */
public class Utils
{
public static void addHint(final Activity a, int id, final String message)
{
	View v = a.findViewById(id);
	if (v == null)
		return;

	v.setOnFocusChangeListener(new View.OnFocusChangeListener()
	{
		@Override
		public void onFocusChange(View v, boolean hasFocus)
		{
			if (hasFocus)
				//Toast.makeText(v.getContext(), ic_message, Toast.LENGTH_SHORT).set.show();
				displayToastAboveButton(a, v, message);
		}
	});
}

private static void displayToastAboveButton(Activity a, View v, String message)
{
	int xOffset = 0;
	int yOffset = 0;
	Rect gvr = new Rect();

	View parent = (View) v.getParent();
	int parentHeight = parent.getHeight();

	if (v.getGlobalVisibleRect(gvr))
	{
		View root = v.getRootView();

		int halfWidth = root.getRight() / 2;
		int halfHeight = root.getBottom() / 2;

		int parentCenterX = ((gvr.right - gvr.left) / 2) + gvr.left;

		int parentCenterY = ((gvr.bottom - gvr.top) / 2) + gvr.top;

		if (parentCenterY <= halfHeight)
		{
			yOffset = -(halfHeight - parentCenterY) - parentHeight;
		}
		else
		{
			yOffset = (parentCenterY - halfHeight) - parentHeight;
		}

		if (parentCenterX < halfWidth)
		{
			xOffset = -(halfWidth - parentCenterX);
		}

		if (parentCenterX >= halfWidth)
		{
			xOffset = parentCenterX - halfWidth;
		}
	}

	LayoutInflater inflater = a.getLayoutInflater();
	View layout = inflater.inflate(R.layout.hint_toast_layout, (ViewGroup) a.findViewById(R.id.layoutRoot));
	TextView text = (TextView) layout.findViewById(R.id.text);
	text.setText(message);

	Toast toast = Toast.makeText(a, message, Toast.LENGTH_SHORT);
	toast.setGravity(Gravity.CENTER, xOffset, yOffset);
	toast.setView(layout);
	toast.show();
}

public static void setTheme(Activity a)
{
	Preferences p = Preferences.getInstance(a);
	switch (p.getTheme())
	{

		case 1:
			a.setTheme(R.style.Theme2);
			break;

		case 2:
			a.setTheme(R.style.Theme3);
			break;

		case 3:
			a.setTheme(R.style.Theme4);
			break;

		case 4:
			a.setTheme(R.style.Theme5);
			break;

		case 5:
			a.setTheme(R.style.Theme6);
			break;
		case 0:
		default:
			a.setTheme(R.style.Theme1);
			break;

	}
}

public static Bitmap getBitmap(Context context, int resId)
{
	/*
}
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		return context.getResources().getDrawable(resId, context.getApplicationContext().getTheme());
else
		return context.getResources().getDrawable(resId);      */
	return BitmapFactory.decodeResource(context.getResources(), resId);
}
}
