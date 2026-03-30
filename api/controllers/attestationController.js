/**
 * @fileoverview Contrôleur des attestations de stage
 * @module controllers/attestationController
 */

const { Attestation, Convention, RechercheStage, Eleve, Professeur } = require('../models');
const PDFDocument = require('pdfkit');
const path = require('path');
const fs = require('fs');

/**
 * retourne une attestation de stage par son id.
 *
 * @async
 * @function getById
 * @param {import('express').Request} req
 * @param {string} req.params.id - id de l'attestation
 * @param {number} req.user.id - id de l'utilisateur authentifié
 * @param {string} req.user.role - rôle de l'utilisateur authentifié
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // GET /attestations/1
 * // Réponse 200 : { id: 1, convention_id: 2, pdf_path: null, date_signature: null }
 * // Réponse 403 : { message: "Accès interdit" }
 * // Réponse 404 : { message: "Attestation introuvable" }
 */

const getById = async (req, res) => {
    try {
        const { id } = req.params;
        const role = req.user.role;

        const attestation = await Attestation.findByPk(id, {
            include: [{
                model: Convention,
                include: [
                    { model: Eleve },
                    { model: RechercheStage, attributes: ['nom_entreprise', 'nom_contact', 'email_contact'] }
                ]
            }]
        });

        if (!attestation) {
            return res.status(404).json({ message: "Attestation introuvable" });
        }

        const eleve = attestation.convention.eleve;

        if (role === 'eleve') {
            const eleveAuth = await Eleve.findOne({ where: { utilisateur_id: req.user.id } });
            if (!eleveAuth || eleve.id !== eleveAuth.id) {
                return res.status(403).json({ message: "Accès interdit" });
            }
        } else if (role === 'professeur') {
            const professeur = await Professeur.findOne({ where: { utilisateur_id: req.user.id } });
            if (!professeur || eleve.professeur_id !== professeur.id) {
                return res.status(403).json({ message: "Accès interdit" });
            }
        }

        res.status(200).json(attestation);
    } catch (error) {
        console.error('Erreur récupération attestation :', error);
        res.status(500).json({ message: "Erreur serveur" });
    }
};

/**
 * génère et retourne le PDF de l'attestation.
 * retourne le PDF signé si disponible, sinon le PDF vierge généré par pdfkit.
 *
 * @async
 * @function generatePdf
 * @param {import('express').Request} req
 * @param {string} req.params.id - id de l'attestation
 * @param {number} req.user.id - id de l'utilisateur authentifié
 * @param {string} req.user.role - rôle de l'utilisateur authentifié
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // GET /attestations/1/pdf
 * // Réponse 200 : fichier PDF en téléchargement
 * // Réponse 403 : { message: "Accès interdit" }
 * // Réponse 404 : { message: "Attestation introuvable" }
 */

const generatePdf = async (req, res) => {
    try {
        const { id } = req.params;
        const role = req.user.role;

        const attestation = await Attestation.findByPk(id, {
            include: [{
                model: Convention,
                include: [
                    { model: Eleve },
                    { model: RechercheStage, attributes: ['nom_entreprise', 'nom_contact', 'email_contact'] }
                ]
            }]
        });

        if (!attestation) {
            return res.status(404).json({ message: "Attestation introuvable" });
        }

        const eleve = attestation.convention.eleve;
        const recherche = attestation.convention.recherche_stage;
        const convention = attestation.convention;

        if (role === 'eleve') {
            const eleveAuth = await Eleve.findOne({ where: { utilisateur_id: req.user.id } });
            if (!eleveAuth || eleve.id !== eleveAuth.id) {
                return res.status(403).json({ message: "Accès interdit" });
            }
        } else if (role === 'professeur') {
            const professeur = await Professeur.findOne({ where: { utilisateur_id: req.user.id } });
            if (!professeur || eleve.professeur_id !== professeur.id) {
                return res.status(403).json({ message: "Accès interdit" });
            }
        }

        // Si PDF signé disponible → l'envoyer directement
        if (attestation.pdf_path) {
            return res.sendFile(path.resolve(attestation.pdf_path));
        }

        // Sinon → générer le PDF vierge
        res.setHeader('Content-Type', 'application/pdf');
        res.setHeader('Content-Disposition', `attachment; filename="attestation-${id}.pdf"`);

        const doc = new PDFDocument({ margin: 50 });
        doc.pipe(res);

        doc.fontSize(20).font('Helvetica-Bold').text('ATTESTATION DE STAGE', { align: 'center' });
        doc.moveDown();

        doc.fontSize(12).font('Helvetica-Bold').text('Élève :');
        doc.font('Helvetica').text(`${eleve.prenom} ${eleve.nom}`);
        doc.text(`Identifiant : ${eleve.identifiant}`);
        doc.moveDown();

        doc.font('Helvetica-Bold').text('Entreprise :');
        doc.font('Helvetica').text(`${recherche.nom_entreprise}`);
        doc.text(`Contact : ${recherche.nom_contact}`);
        doc.text(`Email : ${recherche.email_contact}`);
        doc.moveDown();

        doc.font('Helvetica-Bold').text('Période de stage :');
        doc.font('Helvetica').text(`Du : ${new Date(convention.date_debut).toLocaleDateString('fr-FR')}`);
        doc.text(`Au : ${new Date(convention.date_fin).toLocaleDateString('fr-FR')}`);
        doc.moveDown(2);

        doc.font('Helvetica-Bold').text('Signatures :', { underline: true });
        doc.moveDown();
        doc.font('Helvetica').text('Élève : ____________________');
        doc.moveDown();
        doc.text('Représentant légal : ____________________');
        doc.moveDown();
        doc.text('Responsable entreprise : ____________________');

        doc.end();

    } catch (error) {
        console.error('Erreur génération attestation :', error);
        res.status(500).json({ message: "Erreur serveur" });
    }
};

/**
 * upload le PDF signé et scanné de l'attestation.
 * @async
 * @function upload
 * @param {import('express').Request} req
 * @param {string} req.params.id - id de l'attestation
 * @param {Express.Multer.File} req.file - fichier PDF uploadé
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // PUT /attestations/1/upload
 * // Body : multipart/form-data avec champ "fichier"
 * // Réponse 200 : { message: "Attestation uploadée", attestation: { ... } }
 * // Réponse 400 : { message: "Aucun fichier fourni" }
 * // Réponse 404 : { message: "Attestation introuvable" }
 */

const upload = async (req, res) => {
    try {
        const { id } = req.params;

        const attestation = await Attestation.findByPk(id);
        if (!attestation) {
            return res.status(404).json({ message: "Attestation introuvable" });
        }

        if (!req.file) {
            return res.status(400).json({ message: "Aucun fichier fourni" });
        }

        attestation.pdf_path = req.file.path;
        attestation.date_signature = new Date();
        await attestation.save();

        res.status(200).json({ message: "Attestation uploadée", attestation });

    } catch (error) {
        console.error('Erreur upload attestation :', error);
        res.status(500).json({ message: "Erreur serveur" });
    }
};

module.exports = { getById, generatePdf, upload };