/**
 * @fileoverview Contrôleur des conventions de stage
 * @module controllers/conventionController
 */

const { Convention, RechercheStage, Eleve, Professeur } = require('../models');
const PDFDocument = require('pdfkit');

/**
 * crée une convention de stage pour l'élève
 *
 * @async
 * @function create
 * @param {import('express').Request} req
 * @param {number} req.user.id - id de l'élève (extrait du token)
 * @param {Object} req.body
 * @param {string} req.body.date_debut - date de début du stage (YYYY-MM-DD)
 * @param {string} req.body.date_fin - date de fin du stage (YYYY-MM-DD)
 * @param {number} req.body.recherche_stage_id - id de la recherche de stage associée pour info entreprise
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // POST /conventions
 * // Body : { "date_debut": "2026-06-01", "date_fin": "2026-06-30", "recherche_stage_id": 1 }
 * // Réponse 201 : { id: 1, eleve_id: 3, date_debut: "2026-06-01", ... }
 * // Réponse 400 : { message: "Champs manquants" }
 * // Réponse 400 : { message: "date_fin doit être après date_debut" }
 * // Réponse 403 : { message: "Cette recherche de stage ne vous appartient pas" }
 * // Réponse 404 : { message: "Recherche de stage introuvable" }
 * // Réponse 409 : { message: "Une convention existe déjà pour cette recherche de stage" }
 */

const create = async (req, res) => {
    try {
        const { date_debut, date_fin, recherche_stage_id } = req.body;
        const eleve = await Eleve.findOne({ where: { utilisateur_id: req.user.id } }); // recupère l'id de l'élève via l'id de l'utilisateur authentifié

        if (!eleve) return res.status(404).json({ message: "Élève introuvable" });
        const eleve_id = eleve.id;

        if (!date_debut || !date_fin || !recherche_stage_id) {
            return res.status(400).json({ message: "Champs manquants" });
        }

        const debut = new Date(date_debut);
        const fin = new Date(date_fin);

        if (isNaN(debut.getTime()) || isNaN(fin.getTime())) {
            return res.status(400).json({ message: "Dates invalides" });
        }

        if (fin <= debut) {
            return res.status(400).json({ message: "Le stage ne peut pas être fini avant d'avoir commencé" });
        }

        const recherche = await RechercheStage.findByPk(recherche_stage_id);
        if (!recherche) {
            return res.status(404).json({ message: "Recherche de stage introuvable" });
        }

        // vérifie que la recherche appartient à l'élève
        if (recherche.eleve_id !== eleve_id) {
            return res.status(403).json({ message: "Cette recherche de stage ne vous appartient pas" });
        }

        // vérifie que la recherche est validée
        if (recherche.statut !== 'valide') {
            return res.status(400).json({ message: "La recherche de stage n'est pas encore validée" });
        }

        // vérifie si une convention existe déjà pour cette recherche
        const conventionExistante = await Convention.findOne({
            where: { recherche_stage_id }
        });
        if (conventionExistante) {
            return res.status(409).json({ message: "Une convention existe déjà pour cette recherche de stage" });
        }

        const convention = await Convention.create({
            eleve_id,
            recherche_stage_id,
            date_debut: debut,
            date_fin: fin,
            valide: false
        });

        res.status(201).json(convention);

    } catch (error) {
        console.error('Erreur création convention :', error);
        res.status(500).json({ message: "Erreur serveur" });
    }
};

/**
 * retourne une convention de stage par son id.
 *
 * @async
 * @function getById
 * @param {import('express').Request} req
 * @param {string} req.params.id - id convention
 * @param {number} req.user.id - id de l'utilisateur authentifié
 * @param {string} req.user.role - rôle de l'utilisateur authentifié
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // GET /conventions/1
 * // Réponse 200 : { id: 1, eleve_id: 3, date_debut: "2026-06-01", recherche_stage: { ... } }
 * // Réponse 403 : { message: "Accès interdit" }
 * // Réponse 404 : { message: "Convention introuvable" }
 */

const getById = async (req, res) => {
    try {
        const { id } = req.params;
        const role = req.user.role;

        const convention = await Convention.findByPk(id, { // info d'entreprise
            include: [{
                model: RechercheStage,
                attributes: ['nom_entreprise', 'nom_contact', 'email_contact']
            }]
        });

        if (!convention) {
            return res.status(404).json({ message: "Convention introuvable" });
        }

        if (role === 'eleve') {
            const eleve = await Eleve.findOne({ where: { utilisateur_id: req.user.id } });
            if (!eleve || convention.eleve_id !== eleve.id) {
                return res.status(403).json({ message: "Accès interdit" });
            }
        } else if (role === 'professeur') {
            const professeur = await Professeur.findOne({
                where: { utilisateur_id: req.user.id }
            });
            const eleve = await Eleve.findByPk(convention.eleve_id);
            if (!professeur || !eleve || eleve.professeur_id !== professeur.id) { // vérifie que le professeur est bien le référent de l'élève
                return res.status(403).json({ message: "Accès interdit" });
            }
        }

        res.status(200).json(convention);

    } catch (error) {
        console.error('Erreur récupération convention :', error);
        res.status(500).json({ message: "Erreur serveur" });
    }
};

/**
 * retourne toutes les conventions de stage d'un élève.
 *
 * @async
 * @function getByEleve
 * @param {import('express').Request} req
 * @param {string} req.params.id - id de l'élève
 * @param {number} req.user.id - id de l'utilisateur authentifié
 * @param {string} req.user.role - rôle de l'utilisateur authentifié
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // GET /eleves/1/conventions
 * // Réponse 200 : [{ id: 1, date_debut: "2026-06-01", recherche_stage: { nom_entreprise: "Google" } }]
 * // Réponse 403 : { message: "Accès interdit" }
 * // Réponse 404 : { message: "Élève introuvable" }
 */

const getByEleve = async (req, res) => {
    try {
        const { id } = req.params;
        const role = req.user.role;

        const eleve = await Eleve.findByPk(id);
        if (!eleve) {
            return res.status(404).json({ message: "Élève introuvable" });
        }

        // Vérifie les droits selon le rôle
        if (role === 'eleve') {
            const eleve = await Eleve.findOne({ where: { utilisateur_id: req.user.id } });
            if (!eleve || eleve.id !== parseInt(id)) {
                return res.status(403).json({ message: "Accès interdit" });
            }
        } else if (role === 'professeur') {
            const professeur = await Professeur.findOne({ // info du professeur
                where: { utilisateur_id: req.user.id }
            });
            if (!professeur || eleve.professeur_id !== professeur.id) {
                return res.status(403).json({ message: "Accès interdit" });
            }
        }
        // secretariat et proviseur = pas de vérification supplémentaire

        const conventions = await Convention.findAll({ // liste de toutes les conventions
            where: { eleve_id: id },
            include: [{
                model: RechercheStage,
                attributes: ['nom_entreprise', 'nom_contact', 'email_contact']
            }]
        });

        res.status(200).json(conventions);

    } catch (error) {
        console.error('Erreur récupération conventions :', error);
        res.status(500).json({ message: "Erreur serveur" });
    }
};

/**
 * valide une convention de stage (seulement le réferent peut la valdier)
 *
 * @async
 * @function valider
 * @param {import('express').Request} req
 * @param {string} req.params.id - id de la convention
 * @param {number} req.user.id - id de l'utilisateur authentifié
 * @param {string} req.user.role - rôle de l'utilisateur authentifié
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // PUT /conventions/1/valider
 * // Réponse 200 : { message: "Convention validée", convention: { ... } }
 * // Réponse 400 : { message: "Convention déjà validée" }
 * // Réponse 403 : { message: "Accès interdit" }
 * // Réponse 404 : { message: "Convention introuvable" }
 */

const valider = async (req, res) => {
    try {
        const { id } = req.params;
        const role = req.user.role;

        const convention = await Convention.findByPk(id);
        if (!convention) {
            return res.status(404).json({ message: "Convention introuvable" });
        }

        if (role !== 'professeur') {
            return res.status(403).json({ message: "Accès interdit" });
        }

        const professeur = await Professeur.findOne({
            where: { utilisateur_id: req.user.id }
        });

        const eleve = await Eleve.findByPk(convention.eleve_id);
        if (!professeur || !eleve || eleve.professeur_id !== professeur.id) {
            return res.status(403).json({ message: "Accès interdit" });
        }

        if (convention.valide === true) {
            return res.status(400).json({ message: "Convention déjà validée" });
        }

        convention.valide = true;
        await convention.save();

        res.status(200).json({ message: "Convention validée", convention });

    } catch (error) {
        console.error('Erreur validation convention :', error);
        res.status(500).json({ message: "Erreur serveur" });
    }
};

/**
 * génère et retourne le PDF d'une convention de stage.
 *
 * @async
 * @function generatePdf
 * @param {import('express').Request} req
 * @param {string} req.params.id - Id de la convention
 * @param {number} req.user.id - Id de l'utilisateur authentifié (extrait du token)
 * @param {string} req.user.role - Rôle de l'utilisateur authentifié
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // GET /conventions/:id/pdf
 * // Réponse 200 : fichier PDF en téléchargement
 * // Réponse 403 : { message: "Accès interdit" }
 * // Réponse 404 : { message: "Convention introuvable" }
 */

const generatePdf = async (req, res) => {
    try {
        const { id } = req.params;
        const role = req.user.role;

        // Récupère la convention avec toutes les infos nécessaires
        const convention = await Convention.findByPk(id, {
            include: [
                {
                    model: RechercheStage,
                    attributes: ['nom_entreprise', 'nom_contact', 'email_contact']
                },
                {
                    model: Eleve,
                    attributes: ['nom', 'prenom', 'identifiant']
                }
            ]
        });

        if (!convention) {
            return res.status(404).json({ message: "Convention introuvable" });
        }

        // Vérification des droits — même logique que getById
        if (role === 'eleve') {
            const eleve = await Eleve.findOne({ where: { utilisateur_id: req.user.id } });
            if (!eleve || convention.eleve_id !== eleve.id) {
                return res.status(403).json({ message: "Accès interdit" });
            }
        } else if (role === 'professeur') {
            const professeur = await Professeur.findOne({ where: { utilisateur_id: req.user.id } });
            const eleve = await Eleve.findByPk(convention.eleve_id);
            if (!professeur || !eleve || eleve.professeur_id !== professeur.id) {
                return res.status(403).json({ message: "Accès interdit" });
            }
        }

        // Headers pour que le navigateur/client comprenne que c'est un PDF à télécharger
        res.setHeader('Content-Type', 'application/pdf');
        res.setHeader('Content-Disposition', `attachment; filename="convention-${id}.pdf"`);

        // Création du document PDF
        const doc = new PDFDocument({ margin: 50 });

        // Pipe — branche le document directement sur la réponse HTTP
        // Tout ce qui est écrit dans doc sera envoyé au client en temps réel
        doc.pipe(res);

        // ── Contenu du PDF ──

        // Titre
        doc.fontSize(20).font('Helvetica-Bold').text('CONVENTION DE STAGE', { align: 'center' });
        doc.moveDown();

        // Infos établissement
        doc.fontSize(12).font('Helvetica-Bold').text('Établissement :');
        doc.font('Helvetica').text('Collège-Lycée Asimov');
        doc.moveDown();

        // Infos élève
        doc.font('Helvetica-Bold').text('Élève :');
        doc.font('Helvetica').text(`${convention.eleve.prenom} ${convention.eleve.nom}`);
        doc.text(`Identifiant : ${convention.eleve.identifiant}`);
        doc.moveDown();

        // Infos entreprise
        doc.font('Helvetica-Bold').text('Entreprise :');
        doc.font('Helvetica').text(`${convention.recherche_stage.nom_entreprise}`);
        doc.text(`Contact : ${convention.recherche_stage.nom_contact}`);
        doc.text(`Email : ${convention.recherche_stage.email_contact}`);
        doc.moveDown();

        // Dates du stage
        doc.font('Helvetica-Bold').text('Période de stage :');
        doc.font('Helvetica').text(`Du : ${new Date(convention.date_debut).toLocaleDateString('fr-FR')}`);
        doc.text(`Au : ${new Date(convention.date_fin).toLocaleDateString('fr-FR')}`);
        doc.moveDown(2);

        // Signatures
        doc.font('Helvetica-Bold').text('Signatures :', { underline: true });
        doc.moveDown();
        doc.font('Helvetica').text('Élève : ____________________', { continued: false });
        doc.moveDown();
        doc.text('Représentant légal : ____________________');
        doc.moveDown();
        doc.text('Responsable entreprise : ____________________');
        doc.moveDown();
        doc.text('Proviseur : ____________________');

        // Finalise et envoie le PDF
        doc.end();

    } catch (error) {
        console.error('Erreur génération PDF convention :', error);
        res.status(500).json({ message: "Erreur serveur" });
    }
};

module.exports = { create, getById, getByEleve, valider, generatePdf };