package com.example.hellodroid;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends ActionBarActivity {
	private TextView message;     
	private ImageView droid;
	private View.OnClickListener droidTapListener;
	private int counter = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initialiazeApp();
	}
	
	private void initialiazeApp(){
		message = (TextView) findViewById(R.id.textView1);
		droid = (ImageView) findViewById(R.id.imageView1);
		
		droidTapListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				TapDroid();
			}
		};
		
		droid.setOnClickListener(droidTapListener);
	}
	
	private void TapDroid(){
		counter++;
		String temp;
		switch(counter){
			
		case 1:
			temp = "once";
			break;
		case 2:
			temp = "twice";
			break;
		default:
			temp = String.format("%d times", counter);
			
		}
		
		message.setText(String.format("You touched the droid %s", temp));
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


}
