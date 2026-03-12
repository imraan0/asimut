/**
 * @fileoverview Routes d'authentification
 * @module routes/auth
 */

const express = require('express');
const authController = require('../controllers/authController');

const router = express.Router();

/**
 * @route POST /auth/login
 * @desc Authentifie un utilisateur et retourne un token JWT
 * @access Public
 */
router.post('/login', authController.login);

module.exports = router;