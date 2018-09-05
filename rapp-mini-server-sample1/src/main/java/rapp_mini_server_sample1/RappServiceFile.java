package rapp_mini_server_sample1;

public class RappServiceFile {

	private String path;
	
	private String content;

	public RappServiceFile() {
		
	}

	public RappServiceFile(String path, String content) {
		super();
		this.path = path;
		this.content = content;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
}
