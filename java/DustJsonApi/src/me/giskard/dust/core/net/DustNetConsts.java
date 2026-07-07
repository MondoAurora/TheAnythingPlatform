package me.giskard.dust.core.net;

import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.mind.DustMindConsts;

public interface DustNetConsts extends DustConsts, DustMindConsts {
	
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

	String TOKEN_NET_KNOWN_HOST = UNIT_DUST + DUST_SEP_TOKEN + "knownHost";

	String TOKEN_NET_HOST_PORT = UNIT_DUST + DUST_SEP_TOKEN + "port";

	String TOKEN_NET_SSLINFO_PORT = UNIT_DUST + DUST_SEP_TOKEN + "sslPort";
	String TOKEN_NET_SSLINFO_STOREPATH = UNIT_DUST + DUST_SEP_TOKEN + "sslStorePath";
	String TOKEN_NET_SSLINFO_STOREPASS = UNIT_DUST + DUST_SEP_TOKEN + "sslStorePass";
	String TOKEN_NET_SSLINFO_KEYMANAGERPASS = UNIT_DUST + DUST_SEP_TOKEN + "sslKeyMgrPass";

	String TOKEN_NET_SRVCALL_REQUEST = UNIT_DUST + DUST_SEP_TOKEN + "request";
	String TOKEN_NET_SRVCALL_RESPONSE = UNIT_DUST + DUST_SEP_TOKEN + "response";
	String TOKEN_NET_SRVCALL_METHOD = UNIT_DUST + DUST_SEP_TOKEN + "method";
	String TOKEN_NET_SRVCALL_PATHINFO = UNIT_DUST + DUST_SEP_TOKEN + "pathInfo";
	String TOKEN_NET_SRVCALL_HEADERS = UNIT_DUST + DUST_SEP_TOKEN + "headers";
	String TOKEN_NET_SRVCALL_ATTRIBUTES = UNIT_DUST + DUST_SEP_TOKEN + "attributes";
	String TOKEN_NET_SRVCALL_STATUS = UNIT_DUST + DUST_SEP_TOKEN + "status";

	String TOKEN_NET_SRVRESP_STATUS = UNIT_DUST + DUST_SEP_TOKEN + "respStatus";
	String TOKEN_NET_SRVRESP_TYPE = UNIT_DUST + DUST_SEP_TOKEN + "respType";
	String TOKEN_NET_SRVRESP_HEADER = UNIT_DUST + DUST_SEP_TOKEN + "respHeader";


}
