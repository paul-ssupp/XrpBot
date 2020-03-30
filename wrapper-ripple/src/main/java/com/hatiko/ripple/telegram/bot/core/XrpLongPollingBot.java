package com.hatiko.ripple.telegram.bot.core;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.hatiko.ripple.telegram.bot.core.dto.TelegramUpdate;
import com.hatiko.ripple.telegram.bot.core.handler.TelegramMessageHandler;
import com.hatiko.ripple.telegram.bot.core.properties.XrpBotProperties;
import com.hatiko.ripple.telegram.bot.core.service.KeyboardPreparator;
import com.hatiko.ripple.telegram.bot.core.service.ResponseMessageOperator;
import com.hatiko.ripple.telegram.bot.core.transformer.Transformer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class XrpLongPollingBot extends TelegramLongPollingBot {

	private final List<TelegramMessageHandler> telegramMessageHandlers;

	private final XrpBotProperties xrpBotProperties;
	private final KeyboardPreparator keyboardPreparator;
	private final Transformer<Update, TelegramUpdate> updateToTelegramUpdateTransformer;
	private final ResponseMessageOperator responseMessageOperator; 
	
	@Autowired
	public XrpLongPollingBot(@Lazy List<TelegramMessageHandler> telegramMessageHandlers,
			XrpBotProperties xrpBotProperties, KeyboardPreparator keyboardPreparator,
			Transformer<Update, TelegramUpdate> updateToTelegramUpdateTransformer,
			@Lazy ResponseMessageOperator responseMessageOperator) {

		this.telegramMessageHandlers = telegramMessageHandlers;
		this.xrpBotProperties = xrpBotProperties;
		this.keyboardPreparator = keyboardPreparator;
		this.updateToTelegramUpdateTransformer = updateToTelegramUpdateTransformer;
		this.responseMessageOperator = responseMessageOperator;
	}

	@Override
	public void onUpdateReceived(Update update) {

		TelegramUpdate telegramUpdate = updateToTelegramUpdateTransformer.transform(update);
		telegramMessageHandlers.forEach(telegramMessageHandler -> telegramMessageHandler.handle(telegramUpdate));
	}

	@Override
	public String getBotUsername() {
		return xrpBotProperties.getUsername();
	}

	@Override
	public String getBotToken() {
		return xrpBotProperties.getToken();
	}

	public synchronized Integer sendMessage(Long chatId, String text, ReplyKeyboardMarkup keyboard) {

		SendMessage sendMessage = new SendMessage();

		sendMessage.enableMarkdown(Boolean.TRUE);
		sendMessage.setChatId(chatId);
		sendMessage.setText(text);
		sendMessage.setReplyMarkup(keyboard);

		try {
			return execute(sendMessage).getMessageId();
		} catch (TelegramApiException e) {
			log.error("Error while sending message : {}", e.getMessage());
		}
		return null;
	}
}
