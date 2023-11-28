package wethinkcode.stage;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;

/**
 * I provide a REST API that reports the current loadshedding "stage". I provide
 * two endpoints:
 * <dl>
 * <dt>GET /stage
 * <dd>report the current stage of loadshedding as a JSON serialisation
 *      of a {@code StageDO} data/transfer object
 * <dt>POST /stage
 * <dd>set a new loadshedding stage/level by POSTing a JSON-serialised {@code StageDO}
 *      instance as the body of the request.
 * </ul>
 */
public class StageService
{
    public static final int DEFAULT_STAGE = 0; // no loadshedding. Ha!

    public static final int DEFAULT_PORT = 7001;

    public static void main( String[] args ){
        final StageService svc = new StageService().initialise();
        svc.start();
    }

    private int loadSheddingStage;

    private Javalin server;

    private int servicePort;

    @VisibleForTesting
    StageService initialise(){
        return initialise( DEFAULT_STAGE );
    }

    @VisibleForTesting
    StageService initialise( int initialStage ){
        loadSheddingStage = initialStage;
        assert loadSheddingStage >= 0;

        server = initHttpServer();
        return this;
    }

    public void start(){
        start( DEFAULT_PORT );
    }

    @VisibleForTesting
    void start( int networkPort ){
        servicePort = networkPort;
        run();
    }

    public void stop(){
        server.stop();
    }

    public void run(){
        server.start( servicePort );
    }

    private Javalin initHttpServer() {
        Javalin app = Javalin.create();

        app.get("/stage", ctx -> {
            StageDO stage = new StageDO(loadSheddingStage);
            ctx.json(stage);
        });

        app.post("/stage", ctx -> {
            StageDO stage = ctx.bodyAsClass(StageDO.class);
            if (stage.getStage() < 0) {
                ctx.status(400);
            } else {
                loadSheddingStage = stage.getStage();
                ctx.status(200);
            }
        });

        return app;
    }
}
