package me.giskard.dust.mod.net.httpsrv;

import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustException;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.core.net.DustNetConsts;
import me.giskard.dust.core.utils.DustUtils;

public class DustHttpServerJetty extends DustAgent implements DustNetConsts // , DustConsts.DustThreadOwner
{
//	enum Commands {
//		stop, info,
//	}

	Server jetty;
	String name;
	HandlerList handlers;
	ServletContextHandler ctxHandler;

	@Override
	public void init() throws Exception {
		if (null == jetty) {
			jetty = new Server();
			handlers = new HandlerList();

			name = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_NAME);

			long port = Dust.access(DustAccess.Peek, 8080L, null, HANDLE_NET_ATT_HOST_PORT);
			HttpConfiguration http = new HttpConfiguration();

			ServerConnector connector = new ServerConnector(jetty);
			connector.addConnectionFactory(new HttpConnectionFactory(http));
			connector.setPort((int) port);
			connector.setName("TEST");

			jetty.addConnector(connector);

			System.out.println("Connector: " + connector);

			Long sslPort = Dust.access(DustAccess.Peek, null, null, HANDLE_NET_ATT_SSLINFO_PORT);

			if (null != sslPort) {
				HttpConfiguration https = new HttpConfiguration();
				https.addCustomizer(new SecureRequestCustomizer());

				SslContextFactory sslContextFactory = new SslContextFactory();

				String str;
				str = Dust.access(DustAccess.Peek, null, null, HANDLE_NET_ATT_SSLINFO_STOREPATH);
				sslContextFactory.setKeyStorePath(ClassLoader.getSystemResource(str).toExternalForm());
				str = Dust.access(DustAccess.Peek, null, null, HANDLE_NET_ATT_SSLINFO_STOREPASS);
				sslContextFactory.setKeyStorePassword(str);
				str = Dust.access(DustAccess.Peek, null, null, HANDLE_NET_ATT_SSLINFO_KEYMANAGERPASS);
				sslContextFactory.setKeyManagerPassword(str);

				ServerConnector sslConnector = new ServerConnector(jetty, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
				sslConnector.setPort(sslPort.intValue());

				jetty.addConnector(sslConnector);
			}

			ctxHandler = new ServletContextHandler();
			ctxHandler.setContextPath("/*");
			handlers.addHandler(ctxHandler);
			
			ctxHandler.addServlet(new ServletHolder(new DustHttpServletDispatcher(jetty)), "/*");

			jetty.setHandler(handlers);
			jetty.start();
		}
	}

	@Override
	public void process() throws Exception {
//		Commands cmd = Commands.info;
		DustHandle hCmd = Dust.access(DustAccess.Peek, null, null, HANDLE_DUST_ATT_CTX_MSG, HANDLE_MIND_ATT_CMD);
		
		if ( null == hCmd ) {
			String pathInfo = Dust.access(DustAccess.Peek, "", null, HANDLE_NET_ATT_SRVCALL_PATHINFO);
			switch ( pathInfo ) {
			case "new/info":
				hCmd = HANDLE_MISC_TAG_CMD_INFO;
				break;
			case "new/stop":
				hCmd = HANDLE_MISC_TAG_CMD_STOP;
				break;
			}
			Dust.log(HANDLE_MISC_TAG_LEVEL_TRACE, "Path info", pathInfo);
		}

		HttpServletResponse response = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_TARGET, HANDLE_NET_ATT_SRVCALL_RESPONSE);
		if (null == response) {
			Dust.log(HANDLE_MISC_TAG_LEVEL_ERROR, "no response given");
			return;
		}

		response.setContentType(MEDIATYPE_UTF8_HTML);
		PrintWriter out = response.getWriter();
		
		String title = DustUtils.isEmpty(name) ? hCmd.getId() : name;

		String head = "<!doctype html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n<title>" + title + "</title>\n</head>\n<body>\n";

		if ( HANDLE_MISC_TAG_CMD_STOP == hCmd ) {
				out.println(head);
				out.println("<h2>Server shutdown initiated.</h2>\n");
				out.println("</body></html>\n");

				out.flush();

			new Thread() {
				@Override
				public void run() {
					try {
						Dust.log(HANDLE_MISC_TAG_LEVEL_TRACE, "Shutting down Jetty server...");
						release();
						Dust.log(HANDLE_MISC_TAG_LEVEL_INFO, "Jetty server shutdown OK.");
					} catch (Exception ex) {
						DustException.wrap(ex, "Failed to stop Jetty");
					}
				}
			}.start();
		} else if ( HANDLE_MISC_TAG_CMD_INFO == hCmd ) {
				Properties pp = System.getProperties();

				StringBuilder sb = new StringBuilder(head);

				sb.append("<h2>Server info " + DustUtils.strTime() + " </h2>");
				sb.append("<ul>");

				for (Object o : pp.keySet()) {
					String key = o.toString();
					sb.append("<li>" + key + ": " + pp.getProperty(key) + "</li>");
				}

				sb.append("</ul>");

//				sb.append("<h2>Commands</h2>");
//				sb.append("<ul>");
//				for (Commands cc : Commands.values()) {
//					sb.append("<li><a href=\"/admin/" + cc + "\">" + cc + "</a></li>");
//				}
//				sb.append("</ul>");

				sb.append("</body></html>");
//				response.setContentType(MEDIATYPE_UTF8_HTML);

				out.println(sb.toString());
		}
	}

	@Override
	public void release() throws Exception {
		if (null != jetty) {
			Server j = jetty;
			jetty = null;
			handlers = null;
			ctxHandler = null;

			j.stop();
		}
	}
}
