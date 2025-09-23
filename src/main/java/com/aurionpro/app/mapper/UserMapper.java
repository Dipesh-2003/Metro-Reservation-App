package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.UserDto;
import com.aurionpro.app.dto.WalletDto;
import com.aurionpro.app.dto.WalletTransactionDto;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Wallet;
import com.aurionpro.app.entity.WalletTransaction;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // User -> UserDto
    UserDto entityToDto(User user);
    
    // Wallet -> WalletDto
    // MapStruct is smart enough to see the method below and use it for the list
    WalletDto entityToDto(Wallet wallet);

    // WalletTransaction -> WalletTransactionDto
    WalletTransactionDto entityToDto(WalletTransaction transaction);
    
    // It also automatically handles lists
    List<WalletTransactionDto> entityToDto(List<WalletTransaction> transactions);
}
