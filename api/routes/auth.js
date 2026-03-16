/**
 * @fileoverview Routes d'authentification
 * @module routes/auth
 */

const express = require('express');
const authController = require('../controllers/authController');

const router = express.Router();

/**
 * @desc Authentifie un utilisateur et retourne un token JWT
 */
router.post('/login', authController.login);

module.exports = router;