/**
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Baked with love by corbett
 * Project: treefs
 * Package: net.cworks.treefs.spark
 * Class: SparkServer
 * Created: 1/29/15 1:59 PM
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
package net.cworks.treefs.spark;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class SparkServer {

    public static void main(String[] args) {

        // path resources & operations
        Spark.post("/:fs/.*", (request, response) -> {
            System.out.println(request.toString());
            
            return "";
        });

        Spark.put("/:fs/.*", (request, response) -> {
            System.out.println(request.toString());

            return "";
        });

        Spark.patch("/:fs/.*", (request, response) -> {
            System.out.println(request.toString());

            return "";
        });

        Spark.delete("/:fs/.*", (request, response) -> {
            System.out.println(request.toString());
            if(request.queryParams("command") != null) {
                // has an verb action on the URI
            }

            return "";
        });

        // leaf resources and operations
        Spark.get("/:fs/*/:command", (request, response) -> {
            System.out.println(request.params("fs"));
            System.out.println(request.pathInfo());

            return "";
        });

        Spark.get("/:fs/*", (request, response) -> {
            System.out.println(request.params("fs"));
            System.out.println(request.queryParams("command"));
            System.out.println(request.pathInfo());

            return "";
        });

        Spark.delete("/:fs/.*/_trash", (request, response) -> {
            System.out.println(request.toString());

            return "";
        });

        Spark.post("/:fs/.*/_copy", (request, response) -> {
            System.out.println(request.toString());

            return "";
        });

        Spark.put("/:fs/.*/_move", (request, response) -> {
            System.out.println(request.toString());

            return "";
        });
        
        
        
        
    }
    
}
