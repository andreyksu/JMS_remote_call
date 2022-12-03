package ru.annikonenkov.jms;

public enum DefaultParam {

    CONNECTION_FACTORY("jms/RemoteConnectionFactory"),
    DESTINATION("jms/queue/myqueue"),
    USERNAME("testov3"),
    PASSWORD("testov_3"),
    CONTEXT_FACTORY("org.wildfly.naming.client.WildFlyInitialContextFactory"),
    URL("http-remoting://localhost:8080");

    private String param;

    private DefaultParam(String param) {
        this.param = param;
    }

    public String getParam() {
        return this.param;
    }

}
