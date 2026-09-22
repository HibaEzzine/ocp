package com.ocp.pdr.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ocp.pdr.model.Utilisateur;
import com.ocp.pdr.model.enums.Role;

import lombok.Getter;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    @Getter
    private final Long id;
    private final String username;
    
    @Getter
    private final String nom;
    
    @Getter
    private final String prenom;
    
    @Getter
    private final Role role;
    
    @Getter
    private final Boolean actif;

    @JsonIgnore
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String password, String nom, String prenom, Role role, Boolean actif, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
        this.actif = actif;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(Utilisateur user) {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + (user.getRole() != null ? user.getRole().name() : "ADMINISTRATEUR"))
        );

        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getMotDePasse(),
                user.getNom(),
                user.getPrenom(),
                user.getRole(),
                user.getActif(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return actif == null || actif;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return actif == null || actif;
    }
}
