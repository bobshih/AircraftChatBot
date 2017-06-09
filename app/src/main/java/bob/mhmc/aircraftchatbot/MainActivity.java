package bob.mhmc.aircraftchatbot;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import bob.mhmc.aircraftchatbot.task.*;

public class MainActivity extends AppCompatActivity {
	// view component parameters
	private ListView messages;
	private Button buttonRecord;
	private Button buttonSend;
	private static EditText editTextInput;
	private ToolbarWithTTSActivity tts;

	// app parameters
	private boolean recordState = false;
	private WavAudioRecorder wavAudioRecorder;
	private String strWav;
	public static String dir_Root = Environment.getExternalStorageDirectory().getPath() + "/ACB/";
	public static List<MessageVo> messageList = new ArrayList<MessageVo>();
	public static List<Flight> flights = new ArrayList<>();
	private MessageAdapter messageAdapter = new MessageAdapter(this, messageList);
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
	private int threshold_count = 0;
	private Handler handler = new Handler();
	CookieManager cookieManager = new CookieManager();
	private int flightIndex;

	// strange parameters
	boolean state_Recorder = true;
	int num_start = 0, num_end = 0;
	private Runnable runnable = new Runnable() {            // 用來檢查是否完成錄音
		@Override
		public void run() {
			double threshold = wavAudioRecorder.getThreshold();
			double energy = wavAudioRecorder.getEnergy();
			buttonRecord.setText(String.format(Locale.TAIWAN, "暫停錄音(%.1f/%.1f)", threshold, energy));
			handler.postDelayed(this, 120);
			if (state_Recorder) {
				if (energy > threshold) {
					num_start++;
					if (num_start == 1) {
						state_Recorder = false;
					}
				} else {
					num_start = 0;
					num_end++;
					if (num_end == 30) {
						stopRecord();
						recognition();
						File file_ = new File(dir_Root + strWav);
						file_.delete();
					}
				}
			} else {
				threshold_count = (energy < threshold) ? ++threshold_count : 0;
				if (threshold_count > 10) {
					stopRecord();
					recognition();
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// set view component parameters
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		messages = (ListView) findViewById(R.id.listView_messages);
		buttonRecord = (Button) findViewById(R.id.button_record);
		buttonSend = (Button) findViewById(R.id.button_send);
		editTextInput = (EditText) findViewById(R.id.editText_input);
		tts = new ToolbarWithTTSActivity();
		tts.create(MainActivity.this);

		// request permissions & find folder
		requestPermissions();
		File file = new File(dir_Root);
		if (!file.exists()) {
			file.mkdir();
		}

		// register events
		buttonRecord.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// hide keyboard
				View view = getCurrentFocus();
				if (view != null) {
					InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
				if (!recordState) {
					recordState = true;

					strWav = sdf.format(Calendar.getInstance().getTime()) + ".wav";
					wavAudioRecorder = WavAudioRecorder.getInstanse(false);
					wavAudioRecorder.setOutputFile(dir_Root + strWav);
					wavAudioRecorder.prepare();
					wavAudioRecorder.start();
					handler.postDelayed(runnable, 240);
				} else {
					stopRecord();
					recognition();
				}
			}
		});

		// setting of listview.
		messages.setAdapter(messageAdapter);
		messages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				MessageVo message = messageList.get(position);
				if (!"".equals(message.getUrl()) & message.getUrl() != null) {
					Uri uri = Uri.parse(message.getUrl());
					Intent it = new Intent(Intent.ACTION_VIEW, uri);
					view.getContext().startActivity(it);
				}
			}
		});

		// setting of send button
		buttonSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// hide keyboard
				View view = getCurrentFocus();
				if (view != null) {
					InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}

				String userMessage = editTextInput.getText().toString().trim();
				if (userMessage.equals("")) return;
				if(flights.size() < 1){
					addMessage2ListView(MessageVo.To_Server, userMessage);
					SendMessageTask myClientTask = new SendMessageTask(editTextInput.getText().toString());
					myClientTask.execute();
					return;
				}
				addMessage2ListView(MessageVo.To_Server, userMessage);
				editTextInput.setText("");
				String res = "";
				if(userMessage.contains("next")){
					flightIndex++;
					if(flightIndex >= flights.size()){
						res = "no more other flights";
					}else {
						res = "the next one is "+flights.get(flightIndex).getResponseString();
					}
				}else if(userMessage.contains("cheap")){
					flightIndex = 0;
					Collections.sort(flights, Comparisions.cheapComparision);
					res = "the cheapest one is "+flights.get(flightIndex).getResponseString();
				}else if(userMessage.contains("expensive")){
					flightIndex = 0;
					Collections.sort(flights, Comparisions.expensiveComparision);
					res = "the most expensive one is "+flights.get(flightIndex).getResponseString();
				}else if(userMessage.contains("short")){
					flightIndex = 0;
					Collections.sort(flights, Comparisions.shortComparision);
					res = "the shortest duration flight is "+flights.get(flightIndex).getResponseString();
				}else if(userMessage.contains("earlier")){
					flightIndex = 0;
					Collections.sort(flights, Comparisions.earlyComparision);
					res = "the earlier one is "+flights.get(flightIndex).getResponseString();
				}else if(userMessage.contains("later")){
					flightIndex = 0;
					Collections.sort(flights, Comparisions.lateComparision);
					res = "the later one is "+flights.get(flightIndex).getResponseString();
				}else if(userMessage.contains("research")){
					flightIndex = 0;
					flights.removeAll(flights);
					res = "alright, please tell me which city do you want to depart from";
				}else if(userMessage.contains("i want this")){
					Flight t = flights.get(flightIndex);
					flights.removeAll(flights);
					res = "I get it. I'll book "+t.getResponseString()+", and send you a bill to your cellphone with the detail later";
				}
				tts.speak(res);
				addMessage2ListView(MessageVo.From_Server, res);
			}
		});

		// give a hello word to user
		boolean flag = true;
		final String helloWord = "Hello, how's it going?";
		for (MessageVo message : messageList) {
			if (message.getContent().equals(helloWord)) {
				flag = false;
				break;
			}
		}
		if (flag) {
			addMessage2ListView(MessageVo.From_Server, helloWord);
		}
	}

	private void addMessage2ListView(int messageDirection, String messageContent) {
		if(messageContent.equals("")){
			return;
		}
		messageList.add(new MessageVo(messageDirection, messageContent));
		messages.setSelection(messageAdapter.getCount() - 1);
	}

//	private void addMessage2ListView(int messageDirection, String messageContent, String URL) {
//		messageList.add(new MessageVo(messageDirection, messageContent, URL));
//		messages.setSelection(messageAdapter.getCount() - 1);
//	}

	private void stopRecord() {
		buttonRecord.setText("錄音");
		recordState = false;
		state_Recorder = true;
		num_start = 0;
		num_end = 0;
		handler.removeCallbacks(runnable);
		wavAudioRecorder.stop();
		wavAudioRecorder.release();
		state_Recorder = true;
		threshold_count = 0;
	}

	private boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}

	private void recognition() {
		if (isConnected()) {
			new UploadAndRecognition(MainActivity.this).execute();
		} else {
			android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(MainActivity.this);
			dialog.setCancelable(false);
			dialog.setTitle("請開啟網路");
			dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialoginterface, int i) {
					//MainActivity.this.finish();
				}
			});
			dialog.show();
		}
	}

	static public void showRecognitionResult(String result) {
		editTextInput.append(result);
	}

	private void requestPermissions() {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M)
			return;

		final List<String> permissionsList = new ArrayList<>();
		if (this.checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
			permissionsList.add(Manifest.permission.INTERNET);
		if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
		if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
			permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (this.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
			permissionsList.add(Manifest.permission.RECORD_AUDIO);

		if (permissionsList.size() >= 1) {
			this.requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 0x00);
		}
	}

	private class SendMessageTask extends AsyncTask<Void, Void, Void> {
		private String URL_response = "http://andymememe.hopto.org:8080/FlightBooking/ChatBot";
		private EditText waitingMessage;

		// parameters
		private int dotCount = 0;
		JSONObject serverResponse = null;
		private String message;
		private Runnable waitingServer = new Runnable() {
			@Override
			public void run() {
				dotCount++;
				String message = "Ann is writing";
				dotCount = dotCount >= 4 ? 0 : dotCount;
				for (int i = 0; i < 3; i++) {
					if (i <= dotCount) {
						message = message + "．";
					} else message = message + "　";
				}
				waitingMessage.setText(message);
				handler.postDelayed(waitingServer, 200);
			}
		};

		SendMessageTask(String userMessage) {
			waitingMessage = (EditText) findViewById(R.id.editText_waitingMessage);
			message = userMessage;
			editTextInput.setText("");            // clear user's input
		}

		@Override
		protected void onPreExecute() {
			dotCount++;
			waitingMessage.setText("Ann is writing．　　");
			handler.postDelayed(waitingServer, 200);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			String urlString = URL_response;
			HttpURLConnection connection;
			//	String urlString = URL_test;
			try {
				// open a URL connection to the Servlet
				URL url = new URL(urlString);

				// Open a HTTP connection to the URL
				connection = (HttpURLConnection) url.openConnection();

				// Allow Inputs & Output
				connection.setDoInput(true);
				connection.setDoOutput(true);

				// Don't use a cached copy.
				connection.setUseCaches(false);

				// set parameters
				String parameter = String.format("dialogue=%s", message);
				byte[] postData = parameter.getBytes(StandardCharsets.UTF_8);

				// Use a post method.
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Connection", "Keep-Alive");
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestProperty("charset", "utf-8");
				connection.setRequestProperty("Content-Length", Integer.toString(postData.length));                // set cookie to connection
				if (cookieManager.getCookieStore().getCookies().size() > 0) {
					// While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
					connection.setRequestProperty("Cookie",
							TextUtils.join(";", cookieManager.getCookieStore().getCookies()));
				}

				// send data
				DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
				wr.write(postData);

				// get cookie
				Map<String, List<String>> headerFields = connection.getHeaderFields();
				List<String> cookiesHeader = headerFields.get("Set-Cookie");
				if (cookiesHeader != null) {
					for (String cookie : cookiesHeader) {
						cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
					}
				}

				// decode response
				InputStream is = new BufferedInputStream(connection.getInputStream());
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String inputLine;
				StringBuilder stringBuilder = new StringBuilder();
				while ((inputLine = br.readLine()) != null) {
					stringBuilder.append(inputLine);
				}
				serverResponse = new JSONObject(stringBuilder.toString());
				switch (serverResponse.getInt("status")) {
					case 0:
//						serverMessage = serverResponse.getString("response");
						break;
					case 1:
//						serverMessage = serverResponse.getString("response");
//						URL_flights = String.format("http://140.116.82.99/aircraftbot/detail.php?flights=%s", serverResponse.getJSONArray("flights"));
						break;
					default:
						serverResponse.put("response", "it is an error, please feel free to contact developers");
				}
				is.close();
			} catch (JSONException | IOException ex) {
				Log.e("Msg", "error: " + ex.getMessage(), ex);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			handler.removeCallbacks(waitingServer);

			String sendContent;
			try {
				sendContent = serverResponse.getString("response").trim().replaceAll("\r", "")
						.replaceAll("\t", "").replaceAll("\n", "")
						.replaceAll("\f", "");

				if (!sendContent.equals("")) {
					if(!sendContent.equals("This is what I found")){
						tts.speak(sendContent);
						addMessage2ListView(MessageVo.From_Server, sendContent);
					} else if(serverResponse.getJSONArray("flights").length() > 0){
						tts.speak(sendContent);
						addMessage2ListView(MessageVo.From_Server, sendContent);
						JSONArray temp = serverResponse.getJSONArray("flights");
						MainActivity.flights = new ArrayList<>();
						for(int i = 0;i < temp.length();i++){
							JSONObject t = temp.getJSONObject(i);
							Flight f = new Flight(t.getString("depart-time"), t.getString("arrive-time"), t.getString("airlines"), t.getString("stop"), t.getString("price"), t.getString("duration"));
							if(!MainActivity.flights.contains(f)){
								MainActivity.flights.add(f);
							}else {
								Log.d("dddd", "onPostExecute: find the same flight");
							}
						}
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								Collections.sort(MainActivity.flights, Comparisions.cheapComparision);
								flightIndex = 0;
								String message = "the cheapest one is "+MainActivity.flights.get(flightIndex).getResponseString();
								tts.speak(message);
								addMessage2ListView(MessageVo.From_Server, message);
							}
						}, 1000);
					} else {
						sendContent = "I can't find any flight now. There is no any flight met your requests.";
						tts.speak(sendContent);
						addMessage2ListView(MessageVo.From_Server, sendContent);
					}
				}
			} catch (JSONException e) {
				return ;
			}

			waitingMessage.setText("");
			super.onPostExecute(result);
		}

	}
}
