package com.example.project;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {
    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;
    private final Map<String, String> mHeaders;
    private final Map<String, DataPart> mFileParams;
    private final Map<String, String> mStringParams;

    private final String BOUNDARY = "apiclient-" + System.currentTimeMillis();
    private final String MULTIPART_FORM_DATA = "multipart/form-data; boundary=" + BOUNDARY;

    public VolleyMultipartRequest(int method, String url, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.mFileParams = new HashMap<>();
        this.mStringParams = new HashMap<>();
        this.mHeaders = new HashMap<>();
    }

    public void addFile(String name, byte[] data, String mimeType) {
        mFileParams.put(name, new DataPart(name, data, mimeType));
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
            // Add string parameters
            for (Map.Entry<String, String> entry : mStringParams.entrySet()) {
                buildPart(bos, entry.getKey(), entry.getValue());
            }

            // Add file parameters
            for (Map.Entry<String, DataPart> entry : mFileParams.entrySet()) {
                DataPart dataPart = entry.getValue();
                buildPart(bos, dataPart.getFileName(), dataPart);
            }

            // End of multipart/form-data.
            bos.write(("--" + BOUNDARY + "--\r\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bos.toByteArray();
    }

    private void buildPart(ByteArrayOutputStream dataOutputStream, String name, String value) throws IOException {
        dataOutputStream.write(("--" + BOUNDARY + "\r\n").getBytes());
        dataOutputStream.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n").getBytes());
        dataOutputStream.write(("Content-Type: text/plain; charset=UTF-8\r\n").getBytes());
        dataOutputStream.write(("\r\n").getBytes());
        dataOutputStream.write(value.getBytes("UTF-8"));
        dataOutputStream.write(("\r\n").getBytes());
    }

    private void buildPart(ByteArrayOutputStream dataOutputStream, String name, DataPart dataPart) throws IOException {
        dataOutputStream.write(("--" + BOUNDARY + "\r\n").getBytes());
        dataOutputStream.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + dataPart.getFileName() + "\"\r\n").getBytes());
        dataOutputStream.write(("Content-Type: " + dataPart.getMimeType() + "\r\n").getBytes());
        dataOutputStream.write(("Content-Transfer-Encoding: binary\r\n").getBytes());
        dataOutputStream.write(("\r\n").getBytes());
        dataOutputStream.write(dataPart.getContent());
        dataOutputStream.write(("\r\n").getBytes());
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