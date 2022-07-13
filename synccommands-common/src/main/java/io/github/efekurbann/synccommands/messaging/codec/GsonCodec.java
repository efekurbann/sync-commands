/*

    Big credit to lucko's helper

  https://github.com/lucko/helper

 */
package io.github.efekurbann.synccommands.messaging.codec;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class GsonCodec<T> {

    private final Gson gson;
    private final TypeToken<T> type;

    public GsonCodec(Gson gson, TypeToken<T> type) {
        this.gson = gson;
        this.type = type;
    }

    public byte[] encode(T message) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (Writer writer = new OutputStreamWriter(byteOut, StandardCharsets.UTF_8)) {
            this.gson.toJson(message, this.type.getType(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteOut.toByteArray();
    }

    public T decode(byte[] buf) {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(buf);
        try (Reader reader = new InputStreamReader(byteIn, StandardCharsets.UTF_8)) {
            return this.gson.fromJson(reader, this.type.getType());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
