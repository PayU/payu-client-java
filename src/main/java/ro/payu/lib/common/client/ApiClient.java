package ro.payu.lib.common.client;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import ro.payu.lib.common.authentication.ApiAuthenticationService;
import ro.payu.lib.common.authentication.InvalidSignatureException;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ApiClient {

    private final ApiHttpClient apiHttpClient;
    private final ApiAuthenticationService apiAuthenticationService;
    private final ApiResponseParser apiResponseParser;

    public ApiClient(ApiHttpClient apiHttpClient, ApiAuthenticationService apiAuthenticationService, ApiResponseParser apiResponseParser) {
        this.apiHttpClient = apiHttpClient;
        this.apiAuthenticationService = apiAuthenticationService;
        this.apiResponseParser = apiResponseParser;
    }

    public List<NameValuePair> call(final String endpoint, final List<NameValuePair> requestParameters) throws CommunicationException, InvalidXmlResponseParsingException, InvalidSignatureException {

        final List<NameValuePair> requestParametersWithSignature = apiAuthenticationService.addSignature(requestParameters);

        // create request
        final HttpPost httpRequest = new HttpPost(endpoint);
        httpRequest.setEntity(new UrlEncodedFormEntity(requestParametersWithSignature, StandardCharsets.UTF_8));

        // call http and obtain response
        final HttpResponse httpResponse;
        try {
            httpResponse = apiHttpClient.callHttp(httpRequest);
        } catch (HttpException e) {
            throw new CommunicationException(e);
        }

        final List<NameValuePair> responseParameters = apiResponseParser.parseResponse(httpResponse);

        apiAuthenticationService.verifySignature(responseParameters);

        return responseParameters;
    }
}
