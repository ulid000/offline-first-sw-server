import groovy.json.JsonOutput
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import java.text.SimpleDateFormat

def router = Router.router(vertx)

def format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.UK)
format.setTimeZone(TimeZone.getTimeZone("GMT"))

def counter = 0
def lastUpdate = new Date()

router.route("/offline/*").handler(StaticHandler.create()
        .setCachingEnabled(false)
        .setAllowRootFileSystemAccess(true)
        .setWebRoot("../offline-first-sw/dist")
        .setDirectoryListing(true)
)

router.route("/message").handler({ routingContext ->

    def response = routingContext.response()
    response.setChunked(true)
    response.putHeader("content-type", "application/json")
    response.putHeader("Last-Modified", format.format(lastUpdate))
    response.write(JsonOutput.toJson(
            [
                    message: 'Hello ' + counter,
                    timestamp: lastUpdate.getDateTimeString()
            ]))
            .end()
})

router.route("/message/increment").handler( {routingContext ->
    counter++
    lastUpdate = new Date()
    routingContext.response().end()
})

vertx.createHttpServer().requestHandler(router.&accept).listen(8080)

println("Server is started")