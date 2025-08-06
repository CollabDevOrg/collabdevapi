package com.groupe1.collabdev_api.services;

import com.groupe1.collabdev_api.dto.ContributeurDto;
import com.groupe1.collabdev_api.dto.request_dto.RequestTache;
import com.groupe1.collabdev_api.dto.response_dto.ResponseTache;
import com.groupe1.collabdev_api.entities.*;
import com.groupe1.collabdev_api.entities.enums.Niveau;
import com.groupe1.collabdev_api.entities.enums.NiveauTache;
import com.groupe1.collabdev_api.entities.enums.Role;
import com.groupe1.collabdev_api.exceptions.ProjectNotFoundException;
import com.groupe1.collabdev_api.exceptions.TacheNotFoundException;
import com.groupe1.collabdev_api.exceptions.UserNotFoundException;
import com.groupe1.collabdev_api.repositories.GestionnaireRepository;
import com.groupe1.collabdev_api.repositories.ProjetRepository;
import com.groupe1.collabdev_api.repositories.TacheRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TacheService {

    @Autowired
    private TacheRepository tacheRepository;
    @Autowired
    private ProjetRepository projetRepository;
    @Autowired
    private GestionnaireRepository gestionnaireRepository;
    @Autowired
    private ContributeurService contributeurService;

    @Autowired
    private ContributionService contributionService;

    @Autowired
    private ProjetService projetService;

    //Rechercher une tache dans un projet
    public Tache chercherParId(int idProjet, int tacheId) {
        Projet projet = projetRepository.findById(idProjet).orElseThrow(() -> new RuntimeException("Projet introuvable"));
        Tache tache = tacheRepository.findById(tacheId).orElseThrow(() -> new RuntimeException("Projet introuvable"));
        if (tache.getProjet() != null && tache.getProjet().getId() == projet.getId()) {
            return tache;
        }
        throw new RuntimeException("Cette tache n'existe pas dans ce projet");
//        if (projet.getTaches().get().getId() == tache.getId() ){
//            return tacheRepository.findById(idProjet).orElse(null);
//        }
    }

    //Chercher tous les taches d'un projet
    public List<ResponseTache> chercherTous(int projetId) throws RuntimeException {
        Projet projet = projetRepository.findById(projetId).orElseThrow(() -> new RuntimeException("Projet introuvable"));
        List<Tache> taches = projet.getTaches();
        List<ResponseTache> responseTaches = new ArrayList<>();
        for (Tache tache : taches) {
            responseTaches.add(tache.toResponse());
        }
        return responseTaches;
    }

    public ResponseTache ajouter(RequestTache requestTache) throws UserNotFoundException, ProjectNotFoundException {
        if (!isInRange(requestTache.getPiecesAGagner(), requestTache.getNiveau())) {
            throw new RuntimeException("Le nombre de pièce fourni n'est pas la plage");
        }
        Contributeur contributeur;
        Projet projet = projetRepository.findById(requestTache.getIdProjet())
                .orElseThrow(() -> new ProjectNotFoundException("Projet introuvable!"));
        if (requestTache.getIdContributeur() != 0) {
            contributeur = contributeurService.chercherParId(requestTache.getIdContributeur());
            if (contributeur == null) {
                throw new UserNotFoundException(Role.CONTRIBUTEUR);
            }
        } else {
            contributeur = null;
        }
        Tache tache = new Tache(
                0,
                requestTache.getTitre(),
                requestTache.getDescription(),
                requestTache.getPiecesAGagner(),
                requestTache.getDateDebut(),
                requestTache.getDateFin(),
                false,
                requestTache.getNiveau(),
                contributeur,
                projet
        );

        Gestionnaire gestionnaire = gestionnaireRepository.findById(requestTache.getIdGestionnaire())
                .orElseThrow(() -> new UserNotFoundException(Role.GESTIONNAIRE));
        // Vérification d'autorisation
        if (projet.getGestionnaire().getId() == gestionnaire.getId()) {
            // Liaison de la tâche avec le projet
            Tache tacheAjoute = tacheRepository.save(tache);
            return new ResponseTache(
                    tacheAjoute.getId(),
                    tacheAjoute.getTitre(),
                    tacheAjoute.getDescription(),
                    tacheAjoute.getDateDebut(),
                    tacheAjoute.getDateFin(),
                    tacheAjoute.getPieceAGagner(),
                    tacheAjoute.getNiveau()
            );
        }
        throw new RuntimeException("Vous n'avez pas le droit de créer une tâche pour ce projet");

    }

    public Tache modifier(int idTache, Tache tache) {
        if (tache.getId() == idTache) {
            return tacheRepository.save(tache);
        }
        throw new RuntimeException("Vous n'avez pas le droit de modifier une tâche");
    }

    public Boolean supprimerParId(int idTache, int gestionnaireId) {
        Tache tache = tacheRepository.findById(idTache).orElseThrow(() -> new RuntimeException("Projet introuvable"));
        if (tache.getProjet().getGestionnaire().getId() == gestionnaireId) {
            tacheRepository.deleteById(idTache);
            return true;
        }
        return false;
    }

    private int getPiecesAGagner(Niveau niveau) {
        switch (niveau) {
            case DEBUTANT -> {
                return 10;
            }
            case INTERMEDIAIRE -> {
                return 30;
            }
            case AVANCER -> {
                return 50;
            }
            default -> {
                return 0;
            }
        }
    }

    private boolean isInRange(int piece, NiveauTache niveauTache) {
        switch (niveauTache) {
            case SIMPLE -> {
                if (!(piece >= 1 && piece <= 10)) {
                    return false;
                }
            }
            case NOVICE -> {
                if (!(piece >= 11 && piece <= 20)) {
                    return false;
                }
            }
            case INTERMEDIAIRE -> {
                if (!(piece >= 21 && piece <= 30)) {
                    return false;
                }
            }
            case DIFFICILE -> {
                if (!(piece >= 31 && piece <= 40)) {
                    return false;
                }
            }
            case COMPLEXE -> {
                if (!(piece >= 41 && piece <= 50)) {
                    return false;
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public Boolean finirUneTache(int id) throws TacheNotFoundException {
        Tache tache = tacheRepository.findById(id).orElseThrow(
                () -> new TacheNotFoundException("Tâche introuvable!")
        );
        tache.setEstFini(true);
        tacheRepository.save(tache);
        contributionService.ajouter(
                new Contribution(
                        0,
                        false,
                        tache.getContributeur(),
                        tache.getProjet(),
                        tache
                )
        );
        return true;
    }
    public Boolean affecterContribteurToTache(int idProjet, int idTache, int idContributeur){
        Contributeur contributeur = contributeurService.chercherParId(idContributeur);
        Tache tache = chercherParId(idProjet, idTache);
        //affecter le contributeur à la tache
        tache.setContributeur(contributeur);
        tacheRepository.save(tache);
        return true;
    }
}
