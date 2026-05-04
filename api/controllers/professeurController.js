/**
 * @fileoverview Contrôleur des professeurs référents
 * @module controllers/professeurController
 */

const { Professeur, Eleve, Classe, Niveau } = require('../models');
const { fn, col } = require('sequelize');

/**
 * retourne la liste des élèves dont ce professeur est référent.
 *
 * @async
 * @function getEleves
 * @param {import('express').Request} req
 * @param {string} req.params.id - Id du professeur
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // GET /professeurs/1/eleves
 * // Réponse 200 : [{ id: 1, nom: "Isik", prenom: "Imran", classe: { ... } }]
 * // Réponse 404 : { message: "Professeur introuvable" }
 */

const getEleves = async (req, res) => {
    try {
        const { id } = req.params;

        const professeur = await Professeur.findByPk(id);
        if (!professeur) {
            return res.status(404).json({ message: 'Professeur introuvable' });
        }

        const eleves = await professeur.getEleves_referents({ //select * from eleves where professeur_id = id en sequelize
            include: [ //pour avoir les infos de la classe et du niveau
                {
                    model: Classe,
                    include: [{ model: Niveau }]
                }
            ]
        });
        res.status(200).json(eleves);

    } catch (error) {
        console.error('Erreur getEleves:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

/**
 * retourne la liste de tous les professeurs avec leur nombre d'élèves référents.
 *
 * @async
 * @function getAll
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // GET /professeurs
 * // Réponse 200 : [{ id: 1, nom: "Dupont", prenom: "Jean", nb_eleves: 5 }]
 */

const getAll = async (req, res) => {
    try {
        // fn('COUNT') et col() sont des helpers Sequelize pour écrire du SQL
        // équivalent de SELECT COUNT (eleves.id) AS nb_eleves FROM professeurs

        const professeurs = await Professeur.findAll({
            subQuery: false,
            include: [{
                model: Eleve,
                as: 'eleves_referents',
                attributes: []
            }],
            attributes: [
                'id',
                'nom',
                'prenom',
                'utilisateur_id',
                [fn('COUNT', col('eleves_referents.id')), 'nb_eleves']
            ],
            group: ['professeur.id'],
            order: [['nom', 'ASC']]
        });
        res.status(200).json(professeurs);
    } catch (error) {
        console.error('Erreur getAll:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

/**
 * affecte manuellement un professeur référent à un élève.
 *
 * @async
 * @function affecter
 * @param {import('express').Request} req
 * @param {string} req.params.id - Id de l'élève
 * @param {Object} req.body
 * @param {number} req.body.professeur_id - Id du professeur à affecter
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // POST /eleves/1/referent
 * // Body : { "professeur_id": 2 }
 * // Réponse 200 : { message: "Référent affecté avec succès", eleve: { ... } }
 * // Réponse 404 : { message: "Élève introuvable" }
 */

const affecter = async (req, res) => {
    try {
        const { id } = req.params;
        const { professeur_id } = req.body;

        if (!professeur_id) {
            return res.status(400).json({ message: 'professeur_id manquant' });
        }

        const eleve = await Eleve.findByPk(id);
        if (!eleve) {
            return res.status(404).json({ message: 'Élève introuvable' });
        }

        const professeur = await Professeur.findByPk(professeur_id);
        if (!professeur) {
            return res.status(404).json({ message: 'Professeur introuvable' });
        }

        eleve.professeur_id = professeur_id;
        await eleve.save(); //envoie le UPDATE en BDD

        res.status(200).json({ message: 'Référent affecté avec succès', eleve });

    } catch (error) {
        console.error('Erreur affecter:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};


/**
 * affecte automatiquement le professeur référent avec le moins d'élèves (round-robin).
 *
 * @async
 * @function affecterAuto
 * @param {import('express').Request} req 
 * @param {string} req.params.id - Id de l'élève
 * @param {import('express').Response} res 
 * @returns {Promise<void>}
 *
 * @example
 * // POST /eleves/1/referent/auto
 * // Réponse 200 : { message: "Référent affecté automatiquement", eleve: { ... }, professeur: { ... } }
 * // Réponse 404 : { message: "Aucun professeur disponible" }
 */

const affecterAuto = async (req, res) => {
    try {
        const { id } = req.params;

        const eleve = await Eleve.findByPk(id);
        if (!eleve) {
            return res.status(404).json({ message: 'Élève introuvable' });
        }

        // limit 1 : on prend uniquement le premier
        const professeurs = await Professeur.findAll({
            subQuery: false,
            include: [{
                model: Eleve,
                as: 'eleves_referents',
                attributes: []
            }],
            attributes: [
                'id',
                'nom',
                'prenom',
                [fn('COUNT', col('eleves_referents.id')), 'nb_eleves']
            ],
            group: ['professeur.id'],
            order: [[fn('COUNT', col('eleves_referents.id')), 'ASC']],
            limit: 1
        });

        if (!professeurs.length) {
            return res.status(404).json({ message: 'Aucun professeur disponible' });
        }

        const professeurLeMoinsCharge = professeurs[0];

        // affecter ce prof à l'élève
        eleve.professeur_id = professeurLeMoinsCharge.id;
        await eleve.save(); // envoie le UPDATE en BDD

        res.status(200).json({
            message: 'Référent affecté automatiquement',
            eleve,
            professeur: professeurLeMoinsCharge
        });

    } catch (error) {
        console.error('Erreur affecterAuto:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

/**
 * Retourne le professeur connecté.
 * @async
 * @function getMe
 */
const getMe = async (req, res) => {
    try {
        const professeur = await Professeur.findOne({
            where: { utilisateur_id: req.user.id }
        });
        if (!professeur) {
            return res.status(404).json({ message: 'Professeur introuvable' });
        }
        res.status(200).json(professeur);
    } catch (error) {
        console.error('Erreur getMe:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

module.exports = { getEleves, affecter, affecterAuto, getAll, getMe };