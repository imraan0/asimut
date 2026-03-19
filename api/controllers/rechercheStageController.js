/**
 * @fileoverview contrôleur des recherches de stage
 * @module controllers/rechercheStageController
 */

const { RechercheStage, Eleve } = require('../models');

/**
 * retourne toutes les recherches de stage d'un élève.
 * calcule une alerte si l'élève a 15 refus sans stage validé.
 *
 * @async
 * @function getByEleve
 * @param {import('express').Request} req 
 * @param {string} req.params.id - id de l'élève
 * @param {import('express').Response} res 
 * @returns {Promise<void>}
 *
 * @example
 * // GET /stages/recherches/1
 * // Réponse 200 : { alerte: false, message: null, recherches: [...] }
 * // Réponse 200 : { alerte: true, message: "Cet élève a contacté 15 entreprises sans succès", recherches: [...] }
 * // Réponse 404 : { message: "Élève introuvable" }
 */

const getByEleve = async (req, res) => {
    try {
        const idEleve = req.params.id;

        if (isNaN(idEleve)) {
            return res.status(400).json({ message: 'Id invalide' });
        }

        const eleve = await Eleve.findByPk(idEleve);
        if (!eleve) {
            return res.status(404).json({ message: 'Élève introuvable' });
        }

        const recherches = await RechercheStage.findAll({
            where: { eleve_id: idEleve }
        });

        const nbRefus = recherches.filter(r => //filter donne un tableau avec les condition données
            r.statut === 'refuse' || r.statut === 'entretien_refuse'
        ).length;

        const aUnStage = recherches.some(r => r.statut === 'valide'); //some vérifie si au moins un élément du tableau correspond à la condition
        const alerte = nbRefus >= 15 && !aUnStage;

        res.status(200).json({
            alerte,
            message: alerte ? 'Cet élève a contacté 15 entreprises sans succès' : null,
            recherches
        });

    } catch (error) {
        console.error('Erreur getByEleve recherches:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

/**
 * crée une nouvelle recherche de stage pour un élève.
 *
 * @async
 * @function create
 * @param {import('express').Request} req 
 * @param {string} req.params.id - id de l'élève
 * @param {Object} req.body
 * @param {string} req.body.nom_entreprise - nom de l'entreprise contactée (obligatoire)
 * @param {string} [req.body.nom_contact] - nom du contact dans l'entreprise
 * @param {string} [req.body.email_contact] - email du contact
 * @param {string} [req.body.statut] - statut de la recherche
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // POST /stages/recherches/1
 * // Body : { "nom_entreprise": "Google", "nom_contact": "Dupont", "email_contact": "dupont@google.com" }
 * // Réponse 201 : { id: 1, eleve_id: 1, nom_entreprise: "Google", statut: "non_contacte", ... }
 * // Réponse 400 : { message: "Nom de l'entreprise manquant" }
 */

const create = async (req, res) => {
    try {
        const idEleve = req.params.id;
        const { nom_entreprise, nom_contact, email_contact, statut } = req.body;

        if (!nom_entreprise) {
            return res.status(400).json({ message: 'Nom de l\'entreprise manquant' });
        }

        const eleve = await Eleve.findByPk(idEleve);
        if (!eleve) {
            return res.status(404).json({ message: 'Élève introuvable' });
        }

        const recherche = await RechercheStage.create({
            eleve_id: idEleve,
            nom_entreprise,
            nom_contact,
            email_contact,
            statut
        });

        res.status(201).json(recherche);

    } catch (error) {
        console.error('Erreur create recherche:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

/**
 * met à jour une recherche de stage.
 * tous les champs sont optionnels, seuls les champs fournis sont modifiés.
 *
 * @async
 * @function update
 * @param {import('express').Request} req 
 * @param {string} req.params.id - id de la recherche de stage
 * @param {Object} req.body 
 * @param {string} [req.body.nom_entreprise] - nom de l'entreprise
 * @param {string} [req.body.nom_contact] - nom du contact
 * @param {string} [req.body.email_contact] - email du contact
 * @param {number} [req.body.nb_lettres_envoyees] - nombre de lettres envoyées
 * @param {number} [req.body.nb_lettres_recues] - nombre de lettres reçues
 * @param {string} [req.body.date_entretien] - date de l'entretien
 * @param {string} [req.body.statut] - statut de la recherche
 * @param {string} [req.body.resultat] - résultat de la recherche
 * @param {import('express').Response} res 
 * @returns {Promise<void>}
 *
 * @example
 * // PUT /stages/recherches/1
 * // Body : { "statut": "entretien_accorde", "date_entretien": "2026-04-15" }
 * // Réponse 200 : { id: 1, statut: "entretien_accorde", ... }
 * // Réponse 404 : { message: "Recherche introuvable" }
 */

const update = async (req, res) => {
    try {
        const { id } = req.params;
        const { nom_entreprise, nom_contact, email_contact,
            nb_lettres_envoyees, nb_lettres_recues,
            date_entretien, statut, resultat } = req.body;

        const recherche = await RechercheStage.findByPk(id);
        if (!recherche) {
            return res.status(404).json({ message: 'Recherche introuvable' });
        }
        
        if (recherche.statut === 'valide') {
            return res.status(403).json({ message: 'Recherche déjà validée, modification interdite' });
        }
        // tous les champs modifiables
        const updatesRecherche = {};
        if (nom_entreprise) updatesRecherche.nom_entreprise = nom_entreprise;
        if (nom_contact) updatesRecherche.nom_contact = nom_contact;
        if (email_contact) updatesRecherche.email_contact = email_contact;
        if (nb_lettres_envoyees) updatesRecherche.nb_lettres_envoyees = nb_lettres_envoyees;
        if (nb_lettres_recues) updatesRecherche.nb_lettres_recues = nb_lettres_recues;
        if (date_entretien) updatesRecherche.date_entretien = date_entretien;
        if (statut) updatesRecherche.statut = statut;
        if (resultat) updatesRecherche.resultat = resultat;
        
        await recherche.update(updatesRecherche);
        res.status(200).json(recherche);
    } catch (error) {
        console.error('Erreur update recherche:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

/**
 * supprime une recherche de stage.
 *
 * @async
 * @function remove
 * @param {import('express').Request} req 
 * @param {string} req.params.id - id de la recherche à supprimer
 * @param {import('express').Response} res 
 * @returns {Promise<void>}
 *
 * @example
 * // DELETE /stages/recherches/1
 * // Réponse 200 : { message: "Recherche supprimée" }
 * // Réponse 404 : { message: "Recherche introuvable" }
 */

const remove = async (req, res) => {
    try {
        const { id } = req.params;
        const recherche = await RechercheStage.findByPk(id);
        if (!recherche) {
            return res.status(404).json({ message: 'Recherche introuvable' });
        }
        await recherche.destroy();
        res.status(200).json({ message: 'Recherche supprimée' });
    } catch (error) {
        console.error('Erreur remove recherche:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

module.exports = { getByEleve, create, update, remove };