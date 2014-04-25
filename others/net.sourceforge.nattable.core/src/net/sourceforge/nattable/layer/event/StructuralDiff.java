package net.sourceforge.nattable.layer.event;

import net.sourceforge.nattable.coordinate.Range;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class StructuralDiff {

	public enum DiffTypeEnum {
		ADD, CHANGE, DELETE;
	}
	
	private DiffTypeEnum diffType;
	
	private Range beforePositionRange;
	
	private Range afterPositionRange;
	
	public StructuralDiff(DiffTypeEnum diffType, Range beforePositionRange, Range afterPositionRange) {
		this.diffType = diffType;
		this.beforePositionRange = beforePositionRange;
		this.afterPositionRange = afterPositionRange;
	}
	
	public DiffTypeEnum getDiffType() {
		return diffType;
	}
	
	public Range getBeforePositionRange() {
		return beforePositionRange;
	}
	
	public Range getAfterPositionRange() {
		return afterPositionRange;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if ((obj instanceof StructuralDiff) == false) {
			return false;
		}
		
		StructuralDiff that = (StructuralDiff) obj;

		return new EqualsBuilder()
			.append(this.diffType, that.diffType)
			.append(this.beforePositionRange, that.beforePositionRange)
			.append(this.afterPositionRange, that.afterPositionRange)
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(57, 55)
			.append(diffType)
			.append(beforePositionRange)
			.append(afterPositionRange)
			.toHashCode();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()
			+ " " + diffType
			+ " before: " + beforePositionRange
			+ " after: " + afterPositionRange;
	}
	
}
