package launutsapi;

import java.util.ArrayList;

public class DataContainer {

	private String nut_id;
	private String nut_name;
	private String nut_level;
	private ArrayList<String[]> outer_ring;
	private ArrayList<String[]> inner_rings;
	
	public DataContainer(String nut_id, String nut_name, String nut_level, ArrayList<String[]> outer_ring,
			ArrayList<String[]> inner_rings) {
		super();
		this.nut_id = nut_id;
		this.nut_name = nut_name;
		this.nut_level = nut_level;
		this.outer_ring = outer_ring;
		this.inner_rings = inner_rings;
	}
	
	public String getNut_id() {
		return nut_id;
	}
	public void setNut_id(String nut_id) {
		this.nut_id = nut_id;
	}
	
	public String getNut_name() {
		return nut_name;
	}
	public void setNut_name(String nut_name) {
		this.nut_name = nut_name;
	}
	public String getNut_level() {
		return nut_level;
	}
	public void setNut_level(String nut_level) {
		this.nut_level = nut_level;
	}
	public ArrayList<String[]> getOuter_ring() {
		return outer_ring;
	}
	public void setOuter_ring(ArrayList<String[]> outer_ring) {
		this.outer_ring = outer_ring;
	}
	public ArrayList<String[]> getInner_rings() {
		return inner_rings;
	}
	public void setInner_rings(ArrayList<String[]> inner_rings) {
		this.inner_rings = inner_rings;
	}
	
}