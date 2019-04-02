package sample;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.env.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;

public class MockWebServerPropertySource extends PropertySource<MockWebServer> implements
		DisposableBean {

	private static final MockResponse JWKS_RESPONSE = response(
			"{\n" +
					"  \"access_token\":\"test-token\",\n" +
					"  \"token_type\":\"Bearer\",\n" +
					"  \"expires_in\":86400\n" +
					"}\n",
			200
	);


	/**
	 * Name of the random {@link PropertySource}.
	 */
	public static final String MOCK_WEB_SERVER_PROPERTY_SOURCE_NAME = "mockwebserver";

	private static final String NAME = "mockwebserver.url";

	private static final Log logger = LogFactory.getLog(MockWebServerPropertySource.class);

	private boolean started;

	public MockWebServerPropertySource() {
		super(MOCK_WEB_SERVER_PROPERTY_SOURCE_NAME, new MockWebServer());
	}

	@Override
	public Object getProperty(String name) {
		if (!name.equals(NAME)) {
			return null;
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Looking up the url for '" + name + "'");
		}
		return getUrl();
	}

	@Override
	public void destroy() throws Exception {
		getSource().shutdown();
	}

	/**
	 * Get's the URL (i.e. "http://localhost:123456")
	 * @return
	 */
	private String getUrl() {
		MockWebServer mockWebServer = getSource();
		if (!this.started) {
			intializeMockWebServer(mockWebServer);
		}
		String url = mockWebServer.url("").url().toExternalForm();
		return url.substring(0, url.length() - 1);
	}

	private void intializeMockWebServer(MockWebServer mockWebServer) {
		Dispatcher dispatcher = new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
				if ("/oauth/token".equals(request.getPath())) {
					return JWKS_RESPONSE;
				}else{
					return response("", 404);
				}

			}
		};

		mockWebServer.setDispatcher(dispatcher);
		try {
			mockWebServer.start();
			this.started = true;
		} catch (IOException e) {
			throw new RuntimeException("Could not start " + mockWebServer, e);
		}
	}

	private static MockResponse response(String body, int status) {
		return new MockResponse()
				.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.setResponseCode(status)
				.setBody(body);
	}

}