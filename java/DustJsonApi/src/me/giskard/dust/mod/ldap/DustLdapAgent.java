package me.giskard.dust.mod.ldap;

import java.util.Collection;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.net.ssl.SSLContext;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.mod.net.DustNetUtils;

public class DustLdapAgent extends DustAgent implements DustLdapNewConsts {
	
	static {
		System.setProperty("jdk.tls.client.enableSessionTicketExtension", "false");
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		
    SSLContext ctx = SSLContext.getDefault();
    for (String s : ctx.getSupportedSSLParameters().getCipherSuites()) {
      if (s.contains("AES_256_CBC_SHA"))
        System.out.println(s);
    }

		DustNetUtils.doTrustToCertificates();
		
		Hashtable<String, String> environment = new Hashtable<String, String>();

		DustHandle accInfo = Dust.access(DustAccess.Peek, null, null, TOKEN_ACCESS);

		String url = Dust.access(DustAccess.Peek, null, accInfo, TOKEN_STREAM_URL);
		String user = Dust.access(DustAccess.Peek, null, accInfo, TOKEN_USER);
		String pass = Dust.access(DustAccess.Peek, null, accInfo, TOKEN_PASSWORD);
		String auth = Dust.access(DustAccess.Peek, CONST_LDAP_SIMPLE, accInfo, TOKEN_AUTH);

		url = Dust.access(DustAccess.Peek, url, null, TOKEN_STREAM_URL);
		user = Dust.access(DustAccess.Peek, user, null, TOKEN_USER);
		pass = Dust.access(DustAccess.Peek, pass, null, TOKEN_PASSWORD);

		environment.put(Context.INITIAL_CONTEXT_FACTORY, CONST_LDAP_DEF_CTX_FACTORY);
//		environment.put(Context.SECURITY_PROTOCOL, "ssl");
		environment.put(Context.PROVIDER_URL, url);
		environment.put(Context.SECURITY_AUTHENTICATION, auth);
		environment.put(Context.SECURITY_PRINCIPAL, user);
		environment.put(Context.SECURITY_CREDENTIALS, pass);
		environment.put(CONST_LDAP_READTIMEOUT, "10000");
		environment.put(CONST_LDAP_CONNTIMEOUT, "10000");

		DirContext ldapCtx = null;

		try {
			ldapCtx = new InitialDirContext(environment);

			String filter = Dust.access(DustAccess.Peek, "", null, TOKEN_FILTER);

			Collection<String> m = Dust.access(DustAccess.Peek, null, null, TOKEN_MEMBERS);
			SearchControls searchControls = new SearchControls();
			if (null != m) {
				String[] attrIDs = new String[m.size()];
				m.toArray(attrIDs);
				searchControls.setReturningAttributes(attrIDs);
			}
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			searchControls.setCountLimit(100);
			
			String cn = user.split(",")[0];
			
			filter = "(" + cn + ")";

			String root = Dust.access(DustAccess.Peek, "", null, TOKEN_ROOT);
			NamingEnumeration<SearchResult> searchResults = ldapCtx.search(root, filter, searchControls);

			String distinguishedName = null;

			while (searchResults.hasMore()) {
				SearchResult result = (SearchResult) searchResults.next();
				Attributes attrs = result.getAttributes();

				distinguishedName = result.getNameInNamespace();

				Dust.log(TOKEN_LEVEL_INFO, "Found user", distinguishedName, attrs);
			}
		} finally {
			if (null != ldapCtx) {
				ldapCtx.close();
			}
		}
		
		return null;
	}

}
