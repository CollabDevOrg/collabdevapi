package com.groupe1.collabdev_api.controllers;

import com.groupe1.collabdev_api.dto.request_dto.RequestTache;
import com.groupe1.collabdev_api.dto.response_dto.ResponseTache;
import com.groupe1.collabdev_api.entities.Tache;
import com.groupe1.collabdev_api.exceptions.ProjectNotFoundException;
import com.groupe1.collabdev_api.exceptions.TacheNotFoundException;
import com.groupe1.collabdev_api.exceptions.UserNotFoundException;
import com.groupe1.collabdev_api.services.TacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/utilisateurs/gestionnaires/projets/taches")
@Tag(name = "Tache Api",
        description = "CRUD pour les taches")
@CrossOrigin(origins = "http://localhost:4200")
public class TacheController {

    @Autowired
    private TacheService tacheService;

    @Operation(summary = "pour l'ajout d'une tache")
    @PostMapping
    public ResponseEntity<?> ajouterUneTache(
            @RequestBody RequestTache requestTache
    ) {
        try {
            return
                    new ResponseEntity<>(
                            tacheService.ajouter(requestTache),
                            HttpStatus.CREATED
                    );
        } catch (UserNotFoundException | ProjectNotFoundException e) {
            return
                    new ResponseEntity<>(
                            e.getMessage(),
                            HttpStatus.NOT_FOUND
                    );
        } catch (RuntimeException e) {
            return
                    new ResponseEntity<>(
                            e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR
                    );
        }
    }

    @Operation(summary = "pour l'affichage de tous les taches")
    @GetMapping
    public ResponseEntity<?> afficherTousLesTache(@RequestParam int projetId) {
        try {
            List<ResponseTache> taches = tacheService.chercherTous(projetId);
            if (taches.isEmpty()) {
                return new ResponseEntity<>(
                        "Aucune tache dans ce projet",
                        HttpStatus.OK
                );
            }
            return new ResponseEntity<>(
                    taches,
                    HttpStatus.OK
            );
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("{tacheId}")
    public Tache afficherUneTache(@RequestParam int projetId, @PathVariable int tacheId) {
        return tacheService.chercherParId(projetId, tacheId);
    }

    @Operation(summary = "finition d'une tache")
    @PutMapping("/{id}")
    public ResponseEntity<?> finirUneTache(
            @PathVariable int id
    ) {
        try {
            return
                    new ResponseEntity<>(
                            tacheService.finirUneTache(id),
                            HttpStatus.CREATED
                    );
        } catch (RuntimeException e) {
            return
                    new ResponseEntity<>(
                            e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR
                    );
        } catch (TacheNotFoundException e) {
            return
                    new ResponseEntity<>(
                            e.getMessage(),
                            HttpStatus.NOT_FOUND
                    );
        }
    }

    @Operation(summary = "pour la suppression d'une tache")
    @DeleteMapping
    public boolean supprimerUneTache(@RequestParam int idTache, @RequestParam int idGestionnaire) {
        return tacheService.supprimerParId(idTache, idGestionnaire);
    }

    @PutMapping("/{tacheId}/contributeur")
    public ResponseEntity<?> affecterTache(
            @PathVariable int tacheId,
            @RequestParam int idProjet,
            @RequestParam int idContributeur
    ){
        try {
           return
                   new ResponseEntity<>(
                           tacheService.affecterContribteurToTache(idProjet, tacheId, idContributeur),
                           HttpStatus.OK
                   );
        }catch (RuntimeException e)
        {
            return new ResponseEntity<>(
                    "Projet ou tache ou contributeur non trouvé",
                    HttpStatus.NOT_FOUND
            );
        }
    }
}
