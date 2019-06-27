package cluster.sharding;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.event.LoggingAdapter;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.*;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.TextMessage;
import akka.http.javadsl.model.ws.UpgradeToWebSocket;
import akka.http.javadsl.model.ws.WebSocket;
import akka.http.javadsl.server.AllDirectives;
import akka.japi.JavaPartialFunction;
import akka.stream.ActorMaterializer;
import akka.stream.OverflowStrategy;
import akka.stream.QueueOfferResult;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.SourceQueueWithComplete;
import org.reactivestreams.Subscriber;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HttpServerTest extends AllDirectives {
    private final ActorSystem actorSystem;
    private final String actorSystemNode;
    private final ActorMaterializer actorMaterializer;
    private final Source<Message, SourceQueueWithComplete<Message>> queue;
    private final SourceQueueWithComplete<Message> queueOffer;
    private final LoggingAdapter log;
    private final Random random = new Random();

    public HttpServerTest() {
        actorSystem = ActorSystem.create();
        actorSystemNode = Cluster.get(actorSystem).selfMember().address().toString();
        actorMaterializer = ActorMaterializer.create(actorSystem);
        log = actorSystem.log();
        queue = Source.queue(10240, OverflowStrategy.dropHead());
        queueOffer = queue.to(Sink.foreach(m -> log.info("to client '{}'", m.asTextMessage().getStrictText()))).run(actorMaterializer);
    }

//    public static void main(String[] args) {
//        new HttpServerTest().run();
//    }

    private void run() {
        int serverPort = 8080;

        CompletionStage<ServerBinding> serverBindingCompletionStage = Http.get(actorSystem)
                .bindAndHandleSync(this::handleHttpRequest, ConnectHttp.toHost("localhost", serverPort), actorMaterializer);

        try {
            serverBindingCompletionStage.toCompletableFuture().get(15, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            log.error(e, "Monitor HTTP server error");
        } finally {
            log.info("Monitor HTTP server started on port {}", serverPort);
        }
    }

    private HttpResponse handleHttpRequest(HttpRequest httpRequest) {
        switch (httpRequest.getUri().path()) {
            case "/monitor":
                return getWebPage();
            case "/test":
                return makeTestOffer();
            case "/events":
                return webSocketHandler2(httpRequest);
            default:
                return HttpResponse.create().withStatus(404);
        }
    }

    private HttpResponse getWebPage() {
        return HttpResponse.create()
                .withEntity(ContentTypes.TEXT_HTML_UTF8, monitorWebPage())
                .withStatus(StatusCodes.ACCEPTED);
    }

    private HttpResponse makeTestOffer() {
        makeOffer(generateRandomEventMessage());
        return HttpResponse.create().withStatus(200);
    }

    private HttpResponse webSocketHandler1(HttpRequest httpRequest) {
        Optional<HttpHeader> upgradeToWebSocket = httpRequest.getHeader("UpgradeToWebSocket");
        if (upgradeToWebSocket.isPresent()) {
            return webSocketHandler(upgradeToWebSocket.get());
        } else {
            return HttpResponse.create().withStatus(400);
        }
    }

    private HttpResponse webSocketHandler(HttpHeader upgradeToWebSocketHeader) {
//        Sink<TextMessage, Source<TextMessage, NotUsed>> sourceSink = BroadcastHub.of(TextMessage.class, 256);
//
//        Sink<Message, CompletionStage<Done>> consumer = Sink.foreach(System.out::println);
//        MergeHub.of(Message.class, 16).to(consumer);
//
//        Sink<Message, CompletionStage<Done>> sink = Sink.ignore();
//        Source.fromPublisher(this::publisher);
//        Source<Message, Message> source;
//        Flow<Message, Message, NotUsed> flow = Flow.<Message>create().fromSinkAndSource(sink, source);


        UpgradeToWebSocket upgradeToWebSocket = (UpgradeToWebSocket) upgradeToWebSocketHeader;

        return upgradeToWebSocket.handleMessagesWith(
                Sink.foreach(m -> log.info("from client '{}'", m.asTextMessage().getStrictText())),
                queue);
    }

    private <O> void publisher(Subscriber<? super O> subscriber) {
        //
    }

    private HttpResponse webSocketHandler2(HttpRequest httpRequest) {
        Flow<Message, Message, NotUsed> flow = Flow.<Message>create()
                .collect(new JavaPartialFunction<Message, Message>() {
                    @Override
                    public Message apply(Message message, boolean isCheck) {
                        if (isCheck && message.isText()) {
                            return null;
                        } else if (isCheck && !message.isText()) {
                            throw noMatch();
                        } else if (message.asTextMessage().isStrict()) {
                            return TextMessage.create(generateRandomEventMessage());
                        } else {
                            return TextMessage.create("");
                        }
                    }
                });

        return WebSocket.handleWebSocketRequestWith(httpRequest, flow);
    }

    private void makeOffer(String messageText) {
        queueOffer.offer(TextMessage.create(messageText))
                .whenComplete((r, e) -> {
                    if (r.equals(QueueOfferResult.enqueued())) {
                        log.info("offered '{}'", messageText);
                    } else {
                        log.warning("WebSocket message error", e);
                    }
                });
    }

    private String generateRandomEventMessage() {
        int id = random.nextInt(100) + 1;
        int shard = id % 10;
        return String.format("%s, %d, %d", actorSystemNode, shard, id);
    }

    private static String monitorWebPage() {
        StringBuilder html = new StringBuilder();
        line(html, "<!DOCTYPE html>");
        line(html, "<html>");
        line(html, "  <head>");
        line(html, "    <script src=\"https://d3js.org/d3.v4.min.js\"></script>\n");
        line(html, "    <script>");
        line(html, "      var webSocket = new WebSocket('ws://localhost:8080/events');");
        line(html, "");
        line(html, "      webSocket.onopen = function(event) {");
        line(html, "        webSocket.send('request')");
        line(html, "        console.log('WebSocket connected', event)");
        line(html, "      }");
        line(html, "");
        line(html, "      webSocket.onmessage = function(event) {");
        line(html, "        console.log(event);");
        //line(html, "        webSocket.send('request')");
        line(html, "      }");
        line(html, "");
        line(html, "      webSocket.onerror = function(error) {");
        line(html, "        console.error('WebSocket error', error);");
        line(html, "      }");
        line(html, "");
        line(html, "      webSocket.onclose = function(event) {");
        line(html, "        console.log('WebSocket close', event);");
        line(html, "      }");
        line(html, "");
        line(html, "      var canvas = document.querySelector(\"canvas\"),");
        line(html, "          context = canvas.getContext(\"2d\"),");
        line(html, "          width = canvas.width,");
        line(html, "          height = canvas.height;");
        line(html, "");
        line(html, "    </script>");
        line(html, "  </head>");
        line(html, "  <body>");
        line(html, "    <h3>Hello, World!</h3>");
        line(html, "    <canvas width=\"960\" height=\"960\"></canvas>");
        line(html, "  </body>");
        line(html, "</html>");
        line(html, "");

        return html.toString();
    }

    private static void line(StringBuilder html, String line) {
        html.append(String.format("%s%n", line));
    }
}
