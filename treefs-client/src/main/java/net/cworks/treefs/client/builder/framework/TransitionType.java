package net.cworks.treefs.client.builder.framework;

public enum TransitionType {
    Recursive,      // Fluent call goes back to same instance
    Lateral,        // Fluent call goes to a version of itself minus invoked method
    Terminal,       // Fluent call is ending and returning something of interest
    Ascending,      // Fluent call moves up the graph to a previously collected state
}
