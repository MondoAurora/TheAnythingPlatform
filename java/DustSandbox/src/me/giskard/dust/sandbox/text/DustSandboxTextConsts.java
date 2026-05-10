package me.giskard.dust.sandbox.text;

import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.net.DustNetConsts;

public interface DustSandboxTextConsts extends DustConsts, DustNetConsts {

	enum EventCommand {
		evtToNext, evtToPrev, evtSplit, evtMergeNext, evtMergePrev
	}

	char[] SENTENCE_SPLIT = {'.', '?', '!'};
}
