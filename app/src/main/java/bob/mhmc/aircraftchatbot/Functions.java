package bob.mhmc.aircraftchatbot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Bob on 2017/5/21.
 */

class Functions {
	public static void Alarm_Tagging(final Context ctx)
	{

		AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
		dialog.setTitle("Start the annotation application");
		dialog.setMessage("Do you want to start the annotation application now?");
		dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {

			}
		});
		dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Call_Tagging(ctx);
			}
		});
		dialog.show();

	}
	public static void Call_Tagging(Context ctx)
	{
		try{
			Intent intent = ctx.getPackageManager().getLaunchIntentForPackage("com.example.penny.myapplication");
			String user_name = Get_file("user_name",ctx);
			Bundle bundle = new Bundle();
			bundle.putString("user_name", user_name);
			intent.putExtras(bundle);
			ctx.startActivity(intent);
		}
		catch(Exception e){
			Toast.makeText(ctx,"Open tagging activity error",Toast.LENGTH_SHORT).show();}
	}

	public static void OutFile(String filename,String content,Context ctx)
	{

		FileOutputStream fos ;
		try{
			fos= ctx.openFileOutput(filename, Context.MODE_PRIVATE);
			fos.write(content.getBytes());
			fos.close();
		}catch (Exception e){e.printStackTrace();}
	}
	public static String Get_file(String filename,Context ctx)
	{
		FileInputStream fin ;
		String str="";
		try{
			fin= ctx.openFileInput(filename);
			byte[] buffer = new byte[8];
			int idx = 0;
			while ((idx = fin.read(buffer)) != -1) {
				str+=new String(buffer, 0, idx);
			}
			fin.close();
			fin.close();
		}catch (Exception e){e.printStackTrace();}
		return str;
	}
	private static boolean is_after(String time1, String time2, SimpleDateFormat sdf, Context ctx) // is time1 after time2
	{

		try
		{
			Date t1 = sdf.parse(time1);
			Date t2 = sdf.parse(time2);
			Long t1_=t1.getTime();
			Long t2_=t2.getTime();
			if(t1_-t2_>0.0) return true;
			else return false;
		}
		catch(Exception e) {Functions.Write_System_data("Time_parsing_error_"+new Date().toString()+".txt","time parsing error",ctx);}
		return false;
	}

	public static String[] Get_clocks(Context ctx)
	{
		String clocks=Functions.Get_file("user_alarmclock",ctx);
		String []splits = clocks.split("_");
		return splits;
	}
	public static void Add_clock(String time,Context ctx)
	{
		String clocks = Functions.Get_file("user_alarmclock",ctx);
		clocks = clocks+"_"+time+" on";
		Functions.OutFile("user_alarmclock",clocks,ctx);
	}
	public static void open_clock(int id , Context ctx)
	{
		String []clocks = Functions.Get_file("user_alarmclock",ctx).split("_");
		if (clocks[id].split(" ")[1].equals("off")) clocks[id] = clocks[id].replace("off","on");
		String tmp = Functions.modified_clock(clocks);
		Functions.OutFile("user_alarmclock",tmp,ctx);

	}
	public static String modified_clock(String [] clocks)
	{
		String tmp ="";
		int clock_len = clocks.length;
		for(int i=0;i<clocks.length;i++)
		{

			if(!clocks[i].equals(""))
			{
				tmp = tmp+clocks[i];
				if(i==clocks.length-2 && clocks[i+1].equals("")){}
				else if(i<clocks.length-1 )tmp = tmp+"_";
			}
		}

		return tmp;
	}
	public static String Refresh_clock(Context ctx)
	{
		boolean is_null = false;
		String[] clocks = Functions.Get_file("user_alarmclock",ctx).split("_");
		String tmp = "";
		for(int i=0;i<clocks.length;i++)
		{
			if(clocks[i].equals("")){is_null = true;}
		}
		tmp = Functions.modified_clock(clocks);
		if(is_null) Functions.OutFile("user_alarmclock",tmp,ctx);
		return tmp;
	}
	public static void Delete_clock(Context ctx,boolean delete,int id) //delete=true ->delete //  delete = false -> clock off
	{
		String [] clocks = Functions.Get_clocks(ctx);

		if(delete) clocks[id] = "";
		else
		{
			if(clocks[id].split(" ")[1].equals("on")) clocks[id]=clocks[id].replace("on","off");
		}
		String tmp = Functions.modified_clock(clocks);

		Functions.OutFile("user_alarmclock",tmp,ctx);
		Log.i("Delete",Functions.Get_file("user_alarmclock",ctx));
	}

	public static boolean Is_notification(String time1,Context ctx)
	{

		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY,22);
		c.set(Calendar.MINUTE,00);      // late upper bound

		Calendar c2 = Calendar.getInstance();
		c2.set(Calendar.HOUR_OF_DAY,10); //early lower bound
		c2.set(Calendar.MINUTE,00);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String time_late = sdf.format(c.getTime());
		String time_early = sdf.format(c2.getTime());

		if((!Functions.is_after(time1,time_late,sdf,ctx))&&Functions.is_after(time1,time_early,sdf,ctx))
		{
			Functions.Write_System_data(time1+"_Is_notification"+".txt","time_late\t"+time_late+"\n"+"time_early\t"+time_early+"\n"+"time_now_"+time1+"\n"+"Check_true",ctx);
			return true;
		}
		else
		{
			Functions.Write_System_data(time1+"_Is_notification"+".txt","time_late\t"+time_late+"\n"+"time_early\t"+time_early+"\n"+"time_now_"+time1+"\n"+"Check_false",ctx);
			return false;
		}
	}
	public static void Modify_clock(int id,String time,Context ctx)
	{
		String [] clocks = Get_clocks(ctx);
		String old_time = clocks[id].split(" ")[0] ;
		clocks[id] = clocks[id].replace(old_time,time);
		String tmp = modified_clock(clocks);
		Functions.OutFile("user_alarmclock",tmp,ctx);
	}


	private static int clock_num = 0;
	private static AlarmManager manager;
	private static PendingIntent pi,pi2;



	public static void Write_Text_data(String filename,String content,Context ctx)
	{
		File file = new File(Environment.getExternalStoragePublicDirectory(""), "Tracking");
		//若目錄不存在則建立目錄
		if(!file.exists()){
			try
			{
				if(!file.mkdirs())   Log.e("mkdir_error","mkdir:failed");
			}
			catch(Exception e){ Toast.makeText(ctx , "mkdir_Error" , Toast.LENGTH_SHORT ).show();}
		}
		Functions.Write_data(filename,content,ctx,file);

	}
	public static boolean Is_alarm(Context ctx,String user)
	{

		Date dt1 =new Date();
		Toast.makeText(ctx,dt1.toString(),Toast.LENGTH_SHORT).show();
		Log.i("time:",dt1.toString());
		boolean is_alarm = true;
		File file = new File(Environment.getExternalStoragePublicDirectory(""), "Tracking");
		if(file.exists()){
			File []f = Environment.getExternalStoragePublicDirectory("/Tracking").listFiles();
			for (int i = 0; i < f.length; i++)
			{
				File files = f[i];
				Log.i("file_for",f[i].getName());
				String filename = files.getName();
				filename = filename.replace(".txt","");
				filename=filename.replace(".wav","");
				String [] split = filename.split("-");
				String user_name = split[0];

				if(user_name.equals(user))   //判斷是否為USER，若是則繼續進行判斷
				{
					is_alarm = true;
                    /*
                    Log.i("filename",filename);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    try
                    {
                        Date dt2 =sdf.parse(split[1]);
                        Log.i("time1",dt1.toString());
                        Log.i("time2",dt2.toString());
                        //取得兩個時間的Unix時間
                        Long ut1=dt1.getTime();
                        Long ut2=dt2.getTime();
                        //相減獲得兩個時間差距的毫秒
                        Long timeP=ut1-ut2; //毫秒差
                        Long sec=timeP/1000;//秒差
                        Log.i("sec",sec.toString());
                        if(sec<30*60*1000) return false;
                    }
                    catch (Exception e){
                        Toast.makeText(ctx , "Time checking error" , Toast.LENGTH_SHORT ).show();
                        Log.i("ERROR","time checking error")
                    }*/
				}
				else is_alarm=false;
			}


		}
		else is_alarm = true;

		return is_alarm;
	}
	public static void Write_System_data(String filename,String content,Context ctx)
	{

		File file = new File(Environment.getExternalStoragePublicDirectory(""), "Tracking_log");
		//若目錄不存在則建立目錄
		if(!file.exists()){
			try
			{
				if(!file.mkdirs())   Log.e("mkdir_error","mkdir:failed");
			}
			catch(Exception e){ Toast.makeText(ctx , "mkdir_Error" , Toast.LENGTH_SHORT ).show();}
		}
		Functions.Write_data(filename,content,ctx,file);

	}
	private static void Write_data(String filename,String content,Context ctx,File file)
	{

		try
		{

			File out = new File(file,filename);
			//寫入至SD卡文件裡
			try{
				FileWriter fw = new FileWriter(out, false);
				BufferedWriter bw = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結
				bw.write(content);
				bw.close();
			}catch (Exception e){e.printStackTrace();}
		}
		catch (Exception e){Toast.makeText(ctx , "IOError" , Toast.LENGTH_SHORT ).show();}
	}
	public static String Get_Data_path(){return Environment.getExternalStoragePublicDirectory("")+"/Tracking";}
	public static String Get_log_path(){return Environment.getExternalStoragePublicDirectory("")+"/Tracking_log";}
	public static void installVoiceData() {
		Intent intent = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setPackage("com.google.android.tts"/*replace with the package name of the target TTS engine*/);
		try {
			Log.i("---------tts---------", "Installing voice data: "+ intent.toUri(0));
			//startActivity(intent);
		} catch (ActivityNotFoundException ex) {
			Log.i( "----tts----", "Failed to install TTS data, no acitivty found for  intent");
		}
	}
}
