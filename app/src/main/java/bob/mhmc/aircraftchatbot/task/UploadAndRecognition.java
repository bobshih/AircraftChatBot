package bob.mhmc.aircraftchatbot.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import bob.mhmc.aircraftchatbot.MainActivity;

/**
 * Created by Bob on 2017/5/22.
 */

public class UploadAndRecognition extends AsyncTask<Void, Void, String> {

	private Context mContext;
	private String dir_Root = MainActivity.dir_Root;
	private String recognition_result;
	private String URL_asr = "https://www.google.com/speech-api/v2/recognize?output=json&lang=en-us&key=AIzaSyCEU-Du89FOAfCKpWVYstWpmpnsLW2Q2eg";
//	private HttpURLConnection conn;
	private ProgressDialog pd;


	public UploadAndRecognition(Context mContext) {
		this.mContext = mContext;
	}

	protected void onPreExecute() {
		pd = ProgressDialog.show(mContext , "語音辨識中", "稍等一下...", true);
	}

	protected String doInBackground(Void... v) {

		// TODO Auto-generated method stub
		File dir = new File(dir_Root);
		String filename = dir_Root + dir.listFiles()[0].getName();
		upload(filename);
		pd.dismiss();

		File file_ = new File(filename);
		file_.delete();
		return recognition_result;
	}


	protected void onPostExecute(String result) {
		if(result!=null){
			Log.i("--recognition result--", result);
			ShowResultDialog(result);
		}
	}

	private void upload(String file) {
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1024 * 1024;
		String urlString = URL_asr;
		HttpURLConnection conn = null;

		try {
			// CLIENT REQUEST
			FileInputStream fileInputStream = new FileInputStream(new File(file));

			// open a URL connection to the Servlet
			URL url = new URL(urlString);

			// Open a HTTP connection to the URL
			conn = (HttpURLConnection) url.openConnection();

			// Allow Inputs
			conn.setDoInput(true);

			// Allow Outputs
			conn.setDoOutput(true);

			// Don't use a cached copy.
			conn.setUseCaches(false);

			// Use a post method.
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "audio/l16; rate=16000;");

			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			// create a buffer of maximum size
			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// read file and write it into form...
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			// close streams
			Log.e("Msg", "File is written");
			fileInputStream.close();
			dos.flush();
			dos.close();
		} catch (IOException ex) {
			Log.e("Msg", "error: " + ex.getMessage(), ex);
		}

		try {
			InputStream is = new BufferedInputStream(conn.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String inputLine;
			StringBuilder stringBuilder = new StringBuilder();
			while ((inputLine = br.readLine()) != null) {
				stringBuilder.append(inputLine);
			}
			String response = stringBuilder.toString().substring(13);
			JSONObject temp = new JSONObject(response);
			recognition_result = temp.getJSONArray("result").getJSONObject(0)
					.getJSONArray("alternative").getJSONObject(0)
					.getString("transcript");

			is.close();
		} catch (NullPointerException|IOException|JSONException ex) {
			recognition_result = "";
			Log.e("Msg", "error: " + ex.getMessage(), ex);
		}
	}

	private void ShowResultDialog(String recognition_result) {
		//MainActivity.tt.speak(recognition_result);
		MainActivity.showRecognitionResult(recognition_result);
	}
}
