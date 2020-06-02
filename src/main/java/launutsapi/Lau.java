package launutsapi;

import java.util.ArrayList;

public class Lau {
	
	private String lau_id;
	private String lau_name;
	private String geometry_type;
	private ArrayList<String[]> coordinates;
	private ArrayList<String[]> inner_rings;
	private String msg = "Query was not successful";
	
	
	public Lau(String lau_id, String lau_name, String geometry_type, ArrayList<String[]> coordinates,
			ArrayList<String[]> inner_rings, String msg) {
		this.lau_id = lau_id;
		this.lau_name = lau_name;
		this.geometry_type = geometry_type;
		this.coordinates = coordinates;
		this.inner_rings = inner_rings;
		this.msg = msg;
	}

	public Lau(String text) {
		this.msg = text;
	}
	
	public Lau() {
		
	}

	public String getLauId() {
		return lau_id;
	}

	public void setLauid(String lau_id) {
		this.lau_id = lau_id;
	}

	public String getLauName() {
		return lau_name;
	}

	public void setLauName(String lau_name) {
		this.lau_name = lau_name;
	}

	public String getGeometryType() {
		return geometry_type;
	}

	public void setGeometryType(String geometry_type) {
		this.geometry_type = geometry_type;
	}

	public ArrayList<String[]> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(ArrayList<String[]> coordinates) {
		this.coordinates = coordinates;
	}

	public ArrayList<String[]> getInnerRings() {
		return inner_rings;
	}

	public void setInnerRings(ArrayList<String[]> inner_rings) {
		this.inner_rings = inner_rings;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	

}
