package com.groupe1.collabdev_api.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.groupe1.collabdev_api.dto.response_dto.ResponseTache;
import com.groupe1.collabdev_api.entities.enums.NiveauTache;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "taches")
public class Tache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int pieceAGagner;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Column(nullable = false)
    private boolean estFini = false;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NiveauTache niveau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contributeur")
    private Contributeur contributeur;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "id_projet", nullable = false)
    private Projet projet;

    public ResponseTache toResponse() {
        return new ResponseTache(
                this.id,
                this.titre,
                this.description,
                this.dateDebut,
                this.dateFin,
                this.pieceAGagner,
                this.niveau
        );
    }
}
