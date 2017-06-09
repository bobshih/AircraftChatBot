package bob.mhmc.aircraftchatbot;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.util.Locale;

/**
 * Created by Bob on 2017/5/23.
 */

public class ToolbarWithTTSActivity extends AppCompatActivity {
	public TextToSpeech tts;

	@Override
	protected void onDestroy()
	{
		// 釋放 TTS
		if( tts != null ) tts.shutdown();
		super.onDestroy();
	}

	public void create(Context context){
		if(tts == null){
			tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
				@Override
				public void onInit(int status) {
					if (status == TextToSpeech.SUCCESS){		// 初始化成功
						Locale locale = Locale.US;
						// choose the u.s.
						if(tts.isLanguageAvailable(locale) == TextToSpeech.LANG_COUNTRY_AVAILABLE){
							tts.setLanguage(locale);
						}else {
							Functions.installVoiceData();
						}
					}
				}
			});
		}
	}

	public void speak(String string){
		tts.speak(string, TextToSpeech.QUEUE_FLUSH, null, this.hashCode()+"");
	}
}
