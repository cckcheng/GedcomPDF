
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Colin Cheng
 */
public class Family {

	Family() {
	}

	public boolean noChild() {
		return this.childrean.isEmpty();
	}

	protected String fatherID;

	public String getFatherID() {
		return fatherID;
	}

	public void setFatherID(String fatherID) {
		this.fatherID = fatherID;
	}
	protected String motherID;

	public String getMotherID() {
		return motherID;
	}

	public void setMotherID(String motherID) {
		this.motherID = motherID;
	}
	protected List<String> childrean = new ArrayList();

	public List<String> getChildrean() {
		return childrean;
	}

	public void addChild(String childID) {
		this.childrean.add(childID);
	}
}
