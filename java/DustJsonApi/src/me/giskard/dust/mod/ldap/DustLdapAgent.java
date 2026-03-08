package me.giskard.dust.mod.ldap;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.stream.DustStreamUtils;
import me.giskard.dust.mod.net.DustNetUtils;

public class DustLdapAgent extends DustAgent implements DustLdapNewConsts {

	@Override
	protected Object process(DustAccess access) throws Exception {

		DustNetUtils.sslHack();

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

			String filter = Dust.access(DustAccess.Peek, null, null, TOKEN_FILTER);

			SearchControls searchControls = new SearchControls();

			Collection<String> m = Dust.access(DustAccess.Peek, null, null, TOKEN_MEMBERS);
			if (null != m) {
				String[] attrIDs = new String[m.size()];
				m.toArray(attrIDs);
				searchControls.setReturningAttributes(attrIDs);
			}
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			Long count = Dust.access(DustAccess.Peek, null, null, TOKEN_COUNT);
			if (null != count) {
				searchControls.setCountLimit(count);
			}

			if ("self".equals(filter)) {
				String cn = user.split(",")[0];
				filter = "(" + cn + ")";
			}

			String root = Dust.access(DustAccess.Peek, "", null, TOKEN_ROOT);
			NamingEnumeration<SearchResult> searchResults = ldapCtx.search(root, filter, searchControls);

			String distinguishedName = null;

			if (searchResults.hasMore()) {

				String fileName = Dust.access(DustAccess.Peek, null, null, TOKEN_PATH);
				OutputStream os = null;
				PrintWriter pw = null;
				int lc = 0;

				try {
					while (searchResults.hasMore()) {
						if (0 == (++lc % 100)) {
							Dust.log(TOKEN_LEVEL_TRACE, "record", lc);
						}

						SearchResult result = searchResults.next();
						Attributes attrs = result.getAttributes();

						distinguishedName = result.getNameInNamespace();
						
						if (null == pw) {
							os = DustStreamUtils.getStream(TOKEN_CMD_SAVE, fileName);
							pw = new PrintWriter(os);
						} else {
							pw.println();
						}
						
						pw.print("dn: ");
						pw.println(distinguishedName);
						
						Attribute att;
						for (NamingEnumeration<? extends Attribute> ae = attrs.getAll(); ae.hasMore();) {
							att = ae.next();
							String attId = att.getID();
							for (NamingEnumeration<?> ve = att.getAll(); ve.hasMore();) {
								Object val = ve.next();
								pw.print(attId + ": ");
								pw.println(val);
							}
						}
						
						pw.flush();
					}
				} finally {
					if ( null != pw ) {
						pw.flush();
						os.flush();
						
						pw.close();
						os.close();
					}
					
					Dust.log(TOKEN_LEVEL_INFO, "Loaded count", lc);
				}
			}
		} finally {
			if (null != ldapCtx) {
				ldapCtx.close();
			}
		}

		return null;
	}

}
