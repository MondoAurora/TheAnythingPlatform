package me.giskard.dust.ldap;

import me.giskard.dust.kb.DustKBConsts;
import me.giskard.dust.stream.DustStreamConsts;

public interface DustLDAPConsts extends DustStreamConsts, DustKBConsts {
	String CONST_LDAP_DEF_CTX_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
	String CONST_LDAP_SIMPLE = "simple";
	
	String TOKEN_LDAP_SINGLE_VALUE = "SINGLE-VALUE";
	String TOKEN_LDAP_MAY = "MAY";
	String TOKEN_LDAP_APPEARS = "APPEARS";
	String TOKEN_LDAP_MUST = "MUST";
	String TOKEN_LDAP_OBJECT_CLASSES = "objectclasses";
	String TOKEN_LDAP_ATTRIBUTE_TYPES = "attributetypes";

	String TOKEN_LDAP_DN = "dn";
	String TOKEN_LDAP_CN = "cn";

	String TOKEN_LDAP_CHANGETYPE = "changetype";
	String TOKEN_LDAP_OBJECTCLASS = "objectClass";
	
	String[] DEF_ATTS = new String[] { TOKEN_LDAP_CN };

}
