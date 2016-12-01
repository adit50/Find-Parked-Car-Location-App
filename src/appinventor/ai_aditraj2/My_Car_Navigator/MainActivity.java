package appinventor.ai_aditraj2.My_Car_Navigator;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.apptracker.android.track.AppTracker;
@SuppressLint("SetJavascriptEnabled")
public class MainActivity extends Activity {
   WebView webview;
   WebView webview2;
   WebView ads;
   WebView ads2;
   String answer;
   double CLat;
   double CLng;
   double MapLat;
   double MapLng;
   boolean gps_enabled = false;
   boolean network_enabled = false;
   boolean resume_gpsEnabled = false;
   boolean loaded = false; 
   Uri outputFileUri;
   public static final String PREFS_NAME = "MyApp_Settings";
   public static String TAG = "Main Activity";
   int TAKE_PHOTO_CODE=0;
   int count=0;
   double z;
   float azimuth;
   private SensorManager sensorManager;
   Sensor mAccelerometer;
   Sensor mMagnetometer;
   boolean mLastAccelerometerSet = false;
   boolean mLastMagnetometerSet = false;
   boolean openRateUs = true;
float bearing;
ImageView arrow;
double distance;
int sArrowWidth,sArrowHeight,sArrowRight,sArrowTop;
int ArrowWidth,ArrowHeight,ArrowRight,ArrowTop;
int distanceTop;
RelativeLayout RateUsCover;
ImageView star1;
ImageView star2;
ImageView star3;
ImageView star4;
ImageView star5;
   @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if(null != activeNetwork) {
			answer="Yes";
			new RequestTask().execute("opened");
		}else{
			answer="No";
		}
        
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        LocationListener locListener = new myLocationListener();
		try{
			gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
			network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			
		}catch(Exception ex) {}
		if(!gps_enabled) {
			boolean hasNetwork = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK);
			boolean hasGps = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
			if(network_enabled && hasNetwork){
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locListener);
			}
			if(hasGps){
				RelativeLayout GpsWarning = (RelativeLayout)findViewById(R.id.GpsWarning);	
			    GpsWarning.setVisibility(View.VISIBLE);
			    RelativeLayout loading = (RelativeLayout)findViewById(R.id.loading);	
			    loading.setVisibility(View.GONE);
			}else{
				RelativeLayout GpsWarning = (RelativeLayout)findViewById(R.id.GpsWarning);	
			    GpsWarning.setVisibility(View.GONE);
			}
		}else if(gps_enabled && answer == "No") {
			Toast.makeText(getApplicationContext(), "You can Park offline :)", Toast.LENGTH_SHORT).show();	
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locListener);
			RelativeLayout GpsWarning = (RelativeLayout)findViewById(R.id.GpsWarning);	
		    GpsWarning.setVisibility(View.GONE); 
		}
		else if(gps_enabled){
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locListener);
			RelativeLayout GpsWarning = (RelativeLayout)findViewById(R.id.GpsWarning);	
		    GpsWarning.setVisibility(View.GONE); 	
		}
		ads2 = (WebView)findViewById(R.id.ads2);
		ads2.getSettings().setJavaScriptEnabled(true);
	    ads2.setBackgroundColor(Color.TRANSPARENT);
	    ads2.loadUrl("URL TO SHOW AD HERE");
		webview2 = (WebView)findViewById(R.id.webView2);
		webview2.loadUrl("file:///android_asset/ring.html");
		webview2.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				return(event.getAction()==MotionEvent.ACTION_MOVE);
			}
		});
		webview = (WebView)findViewById(R.id.webView1);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setDomStorageEnabled(true);
		webview.setWebViewClient(new WebViewClient());
		webview.setWebChromeClient(new WebChromeClient() {
			 public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
			    callback.invoke(origin, true, false);
			 }
			});
		webview.addJavascriptInterface(new WebAppInterface(this), "AndroidFunction");
		final EditText searchedit = (EditText)findViewById(R.id.searchInMap);
		searchedit.setOnEditorActionListener(new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView arg0,int arg1, KeyEvent arg2){
				String uri = searchedit.getText().toString();
				if(uri.length() == 0) {
			Toast.makeText(getApplicationContext(), "You've not written anything!", Toast.LENGTH_SHORT).show();		
				}else{
				Intent mapIntent = new Intent(Intent.ACTION_VIEW,Uri.parse("geo:"+CLat+","+CLng+"?q="+uri));
				mapIntent.setPackage("com.google.android.apps.maps");
				try{
					startActivity(mapIntent);
				}catch(Exception e){
					Toast.makeText(getApplicationContext(), "Sorry, You don't have Google Map installed :(", Toast.LENGTH_SHORT).show();
				}		
			}
				return false;
				}
		});

		webview.loadUrl("file:///android_asset/map.html");
		final TextView openSettings = (TextView)findViewById(R.id.OpenSettings);
		final TextView exit = (TextView)findViewById(R.id.Exit);
		openSettings.setOnClickListener(new Button.OnClickListener(){
			
			@Override
			public void onClick(View arg0) {	
				Intent openLoc = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(openLoc);
				}
			    });
		openSettings.setOnTouchListener(new Button.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event){
				if(event.getAction() == MotionEvent.ACTION_DOWN) {		
					openSettings.setTextColor(Color.rgb(164, 164, 164));	
				}
				if(event.getAction() == MotionEvent.ACTION_UP) {	
					openSettings.setTextColor(Color.rgb(232,74,62));
				}	
				return false;
			}
		});
exit.setOnClickListener(new Button.OnClickListener(){
			
			@Override
			public void onClick(View arg0) {	
				finish();
				}
			    });
		exit.setOnTouchListener(new Button.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event){
				if(event.getAction() == MotionEvent.ACTION_DOWN) {		
					exit.setTextColor(Color.rgb(164, 164, 164));	
				}
				if(event.getAction() == MotionEvent.ACTION_UP) {	
					exit.setTextColor(Color.rgb(232,74,62));
				}	
				return false;
			}
		});
		Button sloc = (Button)findViewById(R.id.shareLoc);
		sloc.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int chk = Double.compare(CLat, CLng);
				if(chk == 0) {
			Toast.makeText(getApplicationContext(), "Let me load the location!", Toast.LENGTH_SHORT).show();		
				}else{
				Intent Share = new Intent();
				Share.setAction(Intent.ACTION_SEND);
				Share.setType("text/plain");
				Share.putExtra(Intent.EXTRA_TEXT ,"Hey, I am here https://maps.google.com/maps?q=(I+Am+Here)@"+CLat+","+CLng);
				startActivity(Intent.createChooser(Share,"Share Your Location"));	
				new RequestTask().execute("shareLoc");
				}
				}
		});
		Button scloc = (Button)findViewById(R.id.shareCarLoc);
		scloc.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int chk = Double.compare(MapLat, MapLng);
				if(chk == 0) {
			Toast.makeText(getApplicationContext(), "You've not parked your car!", Toast.LENGTH_SHORT).show();		
				}else{
					Intent Share = new Intent();
					Share.setAction(Intent.ACTION_SEND);
					Share.setType("text/plain");
					Share.putExtra(Intent.EXTRA_TEXT ,"Hey, My Car parked here https://maps.google.com/maps?q=(My+Car+Location)@"+MapLat+","+MapLng);
					startActivity(Intent.createChooser(Share,"Share Your Car Location"));	
					new RequestTask().execute("shareCarLoc");
				}			
		     }
		});
		final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/MyCarNavigator/";
		File newdir = new File(dir);
		newdir.mkdirs();
		ImageView Storedimg = (ImageView)findViewById(R.id.StoredImg);
		Storedimg.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				count = count + 1;
				String file = dir+count+".png";
				File newfile = new File(file);
				try {
					newfile.createNewFile();
				}catch(IOException e) {}
				outputFileUri = Uri.fromFile(newfile);
				Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
				startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
			}
		});
		final Button navigate = (Button)findViewById(R.id.Navigate);
		navigate.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int chk = Double.compare(MapLat, MapLng);
				if(chk == 0) {
			Toast.makeText(getApplicationContext(), "You've not parked your car!", Toast.LENGTH_SHORT).show();		
				}else{
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/maps?daddr="+MapLat+","+MapLng));
			startActivity(intent);
			new RequestTask().execute("navigate");
				}			
		     }
		});
		navigate.setOnTouchListener(new Button.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event){
			if(loaded == true) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {		
			navigate.setTextColor(Color.rgb(255,255,255));
			navigate.setBackgroundColor(Color.rgb(218, 33, 33));	
				}
				if(event.getAction() == MotionEvent.ACTION_UP) {	
					navigate.setBackgroundColor(Color.rgb(0, 0, 0));
					navigate.setTextColor(Color.rgb(232,74,62)); //#E84A3E
				}	
			}	
				return false;
			}
		});
		Button Remember = (Button)findViewById(R.id.remember);
		Remember.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				RelativeLayout rememberme = (RelativeLayout)findViewById(R.id.rememberme);
				rememberme.setVisibility(View.VISIBLE);
				LinearLayout body = (LinearLayout)findViewById(R.id.body);
				body.setClickable(false);
				rememberme.setClickable(true);
				 ads = (WebView)findViewById(R.id.ads);
					ads.getSettings().setJavaScriptEnabled(true);
				    ads.setBackgroundColor(Color.TRANSPARENT);
				    ads.loadUrl("file:///android_asset/ads.html");
					new RequestTask().execute("remember");
			}
		});
		ImageView CloseR = (ImageView)findViewById(R.id.CloseR);
		CloseR.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				RelativeLayout rememberme = (RelativeLayout)findViewById(R.id.rememberme);
				rememberme.setVisibility(View.GONE);
				LinearLayout body = (LinearLayout)findViewById(R.id.body);
				body.setClickable(true);
				rememberme.setClickable(false);
			}
		});
		final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		String isFirstTime = settings.getString("firstTime", "");
		if(isFirstTime.equals("1")){
			openRateUs = false;
		}
		RateUsCover = (RelativeLayout)findViewById(R.id.RateUsCover);
        star1 = (ImageView)findViewById(R.id.star1);
        star2 = (ImageView)findViewById(R.id.star2);
        star3 = (ImageView)findViewById(R.id.star3);
        star4 = (ImageView)findViewById(R.id.star4);
        star5 = (ImageView)findViewById(R.id.star5);
        
        RateUsCover.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				RateUsCover.setVisibility(View.GONE);
			}
		});
        star1.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				star1.setImageResource(R.drawable.added);
				Editor editor = settings.edit();
				editor.putString("firstTime","1");
				editor.commit();
				openRateUs = false;
				RateUsCover.setVisibility(View.GONE);
				new RequestTask().execute("1");
			}
		});
        star2.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				star1.setImageResource(R.drawable.added);
				star2.setImageResource(R.drawable.added);
				Editor editor = settings.edit();
				editor.putString("firstTime","1");
				editor.commit();
				openRateUs = false;
				RateUsCover.setVisibility(View.GONE);
				new RequestTask().execute("2");
			}
		});
        star3.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				star1.setImageResource(R.drawable.added);
				star2.setImageResource(R.drawable.added);
				star3.setImageResource(R.drawable.added);
				Editor editor = settings.edit();
				editor.putString("firstTime","1");
				editor.commit();
				openRateUs = false;
				RateUsCover.setVisibility(View.GONE);
				new RequestTask().execute("3");
			}
		});
        star4.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				star1.setImageResource(R.drawable.added);
				star2.setImageResource(R.drawable.added);
				star3.setImageResource(R.drawable.added);
				star4.setImageResource(R.drawable.added);
				Editor editor = settings.edit();
				editor.putString("firstTime","1");
				editor.commit();
				final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
				try {
				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
				} catch (android.content.ActivityNotFoundException anfe) {
				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
				}
				openRateUs = false;
				RateUsCover.setVisibility(View.GONE);
				new RequestTask().execute("4");
			}
		});
        star5.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				star1.setImageResource(R.drawable.added);
				star2.setImageResource(R.drawable.added);
				star3.setImageResource(R.drawable.added);
				star4.setImageResource(R.drawable.added);
				star5.setImageResource(R.drawable.added);
				Editor editor = settings.edit();
				editor.putString("firstTime","1");
				editor.commit();
				final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
				try {
				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
				} catch (android.content.ActivityNotFoundException anfe) {
				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
				}
				openRateUs = false;
				RateUsCover.setVisibility(View.GONE);
				new RequestTask().execute("5");
			}
		});
        		
		final Button park = (Button)findViewById(R.id.Park);
		double Latitude = Double.longBitsToDouble(settings.getLong("Latitude", 0));
		double Longitude = Double.longBitsToDouble(settings.getLong("Longitude", 0));
		String imguri = settings.getString("imageuri", "n/a");
		String rtext = settings.getString("RText", "");
		double Empty = 0.0;
		int Lati = Double.compare(Latitude, Empty);
		int Longi = Double.compare(Longitude , Empty);
		if(Lati == 0 && Longi == 0) {
			park.setText("Park");
			park.setBackgroundColor(Color.rgb(0, 0, 0));
			park.setTextColor(Color.parseColor("#E84A3E"));
		}else{
			park.setText("Unpark");
			park.setBackgroundColor(Color.rgb(218, 33, 33));
			park.setTextColor(Color.parseColor("#ffffff"));
		}
		if(imguri.length() > 0) {
			FileInputStream in;
			BufferedInputStream buf;
			try {
				in = new FileInputStream(imguri);
				buf = new BufferedInputStream(in);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 8;
				Bitmap bMap = BitmapFactory.decodeStream(buf,null,options);
				Storedimg.setImageBitmap(bMap);
			}catch(Exception e) {
				}
		}
		EditText rText = (EditText)findViewById(R.id.RText);
		rText.setText(rtext);
		TextView RemoveImg = (TextView)findViewById(R.id.RemoveImg);
		RemoveImg.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Editor remoImg = settings.edit();
				remoImg.remove("imageuri");
				remoImg.commit();
				ImageView Storedimg = (ImageView)findViewById(R.id.StoredImg);
				Storedimg.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_camera));
			}
		});
		TextView saveR = (TextView)findViewById(R.id.SaveR);
		saveR.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			EditText rText = (EditText)findViewById(R.id.RText);
			String txt = rText.getText().toString();
				Editor saveText = settings.edit();
				saveText.putString("RText", txt);
				saveText.commit();
				Toast.makeText(getApplicationContext(), "Saved !", Toast.LENGTH_SHORT).show();
			}
		});
		park.setOnClickListener(new Button.OnClickListener(){
			
			@Override
			public void onClick(View arg0) {
				int yu = Double.compare(CLat , CLng);
				if(yu == 0) {
			Toast.makeText(getApplicationContext(), "Let the Location load !", Toast.LENGTH_SHORT).show();		
				}else{
					final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
					double Latitude = Double.longBitsToDouble(settings.getLong("Latitude", 0));
					double Longitude = Double.longBitsToDouble(settings.getLong("Longitude", 0));
					Editor editor = settings.edit();
					double Empty = 0.0;
					int Lati = Double.compare(Latitude, Empty);
					int Longi = Double.compare(Longitude , Empty);
					if(Lati == 0 && Longi == 0) {
						double Time = System.currentTimeMillis();
						SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
						String InHours = df.format(new Date());
						editor.putLong("Latitude", Double.doubleToLongBits(CLat));
						editor.putLong("Longitude", Double.doubleToLongBits(CLng));
						editor.putLong("Time", Double.doubleToLongBits(Time));
						editor.putString("hours", InHours);
						editor.commit();
						park.setText("Unpark");
						park.setBackgroundColor(Color.rgb(218, 33, 33));
						park.setTextColor(Color.parseColor("#ffffff"));
						MapLat = CLat;
						MapLng = CLng;
						webview.loadUrl("javascript:showParked("+CLat+","+CLng+","+InHours+")");
					    webview.loadUrl("javascript:calcRoute()");
					    new RequestTask().execute("park");
					}else{
						if(openRateUs == true){
							RateUsCover.setVisibility(View.VISIBLE);		
						}
						editor.remove("Latitude");
						editor.remove("Longitude");
						editor.remove("Time");
						editor.remove("hours");
						editor.commit();
						park.setText("Park");
						park.setBackgroundColor(Color.rgb(0, 0, 0));
						park.setTextColor(Color.parseColor("#E84A3E"));
						MapLat = 0.0;
						MapLng = 0.0;
						webview.loadUrl("javascript:RemoveParked()");
						new RequestTask().execute("unpark");
					}	
				}
			}
		});
		arrow = (ImageView)findViewById(R.id.arrow);
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		final double width , height;
		width = metrics.widthPixels;
		height = metrics.heightPixels;
		if(metrics.density == 2.0){	
	    sArrowWidth = 90;
	    sArrowHeight = 90;
	    sArrowTop = 6;
	    sArrowRight=10;
	    ArrowWidth = 400;
	    ArrowHeight=400;
	    ArrowTop = 260;	
	    distanceTop = 440;
		}else if(metrics.density == 1.5) {	
	    sArrowWidth = 67;
		sArrowHeight = 67;
		sArrowTop=4;
	    sArrowRight=7;
	    ArrowWidth = 300;
	    ArrowHeight=300;
	    ArrowTop = 170;
	    distanceTop = 315;
		}else if(metrics.density == 1.0) {
			sArrowWidth=45;
		    sArrowHeight=45;
		    sArrowTop=3;
		    sArrowRight=5;
		    ArrowWidth = 200;
		    ArrowHeight=200;
		    ArrowTop = 130;
		    distanceTop = 220;
		}else if(metrics.density == 0.75){
			sArrowWidth=33;
		    sArrowHeight=33;
		    sArrowTop=2;
		    sArrowRight=4;
		    ArrowWidth = 150;
		    ArrowHeight=150;
		    ArrowTop = 97;
		    distanceTop = 165;
		}
	RelativeLayout arrow = (RelativeLayout)findViewById(R.id.arrowcover);
	TextView distanceo = (TextView)findViewById(R.id.distanceo);
	if(answer == "Yes") {
		RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(sArrowWidth ,sArrowHeight);
		parms.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		parms.setMargins(0, sArrowTop, sArrowRight, 0); //left,top,right,bottom
		arrow.setLayoutParams(parms);	
		distanceo.setVisibility(View.GONE);
	}
   }
	private final SensorEventListener mSensorListener = new SensorEventListener() {	
		float[] mGravity;
		float[] mGeomagnetic;
		@Override
		   public void onSensorChanged(SensorEvent event) {
			if(event.sensor == mAccelerometer) {
				mGravity = event.values;
			}else if(event.sensor == mMagnetometer) {
			   mGeomagnetic = event.values;
			}
			if(mGravity != null && mGeomagnetic != null) {
				float R[] = new float[9];
				float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
			if(success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				azimuth = orientation [0];
				float ori = (float)(Math.toDegrees(azimuth)+360)%360;
				ori -= bearing(CLat, CLng, MapLat, MapLng); 
				double chk = Double.compare(MapLat, MapLng);
				if(chk != 0){
				arrow.setRotation(ori);	
				}
		    	}
			}
		}	
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			
		}
	};
	
    	// Create a new HttpClient and Post Header
    class RequestTask extends AsyncTask<String, String, String>{
    	@Override
    	protected String doInBackground(String... uri){
        	HttpClient httpclient = new DefaultHttpClient();
        	String responseBody = "";
        	try {
        		HttpPost httppost = new HttpPost("URL TO SAVE USER BEHAVIOUR TO A PHP FILE");
        	    // Add your data
        	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        	    nameValuePairs.add(new BasicNameValuePair("rate", uri[0]));
        	    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        	    // Execute HTTP Post Request
        	    HttpResponse response = httpclient.execute(httppost);
        	    responseBody = EntityUtils.toString(response.getEntity());
        	} catch (ClientProtocolException e) {
        	    // TODO Auto-generated catch block
        	} catch (IOException e) {
        		
        	}
        	return responseBody;
    	}
    	@Override
    	protected void onPostExecute(String result){
    		super.onPostExecute(result);
    		if(openRateUs == false && result.matches("\\d+(?:\\.\\d+)?")){
        		Toast.makeText(getApplicationContext(), "Thank you for your "+result+" star", Toast.LENGTH_SHORT).show();	
    		}
    	}
    }
    	
    //}
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
    	if (RateUsCover.getVisibility() == View.VISIBLE) {
    		RateUsCover.setVisibility(View.GONE);
    	} else {
            if (doubleBackToExitPressedOnce) {
                finish();
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;                       
                }
            }, 2000);
    	}
    } 
	@Override
	protected void onResume() {
		super.onResume();	
		ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		resume_gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if(answer.equals("No") && null != activeNetwork) {
			Toast.makeText(getApplicationContext(), "Now your Internet is On :)", Toast.LENGTH_SHORT).show();	
		}
		if(gps_enabled == false && resume_gpsEnabled == true){
			Toast.makeText(getApplicationContext(), "GPS is Now Enabled..", Toast.LENGTH_SHORT).show();	
		    Intent start = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
			start.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(start);
		}
		
		sensorManager.registerListener(mSensorListener, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(mSensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
	}
	@Override
	protected void onActivityResult (int requestCode,int resultCode,Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
			if(requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
				ImageView Storedimg = (ImageView)findViewById(R.id.StoredImg);
				FileInputStream in;
				BufferedInputStream buf;
				try {
					in = new FileInputStream("/sdcard/Pictures/MyCarNavigator/"+count+".png");
					buf = new BufferedInputStream(in);
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 8;
					Bitmap bMap = BitmapFactory.decodeStream(buf,null,options);
					Storedimg.setImageBitmap(bMap);
					SharedPreferences saveImg = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
				    String uri = "/sdcard/Pictures/MyCarNavigator/"+count+".png";
				    Editor save = saveImg.edit();
				    save.putString("imageuri", uri);
				    save.commit();
				}catch(Exception e) {
				Toast.makeText(getApplicationContext(), "Failed to save, please try again.", Toast.LENGTH_SHORT).show();	
				}	
	}
	}
	protected double bearing(double startLat, double startLng, double endLat, double endLng) {
		Location mylocation = new Location("");
		Location dest_location = new Location("");
		dest_location.setLatitude(MapLat);
		dest_location.setLongitude(MapLng);
		mylocation.setLatitude(CLat);
		mylocation.setLongitude(CLng);
		return mylocation.bearingTo(dest_location);
	}
	private boolean MyStartActivity(Intent aIntent) {
	    try
	    {
	        startActivity(aIntent);
	        return true;
	    }
	    catch (ActivityNotFoundException e)
	    {
	        return false;
	    }
	}
	 
	//On click event for rate this app button
	public void btnRateAppOnClick(View v) {
	    Intent intent = new Intent(Intent.ACTION_VIEW);
	    //Try Google play
	    intent.setData(Uri.parse("market://details?id=appinventor.ai_aditraj2.My_Car_Navigator"));
	    if (!MyStartActivity(intent)) {
	        //Market (Google play) app seems not installed, let's try to open a webbrowser
	        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=appinventor.ai_aditraj2.My_Car_Navigator"));
	        if (!MyStartActivity(intent)) {
	            //Well if this also fails, we have run out of options, inform the user.
	            Toast.makeText(this, "Could not open Android market, please install the market app.", Toast.LENGTH_SHORT).show();
	        }
	    }
	}
     private class myLocationListener implements LocationListener {
			@Override
			public void onLocationChanged(final Location location) {
				if(location != null) {
			CLat = location.getLatitude();
			CLng = location.getLongitude();		
			Button navigate = (Button)findViewById(R.id.Navigate);
			navigate.setTextColor(Color.parseColor("#E84A3E"));
			final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
			double Latitude = Double.longBitsToDouble(settings.getLong("Latitude", 0));
			double Longitude = Double.longBitsToDouble(settings.getLong("Longitude", 0));
			double time = Double.longBitsToDouble(settings.getLong("Time", 0));
			String HourMe = settings.getString("hours", null);
			MapLat = Latitude;
			MapLng = Longitude;
			Location mylocation = new Location("");
			Location dest_location = new Location("");
			dest_location.setLatitude(MapLat);
			dest_location.setLongitude(MapLng);
			mylocation.setLatitude(CLat);
			mylocation.setLongitude(CLng);
			distance = mylocation.distanceTo(dest_location);
			TextView distanceo = (TextView)findViewById(R.id.distanceo);
			distanceo.setText(Math.round(distance)+" m");
			double chk = Double.compare(MapLat, MapLng);
			if(chk == 0){
		    distance = 0; 
			distanceo.setText("");
			}
			if(gps_enabled){
				RelativeLayout loading = (RelativeLayout)findViewById(R.id.loading);	
			    loading.setVisibility(View.GONE);
			}
			double Empty = 0.0;
			int Lati = Double.compare(Latitude, Empty);
			int Longi = Double.compare(Longitude , Empty);
			Button park = (Button)findViewById(R.id.Park);
			webview.loadUrl("javascript:calcRoute()");
			if(Lati == 0 && Longi == 0) {
				webview.loadUrl("javascript:RemoveParked()");
				park.setTextColor(Color.parseColor("#E84A3E"));
				}else{
				webview.loadUrl("javascript:showParked("+CLat+","+CLng+",'"+HourMe+"')");
				park.setTextColor(Color.parseColor("#ffffff"));
				}
			loaded = true;
			webview.loadUrl("javascript:showPosition("+CLat+","+CLng+",'"+Math.round(distance)+"')");
				}
			}
			@Override
			public void onProviderDisabled(String Provider) {
				}
			@Override
			public void onProviderEnabled(String Provider) {
				}
			@Override
			public void onStatusChanged(String Provider, int status, Bundle extras) {
				}
     }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public class WebAppInterface {
		Context mContext;
		WebAppInterface(Context c) {
			mContext = c;
		}
		@JavascriptInterface
		public void complete(String toast) {
			if(gps_enabled){
				
			}else if(network_enabled && gps_enabled==false){
				RelativeLayout loading = (RelativeLayout)findViewById(R.id.loading);	
			    loading.setVisibility(View.GONE);	
			}
			}
	}
}
