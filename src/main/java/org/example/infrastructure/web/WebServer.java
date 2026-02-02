package org.example.infrastructure.web;

import io.javalin.Javalin;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.example.core.entity.StoredBook;
import org.example.infrastructure.repository.JdbcBookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class WebServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServer.class);
    private final JdbcBookRepository bookRepository;
    private final Configuration freeMarkerCfg;
    private Javalin app;

    public WebServer() {
        this.bookRepository = new JdbcBookRepository();
        this.freeMarkerCfg = new Configuration(Configuration.VERSION_2_3_32);
        this.freeMarkerCfg.setClassForTemplateLoading(WebServer.class, "/");
        this.freeMarkerCfg.setDefaultEncoding("UTF-8");
        this.freeMarkerCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        this.freeMarkerCfg.setLogTemplateExceptions(false);
        this.freeMarkerCfg.setWrapUncheckedExceptions(true);
        this.freeMarkerCfg.setFallbackOnNullLoopVariable(false);
    }

    public void start(int port) {
        try {
            app = Javalin.create(config -> {
                config.staticFiles.add("/public");
                config.showJavalinBanner = false;
            }).start(port);

            app.get("/", ctx -> {
                try {
                    List<StoredBook> books = bookRepository.findPublicBooks();
                    if (books.isEmpty()) {
                        books = bookRepository.findByUserId(1);
                    }
                    
                    Template template = freeMarkerCfg.getTemplate("templates/library.ftl");
                    StringWriter writer = new StringWriter();
                    template.process(Map.of("books", books), writer);
                    
                    ctx.contentType("text/html").result(writer.toString());
                } catch (Exception e) {
                    LOGGER.error("Error processing index page", e);
                    ctx.status(500).result("Internal Server Error: " + e.getMessage());
                }
            });

            app.get("/book/{id}/cover", ctx -> {
                try {
                    int id = Integer.parseInt(ctx.pathParam("id"));
                    bookRepository.findById(id).ifPresentOrElse(book -> {
                        if (book.getCover() != null) {
                            ctx.contentType("image/jpeg").result(book.getCover());
                        } else {
                            ctx.status(404);
                        }
                    }, () -> ctx.status(404));
                } catch (Exception e) {
                    LOGGER.error("Error serving cover", e);
                    ctx.status(500);
                }
            });

            app.exception(Exception.class, (e, ctx) -> {
                LOGGER.error("Unhandled exception in web server", e);
                ctx.status(500).result("Internal Server Error");
            });

            LOGGER.info("Web server started successfully at http://localhost:{}", port);
        } catch (Exception e) {
            LOGGER.error("Failed to start web server on port {}", port, e);
            throw new RuntimeException("Web server failed to start", e);
        }
    }

    public void stop() {
        if (app != null) {
            app.stop();
        }
    }
}
