package me.giskard.boot;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustConsts;
import me.giskard.dust.api.DustHandle;

public class DustGenBootAppManual implements DustConsts {
	public DustGenBootAppManual(String appId) {
		Dust.log(null, "DustGenBootApp", "Manual boot");

		DustHandle HANDLE_DUST_ATT_CTX_APP = Dust.getHandle("giskard.me/dust.1$ctxApp");
		DustHandle HANDLE_DUST_ATT_BINARY_RESOLVER = Dust.getHandle("giskard.me/dust.1$binaryResolver");
		DustHandle HANDLE_MIND_ATT_LISTENERS = Dust.getHandle("giskard.me/mind.1$listeners");
		DustHandle HANDLE_MIND_ATT_NARRATIVE = Dust.getHandle("giskard.me/mind.1$narrative");
		DustHandle HANDLE_MIND_ATT_NEXT = Dust.getHandle("giskard.me/mind.1$next");
		DustHandle HANDLE_STREAM_ATT_SOURCE = Dust.getHandle("giskard.me/stream.1$streamSource");
		DustHandle HANDLE_MISC_ATT_PATH = Dust.getHandle("giskard.me/misc.1$path");
		DustHandle HANDLE_STREAM_ATT_BACKUPFOLDER = Dust.getHandle("giskard.me/stream.1$backupFolder");
		DustHandle HANDLE_DUST_ATT_NODE = Dust.getHandle("giskard.me/dust.1$node");
		DustHandle HANDLE_STREAM_ATT_UNIT_HANDLER = Dust.getHandle("giskard.me/stream.1$unitHandler");

		DustHandle hMsgProcessUnitStream = Dust.getHandle("Lorand/test01$ProcessUnitStream");
		DustHandle hAgtStreamUnitProcessor = Dust.getHandle("Lorand/test01$StreamUnitProcessor");
		DustHandle hNarSerializeJsonApi = Dust.getHandle("giskard.me/stream.1$SerializeJsonApi");

		DustHandle hMsgProvideStream = Dust.getHandle("Lorand/test01$ProvideStream");
		DustHandle hAgtLocalFiles = Dust.getHandle("Lorand/test01$LocalFiles");
		DustHandle hNarFileStreamSource = Dust.getHandle("giskard.me/stream.1$FileStreamSource");

		DustHandle hMsgUnitHandler = Dust.getHandle("Lorand/test01$UnitHandler");
		DustHandle hAgtStreamCentre = Dust.getHandle("Lorand/test01$StreamCentre");
		DustHandle hNarStreamCentre = Dust.getHandle("giskard.me/stream.1$StreamCentre");

		Dust.access(DustAccess.Set, "me.giskard.dust.core.stream.DustStreamJsonApiSerializerAgent", null, HANDLE_DUST_ATT_CTX_APP, HANDLE_DUST_ATT_BINARY_RESOLVER, hNarSerializeJsonApi);
		Dust.access(DustAccess.Set, "me.giskard.dust.core.stream.DustStreamSrcFileAgent", null, HANDLE_DUST_ATT_CTX_APP, HANDLE_DUST_ATT_BINARY_RESOLVER, hNarFileStreamSource);
		Dust.access(DustAccess.Set, "me.giskard.dust.core.machine.DustMachineIOAgent", null, HANDLE_DUST_ATT_CTX_APP, HANDLE_DUST_ATT_BINARY_RESOLVER, hNarStreamCentre);

		Dust.access(DustAccess.Insert, hAgtStreamUnitProcessor, hMsgProcessUnitStream, HANDLE_MIND_ATT_LISTENERS, KEY_ADD);
		Dust.access(DustAccess.Insert, hAgtLocalFiles, hMsgProvideStream, HANDLE_MIND_ATT_LISTENERS, KEY_ADD);
		Dust.access(DustAccess.Insert, hAgtStreamCentre, hMsgUnitHandler, HANDLE_MIND_ATT_LISTENERS, KEY_ADD);

		Dust.access(DustAccess.Set, hMsgProcessUnitStream, hMsgUnitHandler, HANDLE_MIND_ATT_NEXT);

		Dust.access(DustAccess.Set, hNarStreamCentre, hAgtStreamCentre, HANDLE_MIND_ATT_NARRATIVE);
		Dust.access(DustAccess.Set, hMsgProvideStream, hAgtStreamCentre, HANDLE_STREAM_ATT_SOURCE);

		Dust.access(DustAccess.Set, hNarFileStreamSource, hAgtLocalFiles, HANDLE_MIND_ATT_NARRATIVE);
		Dust.access(DustAccess.Set, "localStore", hAgtLocalFiles, HANDLE_MISC_ATT_PATH);
		Dust.access(DustAccess.Set, "backup", hAgtLocalFiles, HANDLE_STREAM_ATT_BACKUPFOLDER);

		Dust.access(DustAccess.Set, hNarSerializeJsonApi, hAgtStreamUnitProcessor, HANDLE_MIND_ATT_NARRATIVE);

		DustHandle hApp = Dust.getHandle(appId);
		Dust.access(DustAccess.Set, hApp, null, HANDLE_DUST_ATT_CTX_APP, HANDLE_DUST_ATT_NODE);
		Dust.access(DustAccess.Set, hMsgUnitHandler, null, HANDLE_DUST_ATT_CTX_APP, HANDLE_DUST_ATT_NODE, HANDLE_STREAM_ATT_UNIT_HANDLER);

	}
}
