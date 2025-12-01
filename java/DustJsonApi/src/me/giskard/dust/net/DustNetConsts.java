package me.giskard.dust.net;

import me.giskard.dust.DustConsts;
import me.giskard.dust.kb.DustKBConsts;

public interface DustNetConsts extends DustConsts, DustKBConsts {
	
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

	String TOKEN_NET_HOST_PORT = "port";

	String TOKEN_NET_SSLINFO_PORT = "sslPort";
	String TOKEN_NET_SSLINFO_STOREPATH = "sslStorePath";
	String TOKEN_NET_SSLINFO_STOREPASS = "sslStorePass";
	String TOKEN_NET_SSLINFO_KEYMANAGERPASS = "sslKeyMgrPass";

	String TOKEN_NET_SRVCALL_REQUEST = "request";
	String TOKEN_NET_SRVCALL_RESPONSE = "response";
	String TOKEN_NET_SRVCALL_METHOD = "method";
	String TOKEN_NET_SRVCALL_PATHINFO = "pathInfo";
	String TOKEN_NET_SRVCALL_HEADERS = "headers";
	String TOKEN_NET_SRVCALL_ATTRIBUTES = "attributes";
	String TOKEN_NET_SRVCALL_STATUS = "status";

	String TOKEN_NET_SRVRESP_STATUS = "respStatus";
	String TOKEN_NET_SRVRESP_TYPE = "respType";
	String TOKEN_NET_SRVRESP_HEADER = "respHeader";


}
