package br.com.joschonarth.springfit.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

    public static final String ASSESSMENT_EXCHANGE = "assessment.exchange";
    public static final String ASSESSMENT_CREATED_QUEUE = "assessment.created.queue";
    public static final String ASSESSMENT_CREATED_ROUTING_KEY = "assessment.created";
    public static final String ASSESSMENT_CREATED_DLQ = "assessment.created.dlq";

    @Bean
    public TopicExchange assessmentExchange() {
        return new TopicExchange(ASSESSMENT_EXCHANGE);
    }

    @Bean
    public Queue assessmentCreatedQueue() {
        return QueueBuilder.durable(ASSESSMENT_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", ASSESSMENT_CREATED_DLQ)
                .build();
    }

    @Bean
    public Queue assessmentCreatedDeadLetterQueue() {
        return new Queue(ASSESSMENT_CREATED_DLQ, true);
    }

    @Bean
    public Binding assessmentCreatedBinding() {
        return BindingBuilder.bind(assessmentCreatedQueue())
                .to(assessmentExchange())
                .with(ASSESSMENT_CREATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter("br.com.joschonarth.springfit.dto.event");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);

        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxRetries(3)
                .backOffOptions(2000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());

        return factory;
    }
}
