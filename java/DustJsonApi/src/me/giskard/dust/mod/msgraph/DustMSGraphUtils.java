package me.giskard.dust.mod.msgraph;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IConfidentialClientApplication;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;

import me.giskard.dust.core.Dust;
import me.giskard.dust.mod.utils.DustUtilsJson;

class DustMSGraphUtils implements DustMSGraphConsts {

	private static IConfidentialClientApplication app;

	static String getAccessToken(String authority, String clientId, String secret, String scope) throws Exception {
		if (app == null) {
			app = ConfidentialClientApplication.builder(clientId, ClientCredentialFactory.createFromSecret(secret)).authority(authority).build();
		}

		ClientCredentialParameters clientCredentialParam = ClientCredentialParameters.builder(Collections.singleton(scope)).build();

		IAuthenticationResult result = app.acquireToken(clientCredentialParam).get();

		return result.accessToken();
	}

	static InputStream callGraphAPI(String accessToken, String method, String request, Object params) throws Exception {

		URL url = URI.create(request).toURL();
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod(method);
		conn.setRequestProperty(REQPROP_AUTHORIZATION, REQPROP_AUTH_BEARER + accessToken);
		conn.setRequestProperty(REQPROP_ACCEPT, MEDIATYPE_JSON);

		if (null != params) {
			conn.setRequestProperty(REQPROP_CONTENT_TYPE, MEDIATYPE_JSON);
			conn.setDoOutput(true);
			String jsonInputString = DustUtilsJson.toJson(params);
			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = jsonInputString.getBytes(DUST_CHARSET_UTF8);
				os.write(input, 0, input.length);
			}
		}

		int httpResponseCode = conn.getResponseCode();

		if ((httpResponseCode == HTTPResponse.SC_OK) || (httpResponseCode == HTTPResponse.SC_CREATED)) {
			return conn.getInputStream();
		} else {
			Dust.log(TOKEN_LEVEL_WARNING, "Not handled response code", httpResponseCode);
			return null;
		}
	}

}
