package bob.mhmc.aircraftchatbot;

/**
 * Created by Bob on 2017/5/21.
 */

public class MessageVo {
	public static final int To_Server = 0;
	public static final int From_Server = 1;

	private int direction;
	private String content;
	private String url;

	public MessageVo(int direction, String content) {
		super();
		this.direction = direction;
		this.content = content;
	}

	public MessageVo(int direction, String content, String url) {
		super();
		this.direction = direction;
		this.content = content;
		this.url = url;
	}
	public int getDirection() {
		return direction;
	}
	public String getContent() {
		return content;
	}
	public String getUrl() {
		return url;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
