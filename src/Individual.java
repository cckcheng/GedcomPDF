
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Colin Cheng
 */
public class Individual implements Comparable {
	private String id;
	private String familyName;
	private String givenName;
	private List<Individual> spouse = new ArrayList();
	private List<String> info = new ArrayList();
	private List<String> notes = new ArrayList();

	private boolean appendLastNote = false;

	Individual() {

	}

	Individual(String family_name, String given_name) {
		this.familyName = family_name;
		this.givenName = given_name;
	}

	Individual(String given_name) {
		this.givenName = given_name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String family_name) {
		this.familyName = family_name;
	}

	public String getPrintName(String rootFamilyName) {
		if(rootFamilyName == null || rootFamilyName.equalsIgnoreCase(this.familyName))return givenName;
		return getFullName();
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String given_name) {
		this.givenName = given_name;
	}

	public List<Individual> getSpouse() {
		return spouse;
	}

	public void addSpouse(Individual spouse) {
		if(this.spouse.contains(spouse)) return;
		this.spouse.add(spouse);
	}

	public List<String> getInfo() {
		return info;
	}

	public void addInfo(String info) {
		this.info.add(info);
	}

	public void addInfo(int idx, String info) {
		this.info.add(idx, info);
	}

	boolean hasInfo() {
		return !info.isEmpty();
	}

	public List<String> getNotes() {
		return notes;
	}

	public void addNote(String note) {
		if(note.toLowerCase().contains("http://")) {
			try {
				note = URLDecoder.decode(note, "UTF-8");
			} catch (UnsupportedEncodingException ex) {
			}
		}

		if(this.appendLastNote) {
			note = this.notes.remove(notes.size() - 1) + note;
		}
		this.notes.add(note);
		this.appendLastNote = note.endsWith(",") || note.endsWith(";") || note.endsWith("，") || note.endsWith("；");
	}

	boolean hasNote() {
		return !notes.isEmpty();
	}

	boolean hasSpouse() {
		return !spouse.isEmpty();
	}

	public String getSpouseName() {
		if(!this.hasSpouse()) return "";

		StringBuilder s = new StringBuilder();
		for(Individual ind : this.spouse) {
			s.append(", ").append(ind.getFullName());
		}
		return s.substring(2);
	}

	public String getFullName() {
		StringBuilder s = new StringBuilder();
		if(this.familyName != null) {
			s.append(this.familyName);
			if(this.familyName.codePointAt(0) < 0x100) s.append(", ");
		}
		if(this.givenName != null) {
			s.append(this.givenName);
		}

		return s.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Individual other = (Individual) obj;
		if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 89 * hash + (this.id != null ? this.id.hashCode() : 0);
		return hash;
	}

	public int compareTo(Object obj) {
		if (obj == null) {
			return -1;
		}
		if (getClass() != obj.getClass()) {
			return -1;
		}
		final Individual other = (Individual) obj;
		return this.getFullName().compareToIgnoreCase(other.getFullName());
	}
}
