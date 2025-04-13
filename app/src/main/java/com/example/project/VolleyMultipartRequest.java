// VolleyMultipartRequest.java
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
    }

    // Updated addFile method to accept filename
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
            OutputStream os = bos;
            // Add string parameters
            for (Map.Entry<String, String> entry : mStringParams.entrySet()) {
                buildPart(os, entry.getKey(), entry.getValue());
            }

            // Add file parameters
            for (Map.Entry<String, DataPart> entry : mFileParams.entrySet()) {
                DataPart dataPart = entry.getValue();
                buildPart(os, entry.getKey(), dataPart); // Use entry.getKey() as name
            }

            // End of multipart/form-data.
            os.write((LINE_FEED + "--" + BOUNDARY + "--" + LINE_FEED).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bos.toByteArray();
    }

    private void buildPart(OutputStream dataOutputStream, String name, String value) throws IOException {
        dataOutputStream.write(("--" + BOUNDARY + LINE_FEED).getBytes());
        dataOutputStream.write(("Content-Disposition: form-data; name=\"" + name + "\"" + LINE_FEED).getBytes());
        dataOutputStream.write(("Content-Type: text/plain; charset=UTF-8" + LINE_FEED).getBytes());
        dataOutputStream.write((LINE_FEED).getBytes());
        dataOutputStream.write(value.getBytes("UTF-8"));
        dataOutputStream.write((LINE_FEED).getBytes());
    }

    // Updated buildPart method to use DataPart's filename
    private void buildPart(OutputStream dataOutputStream, String name, DataPart dataPart) throws IOException {
        dataOutputStream.write(("--" + BOUNDARY + LINE_FEED).getBytes());
        dataOutputStream.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + dataPart.getFileName() + "\"" + LINE_FEED).getBytes());
        dataOutputStream.write(("Content-Type: " + dataPart.getMimeType() + LINE_FEED).getBytes());
        dataOutputStream.write(("Content-Transfer-Encoding: binary" + LINE_FEED).getBytes());
        dataOutputStream.write((LINE_FEED).getBytes());
        dataOutputStream.write(dataPart.getContent());
        dataOutputStream.write((LINE_FEED).getBytes());
    }


    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }

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