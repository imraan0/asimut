/**
 * @fileoverview Contrôleur d'authentification et gestion du login JWT
 * @module controllers/authController
 */

const { Utilisateur } = require('../models');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');

/**
 * Authentifie un utilisateur et retourne un token JWT.
 *
 * @async
 * @function login
 * @param {import('express').Request} req - Requête Express
 * @param {Object} req.body - Corps de la requête
 * @param {string} req.body.email - Email de l'utilisateur
 * @param {string} req.body.mot_de_passe - Mot de passe en clair
 * @param {import('express').Response} res - Réponse Express
 * @returns {Promise<void>}
 *
 * @example
 * // POST /auth/login
 * // Body : { "email": "admin@asimov.fr", "mot_de_passe": "secret" }
 * // Réponse 200 : { "token": "eyJhbGci..." }
 */

const login = async (req, res) => {
  try {
    const { email, mot_de_passe } = req.body;

    if (!email || !mot_de_passe) {
      return res.status(400).json({ message: 'Email et mot de passe sont obligatoires' });
    }

    const utilisateur = await Utilisateur.findOne({ where: { email } });

    if (!utilisateur) {
      return res.status(401).json({ message: 'Email ou mot de passe incorrect' });
    }

    const valide = await bcrypt.compare(mot_de_passe, utilisateur.mot_de_passe);

    if (!valide) {
      return res.status(401).json({ message: 'Email ou mot de passe incorrect' });
    }

    const token = jwt.sign(
      { id: utilisateur.id, role: utilisateur.role },
      process.env.JWT_SECRET,
      { expiresIn: '1h' }
    );

    res.status(200).json({ token });

  } catch (error) {
    console.error('Erreur login:', error);
    res.status(500).json({ message: 'Erreur serveur' });
  }
};

module.exports = { login };