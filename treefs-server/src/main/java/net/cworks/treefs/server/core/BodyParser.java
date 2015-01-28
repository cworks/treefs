package net.cworks.treefs.server.core;

import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerFileUpload;
import org.vertx.java.core.json.DecodeException;

import java.util.HashMap;
import java.util.Map;

import static net.cworks.json.Json.Json;

/**
 * Parse Json (application/json) and Form bodies (application/x-www-form-urlencoded,
 * multipart/form-data) into the body property of the request.
 *
 * When the request is a multipath/form-data then uploaded files are mapped into the
 * files Map(String, HttpServerFileUpload) map.
 *
 */
public class BodyParser extends HttpService {

    /**
     * File System location to save uploaded files.
     */
    private final String uploadDir;

    /**
     * Create this BodyParser with the configured upload Directory
     * @param uploadDir location to save uploaded files
     */
    public BodyParser(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    /**
     * Create a BodyParser with a default upload Directory of java.io.tmpdir
     */
    public BodyParser() {
        this(System.getProperty("java.io.tmpdir"));
    }

    /**
     * Verify there is a body according to the headers in the request, if Content-Type is
     * application/json then parse Json Body if
     * application/x-www-form-urlencoded then parse into a Buffer if
     * multipart/form-data then parse the multipart file upload
     *
     * @param request
     * @param next
     */
    @Override
    public void handle(final HttpServiceRequest request, final Handler<Object> next) {
        final String method = request.method();

        // GET and HEAD have no setBody
        if ("GET".equals(method) || "HEAD".equals(method)) {
            next.handle(null);
        } else {

            // has no body
            MultiMap headers = request.headers();
            if (!headers.contains("transfer-encoding") && !headers.contains("content-length")) {
                next.handle(null);
                return;
            }

            final String contentType = request.getHeader("content-type");

            final boolean isJSON = contentType != null && contentType.contains("application/json");
            final boolean isMULTIPART = contentType != null && contentType.contains("multipart/form-data");
            final boolean isURLENCODEC = contentType != null && contentType.contains("application/x-www-form-urlencoded");
            final Buffer buffer = (!isMULTIPART && !isURLENCODEC) ? new Buffer(0) : null;

            // enable the parsing at Vert.x level
            request.expectMultiPart(true);

            if(isMULTIPART) {
                request.uploadHandler(new Handler<HttpServerFileUpload>() {
                    @Override
                    public void handle(final HttpServerFileUpload fileUpload) {
                        if (request.files() == null) {
                            request.setFiles(new HashMap<String, FileUpload>());
                        }
                        FileUpload upload = new FileUpload(vertx, fileUpload, uploadDir);

                        // setup callbacks
                        fileUpload.exceptionHandler(new Handler<Throwable>() {
                            @Override
                            public void handle(Throwable throwable) {
                                next.handle(throwable);
                            }
                        });

                        // stream to the generated path
                        fileUpload.streamToFileSystem(upload.path());
                        // store a reference in the request
                        request.files().put(fileUpload.name(), upload);
                    }
                });
            }

            request.dataHandler(new Handler<Buffer>() {
                long size = 0;
                final long limit = request.bodyLengthLimit();

                @Override
                public void handle(Buffer event) {
                    if (limit != -1) {
                        size += event.length();
                        if (size < limit) {
                            if (!isMULTIPART && !isURLENCODEC) {
                                buffer.appendBuffer(event);
                            }
                        } else {
                            request.dataHandler(null);
                            request.endHandler(null);
                            next.handle(413);
                        }
                    } else {
                        if (!isMULTIPART && !isURLENCODEC) {
                            buffer.appendBuffer(event);
                        }
                    }
                }
            });

            request.endHandler(new Handler<Void>() {
                @Override
                public void handle(Void _void) {
                    if (isJSON) {
                        parseJson(request, buffer, next);
                    } else {
                        next.handle(null);
                    }
                }
            });
        }
    }

    /**
     * Parse JSON request bodies int the HttpServiceRequest.body
     * @param request
     * @param buffer
     * @param next
     */
    private void parseJson(final HttpServiceRequest request, final Buffer buffer,
        final Handler<Object> next) {
        try {
            String content = buffer.toString();
            if (content.length() > 0) {
                try {
                    request.setBody(Json().toObject(content, Map.class));
                } catch (DecodeException e) {
                    next.handle(400);
                    return;
                }
                next.handle(null);
            } else {
                next.handle(400);
            }
        } catch (DecodeException ex) {
            next.handle(ex);
        }
    }
}

