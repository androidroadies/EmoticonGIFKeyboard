package com.kevalpatel2106.gifpack.tenor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kevalpatel2106.emoticongifkeyboard.gifs.Gif;
import com.kevalpatel2106.emoticongifkeyboard.gifs.GifProviderProtocol;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Keval on 19-Aug-17.
 * GIF providor to load GIFs using Tenor apis.
 *
 * @author <a href='https://github.com/kevalpatel2106'>Kevalpatel2106</a>
 * @see GifProviderProtocol
 */

public final class TenorGifProvider implements GifProviderProtocol {

    /* Giphy api key */
    private final String mApiKey;

    /* Instance */
    private final Context mContext;

    /**
     * Private constructor.
     *
     * @param context Instance.
     * @param apiKey  Giphy api key.
     */
    @SuppressWarnings("ConstantConditions")
    private TenorGifProvider(@NonNull final Context context,
                             @NonNull final String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) throw new RuntimeException("Invalid GIPHY key.");
        mContext = context;
        mApiKey = apiKey;
    }

    /**
     * Create {@link TenorGifProvider}.
     *
     * @param context Instance.
     * @param apiKey  Giphy api key.
     * @return {@link TenorGifProvider}
     * @see <a href='https://developers.giphy.com/dashboard/'>Create Giphy Account</a>
     */
    public static TenorGifProvider create(@NonNull final Context context,
                                          @NonNull final String apiKey) {
        return new TenorGifProvider(context, apiKey);
    }

    /**
     * Get the string from the input stream.
     *
     * @param inputStream {@link InputStream} to read.
     * @return String.
     * @throws IOException - If unable to read
     */
    @Nullable
    private static String getStringFromStream(@NonNull final InputStream inputStream) throws IOException {
        //noinspection ConstantConditions
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) total.append(line).append('\n');
        return total.toString();
    }

    /**
     * Load the trending GIFs list.
     *
     * @param limit Number of GIFs.
     * @return List of all the {@link Gif} or null of the error occurs.
     */
    @Nullable
    @Override
    public List<Gif> getTrendingGifs(final int limit) {
        try {
            Response<ResponseBody> responseBody = new Retrofit.Builder()
                    .baseUrl(TenorApiService.GIPHY_BASE_URL)
                    .client(new OkHttpClient.Builder()
                            .addNetworkInterceptor(new CacheInterceptor(mContext, 15))
                            .cache(CacheInterceptor.getCache(mContext))
                            .build())
                    .build()
                    .create(TenorApiService.class)
                    .getTrending(mApiKey, limit)
                    .execute();


            //Check if the response okay?
            if (responseBody.code() == 200 && responseBody.body() != null) {
                //noinspection ConstantConditions
                String response = getStringFromStream(responseBody.body().byteStream());
                if (response == null) return null;

                JSONArray data = new JSONObject(response).getJSONArray("results");
                ArrayList<Gif> gifs = new ArrayList<>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject medias = data.getJSONObject(i).getJSONArray("media").getJSONObject(0);
                    Gif gif = new Gif(medias.getJSONObject("gif").getString("url"),
                            medias.has("nanogif") ? medias.getJSONObject("nanogif").getString("url") : null,
                            medias.has("mp4") ? medias.getJSONObject("mp4").getString("url") : null);
                    gifs.add(gif);
                }
                return gifs;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Search GIFs.
     *
     * @param limit Number of GIFs.
     * @param query Search query string.
     * @return List of all the {@link Gif} or null of the error occurs.
     */
    @Nullable
    @Override
    public List<Gif> searchGifs(final int limit, @NonNull final String query) {
        try {
            Response<ResponseBody> responseBody = new Retrofit.Builder()
                    .baseUrl(TenorApiService.GIPHY_BASE_URL)
                    .client(new OkHttpClient.Builder()
                            .addNetworkInterceptor(new CacheInterceptor(mContext, 4))
                            .cache(CacheInterceptor.getCache(mContext))
                            .build())
                    .build()
                    .create(TenorApiService.class)
                    .searchGif(mApiKey, query, limit)
                    .execute();


            //Check if the response okay?
            if (responseBody.code() == 200 && responseBody.body() != null) {
                //noinspection ConstantConditions
                String response = getStringFromStream(responseBody.body().byteStream());
                if (response == null) return null;

                JSONArray data = new JSONObject(response).getJSONArray("results");
                ArrayList<Gif> gifs = new ArrayList<>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject medias = data.getJSONObject(i).getJSONArray("media").getJSONObject(0);
                    Gif gif = new Gif(medias.getJSONObject("gif").getString("url"),
                            medias.has("nanogif") ? medias.getJSONObject("nanogif").getString("url") : null,
                            medias.has("mp4") ? medias.getJSONObject("mp4").getString("url") : null);
                    gifs.add(gif);
                }
                return gifs;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
