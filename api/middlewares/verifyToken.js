/**
 * @fileoverview Middleware de vérification du token JWT
 * @module middlewares/verifyToken
 */

const jwt = require('jsonwebtoken');

/**
 * Vérifie la présence et la validité du token JWT dans le header Authorization.
 * Si valide, stocke le payload dans req.user et passe au middleware suivant.
 *
 * @function verifyToken
 * @param {import('express').Request} req - Requête Express
 * @param {import('express').Response} res - Réponse Express
 * @param {import('express').NextFunction} next - Fonction pour passer au middleware suivant
 * @returns {void}
 */
const verifyToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];

  if (!authHeader) {
    return res.status(401).json({ message: 'Token manquant' });
  }

  const token = authHeader.split(' ')[1];

  jwt.verify(token, process.env.JWT_SECRET, (error, payload) => {
    if (error) {
      if (error.name === 'TokenExpiredError') {
        return res.status(401).json({ message: 'Session expirée, veuillez vous reconnecter' });
      }
      return res.status(401).json({ message: 'Token invalide, accès refusé' });
    }

    req.user = payload;
    next();
  });
};

module.exports = verifyToken;