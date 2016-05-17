package frontendserver;

import java.util.HashMap;

public class HTTPRequest {
	
	public String type; 
	public String path;
	public HashMap<String, String> params;
	
	public HTTPRequest(String request){
		params = new HashMap<String, String>();
		this.type = request.split(" ")[0];
		String pathstring = request.split(" ")[1];
		this.path = request.split(" ")[1].substring(pathstring.indexOf("/") + 1, pathstring.indexOf("?"));
		String paramsstring = pathstring.substring(pathstring.indexOf("?")+1, pathstring.length());
		String[] paramsarray = paramsstring.split("&");
		for (String param : paramsarray){
			params.put(param.split("=")[0], param.split("=")[1]);
		}
	}

}
