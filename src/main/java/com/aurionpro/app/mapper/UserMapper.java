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

    // This is the mapping we need for the profile
    UserDto entityToDto(User user);
    
    // These are for the wallet functionality
    WalletDto entityToDto(Wallet wallet);
    WalletTransactionDto entityToDto(WalletTransaction transaction);
    List<WalletTransactionDto> entityToDto(List<WalletTransaction> transactions);
}