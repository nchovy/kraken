package org.krakenapps.socialweb.facebook.jsonobject.fieldelement;

public class CheckinApplication {

	private String id;
	private String canvasName;
	private String namespace;

	public CheckinApplication(){
		
	}
	public CheckinApplication(String id, String canvasName, String namespace){
		this.id = id;
		this.canvasName = canvasName;
		this.namespace = namespace; 
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCanvasName() {
		return canvasName;
	}

	public void setCanvasName(String canvasName) {
		this.canvasName = canvasName;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

}
