package net.heartsome.cat.converter.msexcel2007.document;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import net.heartsome.cat.converter.msexcel2007.common.InternalFileException;
import net.heartsome.cat.converter.msexcel2007.common.SpreadsheetUtil;
import net.heartsome.cat.converter.msexcel2007.document.rels.Relationship;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class RelsPart extends AbstractPart {

	private List<Relationship> relationships;

	public RelsPart(AbstractPart part) throws InternalFileException, FileNotFoundException {
		super(SpreadsheetUtil.getPartRelsTarget(part));
		loadRealtionship();
	}

	public List<Relationship> getRelationshipByType(String type) {
		List<Relationship> rlist = new ArrayList<Relationship>();
		for (Relationship r : relationships) {
			if (r.getType().equals(type)) {
				rlist.add(r);
			}
		}
		return rlist;
	}

	public Relationship getRelationshipById(String rId) {
		for (Relationship r : relationships) {
			if (r.getId().equals(rId)) {
				return r;
			}
		}
		return null;
	}

	public void loadRealtionship() {
		relationships = new ArrayList<Relationship>();

		String xpath = "/Relationships/Relationship";
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		try {
			ap.selectXPath(xpath);

			while (ap.evalXPath() != -1) {
				Hashtable<String, String> attrs = vu.getCurrentElementAttributs();
				if (attrs == null) {
					// TODO Error
				}
				String id = attrs.get("Id");
				String type = attrs.get("Type");
				String target = attrs.get("Target");

				Relationship rsp = new Relationship(id, type, target);
				relationships.add(rsp);
			}
		} catch (VTDException e) {
			logger.error("", e);
		}
	}

}
