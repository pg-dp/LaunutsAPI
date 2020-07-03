package launutsapi;

import java.util.ArrayList;

public class Nuts {

	private String nutsId;
	private String nutsName;
	private String geometryType;
	private String nutsLevel;
	private ArrayList<String[]> coordinates;
	private ArrayList<String[]> innerRings;
	private String msg = "Query was not successful";
	
	public Nuts(String nutsId, String nutsName, String geometryType, String nutsLevel, ArrayList<String[]> coordinates,
			ArrayList<String[]> innerRings, String msg) {
		super();
		this.nutsId = nutsId;
		this.nutsName = nutsName;
		this.geometryType = geometryType;
		this.nutsLevel = nutsLevel;
		this.coordinates = coordinates;
		this.innerRings = innerRings;
		this.msg = msg;
	}
	
	public Nuts(String text) {
		this.msg = text;
	}
	
	public Nuts() {
		
	}

	public String getNutsId() {
		return nutsId;
	}

	public void setNutsId(String nutsId) {
		this.nutsId = nutsId;
	}

	public String getNutsName() {
		return nutsName;
	}

	public void setNutsName(String nutsName) {
		this.nutsName = nutsName;
	}

	public String getGeometryType() {
		return geometryType;
	}

	public void setGeometryType(String geometryType) {
		this.geometryType = geometryType;
	}

	public String getNutsLevel() {
		return nutsLevel;
	}

	public void setNutsLevel(String nutsLevel) {
		this.nutsLevel = nutsLevel;
	}

	public ArrayList<String[]> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(ArrayList<String[]> coordinates) {
		this.coordinates = coordinates;
	}

	public ArrayList<String[]> getInnerRings() {
		return innerRings;
	}

	public void setInnerRings(ArrayList<String[]> innerRings) {
		this.innerRings = innerRings;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	
	
}