package com.android.detonate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.Toast;

/**:Custom Activity**/
public class CAct extends Activity {
	
	static Activity instance;
	static public PlainButton[] interactButtons;
	static CView threadView;
	
	static {
		Class<CAct> jesus = CAct.class;
	}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        findViewById(R.id.weaponPanelTrigger).setOnClickListener(new OnClickListener() {
			 
			public void onClick(View v) {
				
				findViewById(R.id.weaponPanel).
					setVisibility(
						findViewById(R.id.weaponPanel).getVisibility() == View.GONE? View.VISIBLE : View.GONE);
				
			}
		});
        
        instance = this;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// TODO Auto-generated method stub
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
    	return true;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.new_game:
            	
                World.shallBeginNewRound = true;
               
                return true;
                
            case R.id.game_type:
            	
            	final CharSequence[] items = new CharSequence[Level.values().length];
            	for(int i=0; i<items.length; i++){
            		items[i] = Level.values()[i].name();
            	}
            	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            	builder.setTitle("Select level type:");
            	builder.setItems(items, new DialogInterface.OnClickListener() {
            		
            	    public void onClick(DialogInterface dialog, int item) {
            	    	Cnt.levelType = Level.values()[item];
            	    	World.newGame();
            	    }
            	});
            	AlertDialog alert = builder.create();
            	alert.show();
            	
            	return true;
            	 
            default:
                return super.onOptionsItemSelected(item);
                
            
        }
    }
    
}