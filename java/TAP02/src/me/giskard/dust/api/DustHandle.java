package me.giskard.dust.api;

import me.giskard.dust.core.utils.DustUtils;

public interface DustHandle extends Comparable<DustHandle> {
	DustHandle getUnit();

	DustHandle getType();

	String getId();

	@Override
	default int compareTo(DustHandle o) {
		int d = 1;

		if (null != o) {
			d = DustUtils.safeCompare(getUnit().getId(), o.getUnit().getId());

			if (0 == d) {
				d = DustUtils.safeCompare(getId(), o.getId());
			}
		}

		return d;
	}
}