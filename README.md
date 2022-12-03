## Апробация работы с JMS (по мотивам WildFly - QuickStart)
### Первый вариант вызова JMS - когда отправитель и получатель являются внешними по отношению к App-server (т.е. WildFly в этом примере является - MessageBroker)
##### jms_remote_producer_and_receiver 
1. Распаковать wildFly
2. Добавить пользователя `testov3/testov_3`  через скрипт `${WildFly_Home}/bin/add_user.sh`
3. Добавить пользователя в группу guest (без этого не работает - хотя и исходной инструкции ничего не сказано про это):
    - В файле `${WildFly_Home}/standalone/configuration/application-roles.properties` для добавленного пользователя добавить роль:
        ```
        testov3=guest
        ```
    - Это необходимо, т.к. в конфигурации JMS - добавлена эта роль
        ```xml
        <subsystem xmlns="urn:jboss:domain:messaging-activemq:14.0">
                <server name="default">
                    <security elytron-domain="ApplicationDomain"/>
                    <statistics enabled="${wildfly.messaging-activemq.statistics-enabled:${wildfly.statistics-enabled:false}}"/>
                    <security-setting name="#">
                        <role name="guest" send="true" consume="true" create-non-durable-queue="true" delete-non-durable-queue="true"/>
                    </security-setting>
                    ......................
        ```
        - Или можно попробовать создать свою роль для этого пользователя и добавить в узел ```security-setting``` данную роль
4. Добавить очередь в `standalone-full.xml`
    ```xml
        ...................
        <in-vm-acceptor name="in-vm" server-id="0">
            <param name="buffer-pooling" value="false"/>
        </in-vm-acceptor>

        <jms-queue name="ExpiryQueue" entries="java:/jms/queue/ExpiryQueue"/>
        <jms-queue name="FirstQueue" entries="queue/myqueue java:jboss/exported/jms/queue/myqueue"/> <!-- <<<<<<<< -->
        <jms-queue name="DLQ" entries="java:/jms/queue/DLQ"/>

        <connection-factory name="InVmConnectionFactory" entries="java:/ConnectionFactory" connectors="in-vm"/>
        <connection-factory name="RemoteConnectionFactory" entries="java:jboss/exported/jms/RemoteConnectionFactory" connectors="http-connector"/>
        <pooled-connection-factory name="activemq-ra" entries="java:/JmsXA java:jboss/DefaultJMSConnectionFactory" connectors="in-vm" transaction="xa"/>
    </server>
    ```
5. Скопилировать и запустить пример:
    ```sh
    mvn clean compile exec:java
    ```

#### TODO:
1. Поработать в разных потоках:
    - Один поток считывает с клавиатуры сообщения;
    - Второй основной поток читает и печатает;
    - После получения команды выхода, заверашется оба потока;
2. Снова настройка с Elytron - доступ из вне;
3. Сделать номральную обработку исключений (разложить все по отдельным утилитраным классам и методам);
4. Почитать подробнее по JMS (вероятнее лучше уделить время RabbitMQ - как общей очереди);
