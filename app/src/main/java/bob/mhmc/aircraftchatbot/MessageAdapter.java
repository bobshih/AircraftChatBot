package bob.mhmc.aircraftchatbot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Bob on 2017/5/21.
 */

class MessageAdapter extends BaseAdapter{
	private Context context;
	private List<MessageVo> messageList;


	public MessageAdapter(Context context, List<MessageVo> messages) {
		super();
		this.context = context;
		this.messageList = messages;
	}


	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return messageList.size();
	}


	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return messageList.get(position);
	}


	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}


	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		MessageVo message = messageList.get(position);
		if(convertView == null || (holder = (ViewHolder)convertView.getTag()).flag != message.getDirection())
		{
			holder = new ViewHolder();
			if(message.getDirection() == MessageVo.To_Server)	{
				holder.flag = MessageVo.To_Server;
				convertView = LayoutInflater.from(context).inflate(R.layout.to_server, null);
			}
			else {
				holder.flag = MessageVo.From_Server;
				convertView = LayoutInflater.from(context).inflate(R.layout.from_server, null);
			}
			holder.content = (TextView)convertView.findViewById(R.id.content);
			holder.time = (TextView)convertView.findViewById(R.id.time);
			convertView.setTag(holder);
		}
		holder.content.setText(message.getContent());

		return convertView;
	}

	private static class ViewHolder
	{
		int flag;
		TextView content;
		TextView time;
	}
}
