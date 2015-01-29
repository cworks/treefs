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

            return "";
        });
        
        Spark.get("/:fs/*", (request, response) -> {
            System.out.println(request.params("fs"));
            System.out.println(request.pathInfo());

            return "";
        });

        // leaf resources and operations
        Spark.get("/:fs/*/#meta", (request, response) -> {
            System.out.println(request.params("fs"));
            System.out.println(request.pathInfo());

            return "";
        });


        Spark.delete("/:fs/.*/#trash", (request, response) -> {
            System.out.println(request.toString());

            return "";
        });

        Spark.post("/:fs/.*/#copy", (request, response) -> {
            System.out.println(request.toString());

            return "";
        });

        Spark.put("/:fs/.*/#move", (request, response) -> {
            System.out.println(request.toString());

            return "";
        });
        
        
        
        
    }
    
}
