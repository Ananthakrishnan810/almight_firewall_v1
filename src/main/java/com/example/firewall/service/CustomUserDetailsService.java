package com.example.firewall.service;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

<<<<<<< HEAD
import com.example.firewall.entity.UserMasterEntity;
import com.example.firewall.repository.UserMasterDao;
=======
import com.example.firewall.dao.UserMasterDao;
import com.example.firewall.entity.UserMasterEntity;
>>>>>>> 0e72d3586e2d65d51679c159a5c63678f65035c2

@Service
public class CustomUserDetailsService implements UserDetailsService{

    @Autowired
    private UserMasterDao userMasterDao;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

                UserMasterEntity user = userMasterDao.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

                Collection<GrantedAuthority> authorities = new ArrayList<>();

                authorities.add(new SimpleGrantedAuthority(user.getRole().getName()));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>()
        );
    }    


}