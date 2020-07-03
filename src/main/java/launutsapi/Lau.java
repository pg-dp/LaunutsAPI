package launutsapi;

import java.util.ArrayList;

public class Lau {
	
	private String lauId;
	private String lauName;
	private String geometryType;
	private ArrayList<String[]> coordinates;
	private ArrayList<String[]> innerRings;
	private String msg = "Query was not successful";
	
	
	public Lau(String lauId, String lauName, String geometryType, ArrayList<String[]> coordinates,
			ArrayList<String[]> innerRings, String msg) {
		this.lauId = lauId;
		this.lauName = lauName;
		this.geometryType = geometryType;
		this.coordinates = coordinates;
		this.innerRings = innerRings;
		this.msg = msg;
	}

	public Lau(String text) {
		this.msg = text;
	}
	
	public Lau() {
		
	}

	public String getLauId() {
		return lauId;
	}

	public void setLauId(String lauId) {
		this.lauId = lauId;
	}

	public String getLauName() {
		return lauName;
	}

	public void setLauName(String lauName) {
		this.lauName = lauName;
	}

	public String getGeometryType() {
		return geometryType;
	}

	public void setGeometryType(String geometryType) {
		this.geometryType = geometryType;
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
