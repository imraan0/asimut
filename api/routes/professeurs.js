/**
 * @fileoverview Routes des professeurs
 * @module routes/professeurs
 */

const express = require('express');
const router = express.Router();
const professeurController = require('../controllers/professeurController');
const verifyToken = require('../middlewares/verifyToken');
const checkRole = require('../middlewares/checkRole');

router.get('/me', verifyToken, checkRole('professeur'), professeurController.getMe);
router.get('/:id/eleves',         verifyToken, checkRole('secretariat', 'proviseur', 'professeur'), professeurController.getEleves);
router.get('/',                   verifyToken, checkRole('secretariat', 'proviseur', 'professeur'), professeurController.getAll);

module.exports = router;