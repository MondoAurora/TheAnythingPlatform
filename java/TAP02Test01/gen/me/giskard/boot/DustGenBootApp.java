package me.giskard.boot;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.api.DustConsts;
import me.giskard.dust.api.DustException;

// Generation timestamp 20261020T141055Z

public class DustGenBootApp implements DustConsts {
	public DustGenBootApp (String appId) {
		switch(appId) {
		case "Lorand/test01$ThinClient" : {

// Attributes
			DustHandle att_handle_giskard_me_dust___binaryResolver = Dust.getHandle("giskard.me/dust.1$binaryResolver", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_dust___ctxApp = Dust.getHandle("giskard.me/dust.1$ctxApp", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_dust___node = Dust.getHandle("giskard.me/dust.1$node", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_mind___listeners = Dust.getHandle("giskard.me/mind.1$listeners", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_mind___narrative = Dust.getHandle("giskard.me/mind.1$narrative", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_mind___next = Dust.getHandle("giskard.me/mind.1$next", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_misc___fallback = Dust.getHandle("giskard.me/misc.1$fallback", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_misc___path = Dust.getHandle("giskard.me/misc.1$path", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_stream___backupFolder = Dust.getHandle("giskard.me/stream.1$backupFolder", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_stream___streamSource = Dust.getHandle("giskard.me/stream.1$streamSource", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_stream___unitHandler = Dust.getHandle("giskard.me/stream.1$unitHandler", DustHandle.PLACEHOLDER);

// Ideas
			DustHandle idea_handle_Lorand_test___DownloadUnit = Dust.getHandle("Lorand/test01$DownloadUnit", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___LocalFiles = Dust.getHandle("Lorand/test01$LocalFiles", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___ProcessUnitStream = Dust.getHandle("Lorand/test01$ProcessUnitStream", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___RemoteUnitLoader = Dust.getHandle("Lorand/test01$RemoteUnitLoader", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___StreamCentre = Dust.getHandle("Lorand/test01$StreamCentre", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___StreamUnitProcessor = Dust.getHandle("Lorand/test01$StreamUnitProcessor", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___ThinProvideStream = Dust.getHandle("Lorand/test01$ThinProvideStream", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___ThinUnitHandler = Dust.getHandle("Lorand/test01$ThinUnitHandler", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_giskard_me_net___HttpClient = Dust.getHandle("giskard.me/net.1$HttpClient", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_giskard_me_stream___FileStreamSource = Dust.getHandle("giskard.me/stream.1$FileStreamSource", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_giskard_me_stream___SerializeJsonApi = Dust.getHandle("giskard.me/stream.1$SerializeJsonApi", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_giskard_me_stream___StreamCentre = Dust.getHandle("giskard.me/stream.1$StreamCentre", DustHandle.PLACEHOLDER);

// Graph

		// Lorand/test01$DownloadUnit [giskard.me/mind.1$Message]
			Dust.access(DustAccess.Insert, idea_handle_Lorand_test___RemoteUnitLoader, idea_handle_Lorand_test___DownloadUnit, att_handle_giskard_me_mind___listeners, KEY_ADD);

		// Lorand/test01$LocalFiles [giskard.me/mind.1$Agent]
			Dust.access(DustAccess.Set, idea_handle_giskard_me_stream___FileStreamSource, idea_handle_Lorand_test___LocalFiles, att_handle_giskard_me_mind___narrative);

		// Lorand/test01$ProcessUnitStream [giskard.me/mind.1$Message]
			Dust.access(DustAccess.Insert, idea_handle_Lorand_test___StreamUnitProcessor, idea_handle_Lorand_test___ProcessUnitStream, att_handle_giskard_me_mind___listeners, KEY_ADD);

		// Lorand/test01$RemoteUnitLoader [giskard.me/mind.1$Agent]
			Dust.access(DustAccess.Set, idea_handle_giskard_me_net___HttpClient, idea_handle_Lorand_test___RemoteUnitLoader, att_handle_giskard_me_mind___narrative);

		// Lorand/test01$StreamCentre [giskard.me/mind.1$Agent]
			Dust.access(DustAccess.Set, idea_handle_giskard_me_stream___StreamCentre, idea_handle_Lorand_test___StreamCentre, att_handle_giskard_me_mind___narrative);

		// Lorand/test01$StreamUnitProcessor [giskard.me/mind.1$Agent]
			Dust.access(DustAccess.Set, idea_handle_giskard_me_stream___SerializeJsonApi, idea_handle_Lorand_test___StreamUnitProcessor, att_handle_giskard_me_mind___narrative);

		// Lorand/test01$ThinProvideStream [giskard.me/mind.1$Message]
			Dust.access(DustAccess.Insert, idea_handle_Lorand_test___LocalFiles, idea_handle_Lorand_test___ThinProvideStream, att_handle_giskard_me_mind___listeners, KEY_ADD);
			Dust.access(DustAccess.Set, idea_handle_Lorand_test___DownloadUnit, idea_handle_Lorand_test___ThinProvideStream, att_handle_giskard_me_misc___fallback);
			Dust.access(DustAccess.Set, "cache", idea_handle_Lorand_test___ThinProvideStream, att_handle_giskard_me_misc___path);
			Dust.access(DustAccess.Set, "backup", idea_handle_Lorand_test___ThinProvideStream, att_handle_giskard_me_stream___backupFolder);

		// Lorand/test01$ThinUnitHandler [giskard.me/mind.1$Message]
			Dust.access(DustAccess.Insert, idea_handle_Lorand_test___StreamCentre, idea_handle_Lorand_test___ThinUnitHandler, att_handle_giskard_me_mind___listeners, KEY_ADD);
			Dust.access(DustAccess.Set, idea_handle_Lorand_test___ProcessUnitStream, idea_handle_Lorand_test___ThinUnitHandler, att_handle_giskard_me_mind___next);
			Dust.access(DustAccess.Set, idea_handle_Lorand_test___ThinProvideStream, idea_handle_Lorand_test___ThinUnitHandler, att_handle_giskard_me_stream___streamSource);

		// giskard.me/net.1$HttpClient [giskard.me/mind.1$Narrative]
			Dust.access(DustAccess.Set, "me.giskard.dust.core.net.DustNetHttpClientAgent", null, att_handle_giskard_me_dust___ctxApp, att_handle_giskard_me_dust___binaryResolver, idea_handle_giskard_me_net___HttpClient);

		// giskard.me/stream.1$FileStreamSource [giskard.me/mind.1$Narrative]
			Dust.access(DustAccess.Set, "me.giskard.dust.core.stream.DustStreamSrcFileAgent", null, att_handle_giskard_me_dust___ctxApp, att_handle_giskard_me_dust___binaryResolver, idea_handle_giskard_me_stream___FileStreamSource);

		// giskard.me/stream.1$SerializeJsonApi [giskard.me/mind.1$Narrative]
			Dust.access(DustAccess.Set, "me.giskard.dust.core.stream.DustStreamJsonApiSerializerAgent", null, att_handle_giskard_me_dust___ctxApp, att_handle_giskard_me_dust___binaryResolver, idea_handle_giskard_me_stream___SerializeJsonApi);

		// giskard.me/stream.1$StreamCentre [giskard.me/mind.1$Narrative]
			Dust.access(DustAccess.Set, "me.giskard.dust.core.machine.DustMachineIOAgent", null, att_handle_giskard_me_dust___ctxApp, att_handle_giskard_me_dust___binaryResolver, idea_handle_giskard_me_stream___StreamCentre);

// Set unit handler
			DustHandle hApp = Dust.getHandle(appId, DustHandle.PLACEHOLDER);
			Dust.access(DustAccess.Set, hApp, null, att_handle_giskard_me_dust___ctxApp, att_handle_giskard_me_dust___node);
			Dust.access(DustAccess.Set, idea_handle_Lorand_test___ThinUnitHandler, hApp, att_handle_giskard_me_stream___unitHandler);
		} break;
		case "Lorand/test01$TestServer" : {

// Attributes
			DustHandle att_handle_giskard_me_dust___binaryResolver = Dust.getHandle("giskard.me/dust.1$binaryResolver", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_dust___ctxApp = Dust.getHandle("giskard.me/dust.1$ctxApp", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_dust___node = Dust.getHandle("giskard.me/dust.1$node", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_mind___listeners = Dust.getHandle("giskard.me/mind.1$listeners", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_mind___narrative = Dust.getHandle("giskard.me/mind.1$narrative", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_mind___next = Dust.getHandle("giskard.me/mind.1$next", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_misc___fallback = Dust.getHandle("giskard.me/misc.1$fallback", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_misc___path = Dust.getHandle("giskard.me/misc.1$path", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_stream___backupFolder = Dust.getHandle("giskard.me/stream.1$backupFolder", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_stream___streamSource = Dust.getHandle("giskard.me/stream.1$streamSource", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_stream___unitHandler = Dust.getHandle("giskard.me/stream.1$unitHandler", DustHandle.PLACEHOLDER);

// Ideas
			DustHandle idea_handle_Lorand_test___DownloadUnit = Dust.getHandle("Lorand/test01$DownloadUnit", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___LocalFiles = Dust.getHandle("Lorand/test01$LocalFiles", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___ProcessUnitStream = Dust.getHandle("Lorand/test01$ProcessUnitStream", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___ProvideStream = Dust.getHandle("Lorand/test01$ProvideStream", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___RemoteUnitLoader = Dust.getHandle("Lorand/test01$RemoteUnitLoader", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___StreamCentre = Dust.getHandle("Lorand/test01$StreamCentre", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___StreamUnitProcessor = Dust.getHandle("Lorand/test01$StreamUnitProcessor", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___ThinProvideStream = Dust.getHandle("Lorand/test01$ThinProvideStream", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___ThinUnitHandler = Dust.getHandle("Lorand/test01$ThinUnitHandler", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___UnitHandler = Dust.getHandle("Lorand/test01$UnitHandler", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_giskard_me_net___HttpClient = Dust.getHandle("giskard.me/net.1$HttpClient", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_giskard_me_stream___FileStreamSource = Dust.getHandle("giskard.me/stream.1$FileStreamSource", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_giskard_me_stream___SerializeJsonApi = Dust.getHandle("giskard.me/stream.1$SerializeJsonApi", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_giskard_me_stream___StreamCentre = Dust.getHandle("giskard.me/stream.1$StreamCentre", DustHandle.PLACEHOLDER);

// Graph

		// Lorand/test01$DownloadUnit [giskard.me/mind.1$Message]
			Dust.access(DustAccess.Insert, idea_handle_Lorand_test___RemoteUnitLoader, idea_handle_Lorand_test___DownloadUnit, att_handle_giskard_me_mind___listeners, KEY_ADD);

		// Lorand/test01$LocalFiles [giskard.me/mind.1$Agent]
			Dust.access(DustAccess.Set, idea_handle_giskard_me_stream___FileStreamSource, idea_handle_Lorand_test___LocalFiles, att_handle_giskard_me_mind___narrative);

		// Lorand/test01$ProcessUnitStream [giskard.me/mind.1$Message]
			Dust.access(DustAccess.Insert, idea_handle_Lorand_test___StreamUnitProcessor, idea_handle_Lorand_test___ProcessUnitStream, att_handle_giskard_me_mind___listeners, KEY_ADD);

		// Lorand/test01$ProvideStream [giskard.me/mind.1$Message]
			Dust.access(DustAccess.Insert, idea_handle_Lorand_test___LocalFiles, idea_handle_Lorand_test___ProvideStream, att_handle_giskard_me_mind___listeners, KEY_ADD);
			Dust.access(DustAccess.Set, "localStore", idea_handle_Lorand_test___ProvideStream, att_handle_giskard_me_misc___path);
			Dust.access(DustAccess.Set, "backup", idea_handle_Lorand_test___ProvideStream, att_handle_giskard_me_stream___backupFolder);

		// Lorand/test01$RemoteUnitLoader [giskard.me/mind.1$Agent]
			Dust.access(DustAccess.Set, idea_handle_giskard_me_net___HttpClient, idea_handle_Lorand_test___RemoteUnitLoader, att_handle_giskard_me_mind___narrative);

		// Lorand/test01$StreamCentre [giskard.me/mind.1$Agent]
			Dust.access(DustAccess.Set, idea_handle_giskard_me_stream___StreamCentre, idea_handle_Lorand_test___StreamCentre, att_handle_giskard_me_mind___narrative);

		// Lorand/test01$StreamUnitProcessor [giskard.me/mind.1$Agent]
			Dust.access(DustAccess.Set, idea_handle_giskard_me_stream___SerializeJsonApi, idea_handle_Lorand_test___StreamUnitProcessor, att_handle_giskard_me_mind___narrative);

		// Lorand/test01$ThinProvideStream [giskard.me/mind.1$Message]
			Dust.access(DustAccess.Insert, idea_handle_Lorand_test___LocalFiles, idea_handle_Lorand_test___ThinProvideStream, att_handle_giskard_me_mind___listeners, KEY_ADD);
			Dust.access(DustAccess.Set, idea_handle_Lorand_test___DownloadUnit, idea_handle_Lorand_test___ThinProvideStream, att_handle_giskard_me_misc___fallback);
			Dust.access(DustAccess.Set, "cache", idea_handle_Lorand_test___ThinProvideStream, att_handle_giskard_me_misc___path);
			Dust.access(DustAccess.Set, "backup", idea_handle_Lorand_test___ThinProvideStream, att_handle_giskard_me_stream___backupFolder);

		// Lorand/test01$ThinUnitHandler [giskard.me/mind.1$Message]
			Dust.access(DustAccess.Insert, idea_handle_Lorand_test___StreamCentre, idea_handle_Lorand_test___ThinUnitHandler, att_handle_giskard_me_mind___listeners, KEY_ADD);
			Dust.access(DustAccess.Set, idea_handle_Lorand_test___ProcessUnitStream, idea_handle_Lorand_test___ThinUnitHandler, att_handle_giskard_me_mind___next);
			Dust.access(DustAccess.Set, idea_handle_Lorand_test___ThinProvideStream, idea_handle_Lorand_test___ThinUnitHandler, att_handle_giskard_me_stream___streamSource);

		// Lorand/test01$UnitHandler [giskard.me/mind.1$Message]
			Dust.access(DustAccess.Insert, idea_handle_Lorand_test___StreamCentre, idea_handle_Lorand_test___UnitHandler, att_handle_giskard_me_mind___listeners, KEY_ADD);
			Dust.access(DustAccess.Set, idea_handle_Lorand_test___ProcessUnitStream, idea_handle_Lorand_test___UnitHandler, att_handle_giskard_me_mind___next);
			Dust.access(DustAccess.Set, idea_handle_Lorand_test___ProvideStream, idea_handle_Lorand_test___UnitHandler, att_handle_giskard_me_stream___streamSource);

		// giskard.me/net.1$HttpClient [giskard.me/mind.1$Narrative]
			Dust.access(DustAccess.Set, "me.giskard.dust.core.net.DustNetHttpClientAgent", null, att_handle_giskard_me_dust___ctxApp, att_handle_giskard_me_dust___binaryResolver, idea_handle_giskard_me_net___HttpClient);

		// giskard.me/stream.1$FileStreamSource [giskard.me/mind.1$Narrative]
			Dust.access(DustAccess.Set, "me.giskard.dust.core.stream.DustStreamSrcFileAgent", null, att_handle_giskard_me_dust___ctxApp, att_handle_giskard_me_dust___binaryResolver, idea_handle_giskard_me_stream___FileStreamSource);

		// giskard.me/stream.1$SerializeJsonApi [giskard.me/mind.1$Narrative]
			Dust.access(DustAccess.Set, "me.giskard.dust.core.stream.DustStreamJsonApiSerializerAgent", null, att_handle_giskard_me_dust___ctxApp, att_handle_giskard_me_dust___binaryResolver, idea_handle_giskard_me_stream___SerializeJsonApi);

		// giskard.me/stream.1$StreamCentre [giskard.me/mind.1$Narrative]
			Dust.access(DustAccess.Set, "me.giskard.dust.core.machine.DustMachineIOAgent", null, att_handle_giskard_me_dust___ctxApp, att_handle_giskard_me_dust___binaryResolver, idea_handle_giskard_me_stream___StreamCentre);

// Set unit handler
			DustHandle hApp = Dust.getHandle(appId, DustHandle.PLACEHOLDER);
			Dust.access(DustAccess.Set, hApp, null, att_handle_giskard_me_dust___ctxApp, att_handle_giskard_me_dust___node);
			Dust.access(DustAccess.Set, idea_handle_Lorand_test___UnitHandler, hApp, att_handle_giskard_me_stream___unitHandler);
		} break;
		case "Lorand/test01$TestClient" : {

// Attributes
			DustHandle att_handle_giskard_me_dust___binaryResolver = Dust.getHandle("giskard.me/dust.1$binaryResolver", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_dust___ctxApp = Dust.getHandle("giskard.me/dust.1$ctxApp", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_dust___node = Dust.getHandle("giskard.me/dust.1$node", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_mind___listeners = Dust.getHandle("giskard.me/mind.1$listeners", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_mind___narrative = Dust.getHandle("giskard.me/mind.1$narrative", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_mind___next = Dust.getHandle("giskard.me/mind.1$next", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_misc___fallback = Dust.getHandle("giskard.me/misc.1$fallback", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_misc___path = Dust.getHandle("giskard.me/misc.1$path", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_stream___backupFolder = Dust.getHandle("giskard.me/stream.1$backupFolder", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_stream___streamSource = Dust.getHandle("giskard.me/stream.1$streamSource", DustHandle.PLACEHOLDER);
			DustHandle att_handle_giskard_me_stream___unitHandler = Dust.getHandle("giskard.me/stream.1$unitHandler", DustHandle.PLACEHOLDER);

// Ideas
			DustHandle idea_handle_Lorand_test___DownloadUnit = Dust.getHandle("Lorand/test01$DownloadUnit", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___LocalFiles = Dust.getHandle("Lorand/test01$LocalFiles", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___ProcessUnitStream = Dust.getHandle("Lorand/test01$ProcessUnitStream", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___ProvideStream = Dust.getHandle("Lorand/test01$ProvideStream", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___RemoteUnitLoader = Dust.getHandle("Lorand/test01$RemoteUnitLoader", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___StreamCentre = Dust.getHandle("Lorand/test01$StreamCentre", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___StreamUnitProcessor = Dust.getHandle("Lorand/test01$StreamUnitProcessor", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___ThinProvideStream = Dust.getHandle("Lorand/test01$ThinProvideStream", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___ThinUnitHandler = Dust.getHandle("Lorand/test01$ThinUnitHandler", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_Lorand_test___UnitHandler = Dust.getHandle("Lorand/test01$UnitHandler", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_giskard_me_net___HttpClient = Dust.getHandle("giskard.me/net.1$HttpClient", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_giskard_me_stream___FileStreamSource = Dust.getHandle("giskard.me/stream.1$FileStreamSource", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_giskard_me_stream___SerializeJsonApi = Dust.getHandle("giskard.me/stream.1$SerializeJsonApi", DustHandle.PLACEHOLDER);
			DustHandle idea_handle_giskard_me_stream___StreamCentre = Dust.getHandle("giskard.me/stream.1$StreamCentre", DustHandle.PLACEHOLDER);

// Graph

		// Lorand/test01$DownloadUnit [giskard.me/mind.1$Message]
			Dust.access(DustAccess.Insert, idea_handle_Lorand_test___RemoteUnitLoader, idea_handle_Lorand_test___DownloadUnit, att_handle_giskard_me_mind___listeners, KEY_ADD);

		// Lorand/test01$LocalFiles [giskard.me/mind.1$Agent]
			Dust.access(DustAccess.Set, idea_handle_giskard_me_stream___FileStreamSource, idea_handle_Lorand_test___LocalFiles, att_handle_giskard_me_mind___narrative);

		// Lorand/test01$ProcessUnitStream [giskard.me/mind.1$Message]
			Dust.access(DustAccess.Insert, idea_handle_Lorand_test___StreamUnitProcessor, idea_handle_Lorand_test___ProcessUnitStream, att_handle_giskard_me_mind___listeners, KEY_ADD);

		// Lorand/test01$ProvideStream [giskard.me/mind.1$Message]
			Dust.access(DustAccess.Insert, idea_handle_Lorand_test___LocalFiles, idea_handle_Lorand_test___ProvideStream, att_handle_giskard_me_mind___listeners, KEY_ADD);
			Dust.access(DustAccess.Set, "localStore", idea_handle_Lorand_test___ProvideStream, att_handle_giskard_me_misc___path);
			Dust.access(DustAccess.Set, "backup", idea_handle_Lorand_test___ProvideStream, att_handle_giskard_me_stream___backupFolder);

		// Lorand/test01$RemoteUnitLoader [giskard.me/mind.1$Agent]
			Dust.access(DustAccess.Set, idea_handle_giskard_me_net___HttpClient, idea_handle_Lorand_test___RemoteUnitLoader, att_handle_giskard_me_mind___narrative);

		// Lorand/test01$StreamCentre [giskard.me/mind.1$Agent]
			Dust.access(DustAccess.Set, idea_handle_giskard_me_stream___StreamCentre, idea_handle_Lorand_test___StreamCentre, att_handle_giskard_me_mind___narrative);

		// Lorand/test01$StreamUnitProcessor [giskard.me/mind.1$Agent]
			Dust.access(DustAccess.Set, idea_handle_giskard_me_stream___SerializeJsonApi, idea_handle_Lorand_test___StreamUnitProcessor, att_handle_giskard_me_mind___narrative);

		// Lorand/test01$ThinProvideStream [giskard.me/mind.1$Message]
			Dust.access(DustAccess.Insert, idea_handle_Lorand_test___LocalFiles, idea_handle_Lorand_test___ThinProvideStream, att_handle_giskard_me_mind___listeners, KEY_ADD);
			Dust.access(DustAccess.Set, idea_handle_Lorand_test___DownloadUnit, idea_handle_Lorand_test___ThinProvideStream, att_handle_giskard_me_misc___fallback);
			Dust.access(DustAccess.Set, "cache", idea_handle_Lorand_test___ThinProvideStream, att_handle_giskard_me_misc___path);
			Dust.access(DustAccess.Set, "backup", idea_handle_Lorand_test___ThinProvideStream, att_handle_giskard_me_stream___backupFolder);

		// Lorand/test01$ThinUnitHandler [giskard.me/mind.1$Message]
			Dust.access(DustAccess.Insert, idea_handle_Lorand_test___StreamCentre, idea_handle_Lorand_test___ThinUnitHandler, att_handle_giskard_me_mind___listeners, KEY_ADD);
			Dust.access(DustAccess.Set, idea_handle_Lorand_test___ProcessUnitStream, idea_handle_Lorand_test___ThinUnitHandler, att_handle_giskard_me_mind___next);
			Dust.access(DustAccess.Set, idea_handle_Lorand_test___ThinProvideStream, idea_handle_Lorand_test___ThinUnitHandler, att_handle_giskard_me_stream___streamSource);

		// Lorand/test01$UnitHandler [giskard.me/mind.1$Message]
			Dust.access(DustAccess.Insert, idea_handle_Lorand_test___StreamCentre, idea_handle_Lorand_test___UnitHandler, att_handle_giskard_me_mind___listeners, KEY_ADD);
			Dust.access(DustAccess.Set, idea_handle_Lorand_test___ProcessUnitStream, idea_handle_Lorand_test___UnitHandler, att_handle_giskard_me_mind___next);
			Dust.access(DustAccess.Set, idea_handle_Lorand_test___ProvideStream, idea_handle_Lorand_test___UnitHandler, att_handle_giskard_me_stream___streamSource);

		// giskard.me/net.1$HttpClient [giskard.me/mind.1$Narrative]
			Dust.access(DustAccess.Set, "me.giskard.dust.core.net.DustNetHttpClientAgent", null, att_handle_giskard_me_dust___ctxApp, att_handle_giskard_me_dust___binaryResolver, idea_handle_giskard_me_net___HttpClient);

		// giskard.me/stream.1$FileStreamSource [giskard.me/mind.1$Narrative]
			Dust.access(DustAccess.Set, "me.giskard.dust.core.stream.DustStreamSrcFileAgent", null, att_handle_giskard_me_dust___ctxApp, att_handle_giskard_me_dust___binaryResolver, idea_handle_giskard_me_stream___FileStreamSource);

		// giskard.me/stream.1$SerializeJsonApi [giskard.me/mind.1$Narrative]
			Dust.access(DustAccess.Set, "me.giskard.dust.core.stream.DustStreamJsonApiSerializerAgent", null, att_handle_giskard_me_dust___ctxApp, att_handle_giskard_me_dust___binaryResolver, idea_handle_giskard_me_stream___SerializeJsonApi);

		// giskard.me/stream.1$StreamCentre [giskard.me/mind.1$Narrative]
			Dust.access(DustAccess.Set, "me.giskard.dust.core.machine.DustMachineIOAgent", null, att_handle_giskard_me_dust___ctxApp, att_handle_giskard_me_dust___binaryResolver, idea_handle_giskard_me_stream___StreamCentre);

// Set unit handler
			DustHandle hApp = Dust.getHandle(appId, DustHandle.PLACEHOLDER);
			Dust.access(DustAccess.Set, hApp, null, att_handle_giskard_me_dust___ctxApp, att_handle_giskard_me_dust___node);
			Dust.access(DustAccess.Set, idea_handle_Lorand_test___UnitHandler, hApp, att_handle_giskard_me_stream___unitHandler);
		} break;
		default:
			DustException.wrap(null, "Unknown appId parameter", appId);
		break;
		}
	}
}
