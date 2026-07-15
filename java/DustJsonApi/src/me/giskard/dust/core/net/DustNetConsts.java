package me.giskard.dust.core.net;

import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.mind.DustMindConsts;
import me.giskard.tokens.DustGenTokens_net_1;

public interface DustNetConsts extends DustConsts, DustMindConsts, DustGenTokens_net_1 {
	
	int NO_PORT_SET = -1;

	String CHARSET_POSTFIX_UTF8 = "; charset=UTF-8";

	String MEDIATYPE_RAW = "application/octet-stream";
	
	String MEDIATYPE_UTF8_TEXT = "text/plain" + CHARSET_POSTFIX_UTF8;
	String MEDIATYPE_UTF8_HTML = "text/html" + CHARSET_POSTFIX_UTF8;
	String MEDIATYPE_UTF8_CSV = "text/csv" + CHARSET_POSTFIX_UTF8;
	String MEDIATYPE_UTF8_XML = "text/xml" + CHARSET_POSTFIX_UTF8;

	String MEDIATYPE_JSON = "application/json";
	String MEDIATYPE_JSONAPI = "application/vnd.api+json";
	String MEDIATYPE_ZIP = "application/zip";

	String REQPROP_AUTHORIZATION = "Authorization";
	String REQPROP_AUTH_BEARER = "Bearer ";
	String REQPROP_ACCEPT = "Accept";
	String REQPROP_CONTENT_TYPE = "Content-Type";

	String REQPROP_TAP_CLIENT = "Tap-Client";
	String REQPROP_TAP_VERSION = "Tap-Version";
	String REQPROP_TAP_CONTEXT = "Tap-Context";
	
}
