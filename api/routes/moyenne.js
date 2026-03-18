/**
 * @fileoverview Routes CRUD des moyennes
 * @module routes/moyennes
 */

const express = require('express');
const router = express.Router();
const moyenneController = require('../controllers/moyenneController');
const verifyToken = require('../middlewares/verifyToken');
const checkRole = require('../middlewares/checkRole');

router.post('/', verifyToken, checkRole('secretariat'), moyenneController.create);
router.put('/:id/valider', verifyToken, checkRole('proviseur'), moyenneController.valider);
router.put('/:id', verifyToken, checkRole('secretariat'), moyenneController.update);

module.exports = router;