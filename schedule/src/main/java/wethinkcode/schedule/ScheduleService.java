package wethinkcode.schedule;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import wethinkcode.schedule.transfer.SlotDO;
import wethinkcode.schedule.transfer.DayDO;
import wethinkcode.schedule.transfer.ScheduleDO;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * I provide a REST API providing the current loadshedding schedule for a
 * given town (in a specific province) at a given loadshedding stage.
 */
public class ScheduleService
{
    public static final int DEFAULT_STAGE = 0; // no loadshedding. Ha!

    public static final int DEFAULT_PORT = 7002;

    private Javalin server;

    private int servicePort;

    public static void main( String[] args ){
        final ScheduleService svc = new ScheduleService().initialise();
        svc.start();
    }

    @VisibleForTesting
    ScheduleService initialise(){
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

        app.get("/{province}/{town}/{stage}", ctx -> {
            String province = ctx.pathParam("province");
            String town = ctx.pathParam("town");
            int stage = Integer.parseInt(ctx.pathParam("stage"));

            if (stage < 1 || stage > 12) {
                ctx.status(400).result("Illegal stage value");
            } else {
                Optional<ScheduleDO> schedule = getSchedule(province, town, stage);
                if (schedule.isPresent()) {
                    ctx.json(schedule.get());
                } else {
                    ctx.status(404).json(emptySchedule());
                }
            }
        });

        return app;
    }

    // There *must* be a better way than this...
    // See Steps 4 and 5 (the optional ones!) in the course notes.
    Optional<ScheduleDO> getSchedule( String province, String town, int stage ){
        return province.equalsIgnoreCase( "Mars" )
            ? Optional.empty()
            : Optional.of( mockSchedule() );
    }

    /**
     * Answer with a hard-coded/mock Schedule.
     * @return A non-null, slightly plausible Schedule.
     */
    private static ScheduleDO mockSchedule(){
        final List<SlotDO> slots = List.of(
            new SlotDO( LocalTime.of( 2, 0 ), LocalTime.of( 4, 0 )),
            new SlotDO( LocalTime.of( 10, 0 ), LocalTime.of( 12, 0 )),
            new SlotDO( LocalTime.of( 18, 0 ), LocalTime.of( 20, 0 ))
        );
        final List<DayDO> days = List.of(
            new DayDO( slots ),
            new DayDO( slots ),
            new DayDO( slots ),
            new DayDO( slots )
        );
        return new ScheduleDO( days );
    }

    /**
     * Answer with a non-null but empty Schedule.
     * @return The empty Schedule.
     */
    private static ScheduleDO emptySchedule(){
        final List<SlotDO> slots = Collections.emptyList();
        final List<DayDO> days = Collections.emptyList();
        return new ScheduleDO( days );
    }
}
