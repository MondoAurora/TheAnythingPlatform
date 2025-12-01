package me.giskard.dust.ldap;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import me.giskard.dust.Dust;
import me.giskard.dust.DustConsts;
import me.giskard.dust.kb.DustKBUtils;

public class DustLdapAgent extends DustConsts.DustAgentBase implements DustLDAPConsts {

	@Override
	protected Object process(Map<String, Object> cfg, Object params) throws Exception {
		Hashtable<String, String> environment = new Hashtable<String, String>();

		String url = (String) cfg.get(TOKEN_STREAM_URL);
		String auth = DustKBUtils.access(KBAccess.Peek, CONST_LDAP_SIMPLE, params, TOKEN_AUTH);

		String user = (String) cfg.get(TOKEN_USER);
		String pass = (String) cfg.get(TOKEN_PASSWORD);

		environment.put(Context.INITIAL_CONTEXT_FACTORY, CONST_LDAP_DEF_CTX_FACTORY);
		environment.put(Context.PROVIDER_URL, url);
		environment.put(Context.SECURITY_AUTHENTICATION, auth);
		environment.put(Context.SECURITY_PRINCIPAL, user);
		environment.put(Context.SECURITY_CREDENTIALS, pass);

		DirContext ldapCtx = null;

		try {
			ldapCtx = new InitialDirContext(environment);

			String filter = DustKBUtils.access(KBAccess.Peek, "", params, TOKEN_FILTER);

			Collection<String> m = DustKBUtils.access(KBAccess.Peek, null, params, TOKEN_MEMBERS);
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

			NamingEnumeration<SearchResult> searchResults = ldapCtx.search((String) DustKBUtils.access(KBAccess.Peek, "", params, TOKEN_ROOT), filter,
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
