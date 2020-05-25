package launutsapi;

import java.util.ArrayList;

public class Nuts {

	private String nut_id;
	private String nuts_name;
	private String geometry_type;
	private String nuts_level;
	private ArrayList<String[]> outer_ring;
	private ArrayList<String[]> inner_rings;
	private String msg = "Query was not successful";
	
	public Nuts(String nuts_id, String nuts_name, String geometry_type, String nuts_level, ArrayList<String[]> outer_ring,
			ArrayList<String[]> inner_rings, String msg) {
		super();
		this.nut_id = nuts_id;
		this.nuts_name = nuts_name;
		this.geometry_type = geometry_type;
		this.nuts_level = nuts_level;
		this.outer_ring = outer_ring;
		this.inner_rings = inner_rings;
		this.msg = msg;
	}
	
	public Nuts(String text) {
		this.msg = text;
	}
	
	public Nuts() {
		
	}

	public String getNutId() {
		return nut_id;
	}

	public void setNutId(String nut_id) {
		this.nut_id = nut_id;
	}

	public String getNutsName() {
		return nuts_name;
	}

	public void setNutsName(String nut_name) {
		this.nuts_name = nut_name;
	}

	public String getNutsLevel() {
		return nuts_level;
	}

	public void setNutsLevel(String nut_level) {
		this.nuts_level = nut_level;
	}

	public ArrayList<String[]> getOuterRing() {
		return outer_ring;
	}

	public void setOuterRing(ArrayList<String[]> outer_ring) {
		this.outer_ring = outer_ring;
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
	
	public String getGeometryType() {
		return geometry_type;
	}

	public void setGeometryType(String geometry_type) {
		this.geometry_type = geometry_type;
	}
	
}