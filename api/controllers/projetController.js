/**
 * @fileoverview Contrôleur des projets
 * @module controllers/projetController
 */

const { Projet, Eleve, Participation } = require('../models');
const { sequelize } = require('../config/database');

/**
 * Retourne la liste des projets validés avec leurs participants.
 *
 * @async
 * @function getAll
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // GET /projets
 * // Réponse 200 : [{ id: 1, nom: "Sortie musée", valide: true, participations: [...] }]
 */

const getAll = async (req, res) => {
    try {
        const projets = await Projet.findAll({
            where: { valide: true },
            include: [
                {
                    model: Participation,
                    include: [{ model: Eleve, attributes: ['nom', 'prenom'] }]
                }
            ]
        });

        res.status(200).json(projets);
    } catch (error) {
        console.error('Erreur getAll projets:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

/**
 * retourne tous les projets (validés et non validés) - accès secrétariat uniquement.
 *
 * @async
 * @function getAllAdmin
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // GET /projets/all
 * // Réponse 200 : [{ id: 1, nom: "Sortie musée", valide: false, participations: [...] }]
 */

const getAllAdmin = async (req, res) => {
    try {
        const projets = await Projet.findAll({
            include: [
                {
                    model: Participation,
                    include: [{ model: Eleve, attributes: ['nom', 'prenom'] }]
                }
            ]
        });

        res.status(200).json(projets);
    } catch (error) {
        console.error('Erreur getAllAdmin projets:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

/**
 * crée un nouveau projet et inscrit automatiquement l'élève créateur comme responsable.
 * un élève ne peut pas être responsable de plus de 3 projets.
 * utilise une transaction car on touche deux tables : projet et participation.
 *
 * @async
 * @function create
 * @param {import('express').Request} req
 * @param {Object} req.body
 * @param {number} req.body.eleve_id - Id de l'élève créateur
 * @param {string} req.body.nom - Nom du projet
 * @param {string} req.body.objectif - Objectif du projet
 * @param {string} req.body.date_debut - Date de début
 * @param {string} req.body.date_fin - Date de fin
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // POST /projets
 * // Body : { "eleve_id": 1, "nom": "Sortie musée", "objectif": "...", "date_debut": "2026-04-01", "date_fin": "2026-04-01" }
 * // Réponse 201 : { id: 1, nom: "Sortie musée", valide: false, ... }
 * // Réponse 400 : { message: "Maximum 3 projets atteint" }
 */

const create = async (req, res) => {
  const t = await sequelize.transaction();
  try {
    const { eleve_id, nom, objectif, date_debut, date_fin } = req.body;

    if (!eleve_id || !nom || !objectif || !date_debut || !date_fin) {
      await t.rollback();
      return res.status(400).json({ message: 'Champs manquants' });
    }

    const eleve = await Eleve.findByPk(eleve_id);
    if (!eleve) {
      await t.rollback();
      return res.status(404).json({ message: 'Élève introuvable' });
    }

    // vérif max 3 projets par élève
    const nbProjets = await Participation.count({
      where: { eleve_id, est_responsable: true }
    });
    if (nbProjets >= 3) {
      await t.rollback();
      return res.status(400).json({ message: 'Maximum 3 projets atteint' });
    }

    // création du projet
    const projet = await Projet.create({
      nom, objectif, date_debut, date_fin
    }, { transaction: t });

    // l'élève créateur devient automatiquement responsable
    await Participation.create({
      eleve_id,
      projet_id: projet.id,
      est_responsable: true,
      date_debut
    }, { transaction: t });

    await t.commit();
    res.status(201).json(projet);

  } catch (error) {
    await t.rollback();
    console.error('Erreur create projet:', error);
    res.status(500).json({ message: 'Erreur serveur' });
  }
};

/**
 * valide un projet — action réservée au secrétariat.
 * une fois validé, le projet devient visible pour tous les élèves.
 *
 * @async
 * @function valider
 * @param {import('express').Request} req
 * @param {string} req.params.id - Id du projet à valider
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // PUT /projets/1/valider
 * // Réponse 200 : { id: 1, nom: "Sortie musée", valide: true, ... }
 * // Réponse 400 : { message: "Projet déjà validé" }
 * // Réponse 404 : { message: "Projet introuvable" }
 */

const valider = async (req, res) => {
  try {
    const { id } = req.params;

    const projet = await Projet.findByPk(id);
    if (!projet) {
      return res.status(404).json({ message: 'Projet introuvable' });
    }

    if (projet.valide) {
      return res.status(400).json({ message: 'Projet déjà validé' });
    }

    projet.valide = true;
    await projet.save();

    res.status(200).json(projet);

  } catch (error) {
    console.error('Erreur valider projet:', error);
    res.status(500).json({ message: 'Erreur serveur' });
  }
};

/**
 * inscrit un élève à un projet existant et validé.
 *
 * @async
 * @function participer
 * @param {import('express').Request} req
 * @param {Object} req.body
 * @param {number} req.body.eleve_id - Id de l'élève
 * @param {number} req.body.projet_id - Id du projet
 * @param {string} req.body.date_debut - Date de début de participation
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // POST /projets/participer
 * // Body : { "eleve_id": 1, "projet_id": 1, "date_debut": "2026-04-01" }
 * // Réponse 201 : { message: "Participation ajoutée" }
 * // Réponse 400 : { message: "Projet non validé" }
 * // Réponse 409 : { message: "Déjà inscrit" }
 */

const participer = async (req, res) => {
  try {
    const { eleve_id, projet_id, date_debut } = req.body;

    if (!eleve_id || !projet_id || !date_debut) {
      return res.status(400).json({ message: 'Champs manquants' });
    }

    const eleve = await Eleve.findByPk(eleve_id);
    if (!eleve) {
      return res.status(404).json({ message: 'Élève introuvable' });
    }

    const projet = await Projet.findByPk(projet_id);
    if (!projet) {
      return res.status(404).json({ message: 'Projet introuvable' });
    }

    if (!projet.valide) {
      return res.status(400).json({ message: 'Projet non validé' });
    }

    const participationExistante = await Participation.findOne({
      where: { eleve_id, projet_id }
    });
    if (participationExistante) {
      return res.status(409).json({ message: 'Élève déjà inscrit à ce projet' });
    }

    await Participation.create({
      eleve_id,
      projet_id,
      est_responsable: false,
      date_debut
    });

    res.status(201).json({ message: 'Participation ajoutée' });

  } catch (error) {
    console.error('Erreur participer projet:', error);
    res.status(500).json({ message: 'Erreur serveur' });
  }
};

/**
 * retire un élève d'un projet.
 *
 * @async
 * @function retirerParticipation
 * @param {import('express').Request} req
 * @param {string} req.params.projetId - Id du projet
 * @param {string} req.params.eleveId - Id de l'élève
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // DELETE /projets/1/participer/2
 * // Réponse 200 : { message: "Participation supprimée" }
 * // Réponse 404 : { message: "Participation introuvable" }
 */

const retirerParticipation = async (req, res) => {
  try {
    const { projetId, eleveId } = req.params;

    const participation = await Participation.findOne({
      where: { projet_id: projetId, eleve_id: eleveId }
    });
    if (!participation) {
      return res.status(404).json({ message: 'Participation introuvable' });
    }

    // On empêche de retirer le responsable du projet
    if (participation.est_responsable) {
      return res.status(403).json({ message: 'Impossible de retirer le responsable du projet' });
    }

    await participation.destroy();
    res.status(200).json({ message: 'Participation supprimée' });

  } catch (error) {
    console.error('Erreur retirerParticipation:', error);
    res.status(500).json({ message: 'Erreur serveur' });
  }
};

/**
 * supprime un projet.
 *
 * @async
 * @function remove
 * @param {import('express').Request} req
 * @param {string} req.params.id - Id du projet à supprimer
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // DELETE /projets/1
 * // Réponse 200 : { message: "Projet supprimé" }
 * // Réponse 404 : { message: "Projet introuvable" }
 */

const remove = async (req, res) => {
  try {
    const { id } = req.params;

    const projet = await Projet.findByPk(id);
    if (!projet) {
      return res.status(404).json({ message: 'Projet introuvable' });
    }

    await projet.destroy();

    res.status(200).json({ message: 'Projet supprimé' });

  } catch (error) {
    console.error('Erreur remove projet:', error);
    res.status(500).json({ message: 'Erreur serveur' });
  }
};

/**
 * Retourne les projets auxquels l'élève connecté participe.
 * @async
 * @function getMesProjets
 */
const getMesProjets = async (req, res) => {
    try {
        const eleve = await Eleve.findOne({ where: { utilisateur_id: req.user.id } });
        if (!eleve) {
            return res.status(404).json({ message: 'Élève introuvable' });
        }

        const projets = await Projet.findAll({
            include: [{
                model: Participation,
                where: { eleve_id: eleve.id },
                include: [{ model: Eleve, attributes: ['nom', 'prenom'] }]
            }]
        });

        res.status(200).json(projets);
    } catch (error) {
        console.error('Erreur getMesProjets:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

module.exports = { getAll, getAllAdmin, create, valider, remove, participer, retirerParticipation, getMesProjets };
