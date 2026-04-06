package com.stark.kmpcontact.android.contacts.data.remote;

import android.util.Log;
import com.google.gson.Gson;
import com.stark.kmpcontact.data.network.HttpMethod;
import com.stark.kmpcontact.data.network.NetworkException;
import com.stark.kmpcontact.data.network.NetworkRequestExecutor;
import kotlinx.coroutines.Dispatchers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import kotlin.reflect.KClass;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0003\n\u0002\b\t\b\u0007\u0018\u0000 #2\u00020\u0001:\u0001#B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006JT\u0010\u0007\u001a\u0002H\b\"\b\b\u0000\u0010\b*\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u0002H\b0\u000f2\u0012\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u000b0\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u000bH\u0096@\u00a2\u0006\u0002\u0010\u0013JA\u0010\u0014\u001a\u00020\u00152\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u000bH\u0002\u00a2\u0006\u0002\u0010\u001dJ6\u0010\u001e\u001a\u00020\u00152\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\n\u001a\u00020\u000b2\u0012\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u000b0\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u000bH\u0002J9\u0010\u001f\u001a\u00020\u00152\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u00192\b\u0010\u001c\u001a\u0004\u0018\u00010\u000bH\u0002\u00a2\u0006\u0002\u0010 J\u0018\u0010!\u001a\u00020\u000b*\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u000b0\u0011H\u0002J\u000e\u0010\"\u001a\u00020\u000b*\u0004\u0018\u00010\u000bH\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006$"}, d2 = {"Lcom/stark/kmpcontact/android/contacts/data/remote/OkHttpNetworkRequestExecutor;", "Lcom/stark/kmpcontact/data/network/NetworkRequestExecutor;", "okHttpClient", "Lokhttp3/OkHttpClient;", "gson", "Lcom/google/gson/Gson;", "(Lokhttp3/OkHttpClient;Lcom/google/gson/Gson;)V", "execute", "T", "", "url", "", "method", "Lcom/stark/kmpcontact/data/network/HttpMethod;", "responseClass", "Lkotlin/reflect/KClass;", "headers", "", "requestJsonBody", "(Ljava/lang/String;Lcom/stark/kmpcontact/data/network/HttpMethod;Lkotlin/reflect/KClass;Ljava/util/Map;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "logRequestFailure", "", "elapsedMs", "", "responseCode", "", "throwable", "", "responsePreview", "(Lcom/stark/kmpcontact/data/network/HttpMethod;Ljava/lang/String;JLjava/lang/Integer;Ljava/lang/Throwable;Ljava/lang/String;)V", "logRequestStart", "logRequestSuccess", "(Lcom/stark/kmpcontact/data/network/HttpMethod;Ljava/lang/String;JLjava/lang/Integer;Ljava/lang/String;)V", "formatForLog", "previewForLog", "Companion", "contacts_debug"})
public final class OkHttpNetworkRequestExecutor implements com.stark.kmpcontact.data.network.NetworkRequestExecutor {
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient okHttpClient = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.gson.Gson gson = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String LOG_TAG = "NetworkExecutor";
    private static final int LOG_PREVIEW_LIMIT = 300;
    private static final int HEADER_VALUE_PREVIEW_LIMIT = 100;
    @org.jetbrains.annotations.NotNull()
    private static final okhttp3.MediaType JSON_MEDIA_TYPE = null;
    @org.jetbrains.annotations.NotNull()
    private static final okhttp3.RequestBody EMPTY_REQUEST_BODY = null;
    @org.jetbrains.annotations.NotNull()
    private static final com.stark.kmpcontact.android.contacts.data.remote.OkHttpNetworkRequestExecutor.Companion Companion = null;
    
    public OkHttpNetworkRequestExecutor(@org.jetbrains.annotations.NotNull()
    okhttp3.OkHttpClient okHttpClient, @org.jetbrains.annotations.NotNull()
    com.google.gson.Gson gson) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public <T extends java.lang.Object>java.lang.Object execute(@org.jetbrains.annotations.NotNull()
    java.lang.String url, @org.jetbrains.annotations.NotNull()
    com.stark.kmpcontact.data.network.HttpMethod method, @org.jetbrains.annotations.NotNull()
    kotlin.reflect.KClass<T> responseClass, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> headers, @org.jetbrains.annotations.Nullable()
    java.lang.String requestJsonBody, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super T> $completion) {
        return null;
    }
    
    private final void logRequestStart(com.stark.kmpcontact.data.network.HttpMethod method, java.lang.String url, java.util.Map<java.lang.String, java.lang.String> headers, java.lang.String requestJsonBody) {
    }
    
    private final void logRequestSuccess(com.stark.kmpcontact.data.network.HttpMethod method, java.lang.String url, long elapsedMs, java.lang.Integer responseCode, java.lang.String responsePreview) {
    }
    
    private final void logRequestFailure(com.stark.kmpcontact.data.network.HttpMethod method, java.lang.String url, long elapsedMs, java.lang.Integer responseCode, java.lang.Throwable throwable, java.lang.String responsePreview) {
    }
    
    private final java.lang.String previewForLog(java.lang.String $this$previewForLog) {
        return null;
    }
    
    private final java.lang.String formatForLog(java.util.Map<java.lang.String, java.lang.String> $this$formatForLog) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\b\u0082\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R\u000e\u0010\u0007\u001a\u00020\bX\u0082T\u00a2\u0006\u0002\n\u0000R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u000e\u0010\r\u001a\u00020\bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/stark/kmpcontact/android/contacts/data/remote/OkHttpNetworkRequestExecutor$Companion;", "", "()V", "EMPTY_REQUEST_BODY", "Lokhttp3/RequestBody;", "getEMPTY_REQUEST_BODY", "()Lokhttp3/RequestBody;", "HEADER_VALUE_PREVIEW_LIMIT", "", "JSON_MEDIA_TYPE", "Lokhttp3/MediaType;", "getJSON_MEDIA_TYPE", "()Lokhttp3/MediaType;", "LOG_PREVIEW_LIMIT", "LOG_TAG", "", "contacts_debug"})
    static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final okhttp3.MediaType getJSON_MEDIA_TYPE() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final okhttp3.RequestBody getEMPTY_REQUEST_BODY() {
            return null;
        }
    }
}