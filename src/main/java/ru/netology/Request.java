package ru.netology;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;


public class Request {
    private String method;
    private String uri;
    private List<NameValuePair> queryParams;
    private String version;
    private Map<String, String> params = new HashMap<>();
    public byte[] body;

    private boolean badRequest;
    private boolean emptyRequest;

    private String startingLine;
    private String headers;

    Request(BufferedReader inBuffer) throws IOException{
        var header = readHttpMessage(inBuffer);
        if (header.isEmpty()) {emptyRequest = true; return;}
        method = getMethodFromHttpMessage(header);
        if (method.isEmpty()){
            badRequest = true;
        }
        String uriWithParams = "";
        var listUri = getUriFromHttpMessage(header);
        uri = listUri.get(0);
        if(uri.isEmpty()) {
            badRequest = true;
        }
        queryParams = URLEncodedUtils.parse(listUri.get(1), Charset.defaultCharset());
        version = getVersionFromHttpMessage(header);
        if(version.isEmpty()) {
            badRequest = true;
        }
        badRequest = !readParamsToMap();
    }

    private String readHttpMessage(BufferedReader reader) throws IOException {
        StringBuilder builderHttpMessage = new StringBuilder();
        StringBuilder builderHeaders = new StringBuilder();
        String ln = null;
        while (true) {
            try {
                ln = reader.readLine();
            } catch (IOException ex) {
                break;
            }
            if (ln == null) {
                break;
            }

            if (ln.isEmpty()){
                builderHttpMessage.append(System.getProperty("line.separator"));
                break;
            }

            if (builderHttpMessage.isEmpty()) {
                startingLine = ln;
            }
            else {
                builderHeaders.append(ln + System.getProperty("line.separator"));
            }
            builderHttpMessage.append(ln + System.getProperty("line.separator"));
        }

        headers = builderHeaders.toString();
        //TODO чтение тела сообщения (через массв байтов)

        return builderHttpMessage.toString();
    }

    private String getMethodFromHttpMessage(String header) {
        int from = 0;
        int to = header.indexOf(" ", from);
        String method = header.substring(from, to);

        return method;
    }

    private List<String> getUriFromHttpMessage(String header) {
        int from = header.indexOf(" ") + 1;
        int to = header.indexOf(" ", from);
        String uri = header.substring(from, to);
        int paramIndex = uri.indexOf("?");
        String uriWithParams = "";
        if (paramIndex != -1) {
            uriWithParams = uri;
            uriWithParams = uriWithParams.substring(paramIndex + 1);
            uri = uri.substring(0, paramIndex);
        }
        return List.of(uri, uriWithParams);
    }

    private String getVersionFromHttpMessage(String header) {
        int to = header.indexOf(System.getProperty("line.separator"));
        int from = to - 3;
        String version = from > 0 ? header.substring(from, to) : "";

        return version;
    }

    private boolean readParamsToMap(){
        String[] strParams = headers.split(System.getProperty("line.separator"));
        for(String strParam : strParams){
            int separatorInd = strParam.indexOf(":");
            var paramName = strParam.substring(0, separatorInd).trim();
            var paramValue = strParam.substring(separatorInd + 1, strParam.length()).trim();
            if(paramName.length() == 0) {
                badRequest = true;
            }
            params.put(paramName, paramValue);
        }
        return true;
    }

    public String getMethod(){
        return method;
    }

    public String getUri(){
        return uri;
    }

    public String getVersion(){
        return version;
    }

    public Map<String, String> getParams(){
        return params;
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public List<NameValuePair> getQueryParam(String param) {
        List<NameValuePair> returnParams = null;
        if(queryParams == null) {return returnParams;}
        for(NameValuePair valuePair : queryParams){
            if(valuePair.getName().equals(param)){
                returnParams.add(valuePair);
            }
        }
        return returnParams;
    }

    public boolean isBadRequest(){
        return badRequest;
    }

    public boolean isEmptyRequest(){
        return emptyRequest;
    }
}
