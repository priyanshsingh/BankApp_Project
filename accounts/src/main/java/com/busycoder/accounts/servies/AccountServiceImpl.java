package com.busycoder.accounts.servies;

import com.busycoder.accounts.dto.AccountDto;
import com.busycoder.accounts.dto.AccountInfoDto;
import com.busycoder.accounts.dto.CardDto;
import com.busycoder.accounts.dto.LoanDto;
import com.busycoder.accounts.entities.Account;
import com.busycoder.accounts.repo.AccountRepo;
import com.busycoder.accounts.serviceproxy.CardServiceProxy;
import com.busycoder.accounts.serviceproxy.LoanServiceProxy;
import com.busycoder.accounts.util.DtoConvertor;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService{

    private final AccountRepo accountRepo;
    private final CardServiceProxy cardServiceProxy;
    private final LoanServiceProxy loanServiceProxy;

    @Override
    public List<AccountDto> getAll() {
        return accountRepo.findAll().stream()
                .map(DtoConvertor::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public AccountDto getByMobile(String mobile) {
        return DtoConvertor.entityToDto(accountRepo.findByMobile(mobile));
    }

    @Override
    public AccountInfoDto getAccountDetails(String mobile) {
        AccountInfoDto accountInfoDtoResponse = new AccountInfoDto();
        AccountDto accountDto = getByMobile(mobile);
        CardDto cardDto = null;
        LoanDto loanDto = null;

        try {
            cardDto = cardServiceProxy.findByMobileNumber(mobile);
        } catch (FeignException e) {
            System.err.println("Error fetching card details: " + e.getMessage());
        }
        try {
            loanDto = loanServiceProxy.getByMobile(mobile);
        } catch (FeignException e) {
            System.err.println("Error fetching loan details: " + e.getMessage());
        }

        if (accountDto != null) {
            accountInfoDtoResponse.setAccountDto(accountDto);
        }
        if (cardDto != null) {
            accountInfoDtoResponse.setCardDto(cardDto);
        }
        if (loanDto != null) {
            accountInfoDtoResponse.setLoanDto(loanDto);
        }
        return accountInfoDtoResponse;
    }

    @Override
    public String addAccount(AccountDto accountDto) {
        Account account=DtoConvertor.dtoToEntity(accountDto);
        accountRepo.save(account);
        accountDto.setAccId(account.getAccId());
        return "account is added successfully";
    }
}
