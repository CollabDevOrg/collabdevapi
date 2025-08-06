package com.groupe1.collabdev_api.services;

import com.groupe1.collabdev_api.dto.ContributeurDto;
import com.groupe1.collabdev_api.dto.ProjetDto;
import com.groupe1.collabdev_api.entities.Contributeur;
import com.groupe1.collabdev_api.entities.DemandeContribution;
import com.groupe1.collabdev_api.repositories.ContributeurRepository;
import com.groupe1.collabdev_api.utilities.MappingContributeur;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ContributeurService {

    @Autowired
    DemandeContributionService demandeContributionService;
    @Autowired
    private ContributeurRepository contributeurRepository;

    public Contributeur chercherParId(int id) {
        return contributeurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contributeur non trouvée avec l'id : " + id));
    }

    public List<Contributeur> chercherTous() {
        return contributeurRepository.findAll();
    }

    public Contributeur ajouter(Contributeur contributeur) {
        return contributeurRepository.save(contributeur);
    }

    //modifier un contributeur
    public Contributeur modifier(Contributeur contributeur) {
        return contributeurRepository.save(contributeur);
    }

    //modifier un contributeur, ne marche pas
    public Contributeur modifier(int id, ContributeurDto dto) {
        Contributeur existant = contributeurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contributeur non trouvé avec l'id : " + id));

        // Mise à jour uniquement des champs non-nuls
        if (dto.getNiveau() != null) {
            existant.setNiveau(dto.getNiveau());
        }
        if (dto.getSpecialite() != null) {
            existant.setSpecialite(dto.getSpecialite());
        }
        if (dto.getType() != null) {
            existant.setType(dto.getType());
        }
        if (dto.getPieces() != 0) {
            existant.setPieces(dto.getPieces());
        }
        if (dto.getUriCv() != null) {
            existant.setUriCv(dto.getUriCv());
        }

        return contributeurRepository.save(existant);
    }

    public Boolean supprimerParId(int id) {
        //chercher d'abord par id
        Contributeur contributeur = contributeurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contributeur non trouvé" + id));
        contributeurRepository.deleteById(id);
        return true;
    }

    public ContributeurDto chercherContributeurParId(int id) {
        Optional<Contributeur> optional = contributeurRepository.findById(id);
        return optional.map(MappingContributeur::contributeurToDto)
                .orElseThrow(() -> new EntityNotFoundException("Contributeur non trouvée avec l'id : " + id));
    }

    public List<ContributeurDto> chercherTousLesContributeurs() {
        List<Contributeur> contributeurList = contributeurRepository.findAll();
        List<ContributeurDto> contributeurDtoList = new ArrayList<>();
        for (Contributeur contributeur : contributeurList) {
            contributeurDtoList.add(MappingContributeur.contributeurToDto(contributeur));
        }
        return contributeurDtoList;
    }

    // Lister ses projets
    public List<ProjetDto> chercherProjetsParContributeur(int idContributeur) {
        List<DemandeContribution> demandeContributions = demandeContributionService.chercherParIdContributeurEtc(
                idContributeur,
                true
        );
        List<ProjetDto> projets = new ArrayList<>();
        for (DemandeContribution demandeContribution : demandeContributions) {
            projets.add(demandeContribution.getProjet().toDto());
        }
        return projets;
    }

    //quitter un projet
    public int quitterUnProjet(int idContributeur, int idProjet) {
        return demandeContributionService.supprimerParContributeurEtParProjet(
                idContributeur, idProjet
        );

    }

}
