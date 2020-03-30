package com.hatiko.ripple.telegram.bot.core.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hatiko.ripple.telegram.bot.core.XrpLongPollingBot;
import com.hatiko.ripple.telegram.bot.core.properties.ActionProperties;
import com.hatiko.ripple.wrapper.web.model.BalanceResponse;
import com.hatiko.ripple.wrapper.web.model.TransactionResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class ResponseMessageOperatorImpl implements ResponseMessageOperator {
	
	private final XrpLongPollingBot xrpLongPollingBot;
	private final ActionProperties actionProperties;
	private final KeyboardPreparator keyboardPreparator;

	@Override
	public Integer responseStart(String firstName, Long chatId) {

		String text = String.format("Hello, %s", firstName);
		
		return xrpLongPollingBot.sendMessage(chatId, text, keyboardPreparator.getStartKeyboard());
	}

	@Override
	public Integer responseHello(String firstName, Long chatId) {

		String text = String.format("Hello, %s", firstName);
		
		return xrpLongPollingBot.sendMessage(chatId, text, keyboardPreparator.getStartKeyboard());
	}

	@Override
	public Integer responseMain(Long chatId) {

		String text = "You are at main now";
		
		return xrpLongPollingBot.sendMessage(chatId, text, keyboardPreparator.getMainKeyboard());
	}

	@Override
	public Integer responseNext(Long chatId) {

		String text = "You stay unlogged in";
		return xrpLongPollingBot.sendMessage(chatId, text, keyboardPreparator.getMainKeyboard());
	}

	@Override
	public Integer responseHelp(Long chatId) {

		String text = "We will help you";
		return xrpLongPollingBot.sendMessage(chatId, text, null);
	}

	@Override
	public Integer responseLogIn(Long chatId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer responseRegister(Long chatId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer responseGenerateMemo(String walletMemo, Long chatId) {

		return xrpLongPollingBot.sendMessage(chatId, walletMemo, keyboardPreparator.getMainKeyboard());
	}

	@Override
	public Integer responseGetBalance(Object responseObject, Long chatId, Integer operationCounter) {

		String responseMessage = null;
		if(operationCounter.equals(0)) {
			responseMessage = "Insert your wallet (public key)";
			return xrpLongPollingBot.sendMessage(chatId, responseMessage, null);
		}
		if (operationCounter.equals(1)) {
			responseMessage = String.format("Your balance is %s", ((BalanceResponse) responseObject).getAmount());
			return xrpLongPollingBot.sendMessage(chatId, responseMessage, keyboardPreparator.getMainKeyboard());
		}
		return responseErrorMessage(actionProperties.getMethodName().getGetBalance(), chatId);
	}

	@Override
	public Integer responseGetLastTransactions(List<TransactionResponse> transactionResponse, Long chatId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer responseWithdraw(TransactionResponse transactionResponse, Long chatId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer responseErrorMessage(String operation, Long chatId) {
		// TODO Auto-generated method stub
		return null;
	}
}
