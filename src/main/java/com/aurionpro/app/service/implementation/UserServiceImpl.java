// In: com/aurionpro/app/service/implementation/UserServiceImpl.java
package com.aurionpro.app.service.implementation;

import com.aurionpro.app.common.Role;
import com.aurionpro.app.dto.SignUpRequest;
import com.aurionpro.app.dto.UserDto;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Wallet;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.UserMapper;
import com.aurionpro.app.repository.UserRepository;
import com.aurionpro.app.repository.WalletRepository;
import com.aurionpro.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public UserDto registerUser(SignUpRequest signUpRequest) {
        User user = new User();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        
        // --- THIS IS THE KEY PART FOR DEBUGGING ---
        String plainPassword = signUpRequest.getPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);
        
        // This will print the plain and hashed passwords to your console.
        // It's the best way to PROVE that your hashing is working correctly.
        System.out.println(">>>>>>>>>> Registering User: " + signUpRequest.getEmail());
        System.out.println(">>>>>>>>>> Plain Password: " + plainPassword);
        System.out.println(">>>>>>>>>> Hashed Password: " + hashedPassword);
        
        user.setPassword(hashedPassword);
        // ------------------------------------------
        
        user.setRole(Role.USER);
        User savedUser = userRepository.save(user);

        // (wallet creation logic)
        Wallet wallet = new Wallet();
        wallet.setUser(savedUser);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setLastUpdated(Instant.now());
        walletRepository.save(wallet);

        // Use mapper for the final response
        return userMapper.entityToDto(savedUser);
    }

    @Override
    public UserDto findByEmail(String email) {
        User user = findUserEntityByEmail(email);
        return userMapper.entityToDto(user);
    }

    @Override
    public User findUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}