package com.example.project;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;



public class VolleyMultipartRequest extends Request<NetworkResponse> {
    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;
    private final Map<String, String> mHeaders;
    private final Map<String, DataPart> mFileParams;
    private final Map<String, String> mStringParams;
    private final String BOUNDARY = "apiclient-" + System.currentTimeMillis();
    private final String LINE_FEED = "\r\n";
    private final String MULTIPART_FORM_DATA = "multipart/form-data; boundary=" + BOUNDARY;

    public VolleyMultipartRequest(int method, String url, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.mFileParams = new HashMap<>();
        this.mStringParams = new HashMap<>();
        this.mHeaders = new HashMap<>();
        setShouldCache(false);
    }

    public void addFile(String name, String filename, byte[] data, String mimeType) {
        mFileParams.put(name, new DataPart(filename, data, mimeType));
    }

    public void addStringParam(String name, String value) {
        mStringParams.put(name, value);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders;
    }

    @Override
    public String getBodyContentType() {
        return MULTIPART_FORM_DATA;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            for (Map.Entry<String, String> entry : mStringParams.entrySet()) {
                buildTextPart(bos, entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, DataPart> entry : mFileParams.entrySet()) {
                buildFilePart(bos, entry.getValue(), entry.getKey());
            }
            bos.write(("--" + BOUNDARY + "--" + LINE_FEED).getBytes());

        } catch (IOException e) {
            throw new AuthFailureError("Failed to build multipart body", e);
        }
        return bos.toByteArray();
    }

    private void buildTextPart(OutputStream outputStream, String parameterName, String parameterValue) throws IOException {
        outputStream.write(("--" + BOUNDARY + LINE_FEED).getBytes());
        outputStream.write(("Content-Disposition: form-data; name=\"" + parameterName + "\"" + LINE_FEED).getBytes());
        outputStream.write(("Content-Type: text/plain; charset=UTF-8" + LINE_FEED).getBytes()); // Explicitly set charset
        outputStream.write((LINE_FEED).getBytes());
        outputStream.write(parameterValue.getBytes("UTF-8"));
        outputStream.write((LINE_FEED).getBytes());
    }

    private void buildFilePart(OutputStream outputStream, DataPart dataFile, String parameterName) throws IOException {
        outputStream.write(("--" + BOUNDARY + LINE_FEED).getBytes());
        outputStream.write(("Content-Disposition: form-data; name=\"" + parameterName + "\"; filename=\"" + dataFile.getFileName() + "\"" + LINE_FEED).getBytes());
        outputStream.write(("Content-Type: " + dataFile.getMimeType() + LINE_FEED).getBytes());
        outputStream.write(("Content-Transfer-Encoding: binary" + LINE_FEED).getBytes());
        outputStream.write((LINE_FEED).getBytes());
        outputStream.write(dataFile.getContent());
        outputStream.write((LINE_FEED).getBytes());
    }

    // --- Standard Volley Response Handling ---

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        // This is correct for parsing NetworkResponse
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        // Deliver the successful response
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        // Deliver the error response
        mErrorListener.onErrorResponse(error);
    }

    // --- DataPart Static Inner Class ---
    // This inner class seems correct based on your previous code
    public static class DataPart {
        private String fileName;
        private byte[] content;
        private String mimeType;

        public DataPart(String fileName, byte[] content, String mimeType) {
            this.fileName = fileName;
            this.content = content;
            this.mimeType = mimeType;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getMimeType() {
            return mimeType;
        }
    }
}