package com.hatiko.ripple.telegram.bot.core.transformer;

import java.time.LocalDateTime;

import org.telegram.telegrambots.meta.api.objects.Chat;

import com.hatiko.ripple.telegram.bot.core.model.TelegramChat;

public class ChatToTelegramChatTransformer implements Transformer<Chat, TelegramChat> {

	@Override
	public TelegramChat transform(Chat chat) {

		return TelegramChat.builder()
				.id(chat.getId())
				.creationDate(LocalDateTime.now())
				.userChat(chat.isUserChat())
				.groupChat(chat.isGroupChat())
				.channelChat(chat.isChannelChat())
				.superGroupChat(chat.isSuperGroupChat())
				.build();
	}

}
