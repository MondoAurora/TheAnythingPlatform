package me.giskard.dust.mod.net;

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

	String TOKEN_NET_HOST_PORT = DUST_UNIT_ID + DUST_SEP_TOKEN + "port";

	String TOKEN_NET_SSLINFO_PORT = DUST_UNIT_ID + DUST_SEP_TOKEN + "sslPort";
	String TOKEN_NET_SSLINFO_STOREPATH = DUST_UNIT_ID + DUST_SEP_TOKEN + "sslStorePath";
	String TOKEN_NET_SSLINFO_STOREPASS = DUST_UNIT_ID + DUST_SEP_TOKEN + "sslStorePass";
	String TOKEN_NET_SSLINFO_KEYMANAGERPASS = DUST_UNIT_ID + DUST_SEP_TOKEN + "sslKeyMgrPass";

	String TOKEN_NET_SRVCALL_REQUEST = DUST_UNIT_ID + DUST_SEP_TOKEN + "request";
	String TOKEN_NET_SRVCALL_RESPONSE = DUST_UNIT_ID + DUST_SEP_TOKEN + "response";
	String TOKEN_NET_SRVCALL_METHOD = DUST_UNIT_ID + DUST_SEP_TOKEN + "method";
	String TOKEN_NET_SRVCALL_PATHINFO = DUST_UNIT_ID + DUST_SEP_TOKEN + "pathInfo";
	String TOKEN_NET_SRVCALL_HEADERS = DUST_UNIT_ID + DUST_SEP_TOKEN + "headers";
	String TOKEN_NET_SRVCALL_ATTRIBUTES = DUST_UNIT_ID + DUST_SEP_TOKEN + "attributes";
	String TOKEN_NET_SRVCALL_STATUS = DUST_UNIT_ID + DUST_SEP_TOKEN + "status";

	String TOKEN_NET_SRVRESP_STATUS = DUST_UNIT_ID + DUST_SEP_TOKEN + "respStatus";
	String TOKEN_NET_SRVRESP_TYPE = DUST_UNIT_ID + DUST_SEP_TOKEN + "respType";
	String TOKEN_NET_SRVRESP_HEADER = DUST_UNIT_ID + DUST_SEP_TOKEN + "respHeader";


}
