package org.apache.eventmesh.trace.skywalking;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.apache.eventmesh.trace.api.EventMeshTraceService;
import org.apache.eventmesh.trace.api.config.ExporterConfiguration;
import org.apache.eventmesh.trace.api.exception.TraceException;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

public class SkywalkingTraceService implements EventMeshTraceService {
    private SdkTracerProvider sdkTracerProvider;
    private OpenTelemetry openTelemetry;
    private Thread shutdownHook;
    private Tracer tracer;
    private TextMapPropagator textMapPropagator;

    @Override
    public void init() throws TraceException {
        OtlpGrpcSpanExporter otlpGrpcSpanExporter =
                OtlpGrpcSpanExporter.builder().setEndpoint("http://159.75.50.238:11800").build();

        int eventMeshTraceExportInterval = ExporterConfiguration.getEventMeshTraceExportInterval();
        int eventMeshTraceExportTimeout = ExporterConfiguration.getEventMeshTraceExportTimeout();
        int eventMeshTraceMaxExportSize = ExporterConfiguration.getEventMeshTraceMaxExportSize();
        int eventMeshTraceMaxQueueSize = ExporterConfiguration.getEventMeshTraceMaxQueueSize();

        SpanProcessor spanProcessor = BatchSpanProcessor.builder(otlpGrpcSpanExporter)
                .setScheduleDelay(eventMeshTraceExportInterval, TimeUnit.SECONDS)
                .setExporterTimeout(eventMeshTraceExportTimeout, TimeUnit.SECONDS)
                .setMaxExportBatchSize(eventMeshTraceMaxExportSize)
                .setMaxQueueSize(eventMeshTraceMaxQueueSize)
                .build();
        Resource serviceNameResource =
                Resource.create(Attributes.of(stringKey("service.name"), "eventmesh-trace"));

        sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(spanProcessor)
                .setResource(Resource.getDefault().merge(serviceNameResource))
                .build();
        openTelemetry = OpenTelemetrySdk.builder()
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .setTracerProvider(sdkTracerProvider)
                .build();
        tracer = openTelemetry.getTracer("eventmesh-trace");
        textMapPropagator = openTelemetry.getPropagators().getTextMapPropagator();

        shutdownHook = new Thread(sdkTracerProvider::close);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    @Override
    public Context extractFrom(Context context, Map<String, Object> carrier) throws TraceException {
        textMapPropagator.extract(context, carrier, new TextMapGetter<Map<String, Object>>() {
            @Override
            public Iterable<String> keys(Map<String, Object> carrier) {
                return carrier.keySet();
            }

            @Override
            public String get(Map<String, Object> carrier, String key) {
                return carrier.get(key).toString();
            }
        });
        return context;
    }

    @Override
    public void inject(Context context, Map<String, Object> carrier) {
        textMapPropagator.inject(context, carrier, new TextMapSetter<Map<String, Object>>() {
            @Override
            public void set(@Nullable Map<String, Object> carrier, String key, String value) {
                carrier.put(key, value);
            }
        });
    }

    @Override
    public Span createSpan(String spanName, SpanKind spanKind, long startTimestamp, TimeUnit timeUnit, Context context, boolean isSpanFinishInOtherThread) throws TraceException {
        return tracer.spanBuilder(spanName)
                .setParent(context)
                .setSpanKind(spanKind)
                .setStartTimestamp(startTimestamp, timeUnit)
                .startSpan();
    }

    @Override
    public Span createSpan(String spanName, SpanKind spanKind, Context context, boolean isSpanFinishInOtherThread) throws TraceException {
        return tracer.spanBuilder(spanName)
                .setParent(context)
                .setSpanKind(spanKind)
                .setStartTimestamp(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .startSpan();
    }

    @Override
    public void shutdown() throws TraceException {
        sdkTracerProvider.close();
    }
}
