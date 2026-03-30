/**
 * @fileoverview Contrôleur du publipostage parents
 * @module controllers/mailController
 */

const nodemailer = require('nodemailer');
const { Parent } = require('../models');

/**
 * Crée et retourne un transporteur nodemailer configuré via .env
 * On le crée en fonction pour éviter de le garder en mémoire inutilement !!! 
 */
const createTransporter = () => nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
    }
});

/**
 * envoie un email à un parent spécifique.
 *
 * @async
 * @function mailOne
 * @param {import('express').Request} req
 * @param {string} req.params.id - id du parent
 * @param {Object} req.body
 * @param {string} req.body.sujet - objet de l'email
 * @param {string} req.body.message - contenu de l'email
 * @param {string} req.body.auteur - nom de l'expéditeur affiché en footer
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // POST /parents/1/mail
 * // Body : { "sujet": "Convocation", "message": "...", "auteur": "M. Martin" }
 * // Réponse 200 : { message: "Email envoyé" }
 * // Réponse 400 : { message: "Champs manquants" }
 * // Réponse 404 : { message: "Parent introuvable" }
 */

const mailOne = async (req, res) => {
    try {
        const { id } = req.params;
        const { sujet, message, auteur } = req.body;

        // Vérifie que tous les champs sont présents
        if (!sujet || !message || !auteur) {
            return res.status(400).json({ message: "Champs manquants" });
        }

        // Vérifie que le parent existe en BDD
        const parent = await Parent.findByPk(id);
        if (!parent) {
            return res.status(404).json({ message: "Parent introuvable" });
        }

        const transporter = createTransporter();

        // Envoie l'email au parent avec l'auteur en footer
        await transporter.sendMail({
            from: process.env.EMAIL_USER,
            to: parent.email,
            subject: sujet,
            text: `${message}\n\n---\n${auteur}` // auteur ajouté en pied de message
        });

        res.status(200).json({ message: "Email envoyé" });

    } catch (error) {
        console.error('Erreur envoi email :', error);
        res.status(500).json({ message: "Erreur serveur" });
    }
};

/**
 * envoie un email à tous les parents de l'établissement.
 *
 * @async
 * @function mailAll
 * @param {import('express').Request} req
 * @param {Object} req.body
 * @param {string} req.body.sujet - objet de l'email
 * @param {string} req.body.message - contenu de l'email
 * @param {string} req.body.auteur - nom de l'expéditeur affiché en footer
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *
 * @example
 * // POST /parents/mail
 * // Body : { "sujet": "Réunion parents", "message": "...", "auteur": "Secrétariat" }
 * // Réponse 200 : { message: "Emails envoyés", total: 42 }
 * // Réponse 400 : { message: "Champs manquants" }
 * // Réponse 404 : { message: "Aucun parent trouvé" }
 */

const mailAll = async (req, res) => {
    try {
        const { sujet, message, auteur } = req.body;

        // Vérifie que tous les champs sont présents
        if (!sujet || !message || !auteur) {
            return res.status(400).json({ message: "Champs manquants" });
        }

        // Récupère tous les parents en BDD
        const parents = await Parent.findAll();
        if (parents.length === 0) {
            return res.status(404).json({ message: "Aucun parent trouvé" });
        }

        const transporter = createTransporter();

        // Envoie un email individuel à chaque parent, sinon les parents verraient les autres mails
        // RGPD : chaque parent ne voit que son propre email, pas ceux des autres
        for (const parent of parents) {
            await transporter.sendMail({
                from: process.env.EMAIL_USER,
                to: parent.email,
                subject: sujet,
                text: `${message}\n\n---\n${auteur}`
            });
        }

        res.status(200).json({ message: "Emails envoyés", total: parents.length });

    } catch (error) {
        console.error('Erreur envoi emails :', error);
        res.status(500).json({ message: "Erreur serveur" });
    }
};

module.exports = { mailOne, mailAll };