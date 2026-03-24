/**
 * @fileoverview Contrôleur des options
 * @module controllers/optionController
 */

const { Option, Eleve } = require('../models');

/**
 * retourne toutes les options disponibles
 *
 * @async
 * @function getAll
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // GET /options
 * // Réponse 200 : [{ id: 1, nom: "Espagnol", type: "langue" }, ...]
 */

const getAll = async (req, res) => {
    try {
        const options = await Option.findAll();
        res.status(200).json(options);
    } catch (error) {
        console.error('Erreur getAll options:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

/**
 * retourne les options d'un élève
 *
 * @async
 * @function getByEleve
 * @param {import('express').Request} req
 * @param {string} req.params.id - Id de l'élève
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // GET /eleves/1/options
 * // Réponse 200 : [{ id: 1, nom: "Espagnol", type: "langue" }]
 * // Réponse 404 : { message: "Élève introuvable" }
 */

const getByEleve = async (req, res) => {
    try {
        const { id } = req.params;

        const eleve = await Eleve.findByPk(id, {
            include: [{ model: Option }]
        });
        if (!eleve) {
            return res.status(404).json({ message: 'Élève introuvable' });
        }

        const options = await eleve.getOptions();
        res.status(200).json(options);

    } catch (error) {
        console.error('Erreur getByEleve options:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

/**
 * crée une nouvelle option dans le catalogue.
 *
 * @async
 * @function create
 * @param {import('express').Request} req 
 * @param {Object} req.body 
 * @param {string} req.body.nom - Nom de l'option (ex: "Espagnol")
 * @param {string} req.body.type - Type de l'option ("langue" ou "technique")
 * @param {import('express').Response} res 
 * @returns {Promise<void>}
 *
 * @example
 * // POST /options
 * // Body : { "nom": "Espagnol", "type": "langue" }
 * // Réponse 201 : { id: 1, nom: "Espagnol", type: "langue" }
 * // Réponse 400 : { message: "Type invalide" }
 */

const create = async (req, res) => {
    try {
        const { nom, type } = req.body;

        if (!nom || !type) {
            return res.status(400).json({ message: 'Champs manquants' });
        }

        if (type !== 'langue' && type !== 'technique') {
            return res.status(400).json({ message: 'Type invalide' });
        }

        const option = await Option.create({ nom, type });
        res.status(201).json(option);

    } catch (error) {
        console.error('Erreur create option:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

/**
 * affecte une option à un élève.
 * un élève ne peut pas avoir plus de 2 options.
 *
 * @async
 * @function affecter
 * @param {import('express').Request} req 
 * @param {Object} req.body 
 * @param {number} req.body.eleve_id - Id de l'élève
 * @param {number} req.body.option_id - Id de l'option
 * @param {import('express').Response} res 
 * @returns {Promise<void>}
 *
 * @example
 * // POST /options/affecter
 * // Body : { "eleve_id": 1, "option_id": 1 }
 * // Réponse 201 : { message: "Option affectée avec succès" }
 * // Réponse 400 : { message: "Maximum 2 options atteint" }
 * // Réponse 409 : { message: "Élève déjà inscrit à cette option" }
 */

const affecter = async (req, res) => {
    try {
        const { eleve_id, option_id } = req.body;

        if (!eleve_id || !option_id) {
            return res.status(400).json({ message: 'Champs manquants' });
        }

        const eleve = await Eleve.findByPk(eleve_id);
        if (!eleve) {
            return res.status(404).json({ message: 'Élève introuvable' });
        }

        const option = await Option.findByPk(option_id);
        if (!option) {
            return res.status(404).json({ message: 'Option introuvable' });
        }

        const nbOptions = await eleve.countOptions();
        if (nbOptions >= 2) {
            return res.status(400).json({ message: 'Maximum 2 options atteint' });
        }

        // Vérifier doublon
        const options = await eleve.getOptions({ where: { id: option_id } });
        if (options.length > 0) {
            return res.status(409).json({ message: 'Élève déjà inscrit à cette option' });
        }

        await eleve.addOption(option); //addOption car relation Many-to-Many, avec create il ne sait pas que c'est une table d'association
        res.status(201).json({ message: 'Option affectée avec succès' });

    } catch (error) {
        console.error('Erreur affecter option:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

/**
 * retire une option d'un élève
 *
 * @async
 * @function retirer
 * @param {import('express').Request} req 
 * @param {Object} req.body 
 * @param {number} req.body.eleve_id - Id de l'élève
 * @param {number} req.body.option_id - Id de l'option
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // DELETE /options/retirer
 * // Body : { "eleve_id": 1, "option_id": 1 }
 * // Réponse 200 : { message: "Option retirée avec succès" }
 * // Réponse 404 : { message: "Élève non inscrit à cette option" }
 */

const retirer = async (req, res) => {
    try {
        const { eleve_id, option_id } = req.body;

        if (!eleve_id || !option_id) {
            return res.status(400).json({ message: 'Champs manquants' });
        }

        const eleve = await Eleve.findByPk(eleve_id);
        if (!eleve) {
            return res.status(404).json({ message: 'Élève introuvable' });
        }

        const option = await Option.findByPk(option_id);
        if (!option) {
            return res.status(404).json({ message: 'Option introuvable' });
        }

        const hasOption = await eleve.hasOption(option);
        if (!hasOption) {
            return res.status(404).json({ message: 'Élève non inscrit à cette option' });
        }

        await eleve.removeOption(option);
        res.status(200).json({ message: 'Option retirée avec succès' });

    } catch (error) {
        console.error('Erreur retirer option:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

/**
 * supprime une option du catalogue
 * supprime automatiquement toutes les liaisons élève-option associées (CASCADE)
 *
 * @async
 * @function remove
 * @param {import('express').Request} req 
 * @param {string} req.params.id - Id de l'option à supprimer
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // DELETE /options/1
 * // Réponse 200 : { message: "Option supprimée" }
 * // Réponse 404 : { message: "Option introuvable" }
 */

const remove = async (req, res) => {
    try {
        const { id } = req.params;

        const option = await Option.findByPk(id);
        if (!option) {
            return res.status(404).json({ message: 'Option introuvable' });
        }

        await option.destroy();
        res.status(200).json({ message: 'Option supprimée' });

    } catch (error) {
        console.error('Erreur remove option:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
}


module.exports = { getAll, getByEleve, create, affecter, retirer, remove };