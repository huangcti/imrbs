package tw.huangcti.imrbs.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQConfig - RabbitMQ 訊息佇列配置
 * 
 * 訊息類型:
 * - 預約確認通知
 * - 預約取消通知
 * - 30 分鐘提醒通知
 * - 訪客預約審核通知
 * 
 * 設計:
 * - Direct Exchange: 精確路由
 * - Durable Queue: 持久化佇列
 * - Dead Letter Queue: 處理失敗訊息
 */
@Configuration
public class RabbitMQConfig {
    
    // Exchange 名稱
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    
    // Queue 名稱
    public static final String EMAIL_QUEUE = "notification.email.queue";
    public static final String REMINDER_QUEUE = "notification.reminder.queue";
    public static final String EMAIL_DLQ = "notification.email.dlq";
    
    // Routing Key
    public static final String EMAIL_ROUTING_KEY = "notification.email";
    public static final String REMINDER_ROUTING_KEY = "notification.reminder";
    
    /**
     * 定義 Notification Exchange (Direct 類型)
     */
    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE, true, false);
    }
    
    /**
     * 定義 Email 通知佇列
     */
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "email.dlq")
                .build();
    }
    
    /**
     * 定義 Reminder 提醒佇列 (30 分鐘延遲)
     */
    @Bean
    public Queue reminderQueue() {
        return QueueBuilder.durable(REMINDER_QUEUE)
                .withArgument("x-message-ttl", 1800000) // 30 分鐘 = 1800000 毫秒
                .withArgument("x-dead-letter-exchange", NOTIFICATION_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", EMAIL_ROUTING_KEY)
                .build();
    }
    
    /**
     * 定義 Dead Letter Queue (處理失敗訊息)
     */
    @Bean
    public Queue emailDeadLetterQueue() {
        return QueueBuilder.durable(EMAIL_DLQ).build();
    }
    
    /**
     * 綁定 Email Queue 到 Exchange
     */
    @Bean
    public Binding emailQueueBinding(Queue emailQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(emailQueue)
                .to(notificationExchange)
                .with(EMAIL_ROUTING_KEY);
    }
    
    /**
     * 綁定 Reminder Queue 到 Exchange
     */
    @Bean
    public Binding reminderQueueBinding(Queue reminderQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(reminderQueue)
                .to(notificationExchange)
                .with(REMINDER_ROUTING_KEY);
    }
    
    /**
     * 綁定 Dead Letter Queue 到 Exchange
     */
    @Bean
    public Binding emailDlqBinding(Queue emailDeadLetterQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(emailDeadLetterQueue)
                .to(notificationExchange)
                .with("email.dlq");
    }
    
    /**
     * JSON 訊息轉換器
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * RabbitTemplate 配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
    
    /**
     * Listener Container Factory 配置
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }
}
