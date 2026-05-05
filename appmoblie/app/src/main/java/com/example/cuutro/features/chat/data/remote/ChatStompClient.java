package com.example.cuutro.features.chat.data.remote;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cuutro.core.network.BackendConfig;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class ChatStompClient {

    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private static final String STOMP_CONNECT_COMMAND = "CONNECT";
    private static final String STOMP_DISCONNECT_COMMAND = "DISCONNECT";
    private static final String STOMP_SUBSCRIBE_COMMAND = "SUBSCRIBE";
    private static final String STOMP_CONNECTED_FRAME = "CONNECTED";
    private static final String STOMP_MESSAGE_FRAME = "MESSAGE";
    private static final String STOMP_ERROR_FRAME = "ERROR";

    public interface Listener {
        void onConnected();

        void onDisconnected();

        void onMessageBody(@NonNull String body);

        void onError(@NonNull String message);
    }

    private final OkHttpClient webSocketClient;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Object lock = new Object();
    private final StringBuilder incomingFrameBuffer = new StringBuilder();

    @Nullable
    private WebSocket webSocket;
    @Nullable
    private Listener listener;
    @Nullable
    private String authorizationHeader;
    @Nullable
    private String hostHeader;
    @Nullable
    private String subscribeDestination;
    @Nullable
    private String subscribeId;

    private boolean closedByClient;
    private boolean subscribed;

    public ChatStompClient() {
        webSocketClient = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    public void connect(long reportId, @NonNull String authorizationHeader, @NonNull Listener listener) {
        synchronized (lock) {
            disconnectLocked(false);
            String wsUrl = buildWebSocketUrl();
            if (isBlank(wsUrl)) {
                postError(listener, "Không tạo được URL WebSocket");
                return;
            }

            this.listener = listener;
            this.authorizationHeader = authorizationHeader.trim();
            this.closedByClient = false;
            this.subscribed = false;
            this.subscribeDestination = "/topic/phieu-cuu-tro/" + reportId + "/tin-nhan";
            this.subscribeId = "chat-sub-" + reportId;
            this.hostHeader = resolveHostHeader(wsUrl);
            incomingFrameBuffer.setLength(0);

            Request request = new Request.Builder()
                    .url(wsUrl)
                    .build();
            this.webSocket = webSocketClient.newWebSocket(request, new InternalWebSocketListener());
        }
    }

    public void disconnect() {
        synchronized (lock) {
            disconnectLocked(true);
        }
    }

    private void disconnectLocked(boolean notifyDisconnected) {
        Listener previousListener = listener;
        closedByClient = true;
        sendDisconnectFrameLocked();
        if (webSocket != null) {
            webSocket.close(NORMAL_CLOSURE_STATUS, "client_close");
            webSocket = null;
        }
        incomingFrameBuffer.setLength(0);
        subscribed = false;
        authorizationHeader = null;
        hostHeader = null;
        subscribeDestination = null;
        subscribeId = null;
        listener = null;
        if (notifyDisconnected && previousListener != null) {
            postDisconnected(previousListener);
        }
    }

    private void sendConnectFrameLocked() {
        if (webSocket == null || isBlank(authorizationHeader)) {
            return;
        }

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("accept-version", "1.2,1.1");
        headers.put("host", isBlank(hostHeader) ? "localhost" : hostHeader);
        headers.put("heart-beat", "10000,10000");
        headers.put("Authorization", authorizationHeader);
        sendFrameLocked(STOMP_CONNECT_COMMAND, headers, "");
    }

    private void sendSubscribeFrameLocked() {
        if (webSocket == null || isBlank(subscribeDestination) || isBlank(subscribeId)) {
            return;
        }
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("id", subscribeId);
        headers.put("destination", subscribeDestination);
        headers.put("ack", "auto");
        sendFrameLocked(STOMP_SUBSCRIBE_COMMAND, headers, "");
    }

    private void sendDisconnectFrameLocked() {
        if (webSocket == null) {
            return;
        }
        sendFrameLocked(STOMP_DISCONNECT_COMMAND, null, "");
    }

    private void sendFrameLocked(
            @NonNull String command,
            @Nullable Map<String, String> headers,
            @Nullable String body
    ) {
        if (webSocket == null) {
            return;
        }

        StringBuilder frameBuilder = new StringBuilder();
        frameBuilder.append(command).append('\n');
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry == null || isBlank(entry.getKey())) {
                    continue;
                }
                frameBuilder.append(entry.getKey()).append(':');
                frameBuilder.append(entry.getValue() == null ? "" : entry.getValue());
                frameBuilder.append('\n');
            }
        }
        frameBuilder.append('\n');
        if (body != null) {
            frameBuilder.append(body);
        }
        frameBuilder.append('\u0000');
        webSocket.send(frameBuilder.toString());
    }

    private void handleIncomingText(@NonNull String text) {
        synchronized (lock) {
            incomingFrameBuffer.append(text);
            int delimiterIndex;
            while ((delimiterIndex = incomingFrameBuffer.indexOf("\u0000")) >= 0) {
                String rawFrame = incomingFrameBuffer.substring(0, delimiterIndex);
                incomingFrameBuffer.delete(0, delimiterIndex + 1);
                consumeRawFrameLocked(rawFrame);
            }
        }
    }

    private void consumeRawFrameLocked(@Nullable String rawFrame) {
        if (rawFrame == null) {
            return;
        }
        String normalizedFrame = trimLeadingLineFeeds(rawFrame).replace("\r\n", "\n");
        if (normalizedFrame.trim().isEmpty()) {
            return;
        }

        StompFrame frame = parseFrame(normalizedFrame);
        if (frame == null || isBlank(frame.command)) {
            return;
        }

        String command = frame.command.trim();
        if (STOMP_CONNECTED_FRAME.equals(command)) {
            if (!subscribed) {
                sendSubscribeFrameLocked();
                subscribed = true;
            }
            postConnected();
            return;
        }

        if (STOMP_MESSAGE_FRAME.equals(command)) {
            if (!isBlank(frame.body)) {
                postMessageBody(frame.body.trim());
            }
            return;
        }

        if (STOMP_ERROR_FRAME.equals(command)) {
            String error = firstNonBlank(frame.headers.get("message"), frame.body, "Kết nối realtime bị lỗi");
            postError(error);
            return;
        }
    }

    @Nullable
    private StompFrame parseFrame(@NonNull String rawFrame) {
        int commandBreak = rawFrame.indexOf('\n');
        if (commandBreak <= 0) {
            return null;
        }
        String command = rawFrame.substring(0, commandBreak).trim();
        int headerBodySeparator = rawFrame.indexOf("\n\n", commandBreak + 1);
        String headerText;
        String bodyText;
        if (headerBodySeparator >= 0) {
            headerText = rawFrame.substring(commandBreak + 1, headerBodySeparator);
            bodyText = rawFrame.substring(headerBodySeparator + 2);
        } else {
            headerText = rawFrame.substring(commandBreak + 1);
            bodyText = "";
        }

        Map<String, String> headers = new LinkedHashMap<>();
        if (!headerText.isEmpty()) {
            String[] lines = headerText.split("\n");
            for (String line : lines) {
                if (line == null || line.isEmpty()) {
                    continue;
                }
                int colonIndex = line.indexOf(':');
                if (colonIndex <= 0) {
                    continue;
                }
                String key = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                if (!key.isEmpty()) {
                    headers.put(key, value);
                }
            }
        }

        return new StompFrame(command, headers, bodyText);
    }

    @Nullable
    private String buildWebSocketUrl() {
        String baseUrl = BackendConfig.getBaseUrl();
        if (isBlank(baseUrl)) {
            return null;
        }

        URI baseUri;
        try {
            baseUri = URI.create(baseUrl);
        } catch (Exception ignored) {
            return null;
        }

        String scheme = baseUri.getScheme();
        if (isBlank(scheme)) {
            return null;
        }
        String wsScheme = "https".equalsIgnoreCase(scheme) ? "wss" : "ws";

        String host = baseUri.getHost();
        if (isBlank(host)) {
            return null;
        }

        int port = baseUri.getPort();
        String path = baseUri.getPath();
        String normalizedPath = normalizeBasePath(path);

        StringBuilder result = new StringBuilder();
        result.append(wsScheme).append("://").append(host);
        if (port > 0) {
            result.append(':').append(port);
        }
        if (!normalizedPath.isEmpty()) {
            result.append(normalizedPath);
        }
        result.append("/ws");
        return result.toString();
    }

    @Nullable
    private String resolveHostHeader(@NonNull String wsUrl) {
        try {
            URI uri = URI.create(wsUrl);
            String host = uri.getHost();
            if (isBlank(host)) {
                return null;
            }
            int port = uri.getPort();
            if (port <= 0) {
                return host;
            }
            return host + ":" + port;
        } catch (Exception ignored) {
            return null;
        }
    }

    @NonNull
    private String normalizeBasePath(@Nullable String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty() || "/".equals(rawPath.trim())) {
            return "";
        }
        String normalized = rawPath.trim();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized;
    }

    @NonNull
    private String trimLeadingLineFeeds(@NonNull String value) {
        int index = 0;
        while (index < value.length() && (value.charAt(index) == '\n' || value.charAt(index) == '\r')) {
            index++;
        }
        return index == 0 ? value : value.substring(index);
    }

    @NonNull
    private String firstNonBlank(@Nullable String first, @Nullable String second, @NonNull String fallback) {
        if (!isBlank(first)) {
            return first.trim();
        }
        if (!isBlank(second)) {
            return second.trim();
        }
        return fallback;
    }

    private boolean isBlank(@Nullable String value) {
        return value == null || value.trim().isEmpty();
    }

    private void postConnected() {
        Listener currentListener = listener;
        if (currentListener == null) {
            return;
        }
        mainHandler.post(currentListener::onConnected);
    }

    private void postDisconnected() {
        Listener currentListener = listener;
        if (currentListener == null) {
            return;
        }
        postDisconnected(currentListener);
    }

    private void postDisconnected(@NonNull Listener listener) {
        mainHandler.post(listener::onDisconnected);
    }

    private void postMessageBody(@NonNull String body) {
        Listener currentListener = listener;
        if (currentListener == null) {
            return;
        }
        mainHandler.post(() -> currentListener.onMessageBody(body));
    }

    private void postError(@NonNull String message) {
        Listener currentListener = listener;
        if (currentListener == null) {
            return;
        }
        postError(currentListener, message);
    }

    private void postError(@NonNull Listener listener, @NonNull String message) {
        mainHandler.post(() -> listener.onError(message));
    }

    private final class InternalWebSocketListener extends WebSocketListener {

        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            synchronized (lock) {
                if (closedByClient || ChatStompClient.this.webSocket != webSocket) {
                    return;
                }
                sendConnectFrameLocked();
            }
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            handleIncomingText(text);
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
            handleIncomingText(bytes.utf8());
        }

        @Override
        public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, reason);
        }

        @Override
        public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            synchronized (lock) {
                if (ChatStompClient.this.webSocket == webSocket) {
                    ChatStompClient.this.webSocket = null;
                }
                incomingFrameBuffer.setLength(0);
                subscribed = false;
                if (closedByClient) {
                    return;
                }
            }
            postDisconnected();
        }

        @Override
        public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
            synchronized (lock) {
                if (ChatStompClient.this.webSocket == webSocket) {
                    ChatStompClient.this.webSocket = null;
                }
                incomingFrameBuffer.setLength(0);
                subscribed = false;
                if (closedByClient) {
                    return;
                }
            }
            String message = t.getMessage();
            if (isBlank(message)) {
                message = "Mất kết nối realtime";
            }
            postError(message);
            postDisconnected();
        }
    }

    private static final class StompFrame {

        @NonNull
        private final String command;
        @NonNull
        private final Map<String, String> headers;
        @NonNull
        private final String body;

        private StompFrame(
                @NonNull String command,
                @NonNull Map<String, String> headers,
                @NonNull String body
        ) {
            this.command = command;
            this.headers = headers;
            this.body = body;
        }
    }
}
