package io.cryptex.ms.wrapper.ripple.service;

import io.cryptex.ms.wrapper.ripple.converter.RippleBalanceConverter;
import io.cryptex.ms.wrapper.ripple.converter.RippleTransactionConverter;
import io.cryptex.ms.wrapper.ripple.exception.InnerServiceException;
import io.cryptex.ms.wrapper.ripple.integration.blockchain.dto.request.RippleAccountInfoRequest;
import io.cryptex.ms.wrapper.ripple.integration.blockchain.dto.request.RippleAccountInfoRequest.Param;
import io.cryptex.ms.wrapper.ripple.integration.blockchain.dto.request.RippleTrxByHashRequest;
import io.cryptex.ms.wrapper.ripple.integration.blockchain.dto.request.RippleWithdrawRequest;
import io.cryptex.ms.wrapper.ripple.integration.blockchain.dto.response.RippleAccountInfoResponse;
import io.cryptex.ms.wrapper.ripple.integration.blockchain.dto.response.RippleWithdrawResponse;
import io.cryptex.ms.wrapper.ripple.integration.blockchain.dto.response.TransactionBody;
import io.cryptex.ms.wrapper.ripple.integration.blockchain.properties.RippleBlockchainProperties;
import io.cryptex.ms.wrapper.ripple.integration.blockchain.rest.RippleCommunicationService;
import io.cryptex.ms.wrapper.ripple.integration.properties.WalletProperties;
import io.cryptex.ms.wrapper.ripple.web.model.AddressResponse;
import io.cryptex.ms.wrapper.ripple.web.model.BalanceResponse;
import io.cryptex.ms.wrapper.ripple.web.model.MemoResponse;
import io.cryptex.ms.wrapper.ripple.web.model.TransactionResponse;
import io.cryptex.ms.wrapper.ripple.web.model.WithdrawRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class RippleServiceImpl implements RippleService {

    private static final String SUCCESS = "success";
    private final RippleCommunicationService rippleCommunicationService;
    private final SignatureService signatureService;
    private final RippleBlockchainProperties rippleBlockchainProperties;
    private final WalletProperties walletProperties;
    private Random random = new Random();

    @Override
    public BalanceResponse getWalletBalance() {

        RippleAccountInfoRequest rippleAccountInfoRequest = RippleAccountInfoRequest.builder()
                .method(rippleBlockchainProperties.getMethod().getAccountInfo())
                .params(Collections.singletonList(Param.builder()
                        .account(walletProperties.getAccount())
                        .build())
                )
                .build();

        RippleAccountInfoResponse rippleAccountInfoResponse;
        try {
            rippleAccountInfoResponse = rippleCommunicationService.getAccountInfo(rippleAccountInfoRequest);
        } catch (Exception e) {
            log.error("Error while connecting to Ripple node when getting WalletBalance | Message : {}",
                    e.getMessage());
            log.debug("Retry request to get balance by other uri");
            rippleAccountInfoResponse = rippleCommunicationService.getAccountInfo(rippleAccountInfoRequest);
        }

        return BalanceResponse.builder()
                .amount(RippleBalanceConverter.toDouble(rippleAccountInfoResponse.getResult().getAccountData().getBalance()))
                .build();
    }

    @Override
    public AddressResponse getAccountAddress() {
        return AddressResponse.builder().walletAddress(walletProperties.getAccount()).build();
    }

    @Override
    public TransactionResponse withdraw(WithdrawRequest withdrawRequest) {
        String to = withdrawRequest.getTo();
        Double amount = withdrawRequest.getQuantity();
        String memo = withdrawRequest.getMemo();

        String signature = signatureService.signTransaction(to, amount, memo);

        RippleWithdrawRequest requestToSubmit = RippleWithdrawRequest.builder()
                .method(rippleBlockchainProperties.getMethod().getSubmit())
                .params(Collections.singletonList(RippleWithdrawRequest.Param.builder()
                        .txBlob(signature)
                        .build())
                )
                .build();

        RippleWithdrawResponse rippleWithdrawResponse;
        try {
            rippleWithdrawResponse = rippleCommunicationService.withdraw(requestToSubmit);
        } catch (Exception e) {
            log.error("Error while connecting to Ripple node when requesting to submit a trx | Message : {}",
                    e.getMessage());
            log.debug("Retry request to submit transaction by other uri");
            rippleWithdrawResponse = rippleCommunicationService.withdraw(requestToSubmit);
        }

        String status = rippleWithdrawResponse.getResult().getStatus();
        if (status.equals(SUCCESS)) {
            return RippleTransactionConverter.toTransactionResponse(rippleWithdrawResponse.getResult().getTxJson(), walletProperties);
        } else {
            log.error("Error while on withdraw. To = {}, amount = {}, memo = {}. Transaction status = {}",
                    to, amount, memo, status);
            throw new InnerServiceException(String.format("Error while on withdraw. To = %s, amount = %s, memo = %s. Transaction status = %s",
                    to, amount, memo, status));
        }

    }

    @Override
    public TransactionResponse getTransactionByHash(String transactionHash) {
        RippleTrxByHashRequest request = RippleTrxByHashRequest.builder()
                .method(rippleBlockchainProperties.getMethod().getTransaction())
                .params(Collections.singletonList(RippleTrxByHashRequest.Param.builder()
                        .transactionHash(transactionHash)
                        .build())
                )
                .build();

        TransactionBody transactionBody;
        try {
            transactionBody = rippleCommunicationService.getTransactionByHash(request).getResult();
        } catch (Exception e) {
            log.error("Error while connecting to Ripple node when getting trx by id | Message : {}", e.getMessage());
            log.debug("Retry request to get transaction by id by other uri");
            transactionBody = rippleCommunicationService.getTransactionByHash(request).getResult();
        }
        return RippleTransactionConverter.toTransactionResponse(transactionBody, walletProperties);
    }

    @Override
    public MemoResponse generateMemo() {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < walletProperties.getMemoLength()) {
            sb.append(Integer.toHexString(random.nextInt()).toUpperCase());
        }
        String memo = sb.toString().substring(0, walletProperties.getMemoLength());
        return MemoResponse.builder().walletMemo(memo).build();
    }

}
