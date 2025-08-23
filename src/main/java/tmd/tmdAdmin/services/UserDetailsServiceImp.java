package tmd.tmdAdmin.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tmd.tmdAdmin.data.entities.User;
import tmd.tmdAdmin.data.repositories.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDetailsServiceImp implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user=userRepository.findUserByUsername(username);
        if(user ==null){
            throw  new UsernameNotFoundException("invalid username or password");
        }
        return new UserDetailsImp(user);
    }
}
