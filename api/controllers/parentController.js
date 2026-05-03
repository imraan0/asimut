/**
 * @fileoverview Contrôleur des parents
 * @module controllers/parentController
 */

const { Parent, Eleve } = require('../models');

/**
 * Retourne la liste de tous les parents avec l'élève associé.
 *
 * @async
 * @function getAll
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // GET /parents
 * // Réponse 200 : [{ id: 1, nom: "Dupont", prenom: "Jean", email: "...", eleve: { nom: "Dupont", prenom: "Lucas" } }]
 */
const getAll = async (req, res) => {
    try {
        const parents = await Parent.findAll({
            include: [{ model: Eleve, attributes: ['nom', 'prenom'] }]
        });
        res.status(200).json(parents);
    } catch (error) {
        console.error('Erreur getAll parents:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

module.exports = { getAll };