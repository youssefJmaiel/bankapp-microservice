package com.bankapp.mission.config;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.JMSException;

@Configuration
public class MissionMqConfig {

    @Value("${ibm.mq.host}")
    private String host;

    @Value("${ibm.mq.port}")
    private int port;

    @Value("${ibm.mq.queue-manager}")
    private String queueManager;

    @Value("${ibm.mq.channel}")
    private String channel;

    @Value("${ibm.mq.user}")
    private String user;

    @Value("${ibm.mq.password}")
    private String password;

    @Bean
    public MQQueueConnectionFactory missionMqConnectionFactory() throws JMSException {
        MQQueueConnectionFactory factory = new MQQueueConnectionFactory();
        factory.setHostName(host);
        factory.setPort(port);
        factory.setQueueManager(queueManager);
        factory.setChannel(channel);
        factory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        factory.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
        factory.setStringProperty(WMQConstants.USERID, user);
        factory.setStringProperty(WMQConstants.PASSWORD, password);
        return factory;
    }

    @Bean
    public CachingConnectionFactory missionCachingConnectionFactory(
            MQQueueConnectionFactory missionMqConnectionFactory) {
        CachingConnectionFactory factory =
                new CachingConnectionFactory(missionMqConnectionFactory);
        factory.setSessionCacheSize(5);
        return factory;
    }

    @Bean
    public JmsTemplate missionJmsTemplate(
            CachingConnectionFactory missionCachingConnectionFactory) {
        return new JmsTemplate(missionCachingConnectionFactory);
    }
}
