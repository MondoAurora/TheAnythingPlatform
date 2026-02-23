package me.giskard.dust.ldap;

import me.giskard.dust.mind.DustMindConsts;
import me.giskard.dust.stream.DustStreamConsts;

public interface DustLdapNewConsts extends DustStreamConsts, DustMindConsts {
	String CONST_LDAP_DEF_CTX_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
	String CONST_LDAP_READTIMEOUT = "com.sun.jndi.ldap.read.timeout";
	String CONST_LDAP_CONNTIMEOUT = "com.sun.jndi.ldap.connect.timeout";
	String CONST_LDAP_SIMPLE = "simple";
	
	String LDAP_SINGLE_VALUE = "SINGLE-VALUE";
	String LDAP_MAY = "MAY";
	String LDAP_APPEARS = "APPEARS";
	String LDAP_MUST = "MUST";
	String LDAP_OBJECT_CLASSES = "objectclasses";
	String LDAP_ATTRIBUTE_TYPES = "attributetypes";

	String LDAP_DN = "dn";
	String LDAP_CN = "cn";

	String LDAP_CHANGETYPE = "changetype";
	String LDAP_OBJECTCLASS = "objectClass";
	
	String[] DEF_ATTS = new String[] { LDAP_CN };

}
