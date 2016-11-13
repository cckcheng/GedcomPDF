
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Colin Cheng
 */
public class FamilyTree {

	private HashMap<String, DefaultMutableTreeNode> individualList = new HashMap();
	private HashMap<String, DefaultMutableTreeNode> cloneList = new HashMap();
	private List<Family> familyList = new ArrayList();
	private String gedcomFile;
	private StringBuilder err = new StringBuilder();
	private boolean fatal = false;
	private Set<String> gedKeys = new HashSet();

	public static final int SESSION_HEAD = 1;
	public static final int SESSION_INDI = 2;
	public static final int SESSION_FAM = 3;
	public static final int SESSION_SUBM = 4;
	public static final int SESSION_OBJE = 5;

	FamilyTree() {
	}

	public void buildFamilyTree(String gedcomFile) {
		this.gedcomFile = gedcomFile;
		if(!this.readFile()) return;
		this.buildTree();
	}

	static final HashMap<String, String> dictionary = new HashMap() {
		{
			put("BIRT", "出生");
			put("DEAT", "亡故");
			put("DATE", "日期");
			put("PLAC", "地点");
		}
	};

	public void addGedKey(String key) {
		this.gedKeys.add(key);
	}

	public boolean hasError() {
		return this.err.length() > 0;
	}

	public boolean isFatal() {
		return this.fatal;
	}

	public String getErrorMessage() {
		return err.toString();
	}

	public HashMap<String, DefaultMutableTreeNode> getIndividualList() {
		return individualList;
	}

	private Integer getInteger(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return null;
		}
	}

	private boolean readFile() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(this.gedcomFile), Charset.forName("UTF-8")));
//			br = new BufferedReader(new FileReader(this.gedcomFile));
			String s;
			Individual ind = null;
			Family fam = null;
			int session = 0;
			HashMap<Integer, String> keymap = new HashMap();
			while ((s = br.readLine()) != null) {
//				System.out.println(s);
				String[] ss = s.trim().split(" ");
				if(ss.length < 2) continue;
				Integer level = this.getInteger(ss[0]);
				if(level == null) continue;
				if(level == 0) keymap.clear();
				keymap.put(level, ss[1]);

				if(level == 0) {
					if(ss[1].startsWith("@")) {
						if(ss[2].equalsIgnoreCase("INDI")){
							session = SESSION_INDI;
							ind = new Individual();
							ind.setId(ss[1]);
							this.individualList.put(ss[1], new DefaultMutableTreeNode(ind));
							continue;
						}
						if(ss[2].equalsIgnoreCase("FAM")){
							session = SESSION_FAM;
							fam = new Family();
							this.familyList.add(fam);
							continue;
						}
					} else if(ss[1].equalsIgnoreCase("HEAD")){
						session = SESSION_HEAD;
					} else {
						session = 0;
					}
				} else if(session == SESSION_INDI) {
					if(ss.length < 3) continue;
					if(ss[1].equalsIgnoreCase("NAME")) {
						ind.setGivenName(ss[2]);
					} else if(ss[1].equalsIgnoreCase("GIVN")) {
//						System.out.println(ss[2]);
						ind.setGivenName(ss[2]);
					} else if(ss[1].equalsIgnoreCase("SURN")) {
						ind.setFamilyName(ss[2]);
					} else if(ss[1].equalsIgnoreCase("NICK")) {
						ind.addInfo(0, s.substring(ss[0].length() + ss[1].length() + 2));
					} else if(ss[1].equalsIgnoreCase("NSFX")) {
						ind.addInfo(s.substring(ss[0].length() + ss[1].length() + 2));
					} else if(ss[1].equalsIgnoreCase("NOTE")){
						ind.addNote(s.substring(ss[0].length() + ss[1].length() + 2));
					} else if(ss[1].equalsIgnoreCase("CONT")){
						String pKey = keymap.get(level - 1);
						if(pKey != null && pKey.equalsIgnoreCase("NOTE")) {
							ind.addNote(s.substring(ss[0].length() + ss[1].length() + 2));
						}
					} else if(ss[1].equalsIgnoreCase("DATE") || ss[1].equalsIgnoreCase("PLAC")) {
						String pKey = keymap.get(level - 1);
						if(pKey != null && this.gedKeys.contains(pKey)) {
							String val = s.substring(ss[0].length() + ss[1].length() + 2);
							if(ss[1].equalsIgnoreCase("DATE")) {
								val = convertDate(val);
							}
							if(val != null) {
								ind.addInfo(dictionary.get(pKey) + dictionary.get(ss[1]) + ": " + val);
							}
						}
					}
				} else if(session == SESSION_FAM) {
					if(ss.length < 3) continue;
					if(ss[1].equalsIgnoreCase("HUSB")) {
						fam.setFatherID(ss[2]);
					} else if(ss[1].equalsIgnoreCase("WIFE")) {
						fam.setMotherID(ss[2]);
					} else if(ss[1].equalsIgnoreCase("CHIL")) {
						fam.addChild(ss[2]);
					}
				} else if(session == SESSION_HEAD) {
					if(ss.length < 3) continue;
					if(ss[1].equalsIgnoreCase("FILE")) {
//						this.title = ss[2];
					}
				}
			}
			br.close();
		} catch (FileNotFoundException ex) {
			this.err.append(this.gedcomFile).append(" not found.");
			this.fatal = true;
			return false;
		} catch (IOException ex) {
			this.err.append("Failed to read ").append(this.gedcomFile);
			this.fatal = true;
			return false;
		}

		return true;
	}

	private void buildTree() {
		DefaultMutableTreeNode nodeF = null, nodeM = null;
		for(Family fam : this.familyList) {
//			if(fam.noChild()) continue;
			if(this.individualList.containsKey(fam.getFatherID())) {
				nodeF = this.individualList.get(fam.getFatherID());
				this.connectParent(nodeF, fam);
			}

			if (this.individualList.containsKey(fam.getMotherID())) {
				nodeM = this.individualList.get(fam.getMotherID());
				this.connectParent(nodeM, fam);
				if(nodeF != null) setSpouse(nodeF, nodeM);
			}

//			if(this.cloneList.containsKey(fam.getFatherID())) {
//				nodeF = this.cloneList.get(fam.getFatherID());
//				this.connectParent(nodeF, fam);
//			}
//
//			if (this.cloneList.containsKey(fam.getMotherID())) {
//				nodeM = this.cloneList.get(fam.getMotherID());
//				this.connectParent(nodeM, fam);
//			}
		}
	}

	private void connectParent(DefaultMutableTreeNode node, Family fam) {
		DefaultMutableTreeNode cloneNode;
		for(String childID : fam.getChildrean()) {
			if(this.individualList.containsKey(childID)) {
				DefaultMutableTreeNode childNode = this.individualList.get(childID);
				if(childNode.isNodeAncestor(node)) {
					this.err.append("Gedcom File error: ").append(childID).append(" is an ancestor of ")
							.append(((Individual)node.getUserObject()).getId()).append("\n");
					continue;
				}
				if(childNode.isRoot()){
					node.add(childNode);
				} else {
					cloneNode = (DefaultMutableTreeNode) childNode.clone();
					node.add(cloneNode);
//					this.cloneList.put(childID, cloneNode);
				}
			}
		}
	}

	public DefaultMutableTreeNode getRoot() {
		int depth = 0;
		DefaultMutableTreeNode top = null;
		DefaultMutableTreeNode node = null;
		for(Family fam : this.familyList) {
			if(this.individualList.containsKey(fam.getFatherID())) {
				node = this.individualList.get(fam.getFatherID());
			} else if(this.individualList.containsKey(fam.getMotherID())) {
				node = this.individualList.get(fam.getMotherID());
			} else {
				continue;
			}

			if(node.getParent() == null) {
				if(node.getDepth() > depth) {
					depth = node.getDepth();
					top = node;
				}
			}
		}

		// replace the cloned nodes
		replaceClonedNodes(top);

		return top;
	}

	private void replaceClonedNodes(DefaultMutableTreeNode node) {
		Enumeration n = node.children();
		while(n.hasMoreElements()) {
			DefaultMutableTreeNode son = (DefaultMutableTreeNode) n.nextElement();
			Individual ind = (Individual) son.getUserObject();
			DefaultMutableTreeNode org = this.individualList.get(ind.getId());
			if(!org.equals(son)) {
				node.insert(org, node.getIndex(son));
				son.removeFromParent();
				replaceClonedNodes(org);
			} else {
				replaceClonedNodes(son);
			}
		}
	}

	public void printTree() {
		displayNode(this.getRoot());
	}

	private void displayNode(DefaultMutableTreeNode node) {
		if(node == null) return;

		Individual ind = (Individual) node.getUserObject();
		System.out.println(ind.getFullName());
		Enumeration n = node.children();
		while(n.hasMoreElements()) {
			displayNode((DefaultMutableTreeNode) n.nextElement());
		}
	}

	private void setSpouse(DefaultMutableTreeNode nodeF, DefaultMutableTreeNode nodeM) {
		Individual indF = (Individual) nodeF.getUserObject();
		Individual indM = (Individual) nodeM.getUserObject();
		indF.addSpouse(indM);
		indM.addSpouse(indF);
	}

	private String convertDate(String val) {
		DateFormat in = new SimpleDateFormat("dd MMM yyyy");
		DateFormat out = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return out.format(in.parse(val));
		} catch (Exception e) {
		}

		return null;
	}
}
