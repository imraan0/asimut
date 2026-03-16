/**
 * @fileoverview CRUD des élèves
 * @module controllers/eleveController
 */

const { Eleve, Classe, Niveau, Professeur, Utilisateur } = require('../models');
const { sequelize } = require('../config/database');
const bcrypt = require('bcrypt');

/**
 * Retourne la liste complète des élèves avec leur classe, niveau et prof référent.
 *
 * @async
 * @function getAll
 * @param {import('express').Request} req - Requête Express
 * @param {import('express').Response} res - Réponse Express
 * @returns {Promise<void>}
 *
 * @example
 * // GET /eleves
 * // Réponse 200 : [{ id: 1, nom: "Dujardin", prenom: "Jean", ... }, { id: 2, nom: "Norris", prenom: "Chuck", ... }]
 */

const getAll = async (req, res) => {
    try {
        const eleves = await Eleve.findAll({
            include: [
                {
                    model: Classe,
                    include: [{ model: Niveau }]
                },
                {
                    model: Professeur,
                    as: 'referent',
                    attributes: ['nom', 'prenom']
                }
            ]
        });

        res.status(200).json(eleves);
    } catch (error) {
        console.error('Erreur getAll élèves:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

/**
 * Retourne un élève avec sa classe, niveau et prof référent.
 *
 * @async
 * @function getById
 * @param {import('express').Request} req - Requête Express
 * @param {string} req.params.id - Id de l'élève
 * @param {import('express').Response} res - Réponse Express
 * @returns {Promise<void>}
 *
 * @example
 * // GET /eleves/1
 * // Réponse 200 : { id: 1, nom: "Dujardin", prenom: "Jean", ... }
 * // Réponse 404 : { message: "Élève introuvable" }
 */

const getById = async (req, res) => {
    try {
        const eleve = await Eleve.findByPk(req.params.id, {
            include: [
                {
                    model: Classe,
                    include: [{ model: Niveau }]
                },
                {
                    model: Professeur,
                    as: 'referent',
                    attributes: ['nom', 'prenom']
                }
            ]
        })
        if (!eleve) {
            return res.status(404).json({ message: 'Élève introuvable' });
        }
        res.status(200).json(eleve);
    }
    catch (error) {
        console.error('Erreur getById élèves:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

/**
 * Crée un nouvel élève et son compte utilisateur associé.
 * Utilise une transaction pour garantir l'intégrité des données :
 * si une étape échoue, tout est annulé.
 *
 * @async
 * @function create
 * @param {import('express').Request} req - Requête Express
 * @param {Object} req.body 
 * @param {string} req.body.email 
 * @param {string} req.body.mot_de_passe
 * @param {number} req.body.classe_id 
 * @param {string} req.body.nom
 * @param {string} req.body.prenom 
 * @param {string} req.body.identifiant
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // POST /eleves
 * // Body : { "email": "imran@asimov.fr", "mot_de_passe": "Secret1!", "classe_id": 1, "nom": "Isik", "prenom": "Imran", "identifiant": "ISI001" }
 * // Réponse 201 : { message: "Élève créé avec succès", eleve: { id: 1, ... } }
 */

const create = async (req, res) => {

    // On démarre une transaction, tout ce qui suit est lié :
    // si une étape plante, tout sera annulé via rollback
    const t = await sequelize.transaction();

    try {
        const { email, mot_de_passe, classe_id, nom, prenom, identifiant } = req.body;

        if (!email || !mot_de_passe || !classe_id || !nom || !prenom || !identifiant) {
            return res.status(400).json({ message: 'Champs manquants' });
        }

        const hashedPassword = await bcrypt.hash(mot_de_passe, 10);

        // { transaction: t } lie cette opération à notre transaction
        const utilisateur = await Utilisateur.create({
            email,
            mot_de_passe: hashedPassword,
            role: 'eleve'
        }, { transaction: t });

        // { transaction: t } lie cette opération à la même transaction
        const eleve = await Eleve.create({
            utilisateur_id: utilisateur.id,  // clé étrangère vers l'utilisateur créé juste avant
            classe_id,
            nom,
            prenom,
            identifiant
        }, { transaction: t });

        // Les deux insert sont confirmés
        await t.commit();

        res.status(201).json({
            message: 'Élève créé avec succès',
            eleve  // on renvoie l'élève créé pour que le client puisse l'utiliser
        });

    } catch (error) {
        // Si l'utilisateur avait été créé mais pas l'élève, il sera supprimé (rollback, on annule tout)
        await t.rollback();
        console.error('Erreur create élèves:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

/**
 * Met à jour les informations d'un élève et/ou son compte utilisateur.
 * Utilise une transaction pour garantir l'intégrité des données.
 *
 * @async
 * @function update
 * @param {import('express').Request} req 
 * @param {string} req.params.id - id eleve
 * @param {Object} req.body 
 * @param {string} [req.body.email] - nouvel email
 * @param {string} [req.body.mot_de_passe] - nouveau mot de passe en clair
 * @param {string} [req.body.nom] - nouveau nom
 * @param {string} [req.body.prenom]  - nouveau prénom
 * @param {number} [req.body.classe_id] - nouvel id de classe
 * @param {string} [req.body.identifiant]  - nouvel identifiant
 * @param {import('express').Response} res 
 * @returns {Promise<void>}
 *
 * @example
 * // PUT /eleves/1
 * // Body : { "nom": "Isik", "classe_id": 2 }
 * // Réponse 200 : { message: "Élève mis à jour avec succès", eleve: { ... } }
 * // Réponse 404 : { message: "Élève introuvable" }
 */

const update = async (req, res) => {

    const t = await sequelize.transaction();

    try {
        const { id } = req.params;

        const { email, mot_de_passe, nom, prenom, classe_id, identifiant } = req.body;

        // On vérifie que l'élève existe avant de faire quoi que ce soit
        const eleve = await Eleve.findByPk(id, { transaction: t });
        if (!eleve) {
            await t.rollback(); // on annule la transaction
            return res.status(404).json({ message: 'Élève introuvable' });
        }

        // mise à jour de la table utilisateur
        // on met à jour que les champs fournis dans le body
        const updatesUtilisateur = {};
        if (email) updatesUtilisateur.email = email;
        if (mot_de_passe) updatesUtilisateur.mot_de_passe = await bcrypt.hash(mot_de_passe, 10);

        // on ne fait la requête que si au moins un champ utilisateur est modifié
        if (Object.keys(updatesUtilisateur).length > 0) {
            await Utilisateur.update(updatesUtilisateur, {
                where: { id: eleve.utilisateur_id },
                transaction: t
            });
        }

        // pareil pour la table eleve
        const updatesEleve = {};
        if (nom) updatesEleve.nom = nom;
        if (prenom) updatesEleve.prenom = prenom;
        if (classe_id) updatesEleve.classe_id = classe_id;
        if (identifiant) updatesEleve.identifiant = identifiant;

        if (Object.keys(updatesEleve).length > 0) {
            await eleve.update(updatesEleve, { transaction: t });
        }

        // on valide si tout s'est bien passé
        await t.commit();

        res.status(200).json({
            message: 'Élève mis à jour avec succès',
            eleve
        });

    } catch (error) {
        await t.rollback();
        console.error('Erreur update élève:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};

/**
 * supprime un élève et son compte utilisateur associé
 * la suppression de l'utilisateur entraîne automatiquement la suppression de l'élève grâce au ON DELETE CASCADE
 *
 * @async
 * @function remove
 * @param {import('express').Request} req 
 * @param {string} req.params.id - id de l'élève
 * @param {import('express').Response} res 
 * @returns {Promise<void>}
 *
 * @example
 * // DELETE /eleves/1
 * // réponse 200 : { message: "Élève supprimé avec succès" }
 * // réponse 404 : { message: "Élève introuvable" }
 */

const remove = async (req, res) => {
    try {
        const { id } = req.params;

        const eleve = await Eleve.findByPk(id);
        if (!eleve) {
            return res.status(404).json({ message: 'Élève introuvable' });
        }

        // on supprime l'utilisateur lié à cet élève avec destroy (sequelize)
        await Utilisateur.destroy({
            where: { id: eleve.utilisateur_id }
        });

        res.status(200).json({ message: 'Élève supprimé avec succès' });

    } catch (error) {
        console.error('Erreur remove élève:', error);
        res.status(500).json({ message: 'Erreur serveur' });
    }
};


/**
 * importe une liste d'élèves depuis un fichier CSV.
 * le fichier est supprimé après traitement (du au RGPD).
 * format CSV attendu : nom,prenom,email,mot_de_passe,identifiant,classe_id
 *
 * @async
 * @function importCSV
 * @param {import('express').Request} req - 
 * @param {import('express').Response} res - 
 * @returns {Promise<void>}
 *
 * @example
 * // POST /eleves/import
 * // Body : form-data, champ "fichier" avec le fichier CSV
 * // Réponse 200 : { importes: 48, erreurs: [{ ligne: 3, raison: "Email déjà existant" }] }
 */

const importCSV = async (req, res) => {
  // On vérifie qu'un fichier a bien été uploadé par multer
  if (!req.file) {
    return res.status(400).json({ message: 'Aucun fichier CSV fourni' });
  }

  // fs permet de lire et supprimer des fichiers sur le serveur
  const fs = require('fs');
  const { parse } = require('csv-parse');

  // compteurs pour le rapport final d'erreurs
  const rapport = { importes: 0, erreurs: [] };

  try {
    const contenu = fs.readFileSync(req.file.path, 'utf8');

    // csv-parse transforme le texte CSV en tableau d'objets JavaScript
    // columns: true -> utilise la première ligne comme noms de colonnes
    // skip_empty_lines: true -> ignore les lignes vides
    const records = await new Promise((resolve, reject) => {
      parse(contenu, { columns: true, skip_empty_lines: true }, (err, records) => {
        if (err) reject(err);
        else resolve(records);
      });
    });

    // on traite chaque ligne du CSV une par une
    for (let i = 0; i < records.length; i++) {
      const ligne = records[i];
      const numeroLigne = i + 2; // +2 car ligne 1 = headers

      // transaction par élève — si un élève plante, les autres continuent
      const t = await sequelize.transaction();
      try {
        const hashedPassword = await bcrypt.hash(ligne.mot_de_passe, 10);

        const utilisateur = await Utilisateur.create({
          email:        ligne.email,
          mot_de_passe: hashedPassword,
          role:         'eleve'
        }, { transaction: t });

        await Eleve.create({
          utilisateur_id: utilisateur.id,
          classe_id:      ligne.classe_id,
          nom:            ligne.nom,
          prenom:         ligne.prenom,
          identifiant:    ligne.identifiant
        }, { transaction: t });

        await t.commit();
        rapport.importes++;

      } catch (erreurLigne) {
        // on annule uniquement cet élève, pas tout l'import
        await t.rollback();

        // on détermine le message d'erreur selon le type
        let raison = 'Erreur inconnue';
        if (erreurLigne.name === 'SequelizeUniqueConstraintError') {
          raison = 'Email ou identifiant déjà existant';
        } else if (erreurLigne.name === 'SequelizeForeignKeyConstraintError') {
          raison = `classe_id ${ligne.classe_id} introuvable`;
        }

        rapport.erreurs.push({ ligne: numeroLigne, email: ligne.email, raison });
      }
    }

    // on supprime le fichier CSV après traitement (rgpd)
    fs.unlinkSync(req.file.path);

    res.status(200).json(rapport);

  } catch (error) {
    // si le fichier existe encore on le supprime quand même
    if (req.file && require('fs').existsSync(req.file.path)) {
      require('fs').unlinkSync(req.file.path);
    }
    console.error('Erreur importCSV:', error);
    res.status(500).json({ message: 'Erreur serveur' });
  }
};

module.exports = { getAll, getById, create, update, remove, importCSV };