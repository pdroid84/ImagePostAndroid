package com.example.imagepostandroid;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	EditText mEditText;
	ImageView mImageView;
	String input;
	//This is the url for WEB (Browser)
//	final String BASE_URL = "http://localhost:8888/";
	//This is the url for Android emulator
//	final String BASE_URL = "http://10.0.2.2:8888/";
	//This is the url for genemotion emulator
	final String BASE_URL = "http://10.0.3.2:8888/";
	final String UPLOAD_URL = "upload";
	final String SERVE_URL = "serve?title=";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d("DEB", "onCreate is called!");
		mEditText = (EditText) findViewById(R.id.editText1);
		mImageView = (ImageView) findViewById(R.id.imageView1);
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
	
	public void getImage (View v) {
		Log.d("DEB", "getImage is called!");
		input = mEditText.getText().toString();
		Log.d("DEB","input= "+ input);
		Toast.makeText(getApplicationContext(), "getImage is called", Toast.LENGTH_LONG).show();
		String[] params = {BASE_URL+SERVE_URL+input};
		new GetImageAsyncTask(this).execute(params);
	}
	
	public void postImage (View v) {
		Log.d("DEB", "postImage is called!");
		input = mEditText.getText().toString();
		Log.d("DEB","input= "+ input);
		Toast.makeText(getApplicationContext(), "postImage is called", Toast.LENGTH_LONG).show();
		String[] params = {BASE_URL+UPLOAD_URL,input};
		new PostImageAsyncTask(this).execute(params);
	}
	
	private class GetImageAsyncTask extends AsyncTask<String, Void, ByteArrayOutputStream> {
		Context context;
		private ProgressDialog pd;
	    private HttpURLConnection conn;
	    ByteArrayOutputStream mReadBuffer = new ByteArrayOutputStream();


		public GetImageAsyncTask(Context context) {
			this.context = context;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(context);
			pd.setMessage("Getting the image...");
			pd.show();
		}

		protected ByteArrayOutputStream doInBackground(String... params) {
			
			Log.d("DEB","The value of params[0] = " + params[0]);
			try
			{
				URL url = new URL(params[0]);
			    conn = (HttpURLConnection) url.openConnection();
			    Log.d("DEB","The response code is = "+ conn.getResponseCode());
		//	    Log.d("DEB", "The error message details = " + conn.getErrorStream().toString());
			    Log.d("DEB","The header data field 1 = " + conn.getHeaderField(0));
			    Log.d("DEB","The header data field 2 = " + conn.getHeaderField(1));
			    Log.d("DEB","The header data field 3 = " + conn.getHeaderField(2));
			    Log.d("DEB","The header data field 4 = " + conn.getHeaderField(3));
			    Log.d("DEB","The header data field 5 = " + conn.getHeaderField(4));
			    Log.d("DEB","The header data field 6 = " + conn.getHeaderField(5));
			    Log.d("DEB","The header data field 7 = " + conn.getHeaderField(6));
			    Log.d("DEB","The header data field 8 = " + conn.getHeaderField(7));
			    Log.d("DEB","The header data field 9 = " + conn.getHeaderField(8));
			    Log.d("DEB","The header data field 10 = " + conn.getHeaderField(9));
			    InputStream ins = new BufferedInputStream(conn.getInputStream());
			    byte[] buffer = new byte[1024];
			    int len = 0;
			    while((len=ins.read(buffer, 0, buffer.length)) != -1) {
			    	mReadBuffer.write(buffer, 0, len);
			    } 
			    return mReadBuffer;
			}
			catch(IOException e)
			{
				Log.d("DEB", "Exception is occurred");
			  //  Log.d("DEB", "The error message details = " + conn.getErrorStream().toString());
			    e.printStackTrace();
			    return null;
			}
			finally {
				conn.disconnect();
			}
		}

		protected void onPostExecute(ByteArrayOutputStream resp) {
			// Clear the progress dialog and the fields
			pd.dismiss();
			// Display success message to user
						if (resp != null) {
							Log.d("DEB", "Reponse from the background is NOT null");
							byte[] data = resp.toByteArray();
						    mImageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
						}
						else {
							Log.d("DEB", "Reponse from the background is null");
						}
	}
	}
	private class PostImageAsyncTask extends AsyncTask<String, Void, String> {
		Context context;
		private ProgressDialog pd;
		private HttpURLConnection conn;
		private String boundary;
		// Line separator required by multipart/form-data.
	    private String LINE_FEED = "\r\n";
	    private String fileField = "fileField";
	    private String fileName = "test1.jpg";
	    private String CHARSET = "UTF-8";
	    byte[] image;
		

		public PostImageAsyncTask(Context context) {
			this.context = context;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(context);
			pd.setMessage("Posting the image...");
			pd.show();
		}

		protected String doInBackground(String... params) {
			
		    System.out.println("This input parameter can be used later here in this program = " + params[1]);
		    Bitmap bitmap=BitmapFactory.decodeResource(getResources(), R.drawable.test1);
		    if (bitmap == null) {
		    	Log.d("DEB","The bitmap contains NULL");
		    }
		    else {
		    	Log.d("DEB","The bitmap is NOT null");
		    	ByteArrayOutputStream stream=new ByteArrayOutputStream();
			    bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
			    image=stream.toByteArray();
		    }
		    
		    
		//    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		//    builder.addTextBody("title", "Title 1", ContentType.TEXT_PLAIN);
		//    builder.addBinaryBody("file", image);
		//    builder.build()

			try
			{
				URL url = new URL(params[0]);
			    conn = (HttpURLConnection) url.openConnection();
			    conn.setDoInput(true);
			    conn.setRequestProperty("Accept-Charset", CHARSET);
			    //following to ensure it's a PSOT request
			    conn.setDoOutput(true);
			    //following to ensure that connection does not force to buffer the complete request body in memory
			    conn.setChunkedStreamingMode(0);
			 // creates a unique boundary based on time stamp
		        boundary = Long.toHexString(System.currentTimeMillis());
			    //set content length
			    conn.setRequestProperty("Content-Length", Integer.toString(image.length));
			    //test filed
			    conn.setRequestProperty("Test", "Bonjour");
			  //set content type header
			    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
			  //send the POST out
			    OutputStream output = conn.getOutputStream();
			  PrintWriter out = new PrintWriter(new OutputStreamWriter(output,CHARSET),true);
			    //OutputStream out1 = new BufferedOutputStream(conn.getOutputStream());
			    
			    
			    //Now put the byte to output stream
			    if(out != null) {
			    	Log.d("DEB","The outputstream OUT1 is NOT null");
			    	  //adding form field part
			    	out.append("--" + boundary).append(LINE_FEED);
			        out.append("Content-Disposition: form-data; name=\"Field1\"").append(LINE_FEED);
			        out.append("Content-Type: text/plain; charset=" + CHARSET).append(LINE_FEED);
			        out.append(LINE_FEED);
			        out.append("ImageField").append(LINE_FEED);
			        out.flush();
			        //adding from file part
			          out.append("--"+boundary).append(LINE_FEED);
			          out.append("Content-Disposition: form-data; name=\"imgaeFile\"; filename=\"" + fileName + "\"" + LINE_FEED);
			          out.append("Content-Type: image/jpg").append(LINE_FEED);
			        //  out.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
			          out.append(LINE_FEED);
			          out.flush();
			          output.write(image);
			          output.flush(); // Important before continuing with writer!
			          out.append(LINE_FEED).flush(); // CRLF is important! It indicates end of boundary.
			       // End of multipart/form-data.
			          out.append("--" + boundary + "--").append(LINE_FEED).flush();;
			          out.close();
			       }
			       if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			          Log.d("DEB","The response from the server is HTTP_OK");
			       }
			    
			    
			    Log.d("DEB","The response code is = "+ conn.getResponseCode());
		//	    Log.d("DEB", "The error message details = " + conn.getErrorStream().toString());
			    Log.d("DEB","The header data field 1 = " + conn.getHeaderField(0));
			    Log.d("DEB","The header data field 2 = " + conn.getHeaderField(1));
			    Log.d("DEB","The header data field 3 = " + conn.getHeaderField(2));
			    Log.d("DEB","The header data field 4 = " + conn.getHeaderField(3));
			    Log.d("DEB","The header data field 5 = " + conn.getHeaderField(4));
			    Log.d("DEB","The header data field 6 = " + conn.getHeaderField(5));
			    Log.d("DEB","The header data field 7 = " + conn.getHeaderField(6));
			    Log.d("DEB","The header data field 8 = " + conn.getHeaderField(7));
			    Log.d("DEB","The header data field 9 = " + conn.getHeaderField(8));
			    Log.d("DEB","The header data field 10 = " + conn.getHeaderField(9));
			    
			    return "OK";
				
			}
			catch(IOException e)
			{
				Log.d("DEB", "Exception is occurred");
			  //  Log.d("DEB", "The error message details = " + conn.getErrorStream().toString());
			    e.printStackTrace();
			    InputStream in =  conn.getErrorStream();
			    byte[] buf = new byte[1024 * 1024];
			    try {
					in.read(buf);
					String errMsg = buf.toString();
					Log.d("DEB", "The error message from server is = " + errMsg);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			    return null;
			}
			finally {
				conn.disconnect();
			}
		}

		protected void onPostExecute(String resp) {
			// Clear the progress dialog and the fields
			pd.dismiss();
			Log.d("DEB", "On Post execute is called");
			// Display success message to user
			if (resp != null) {
				Log.d("DEB", "Reponse from the background is NOT null.resp= " +resp);
			}
			else {
				Log.d("DEB", "Reponse from the background is NULL");
			}  
		} 
	}
}

