package me.giskard.dust.sandbox.text;

import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.net.DustNetConsts;
import me.giskard.tokens.DustGenTokens_lang_1;
import me.giskard.tokens.DustGenTokens_layout_1;
import me.giskard.tokens.DustGenTokens_text_1;

public interface DustSandboxTextConsts extends DustConsts, DustNetConsts, DustGenTokens_text_1, DustGenTokens_layout_1, DustGenTokens_lang_1 {

	enum EventCommand {
		evtToNext, evtToPrev, evtSplit, evtMergeNext, evtMergePrev
	}

	char[] SENTENCE_SPLIT = {'.', '?', '!'};
}
