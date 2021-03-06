import groovy.json.JsonOutput
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.impl.Utils

import java.text.SimpleDateFormat

def router = Router.router(vertx)

def format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.UK)
format.setTimeZone(TimeZone.getTimeZone("GMT"))

def counter = 0
def lastUpdate = new Date()

router.route("/offline-first-sw/*").handler(StaticHandler.create()
        .setCachingEnabled(false)
        .setAllowRootFileSystemAccess(true)
        .setWebRoot("../offline-first-sw/dist")
        .setDirectoryListing(true)
)

router.route("/offline-first-pouchdb/*").handler(StaticHandler.create()
        .setCachingEnabled(false)
        .setAllowRootFileSystemAccess(true)
        .setWebRoot("../offline-first-pouchdb/dist")
        .setDirectoryListing(true)
)

router.route("/message").handler({ routingContext ->

    def request = routingContext.request()
    def ifModifiedSince = request.headers().get("if-modified-since")

    def response = routingContext.response()
    response.setChunked(true)
    response.putHeader("Content-Type", "application/json")
    response.putHeader("Last-Modified", format.format(lastUpdate))
    response.putHeader("Cache-Control", "no-cache, must-revalidate, max-age=0")

    if (ifModifiedSince != null) {
        Date ifModifiedSinceDate = format.parse(ifModifiedSince);
        // only compare seconds because header doesn't include millis
        ifModifiedSinceSeconds = Utils.secondsFactor(ifModifiedSinceDate.getTime())
        lastUpdateSeconds = Utils.secondsFactor(lastUpdate.getTime())

        if (ifModifiedSinceSeconds >= lastUpdateSeconds) {
            response.setStatusCode(304)
            response.end()
            return
        }
    }

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

