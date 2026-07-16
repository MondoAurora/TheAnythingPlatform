package me.giskard.dust.sandbox.text;

import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.net.DustNetConsts;
import me.giskard.dust.core.text.DustTextConsts;

public interface DustSandboxTextConsts extends DustConsts, DustNetConsts, DustTextConsts {

	enum EventCommand {
		evtToNext, evtToPrev, evtSplit, evtMergeNext, evtMergePrev
	}

	char[] SENTENCE_SPLIT = {'.', '?', '!'};
}
