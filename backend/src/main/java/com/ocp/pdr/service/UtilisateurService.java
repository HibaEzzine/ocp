package com.ocp.pdr.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ocp.pdr.model.Utilisateur;
import com.ocp.pdr.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository repo;
    private final PasswordEncoder passwordEncoder;

    public List<Utilisateur> findAll() {
        return repo.findAll();
    }

    public Optional<Utilisateur> findById(Long id) {
        return repo.findById(id);
    }

    public Optional<Utilisateur> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    @Transactional
    public Utilisateur register(Utilisateur utilisateur) {
        if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().isBlank()) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire");
        }
        if (repo.findByEmail(utilisateur.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Cet email existe déjà");
        }
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        return repo.save(utilisateur);
    }

    @Transactional
    public Utilisateur save(Utilisateur utilisateur) {
        if (utilisateur.getId() != null) {
            Utilisateur existing = repo.findById(utilisateur.getId()).orElse(utilisateur);
            if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().isBlank()) {
                utilisateur.setMotDePasse(existing.getMotDePasse());
            } else if (!utilisateur.getMotDePasse().equals(existing.getMotDePasse())) {
                utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
            }
        } else if (utilisateur.getMotDePasse() != null && !utilisateur.getMotDePasse().isBlank()) {
            utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        }
        return repo.save(utilisateur);
    }

    @Transactional
    public Utilisateur changePassword(Long id, String currentPassword, String newPassword) {
        Utilisateur utilisateur = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'id: " + id));
        if (!passwordEncoder.matches(currentPassword, utilisateur.getMotDePasse())) {
            throw new IllegalArgumentException("Le mot de passe actuel est incorrect");
        }
        utilisateur.setMotDePasse(passwordEncoder.encode(newPassword));
        return repo.save(utilisateur);
    }

    @Transactional
    public Utilisateur updateProfile(String email, Utilisateur updated) {
        Utilisateur utilisateur = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'email: " + email));
        utilisateur.setNom(updated.getNom());
        utilisateur.setPrenom(updated.getPrenom());
        utilisateur.setEmail(updated.getEmail());
        if (updated.getMotDePasse() != null && !updated.getMotDePasse().isBlank()) {
            utilisateur.setMotDePasse(passwordEncoder.encode(updated.getMotDePasse()));
        }
        return repo.save(utilisateur);
    }

    @Transactional
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
