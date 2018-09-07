package com.tch9.photogallery;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetchr
{
    private static final String TAG = "OLEG";

    // secret - 6d44de493aeed924
    private static final String API_KEY = "534e4d92aa16e1205acc5414068a709c";
    private static final String FETCH_RECENT_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPINT = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();


    private Proxy mProxy;

    public FlickrFetchr()
    {
        mProxy = null;
    }

    public FlickrFetchr(boolean isProxy)
    {
        if (isProxy)
        {
            mProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.5.198.126", 3128));
        }
        else
        {
            mProxy = null;
        }
    }

    public byte[] getUrlBytes(String urlSpec) throws IOException
    {
        URL url = new URL(urlSpec);
        HttpURLConnection connection;
        if (mProxy == null)
        {
            connection = (HttpURLConnection) url.openConnection();
        }
        else
        {
            connection = (HttpURLConnection) url.openConnection(mProxy);
        }
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }
            int bytesRead = 0;
            byte[] bufer = new byte[1024];
            while ((bytesRead = in.read(bufer)) > 0)
            {
                out.write(bufer, 0 , bytesRead);
            }
            out.close();
            return out.toByteArray();
        }
        finally
        {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException
    {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchRecentPhotos()
    {
        String url = buildUrl(FETCH_RECENT_METHOD, null);
        return downloadsGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query)
    {
        String url = buildUrl(SEARCH_METHOD, query);
        return downloadsGalleryItems(url);
    }

    private List<GalleryItem> downloadsGalleryItems(String url)
    {
        List<GalleryItem> items = new ArrayList<>();
        try
        {




            String jsonString = getUrlString(url);
            Log.i(TAG, "Recieved JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        }
        catch (IOException ioe)
        {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        catch (JSONException je)
        {
            Log.e(TAG, "Failed to parse JSON", je);
        }
        return items;
    }

    private String buildUrl(String method, String query)
    {
        Uri.Builder uriBuilder = ENDPINT.buildUpon()
                .appendQueryParameter("method", method);
        if (method.equals(SEARCH_METHOD))
        {
            uriBuilder.appendQueryParameter("text", query);
        }
        return uriBuilder.build().toString();
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException
    {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.optJSONArray("photo");
        for (int i = 0; i < photoJsonArray.length(); i++)
        {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));
            if (!photoJsonObject.has("url_s"))
            {
                continue;
            }
            item.setUrl(photoJsonObject.getString("url_s"));
            //Log.i(TAG, Integer.toString(i) + " - Title: " + item.toString());
            item.setOwner(photoJsonObject.getString("owner"));
            items.add(item);
        }
    }
}
