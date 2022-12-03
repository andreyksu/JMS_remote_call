package ru.annikonenkov.jms;

import static ru.annikonenkov.jms.DefaultParam.*;

import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSProducer;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;

public class FirstRemoteClient {

    private static final Logger log = Logger.getLogger(FirstRemoteClient.class.getName());

    private static Context initialContext;

    public static void main(String[] args) throws Exception {
        initialContext = createContext();

        log.info("_____________________________________________________");
        initialContext.getEnvironment().forEach((k, v) -> System.out.println("key = " + k.toString() + " :: Value = " + v.toString()));
        log.info("_____________________________________________________");
        log.info(String.format("The initialContext is = %s", initialContext));
        // Создание фабрики соеденинений - испльзуется для подключения к пункту назначения (по сути к брокеру)
        ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup(CONNECTION_FACTORY.getParam());
        log.info(String.format("The connectionFactory is = %s", connectionFactory));

        // Создание места-назначения. Т.е. место, где хранятся/доступны сообщения(по сути брокер).
        Destination queue = (Destination) initialContext.lookup(DESTINATION.getParam());
        log.info(String.format("The destination is = %s", queue));

        /*
         * Инкапсулирует соединение с провайдером JMS (JMSContext по сути является аналогом Connection и Session ). Connection - физическое подключение с JMS поставщиком Session - однопоточный контекст для получения и отправки сообщений
         * JMSContext - в контейнере JEE - включается через Inject
         */
        try (JMSContext context = connectionFactory.createContext(USERNAME.getParam(), PASSWORD.getParam())) {

            JMSProducer producer = context.createProducer();
            producer.send(queue, "_____________________________________________________");
            producer.send(queue, new Date().toString());
            producer.send(queue, "_____________________________________________________");

            // Синхронне получение сообщений.
            JMSConsumer consumer = context.createConsumer(queue);
            String text = consumer.receiveBody(String.class, 5000);
            String text1 = consumer.receiveBody(String.class, 5000);
            String text2 = consumer.receiveBody(String.class, 5000);

            log.info("Received message with content " + text);
            log.info("Received message with content " + text1);
            log.info("Received message with content " + text2);

            // А можнжо принимать асинхронно
            context.createConsumer(queue).setMessageListener(new MessageListener() {

                @Override
                public void onMessage(Message message) {
                    try {
                        log.info("Received message with content " + message.getBody(String.class));
                    } catch (JMSException e) {
                        log.severe("При чтении сообщения из очереди получили ошибку : ");
                        log.severe(e.getMessage());
                    }
                }
            });

            // А можнжо принимать асинхронно и с новомодной лямбдой!
            context.createConsumer(queue).setMessageListener(message -> {
                try {
                    log.info("Received message with content " + message.getBody(String.class));
                } catch (JMSException e) {
                    log.severe("При чтении сообщения из очереди получили ошибку : ");
                    log.severe(e.getMessage());
                }
            });

        } finally {
            try {
                log.info("Закрываем initialContext. ");
                if (initialContext != null) {
                    initialContext.close();
                } else {
                    log.info("initialContext = null - не будет закрыт. ");
                }
            } catch (NamingException e) {
                log.severe("При попытке закрыть initialContext возникло исключение: ");
                log.severe(e.getMessage());
            } finally {
                log.info("That is all!!!");
            }
        }

    }

    public static Context createContext() {
        Context initialContext = null;
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY.getParam());
        env.put(Context.PROVIDER_URL, URL.getParam());
        env.put(Context.SECURITY_PRINCIPAL, USERNAME.getParam());
        env.put(Context.SECURITY_CREDENTIALS, PASSWORD.getParam());
        try {
            initialContext = new InitialContext(env);
        } catch (NamingException e) {
            log.severe(e.getMessage());
        }
        return initialContext;
    }
}
