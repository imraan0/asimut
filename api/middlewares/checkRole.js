/**
 * @fileoverview Middleware de vérification du rôle utilisateur
 * @module middlewares/checkRole
 */

/**
 * Vérifie que l'utilisateur connecté possède un des rôles autorisés.
 * Doit être utilisé après le middleware verifyToken.
 *
 * @function checkRole
 * @param {...string} roles - Les rôles autorisés à accéder à la route
 * @returns {import('express').RequestHandler} Middleware Express
 *
 * @example
 * // Autoriser uniquement secretariat et proviseur
 * router.get('/eleves', verifyToken, checkRole('secretariat', 'proviseur'), getEleves)
 */

const checkRole = (...roles) => { //...roles génère un tableau des rôles passés en paramètres
  return (req, res, next) => {
    if (!roles.includes(req.user.role)) { //si le rôle n'est pas dans le tableau
      return res.status(403).json({ message: 'Accès refusé : permissions insuffisantes' });
    }
    next();
  };
};

module.exports = checkRole;