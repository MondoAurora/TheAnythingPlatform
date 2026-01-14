package me.giskard.dust.net.httpsrv;

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

import me.giskard.dust.Dust;
import me.giskard.dust.DustConsts.DustAgent;
import me.giskard.dust.DustException;
import me.giskard.dust.net.DustNetConsts;

public class DustHttpServerJetty extends DustAgent implements DustNetConsts // , DustConsts.DustThreadOwner
{
	enum Commands {
		stop, info,
	}

	Server jetty;
	String name;
	HandlerList handlers;
	ServletContextHandler ctxHandler;

	@Override
	protected void init() throws Exception {
		if (null == jetty) {
			jetty = new Server();
			handlers = new HandlerList();

			name = Dust.access(DustAccess.Peek, null, null, TOKEN_NAME);

			long port = Dust.access(DustAccess.Peek, 8080L, null, TOKEN_NET_HOST_PORT);
			HttpConfiguration http = new HttpConfiguration();

			ServerConnector connector = new ServerConnector(jetty);
			connector.addConnectionFactory(new HttpConnectionFactory(http));
			connector.setPort((int) port);
			connector.setName("TEST");

			jetty.addConnector(connector);

			System.out.println("Connector: " + connector);

			Long sslPort = Dust.access(DustAccess.Peek, null, null, TOKEN_NET_SSLINFO_PORT);

			if (null != sslPort) {
				HttpConfiguration https = new HttpConfiguration();
				https.addCustomizer(new SecureRequestCustomizer());

				SslContextFactory sslContextFactory = new SslContextFactory();

				String str;
				str = Dust.access(DustAccess.Peek, null, null, TOKEN_NET_SSLINFO_STOREPATH);
				sslContextFactory.setKeyStorePath(ClassLoader.getSystemResource(str).toExternalForm());
				str = Dust.access(DustAccess.Peek, null, null, TOKEN_NET_SSLINFO_STOREPASS);
				sslContextFactory.setKeyStorePassword(str);
				str = Dust.access(DustAccess.Peek, null, null, TOKEN_NET_SSLINFO_KEYMANAGERPASS);
				sslContextFactory.setKeyManagerPassword(str);

				ServerConnector sslConnector = new ServerConnector(jetty, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
				sslConnector.setPort(sslPort.intValue());

				jetty.addConnector(sslConnector);
			}

			ctxHandler = new ServletContextHandler();
			ctxHandler.setContextPath("/*");
			handlers.addHandler(ctxHandler);

//			Object dispatch = Dust.access(DustAccess.Peek, null, null, TOKEN_MEMBERS);

//			ctxHandler.addServlet(new ServletHolder(new DustHttpServletDispatcher(dispatch)), "/*");
			Object cfg =  Dust.access(DustAccess.Peek, null, null);
			ctxHandler.addServlet(new ServletHolder(new DustHttpServletDispatcher(cfg)), "/*");

			jetty.setHandler(handlers);
			jetty.start();
		}
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		Commands cmd = Commands.info;
		String str = Dust.access(DustAccess.Peek, "", null, TOKEN_TARGET, TOKEN_NET_SRVCALL_PATHINFO);
		String[] path = str.split("/");
		if (0 < path.length) {
			try {
				cmd = Commands.valueOf(path[0]);
			} catch (Exception e) {
//							DustException.swallow(e);
			}
		}

		HttpServletResponse response = Dust.access(DustAccess.Peek, null, null, TOKEN_TARGET, TOKEN_NET_SRVCALL_RESPONSE);
		if (null == response) {
			Dust.log(TOKEN_LEVEL_ERROR, "no response given?");
		}

		String head = "<!doctype html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n<title>" + name + "</title>\n</head>\n<body>\n";

		switch (cmd) {
		case stop:

			if (null != response) {
				response.setContentType(MEDIATYPE_UTF8_HTML);
				PrintWriter out = response.getWriter();

				out.println(head);
				out.println("<h2>Server shutdown initiated.</h2>\n");
				out.println("</body></html>\n");

				out.flush();
			}

			new Thread() {
				@Override
				public void run() {
					try {
						Dust.log(TOKEN_LEVEL_TRACE, "Shutting down Jetty server...");
						release();
						Dust.log(TOKEN_LEVEL_INFO, "Jetty server shutdown OK.");
					} catch (Exception ex) {
						DustException.wrap(ex, "Failed to stop Jetty");
					}
				}
			}.start();
			break;
		case info:
			response = Dust.access(DustAccess.Peek, null, null, TOKEN_TARGET, TOKEN_NET_SRVCALL_RESPONSE);

			if (null != response) {
				Properties pp = System.getProperties();

				StringBuilder sb = new StringBuilder(head);

				sb.append("<h2>Server info</h2>");
				sb.append("<ul>");

				for (Object o : pp.keySet()) {
					String key = o.toString();
					sb.append("<li>" + key + ": " + pp.getProperty(key) + "</li>");
				}

				sb.append("</ul>");

				sb.append("<h2>Commands</h2>");
				sb.append("<ul>");
				for (Commands cc : Commands.values()) {
					sb.append("<li><a href=\"/admin/" + cc + "\">" + cc + "</a></li>");
				}
				sb.append("</ul>");

				sb.append("</body></html>");
				response.setContentType(MEDIATYPE_UTF8_HTML);
				PrintWriter out = response.getWriter();

				out.println(sb.toString());
			}
			break;
		}

		return null;
	}

	@Override
	protected void release() throws Exception {
		if (null != jetty) {
			Server j = jetty;
			jetty = null;
			handlers = null;
			ctxHandler = null;

			j.stop();
		}
	}
}
