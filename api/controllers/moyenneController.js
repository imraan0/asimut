/**
 * @fileoverview CRUD des moyennes
 * @module controllers/moyenneController
 */

const { Moyenne, Eleve, Semestre } = require('../models');

/**
 * Retourne toutes les moyennes d'un élève avec le détail du semestre.
 *
 * @async
 * @function getByEleve
 * @param {import('express').Request} req 
 * @param {string} req.params.id - Id de l'élève
 * @param {import('express').Response} res 
 * @returns {Promise<void>}
 *
 * @example
 * // GET /eleves/1/moyennes
 * // Réponse 200 : [{ id: 1, valeur: 14.5, valide: false, semestre: { numero: 1, annee_scolaire: "2025-2026" } }]
 * // Réponse 404 : { message: "Élève introuvable" }
 */

const getByEleve = async (req, res) => {
  try {
    const idEleve = req.params.id;

    if (isNaN(idEleve)) {
      return res.status(400).json({ message: 'Id invalide' });
    }

    // Si élève connecté, vérifier qu'il ne voit que ses propres notes
    if (req.user.role === 'eleve') {
      const eleveAuth = await Eleve.findOne({ where: { utilisateur_id: req.user.id } });
      if (!eleveAuth || eleveAuth.id !== parseInt(idEleve)) {
        return res.status(403).json({ message: 'Accès interdit' });
      }
    }

    const eleve = await Eleve.findByPk(idEleve);
    if (!eleve) {
      return res.status(404).json({ message: 'Élève introuvable' });
    }

    const moyennes = await Moyenne.findAll({
      where: { eleve_id: idEleve },
      include: [{ model: Semestre }]
    });

    res.status(200).json(moyennes);

  } catch (error) {
    console.error('Erreur getByEleve:', error);
    res.status(500).json({ message: 'Erreur serveur' });
  }
};

/**
 * crée une moyenne pour un élève sur un semestre donné.
 * un élève ne peut avoir qu'une seule moyenne par semestre.
 *
 * @async
 * @function create
 * @param {import('express').Request} req 
 * @param {Object} req.body 
 * @param {number} req.body.eleve_id
 * @param {number} req.body.semestre_id 
 * @param {number} req.body.valeur - valeur de la moyenne (ex: 14.5)
 * @param {import('express').Response} res 
 * @returns {Promise<void>}
 *
 * @example
 * // POST /moyennes
 * // Body : { "eleve_id": 1, "semestre_id": 1, "valeur": 14.5 }
 * // Réponse 201 : { id: 1, eleve_id: 1, semestre_id: 1, valeur: 14.5, valide: false }
 * // Réponse 409 : { message: "Une moyenne existe déjà pour cet élève ce semestre" }
 */

const create = async (req, res) => {
  try {
    const { eleve_id, semestre_id, valeur } = req.body;

    if (!eleve_id || !semestre_id || !valeur) {
      return res.status(400).json({ message: 'Champs manquants' });
    }

    const eleve = await Eleve.findByPk(eleve_id);
    if (!eleve) {
      return res.status(404).json({ message: 'Élève introuvable' });
    }

    const semestre = await Semestre.findByPk(semestre_id);
    if (!semestre) {
      return res.status(404).json({ message: 'Semestre introuvable' });
    }

    const moyenne = await Moyenne.create({ eleve_id, semestre_id, valeur });
    res.status(201).json(moyenne);

  } catch (error) {
    if (error.name === 'SequelizeUniqueConstraintError') {
      return res.status(409).json({ message: 'Une moyenne existe déjà pour cet élève ce semestre' });
    }
    console.error('Erreur create moyenne:', error);
    res.status(500).json({ message: 'Erreur serveur' });
  }
};

/**
 * modifie la valeur d'une moyenne si elle n'a pas encore été validée par le proviseur.
 *
 * @async
 * @function update
 * @param {import('express').Request} req
 * @param {string} req.params.id - id de la moyenne à modifier, params car c'est une route dynamique
 * @param {Object} req.body 
 * @param {number} req.body.valeur - nouvelle valeur de la moyenne, body car c'est une donnée que l'on envoie au serveur
 * @param {import('express').Response} res 
 * @returns {Promise<void>}
 *
 * @example
 * // PUT /moyennes/1
 * // Body : { "valeur": 16.5 }
 * // Réponse 200 : { id: 1, valeur: 16.5, valide: false, ... }
 * // Réponse 403 : { message: "Moyenne déjà validée, modification interdite" }
 * // Réponse 404 : { message: "Moyenne introuvable" }
 */

const update = async (req, res) => {
  try {
    const { id } = req.params;
    const { valeur } = req.body;

    if (!valeur) {
      return res.status(400).json({ message: 'Valeur manquante' });
    }

    // On vérifie que la moyenne existe
    const moyenne = await Moyenne.findByPk(id);
    if (!moyenne) {
      return res.status(404).json({ message: 'Moyenne introuvable' });
    }

    // Règle métier — si le proviseur a validé, le secrétariat ne peut plus modifier
    if (moyenne.valide === true) {
      return res.status(403).json({ message: 'Moyenne déjà validée, modification interdite' });
    }

    // On met à jour uniquement la valeur
    await moyenne.update({ valeur });

    res.status(200).json(moyenne);

  } catch (error) {
    console.error('Erreur update moyenne:', error);
    res.status(500).json({ message: 'Erreur serveur' });
  }
};

/**
 * valide une moyenne, action réservée au proviseur.
 * une fois validée, la moyenne ne peut plus être modifiée par le secrétariat.
 *
 * @async
 * @function valider
 * @param {import('express').Request} req 
 * @param {string} req.params.id - id de la moyenne à valider
 * @param {import('express').Response} res 
 * @returns {Promise<void>}
 *
 * @example
 * // PUT /moyennes/1/valider
 * // Réponse 200 : { id: 1, valeur: 14.5, valide: true, ... }
 * // Réponse 400 : { message: "Moyenne déjà validée" }
 * // Réponse 404 : { message: "Moyenne introuvable" }
 */

const valider = async (req, res) => {
  try {
    const { id } = req.params;

    const moyenne = await Moyenne.findByPk(id);
    if (!moyenne) {
      return res.status(404).json({ message: 'Moyenne introuvable' });
    }

    if (moyenne.valide === true) {
      return res.status(400).json({ message: 'Moyenne déjà validée' });
    }

    await moyenne.update({ valide: true });
    res.status(200).json(moyenne);

  } catch (error) {
    console.error('Erreur valider moyenne:', error);
    res.status(500).json({ message: 'Erreur serveur' });
  }
};

module.exports = { getByEleve, create, update, valider };

//Maintenant crée `routes / moyennes.js` — tu sais faire ! 

//Les 4 routes :
//```
//GET / eleves /: id / moyennes   → getByEleve
//POST / moyennes              → create(secretariat)
//PUT / moyennes /: id          → update(secretariat)
//PUT / moyennes /: id / valider  → valider(proviseur)