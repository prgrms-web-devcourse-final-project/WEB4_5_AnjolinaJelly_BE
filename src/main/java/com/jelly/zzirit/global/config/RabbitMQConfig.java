package com.jelly.zzirit.global.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	@Value("${spring.rabbitmq.host}")
	private String host;

	@Value("${spring.rabbitmq.port}")
	private int port;

	@Value("${spring.rabbitmq.username}")
	private String username;

	@Value("${spring.rabbitmq.password}")
	private String password;

	@Value("${rabbitmq.queue.name}")
	private String queueName;

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	@Value("${rabbitmq.routing.key}")
	private String routingKey;

	@Value("${rabbitmq.queue.dlq-name}")
	private String dlqName;

	@Value("${rabbitmq.exchange.dlq-name}")
	private String dlxName;

	@Value("${rabbitmq.routing.dlq-key}")
	private String dlqRoutingKey;

	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
		factory.setUsername(username);
		factory.setPassword(password);
		return factory;
	}

	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
		MessageConverter messageConverter) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(messageConverter);
		return template;
	}

	@Bean
	public Queue orderConfirmQueue() {
		return QueueBuilder.durable(queueName)
			.withArgument("x-dead-letter-exchange", dlxName)
			.withArgument("x-dead-letter-routing-key", dlqRoutingKey)
			.build();
	}

	@Bean
	public DirectExchange orderConfirmExchange() {
		return new DirectExchange(exchangeName);
	}

	@Bean
	public Binding orderConfirmBinding() {
		return BindingBuilder.bind(orderConfirmQueue())
			.to(orderConfirmExchange())
			.with(routingKey);
	}

	@Bean
	public Queue orderConfirmDlqQueue() {
		return new Queue(dlqName, true);
	}

	@Bean
	public DirectExchange orderConfirmDlqExchange() {
		return new DirectExchange(dlxName);
	}

	@Bean
	public Binding orderConfirmDlqBinding() {
		return BindingBuilder.bind(orderConfirmDlqQueue())
			.to(orderConfirmDlqExchange())
			.with(dlqRoutingKey);
	}
}