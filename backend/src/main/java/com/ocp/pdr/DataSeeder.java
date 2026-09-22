package com.ocp.pdr;

import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ocp.pdr.model.Utilisateur;
import com.ocp.pdr.model.enums.ResponsableDA;
import com.ocp.pdr.model.enums.Role;
import com.ocp.pdr.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // Adaptation automatique des types de colonnes pour MySQL
        try {
            jdbcTemplate.execute("ALTER TABLE article_pdr MODIFY COLUMN reference TEXT");
            jdbcTemplate.execute("ALTER TABLE article_pdr MODIFY COLUMN description TEXT");
            jdbcTemplate.execute("ALTER TABLE article_pdr MODIFY COLUMN categorie TEXT");
            jdbcTemplate.execute("ALTER TABLE bom MODIFY COLUMN reference LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE demande_achat MODIFY COLUMN description TEXT");
            jdbcTemplate.execute("ALTER TABLE demande_achat MODIFY COLUMN commentaire TEXT");
        } catch (DataAccessException ignored) {
            // Ignoré si les tables n'existent pas encore ou selon la base
        }

        // 1. Administrateur principal
        createOrUpdateUser("Admin", "OCP", "admin@ocp.ma", "password123", Role.ADMINISTRATEUR);

        // 2. Administrateurs / Responsables de dossiers (JPH)
        for (ResponsableDA resp : ResponsableDA.values()) {
            String email = (resp.name().charAt(0) + "." + resp.name() + "@ocpgroup.ma").toLowerCase();
            createOrUpdateUser(resp.name(), "Admin", email, "admin123", Role.ADMINISTRATEUR);
        }

        // 3. Utilisateurs Consultants
        createOrUpdateUser("Export", "Consultant", "export@jph.local", "export123", Role.CONSULTANT);
        createOrUpdateUser("Soufre", "Consultant", "soufre@jph.local", "soufre123", Role.CONSULTANT);
        createOrUpdateUser("Ammoniac", "Consultant", "ammoniac@jph.local", "ammoniac123", Role.CONSULTANT);

        log.info("Initialisation des comptes utilisateurs terminée.");
    }

    private void createOrUpdateUser(String nom, String prenom, String email, String rawPassword, Role role) {
        String cleanEmail = email.trim().toLowerCase();
        Optional<Utilisateur> existing = utilisateurRepository.findByEmailIgnoreCase(cleanEmail);

        if (existing.isPresent()) {
            Utilisateur user = existing.get();
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setMotDePasse(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setActif(true);
            utilisateurRepository.save(user);
            log.info("Compte synchronisé : {} ({})", cleanEmail, role);
        } else {
            Utilisateur user = Utilisateur.builder()
                    .nom(nom)
                    .prenom(prenom)
                    .email(cleanEmail)
                    .motDePasse(passwordEncoder.encode(rawPassword))
                    .role(role)
                    .actif(true)
                    .build();
            utilisateurRepository.save(user);
            log.info("Compte créé : {} ({})", cleanEmail, role);
        }
    }
}
