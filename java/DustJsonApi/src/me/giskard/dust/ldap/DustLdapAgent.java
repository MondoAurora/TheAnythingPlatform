package me.giskard.dust.ldap;

import java.util.Collection;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import me.giskard.dust.Dust;
import me.giskard.dust.DustConsts.DustAgent;

public class DustLdapAgent extends DustAgent implements DustLDAPConsts {

	@Override
	protected Object process(DustAccess access) throws Exception {
		Hashtable<String, String> environment = new Hashtable<String, String>();
		
		DustObject accInfo =  Dust.access(DustAccess.Peek, null, null, TOKEN_ACCESS);

		String url = Dust.access(DustAccess.Peek, null, accInfo, TOKEN_STREAM_URL);
		String user = Dust.access(DustAccess.Peek, null, accInfo, TOKEN_USER);
		String pass = Dust.access(DustAccess.Peek, null, accInfo, TOKEN_PASSWORD);
		String auth = Dust.access(DustAccess.Peek, CONST_LDAP_SIMPLE, accInfo, TOKEN_AUTH);

		environment.put(Context.INITIAL_CONTEXT_FACTORY, CONST_LDAP_DEF_CTX_FACTORY);
		environment.put(Context.PROVIDER_URL, url);
		environment.put(Context.SECURITY_AUTHENTICATION, auth);
		environment.put(Context.SECURITY_PRINCIPAL, user);
		environment.put(Context.SECURITY_CREDENTIALS, pass);
		environment.put(CONST_LDAP_TIMEOUT, "10000");

		DirContext ldapCtx = null;

		try {
			ldapCtx = new InitialDirContext(environment);

			String filter = Dust.access(DustAccess.Peek, "", null, TOKEN_FILTER);

			Collection<String> m = Dust.access(DustAccess.Peek, "", null, TOKEN_MEMBERS);
			String[] attrIDs;
			if (null == m) {
				attrIDs = DEF_ATTS;
			} else {
				attrIDs = new String[m.size()];
				m.toArray(attrIDs);
			}
			SearchControls searchControls = new SearchControls();
			searchControls.setReturningAttributes(attrIDs);
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			NamingEnumeration<SearchResult> searchResults = ldapCtx.search((String) Dust.access(DustAccess.Peek, "", null, TOKEN_ROOT), filter,
					searchControls);

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
